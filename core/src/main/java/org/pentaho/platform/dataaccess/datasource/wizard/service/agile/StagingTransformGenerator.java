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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.platform.engine.core.system.PentahoBase;

public abstract class StagingTransformGenerator extends PentahoBase {

  private static final long serialVersionUID = -185098401772609035L;

  private static final String DUMMY = "dummy"; //$NON-NLS-1$

  private static final String TABLE_OUTPUT = "output"; //$NON-NLS-1$

  private static final String TRANS_SESSION_ATTR = "PDI_Trans"; //$NON-NLS-1$

  private static final Log log = LogFactory.getLog( StagingTransformGenerator.class );

  private DatabaseMeta targetDatabaseMeta;

  private String tableName = null;

  private FileTransformStats transformStats;

  protected abstract StepMeta[] getSteps( TransMeta transMeta );

  protected abstract String[] getIndexedColumnNames();

  protected long errorRowCount;
  protected long maxErrorRows = 100;
  protected long csvErrorRowCount;

  private ModelInfo modelInfo;

  /**
   * Default constructor that uses the JNDI datasource configured in the plugin.xml file.
   */
  public StagingTransformGenerator() {
    targetDatabaseMeta = AgileHelper.getDatabaseMeta();
  }

  /**
   * Use this contructor if you want to specify a different datasource than the one configured in plugin.xml. Typically
   * used for unit testing.
   *
   * @param databaseMeta
   */
  public StagingTransformGenerator( DatabaseMeta databaseMeta ) {
    this.targetDatabaseMeta = databaseMeta;
  }

