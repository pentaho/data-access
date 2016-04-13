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
* Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.junit.Assert;
import org.mockito.Mockito;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBigNumber;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
import org.pentaho.test.platform.engine.core.BaseTest;

@SuppressWarnings( { "all" } )
public class CsvTransformGeneratorIT extends BaseTest {

  private static final String solution = "testsolution"; //$NON-NLS-1$

  private static final String SOLUTION_PATH = "test-res/solution1/"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-res/solution11"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

  private static final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$

  static class CutLongNamesStepInputContext {
    String[] fieldNames = new String[] {"a", "b", "A_1", "b_1", "LONGlonglong", "longlonglong_again", "a_2", };
    int[] fieldLengths = new int[] { 5, 6, 10, 15, 12, 7, 4 };
    int[] fieldPrecisions = new int[] { 0, 0, 1, 1, 2, 0, 2 };
    RowMetaInterface fields = new RowMeta();
    {
      for ( int i = 0; i < fieldNames.length; i++ ) {
        final ValueMetaInterface valueMeta = new ValueMetaBigNumber( fieldNames[i] );
        valueMeta.setLength( fieldLengths[i] );
        valueMeta.setPrecision( fieldPrecisions[i] );
        fields.addValueMeta( valueMeta );
      }
    }
  }

  public String getSolutionPath() {
    File file = new File( SOLUTION_PATH + PENTAHO_XML_PATH );
    if ( file.exists() ) {
      System.out.println( "File exist returning " + SOLUTION_PATH ); //$NON-NLS-1$
      return SOLUTION_PATH;
    } else {
      System.out.println( "File does not exist returning " + ALT_SOLUTION_PATH ); //$NON-NLS-1$
      return ALT_SOLUTION_PATH;
    }
  }

  private void init() {
    if ( !PentahoSystem.getInitializedOK() ) {
      IApplicationContext context = new StandaloneApplicationContext( SOLUTION_PATH, "." ); //$NON-NLS-1$
      PentahoSystem.init( context );
    }
  }

