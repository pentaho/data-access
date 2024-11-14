/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtDSWDatasourceServiceAsync {
  void getLogicalModels( String context, AsyncCallback<List<LogicalModelSummary>> callback );

  void deleteLogicalModel( String domainId, String modelName, AsyncCallback<Boolean> callback );

  void doPreview( String connectionName, String query, String previewLimit,
                  AsyncCallback<SerializedResultSet> callback );

  void generateLogicalModel( String modelName, String connectionName, String dbType, String query, String previewLimit,
                             AsyncCallback<BusinessData> callback );

  void saveLogicalModel( Domain domain, boolean overwrite, AsyncCallback<Boolean> callback );

  void hasPermission( AsyncCallback<Boolean> callback );

  void gwtWorkaround( BogoPojo pojo, AsyncCallback<BogoPojo> callback );

  void loadBusinessData( String domainId, String modelId, AsyncCallback<BusinessData> callback );

  void serializeModelState( DatasourceDTO dto, AsyncCallback<String> callback );

  void deSerializeModelState( String dtoStr, AsyncCallback<DatasourceDTO> callback );

  public void listDatasourceNames( AsyncCallback<List<String>> callback );

  void generateQueryDomain( String name, String query, DatabaseConnection connection, DatasourceDTO datasourceDTO,
                            AsyncCallback<QueryDatasourceSummary> callback );

  void getDatasourceIllegalCharacters( AsyncCallback<String> callback );

  void getGeoContext( AsyncCallback<GeoContext> callback );
}
