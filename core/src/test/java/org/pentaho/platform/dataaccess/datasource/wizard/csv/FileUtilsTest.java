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

package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;

public class FileUtilsTest {

  @Test
  public void testListFiles() {
    FileUtils fu = new FileUtils();
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
