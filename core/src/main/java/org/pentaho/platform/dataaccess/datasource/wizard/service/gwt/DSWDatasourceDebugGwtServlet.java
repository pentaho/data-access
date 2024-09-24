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

package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.io.IOException;
import java.util.List;

import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryDSWDatasourceServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DSWDatasourceDebugGwtServlet extends RemoteServiceServlet implements IGwtDSWDatasourceService {

  /**
   *
   */
  private static final long serialVersionUID = -8247397306730500944L;
  InMemoryDSWDatasourceServiceImpl SERVICE;

  public DSWDatasourceDebugGwtServlet() {
    SERVICE = new InMemoryDSWDatasourceServiceImpl();
  }

  public SerializedResultSet doPreview( String connectionName, String query, String previewLimit )
    throws DatasourceServiceException {
    return SERVICE.doPreview( connectionName, query, previewLimit );
  }

  public BusinessData generateLogicalModel( String modelName, String connectionName, String dbType, String query,
                                            String previewLimit ) throws DatasourceServiceException {
    return SERVICE.generateLogicalModel( modelName, connectionName, dbType, query, previewLimit );
  }

  public boolean saveLogicalModel( Domain modelName, boolean overwrite ) throws DatasourceServiceException {
    return SERVICE.saveLogicalModel( modelName, overwrite );
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return pojo;
  }

  public boolean hasPermission() {
    return SERVICE.hasPermission();
  }

  public boolean deleteLogicalModel( String domainId, String modelName ) throws DatasourceServiceException {
    return SERVICE.deleteLogicalModel( domainId, modelName );
  }

  public List<LogicalModelSummary> getLogicalModels( String context ) throws DatasourceServiceException {
    return SERVICE.getLogicalModels( context );
  }

  public BusinessData loadBusinessData( String domainId, String modelId ) throws DatasourceServiceException {
    return SERVICE.loadBusinessData( domainId, modelId );
  }


  public String serializeModelState( DatasourceDTO dto ) throws DatasourceServiceException {
    return SERVICE.serializeModelState( dto );
  }

  public DatasourceDTO deSerializeModelState( String dtoStr ) throws DatasourceServiceException {
    return SERVICE.deSerializeModelState( dtoStr );
  }

  @Override
  public List<String> listDatasourceNames() throws IOException {
    return SERVICE.listDatasourceNames();
  }

  public QueryDatasourceSummary generateQueryDomain( String name, String query, DatabaseConnection connection,
                                                     DatasourceDTO datasourceDTO ) throws DatasourceServiceException {
    try {
      return SERVICE.generateQueryDomain( name, query, connection, datasourceDTO );
    } catch ( DatasourceServiceException e ) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      throw e;
    }
  }

  @Override
  public String getDatasourceIllegalCharacters() throws DatasourceServiceException {
    try {
      return SERVICE.getDatasourceIllegalCharacters();
    } catch ( DatasourceServiceException e ) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      throw e;
    }
  }

  @Override
  public GeoContext getGeoContext() throws DatasourceServiceException {
    try {
      return SERVICE.getGeoContext();
    } catch ( DatasourceServiceException e ) {
      e.printStackTrace();
      throw e;
    }
  }

}
