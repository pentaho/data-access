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
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;

public interface IDSWDatasourceService {
  /**
   * Returns the list of Logical Models. This method is used by the client app to display list of models
   *
   * @return List of LogicalModelSummary.
   */
  public List<LogicalModelSummary> getLogicalModels( String context ) throws DatasourceServiceException;

  /**
   * Delete the Logical Mode identified by the Domain ID and the Model Name
   *
   * @return true if the deletion of model was successful otherwise false.
   */
  public boolean deleteLogicalModel( String domainId, String modelName ) throws DatasourceServiceException;

  /**
   * Returns the serialized version of SQL ResultSet.
   *
   * @param connectionName - Name of the connection
   * @param query          - Query which needs to be executed
   * @param previewLimit   - Number of row which needs to be returned for this query
   * @return SerializedResultSet - This object contains the data, column name and column types
   * @throws DatasourceServiceException
   */
  public SerializedResultSet doPreview( String connectionName, String query, String previewLimit )
    throws DatasourceServiceException;

  /**
   * Returns the generated relational based logical model along with the sample data for the given connection name and
   * query
   *
   * @param modelName      - Name of the model to be generated
   * @param connectionName - Name of the connection
   * @param dbType         - Dialect type
   * @param query          - Query which needs to be executed
   * @param previewLimit   - Number of row which needs to be returned for this query
   * @return BusinessData - This object contains the data, column name, column types and sample data
   * @throws DatasourceServiceException
   */
  public BusinessData generateLogicalModel( String modelName, String connectionName, String dbType, String query,
                                            String previewLimit ) throws DatasourceServiceException;

  /**
   * Save the generated model. This could be either Relational or CSV based model
   *
   * @param domain    - generated Domain
   * @param overwrite - should the domain be overwritten or not
   * @return true if the model was saved successfully otherwise false
   * @throws DatasourceServiceException
   */
  public boolean saveLogicalModel( Domain domain, boolean overwrite ) throws DatasourceServiceException;

  /**
   * Returns whether the current user has the authority to create/edit/delete datasources
   *
   * @return true if the user has permission otherwise false
   * @throws DatasourceServiceException
   */
  public boolean hasPermission();

  /**
   * This is a method for the Gwt workaround. This should not be used by any client at all
   *
   * @return BogoPojo
   */
  public BogoPojo gwtWorkaround( BogoPojo pojo );

  /**
   * Returns the save logical model for a given Domain ID and Model ID
   *
   * @param domainId - ID of the domain to be generated
   * @param modelId  - ID of the model to be generated
   * @return BusinessData - This object contains the data, column name, column types and sample data
   * @throws DatasourceServiceException
   */
  public BusinessData loadBusinessData( String domainId, String modelId ) throws DatasourceServiceException;

  /**
   * Returns a serialized version of the DatasourceDTO class.
   *
   * @param dto - Datasource data transfer object to serialize
   * @throws DatasourceServiceException
   */
  String serializeModelState( DatasourceDTO dto ) throws DatasourceServiceException;

  /**
   * Returns a DatasourceDTO from a serialized string.
   *
   * @param dto - Datasource data transfer object to serialize
   * @throws DatasourceServiceException
   */
  DatasourceDTO deSerializeModelState( String dto ) throws DatasourceServiceException;

  public List<String> listDatasourceNames() throws IOException;

  QueryDatasourceSummary generateQueryDomain( String name, String query, DatabaseConnection connection,
                                              DatasourceDTO datasourceDTO ) throws DatasourceServiceException;

  /**
   * Returns a list of illegal characters in a string that are not allowed in a Data Source name This string is stored
   * in settings.xml in data-access-datasource-illegal-characters xml tag
   *
   * @return string of illegal character
   * @throws DatasourceServiceException
   */
  public String getDatasourceIllegalCharacters() throws DatasourceServiceException;

  /**
   * Returns a GeoContext object configured from the settings.xml file for the data access plugin.
   *
   * @return GeoContext
   * @throws DatasourceServiceException
   */
  public GeoContext getGeoContext() throws DatasourceServiceException;

  //  public Map<String, InputStream>getDomainFilesData(String domainId) throws DatasourceServiceException;
}
