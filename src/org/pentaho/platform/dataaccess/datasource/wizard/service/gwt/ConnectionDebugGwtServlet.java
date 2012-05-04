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

import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryConnectionServiceImpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class ConnectionDebugGwtServlet extends RemoteServiceServlet implements IGwtConnectionService {

  /**
   * 
   */
  private static final long serialVersionUID = -6800099826721568704L;
  public static InMemoryConnectionServiceImpl SERVICE;

 
  public ConnectionDebugGwtServlet() {

  }

  private InMemoryConnectionServiceImpl getService(){
    if(SERVICE == null){
      try {
        SERVICE = new InMemoryConnectionServiceImpl();
        // add the sample data default connection for testing
        Connection connection = new Connection();
        connection.setDriverClass("org.hsqldb.jdbcDriver"); //$NON-NLS-1$
        connection.setName("SampleData");//$NON-NLS-1$
        connection.setUrl("jdbc:hsqldb:file:test-res/solution1/system/data/sampledata");//$NON-NLS-1$
        connection.setUsername("pentaho_user");//$NON-NLS-1$
        connection.setPassword("password");//$NON-NLS-1$
        SERVICE.addConnection(connection);
      } catch (Exception e) {
        System.out.println(e.getMessage());
        e.printStackTrace();
      }
    }
    return SERVICE;
  }

  public List<Connection> getConnections()  throws ConnectionServiceException {
    return getService().getConnections();
  }
  public Connection getConnectionByName(String name)  throws ConnectionServiceException {
    return getService().getConnectionByName(name);
  }
  public boolean addConnection(Connection connection)  throws ConnectionServiceException{ 
    return getService().addConnection(connection);
  }

  public boolean updateConnection(Connection connection)  throws ConnectionServiceException {
    return getService().updateConnection(connection);
  }

  public boolean deleteConnection(Connection connection)  throws ConnectionServiceException {
    return getService().deleteConnection(connection);
  }
    
  public boolean deleteConnection(String name)  throws ConnectionServiceException {
    return getService().deleteConnection(name);    
  }

  public boolean testConnection(Connection connection)  throws ConnectionServiceException{
    return getService().testConnection(connection);
  }

  public IDatabaseConnection convertFromConnection(Connection connection) throws ConnectionServiceException {
    return getService().convertFromConnection(connection);
  }

  public Connection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException {
    return getService().convertToConnection(connection);
  }
}