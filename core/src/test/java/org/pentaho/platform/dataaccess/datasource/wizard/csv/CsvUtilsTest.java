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
 * Copyright (c) 2011-2022 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.core.row.value.ValueMetaDate;
import org.pentaho.di.core.row.value.ValueMetaInteger;
import org.pentaho.di.core.row.value.ValueMetaNone;
import org.pentaho.di.core.row.value.ValueMetaNumber;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.util.Util;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;

import java.io.File;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class CsvUtilsTest {

  private static final int LINES_FOR_READ = 2;

  private static final String DELIMITER = ";";
  private static final int DEFAULT_INTEGER_SIZE = 15;

  private CsvUtils utils;

  private ColumnInfo columnInfo;

  private File tempFile;
  private Locale defaultLocale;

  @Before
  public void setUp() throws Exception {
    defaultLocale = Locale.getDefault();
    Locale.setDefault( new Locale( "en", "US" ) );
    utils = instantiateTestInstance();
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
      pw.print(DELIMITER);
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
            rowLimit, DELIMITER, enclosure, headerRows, true, true, "utf-8" );
  }

  @Test
  public void testGetLines() throws Exception {
    prepareFile( new String[] { "col1", "col2" } );
    String lines = utils.getLines( tempFile.getAbsolutePath(), LINES_FOR_READ, "UTF-8" );
    StringTokenizer tokenizer = new StringTokenizer( lines, DELIMITER);
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
    assertEquals( "#,##0.####", columnInfo.getFormat() );
    assertEquals( 4, columnInfo.getPrecision() );
  }

  @Test
  public void testAssumeColumnDetails_Currency() {
    List<String> samples = asList( "$101.04", "$100.3", "$100.3000", "$100.1", "$11100.32", "$7,100.433", "($500.00)" );
    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.NUMERIC, columnInfo.getDataType() );
    assertEquals( 4, columnInfo.getPrecision() );

    String format = columnInfo.getFormat();
    assertNotNull( format );
    assertEquals( "$#,##0.0000;($#,##0.0000)", format );

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

  /**
   * test all the custom date formats this class passes to constructor for StringEvaluator.java
   * @throws Exception
   */
  @Test
  public void testDateFormat_allFormats() throws Exception {

    SimpleDateFormat formatterBaseline = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
    // create baseline date unambiguous numeric month and date
    Date dateBaseline = formatterBaseline.parse( "2015/10/31 11:57:33" );

    List<String> testDateFormats = getTestDateFormat();
    assertTrue( ColumnInfo.DATE_FORMATS.size() == testDateFormats.size()
            && ColumnInfo.DATE_FORMATS.containsAll( testDateFormats )
            && testDateFormats.containsAll( ColumnInfo.DATE_FORMATS )
    );

    for ( String expectedDateFormat : testDateFormats ) {

      String testSampleDateFormat = new SimpleDateFormat( expectedDateFormat ).format( dateBaseline );

      ColumnInfo actualColumnInfo = new ColumnInfo();

      utils.assumeColumnDetails( actualColumnInfo, asList( testSampleDateFormat ) );
      assertEquals( DataType.DATE, actualColumnInfo.getDataType() );
      assertEquals( expectedDateFormat, actualColumnInfo.getFormat() );
    }
  }

  @Test
  public void testDateFormatDynamic_ambiguousMonthDay() throws Exception {

    SimpleDateFormat formatterBaseline = new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
    // create baseline date ambiguous numeric month and date
    Date dateBaseline = formatterBaseline.parse( "2015/11/10 11:57:33" ); // create baseline date easy to debug

    List<List<String>> allAmbiguousFormatCases = new ArrayList<>();
    allAmbiguousFormatCases.add( Arrays.asList( "dd-MM-yy", "MM-dd-yy" ) );
    allAmbiguousFormatCases.add( Arrays.asList( "dd-MM-yyyy", "MM-dd-yyyy" ) );
    allAmbiguousFormatCases.add( Arrays.asList( "MM/dd/yy", "dd/MM/yy" ) );
    allAmbiguousFormatCases.add( Arrays.asList( "MM/dd/yyyy", "dd/MM/yyyy" ) );

    for (  List<String> ambiguousFormatCase : allAmbiguousFormatCases ) {
      String testDateFormat  = ambiguousFormatCase.get( 0 );
      List<String> expectedDateFormats = ambiguousFormatCase;
      String testDate = new SimpleDateFormat( testDateFormat ).format( dateBaseline );

      ColumnInfo actualColumnInfo = new ColumnInfo();
      CsvUtils testInstance = instantiateTestInstance();

      testInstance.assumeColumnDetails( actualColumnInfo, asList( testDate ) );
      assertEquals( DataType.DATE, actualColumnInfo.getDataType() );
      assertTrue( String.format( "Could not find date '%s' with format '%s' in expectedDateFormats: %s",
                      testDate, actualColumnInfo.getFormat(), expectedDateFormats ),
              expectedDateFormats.contains( actualColumnInfo.getFormat() ) );
    }

  }

  @Test
  public void testConvertDataType() throws Exception {
    ValueMetaInterface vmi = mock( ValueMetaInterface.class );
    when( vmi.getType() ).thenReturn( ValueMetaInterface.TYPE_STRING );

    assertEquals( DataType.STRING, utils.convertDataType( vmi ) );
  }

  @Test
  public void testConvertDataType_numeric() throws Exception {
    assertEquals( DataType.NUMERIC, utils.convertDataType( ValueMetaInterface.TYPE_NUMBER ) );
    assertEquals( DataType.NUMERIC, utils.convertDataType( ValueMetaInterface.TYPE_INTEGER ) );
    assertEquals( DataType.NUMERIC, utils.convertDataType( ValueMetaInterface.TYPE_BIGNUMBER ) );
  }

  @Test
  public void testConvertDataType_date() throws Exception {
    assertEquals( DataType.DATE, utils.convertDataType( ValueMetaInterface.TYPE_DATE ) );
  }

  @Test
  public void testConvertDataType_boolean() throws Exception {
    assertEquals( DataType.BOOLEAN, utils.convertDataType( ValueMetaInterface.TYPE_BOOLEAN ) );
  }

  @Test
  public void testConvertDataType_string() throws Exception {
    assertEquals( DataType.STRING, utils.convertDataType( ValueMetaInterface.TYPE_NONE ) );
    assertEquals( DataType.STRING, utils.convertDataType( ValueMetaInterface.TYPE_STRING ) );
    assertEquals( DataType.STRING, utils.convertDataType( ValueMetaInterface.TYPE_SERIALIZABLE ) );
    assertEquals( DataType.STRING, utils.convertDataType( ValueMetaInterface.TYPE_BINARY ) );
    assertEquals( DataType.STRING, utils.convertDataType( ValueMetaInterface.TYPE_TIMESTAMP ) );
    assertEquals( DataType.STRING, utils.convertDataType( ValueMetaInterface.TYPE_INET ) );
  }

  @Test
  public void testConvertLength() throws Exception {
    assertEquals( 0, utils.convertLength( new ValueMetaNone( "testNone" ) ) );
    assertEquals( 0, utils.convertLength( new ValueMetaNumber( "testNumber", 2, -1 ) ) );
    assertEquals( 12, utils.convertLength( new ValueMetaString( "testString",  8, -1 ) ) );
    assertEquals( 15, utils.convertLength( new ValueMetaInteger( "testInteger", 15, -1 ) ) );
    assertEquals( 8, utils.convertLength( new ValueMetaBigNumber( "testBigNumber", 8, 3 ) ) );
    assertEquals( 0, utils.convertLength( new ValueMetaDate( "testDate" ) ) );
  }

  @Test
  public void testConvertPrecision() throws Exception {
    assertEquals( 5, utils.convertPrecision( new ValueMetaBigNumber( "testBigNumber", 11, 5 ) ) );
    assertEquals( 0, utils.convertPrecision( new ValueMetaDate( "testDate" ) ) );
  }

  @Test
  public void testAssumeColumnDetails() throws Exception {
    ColumnInfo columnInfo = new ColumnInfo();
    List<String> data = asList( "1" );

    utils.assumeColumnDetails( columnInfo, data );

    // testing all the fields are populated correctly
    assertEquals( "#", columnInfo.getFormat() );
    assertEquals( 0, columnInfo.getPrecision() );
    assertEquals( DataType.NUMERIC, columnInfo.getDataType() );
    assertEquals( DEFAULT_INTEGER_SIZE, columnInfo.getLength() );
  }

  private void testAssumeColumnDetails_Dates( List<String> samples, String dateFormat ) {
    utils.assumeColumnDetails( columnInfo, samples );
    assertEquals( DataType.DATE, columnInfo.getDataType() );
    assertEquals( dateFormat, columnInfo.getFormat() );
  }

  private CsvUtils instantiateTestInstance() {
    return new CsvUtils();
  }


  private List<String> getTestDateFormat() {
    /**
     * should be exact copy of {@link ColumnInfo#DATE_FORMATS}
     */
    return  Arrays.asList(
            "MM-dd-yyyy",
            "dd/MM/yyyy",
            "MM-dd-yy",
            "dd/MM/yy",
            "yyyyMMdd",
            "dd-MM-yy",
            "dd-MM-yyyy",
            "MM/dd/yy",
            "MM/dd/yyyy",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd MMM yyyy",
            "dd MMMM yyyy",
            "yyyyMMddHHmm",
            "yyyyMMdd HHmm",
            "dd-MM-yy HH:mm",
            "dd-MM-yyyy HH:mm",
            "MM/dd/yy HH:mm",
            "MM/dd/yyyy HH:mm",
            "yyyy-MM-dd HH:mm",
            "yyyy/MM/dd HH:mm",
            "dd MMM yyyy HH:mm",
            "dd MMMM yyyy HH:mm",
            "yyyyMMddHHmmss",
            "yyyyMMdd HHmmss",
            "dd-MM-yy HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "MM/dd/yy HH:mm:ss",
            "MM/dd/yyyy HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "dd MMM yyyy HH:mm:ss",
            "dd MMMM yyyy HH:mm:ss"
    );
  }

}
