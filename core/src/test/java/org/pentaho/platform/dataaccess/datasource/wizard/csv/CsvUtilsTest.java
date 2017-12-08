/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 11110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2011-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.util.Util;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static java.util.Arrays.asList;

/**
 * @author Andrey Khayrutdinov
 */
public class CsvUtilsTest {

  private static final int LINES_FOR_READ = 2;

  private static final String DELIMETR = ";";
  private static final int DEFAULT_INTEGER_SIZE = 15;

  private CsvUtils utils;

  private ColumnInfo columnInfo;

  private File tempFile;
  private Locale defaultLocale;

  @Before
  public void setUp() throws Exception {
    defaultLocale = Locale.getDefault();
    Locale.setDefault( new Locale( "en", "US" ) );
    utils = new CsvUtils();
    columnInfo = new ColumnInfo();

    tempFile = File.createTempFile( "CsvUtilsTest", ".tmp" );
    tempFile.deleteOnExit();
  }

  @After
  public void tearDown() throws Exception {
    if ( tempFile != null ) {
      tempFile.delete();
    }
    Locale.setDefault( defaultLocale );
  }


  private static void printlnRow( PrintWriter pw, String[] row ) throws Exception {
    if ( row.length == 0 ) {
      return;
    }

    pw.print( row[ 0 ] );
    for ( int i = 1; i < row.length; i++ ) {
      String s = row[ i ];
      pw.print( DELIMETR );
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
      rowLimit, DELIMETR, enclosure, headerRows, true, true, "utf-8" );
  }

  @Test
  public void testGetLines() throws Exception {
    prepareFile( new String[] { "col1", "col2" } );
    String lines = utils.getLines( tempFile.getAbsolutePath(), LINES_FOR_READ, "UTF-8" );
    StringTokenizer tokenizer = new StringTokenizer( lines, DELIMETR );
    assertNotNull( lines );
    assertEquals( LINES_FOR_READ, tokenizer.countTokens() );
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
  public void ColumnOfIntegerType_HasCorrectLength() {
    ColumnInfo columnInfo = new ColumnInfo();
    List<String> data = asList( "1", "2" );

    utils.assumeColumnDetails( columnInfo, data );
    assertEquals( DataType.NUMERIC, columnInfo.getDataType() );
    assertEquals( DEFAULT_INTEGER_SIZE, columnInfo.getLength() );
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

  @Test
  public void testAssumeColumnDetails_Numeric() {
    List<String> samples = asList( "100.00", "100.00", "100.08", "100.00", "100.12", "100.11" );

    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.NUMERIC, columnInfo.getDataType() );
    assertEquals( 2, columnInfo.getPrecision() );

    samples = asList( "12.009", "988,000.3", "9877.9991", "999", "888.11" );
    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.NUMERIC, columnInfo.getDataType() );
    assertEquals( "#,##0.###", columnInfo.getFormat() );
    assertEquals( 4, columnInfo.getPrecision() );
  }

  @Test
  public void testAssumeColumnDetails_Currency() {
    List<String> samples = asList( "$101.04", "$100.3", "$100.3000", "$100.1", "$11100.32", "$7,100.433", "($500.00)" );
    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.NUMERIC, columnInfo.getDataType() );
    assertEquals( 2, columnInfo.getPrecision() );

    String format = columnInfo.getFormat();
    assertNotNull( format );
    assertEquals( "$#,##0.00;($#,##0.00)", format );

