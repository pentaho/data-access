/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class CsvDatasourceServiceImplTest {

  private static String TMP_DIR;
  private static Path tempDirPath;

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

  @BeforeClass
  public static void setUpClass() throws Exception {
    tempDirPath = Files.createTempDirectory( "CsvDatasourceServiceImplTest" );
    TMP_DIR = tempDirPath.toAbsolutePath().toString();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    Files.deleteIfExists( tempDirPath );
  }

  @Before
  public void setUp() throws Exception {
    assertNotNull( "Temp directory was not found", TMP_DIR );

    existingContext = PentahoSystem.getApplicationContext();

    mockContext = mock( IApplicationContext.class );
    when( mockContext.getSolutionPath( any() ) ).thenReturn( TMP_DIR + '/' );
    PentahoSystem.setApplicationContext( mockContext );

    service = new CsvDatasourceServiceImpl();

    //Skip permission check by default
    final ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( "data-access-override", "false" ) ).thenReturn( "true" );
    PentahoSystem.setSystemSettingsService( systemSettings );
    PentahoSessionHolder.setSession( mock( IPentahoSession.class ) );
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
      fail( "Should throw exception" );
    } catch ( Exception e ) {
      String message = e.getMessage();
      assertFalse( message, message.contains( TMP_DIR ) );
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
      assertEquals( "One header row", 1, fileInfo.getHeaderRows() );
      assertEquals( "Header + content row", 2, fileInfo.getContents().size() );
      assertEquals( filename, fileInfo.getTmpFilename() );
    } finally {
      file.delete();
    }
  }

  @Test
  public void testNoPermissions() throws Exception {
    final ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( "data-access-override", "false" ) ).thenReturn( "false" );
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
      assertTrue( thrown );
      thrown = false;
      try {
        service.getStagedFiles();
      } catch ( SecurityException e ) {
        thrown = true;
      }
      assertTrue( thrown );
      thrown = false;
      try {
        service.getPreviewRows( filename, true, 1, "utf-8" );
      } catch ( SecurityException e ) {
        thrown = true;
      }
      assertTrue( thrown );
      thrown = false;
      try {
        service.getEncoding( filename );
      } catch ( SecurityException e ) {
        thrown = true;
      }
      assertTrue( thrown );
      thrown = false;
      try {
        service.generateDomain( mock( DatasourceDTO.class ) );
      } catch ( SecurityException e ) {
        thrown = true;
      }
      assertTrue( thrown );
    } finally {
      file.delete();
    }
  }

  @Test
  public void testHasPermissions() throws Exception {
    hasPermissions = true;
    final ISystemSettings systemSettings = mock( ISystemSettings.class );
    when( systemSettings.getSystemSetting( "data-access-override", "false" ) ).thenReturn( "false" );
    PentahoSystem.setSystemSettingsService( systemSettings );


    String filename = "anotherStageFile_CsvFile.csv";
    File file = createTmpCsvFile( filename );
    file.deleteOnExit();

    try {

      ModelInfo modelInfo = service.stageFile( filename, ",", "\n", true, "utf-8" );
      CsvFileInfo fileInfo = modelInfo.getFileInfo();
      assertEquals( "One header row", 1, fileInfo.getHeaderRows() );
      assertEquals( "Header + content row", 2, fileInfo.getContents().size() );
      assertEquals( filename, fileInfo.getTmpFilename() );

      final FileInfo[] stagedFiles = service.getStagedFiles();

      assertNotNull( stagedFiles );
      boolean present = false;

      for ( FileInfo info : stagedFiles ) {
        if ( filename.equals( info.getName() ) ) {
          present = true;
          break;
        }
      }
      assertTrue( present );

      final String encoding = service.getEncoding( filename );
      assertNotNull( encoding  );

      final List<String> previewRows = service.getPreviewRows( filename, true, 1, "utf-8" );
      assertNotNull( previewRows );
      assertEquals( 1, previewRows.size() );
      assertEquals( "col1,col2", previewRows.get( 0 ) );


      final DatasourceDTO datasourceDto = mock( DatasourceDTO.class );
      when( datasourceDto.getCsvModelInfo() ).thenReturn( modelInfo );
      try {
        final FileTransformStats fileTransformStats = service.generateDomain( datasourceDto );
      } catch ( Exception e ) {
        //Testing this logic is not a purpose of this junit
      }
      //Passed permissions check
      verify( datasourceDto, times( 1 ) ).getCsvModelInfo();
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
