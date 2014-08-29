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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

/**
 * A wrapper around ConnectionServiceImpl that provides an API that uses only concrete classes not interfaces. This is
 * needed by Apache Axis in order to expose this as a SOAP service.
 * 
 * @author jamesdixon
 * 
 */
public class ConnectionServiceConcrete {

  private ConnectionServiceImpl service = new ConnectionServiceImpl();

  public ConnectionServiceConcrete() {
  }

  public List<DatabaseConnection> getConnections() throws ConnectionServiceException {
    List<IDatabaseConnection> iConnections = service.getConnections();
    List<DatabaseConnection> connections = new ArrayList<DatabaseConnection>();
    for ( IDatabaseConnection iConnection : iConnections ) {
      connections.add( (DatabaseConnection) iConnection );
    }
    return connections;
  }

  public DatabaseConnection getConnectionByName( String name ) throws ConnectionServiceException {
    return (DatabaseConnection) service.getConnectionByName( name );
  }

  public boolean addConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    return service.addConnection( connection );
  }

  public boolean updateConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    return service.updateConnection( connection );
  }

  public boolean deleteConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    return service.deleteConnection( connection );
  }

  public boolean deleteConnectionByName( String name ) throws ConnectionServiceException {
    return service.deleteConnection( name );
  }

  public boolean testConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    return service.testConnection( connection );
  }

  public String isConnectionExist( String connectionName ) throws ConnectionServiceException {
    return service.isConnectionExist( connectionName );
  }

}
