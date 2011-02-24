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
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

/**
 * A wrapper around ConnectionServiceImpl that provides an API that uses only concrete classes
 * not interfaces. This is needed by Apache Axis in order to expose this as a SOAP service.
 * @author jamesdixon
 *
 */
public class ConnectionServiceConcrete {

  private ConnectionServiceImpl service = new ConnectionServiceImpl();

  public ConnectionServiceConcrete() {
    service = new ConnectionServiceImpl();
  }

  public List<Connection> getConnections() throws ConnectionServiceException {
    List<IConnection> iConnections = service.getConnections();
    List<Connection> connections = new ArrayList<Connection>();
    for( IConnection iConnection : iConnections ) {
      connections.add( (Connection) iConnection );
    }
    return connections;
  }

  public Connection getConnectionByName(String name) throws ConnectionServiceException {
    return (Connection) service.getConnectionByName(name);
  }

  public boolean addConnection(Connection connection) throws ConnectionServiceException {
    return service.addConnection(connection);
  }

  public boolean updateConnection(Connection connection) throws ConnectionServiceException {
    return service.updateConnection(connection);
  }

  public boolean deleteConnection(Connection connection) throws ConnectionServiceException {
    return service.deleteConnection(connection);
  }

  public boolean deleteConnectionByName(String name) throws ConnectionServiceException {
    return service.deleteConnection(name);
  }

  public boolean testConnection(Connection connection) throws ConnectionServiceException {
    return service.testConnection(connection);
  }

  public DatabaseConnection convertFromConnection(Connection connection) throws ConnectionServiceException {
      return (DatabaseConnection) service.convertFromConnection(connection);
  }

  public Connection convertToConnection(DatabaseConnection connection) throws ConnectionServiceException {
    return (Connection) service.convertToConnection(connection);
  }
}
