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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
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

  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit) throws DatasourceServiceException{
    return SERVICE.doPreview(connectionName, query, previewLimit);
  }

  public BusinessData generateLogicalModel(String modelName, String connectionName, String dbType, String query, String previewLimit) throws DatasourceServiceException {
    return SERVICE.generateLogicalModel(modelName, connectionName, dbType, query, previewLimit);
   }
  public boolean saveLogicalModel(Domain modelName, boolean overwrite) throws DatasourceServiceException {
    return SERVICE.saveLogicalModel(modelName, overwrite);
  }
  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }
  
  public boolean hasPermission() {
    return SERVICE.hasPermission();
  }

  public boolean deleteLogicalModel(String domainId, String modelName)  throws DatasourceServiceException {
    return SERVICE.deleteLogicalModel(domainId, modelName);
  }

  public List<LogicalModelSummary> getLogicalModels(String context) throws DatasourceServiceException {
    return SERVICE.getLogicalModels(context);
  }

  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException {
    return SERVICE.loadBusinessData(domainId, modelId);
  }


  public String serializeModelState(DatasourceDTO dto) throws DatasourceServiceException{
    return SERVICE.serializeModelState(dto);
  }

  public DatasourceDTO deSerializeModelState(String dtoStr) throws DatasourceServiceException{
    return SERVICE.deSerializeModelState(dtoStr);
  }

  @Override
  public List<String> listDatasourceNames() throws IOException {
    return SERVICE.listDatasourceNames();
  }

  public QueryDatasourceSummary generateQueryDomain(String name, String query, DatabaseConnection connection, DatasourceDTO datasourceDTO) throws DatasourceServiceException {
    try {
      return SERVICE.generateQueryDomain(name, query, connection, datasourceDTO);
    } catch (DatasourceServiceException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      throw e;
    }
  }

  @Override
  public String getDatasourceIllegalCharacters() throws DatasourceServiceException {
    try {
      return SERVICE.getDatasourceIllegalCharacters();
    } catch (DatasourceServiceException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      throw e;
    }
  }

  @Override
  public GeoContext getGeoContext() throws DatasourceServiceException {
    try{
      return SERVICE.getGeoContext();
    } catch (DatasourceServiceException e) {
      e.printStackTrace();
      throw e;
    }
  }

}