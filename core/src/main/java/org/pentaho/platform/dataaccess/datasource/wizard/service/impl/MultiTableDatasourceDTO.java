/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.Serializable;
import java.util.List;

import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.database.model.IDatabaseConnection;

public class MultiTableDatasourceDTO implements Serializable {

  private static final long serialVersionUID = 1368165523678535182L;

  private String datasourceName;
  private IDatabaseConnection selectedConnection;
  private SchemaModel schemaModel;
  private List<String> selectedTables;
  private boolean doOlap;

  public MultiTableDatasourceDTO() {
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public void setDatasourceName( String datasourceName ) {
    this.datasourceName = datasourceName;
  }

  public SchemaModel getSchemaModel() {
    return schemaModel;
  }

  public void setSchemaModel( SchemaModel schemaModel ) {
    this.schemaModel = schemaModel;
  }

  public IDatabaseConnection getSelectedConnection() {
    return selectedConnection;
  }

  public void setSelectedConnection( IDatabaseConnection selectedConnection ) {
    this.selectedConnection = selectedConnection;
  }

  public List<String> getSelectedTables() {
    return selectedTables;
  }

  public void setSelectedTables( List<String> selectedTables ) {
    this.selectedTables = selectedTables;
  }

  public boolean isDoOlap() {
    return doOlap;
  }

  public void setDoOlap( boolean doOlap ) {
    this.doOlap = doOlap;
  }
}
