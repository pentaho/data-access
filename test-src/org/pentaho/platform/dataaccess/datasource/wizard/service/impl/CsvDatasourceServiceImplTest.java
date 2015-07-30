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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class CsvDatasourceServiceImplTest {

  private static final String TMP_DIR = System.getProperty( "java.io.tmpdir" );

  private IApplicationContext mockContext;
  private IApplicationContext existingContext;
  private CsvDatasourceServiceImpl service;

  @Before
  public void setUp() throws Exception {
    assertNotNull( "Temp directory was not found", TMP_DIR + '/' );

    existingContext = PentahoSystem.getApplicationContext();

    mockContext = mock( IApplicationContext.class );
    when( mockContext.getSolutionPath( anyString() ) ).thenReturn( TMP_DIR );
    PentahoSystem.setApplicationContext( mockContext );

    service = new CsvDatasourceServiceImpl();
  }

  @After
  public void tearDown() throws Exception {
    PentahoSystem.setApplicationContext( existingContext );

    mockContext = null;
    service = null;
  }


  @Test( expected = FileNotFoundException.class )
  public void stageFile_InvalidPath_Csv_ThrowsException() throws Exception {
    service.stageFile( "../../../secret-file.csv", ",", "\n", false, "utf-8" );
  }

  @Test( expected = FileNotFoundException.class )
  public void stageFile_InvalidPath_Tmp_ThrowsException() throws Exception {
    service.stageFile( "../../../secret-file.tmp", ",", "\n", false, "utf-8" );
  }

  @Test( expected = FileNotFoundException.class )
  public void stageFile_InvalidPath_WindowsStyle_ThrowsException() throws Exception {
    service.stageFile( "..\\..\\..\\secret-file.csv", ",", "\n", false, "utf-8" );
  }

  @Test( expected = FileNotFoundException.class )
  public void stageFile_InvalidPath_SlashAtEnd_ThrowsException() throws Exception {
    service.stageFile( "../../../secret-file/", ",", "\n", false, "utf-8" );
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
