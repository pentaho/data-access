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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.exception.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IDSWDatasourceService;
import java.io.IOException;
import java.util.List;

public class DSWDatasourceServiceImpl implements IDSWDatasourceService {
  /**
   * Returns the list of Logical Models. This method is used by the client app to display list of models
   *
   * @return List of LogicalModelSummary.
   */
  @Override
  public List<LogicalModelSummary> getLogicalModels(String context) throws DatasourceServiceException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Delete the Logical Mode identified by the Domain ID and the Model Name
   *
   * @return true if the deletion of model was successful otherwise false.
   */
  @Override
  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Returns the serialized version of SQL ResultSet.
   *
   * @param connectionName - Name of the connection
   * @param query          - Query which needs to be executed
   * @param previewLimit   - Number of row which needs to be returned for this query
   * @return SerializedResultSet - This object contains the data, column name and column types
   * @throws org.pentaho.platform.dataaccess.datasource.wizard.service.exception.DatasourceServiceException
   *
   */
  @Override
  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit) throws DatasourceServiceException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Save the generated model. This could be either Relational or CSV based model
   *
   * @param domain    - generated Domain
   * @param overwrite - should the domain be overwritten or not
   * @return true if the model was saved successfully otherwise false
   * @throws org.pentaho.platform.dataaccess.datasource.wizard.service.exception.DatasourceServiceException
   *
   */
  @Override
  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Returns whether the current user has the authority to create/edit/delete datasources
   *
   * @return true if the user has permission otherwise false
   * @throws org.pentaho.platform.dataaccess.datasource.wizard.service.exception.DatasourceServiceException
   *
   */
  @Override
  public boolean hasPermission() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Returns a list of datasource names
   *
   * @return
   * @throws java.io.IOException
   */
  @Override
  public List<String> listDatasourceNames() throws IOException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  /**
   * Returns a list of illegal characters in a string that are not allowed in a Data Source name
   * This string is stored in settings.xml in data-access-datasource-illegal-characters xml tag
   *
   * @return string of illegal character
   * @throws org.pentaho.platform.dataaccess.datasource.wizard.service.exception.DatasourceServiceException
   *
   */
  @Override
  public String getDatasourceIllegalCharacters() throws DatasourceServiceException {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
}
