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

import java.io.File;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class CsvTransformGenerator extends StagingTransformGenerator {

  private static final long serialVersionUID = -185098401772609035L;

  private static final String CSV_INPUT = "csvinput"; //$NON-NLS-1$

  private static final String SELECT_VALUES = "select"; //$NON-NLS-1$

  private static final String CUT_LONG_NAMES = "cutLongNames"; //$NON-NLS-1$

  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar
    + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar;
    //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  public static final String TMP_FILE_PATH =
    File.separatorChar + "system" + File.separatorChar + File.separatorChar + "tmp" + File.separatorChar;

  private static final Log log = LogFactory.getLog( CsvTransformGenerator.class );

  /**
   * Default constructor that uses the JNDI datasource configured in the plugin.xml file.
   */
  public CsvTransformGenerator( final ModelInfo info ) {
    setModelInfo( info );
    init();
  }

  public CsvTransformGenerator( final ModelInfo info, DatabaseMeta databaseMeta ) {
    super( databaseMeta );
    setModelInfo( info );
    init();
  }

  private void init() {
    setTransformStats( getModelInfo().getTransformStats() );
    setTableName( getModelInfo().getStageTableName() );
  }

  @Override
  protected String[] getIndexedColumnNames() {

    ArrayList<String> indexed = new ArrayList<String>();
    for ( ColumnInfo column : getModelInfo().getColumns() ) {
      if ( column.isIndex() ) {
        indexed.add( column.getId() );
      }
    }

    return indexed.toArray( new String[ indexed.size() ] );

  }

  @Override
  protected StepMeta[] getSteps( TransMeta transMeta ) {

    List<StepMeta> steps = new ArrayList<StepMeta>();

    StepMeta inputStep = createInputStep( transMeta );
    steps.add( inputStep );

    StepMeta step = createSelectStep( transMeta, SELECT_VALUES );
    if ( step != null ) {
      steps.add( step );
      createHop( inputStep, step, transMeta );
    }
    /*
    step = createCalcStep(transMeta, CALC_DATES, getModelInfo().getColumns());
    if( step != null ) {
      steps.add( step );
      createHop(steps.get(steps.size()-2), step, transMeta);
    }
    */

    final int targetDatabaseMaxColumnNameLength = getMaxColumnNameLength();
    StepMeta cutLongNamesStep = createCutLongNamesStep( transMeta, steps, targetDatabaseMaxColumnNameLength, CUT_LONG_NAMES );
    if ( cutLongNamesStep != null ) {
      steps.add( cutLongNamesStep );
      createHop( steps.get( steps.size() - 2 ), cutLongNamesStep, transMeta );
    }

    return steps.toArray( new StepMeta[ steps.size() ] );
  }

  protected StepMeta createInputStep( TransMeta transMeta ) {

    CsvInputMeta csvInputMeta = new CsvInputMeta();
    CsvFileInfo fileInfo = getModelInfo().getFileInfo();

    String fileName = fileInfo.getTmpFilename();
    String path;
    if ( fileName.endsWith( ".tmp" ) ) { //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
    } else {
      String relativePath = PentahoSystem.getSystemSetting( "file-upload-defaults/relative-path",
        String.valueOf( DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) );  //$NON-NLS-1$
      path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
    }

    File file = new File( path + fileInfo.getTmpFilename() );
    String filename = file.getAbsolutePath();

    ColumnInfo[] columns = getModelInfo().getColumns();
    TextFileInputField[] inputFields = new TextFileInputField[ columns.length ];
    int idx = 0;
    for ( ColumnInfo column : columns ) {
      TextFileInputField field = new TextFileInputField();
      field.setCurrencySymbol( fileInfo.getCurrencySymbol() );
      field.setDecimalSymbol( fileInfo.getCurrencySymbol() );
      field.setFormat( column.getFormat() );
      field.setGroupSymbol( fileInfo.getGroupSymbol() );
      field.setIfNullValue( fileInfo.getIfNull() );
      field.setIgnored( column.isIgnore() );
      field.setLength( column.getLength() );
      field.setName( column.getId() );
      field.setNullString( fileInfo.getNullStr() );
      // field.setPosition(position);
      field.setPrecision( column.getPrecision() );
      field.setRepeated( false );
      field.setSamples( null );
      field.setTrimType( ValueMeta.TRIM_TYPE_BOTH );
      field.setType( convertDataType( column ) );

      inputFields[ idx ] = field;
      idx++;
    }

    csvInputMeta.setAddResultFile( false );
    csvInputMeta.setBufferSize( "5000" ); //$NON-NLS-1$
    csvInputMeta.setDelimiter( fileInfo.getDelimiter() );
    csvInputMeta.setEnclosure( fileInfo.getEnclosure() );
    csvInputMeta.setEncoding( fileInfo.getEncoding() );
    csvInputMeta.setFilename( filename );
    csvInputMeta.setFilenameField( null );
    // TODO strip off more than one row if present...
    csvInputMeta.setHeaderPresent( fileInfo.getHeaderRows() > 0 );
    // inputMeta.get.setID(1);
    csvInputMeta.setIncludingFilename( false );
    csvInputMeta.setInputFields( inputFields );
    csvInputMeta.setLazyConversionActive( true );
    csvInputMeta.setRowNumField( "" ); //$NON-NLS-1$
    csvInputMeta.setRunningInParallel( false );
    // inputMeta.setTargetSteps(null);

    StepMeta csvInputStepMeta = new StepMeta( CSV_INPUT, CSV_INPUT, csvInputMeta );
    csvInputStepMeta.setStepErrorMeta( new StepErrorMeta( transMeta, csvInputStepMeta ) );
    transMeta.addStep( csvInputStepMeta );
    csvErrorRowCount = 0;

    final FileTransformStats stats = getTransformStats();
    StepErrorMeta csvInputErrorMeta = new StepErrorMeta( transMeta, csvInputStepMeta ) {
      public void addErrorRowData( Object[] row, int startIndex, long nrErrors, String errorDescriptions,
                                   String fieldNames, String errorCodes ) {
        if ( csvErrorRowCount < maxErrorRows ) {
          StringBuffer sb = new StringBuffer();
          sb.append( "Rejected Row: " );
          for ( Object rowData : row ) {
            sb.append( rowData );
            sb.append( ", " );
          }
          sb.append( "\r\n" );
          stats.getErrors().add( sb.toString() + errorDescriptions );
        }
        csvErrorRowCount++;
        stats.setErrorCount( csvErrorRowCount );
        super.addErrorRowData( row, startIndex, nrErrors, errorDescriptions, fieldNames, errorCodes );
      }
    };
    StepMeta outputDummyStepMeta = addDummyStep( transMeta, "CSVInputErrorDummy" );
    csvInputErrorMeta.setTargetStep( outputDummyStepMeta );
    csvInputErrorMeta.setEnabled( true );
    csvInputStepMeta.setStepErrorMeta( csvInputErrorMeta );

    return csvInputStepMeta;
  }

  protected StepMeta createSelectStep( TransMeta transMeta, String stepName ) {
    SelectValuesMeta meta = new SelectValuesMeta();
    // find out which columns need to be deleted

    List<String> deleteNameList = new ArrayList<String>();
    for ( ColumnInfo column : getModelInfo().getColumns() ) {
      if ( column.isIgnore() ) {
        deleteNameList.add( column.getId() );
      }
    }
    if ( deleteNameList.size() == 0 ) {
      return null;
    }

    String[] deleteName = deleteNameList.toArray( new String[ deleteNameList.size() ] );
    meta.setDeleteName( deleteName );
    // meta.setID(3);
    StepMeta stepMeta = new StepMeta( stepName, stepName, meta );
    transMeta.addStep( stepMeta );
    return stepMeta;
  }


  @Override
  public Log getLogger() {
    return log;
  }

  /**
   * The target database maxColumnNameLength value if available;
   * 0 otherwise.
   * @return
   */
  protected int getMaxColumnNameLength() {
    int maxLen = 0;
    Database db = null;
    try {
      db = this.getDatabase( getTargetDatabaseMeta() );
      if ( db == null ) {
        log.debug( "Cannot getMaxColumnNameLength (defaults to 0): database is not available." ); //$NON-NLS-1$
        return maxLen;
      }
      db.connect( null );
      final DatabaseMetaData databaseMetaData = db.getDatabaseMetaData();
      if ( databaseMetaData == null ) {
        log.debug( "Cannot getMaxColumnNameLength (defaults to 0): database metadata are not available." ); //$NON-NLS-1$
        return maxLen;
      }
      maxLen = databaseMetaData.getMaxColumnNameLength();
    } catch ( KettleDatabaseException e ) {
      log.debug( "Cannot getMaxColumnNameLength (defaults to 0): " + e.getMessage(), e ); //$NON-NLS-1$
    } catch ( SQLException e ) {
      log.debug( "Cannot getMaxColumnNameLength (defaults to 0): " + e.getMessage(), e ); //$NON-NLS-1$
    } finally {
      if ( db != null ) {
        db.disconnect();
      }
    }
    return maxLen;
  }

  /**
   * This step scans output fields of the last step in <code>steps</code>,
   * 
   * cut field names that longer than <code>maxColumnNameLength</code>,
   * 
   * renames them if necessary to keep them unique.
   * <br/>
   * If <code>maxColumnNameLength</code>&lt;=0 or all field names are short enough, the step is not created;
   * @param transMeta
   * @param steps
   * @param maxColumnNameLength
   * @param stepName
   * @return created {@link StepMeta} or null
   * @throws KettleTransException 
   */
  protected StepMeta createCutLongNamesStep( TransMeta transMeta, List<StepMeta> steps, int maxColumnNameLength, String stepName ) {
    if ( maxColumnNameLength <= 0 ) {
      return null;
    }
    try {
      StepMeta prevStepMeta = steps.get( steps.size() - 1 );
      RowMetaInterface fields = transMeta.getStepFields( prevStepMeta );
      StepMeta stepMeta = createCutLongNamesStep( fields, maxColumnNameLength, stepName );
      if ( stepMeta != null ) {
        transMeta.addStep( stepMeta );
      }
      return stepMeta;
    } catch ( KettleTransException e ) {
      log.warn( "Unable to createCutLongNamesStep. Skipping it.", e );
    } catch ( RuntimeException e ) {
      log.warn( "Unable to createCutLongNamesStep. Skipping it.", e );
    }
    return null;
  }

  /**
   * 
   * @param fields
   * @param maxColumnNameLength
   * @param stepName
   * @return
   * @throws KettleTransException 
   */
  protected StepMeta createCutLongNamesStep( RowMetaInterface fields, int maxColumnNameLength, String stepName ) throws KettleTransException {
    final int fieldsCount = fields.size();

    SelectValuesMeta meta = new SelectValuesMeta();
    List<String> selectNameList = new ArrayList<String>( fieldsCount );
    List<String> selectRenameList = new ArrayList<String>( fieldsCount );
    List<Integer> selectLengthList = new ArrayList<Integer>( fieldsCount );
    List<Integer> selectPrecisionList = new ArrayList<Integer>( fieldsCount );
    final Collection<String> controlNames = new HashSet<String>();
    boolean renameRequired = false;
    for ( ValueMetaInterface valueMeta : fields.getValueMetaList() ) {
      final String oldName = valueMeta.getName();
      selectNameList.add( oldName );
      String newName = oldName;
      if ( newName.length() > maxColumnNameLength ) {
        renameRequired = true;
        newName = newName.substring( 0, maxColumnNameLength );
      }
      if ( controlNames.contains( newName.toLowerCase() ) ) {
        renameRequired = true;
        newName = null;
        String candidateName = null;
        final int maxAppendableSuffixLength = maxColumnNameLength - oldName.length();
        for ( int j = 1; newName == null && j < Integer.MAX_VALUE; j++ ) {
          String suffix = "_" + j;
          if ( suffix.length() > maxColumnNameLength ) {
            throw new KettleTransException( "Cannot cut field name. Maximum suffix length is exceeded" ); //$NON-NLS-1$
          }
          if ( suffix.length() <= maxAppendableSuffixLength ) {
            candidateName = oldName + suffix;
          } else {
            candidateName = oldName.substring( 0, maxColumnNameLength - suffix.length() ) + suffix;
          }
          if ( !controlNames.contains( candidateName.toLowerCase() ) ) {
            newName = candidateName;
          }
        }
        if ( newName == null ) { // This is fantastic but... let it be
          throw new KettleTransException( "Cannot cut field name. Maximum trials number is reached." ); //$NON-NLS-1$
        }
      }
      controlNames.add( newName.toLowerCase() );
      selectRenameList.add( newName );
      selectLengthList.add( valueMeta.getLength() );
      selectPrecisionList.add( valueMeta.getPrecision() );
    }
    if ( !renameRequired ) {
      return null;
    }
    String[] selectName = selectNameList.toArray( new String[ selectNameList.size() ] );
    meta.setSelectName( selectName );
    String[] selectRename = selectRenameList.toArray( new String[ selectRenameList.size() ] );
    meta.setSelectRename( selectRename );

    int[] selectLength = new int[ selectLengthList.size() ];
    int[] selectPrecision = new int[ selectPrecisionList.size() ];
    for ( int i = 0; i < selectLength.length; i++ ) {
      selectLength[ i ] = selectLengthList.get( i );
    }
    for ( int i = 0; i < selectPrecision.length; i++ ) {
      selectPrecision[ i ] = selectPrecisionList.get( i );
    }
    meta.setSelectLength( selectLength );
    meta.setSelectPrecision( selectPrecision );

    StepMeta stepMeta = new StepMeta( stepName, stepName, meta );
    return stepMeta;
  }
}
