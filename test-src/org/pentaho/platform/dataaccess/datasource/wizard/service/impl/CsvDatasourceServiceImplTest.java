/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISystemSettings;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import org.junit.Assert;
import org.mockito.Matchers;
import org.mockito.Mockito;


/**
 * @author Andrey Khayrutdinov
 */
public class CsvDatasourceServiceImplTest {

  private static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );

  private IApplicationContext mockContext;
  private IApplicationContext existingContext;
  private CsvDatasourceServiceImpl service;
  private static boolean hasPermissions;
  private final IAuthorizationPolicy policy = new IAuthorizationPolicy() {
    @Override public boolean isAllowed( String s ) {
      return hasPermissions;
    }

    @Override public List<String> getAllowedActions( String s ) {
      return null;
    }
  };

  @Before
  public void setUp() throws Exception {
    Assert.assertNotNull( "Temp directory was not found", TMP_DIR );

    existingContext = PentahoSystem.getApplicationContext();

    mockContext = Mockito.mock( IApplicationContext.class );
    Mockito.when( mockContext.getSolutionPath( Matchers.anyString() ) ).thenReturn( TMP_DIR + '/' );
    PentahoSystem.setApplicationContext( mockContext );

    service = new CsvDatasourceServiceImpl();

    //Skip permission check by default
    final ISystemSettings systemSettings =  Mockito.mock( ISystemSettings.class );
    Mockito.when( systemSettings.getSystemSetting( "data-access-override", "false" ) ).thenReturn( "true" );
    PentahoSystem.setSystemSettingsService( systemSettings );
    PentahoSessionHolder.setSession( Mockito.mock( IPentahoSession.class ) );
    PentahoSystem.registerObject( policy );
  }

  @After
  public void tearDown() throws Exception {
    hasPermissions = false;
    PentahoSystem.setApplicationContext( existingContext );
    PentahoSessionHolder.removeSession();

    mockContext = null;
    service = null;
  }


  @Test( expected = Exception.class )
  public void stageFile_InvalidPath_Csv_ThrowsException() throws Exception {
    service.stageFile( "../../../secret-file.csv", ",", "\n", false, "utf-8" );
  }

  @Test( expected = Exception.class )
  public void stageFile_InvalidPath_Tmp_ThrowsException() throws Exception {
    service.stageFile( "../../../secret-file.tmp", ",", "\n", false, "utf-8" );
  }

  @Test( expected = Exception.class )
  public void stageFile_InvalidPath_WindowsStyle_ThrowsException() throws Exception {
    service.stageFile( "..\\..\\..\\secret-file.csv", ",", "\n", false, "utf-8" );
  }

  @Test( expected = Exception.class )
  public void stageFile_InvalidPath_SlashAtEnd_ThrowsException() throws Exception {
    service.stageFile( "../../../secret-file/", ",", "\n", false, "utf-8" );
  }


  @Test
  public void stageFile_InvalidPath_DoesNotRevealInternalDetails() throws Exception {
    try {
      service.stageFile( "../../../secret-file.tmp", ",", "\n", false, "utf-8" );
      Assert.fail( "Should throw exception" );
    } catch ( Exception e ) {
      String message = e.getMessage();
      Assert.assertFalse( message, message.contains( TMP_DIR ) );
    }
  }


  @Test
  public void stageFile_CsvFile() throws Exception {
    String filename = "stageFile_CsvFile.csv";
    File file = createTmpCsvFile( filename );
    file.deleteOnExit();
    try {
      ModelInfo modelInfo = service.stageFile( filename, ",", "\n", true, "utf-8" );
      CsvFileInfo fileInfo = modelInfo.getFileInfo();
      Assert.assertEquals( "One header row", 1, fileInfo.getHeaderRows() );
      Assert.assertEquals( "Header + content row", 2, fileInfo.getContents().size() );
      Assert.assertEquals( filename, fileInfo.getTmpFilename() );
    } finally {
      file.delete();
    }
  }

  @Test
  public void testNoPermissions() throws Exception {
    final ISystemSettings systemSettings =  Mockito.mock( ISystemSettings.class );
    Mockito.when( systemSettings.getSystemSetting( "data-access-override", "false" ) ).thenReturn( "false" );
    PentahoSystem.setSystemSettingsService( systemSettings );

    String filename = "stageFile_CsvFile.csv";
    File file = createTmpCsvFile( filename );
    file.deleteOnExit();
    try {
      boolean thrown = false;
      try {
        service.stageFile( filename, ",", "\n", true, "utf-8" );
      } catch ( SecurityException e ) {
        thrown = true;
      }
      Assert.assertTrue( thrown );
      thrown = false;
      try {
        service.getStagedFiles();
      } catch ( SecurityException e ) {
        thrown = true;
      }
      Assert.assertTrue( thrown );
      thrown = false;
      try {
        service.getPreviewRows( filename, true, 1, "utf-8" );
      } catch ( SecurityException e ) {
        thrown = true;
      }
      Assert.assertTrue( thrown );
      thrown = false;
      try {
        service.getEncoding( filename );
      } catch ( SecurityException e ) {
        thrown = true;
      }
      Assert.assertTrue( thrown );
      thrown = false;
      try {
        service.generateDomain(  Mockito.mock( DatasourceDTO.class ) );
      } catch ( SecurityException e ) {
        thrown = true;
      }
      Assert.assertTrue( thrown );
    } finally {
      file.delete();
    }
  }

  @Test
  public void testHasPermissions() throws Exception {
    hasPermissions = true;
    final ISystemSettings systemSettings =  Mockito.mock( ISystemSettings.class );
    Mockito.when( systemSettings.getSystemSetting( "data-access-override", "false" ) ).thenReturn( "false" );
    PentahoSystem.setSystemSettingsService( systemSettings );


    String filename = "anotherStageFile_CsvFile.csv";
    File file = createTmpCsvFile( filename );
    file.deleteOnExit();

    try {

      ModelInfo modelInfo = service.stageFile( filename, ",", "\n", true, "utf-8" );
      CsvFileInfo fileInfo = modelInfo.getFileInfo();
      Assert.assertEquals( "One header row", 1, fileInfo.getHeaderRows() );
      Assert.assertEquals( "Header + content row", 2, fileInfo.getContents().size() );
      Assert.assertEquals( filename, fileInfo.getTmpFilename() );

      final FileInfo[] stagedFiles = service.getStagedFiles();

      Assert.assertNotNull( stagedFiles );
      boolean present = false;

      for ( FileInfo info : stagedFiles ) {
        if ( filename.equals( info.getName() ) ) {
          present = true;
          break;
        }
      }
      Assert.assertTrue( present );

      final String encoding = service.getEncoding( filename );
      Assert.assertNotNull( encoding  );

      final List<String> previewRows = service.getPreviewRows( filename, true, 1, "utf-8" );
      Assert.assertNotNull( previewRows );
      Assert.assertEquals( 1, previewRows.size() );
      Assert.assertEquals( "col1,col2", previewRows.get( 0 ) );


      final DatasourceDTO datasourceDto =  Mockito.mock( DatasourceDTO.class );
      Mockito.when( datasourceDto.getCsvModelInfo() ).thenReturn( modelInfo );
      try {
        final FileTransformStats fileTransformStats = service.generateDomain( datasourceDto );
      } catch ( Exception e ) {
        //Testing this logic is not a purpose of this junit
      }
      //Passed permissions check
      Mockito.verify( datasourceDto,  Mockito.times( 1 ) ).getCsvModelInfo();
    } finally {
      file.delete();
    }

  }


  private static File createTmpCsvFile( String filename ) throws Exception {
    File csvFile = new File( TMP_DIR, filename );

    PrintWriter pw = new PrintWriter( csvFile );
    try {
      pw.println( "col1,col2" );
      pw.println( "1,2" );
    } finally {
      pw.close();
    }

    return csvFile;
  }
}
