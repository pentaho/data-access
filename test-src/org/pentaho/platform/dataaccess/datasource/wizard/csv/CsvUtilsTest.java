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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.util.Util;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;

import java.io.File;
import java.io.PrintWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Andrey Khayrutdinov
 */
public class CsvUtilsTest {

  private static final String delimiter = ";";

  private CsvUtils utils;
  private File tempFile;

  @Before
  public void setUp() throws Exception {
    utils = new CsvUtils();

    tempFile = File.createTempFile( "CsvUtilsTest", ".tmp" );
    tempFile.deleteOnExit();
  }

  @After
  public void tearDown() throws Exception {
    if ( tempFile != null ) {
      tempFile.delete();
    }
  }


  private static void printlnRow( PrintWriter pw, String[] row ) throws Exception {
    if ( row.length == 0 ) {
      return;
    }

    pw.print( row[ 0 ] );
    for ( int i = 1; i < row.length; i++ ) {
      String s = row[ i ];
      pw.print( delimiter );
      pw.print( s );
    }
    pw.println();
  }

  private void prepareFile( String[] headers, String[]... data ) throws Exception {
    PrintWriter pw = new PrintWriter( tempFile );
    try {
      printlnRow( pw, headers );

      if ( data != null ) {
        for ( String[] row : data ) {
          printlnRow( pw, row );
        }
      }
    } finally {
      pw.close();
    }
  }


  private ModelInfo generateFields( int rowLimit, String enclosure, int headerRows ) throws Exception {
    return utils.generateFields( "CsvUtilsTest", tempFile.getAbsolutePath(), tempFile.getName(),
      rowLimit, delimiter, enclosure, headerRows, true, true, "utf-8" );
  }

  @Test
  public void generateFields_OneHeaderLine_OneDataLine() throws Exception {
    prepareFile( new String[] { "col1", "col2" }, new String[] { "1", "2" } );

    ModelInfo info = generateFields( 2, null, 1 );

    ColumnInfo[] columns = info.getColumns();
    assertEquals( 2, columns.length );
    assertEquals( "col1", columns[ 0 ].getId() );
    assertEquals( "col2", columns[ 1 ].getId() );

    DataRow[] data = info.getData();
    assertEquals( 1, data.length );
    assertEquals( 2, data[ 0 ].getCells().length );
    assertEquals( "1", data[ 0 ].getCells()[ 0 ] );
    assertEquals( "2", data[ 0 ].getCells()[ 1 ] );
  }


  @Test
  public void generateFields_EscapesColumnsNames() throws Exception {
    String[] headers = { "col.1", "col  2 */+" };
    for ( String header : headers ) {
      assertFalse( Util.validateId( header ) );
    }
    prepareFile( headers );

    ColumnInfo[] columns = generateFields( 1, null, 1 ).getColumns();

    for ( int i = 0; i < columns.length; i++ ) {
      ColumnInfo column = columns[ i ];
      assertEquals( "Keeps original title", headers[ i ], column.getTitle() );
      assertTrue( "Escapes id", Util.validateId( column.getId() ) );
    }
  }
}
