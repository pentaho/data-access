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
 * Created July 15, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.io.IOException;
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;

public interface IDatasourceService {
  /**
   * Returns the list of Logical Models. This method is used by the client app to display list of models
   * 
   * @return List of LogicalModelSummary.
   */
  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException;
  /**
   * Delete the Logical Mode identified by the Domain ID and the Model Name
   * 
   * @return true if the deletion of model was successful otherwise false.
   */  
  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException;
  /**
   * Returns the serialized version of SQL ResultSet. 
   * 
   * @param connectionName - Name of the connection
   * @param query - Query which needs to be executed
   * @param previewLimit - Number of row which needs to be returned for this query
   *
   * @throws DatasourceServiceException
   * @return SerializedResultSet - This object contains the data, column name and column types
   */    
  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit) throws DatasourceServiceException;
  /**
   * Returns the generated relational based logical model along with the sample data for the given connection name and query
   *
   * @param modelName - Name of the model to be generated   
   * @param connectionName - Name of the connection
   * @param dbType - Dialect type
   * @param query - Query which needs to be executed
   * @param previewLimit - Number of row which needs to be returned for this query
   * 
   * @throws DatasourceServiceException 
   * @return BusinessData - This object contains the data, column name, column types and sample data
   */    
  public BusinessData generateLogicalModel(String modelName, String connectionName, String dbType, String query, String previewLimit) throws DatasourceServiceException;
  /**
   * Save the generated model. This could be either Relational or CSV based model
   *
   * @param domain - generated Domain     
   * @param overwrite - should the domain be overwritten or not
   * 
   * @throws DatasourceServiceException
   * @return true if the model was saved successfully otherwise false
   */    
  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException;

  /**
   * Returns whether the current user has the authority to create/edit/delete datasources
   *
   * @throws DatasourceServiceException
   * @return true if the user has permission otherwise false
   */      
  public boolean hasPermission();
  /**
   * This is a method for the Gwt workaround. This should not be used by any client at all
   *
   * @return BogoPojo
   */      
  public BogoPojo gwtWorkaround(BogoPojo pojo);
  /**
   * Returns the save logical model for a given Domain ID and Model ID
   *
   * @param domainId - ID of the domain to be generated   
   * @param modelId - ID of the model to be generated
   * 
   * @throws DatasourceServiceException
   * @return BusinessData - This object contains the data, column name, column types and sample data
   */      
  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException;

  /**
   * Returns a serialized version of the DatasourceDTO class.
   *
   * @param dto - Datasource data transfer object to serialize
   * @throws DatasourceServiceException
   */
  String serializeModelState(DatasourceDTO dto) throws DatasourceServiceException;

  /**
   * Returns a DatasourceDTO from a serialized string.
   *
   * @param dto - Datasource data transfer object to serialize
   * @throws DatasourceServiceException
   */
  DatasourceDTO deSerializeModelState(String dto) throws DatasourceServiceException;

  public List<String> listDatasourceNames() throws IOException;

  QueryDatasourceSummary generateQueryDomain(String name, String query, String datasourceId, IConnection connection, DatasourceDTO datasourceDTO) throws DatasourceServiceException;
}

 