  public void testGoodTransform() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    String KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL = System.getProperty( "KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL", "N" );
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, getDatabaseMeta() );

    gen.preview( session );

    DataRow[] rows = info.getData();
    assertNotNull( rows );
    assertEquals( 235, rows.length );

    Date testDate = new Date();
    testDate.setDate( 1 );
    testDate.setHours( 0 );
    testDate.setMinutes( 0 );
    testDate.setMonth( 0 );
    testDate.setSeconds( 0 );
    testDate.setYear( 110 );

    // test the first row
    // test the data types
    DataRow row = rows[ 0 ];
    assertNotNull( row );
    Object[] cells = row.getCells();
    assertNotNull( cells );
    //    assertEquals( 8, cells.length );
    assertTrue( cells[ 0 ] instanceof Long );
    assertTrue( cells[ 1 ] instanceof Double );
    assertTrue( cells[ 2 ] instanceof Long );
    assertTrue( cells[ 3 ] instanceof Date );
    assertTrue( cells[ 4 ] instanceof String );
    assertTrue( cells[ 5 ] instanceof Long );
    assertTrue( cells[ 6 ] instanceof Double );
    assertTrue( cells[ 7 ] instanceof Boolean );
    // test the values
    assertEquals( (long) 3, cells[ 0 ] );
    assertEquals( 25677.96525, cells[ 1 ] );
    assertEquals( (long) 1231, cells[ 2 ] );
    assertEquals( testDate.getYear(), ( (Date) cells[ 3 ] ).getYear() );
    assertEquals( testDate.getMonth(), ( (Date) cells[ 3 ] ).getMonth() );
    assertEquals( testDate.getDate(), ( (Date) cells[ 3 ] ).getDate() );
    assertEquals( testDate.getHours(), ( (Date) cells[ 3 ] ).getHours() );
    //    assertEquals( testDate.getMinutes(), ((Date)cells[3]).getMinutes() ); this fails, a bug in the PDI date
    // parsing?
    assertEquals( testDate.getSeconds(), ( (Date) cells[ 3 ] ).getSeconds() );

    //    assertEquals( testDate, cells[3] );
    assertEquals( "Afghanistan", cells[ 4 ] );
    assertEquals( (long) 11, cells[ 5 ] );
    assertEquals( 111.9090909, cells[ 6 ] );
    assertEquals( false, cells[ 7 ] );

    // test the second row
    testDate.setDate( 2 );
    // test the data types
    row = rows[ 1 ];
    assertNotNull( row );
    cells = row.getCells();
    assertNotNull( cells );
    assertTrue( cells[ 0 ] instanceof Long );
    assertTrue( cells[ 1 ] instanceof Double );
    assertTrue( cells[ 2 ] instanceof Long );
    assertTrue( cells[ 3 ] instanceof Date );
    if ( "Y".equals( KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL ) ) {
      assertTrue( "".equals( cells[ 4 ] ) );
    } else {
      assertTrue( cells[ 4 ] == null );
    }
    assertTrue( cells[ 5 ] instanceof Long );
    assertTrue( cells[ 6 ] instanceof Double );
    assertTrue( cells[ 7 ] instanceof Boolean );
    // test the values
    assertEquals( (long) 4, cells[ 0 ] );
    assertEquals( 24261.81026, cells[ 1 ] );
    assertEquals( (long) 1663, cells[ 2 ] );
    assertEquals( testDate.getYear(), ( (Date) cells[ 3 ] ).getYear() );
    assertEquals( testDate.getMonth(), ( (Date) cells[ 3 ] ).getMonth() );
    assertEquals( testDate.getDate(), ( (Date) cells[ 3 ] ).getDate() );
    assertEquals( testDate.getHours(), ( (Date) cells[ 3 ] ).getHours() );
    //    assertEquals( testDate.getMinutes(), ((Date)cells[3]).getMinutes() ); this fails, a bug in the PDI date
    // parsing?
    assertEquals( testDate.getSeconds(), ( (Date) cells[ 3 ] ).getSeconds() );

    //    assertEquals( testDate, cells[3] );
    if ( "Y".equals( KETTLE_EMPTY_STRING_DIFFERS_FROM_NULL ) ) {
      assertEquals( "", cells[ 4 ] );
      assertEquals( cells[ 4 ], "" );
    } else {
      assertEquals( null, cells[ 4 ] ); // IfNull value does not seem to work
    }

    assertEquals( (long) 7, cells[ 5 ] );
    assertEquals( 237.5714286, cells[ 6 ] );
    assertEquals( true, cells[ 7 ] );

  }

  public void testCreateTable() throws Exception {

    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );

    DatabaseMeta dbMeta = getDatabaseMeta();
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, dbMeta );

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), dbMeta, null );
    } catch ( CsvTransformGeneratorException e ) {
      // it is OK if the table doesn't exist previously
    }
    gen.createOrModifyTable( session );

    // check the results
    long rowCount = this.getRowCount( tableName );
    assertEquals( (long) 0, rowCount );
  }

  public void testCreateTable_longColumnNames() throws Exception {

    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );

    DatabaseMeta dbMeta = getDatabaseMeta();
    ModelInfo info = createModel();
    CsvTransformGenerator gen = spy( new CsvTransformGenerator( info, dbMeta ) );
    doReturn( 8 ).when( gen ).getMaxColumnNameLength();

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), dbMeta, null );
    } catch ( CsvTransformGeneratorException e ) {
      // it is OK if the table doesn't exist previously
    }
    gen.createOrModifyTable( session );

    // check the results
    long rowCount = this.getRowCount( tableName );
    assertEquals( (long) 0, rowCount );
  }

  public void testCreateTable_littleMaxColumnNameLength() throws Exception {

    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );

    DatabaseMeta dbMeta = getDatabaseMeta();
    ModelInfo info = createModel();
    CsvTransformGenerator gen = spy( new CsvTransformGenerator( info, dbMeta ) );
    doReturn( 1 ).when( gen ).getMaxColumnNameLength();

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), dbMeta, null );
    } catch ( CsvTransformGeneratorException e ) {
      // it is OK if the table doesn't exist previously
    }
    gen.createOrModifyTable( session );

    // check the results
    long rowCount = this.getRowCount( tableName );
    assertEquals( (long) 0, rowCount );
  }

  /**
   * Given a name of an existing table to drop.
   * <br/>
   * When StagingTransformGenerator is called to drop this table,
   * then it should execute drop statement.
   */
  public void testDropExistingTable() throws Exception {

    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );

    DatabaseMeta dbMeta = getDatabaseMeta();
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, dbMeta );

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), dbMeta, null );
    } catch ( CsvTransformGeneratorException e ) {
      // it is OK if the table doesn't exist previously
    }
    gen.createOrModifyTable( session );

    // check the results
    long rowCount = this.getRowCount( tableName );
    assertEquals( (long) 0, rowCount );

    // now make sure I can drop the table as well
    gen.dropTable( tableName );
    try {
      this.getRowCount( tableName );
      fail();
    } catch ( Exception e ) {
      // expect the table to not exist
    }

  }

  /**
   * Given a name of a non-existing table to drop.
   * <br/>
   * When StagingTransformGenerator is called to drop this table,
   * then it shouldn't execute drop statement.
   */
  public void testDropNonExistingTable() throws Exception {

    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );

    DatabaseMeta dbMeta = getDatabaseMeta();
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, dbMeta );

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), dbMeta, null );
    } catch ( CsvTransformGeneratorException e ) {
      // it is OK if the table doesn't exist previously
    }

    // now make sure we do not execute drop statement for non-existing table
    try {
      gen.dropTable( tableName );
    } catch ( CsvTransformGeneratorException e ) {
      // no need to forward exception, just fail the test
      fail();
    }

  }

  public void testSemiColonAfterQuoteIsFound() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );

    DatabaseMeta realDbMeta = getDatabaseMeta();
    ModelInfo info = createModel();

    final DatabaseMeta dbMeta = Mockito.mock( DatabaseMeta.class );
    final Database db = Mockito.mock( Database.class );
    CsvTransformGenerator gen = new CsvTransformGenerator( info, realDbMeta ) {
      @Override Database getDatabase( final DatabaseMeta databaseMeta ) {
        assertSame( dbMeta, databaseMeta );
        return db;
      }
    };
    gen.execSqlStatement(
      "UPDATE \"csv_test4\" SET \"YEAR_ID_KTL\"=\"YEAR_ID\";\nALTER TABLE \"csv_test4\" DROP ( \"YEAR_ID\" )",
      dbMeta, new StringBuilder() );
    Mockito.verify( db ).execStatement( "UPDATE \"csv_test4\" SET \"YEAR_ID_KTL\"=\"YEAR_ID\"" );
    Mockito.verify( db ).execStatement( "ALTER TABLE \"csv_test4\" DROP ( \"YEAR_ID\" )" );

    //again but with single quotes
    gen.execSqlStatement(
      "UPDATE \"csv_test4\" SET \"YEAR_ID_KTL\"=\'YEAR_ID\';\nALTER TABLE \"csv_test4\" DROP ( \'YEAR_ID\' )",
      dbMeta, new StringBuilder() );
    Mockito.verify( db ).execStatement( "UPDATE \"csv_test4\" SET \"YEAR_ID_KTL\"=\'YEAR_ID\'" );
    Mockito.verify( db ).execStatement( "ALTER TABLE \"csv_test4\" DROP ( \'YEAR_ID\' )" );
  }

  // Test helper to create an in-memory database to use
  private static DatabaseMeta getDatabaseMeta() {
    //------------------------------------------------------------------------
    // modify /test-res/solution1/system/data-access/settings.xml to change
    // simple-jndi connection
    //------------------------------------------------------------------------
    return AgileHelper.getDatabaseMeta();
  }

  private static String getDropTableStatement( String tableName ) {
    return String.format( "DROP TABLE %s;", tableName );
  }

  public void testLoadTable1() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, getDatabaseMeta() );

    // create the model
    String tableName = info.getStageTableName();
    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), getDatabaseMeta(), null );
    } catch ( CsvTransformGeneratorException e ) {
      // table might not be there yet, it is OK
    }

    // generate the database table
    gen.createOrModifyTable( session );

    // load the table
    loadTable( gen, info, true, session );

    // check the results
    long rowCount = this.getRowCount( tableName );
    assertEquals( (long) 235, rowCount );
    DatabaseMeta databaseMeta = getDatabaseMeta();
    assertNotNull( databaseMeta );
    Database database = new Database( databaseMeta );
    assertNotNull( database );
    database.connect();

    Connection connection = null;
    Statement stmt = null;
    ResultSet sqlResult = null;

    try {
      connection = database.getConnection();
      assertNotNull( connection );
      stmt = database.getConnection().createStatement();

      // check the first row
      Date testDate = new Date();
      testDate.setDate( 1 );
      testDate.setHours( 0 );
      testDate.setMinutes( 0 );
      testDate.setMonth( 0 );
      testDate.setSeconds( 0 );
      testDate.setYear( 110 );
      boolean ok = stmt.execute( "select * from " + tableName );
      assertTrue( ok );
      sqlResult = stmt.getResultSet();
      assertNotNull( sqlResult );
      ok = sqlResult.next();
      assertTrue( ok );

      // test the values
      assertEquals( (long) 3, sqlResult.getLong( 1 ) );
      assertEquals( 25677.96525, sqlResult.getDouble( 2 ) );
      assertEquals( (long) 1231, sqlResult.getLong( 3 ) );
      assertEquals( testDate.getYear(), sqlResult.getDate( 4 ).getYear() );
      assertEquals( testDate.getMonth(), sqlResult.getDate( 4 ).getMonth() );
      assertEquals( testDate.getDate(), sqlResult.getDate( 4 ).getDate() );
      assertEquals( testDate.getHours(), sqlResult.getTime( 4 ).getHours() );
      //    assertEquals( testDate.getMinutes(), ((Date)cells[3]).getMinutes() ); this fails, a bug in the PDI date
      // parsing?
      assertEquals( testDate.getSeconds(), sqlResult.getTime( 4 ).getSeconds() );

      //    assertEquals( testDate, cells[3] );
      assertEquals( "Afghanistan", sqlResult.getString( 5 ) );
      assertEquals( (long) 11, sqlResult.getLong( 6 ) );
      assertEquals( 111.9090909, sqlResult.getDouble( 7 ) );
      assertEquals( false, sqlResult.getBoolean( 8 ) );
    } finally {
      sqlResult.close();
      stmt.close();
      connection.close();
    }

  }

  private CsvTransformGenerator getCleanTransformGen() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    // create the model
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, getDatabaseMeta() );

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), getDatabaseMeta(), null );
    } catch ( CsvTransformGeneratorException e ) {
      // table might not be there yet, it is OK
    }

    return gen;
  }

  public void testModifyEmptyTable_AddColumn() throws Exception {
    CsvTransformGenerator gen = getCleanTransformGen();
    IPentahoSession session = new StandaloneSession( "test" );

    // create the model
    ModelInfo info = gen.getModelInfo();

    // generate the database table initially
    gen.createOrModifyTable( session );

    // now, lets update it by changing the model info slightly.. add a column
    addColumnToModel( info );

    gen.createOrModifyTable( session );

    // make sure the table has an extra integer column in it
    String tableName = gen.getTableName();
    String sql = "select " + info.getColumns()[ info.getColumns().length - 1 ].getId()
      + " from " + tableName + ";";
    gen.execSqlStatement( sql, getDatabaseMeta(), null );
  }

  public void testModifyEmptyTable_RemoveColumn() throws Exception {
    CsvTransformGenerator gen = getCleanTransformGen();
    IPentahoSession session = new StandaloneSession( "test" );

    // create the model
    ModelInfo info = gen.getModelInfo();

    // generate the database table initially
    gen.createOrModifyTable( session );

    String removedColumn = info.getColumns()[ info.getColumns().length - 1 ].getId();

    // now, lets update it by changing the model info slightly.. add a column
    removeColumnFromModel( info );

    gen.createOrModifyTable( session );

    // make sure the table has an extra integer column in it
    String tableName = info.getStageTableName();
    String sql = "select " + removedColumn + " from " + tableName + ";";
    try {
      gen.execSqlStatement( sql, getDatabaseMeta(), null );
      fail( "Column should have been removed and an error raised" );
    } catch ( CsvTransformGeneratorException e ) {
      //expected, the column should not be there to select
    }

  }

  public void testCreateOrModifyTable_NullInput() throws Exception {
    CsvTransformGenerator gen = getCleanTransformGen();
    // generate the database table initially
    try {
      gen.setTableName( null );
      gen.createOrModifyTable( null );
      fail( "IllegalArgumentException should be thrown if a null is passed into createOrModifyTable" );
    } catch ( IllegalArgumentException e ) {
      // expected
    }
  }

  public void testLoadTable_NullModelInfo() throws Exception {
    CsvTransformGenerator gen = getCleanTransformGen();
    // generate the database table initially
    try {
      IPentahoSession session = new StandaloneSession( "test" );
      gen.setTableName( null );
      gen.loadTable( true, session, false );
      fail( "IllegalArgumentException should be thrown if a null is passed into loadTable for ModelInfo" );
    } catch ( IllegalArgumentException e ) {
      // expected
    }
  }

  public void testLoadTable_NullSession() throws Exception {
    CsvTransformGenerator gen = getCleanTransformGen();

    // generate the database table initially
    try {
      gen.loadTable( true, null, false );
      fail( "IllegalArgumentException should be thrown if a null is passed into loadTable for IPentahoSession" );
    } catch ( IllegalArgumentException e ) {
      // expected
    }
  }

  public void testLoadTableTruncate() throws Exception {

    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    // create the model
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, getDatabaseMeta() );

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), getDatabaseMeta(), null );
    } catch ( CsvTransformGeneratorException e ) {
      // table might not be there yet, it is OK
    }

    // generate the database table
    gen.createOrModifyTable( session );

    // load the table
    loadTable( gen, info, true, session );

    // check the results
    long rowCount = this.getRowCount( tableName );
    assertEquals( (long) 235, rowCount );

    // load again, no truncate
    loadTable( gen, info, false, session );

    // check the results
    rowCount = this.getRowCount( tableName );
    assertEquals( (long) 470, rowCount );

    // load again, with truncate
    loadTable( gen, info, true, session );

    // check the results
    rowCount = this.getRowCount( tableName );
    assertEquals( (long) 235, rowCount );
  }

  private int loadTable( CsvTransformGenerator gen, ModelInfo info, boolean truncate, IPentahoSession session )
    throws InterruptedException, CsvTransformGeneratorException {

    gen.loadTable( truncate, session, false );

    FileTransformStats stats = gen.getTransformStats();

    // wait until it it done
    while ( !stats.isRowsFinished() ) {
      Thread.sleep( 100 );
    }
    return 1;
  }

  public void testCreateIndex() throws Exception {

    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    // create the model
    ModelInfo info = createModel();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, getDatabaseMeta() );

    String tableName = info.getStageTableName();

    try {
      gen.execSqlStatement( getDropTableStatement( tableName ), getDatabaseMeta(), null );
    } catch ( CsvTransformGeneratorException e ) {
      // table might not be there yet, it is OK
    }

    // generate the database table
    gen.createOrModifyTable( session );

    // load the table
    loadTable( gen, info, true, session );

    // check the results
    long rowCount = this.getRowCount( tableName );
    assertEquals( (long) 235, rowCount );

    int indexCount = gen.createIndices( session );
    assertEquals( 5, indexCount );
  }

  private long getRowCount( String tableName ) throws Exception {
    DatabaseMeta databaseMeta = getDatabaseMeta();
    assertNotNull( databaseMeta );
    Database database = new Database( databaseMeta );
    assertNotNull( database );
    database.connect();

    Connection connection = null;
    Statement stmt = null;
    ResultSet sqlResult = null;
    try {
      connection = database.getConnection();
      assertNotNull( connection );
      stmt = database.getConnection().createStatement();
      boolean ok = stmt.execute( "select count(*) from " + tableName );
      assertTrue( ok );
      sqlResult = stmt.getResultSet();
      assertNotNull( sqlResult );
      ok = sqlResult.next();
      assertTrue( ok );
      return sqlResult.getLong( 1 );
    } finally {
      if ( sqlResult != null ) {
        sqlResult.close();
      }
      if ( stmt != null ) {
        stmt.close();
      }
      if ( connection != null ) {
        connection.close();
      }
    }
  }

  private static void addColumnToModel( ModelInfo info ) {
    ColumnInfo[] columns = info.getColumns();

    ColumnInfo col = new ColumnInfo();
    //    col.setDataType(ValueMeta.getTypeDesc(ValueMeta.TYPE_INTEGER));
    col.setDataType( DataType.NUMERIC );
    col.setId( "PC_999" );
    col.setTitle( "NEW_COLUMN" );
    col.setIndex( true );
    col.setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    col.setAggregateType( AggregationType.SUM.toString() );

    ColumnInfo[] newColumns = (ColumnInfo[]) ArrayUtils.add( columns, col );

    info.setColumns( newColumns );
  }

  private void removeColumnFromModel( ModelInfo info ) {
    ColumnInfo[] columns = info.getColumns();
    ColumnInfo[] newColumns = (ColumnInfo[]) ArrayUtils.remove( columns, columns.length - 1 );
    info.setColumns( newColumns );
  }

  public static ModelInfo createModel() {
    CsvFileInfo fileInfo = new CsvFileInfo();
    fileInfo.setTmpFilename( "unit_test.csv" );
    fileInfo.setProject( "testsolution" );
    fileInfo.setHeaderRows( 1 );
    fileInfo.setDelimiter( "," );
    fileInfo.setEnclosure( "\"" );

    ColumnInfo[] columns = new ColumnInfo[ 9 ];
    columns[ 0 ] = new ColumnInfo();
    columns[ 0 ].setDataType( DataType.NUMERIC );
    columns[ 0 ].setPrecision( 0 );
    columns[ 0 ].setId( "PC_0" );
    columns[ 0 ].setTitle( "REGIONC" );
    columns[ 0 ].setIndex( true );
    columns[ 0 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 0 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 1 ] = new ColumnInfo();
    columns[ 1 ].setDataType( DataType.NUMERIC );
    columns[ 1 ].setId( "PC_1" );
    columns[ 1 ].setTitle( "NWEIGHT" );
    columns[ 1 ].setPrecision( 5 );
    columns[ 1 ].setIndex( true );
    columns[ 1 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 1 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 2 ] = new ColumnInfo();
    columns[ 2 ].setDataType( DataType.NUMERIC );
    columns[ 2 ].setId( "PC_2" );
    columns[ 2 ].setTitle( "Int" );
    columns[ 2 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 2 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 3 ] = new ColumnInfo();
    columns[ 3 ].setDataType( DataType.DATE );
    columns[ 3 ].setId( "PC_3" );
    columns[ 3 ].setTitle( "xdate" );
    columns[ 3 ].setFormat( "mm/dd/yy" );
    columns[ 3 ].setIndex( true );
    columns[ 3 ].setFieldType( ColumnInfo.FIELD_TYPE_DIMENSION );
    columns[ 3 ].setAggregateType( AggregationType.NONE.toString() );

    columns[ 4 ] = new ColumnInfo();
    columns[ 4 ].setDataType( DataType.STRING );
    columns[ 4 ].setId( "PC_4" );
    columns[ 4 ].setTitle( "" );
    columns[ 4 ].setIgnore( true );
    columns[ 4 ].setFieldType( ColumnInfo.FIELD_TYPE_DIMENSION );
    columns[ 4 ].setAggregateType( AggregationType.NONE.toString() );

    columns[ 5 ] = new ColumnInfo();
    columns[ 5 ].setDataType( DataType.STRING );
    columns[ 5 ].setId( "PC_5" );
    columns[ 5 ].setTitle( "Location" );
    columns[ 5 ].setIndex( true );
    columns[ 5 ].setLength( 60 );
    columns[ 5 ].setFieldType( ColumnInfo.FIELD_TYPE_DIMENSION );
    columns[ 5 ].setAggregateType( AggregationType.NONE.toString() );

    columns[ 6 ] = new ColumnInfo();
    columns[ 6 ].setDataType( DataType.NUMERIC );
    columns[ 6 ].setId( "PC_6" );
    columns[ 6 ].setTitle( "charlen" );
    columns[ 6 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 6 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 7 ] = new ColumnInfo();
    columns[ 7 ].setDataType( DataType.NUMERIC );
    columns[ 7 ].setId( "PC_7" );
    columns[ 7 ].setTitle( "xfactor" );
    columns[ 7 ].setPrecision( 7 );
    columns[ 7 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 7 ].setAggregateType( AggregationType.SUM.toString() );

    columns[ 8 ] = new ColumnInfo();
    columns[ 8 ].setDataType( DataType.BOOLEAN );
    columns[ 8 ].setId( "PC_8" );
    columns[ 8 ].setTitle( "Flag" );
    columns[ 8 ].setIndex( true );
    columns[ 8 ].setFieldType( ColumnInfo.FIELD_TYPE_BOTH );
    columns[ 8 ].setAggregateType( AggregationType.SUM.toString() );

    ModelInfo info = new ModelInfo();
    info.setFileInfo( fileInfo );
    info.setColumns( columns );
    info.setStageTableName( "UNIT_TESTS" );

    return info;
  }


  private static DatabaseMeta getJndiDatabaseMeta( String jndi ) {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    databaseMeta.setDBName( jndi );
    databaseMeta.setName( jndi );
    return databaseMeta;
  }

  public void testGetMaxColumnNameLength() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    ModelInfo info = createModel();

    final String H2_JNDI = "pentaho_staging_H2"; // test-res/solution1/system/simple-jndi/jdbc.properties
    final int H2_MAX_COLUMN_NAME_LENGTH = 0; // org.h2.jdbc.JdbcDatabaseMetaData: h2-1.0.78.jar

    final String HYPERSONIC_JNDI = "pentaho_staging_Hypersonic"; // test-res/solution1/system/simple-jndi/jdbc.properties
    final int HYPERSONIC_MAX_COLUMN_NAME_LENGTH = 128; // org.hsqldb.jdbc.JDBCDatabaseMetaData: hsqldb-2.3.2.jar

    final String INVALID_JNDI = "some_invalid_jndi";
    final int DEFAULT_MAX_COLUMN_NAME_LENGTH = 0;

    CsvTransformGenerator genH2 = new CsvTransformGenerator( info, getJndiDatabaseMeta( H2_JNDI ) );
    assertEquals( H2_MAX_COLUMN_NAME_LENGTH, genH2.getMaxColumnNameLength() );

    CsvTransformGenerator genHsqldb = new CsvTransformGenerator( info, getJndiDatabaseMeta( HYPERSONIC_JNDI ) );
    assertEquals( HYPERSONIC_MAX_COLUMN_NAME_LENGTH, genHsqldb.getMaxColumnNameLength() );

    CsvTransformGenerator genInvalid = new CsvTransformGenerator( info, getJndiDatabaseMeta( INVALID_JNDI ) );
    assertEquals( DEFAULT_MAX_COLUMN_NAME_LENGTH, genInvalid.getMaxColumnNameLength() );
  }

  public void testGetMaxColumnNameLength_specialCases() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    ModelInfo info = createModel();
    DatabaseMeta dbMeta = getDatabaseMeta();

    final int DEFAULT_MAX_COLUMN_NAME_LENGTH = 0;

    CsvTransformGenerator genNoDB = spy( new CsvTransformGenerator( info, dbMeta ) );
    doReturn( null ).when( genNoDB ).getDatabase( dbMeta );
    assertEquals( DEFAULT_MAX_COLUMN_NAME_LENGTH, genNoDB.getMaxColumnNameLength() );

    CsvTransformGenerator genNoMetadata = spy( new CsvTransformGenerator( info, dbMeta ) );
    Database dbNoMetadata = mock( Database.class );
    doReturn( null ).when( dbNoMetadata ).getDatabaseMetaData();
    doReturn( dbNoMetadata ).when( genNoDB ).getDatabase( dbMeta );
    assertEquals( DEFAULT_MAX_COLUMN_NAME_LENGTH, genNoMetadata.getMaxColumnNameLength() );

    CsvTransformGenerator genInvalidDb = spy( new CsvTransformGenerator( info, dbMeta ) );
    Database dbInvalid = mock( Database.class );
    doThrow( new KettleDatabaseException() ).when( dbInvalid ).connect();
    doReturn( dbInvalid ).when( genInvalidDb ).getDatabase( dbMeta );
    assertEquals( DEFAULT_MAX_COLUMN_NAME_LENGTH, genInvalidDb.getMaxColumnNameLength() );

    CsvTransformGenerator genInvalidDbMetadata = spy( new CsvTransformGenerator( info, dbMeta ) );
    final DatabaseMetaData dbMetaDataInvalid = mock( DatabaseMetaData.class );
    Database dbInvalidMetadata = mock( Database.class );
    doThrow( new SQLException() ).when( dbMetaDataInvalid ).getMaxColumnNameLength();
    doReturn( dbMetaDataInvalid ).when( dbInvalidMetadata ).getDatabaseMetaData();
    doReturn( dbInvalidMetadata ).when( genInvalidDbMetadata ).getDatabase( dbMeta );
    assertEquals( DEFAULT_MAX_COLUMN_NAME_LENGTH, genInvalidDbMetadata.getMaxColumnNameLength() );

  }

  public void testCreateCutLongNamesStep_longColumnNames() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    ModelInfo info = createModel();

    final int DATABASE_MAX_COLUMN_NAME_LENGTH = 8;
    final String STEP_NAME = "TEST_STEP_CutLongNames";
    CutLongNamesStepInputContext prev = new CutLongNamesStepInputContext();
    String[] fieldRenames = new String[] {"a", "b", "A_1", "b_1", "LONGlong", "longlo_1", "a_2"};
    StepMeta prevStepMeta = mock( StepMeta.class ); // No functionality is required
    List<StepMeta> steps = java.util.Collections.singletonList( prevStepMeta );
    TransMeta transMeta = spy( new TransMeta() );
    doReturn( prev.fields ).when( transMeta ).getStepFields( prevStepMeta );

    final DatabaseMeta databaseMeta = getDatabaseMeta();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, databaseMeta );

    StepMeta step = gen.createCutLongNamesStep( transMeta, steps, DATABASE_MAX_COLUMN_NAME_LENGTH, STEP_NAME );

    Assert.assertNotNull( "step", step );
    Assert.assertEquals( "step name", STEP_NAME, step.getName() );

    StepMetaInterface stepMetaIntegrface = step.getStepMetaInterface();
    Assert.assertNotNull( "stepMetaIntegrface", stepMetaIntegrface );
    Assert.assertTrue( "stepMetaIntegrface instanceof SelectValuesMeta", stepMetaIntegrface instanceof SelectValuesMeta );
    SelectValuesMeta svm = (SelectValuesMeta) stepMetaIntegrface;

    String[] selectName = svm.getSelectName();
    Assert.assertArrayEquals( "selectName", prev.fieldNames, selectName );
    String[] selectRename = svm.getSelectRename();
    Assert.assertArrayEquals( "selectRename", fieldRenames, selectRename );
    int[] selectLengths = svm.getSelectLength();
    Assert.assertArrayEquals( "selectLength", prev.fieldLengths, selectLengths );
    int[] selectPrecisions = svm.getSelectPrecision();
    Assert.assertArrayEquals( "selectPrecision", selectPrecisions, selectPrecisions );

    Assert.assertEquals( step, transMeta.findStep( STEP_NAME ) );
  }

  public void testCreateCutLongNamesStep_shortColumnNames() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    ModelInfo info = createModel();

    final int DATABASE_MAX_COLUMN_NAME_LENGTH = 18;
    final String STEP_NAME = "TEST_STEP_CutLongNames";
    CutLongNamesStepInputContext prev = new CutLongNamesStepInputContext();

    StepMeta prevStepMeta = mock( StepMeta.class ); // No functionality is required
    List<StepMeta> steps = java.util.Collections.singletonList( prevStepMeta );
    TransMeta transMeta = spy( new TransMeta() );
    doReturn( prev.fields ).when( transMeta ).getStepFields( prevStepMeta );

    final DatabaseMeta databaseMeta = getDatabaseMeta();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, databaseMeta );

    StepMeta step = gen.createCutLongNamesStep( transMeta, steps, DATABASE_MAX_COLUMN_NAME_LENGTH, STEP_NAME );

    Assert.assertNull( "step", step );
  }

  public void testCreateCutLongNamesStep_littleMaxColumnNameLength() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    ModelInfo info = createModel();

    final int DATABASE_MAX_COLUMN_NAME_LENGTH = 1;
    final String STEP_NAME = "TEST_STEP_CutLongNames";
    CutLongNamesStepInputContext prev = new CutLongNamesStepInputContext();

    StepMeta prevStepMeta = mock( StepMeta.class ); // No functionality is required
    List<StepMeta> steps = java.util.Collections.singletonList( prevStepMeta );
    TransMeta transMeta = spy( new TransMeta() );
    doReturn( prev.fields ).when( transMeta ).getStepFields( prevStepMeta );

    final DatabaseMeta databaseMeta = getDatabaseMeta();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, databaseMeta );

    StepMeta step = gen.createCutLongNamesStep( transMeta, steps, DATABASE_MAX_COLUMN_NAME_LENGTH, STEP_NAME );
    assertNull( step );
  }

  public void testCreateCutLongNamesStep_prevStepError() throws Exception {
    IPentahoSession session = new StandaloneSession( "test" );
    KettleSystemListener.environmentInit( session );
    ModelInfo info = createModel();

    final int DATABASE_MAX_COLUMN_NAME_LENGTH = 18;
    final String STEP_NAME = "TEST_STEP_CutLongNames";

    StepMeta prevStepMeta = mock( StepMeta.class ); // No functionality is required
    List<StepMeta> steps = java.util.Collections.singletonList( prevStepMeta );
    TransMeta transMeta = spy( new TransMeta() );
    doThrow( new KettleStepException() ).when( transMeta ).getStepFields( prevStepMeta );

    final DatabaseMeta databaseMeta = getDatabaseMeta();
    CsvTransformGenerator gen = new CsvTransformGenerator( info, databaseMeta );

    StepMeta step = gen.createCutLongNamesStep( transMeta, steps, DATABASE_MAX_COLUMN_NAME_LENGTH, STEP_NAME );

    Assert.assertNull( "step", step );
  }

}
