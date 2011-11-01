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
 * Created May 6, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.DatabaseDialectException;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseConnectionService;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;

public class InMemoryConnectionServiceImpl implements IConnectionService {

  private String locale = Locale.getDefault().toString();
  
  // this should be a singleton
  private DatabaseConnectionService databaseConnectionService = new DatabaseConnectionService();
  private DatabaseDialectService databaseDialectService = new DatabaseDialectService();

  private List<IConnection> connectionList = new ArrayList<IConnection>();
  private static final Log logger = LogFactory.getLog(InMemoryConnectionServiceImpl.class);
  public InMemoryConnectionServiceImpl() {
  }
  
  public List<IConnection> getConnections() throws ConnectionServiceException  {
    return connectionList;
  }
  public IConnection getConnectionByName(String name) throws ConnectionServiceException  {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(name)) {
        return connection;
      }
    }
    logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0003_UNABLE_TO_GET_CONNECTION",name,null));
    throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0003_UNABLE_TO_GET_CONNECTION",name,null));
  }
  public boolean addConnection(IConnection connection) throws ConnectionServiceException  {
    if(!isConnectionExist(connection.getName())) {
      connectionList.add(connection);
      return true;
    } else {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0004_UNABLE_TO_ADD_CONNECTION",connection.getName(),null));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0004_UNABLE_TO_ADD_CONNECTION",connection.getName(),null));
    }
  }
  public boolean updateConnection(IConnection connection) throws ConnectionServiceException  {
    IConnection conn = getConnectionByName(connection.getName());
    if(conn != null) {
      conn.setDriverClass(connection.getDriverClass());
      conn.setPassword(connection.getPassword());
      conn.setUrl(connection.getUrl());
      conn.setUsername(connection.getUsername());
      return true;
    } else {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION",connection.getName(),null));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION",connection.getName(),null));
    }
  }
  public boolean deleteConnection(IConnection connection) throws ConnectionServiceException  {
    connectionList.remove(connectionList.indexOf(connection));
    return true;
  }
  public boolean deleteConnection(String name) throws ConnectionServiceException  {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(name)) {
        return deleteConnection(connection);
      }
    }
    logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0006_UNABLE_TO_DELETE_CONNECTION",name,null));
    throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0006_UNABLE_TO_DELETE_CONNECTION",name,null));
  }
  
  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    Connection conn = null;
    try {
      conn = getConnection(connection);
    } catch (ConnectionServiceException dme) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),dme.getLocalizedMessage()),dme);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),dme.getLocalizedMessage()),dme);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),null));
        throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0026_UNABLE_TO_TEST_CONNECTION",connection.getName(),null));
      }
    }
    return true;
  }
  
  /**
   * NOTE: caller is responsible for closing connection
   * 
   * @param ds
   * @return
   * @throws DataSourceManagementException
   */
  private static Connection getConnection(IConnection connection) throws ConnectionServiceException {
    Connection conn = null;

    String driverClass = connection.getDriverClass();
    if (StringUtils.isEmpty(driverClass)) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0020_CONNECTION_ATTEMPT_FAILED"));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0020_CONNECTION_ATTEMPT_FAILED")); //$NON-NLS-1$
      
    }
    Class<?> driverC = null;

    try {
      driverC = Class.forName(driverClass);
    } catch (ClassNotFoundException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass),e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH"),e); //$NON-NLS-1$

    }
    if (!Driver.class.isAssignableFrom(driverC)) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH", driverClass));
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0021_DRIVER_NOT_FOUND_IN_CLASSPATH")); //$NON-NLS-1$

    }
    Driver driver = null;
    
    try {
      driver = driverC.asSubclass(Driver.class).newInstance();
    } catch (InstantiationException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    } catch (IllegalAccessException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER", driverClass),e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0022_UNABLE_TO_INSTANCE_DRIVER"), e); //$NON-NLS-1$
    }
    try {
      DriverManager.registerDriver(driver);
      conn = DriverManager.getConnection(connection.getUrl(), connection.getUsername(), connection.getPassword());
      return conn;
    } catch (SQLException e) {
      logger.error(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0023_UNABLE_TO_CONNECT"), e);
      throw new ConnectionServiceException(Messages.getErrorString("ConnectionServiceInMemoryDelegate.ERROR_0023_UNABLE_TO_CONNECT"), e); //$NON-NLS-1$
   }
  }
  private boolean isConnectionExist(String connectionName) {
    for(IConnection connection:connectionList) {
      if(connection.getName().equals(connectionName)) {
        return true;
      }
    }
    return false;
  }
  
  public IDatabaseConnection convertFromConnection(IConnection connection) throws ConnectionServiceException {
    IDatabaseConnection conn = databaseConnectionService.createDatabaseConnection(connection.getDriverClass(), connection.getUrl());
    conn.setName(connection.getName());
    conn.setUsername(connection.getUsername());
    conn.setPassword(connection.getPassword());
    return conn;
  }
  
  public IConnection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException {
    try {
      IDatabaseDialect dialect = databaseDialectService.getDialect(connection);
      org.pentaho.platform.dataaccess.datasource.beans.Connection conn = new org.pentaho.platform.dataaccess.datasource.beans.Connection();

      conn.setName(connection.getName());
      conn.setUsername(connection.getUsername());
      conn.setPassword(connection.getPassword());
      String url = dialect.getURL(connection);
      conn.setUrl(url);
      if (connection.getDatabaseType().getShortName().equals("GENERIC")) { //$NON-NLS-1$
        conn.setDriverClass(connection.getAttributes().get(GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS));
      } else {
        conn.setDriverClass(dialect.getNativeDriver());
      }
      return conn;
    } catch (DatabaseDialectException e) {
      throw new ConnectionServiceException(e);
    }
  }

}