    samples = asList( "$101.04", "$100.3", "$100.3000", "$100.1", "not currency" );
    utils.assumeColumnDetails( columnInfo, samples );
    format = columnInfo.getFormat();
    assertNull( format );
  }

  @Test
  public void testAssumeColumnDetails_NumericWithPrecisionAndLength() {
    List<String> samples = asList( "11.1", "11.111", "11.1111" );
    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.NUMERIC, columnInfo.getDataType() );
    assertEquals( 4, columnInfo.getPrecision() );
    assertEquals( 7, columnInfo.getLength() );
  }

  @Test
  public void testDateFormat1() {
    testAssumeColumnDetails_Dates( asList( "20151110" ), "yyyyMMdd" );
  }

  @Test
  public void testDateFormat2() {
    testAssumeColumnDetails_Dates( asList( "10-11-15", "31-12-15" ), "dd-MM-yy" );
  }

  @Test
  public void testDateFormat3() {
    testAssumeColumnDetails_Dates( asList( "10-11-2015", "31-12-2015" ), "dd-MM-yyyy" );
  }

  @Test
  public void testDateFormat4() {
    testAssumeColumnDetails_Dates( asList( "11/10/15", "12/31/15" ), "MM/dd/yy" );
  }

  @Test
  public void testDateFormat5() {
    testAssumeColumnDetails_Dates( asList( "11/10/2015", "12/31/2015" ), "MM/dd/yyyy" );
  }

  @Test
  public void testDateFormat6() {
    testAssumeColumnDetails_Dates( asList( "2015-11-10" ), "yyyy-MM-dd" );
  }

  @Test
  public void testDateFormat7() {
    testAssumeColumnDetails_Dates( asList( "2015/11/10" ), "yyyy/MM/dd" );
  }

  //minutes
  @Test
  public void testDateFormat1m() {
    testAssumeColumnDetails_Dates( asList( "201511101157" ), "yyyyMMddHHmm" );
  }

  @Test
  public void testDateFormat1m2() {
    testAssumeColumnDetails_Dates( asList( "20151110 1157" ), "yyyyMMdd HHmm" );
  }

  @Test
  public void testDateFormat2m() {
    testAssumeColumnDetails_Dates( asList( "10-11-15 11:57" ), "dd-MM-yy HH:mm" );
  }

  @Test
  public void testDateFormat3m() {
    testAssumeColumnDetails_Dates( asList( "10-11-2015 11:57" ), "dd-MM-yyyy HH:mm" );
  }

  @Test
  public void testDateFormat4m() {
    testAssumeColumnDetails_Dates( asList( "11/10/15 11:57" ), "MM/dd/yy HH:mm" );
  }

  @Test
  public void testDateFormat5m() {
    testAssumeColumnDetails_Dates( asList( "11/10/2015 11:57" ), "MM/dd/yyyy HH:mm" );
  }

  @Test
  public void testDateFormat6m() {
    testAssumeColumnDetails_Dates( asList( "2015-11-10 11:57" ), "yyyy-MM-dd HH:mm" );
  }

  @Test
  public void testDateFormat7m() {
    testAssumeColumnDetails_Dates( asList( "2015/11/10 11:57" ), "yyyy/MM/dd HH:mm" );
  }

  //seconds
  @Test
  public void testDateFormat1ms() {
    testAssumeColumnDetails_Dates( asList( "20151110115733" ), "yyyyMMddHHmmss" );
  }

  @Test
  public void testDateFormat1m2s() {
    testAssumeColumnDetails_Dates( asList( "20151110 115733" ), "yyyyMMdd HHmmss" );
  }

  @Test
  public void testDateFormat2ms() {
    testAssumeColumnDetails_Dates( asList( "10-11-15 11:57:33" ), "dd-MM-yy HH:mm:ss" );
  }

  @Test
  public void testDateFormat3ms() {
    testAssumeColumnDetails_Dates( asList( "10-11-2015 11:57:33" ), "dd-MM-yyyy HH:mm:ss" );
  }

  @Test
  public void testDateFormat4ms() {
    testAssumeColumnDetails_Dates( asList( "11/10/15 11:57:33" ), "MM/dd/yy HH:mm:ss" );
  }

  @Test
  public void testDateFormat5ms() {
    testAssumeColumnDetails_Dates( asList( "11/10/2015 11:57:33" ), "MM/dd/yyyy HH:mm:ss" );
  }

  @Test
  public void testDateFormat6ms() {
    testAssumeColumnDetails_Dates( asList( "2015-11-10 11:57:33" ), "yyyy-MM-dd HH:mm:ss" );
  }

  @Test
  public void testDateFormat7ms() {
    testAssumeColumnDetails_Dates( asList( "2015/11/10 11:57:33" ), "yyyy/MM/dd HH:mm:ss" );
  }

  @Test
  public void testTimestampFormat() {
    List<String> samples = asList( "2015-11-10 11:57:33.0" );
    String dateFormat = "yyyy-MM-dd HH:mm:ss.S";
    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.TIMESTAMP, columnInfo.getDataType() );
    assertEquals( dateFormat, columnInfo.getFormat() );
  }

  private void testAssumeColumnDetails_Dates( List<String> samples, String dateFormat ) {
    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.DATE, columnInfo.getDataType() );
    assertEquals( dateFormat, columnInfo.getFormat() );
  }

}
