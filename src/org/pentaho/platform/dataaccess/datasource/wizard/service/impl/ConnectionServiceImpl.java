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

import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.database.DatabaseDialectException;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.ConnectionServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

import com.google.gwt.http.client.Response;

/**
 * ConnectionServiceImpl extends PenahoBase so that it inherits the ILogger functionality.
 */
public class ConnectionServiceImpl extends PentahoBase implements IConnectionService {

  private IDataAccessPermissionHandler dataAccessPermHandler;

  protected IDatasourceMgmtService datasourceMgmtSvc;

  protected IDBDatasourceService datasourceService;

  private DatabaseDialectService dialectService = new DatabaseDialectService();

  GenericDatabaseDialect genericDialect = new GenericDatabaseDialect();

  private static final Log logger = LogFactory.getLog( ConnectionServiceImpl.class );

  public Log getLogger() {
    return logger;
  }

  public ConnectionServiceImpl() {
    IPentahoSession session = PentahoSessionHolder.getSession();
    datasourceMgmtSvc = PentahoSystem.get( IDatasourceMgmtService.class, session );
    String dataAccessClassName;
    try {
      //FIXME: we should be using an object factory of some kind here
      IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
      dataAccessClassName = resLoader.getPluginSetting( getClass(),
        "settings/data-access-permission-handler", SimpleDataAccessPermissionHandler.class.getName() ); //$NON-NLS-1$
      Class<?> clazz = Class.forName( dataAccessClassName, true, getClass().getClassLoader() );
      Constructor<?> defaultConstructor = clazz.getConstructor( new Class[] {} );
      dataAccessPermHandler = (IDataAccessPermissionHandler) defaultConstructor.newInstance();
      IPentahoObjectFactory objectFactory = PentahoSystem.getObjectFactory();
      datasourceService = PentahoSystem.getObjectFactory().objectDefined( IDBDatasourceService.class )
          ?  objectFactory.get( IDBDatasourceService.class, null ) : null;
    } catch ( Exception e ) {
      logger.error(
        Messages.getErrorString( "ConnectionServiceImpl.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR", e //$NON-NLS-1$
          .getLocalizedMessage() ), e );
      // TODO: Unhardcode once this is an actual plugin
      dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
    }

  }

  public boolean hasDataAccessPermission() {
    return dataAccessPermHandler != null
      && dataAccessPermHandler.hasDataAccessPermission( PentahoSessionHolder.getSession() );
  }

  protected void ensureDataAccessPermission() throws ConnectionServiceException {
    if ( !hasDataAccessPermission() ) {
      String message = Messages.getErrorString( "ConnectionServiceImpl.ERROR_0001_PERMISSION_DENIED" ); //$NON-NLS-1$
      logger.error( message );
      throw new ConnectionServiceException( Response.SC_FORBIDDEN, message ); //$NON-NLS-1$
    }
  }

  public List<IDatabaseConnection> getConnections() throws ConnectionServiceException {
    ensureDataAccessPermission();
    List<IDatabaseConnection> connectionList = null;
    try {
      connectionList = datasourceMgmtSvc.getDatasources();
    } catch ( DatasourceMgmtServiceException dme ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0002_UNABLE_TO_GET_CONNECTION_LIST", //$NON-NLS-1$
        dme.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( message, dme );
    }
    return connectionList;
  }

  public IDatabaseConnection getConnectionByName( String name ) throws ConnectionServiceException {
    ensureDataAccessPermission();
    try {
      IDatabaseConnection connection = datasourceMgmtSvc.getDatasourceByName( name );
      if ( connection == null ) {
        throw new ConnectionServiceException( Response.SC_NOT_FOUND, Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", name ) ); //$NON-NLS-1$
      } else {
        return connection;
      }
    } catch ( DatasourceMgmtServiceException dme ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", //$NON-NLS-1$
        dme.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( message, dme );
    }
  }

  public IDatabaseConnection getConnectionById( String id ) throws ConnectionServiceException {
    ensureDataAccessPermission();
    try {
      IDatabaseConnection connection = datasourceMgmtSvc.getDatasourceById( id );
      if ( connection == null ) {
        throw new ConnectionServiceException( Response.SC_NOT_FOUND, Messages.getErrorString(
          "ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", id ) ); //$NON-NLS-1$
      } else {
        return connection;
      }
    } catch ( DatasourceMgmtServiceException dme ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", //$NON-NLS-1$
        dme.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( message, dme );
    }
  }

  public boolean addConnection( IDatabaseConnection connection ) throws ConnectionServiceException {
    ensureDataAccessPermission();
    try {
      datasourceMgmtSvc.createDatasource( connection );
      return true;
    } catch ( DuplicateDatasourceException duplicateDatasourceException ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0004_UNABLE_TO_ADD_CONNECTION", //$NON-NLS-1$
        connection.getName(),
        duplicateDatasourceException.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( Response.SC_CONFLICT, message, duplicateDatasourceException );
    } catch ( Exception e ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0004_UNABLE_TO_ADD_CONNECTION", //$NON-NLS-1$
        connection.getName(),
        e.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( message, e );
    }
  }

  protected String getConnectionPassword( String name, String password ) throws ConnectionServiceException {
    return ConnectionServiceHelper.getConnectionPassword( name, password );
  }

  public boolean updateConnection( IDatabaseConnection connection ) throws ConnectionServiceException {
    ensureDataAccessPermission();
    try {
      connection.setPassword( getConnectionPassword( connection.getName(), connection
        .getPassword() ) );
      datasourceMgmtSvc.updateDatasourceByName( connection.getName(), connection );
      clearDatasource( connection.getName() );
      return true;
    } catch ( NonExistingDatasourceException nonExistingDatasourceException ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION", //$NON-NLS-1$
        connection.getName(),
        nonExistingDatasourceException.getLocalizedMessage()
      );
      throw new ConnectionServiceException( Response.SC_NOT_FOUND, message, nonExistingDatasourceException );
    } catch ( Exception e ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0005_UNABLE_TO_UPDATE_CONNECTION", //$NON-NLS-1$
        connection.getName(),
        e.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( message, e );
    }
  }

