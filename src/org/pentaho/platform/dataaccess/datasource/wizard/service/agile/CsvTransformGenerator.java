/*
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
 * Copyright 2009-2010 Pentaho Corporation.  All rights reserved.
 *
 * Created Sep, 2010
 * @author jdixon
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.csvinput.CsvInputMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.textfileinput.TextFileInputField;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileTransformStats;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class CsvTransformGenerator extends StagingTransformGenerator {

  private static final long serialVersionUID = -185098401772609035L;

  private static final String CSV_INPUT = "csvinput"; //$NON-NLS-1$

  private static final String SELECT_VALUES = "select"; //$NON-NLS-1$
  
  private static final String CALC_DATES = "calc dates"; //$NON-NLS-1$

  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar
      + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  public static final String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + File.separatorChar + "tmp" + File.separatorChar;

  private static final Log log = LogFactory.getLog(CsvTransformGenerator.class);
  
  /**
   * Default constructor that uses the JNDI datasource configured in the plugin.xml file.
   */
  public CsvTransformGenerator(final ModelInfo info ) {
    setModelInfo(info);
    init();
  }

  public CsvTransformGenerator(final ModelInfo info, DatabaseMeta databaseMeta ) {
    super(databaseMeta);
    setModelInfo(info);
    init();
  }

  private void init() {
    setTransformStats( getModelInfo().getTransformStats() );
    setTableName( getModelInfo().getStageTableName() );    
  }
  
  @Override
  protected String[] getIndexedColumnNames() {
    
    ArrayList<String> indexed = new ArrayList<String>();
    for (ColumnInfo column : getModelInfo().getColumns()) {
      if (column.isIndex()) {
        indexed.add( column.getId() );
      }
    }

    return indexed.toArray( new String[indexed.size() ] );
    
  }
  
  @Override
  protected StepMeta[] getSteps( TransMeta transMeta ) {
    
    List<StepMeta> steps = new ArrayList<StepMeta>();

    StepMeta inputStep = createInputStep(transMeta);
    steps.add(inputStep);
    
    StepMeta step = createSelectStep(transMeta, SELECT_VALUES);
    if( step != null ) {
      steps.add( step );
      createHop(inputStep, step, transMeta);
    }
    /*
    step = createCalcStep(transMeta, CALC_DATES, getModelInfo().getColumns());
    if( step != null ) {
      steps.add( step );
      createHop(steps.get(steps.size()-2), step, transMeta);
    }
    */
    return steps.toArray(new StepMeta[steps.size()]);
  }
  
  protected StepMeta createInputStep(TransMeta transMeta ) {

    CsvInputMeta csvInputMeta = new CsvInputMeta();
    CsvFileInfo fileInfo = getModelInfo().getFileInfo();
    
    String fileName = fileInfo.getTmpFilename();
    String path;
    if(fileName.endsWith(".tmp")) { //$NON-NLS-1$  
      path = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
    } else {
      String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(DEFAULT_RELATIVE_UPLOAD_FILE_PATH));  //$NON-NLS-1$  
      path = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
    }
    
    File file = new File(path + fileInfo.getTmpFilename());
    String filename = file.getAbsolutePath();

    ColumnInfo columns[] = getModelInfo().getColumns();
    TextFileInputField inputFields[] = new TextFileInputField[columns.length];
    int idx=0;
    for (ColumnInfo column : columns ) {
      TextFileInputField field = new TextFileInputField();
      field.setCurrencySymbol(fileInfo.getCurrencySymbol());
      field.setDecimalSymbol(fileInfo.getCurrencySymbol());
      field.setFormat(column.getFormat());
      field.setGroupSymbol(fileInfo.getGroupSymbol());
      field.setIfNullValue(fileInfo.getIfNull());
      field.setIgnored(column.isIgnore());
      field.setLength(column.getLength());
      field.setName(column.getId());
      field.setNullString(fileInfo.getNullStr());
      // field.setPosition(position);
      field.setPrecision(column.getPrecision());
      field.setRepeated(false);
      field.setSamples(null);
      field.setTrimType(ValueMeta.TRIM_TYPE_BOTH);
      field.setType(convertDataType(column));

      inputFields[idx] = field;
      idx++;
    }

    csvInputMeta.setAddResultFile(false);
    csvInputMeta.setBufferSize("5000"); //$NON-NLS-1$
    csvInputMeta.setDelimiter(fileInfo.getDelimiter());
    csvInputMeta.setEnclosure(fileInfo.getEnclosure());
    csvInputMeta.setEncoding(fileInfo.getEncoding());
    csvInputMeta.setFilename(filename);
    csvInputMeta.setFilenameField(null);
    // TODO strip off more than one row if present...
    csvInputMeta.setHeaderPresent(fileInfo.getHeaderRows() > 0);
    // inputMeta.get.setID(1);
    csvInputMeta.setIncludingFilename(false);
    csvInputMeta.setInputFields(inputFields);
    csvInputMeta.setLazyConversionActive(true);
    csvInputMeta.setRowNumField(""); //$NON-NLS-1$
    csvInputMeta.setRunningInParallel(false);
    // inputMeta.setTargetSteps(null);

    StepMeta csvInputStepMeta = new StepMeta(CSV_INPUT, CSV_INPUT, csvInputMeta);
    csvInputStepMeta.setStepErrorMeta(new StepErrorMeta(transMeta, csvInputStepMeta));
    transMeta.addStep(csvInputStepMeta);
    csvErrorRowCount= 0;

    final FileTransformStats stats = getTransformStats();
    StepErrorMeta csvInputErrorMeta = new StepErrorMeta(transMeta, csvInputStepMeta) {
      public void addErrorRowData(Object[] row, int startIndex, long nrErrors, String errorDescriptions, String fieldNames, String errorCodes) {
        if (csvErrorRowCount < maxErrorRows) {
        	StringBuffer sb = new StringBuffer();
        	sb.append("Rejected Row: ");
        	for (Object rowData : row) {
          	sb.append(rowData);
          	sb.append(", ");
        	}
        	sb.append("\r\n");
        	stats.getCsvInputErrors().add(sb.toString() + errorDescriptions);
        }
        csvErrorRowCount++;
        stats.setCsvInputErrorCount(csvErrorRowCount);        
        super.addErrorRowData(row, startIndex, nrErrors, errorDescriptions, fieldNames, errorCodes);
      }
    };
    StepMeta outputDummyStepMeta = addDummyStep(transMeta, "CSVInputErrorDummy");
    csvInputErrorMeta.setTargetStep(outputDummyStepMeta);
    csvInputErrorMeta.setEnabled(true);
    csvInputStepMeta.setStepErrorMeta(csvInputErrorMeta);

    return csvInputStepMeta;
  }

  protected StepMeta createSelectStep(TransMeta transMeta, String stepName) {
    SelectValuesMeta meta = new SelectValuesMeta();
    // find out which columns need to be deleted

    List<String> deleteNameList = new ArrayList<String>();
    for (ColumnInfo column : getModelInfo().getColumns()) {
      if (column.isIgnore()) {
        deleteNameList.add(column.getId());
      }
    }
    if (deleteNameList.size() == 0) {
      return null;
    }

    String deleteName[] = deleteNameList.toArray(new String[deleteNameList.size()]);
    meta.setDeleteName(deleteName);
    // meta.setID(3);
    StepMeta stepMeta = new StepMeta(stepName, stepName, meta);
    transMeta.addStep(stepMeta);
    return stepMeta;
  }


  @Override
  public Log getLogger() {
    return log;
  }

}
