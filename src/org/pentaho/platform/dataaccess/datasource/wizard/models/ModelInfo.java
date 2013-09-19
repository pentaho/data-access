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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class ModelInfo extends XulEventSourceAdapter implements Serializable {

  public static final String CSV_FILE_INFO_ATTRIBUTE = "fileInfo";  //$NON-NLS-1$
  public static final String CSV_COLUMN_INFO_ATTRIBUTE = "columns";  //$NON-NLS-1$
  public static final String STAGE_TABLE_NAME_ATTRIBUTE = "stageTableName";  //$NON-NLS-1$
  
  private static final long serialVersionUID = 2498165533158485182L;
  
  private CsvFileInfo fileInfo;
  
  private String stageTableName;
  
  private ColumnInfo columns[];

  private transient ColumnInfoCollection columnCollection = new ColumnInfoCollection();

  private transient ModelInfoValidationListenerCollection listeners = new ModelInfoValidationListenerCollection();
  
  private boolean validated;

  private ArrayList<String> csvInputErrors = new ArrayList<String>();
  
  private ArrayList<String> tableOutputErrors = new ArrayList<String>();

  private transient FileTransformStats transformStats = new FileTransformStats();


  private String datasourceName;
  
  public ModelInfo () {
    columnCollection.addPropertyChangeListener("selectedCount", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        validate();
      }
    });
  }

  @Bindable
  public CsvFileInfo getFileInfo() {
    return fileInfo;
  }

  @Bindable
  public void setFileInfo(CsvFileInfo fileInfo) {
    CsvFileInfo previousVal = this.fileInfo;
    this.fileInfo = fileInfo;
    firePropertyChange(CSV_FILE_INFO_ATTRIBUTE, previousVal, fileInfo);
    validate();
  }

  @Bindable
  public ColumnInfo[] getColumns() {
    return columns;
  }

  @Bindable
  public void setColumns(ColumnInfo[] columns) {
    ColumnInfo[] previousVal = this.columns;
    this.columns = columns;
    firePropertyChange(CSV_COLUMN_INFO_ATTRIBUTE, previousVal, columns);
    setColumnCollection(columns);
    validate();
  }

  private void setColumnCollection(ColumnInfo[] columns) {
    columnCollection.clear();
    if (columns != null) {
      for (ColumnInfo column : columns) {
        columnCollection.add(column);
      }
    }
  }

  @Bindable
  public DataRow[] getData() {
    return transformStats.getDataRows();
  }

  @Bindable
  public void setData(DataRow[] data) {
    transformStats.setDataRows( data );
  }

  @Bindable
  public String getStageTableName() {
    return stageTableName;
  }

  @Bindable
  public void setStageTableName(String tableName) {
    String previousVal = this.stageTableName;
    this.stageTableName = tableName;
    firePropertyChange(STAGE_TABLE_NAME_ATTRIBUTE, previousVal, tableName);
    validate();
  }

  @Bindable
  public ArrayList<String> getCsvInputErrors() {
	  return csvInputErrors;
  }
  
  @Bindable
  public void setCsvInputErrors(ArrayList<String> csvInputErrors) {
  	this.csvInputErrors = csvInputErrors;
  }
  
  @Bindable
  public ArrayList<String> getTableOutputErrors() {
	  return tableOutputErrors;
  }
  
  @Bindable
  public void setTableOutputErrors(ArrayList<String> tableOutputErrors) {
  	this.tableOutputErrors = tableOutputErrors;
  }  
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(columns);
    result = prime * result + ((fileInfo == null) ? 0 : fileInfo.hashCode());
    result = prime * result + ((stageTableName == null) ? 0 : stageTableName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ModelInfo other = (ModelInfo) obj;
    if (!Arrays.equals(columns, other.columns))
      return false;
    if (fileInfo == null) {
      if (other.fileInfo != null)
        return false;
    } else if (!fileInfo.equals(other.fileInfo))
      return false;
    if (stageTableName == null) {
      if (other.stageTableName != null)
        return false;
    } else if (!stageTableName.equals(other.stageTableName))
      return false;
    return true;
  }

  public void validate() {
    if (stageTableName != null && 
        stageTableName.trim().length() > 0 &&
        fileInfo != null && 
        fileInfo.getTmpFilename() != null && 
        fileInfo.getTmpFilename().length() > 0 && 
        fileInfo.getDelimiter() != null && 
        fileInfo.getDelimiter().length() > 0) {

      listeners.fireCsvInfoValid();

      if (columnsAreValid()) {

        setValidated(true);
        listeners.fireModelInfoValid();
      } else {
        setValidated(false);
        listeners.fireModelInfoInvalid();      
      }
    } else {
      setValidated(false);
      listeners.fireCsvInfoInvalid();
      listeners.fireModelInfoInvalid();      
    }
  }

  private boolean columnsAreValid() {
    if (columns != null) {
      for (ColumnInfo col : columns) {
        if (col == null) return false;
        if (col.getDataType() == null) return false;
        if (col.getTitle() == null) return false;
        if (col.getId() == null) return false;
        if (col.getDataType() == null) return false;
        if (col.getTitle().trim().length() == 0) return false;
        if (col.getId().trim().length() == 0) return false;
      }
      if (columnCollection.getSelectedCount() == 0) return false;
    } else {
      return false;
    }
    return true;
  }
  @Bindable
  public boolean isValidated() {
    return validated;
  }

  @Bindable
  private void setValidated(boolean value) {
    if(value != this.validated) {
      this.validated = value;
      this.firePropertyChange("validated", !value, value); //$NON-NLS-1$
    }
  }

  public void clearModel() {
    setStageTableName(null);
    getFileInfo().clear();
    setColumns(null);
    setData(null);
    validate();
  }

  public void addModelInfoValidationListener(IModelInfoValidationListener listener) {
    if (listeners != null && listener != null) {
      listeners.add(listener);
    }
  }

  public FileTransformStats getTransformStats() {
    return transformStats;
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }
}
