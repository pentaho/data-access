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
* Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

/**
 * A wrapper around ConnectionServiceImpl that provides an API that uses only concrete classes not interfaces. This is
 * needed by Apache Axis in order to expose this as a SOAP service.
 *
 * @author jamesdixon
 */
public class ConnectionServiceConcrete {

  private ConnectionServiceImpl service = new ConnectionServiceImpl();
  private static final Log logger = LogFactory.getLog( ConnectionServiceConcrete.class );

  public ConnectionServiceConcrete() {
  }

  public List<DatabaseConnection> getConnections() throws ConnectionServiceException {
    List<IDatabaseConnection> iConnections = service.getConnections();
    List<DatabaseConnection> connections = new ArrayList<DatabaseConnection>();
    for ( IDatabaseConnection iConnection : iConnections ) {
      hidePassword( iConnection );
      connections.add( (DatabaseConnection) iConnection );
    }
    return connections;
  }

  public DatabaseConnection getConnectionByName( String name ) throws ConnectionServiceException {
    DatabaseConnection connection = (DatabaseConnection) service.getConnectionByName( name );
    hidePassword( connection );
    return connection;
  }

  public boolean addConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    return service.addConnection( connection );
  }

  public boolean updateConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    applySavedPassword( connection );
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

  public boolean isConnectionExist( String connectionName ) throws ConnectionServiceException {
    return service.isConnectionExist( connectionName );
  }

  /**
   * Set password to empty string before sending to client
   */
  private void hidePassword( IDatabaseConnection connection ) {
    connection.setPassword( "" );
  }

  /**
   * If password is empty, that means client didn't change password.
   * Since we cleaned password during sending to client, we need to use stored password.
   * @throws ConnectionServiceException if unable to get connection
   */
  private void applySavedPassword( IDatabaseConnection conn ) throws ConnectionServiceException {
    if ( StringUtils.isBlank( conn.getPassword() ) ) {
      IDatabaseConnection savedConn;
      if ( conn.getId() != null ) {
        savedConn = service.getConnectionById( conn.getId() );
      } else {
        try {
          savedConn = service.getConnectionByName( conn.getName() );
        } catch ( ConnectionServiceException e ) {
          logger.warn( e.getMessage() );
          savedConn = null;
        }
      }
      // The connection might not be in the database because this may be a new
      // hive connection that doesn't require a password
      if ( savedConn != null ) {
        conn.setPassword( savedConn.getPassword() );
      }
    }
  }

}
