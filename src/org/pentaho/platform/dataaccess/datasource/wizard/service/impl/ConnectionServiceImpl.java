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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.dialect.IDatabaseDialect;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseConnectionService;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.IServiceManager;
import org.pentaho.platform.api.engine.ServiceException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasource;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.ConnectionServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

/**
 * ConnectionServiceImpl extends PenahoBase so that it inherits the ILogger functionality.
 */
public class ConnectionServiceImpl extends PentahoBase implements IConnectionService {

  private IDataAccessPermissionHandler dataAccessPermHandler;

  private IDatasourceMgmtService datasourceMgmtSvc;

  private static final Log logger = LogFactory.getLog(ConnectionServiceImpl.class);

  public Log getLogger() {
    return logger;
  }
  
  public ConnectionServiceImpl() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    datasourceMgmtSvc = PentahoSystem.get(IDatasourceMgmtService.class, session);
    String dataAccessClassName = null;
    try {
      //FIXME: we should be using an object factory of some kind here
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
      dataAccessClassName = resLoader.getPluginSetting(getClass(),
          "settings/data-access-permission-handler", SimpleDataAccessPermissionHandler.class.getName()); //$NON-NLS-1$ 
      Class<?> clazz = Class.forName(dataAccessClassName, true, getClass().getClassLoader());
      Constructor<?> defaultConstructor = clazz.getConstructor(new Class[] {});
      dataAccessPermHandler = (IDataAccessPermissionHandler) defaultConstructor.newInstance(new Object[] {});
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR", e //$NON-NLS-1$
          .getLocalizedMessage()), e);
      // TODO: Unhardcode once this is an actual plugin
      dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
    }

  }

  protected boolean hasDataAccessPermission() {
    return dataAccessPermHandler != null
        && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession());
  }

  public List<IConnection> getConnections() throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }

    List<IConnection> connectionList = new ArrayList<IConnection>();
    try {
      for (IDatasource datasource : datasourceMgmtSvc.getDatasources()) {
        connectionList.add(convertTo(datasource));
      }
    } catch (DatasourceMgmtServiceException dme) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0002_UNABLE_TO_GET_CONNECTION_LIST", dme //$NON-NLS-1$
          .getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0002_UNABLE_TO_GET_CONNECTION_LIST", dme.getLocalizedMessage()), dme); //$NON-NLS-1$
    }
    return connectionList;
  }

  public IConnection getConnectionByName(String name) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }
    try {
      return convertTo(datasourceMgmtSvc.getDatasource(name));
    } catch (DatasourceMgmtServiceException dme) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", name, dme //$NON-NLS-1$
          .getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", name, dme.getLocalizedMessage()), dme); //$NON-NLS-1$
    }
  }

  public boolean addConnection(IConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }

    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.createDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0004_UNABLE_TO_ADD_CONNECTION", connection //$NON-NLS-1$
          .getName(), e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0004_UNABLE_TO_ADD_CONNECTION", connection.getName(), e //$NON-NLS-1$
              .getLocalizedMessage()), e);
    }
  }

  public boolean updateConnection(IConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }
    try {
      HibernateUtil.beginTransaction();
      connection.setPassword(ConnectionServiceHelper.getConnectionPassword(connection.getName(), connection
          .getPassword()));
      datasourceMgmtSvc.updateDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION", //$NON-NLS-1$
          connection.getName(), e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION", connection.getName(), e //$NON-NLS-1$
              .getLocalizedMessage()), e);
    }
  }

  public boolean deleteConnection(IConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }
    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(convertFrom(connection));
      HibernateUtil.commitTransaction();
      return true;
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", //$NON-NLS-1$
          connection.getName(), e.getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", connection.getName(), e //$NON-NLS-1$
              .getLocalizedMessage()), e);
    }
  }

  public boolean deleteConnection(String name) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }

    try {
      HibernateUtil.beginTransaction();
      datasourceMgmtSvc.deleteDatasource(name);
      HibernateUtil.commitTransaction();
      return true;
    } catch (Exception e) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", name, e //$NON-NLS-1$
          .getLocalizedMessage()));
      throw new ConnectionServiceException(Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", name, e.getLocalizedMessage()), e); //$NON-NLS-1$
    }
  }

  public boolean testConnection(IConnection connection) throws ConnectionServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }
    if (connection != null) {
      if (connection.getPassword() == null) { // Can have an empty password but not a null one
        connection.setPassword(""); //$NON-NLS-1$
      }
      IPentahoConnection pentahoConnection = null;
      pentahoConnection = PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connection
          .getDriverClass(), connection.getUrl(), connection.getUsername(), ConnectionServiceHelper
          .getConnectionPassword(connection.getName(), connection.getPassword()), null, this);
      if (pentahoConnection != null) {
        // make sure we have a native connection behind the SQLConnection object
        // if the native connection is null, the test of the connection failed
        boolean testedOk = ((SQLConnection) pentahoConnection).getNativeConnection() != null;
        pentahoConnection.close();
        return testedOk;
      } else {
        return false;
      }
    } else {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0008_UNABLE_TO_TEST_NULL_CONNECTION")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0008_UNABLE_TO_TEST_NULL_CONNECTION")); //$NON-NLS-1$
    }
  }

  /**
   * This method converts from IDatasource to IConnection 
   * 
   * @param IDatasource
   * @return IConnection
   */
  private IConnection convertTo(IDatasource datasource) {
    if( datasource == null ) {
      // make sure we don't attempt bad conversions
      return null;
    }
    IConnection returnDatasource = new org.pentaho.platform.dataaccess.datasource.beans.Connection();
    returnDatasource.setDriverClass(datasource.getDriverClass());
    returnDatasource.setName(datasource.getName());
    returnDatasource.setPassword(ConnectionServiceHelper.encodePassword(datasource.getPassword()));
    returnDatasource.setUsername(datasource.getUserName());
    returnDatasource.setUrl(datasource.getUrl());
    return returnDatasource;
  }

  /**
   * This method converts from IConnection to IDatasource 
   * 
   * @param IConnection
   * @return IDatasource
   */
  private IDatasource convertFrom(IConnection connection) {
    if( connection == null ) {
      // make sure we don't attempt bad conversions
      return null;
    }
    IDatasource returnDatasource = (IDatasource) PentahoSystem.get(IDatasource.class, null);
    returnDatasource.setDriverClass(connection.getDriverClass());
    returnDatasource.setName(connection.getName());
    returnDatasource.setPassword(connection.getPassword());
    returnDatasource.setUserName(connection.getUsername());
    returnDatasource.setUrl(connection.getUrl());
    return returnDatasource;
  }

  public IDatabaseConnection convertFromConnection(IConnection connection) throws ConnectionServiceException {
    if( connection == null ) {
      // make sure we don't attempt bad conversions
      return null;
    }
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }

    try {
      IServiceManager manager = PentahoSystem.get(IServiceManager.class);
      DatabaseConnectionService service = (DatabaseConnectionService) manager.getServiceBean("gwt", //$NON-NLS-1$
          "databaseConnectionService"); //$NON-NLS-1$
      IDatabaseConnection conn = service.createDatabaseConnection(connection.getDriverClass(), connection.getUrl());
      conn.setName(connection.getName());
      conn.setUsername(connection.getUsername());
      conn.setPassword(ConnectionServiceHelper.encodePassword(connection.getPassword()));
      return conn;
    } catch (ServiceException e) {
      throw new ConnectionServiceException(e);
    } catch (Throwable e) {
      throw new ConnectionServiceException(e);
    }
  }

  public IConnection convertToConnection(IDatabaseConnection connection) throws ConnectionServiceException {
    if( connection == null ) {
      // make sure we don't attempt bad conversions
      return null;
    }
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new ConnectionServiceException(Messages
          .getErrorString("ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }

    try {
      IServiceManager manager = PentahoSystem.get(IServiceManager.class);

      DatabaseConnectionService service = (DatabaseConnectionService) manager.getServiceBean("gwt", //$NON-NLS-1$ 
          "databaseConnectionService"); //$NON-NLS-1$
      IDatabaseDialect dialect = service.getDialectService().getDialect(connection);

      Connection conn = new Connection();
      conn.setName(connection.getName());
      conn.setUsername(connection.getUsername());
      conn.setPassword(connection.getPassword());
      String url = dialect.getURLWithExtraOptions(connection);
      conn.setUrl(url);
      if (connection.getDatabaseType().getShortName().equals("GENERIC")) { //$NON-NLS-1$
        conn.setDriverClass(connection.getAttributes().get(GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS));
      } else {
        conn.setDriverClass(dialect.getNativeDriver());
      }
      return conn;
    } catch (KettleDatabaseException e) {
      throw new ConnectionServiceException(e);
    } catch (ServiceException e) {
      throw new ConnectionServiceException(e);
    }
  }
}
