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


package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncDSWDatasourceService {
  void getLogicalModels( String context, XulServiceCallback<List<LogicalModelSummary>> callback );

  void deleteLogicalModel( String domainId, String modelName, XulServiceCallback<Boolean> callback );

  void doPreview( String connectionName, String query, String previewLimit,
                  XulServiceCallback<SerializedResultSet> callback );

  void generateLogicalModel( String modelName, String connectionName, String dbType, String query, String previewLimit,
                             XulServiceCallback<BusinessData> callback );

  void saveLogicalModel( Domain domain, boolean overwrite, XulServiceCallback<Boolean> callback );

  void hasPermission( XulServiceCallback<Boolean> callback );

  void loadBusinessData( String domainId, String modelId, XulServiceCallback<BusinessData> callback );

  void serializeModelState( DatasourceDTO dto, XulServiceCallback<String> callback );

  void deSerializeModelState( String dtoStr, XulServiceCallback<DatasourceDTO> callback );

  public void listDatasourceNames( XulServiceCallback<List<String>> callback );

  void generateQueryDomain( String name, String query, DatabaseConnection connection, DatasourceDTO datasourceDTO,
                            XulServiceCallback<IDatasourceSummary> callback );

  void getDatasourceIllegalCharacters( XulServiceCallback<String> callback );

  void getGeoContext( XulServiceCallback<GeoContext> callback );
}