  public boolean deleteConnection( IDatabaseConnection connection ) throws ConnectionServiceException {
    ensureDataAccessPermission();
    try {
      datasourceMgmtSvc.deleteDatasourceByName( connection.getName() );
      clearDatasource( connection.getName() );
      return true;
    } catch ( NonExistingDatasourceException nonExistingDatasourceException ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", //$NON-NLS-1$
        connection.getName(),
        nonExistingDatasourceException.getLocalizedMessage()
      );
      throw new ConnectionServiceException( Response.SC_NOT_FOUND, message, nonExistingDatasourceException );
    } catch ( Exception e ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", //$NON-NLS-1$
        connection.getName(),
        e.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( message, e );
    }
  }

  public boolean deleteConnection( String name ) throws ConnectionServiceException {
    ensureDataAccessPermission();
    try {
      datasourceMgmtSvc.deleteDatasourceByName( name );
      clearDatasource( name );
      return true;
    } catch ( NonExistingDatasourceException nonExistingDatasourceException ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", //$NON-NLS-1$
        name,
        nonExistingDatasourceException.getLocalizedMessage()
      );
      throw new ConnectionServiceException( Response.SC_NOT_FOUND, message, nonExistingDatasourceException );
    } catch ( Exception e ) {
      String message = Messages.getErrorString(
        "ConnectionServiceImpl.ERROR_0006_UNABLE_TO_DELETE_CONNECTION", //$NON-NLS-1$
        name,
        e.getLocalizedMessage()
      );
      logger.error( message );
      throw new ConnectionServiceException( message, e );
    }
  }

  public boolean testConnection( IDatabaseConnection connection ) throws ConnectionServiceException {
    ensureDataAccessPermission();
    if ( connection != null ) {
      if ( connection.getPassword() == null ) { // Can have an empty password but not a null one
        connection.setPassword( "" ); //$NON-NLS-1$
      }
      IDatabaseDialect dialect = dialectService.getDialect( connection );
      String driverClass = null;

      if ( connection.getDatabaseType().getShortName().equals( "GENERIC" ) ) {
        driverClass = connection.getAttributes().get( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS );
      } else {
        driverClass = dialect.getNativeDriver();
      }
      IPentahoConnection pentahoConnection = null;
      try {

        if ( connection.getAccessType().equals( DatabaseAccessType.JNDI ) ) {

          pentahoConnection = PentahoConnectionFactory
            .getConnection( IPentahoConnection.SQL_DATASOURCE, connection.getDatabaseName(), null, this );

        } else {

          pentahoConnection = PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE, driverClass,
            dialect.getURLWithExtraOptions( connection ), connection.getUsername(),
            getConnectionPassword( connection.getName(), connection.getPassword() ), null, this );

        }

      } catch ( DatabaseDialectException e ) {
        throw new ConnectionServiceException( e );
      }
      if ( pentahoConnection != null ) {
        // make sure we have a native connection behind the SQLConnection object
        // if the native connection is null, the test of the connection failed
        boolean testedOk = ( (SQLConnection) pentahoConnection ).getNativeConnection() != null;
        pentahoConnection.close();
        return testedOk;
      } else {
        return false;
      }
    } else {
      String message =
        Messages.getErrorString( "ConnectionServiceImpl.ERROR_0008_UNABLE_TO_TEST_NULL_CONNECTION" ); //$NON-NLS-1$
      logger.error( message );
      throw new ConnectionServiceException( Response.SC_BAD_REQUEST, message ); //$NON-NLS-1$
    }
  }

  public boolean isConnectionExist( String connectionName )
    throws ConnectionServiceException {
    ensureDataAccessPermission();

    try {
      IDatabaseConnection connection = datasourceMgmtSvc
        .getDatasourceByName( connectionName );
      if ( connection == null ) {
        return false;
      }
      return true;

    } catch ( DatasourceMgmtServiceException dme ) {
      String message = Messages
        .getErrorString(
          "ConnectionServiceImpl.ERROR_0003_UNABLE_TO_GET_CONNECTION", //$NON-NLS-1$
          dme.getLocalizedMessage() );
      logger.error( message );
      throw new ConnectionServiceException( message, dme );
    }

  }

  private void clearDatasource( String name ) {
    if ( datasourceService == null ) {
      logger.warn( "IDBDatasourceService bean not initialized. "
          + "Unable to clear data source:  " + name );
      return;
    }
    datasourceService.clearDataSource( name );
  }

}
