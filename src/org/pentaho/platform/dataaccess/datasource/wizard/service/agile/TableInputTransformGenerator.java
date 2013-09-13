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
 * Copyright 2010 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created Sep, 2010
 * @author jdixon
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepErrorMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;

public class TableInputTransformGenerator extends StagingTransformGenerator {

  private static final long serialVersionUID = -185098401772609035L;

  private static final String TABLE_INPUT = "tableinput"; //$NON-NLS-1$

  private static final Log log = LogFactory.getLog(TableInputTransformGenerator.class);

  private DatabaseMeta sourceDatabaseMeta;
  
  private String sql;
  
  private int rowLimit = -1;
  
  public TableInputTransformGenerator( DatabaseMeta sourceDatabaseMeta, DatabaseMeta targetDatabaseMeta ) {
    super(targetDatabaseMeta);
    this.sourceDatabaseMeta = sourceDatabaseMeta;
  }
  
  @Override
  protected String[] getIndexedColumnNames() {

    return new String[0];
    
  }
  
  @Override
  protected StepMeta[] getSteps( TransMeta transMeta ) {
    
    StepMeta steps[] = new StepMeta[1];
    steps[0] = createInputStep(transMeta);
    
    return steps;
  }
  
  protected StepMeta createInputStep(TransMeta transMeta) {

    TableInputMeta inputMeta = new TableInputMeta();

    inputMeta.setDatabaseMeta(sourceDatabaseMeta);
    inputMeta.setExecuteEachInputRow(false);
    inputMeta.setRowLimit(Integer.toString(rowLimit));
    inputMeta.setSQL(sql);
    inputMeta.setVariableReplacementActive(false);
    inputMeta.setLazyConversionActive(false);
    // inputMeta.setTargetSteps(null);

    StepMeta inputStepMeta = new StepMeta(TABLE_INPUT, TABLE_INPUT, inputMeta);
    inputStepMeta.setStepErrorMeta(new StepErrorMeta(transMeta, inputStepMeta));
    transMeta.addStep(inputStepMeta);

    final FileTransformStats stats = getTransformStats();
    StepErrorMeta inputErrorMeta = new StepErrorMeta(transMeta, inputStepMeta) {
      public void addErrorRowData(Object[] row, int startIndex, long nrErrors, String errorDescriptions, String fieldNames, String errorCodes) {
        StringBuffer sb = new StringBuffer();
        sb.append("Rejected Row: ");
        for (Object rowData : row) {
          sb.append(rowData);
          sb.append(", ");
        }
        sb.append("\r\n");
        stats.getErrors().add(sb.toString() + errorDescriptions);
        super.addErrorRowData(row, startIndex, nrErrors, errorDescriptions, fieldNames, errorCodes);
      }
    };
    StepMeta outputDummyStepMeta = addDummyStep(transMeta, "InputErrorDummy");
    inputErrorMeta.setTargetStep(outputDummyStepMeta);
    inputErrorMeta.setEnabled(true);
    inputStepMeta.setStepErrorMeta(inputErrorMeta);

    return inputStepMeta;
  }

  @Override
  public Log getLogger() {
    return log;
  }

  public DatabaseMeta getSourceDatabaseMeta() {
    return sourceDatabaseMeta;
  }

  public void setSourceDatabaseMeta(DatabaseMeta databaseMeta) {
    this.sourceDatabaseMeta = databaseMeta;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public int getRowLimit() {
    return rowLimit;
  }

  public void setRowLimit(int rowLimit) {
    this.rowLimit = rowLimit;
  }

}
