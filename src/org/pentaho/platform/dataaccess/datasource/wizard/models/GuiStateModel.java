/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Aug 15, 2010 
 * @author wseyler
 */


package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * @author wseyler
 *
 */
public class GuiStateModel extends XulEventSourceAdapter {
  public static enum ConnectionEditType {ADD, EDIT}

  private RelationalModelValidationListenerCollection relationalModelValidationListeners;
  private boolean relationalValidated;
  private boolean relationalPreviewValidated;
  private boolean relationalApplyValidated;
  private List<IConnection> connections = new ArrayList<IConnection>();
  private String previewLimit = "10"; //$NON-NLS-1$
  private ConnectionEditType editType = ConnectionEditType.ADD;
  private List<LogicalModel> logicalModels;
  private String localeCode;

  private boolean dataStagingComplete;
  private FileInfo selectedCsvFile;

  private boolean editing;
  private boolean dirty = true;


  private IXulAsyncConnectionService connectionService;

  @Bindable
  public ConnectionEditType getEditType() {
    return editType;
  }

  @Bindable
  public void setEditType(ConnectionEditType value) {
    this.editType = value;
  }
  
  @Bindable
  public List<IConnection> getConnections() {
    return connections;
  }

  public void addConnection(IConnection connection) {
    List<IConnection> previousValue = getPreviousValue();
    connections.add(connection);
    this.firePropertyChange("connections", previousValue, connections); //$NON-NLS-1$
  }

  public void updateConnection(IConnection connection) {
    List<IConnection> previousValue = getPreviousValue();
    IConnection conn = getConnectionByName(connection.getName());
    conn.setDriverClass(connection.getDriverClass());
    conn.setPassword(connection.getPassword());
    conn.setUrl(connection.getUrl());
    conn.setUsername(connection.getUsername());
    this.firePropertyChange("connections", previousValue, connections); //$NON-NLS-1$
  }

  @Bindable
  private List<IConnection> getPreviousValue() {
    List<IConnection> previousValue = new ArrayList<IConnection>();
    for (IConnection conn : connections) {
      previousValue.add(conn);
    }
    return previousValue;
  }

  public void deleteConnection(IConnection connection) {
    List<IConnection> previousValue = getPreviousValue();
    connections.remove(connections.indexOf(connection));
    this.firePropertyChange("connections", previousValue, connections); //$NON-NLS-1$
  }

  public void deleteConnection(String name) {
    for (IConnection connection : connections) {
      if (connection.getName().equals(name)) {
        deleteConnection(connection);
        break;
      }
    }
  }

  @Bindable
  public void setConnections(List<IConnection> value) {
    List<IConnection> previousValue = getPreviousValue();
    this.connections = value;
    this.firePropertyChange("connections", previousValue, value); //$NON-NLS-1$
  }

  @Bindable
  public String getPreviewLimit() {
    return previewLimit;
  }

  @Bindable
  public void setPreviewLimit(String value) {
    String previousVal = this.previewLimit;
    this.previewLimit = value;
    this.firePropertyChange("previewLimit", previousVal, value); //$NON-NLS-1$
  }

  public IConnection getConnectionByName(String name) {
    for (IConnection connection : connections) {
      if (connection.getName().equals(name)) {
        return connection;
      }
    }
    return null;
  }

  public Integer getConnectionIndex(IConnection conn) {
    IConnection connection = getConnectionByName(conn.getName());
    return connections.indexOf(connection);
  }

  @Bindable
  public boolean isRelationalValidated() {
    return relationalValidated;
  }

  @Bindable
  private void setRelationalValidated(boolean value) {
    if(value != this.relationalValidated) {
      this.relationalValidated = value;
      this.firePropertyChange("relationalValidated", !value, value);
    }
  }

  public void validateRelational() {
    setRelationalPreviewValidated(true);
    setRelationalApplyValidated(true);
    setRelationalValidated(true);
    fireRelationalModelValid();
  }

  public void invalidateRelational() {
    setRelationalPreviewValidated(false);
    setRelationalApplyValidated(false);
    setRelationalValidated(false);
    fireRelationalModelInValid();
  }

  private List<String> getColumnData(int columnNumber, List<List<String>> data) {
    List<String> column = new ArrayList<String>();
    for (List<String> row : data) {
      if (columnNumber < row.size()) {
        column.add(row.get(columnNumber));
      }
    }
    return column;
  }

  /*
   * Clears out the model
   */
  @Bindable
  public void clearModel() {
    setPreviewLimit("10");
    setSelectedCsvFile(null);
  }

  public void addRelationalModelValidationListener(IRelationalModelValidationListener listener) {
    if (relationalModelValidationListeners == null) {
      relationalModelValidationListeners = new RelationalModelValidationListenerCollection();
    }
    relationalModelValidationListeners.add(listener);
  }

  public void removeRelationalListener(IRelationalModelValidationListener listener) {
    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.remove(listener);
    }
  }

  /**
   * Fire all current {@link IRelationalModelValidationListener}.
   */
  void fireRelationalModelValid() {

    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.fireRelationalModelValid();
    }
  }
  
  /**
   * Fire all current {@link IRelationalModelValidationListener}.
   */
  void fireRelationalModelInValid() {

    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.fireRelationalModelInValid();
    }
  }
  public void setRelationalPreviewValidated(boolean value) {
    if (value != this.relationalPreviewValidated) {
      this.relationalPreviewValidated = value;
      this.firePropertyChange("relationalPreviewValidated", !value, this.relationalPreviewValidated);
    }
  }
  public boolean isRelationalPreviewValidated() {
    return this.relationalPreviewValidated;
  }
  
  public boolean isRelationalApplyValidated() {
    return relationalApplyValidated;
  }

  public void setRelationalApplyValidated(boolean value) {
    if (value != this.relationalApplyValidated) {
      this.relationalApplyValidated = value;
      this.firePropertyChange("relationalApplyValidated", !value, this.relationalApplyValidated);
    }    
  }

  public List<LogicalModel> getLogicalModels() {
    return logicalModels;
  }

  public void setLogicalModels(List<LogicalModel> logicalModels) {
    this.logicalModels = logicalModels;
  }

  public String getLocaleCode() {
    return localeCode;
  }

  public void setLocaleCode(String localeCode) {
    this.localeCode = localeCode;
  }

  public IXulAsyncConnectionService getConnectionService() {
    return connectionService;
  }

  public void setConnectionService(IXulAsyncConnectionService connectionService) {
    this.connectionService = connectionService;
  }

  public void setDataStagingComplete(boolean status) {
    dataStagingComplete = status;
  }
  
  public boolean isDataStagingComplete() {
    return dataStagingComplete;
  }

  public FileInfo getSelectedCsvFile() {
    return selectedCsvFile;
  }

  public void setSelectedCsvFile(FileInfo selectedCsvFile) {
    this.selectedCsvFile = selectedCsvFile;
  }

  @Bindable
  public boolean isEditing() {
    return editing;
  }

  @Bindable
  public void setEditing(boolean editing) {
    this.editing = editing;
    firePropertyChange("editing", null, editing);
  }

  @Bindable
  public boolean isDirty() {
    return dirty;
  }

  @Bindable
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
    firePropertyChange("dirty", null, dirty);
  }
}