  private static String getStackTraceAsString( Throwable aThrowable ) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter( result );
    aThrowable.printStackTrace( printWriter );
    return result.toString();
  }

  public void preview( IPentahoSession session ) throws CsvTransformGeneratorException {
    Trans trans = createTransform( false );
    try {
      prepareTransform( trans, session );
    } catch ( Exception e ) {
      error( "Preview Failed: transformation preparation", e ); //$NON-NLS-1$
      throw new CsvTransformGeneratorException( "Preview Failed: transformation preparation: preview", e,
        getStackTraceAsString( e ) ); //$NON-NLS-1$
    }

    String[] stepNames = trans.getTransMeta().getStepNames();
    executeTransformSync( trans, stepNames[ stepNames.length - 1 ], session );
  }

  public void dropTable( String tableName ) throws CsvTransformGeneratorException {
    if ( tableName == null ) {
      throw new IllegalArgumentException( "Table Name cannot be null" ); //$NON-NLS-1$
    }
    String schemaTableName =
      targetDatabaseMeta.getQuotedSchemaTableCombination( AgileHelper.getSchemaName(), tableName );
    if ( checkTableExists( schemaTableName ) ) {
      // TODO this should be dialected
      String ddl = "DROP TABLE " + schemaTableName;
      execSqlStatement( ddl, targetDatabaseMeta, null );
    }
  }

  public void createOrModifyTable( IPentahoSession session )
    throws CsvTransformGeneratorException, IllegalArgumentException {

    if ( session == null ) {
      throw new IllegalArgumentException( "IPentahoSession cannot be null" ); //$NON-NLS-1$
    }

    if ( tableName == null ) {
      throw new IllegalArgumentException( "Table name cannot be null" ); //$NON-NLS-1$
    }

    TransMeta transMeta = createTransMeta( true );
    // the table output is the last step
    StepMeta[] steps = transMeta.getStepsArray();
    StepMeta tableStepMeta = steps[ steps.length - 1 ];
    TableOutputMeta meta = (TableOutputMeta) tableStepMeta.getStepMetaInterface();
    meta.setDatabaseMeta( targetDatabaseMeta );

    try {
      executeSql( meta, tableStepMeta, transMeta );
    } catch ( CsvTransformGeneratorException e ) {
      if ( !e.getMessage().equalsIgnoreCase( "No SQL generated" ) ) { //$NON-NLS-1$
        error( e.getMessage() );
        throw new CsvTransformGeneratorException( "Could not create or modify table", e, //$NON-NLS-1$
          getStackTraceAsString( e ), null, Messages.getString( "StagingTransformGenerator.ERROR_0001_UNABLE_TO_CREATE_OR_MODIFY_TABLE" ) ); //$NON-NLS-1$
      }
    }
  }

  /**
   * Stages the data from a CSV file into a database table. As the table is loading, a {@link TransformStats} monitors
   * the progress. This is placed in the supplied {@link IPentahoSession} to allow interrogation under the attribute key
   * <code>FileTransformStats_<em>fileName</em></code>
   *
   * @param truncate
   * @param session
   * @throws CsvTransformGeneratorException
   */
  public void loadTable( boolean truncate, IPentahoSession session, boolean async )
    throws CsvTransformGeneratorException {

    if ( session == null ) {
      throw new IllegalArgumentException( "IPentahoSession cannot be null" ); //$NON-NLS-1$
    }

    if ( tableName == null ) {
      throw new IllegalArgumentException( "Table name cannot be null" ); //$NON-NLS-1$
    }

    if ( transformStats != null ) {
      transformStats.setRowsFinished( false );
      transformStats.setRowsStarted( true );
      transformStats.setTotalRecords( 0 );
      transformStats.setRowsRejected( 0 );
    }
    Trans trans = createTransform( true );
    // the table output is the last step
    StepMeta[] steps = trans.getTransMeta().getStepsArray();
    StepMeta tableStepMeta = steps[ steps.length - 1 ];
    TableOutputMeta meta = (TableOutputMeta) tableStepMeta.getStepMetaInterface();
    meta.setDatabaseMeta( targetDatabaseMeta );
    meta.setTruncateTable( truncate );
    try {
      prepareTransform( trans, session );
    } catch ( Exception e ) {
      error( "Preview Failed: transformation preparation", e ); //$NON-NLS-1$
      Throwable e2 = e.getCause();
      e2 = e2 == null ? e : e2;
      throw new CsvTransformGeneratorException( "Preview Failed: transformation preparation: loadTable", e2,
        getStackTraceAsString( e2 ) ); //$NON-NLS-1$
    }

    StepInterface step = trans.findRunThread( TABLE_OUTPUT );
    PdiTransListener listener = new PdiTransListener( trans, step, transformStats );
    // start the listener in a thread
    Thread listenerThread = new Thread( listener );
    listenerThread.start();
    session.setAttribute( TRANS_SESSION_ATTR, trans );

    if ( async ) {
      executeTransformAsync( trans );
    } else {
      executeTransformSync( trans, null, session );
    }
  }

  public int createIndices( IPentahoSession session ) {

    if ( transformStats != null ) {
      transformStats.setIndexFinished( false );
      transformStats.setIndexStarted( true );
    }

    String tableName = getTableName();
    Database db = new Database( targetDatabaseMeta );

    String[] indexed = getIndexedColumnNames();

    List<String> commands = new ArrayList<String>();
    // TODO base this on the input rows meta for the table output step?
    for ( String columnName : indexed ) {
      String indexSql = db.getCreateIndexStatement( tableName, columnName + "_idx", //$NON-NLS-1$
        new String[] { columnName }, false, false, false, true );
      commands.add( indexSql );
    }
    if ( transformStats != null ) {
      transformStats.setIndexCount( commands.size() );
    }
    int indexDone = 0;
    int indexSuccess = 0;
    for ( String command : commands ) {
      try {
        execSqlStatement( command, targetDatabaseMeta, null );
        indexSuccess++;
      } catch ( CsvTransformGeneratorException e ) {
        // failed to execute
      }
      indexDone++;
      if ( transformStats != null ) {
        transformStats.setIndexDone( indexDone );
      }
    }
    if ( transformStats != null ) {
      transformStats.setIndexFinished( true );
      transformStats.setIndexStarted( false );
    }
    return indexSuccess;
  }

  public void cancelLoad( IPentahoSession session ) {
    Trans trans = (Trans) session.getAttribute( TRANS_SESSION_ATTR );
    trans.stopAll();
  }

  protected StepMeta addDummyStep( TransMeta transMeta, String stepName ) {
    DummyTransMeta meta = new DummyTransMeta();
    // meta.setID(2);
    StepMeta stepMeta = new StepMeta( stepName, stepName, meta );
    transMeta.addStep( stepMeta );
    return stepMeta;

  }

  protected StepMeta addTableOutputStep( TransMeta transMeta, String tableOutputStepName, String modelName ) {
    TableOutputMeta tableOutputMeta = new TableOutputMeta();
    tableOutputMeta.setCommitSize( 1000 );
    tableOutputMeta.setIgnoreErrors( true );
    tableOutputMeta.setPartitioningEnabled( false );
    tableOutputMeta.setSchemaName( AgileHelper.getSchemaName() );
    tableOutputMeta.setTableName( getTableName() );
    tableOutputMeta.setUseBatchUpdate( false );

    StepMeta tableOutputStepMeta = new StepMeta( tableOutputStepName, tableOutputStepName, tableOutputMeta );

    transMeta.addStep( tableOutputStepMeta );
    return tableOutputStepMeta;
  }

  protected void createHop( StepMeta fromStep, StepMeta toStep, TransMeta transMeta ) {
    TransHopMeta hopMeta = new TransHopMeta();
    hopMeta.setFromStep( fromStep );
    hopMeta.setToStep( toStep );
    hopMeta.setEnabled( true );
    transMeta.addTransHop( hopMeta );
  }

  protected void prepareTransform( Trans trans, final IPentahoSession session ) throws KettleException {
    trans.prepareExecution( trans.getArguments() );

    StepInterface tableOutputStep = trans.findRunThread( TABLE_OUTPUT );

    if ( tableOutputStep != null ) {
      StepErrorMeta tableOutputErrorMeta = new StepErrorMeta( trans.getTransMeta(), tableOutputStep.getStepMeta() ) {
        public void addErrorRowData( Object[] row, int startIndex, long nrErrors, String errorDescriptions,
                                     String fieldNames, String errorCodes ) {
          // don't overwhelm the user with too many errors
          if ( errorRowCount < maxErrorRows ) {
            StringBuffer sb = new StringBuffer();
            sb.append( "Rejected Row: " );
            for ( Object rowData : row ) {
              sb.append( rowData );
              sb.append( ", " );
            }
            sb.append( "\r\n" );
            if ( transformStats != null ) {
              transformStats.getErrors().add( sb.toString() + errorDescriptions );
            }
          }
          errorRowCount++;
          transformStats.setErrorCount( errorRowCount );
          super.addErrorRowData( row, startIndex, nrErrors, errorDescriptions, fieldNames, errorCodes );
        }
      };
      StepMeta outputDummyStepMeta = addDummyStep( trans.getTransMeta(), "TableOutputErrorDummy" );
      tableOutputErrorMeta.setTargetStep( outputDummyStepMeta );
      tableOutputErrorMeta.setEnabled( true );

      tableOutputStep.getStepMeta().setStepErrorMeta( tableOutputErrorMeta );
    }
  }

  protected void executeTransformSync( Trans trans, String listenerStepName, IPentahoSession session )
    throws CsvTransformGeneratorException {

    PdiRowListener rowListener = new PdiRowListener();
    if ( listenerStepName != null ) {
      trans.getStepInterface( listenerStepName, 0 ).addRowListener( rowListener );
    }

    try {
      trans.startThreads();
    } catch ( Exception e ) {
      error( "Preview Failed: starting threads", e ); //$NON-NLS-1$
      throw new CsvTransformGeneratorException( "Preview Failed: starting threads", e,
        getStackTraceAsString( e ) ); //$NON-NLS-1$
    }

    try {
      trans.waitUntilFinished();
    } catch ( Exception e ) {
      error( "Preview Failed: running", e ); //$NON-NLS-1$
      throw new CsvTransformGeneratorException( "Preview Failed: running", e,
        getStackTraceAsString( e ) ); //$NON-NLS-1$
    }

    try {
      trans.cleanup();
    } catch ( Exception e ) {
      error( "Preview Failed: ending", e ); //$NON-NLS-1$
      throw new CsvTransformGeneratorException( "Preview Failed: ending", e, getStackTraceAsString( e ) ); //$NON-NLS-1$
    }

    if ( transformStats != null ) {
      transformStats.setDataRows( rowListener.getWrittenRows() );
    }

  }

  protected void executeTransformAsync( Trans trans ) throws CsvTransformGeneratorException {
    try {
      trans.startThreads();
    } catch ( Exception e ) {
      error( "Preview Failed: starting threads", e ); //$NON-NLS-1$
      throw new CsvTransformGeneratorException( "Preview Failed: starting threads", e,
        getStackTraceAsString( e ) ); //$NON-NLS-1$
    }
  }

  protected Trans createTransform( boolean doOutput ) {
    TransMeta transMeta = createTransMeta( doOutput );
    return new Trans( transMeta );
  }

  private TransMeta createTransMeta( boolean doOutput ) {
    TransMeta transMeta = new TransMeta();

    StepMeta[] steps = getSteps( transMeta );

    StepMeta lastStep = steps[ steps.length - 1 ];

    // create the table step if necessary
    StepMeta tableStepMeta = null;
    if ( doOutput ) {
      tableStepMeta = addTableOutputStep( transMeta, TABLE_OUTPUT, tableName );
      createHop( lastStep, tableStepMeta, transMeta );
      lastStep = tableStepMeta;
    }

    // we need to create a dummy step as a sink, otherwise the transform won't execute anything
    StepMeta dummyStepMeta = null;
    if ( lastStep != tableStepMeta ) {
      dummyStepMeta = addDummyStep( transMeta, DUMMY );
      createHop( lastStep, dummyStepMeta, transMeta );
    }
    return transMeta;
  }

  protected void executeSql( TableOutputMeta meta, StepMeta stepMeta, TransMeta transMeta )
    throws CsvTransformGeneratorException {

    try {
      RowMetaInterface prev = transMeta.getPrevStepFields( TABLE_OUTPUT );
      SQLStatement sqlStatement = meta.getSQLStatements( transMeta, stepMeta, prev, null, false, null );
      if ( !sqlStatement.hasError() ) {
        if ( sqlStatement.hasSQL() ) {
          // now we can execute the SQL

          String sqlScript = sqlStatement.getSQL();
          execSqlStatement( sqlScript, meta.getDatabaseMeta(), null );
        } else {
          // No SQL was generated
          error( "No SQL generated" ); //$NON-NLS-1$
          throw new CsvTransformGeneratorException( "No SQL generated" ); //$NON-NLS-1$
        }
      } else {
        error( sqlStatement.getError() );
        throw new CsvTransformGeneratorException( sqlStatement.getError() );
      }
    } catch ( KettleException ke ) {
      error( "Exception encountered", ke ); //$NON-NLS-1$
      throw new CsvTransformGeneratorException( "Exception encountered", ke,
        getStackTraceAsString( ke ) ); //$NON-NLS-1$
    }
  }

  public void execSqlStatement( String sqlScript, DatabaseMeta ci, StringBuilder message )
    throws IllegalArgumentException, CsvTransformGeneratorException {

    if ( ci == null ) {
      throw new IllegalArgumentException( "DatabaesMeta cannot be null" ); //$NON-NLS-1$
    }

    Database db = getDatabase( ci );
    try {
      db.connect( null );

      // Multiple statements have to be split into parts
      // We use the ";" to separate statements...
      String all = sqlScript + Const.CR;
      int from = 0;
      int to = 0;
      int length = all.length();

      while ( to < length ) {
        char c = all.charAt( to );
        if ( c == '"' ) {
          to++;
          c = ' ';
          while ( to < length && c != '"' ) {
            c = all.charAt( to );
            to++;
          }
        } else if ( c == '\'' ) { // skip until next '
          to++;
          c = ' ';
          while ( to < length && c != '\'' ) {
            c = all.charAt( to );
            to++;
          }
        }
        c = all.charAt( to );
        if ( c == ';' || to >= length - 1 ) { // end of statement
          if ( to >= length - 1 ) {
            to++; // grab last char also!
          }

          String stat = all.substring( from, to );
          String sql = Const.trim( stat );
          try {
            if ( !sql.equals( "" ) ) { //$NON-NLS-1$
              db.execStatement( sql );
            }

          } catch ( Exception dbe ) {
            error( "Error executing DDL", dbe ); //$NON-NLS-1$
            throw new CsvTransformGeneratorException( dbe.getMessage(), dbe, getStackTraceAsString( dbe ) );
          }
          to++;
          from = to;
        } else {
          to++;
        }
      }
    } catch ( KettleDatabaseException dbe ) {
      error( "Connection error", dbe ); //$NON-NLS-1$
      throw new CsvTransformGeneratorException( "Connection error", dbe, getStackTraceAsString( dbe ) ); //$NON-NLS-1$
    } finally {
      db.disconnect();
    }
  }

  Database getDatabase( final DatabaseMeta databaseMeta ) {
    return new Database( databaseMeta );
  }

  protected int convertDataType( ColumnInfo ci ) {
    if ( ci != null && ci.getDataType() != null ) {
      switch ( ci.getDataType() ) {
        case NUMERIC:
          if ( ci.getPrecision() <= 0 ) {
            return ValueMetaInterface.TYPE_INTEGER;
          } else {
            return ValueMetaInterface.TYPE_NUMBER;
          }
        default:
          return ValueMetaBase.getType( ci.getDataType().getName() );
      }
    } else {
      return ValueMetaInterface.TYPE_STRING;
    }
  }

  @Override
  public Log getLogger() {
    return log;
  }

  public FileTransformStats getTransformStats() {
    return transformStats;
  }

  public void setTransformStats( FileTransformStats transformStats ) {
    this.transformStats = transformStats;
  }

  public String getTableName() {
    return tableName;
  }

  public void setTableName( String tableName ) {
    this.tableName = tableName;
  }

  protected StepMeta createCalcStep( TransMeta transMeta, String stepName, ColumnInfo[] columns ) {

    CalculatorMeta meta = new CalculatorMeta();

    List<CalculatorMetaFunction> funcs = new ArrayList<CalculatorMetaFunction>();

    for ( ColumnInfo column : columns ) {

      if ( column != null && !column.isIgnore() && column.getDataType() == DataType.DATE ) {
        // see if we need to break out the date fields
        int dateBreakOut = column.getDateFieldBreakout();

        if ( ( dateBreakOut & ColumnInfo.DATE_LEVEL_YEAR ) > 0 ) {
          CalculatorMetaFunction func =
            createDateCalc( CalculatorMetaFunction.CALC_YEAR_OF_DATE, column.getTitle() + " (year)", column.getId(),
              4 );
          funcs.add( func );
        }
        if ( ( dateBreakOut & ColumnInfo.DATE_LEVEL_QUARTER ) > 0 ) {
          CalculatorMetaFunction func =
            createDateCalc( CalculatorMetaFunction.CALC_QUARTER_OF_DATE, column.getTitle() + " (qtr)", column.getId(),
              4 );
          funcs.add( func );
        }
        if ( ( dateBreakOut & ColumnInfo.DATE_LEVEL_MONTH ) > 0 ) {
          CalculatorMetaFunction func =
            createDateCalc( CalculatorMetaFunction.CALC_MONTH_OF_DATE, column.getTitle() + " (month)", column.getId(),
              4 );
          funcs.add( func );
        }
        if ( ( dateBreakOut & ColumnInfo.DATE_LEVEL_WEEK ) > 0 ) {
          CalculatorMetaFunction func =
            createDateCalc( CalculatorMetaFunction.CALC_WEEK_OF_YEAR, column.getTitle() + " (week)", column.getId(),
              4 );
          funcs.add( func );
        }
        if ( ( dateBreakOut & ColumnInfo.DATE_LEVEL_DAY ) > 0 ) {
          CalculatorMetaFunction func =
            createDateCalc( CalculatorMetaFunction.CALC_DAY_OF_MONTH, column.getTitle() + " (day)", column.getId(), 4 );
          funcs.add( func );
        }
        if ( ( dateBreakOut & ColumnInfo.DATE_LEVEL_DAYOFWEEK ) > 0 ) {
          CalculatorMetaFunction func =
            createDateCalc( CalculatorMetaFunction.CALC_DAY_OF_WEEK, column.getTitle() + " (day of week)",
              column.getId(), 4 );
          funcs.add( func );
        }

      }

    }
    if ( funcs.size() == 0 ) {
      return null;
    }

    meta.setCalculation( funcs.toArray( new CalculatorMetaFunction[ funcs.size() ] ) );

    StepMeta stepMeta = new StepMeta( stepName, stepName, meta );
    transMeta.addStep( stepMeta );
    return stepMeta;

  }

  /**
   * Creates a calculation. Used to break out date fields
   *
   * @param calcType
   * @param fieldName
   * @param fieldId
   * @param valueLength
   * @return
   */
  protected CalculatorMetaFunction createDateCalc( int calcType, String fieldName, String fieldId, int valueLength ) {
    String fieldB = null;
    System.out.println( 99 );
    String fieldC = null;
    int valueType = ValueMetaInterface.TYPE_INTEGER;
    int valuePrecision = 0;
    boolean removedFromResult = false;
    String conversionMask = ""; //$NON-NLS-1$
    String decimalSymbol = ""; //$NON-NLS-1$
    String groupingSymbol = ""; //$NON-NLS-1$
    String currencySymbol = ""; //$NON-NLS-1$

    CalculatorMetaFunction func = new CalculatorMetaFunction( fieldName, calcType, fieldId, fieldB, fieldC,
      valueType, valueLength, valuePrecision,
      removedFromResult, conversionMask, decimalSymbol, groupingSymbol, currencySymbol );

    // update the model
    ColumnInfo column = new ColumnInfo();
    column.setAggregateType( AggregationType.NONE.toString() );
    column.setDataType( DataType.NUMERIC );
    column.setFieldType( ColumnInfo.FIELD_TYPE_DIMENSION );
    column.setIgnore( false );
    column.setId( fieldId );
    column.setIndex( true );
    column.setTitle( fieldName );
    return func;
  }

  public ModelInfo getModelInfo() {
    return modelInfo;
  }

  public void setModelInfo( ModelInfo modelInfo ) {
    this.modelInfo = modelInfo;
  }

  private boolean checkTableExists( String tableName ) throws CsvTransformGeneratorException {
    Database db = getDatabase( targetDatabaseMeta );
    try {
      db.connect( null );
      try {
        try {
          if ( db.getConnection().getAutoCommit() == false ) {
            db.setCommit( 0 );
          }
        } catch ( SQLException e ) {
          // do nothing
        }
        return db.checkTableExists( tableName );
      } catch ( KettleDatabaseException dbe ) {
        error( "Error executing DDL", dbe );
        throw new CsvTransformGeneratorException( dbe.getMessage(), dbe, getStackTraceAsString( dbe ) );
      }
    } catch ( KettleDatabaseException dbe ) {
      error( "Connection error", dbe );
      throw new CsvTransformGeneratorException( "Connection error", dbe, getStackTraceAsString( dbe ) );
    } finally {
      db.disconnect();
    }
  }
}
