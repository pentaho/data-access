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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

/**
 * The IConnectionService interface is used as a GWT and XML webservice.  It is also used by the Datasource Service to
 * map a name to an IConnection.
 */
public interface IConnectionService {
  List<IDatabaseConnection> getConnections() throws ConnectionServiceException;

  IDatabaseConnection getConnectionByName( String name ) throws ConnectionServiceException;

  boolean addConnection( IDatabaseConnection connection ) throws ConnectionServiceException;

  boolean updateConnection( IDatabaseConnection connection ) throws ConnectionServiceException;

  boolean deleteConnection( IDatabaseConnection connection ) throws ConnectionServiceException;

  boolean deleteConnection( String name ) throws ConnectionServiceException;

  boolean testConnection( IDatabaseConnection connection ) throws ConnectionServiceException;

  boolean isConnectionExist( String connectionName ) throws ConnectionServiceException;
}
