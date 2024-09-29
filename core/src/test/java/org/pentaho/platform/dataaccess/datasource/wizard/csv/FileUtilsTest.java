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


package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import org.junit.Test;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileUtilsTest {

  @Test
  public void testListFiles() {
    FileUtils fu = mock( FileUtils.class );
    // Workaround related to the static initializer in FileUtils
    when( fu.getRelativeSolutionPath() ).thenReturn( "target/test-classes/solution1/system/metadata/csvfiles/" );
    when( fu.listFiles() ).thenCallRealMethod();
    FileInfo [] files = fu.listFiles();
    assertTrue( files != null && files.length > 0 );
  }

  @Test
  public void testDeleteFile() {
    FileUtils fu = new FileUtils();
    String testUniqueFileName = "testFileToDelete-" + System.currentTimeMillis();
    File testUniqueFile = new File( fu.getRelativeSolutionPath() + File.separatorChar + testUniqueFileName );
    try {
      testUniqueFile.createNewFile();
    } catch ( IOException  ioe ) {
      fail( "File creation failed" );
    }
    //Make sure the file got deleted
    if ( !testUniqueFile.exists() ) {
      fail( "File could not be created" );
    }

    FileInfo [] files = fu.listFiles();
    if ( files != null && files.length > 0 ) {
      fu.deleteFile( testUniqueFileName );
      FileInfo [] files1 = fu.listFiles();
      System.err.println( "File1 length " + files1.length  );
      System.err.println( "Files length " + files.length  );
      try {
        assertTrue( files1.length < files.length );
      } finally {
        for ( FileInfo file : files1 ) {
          if ( file.getName().contains( testUniqueFileName ) ) {
            testUniqueFile.deleteOnExit();
            fail( "File should have been deleted. We will delete the file ourselves" );
          }
        }
      }
    }
  }
}
