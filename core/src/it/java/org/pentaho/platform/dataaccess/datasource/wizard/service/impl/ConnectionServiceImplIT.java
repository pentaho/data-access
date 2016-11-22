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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.MockDataSourceService;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.security.core.context.SecurityContextHolder;

public class ConnectionServiceImplIT {

  private static final String EXIST_CONNECTION_NAME = "ExistConnection";

  private static final String EXIST_CONNECTION_ID = "ExistConnectionID";

  private static final String NON_EXIST_CONNECTION_NAME = "NonExistConnection";

  private static final String NON_EXIST_CONNECTION_ID = "NonExistConnectionID";

  private static final String ERROR_CONNECTION_NAME = "ErrorConnection";

  private static final String ERROR_CONNECTION_ID = "ErrorConnectionID";

  private static final String DUBLICATE_CONNECTION_NAME = "DublicateConnection";

  private static final String SECOND_CONNECTION = "SecondConnection";

  private static final String PASSWORD = "password";

  private static final String NEW_PASSWORD = "new_password";

  private static ConnectionServiceImpl connectionServiceImpl;

  private static MicroPlatform booter;

  private static MockDatasourceMgmtService mgmtService = new MockDatasourceMgmtService();

  @BeforeClass
  public static void setUpClass() throws PlatformInitializationException, DuplicateDatasourceException,
    DatasourceMgmtServiceException {

    MockDataSourceService dataSourceService = new MockDataSourceService( false );

    IAuthorizationPolicy mockAuthorizationPolicy = mock( IAuthorizationPolicy.class );
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );

    booter = new MicroPlatform( "target/test-classes/solution1" );
    booter.define( IPentahoConnection.class, SQLConnection.class );
    booter.defineInstance( IDBDatasourceService.class, dataSourceService );
    booter.defineInstance( IAuthorizationPolicy.class, mockAuthorizationPolicy );
    booter.defineInstance( IPluginResourceLoader.class, new PluginResourceLoader() {
      protected PluginClassLoader getOverrideClassloader() {
        return new PluginClassLoader( new File( ".", "target/test-classes/solution1/system/simple-jndi" ), this );
      }
    } );
    booter.defineInstance( IDatasourceMgmtService.class, mgmtService );
    booter.start();

    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );

    connectionServiceImpl = new ConnectionServiceImpl();
  }

  @Before
  public void setUp() throws DuplicateDatasourceException, DatasourceMgmtServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( EXIST_CONNECTION_NAME );
    connection.setId( EXIST_CONNECTION_ID );
    connection.setPassword( PASSWORD );
    mgmtService.createDatasource( connection );
  }

  @After
  public void tearDown() throws DatasourceMgmtServiceException {
    for ( Iterator<IDatabaseConnection> iterator = mgmtService.getDatasources().iterator(); iterator.hasNext(); ) {
      iterator.next();
      iterator.remove();
    }
  }

  @AfterClass
  public static void tearDownClass() {
    booter.stop();
  }

  @Test
  public void testDeleteConnectionByName() throws Exception {
    assertTrue( connectionServiceImpl.deleteConnection( EXIST_CONNECTION_NAME ) );
    for ( Iterator<IDatabaseConnection> iterator = connectionServiceImpl.getConnections().iterator(); iterator
        .hasNext(); ) {
      if ( iterator.next().getName().equals( EXIST_CONNECTION_NAME ) ) {
        fail( "connection should be deleted" );
      }
    }
    ;
  }

  @Test( expected = ConnectionServiceException.class )
  public void testDeleteConnectionByNameNONExist() throws Exception {
    connectionServiceImpl.deleteConnection( NON_EXIST_CONNECTION_NAME );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testDeleteConnectionByNameError() throws Exception {
    connectionServiceImpl.deleteConnection( ERROR_CONNECTION_NAME );
  }

  // we should remove connection if it is have the same name
  @Test
  public void testDeleteConnection() throws Exception {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( EXIST_CONNECTION_NAME );

    assertTrue( connectionServiceImpl.deleteConnection( connection ) );
    for ( Iterator<IDatabaseConnection> iterator = connectionServiceImpl.getConnections().iterator(); iterator
        .hasNext(); ) {
      if ( iterator.next().getName().equals( EXIST_CONNECTION_NAME ) ) {
        fail( "connection should be deleted" );
      }
    }
    ;
  }

  @Test( expected = ConnectionServiceException.class )
  public void testDeleteConnectionNONExist() throws Exception {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( NON_EXIST_CONNECTION_NAME );
    connectionServiceImpl.deleteConnection( connection );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testDeleteConnectionError() throws Exception {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( ERROR_CONNECTION_NAME );
    connectionServiceImpl.deleteConnection( connection );
  }

  @Test
  public void testGetConnectionByName() throws ConnectionServiceException {
    IDatabaseConnection connection = connectionServiceImpl.getConnectionByName( EXIST_CONNECTION_NAME );
    assertNotNull( connection );
    assertEquals( EXIST_CONNECTION_NAME, connection.getName() );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testGetConnectionByNameNONEXIST() throws ConnectionServiceException {
    connectionServiceImpl.getConnectionByName( NON_EXIST_CONNECTION_NAME );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testGetConnectionByNameError() throws ConnectionServiceException {
    connectionServiceImpl.getConnectionByName( ERROR_CONNECTION_NAME );
  }

  @Test
  public void testGetConnectionByID() throws ConnectionServiceException {
    IDatabaseConnection connection = connectionServiceImpl.getConnectionById( EXIST_CONNECTION_ID );
    assertNotNull( connection );
    assertEquals( EXIST_CONNECTION_NAME, connection.getName() );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testGetConnectionByIDNONEXIST() throws ConnectionServiceException {
    connectionServiceImpl.getConnectionById( NON_EXIST_CONNECTION_ID );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testGetConnectionByIDError() throws ConnectionServiceException {
    connectionServiceImpl.getConnectionById( ERROR_CONNECTION_ID );
  }

  @Test
  public void testIsConnectionExist() throws ConnectionServiceException {
    assertTrue( connectionServiceImpl.isConnectionExist( EXIST_CONNECTION_NAME ) );
  }

  @Test
  public void testIsConnectionExistFalse() throws ConnectionServiceException {
    assertFalse( connectionServiceImpl.isConnectionExist( NON_EXIST_CONNECTION_NAME ) );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testIsConnectionExistError() throws ConnectionServiceException {
    connectionServiceImpl.isConnectionExist( ERROR_CONNECTION_NAME );
  }

  @Test
  public void testAddConnection() throws ConnectionServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( SECOND_CONNECTION );
    connectionServiceImpl.addConnection( connection );
    IDatabaseConnection actualCnnection = connectionServiceImpl.getConnectionByName( SECOND_CONNECTION );
    assertEquals( connection, actualCnnection );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testAddConnectionError() throws ConnectionServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( ERROR_CONNECTION_NAME );
    connectionServiceImpl.addConnection( connection );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testAddConnectionDublicate() throws ConnectionServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( DUBLICATE_CONNECTION_NAME );
    connectionServiceImpl.addConnection( connection );
  }

  @Test
  public void testUpdateConnection() throws ConnectionServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( EXIST_CONNECTION_NAME );
    connection.setPassword( NEW_PASSWORD );
    connectionServiceImpl.updateConnection( connection );
    IDatabaseConnection actualConnection = connectionServiceImpl.getConnectionByName( EXIST_CONNECTION_NAME );
    assertEquals( NEW_PASSWORD, actualConnection.getPassword() );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testUpdateConnectionError() throws ConnectionServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( ERROR_CONNECTION_NAME );
    connectionServiceImpl.updateConnection( connection );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testUpdateConnectionNonExist() throws ConnectionServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( NON_EXIST_CONNECTION_NAME );
    connectionServiceImpl.updateConnection( connection );
  }

  @Test
  public void testTestConnection() throws ConnectionServiceException {
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( NON_EXIST_CONNECTION_NAME );
    connection.setAccessType( DatabaseAccessType.NATIVE );
    connection.setDatabaseType( new DatabaseType( "H2", "H2", Arrays.asList( DatabaseAccessType.NATIVE ), 0, null ) );
    assertTrue( connectionServiceImpl.testConnection( connection ) );
  }

  public static class MockDatasourceMgmtService implements IDatasourceMgmtService {

    private List<IDatabaseConnection> connections = new ArrayList<IDatabaseConnection>();

    @Override
    public void init( IPentahoSession arg0 ) {
    }

    @Override
    public String createDatasource( IDatabaseConnection connection ) throws DuplicateDatasourceException,
      DatasourceMgmtServiceException {
      if ( connection.getName().equals( ERROR_CONNECTION_NAME ) ) {
        throw new DatasourceMgmtServiceException();
      }
      if ( connection.getName().equals( DUBLICATE_CONNECTION_NAME ) ) {
        throw new DuplicateDatasourceException();
      }
      connections.add( connection );
      return connection.getDatabaseName();
    }

    @Override
    public void deleteDatasourceById( String arg0 ) throws NonExistingDatasourceException,
      DatasourceMgmtServiceException {
    }

    @Override
    public void deleteDatasourceByName( String connectionName ) throws NonExistingDatasourceException,
      DatasourceMgmtServiceException {
      for ( Iterator<IDatabaseConnection> iterator = connections.iterator(); iterator.hasNext(); ) {
        IDatabaseConnection connection = (IDatabaseConnection) iterator.next();
        if ( connection.getName().equals( connectionName ) ) {
          iterator.remove();
        }
        ;
        if ( connectionName.equals( NON_EXIST_CONNECTION_NAME ) ) {
          throw new NonExistingDatasourceException();
        }
        if ( connectionName.equals( ERROR_CONNECTION_NAME ) ) {
          throw new DatasourceMgmtServiceException();
        }
      }
    }

    @Override
    public IDatabaseConnection getDatasourceById( String connectionID ) throws DatasourceMgmtServiceException {
      for ( Iterator<IDatabaseConnection> iterator = connections.iterator(); iterator.hasNext(); ) {
        IDatabaseConnection connection = (IDatabaseConnection) iterator.next();
        if ( connection.getId().equals( connectionID ) ) {
          return connection;
        }
        ;
        if ( connectionID.equals( ERROR_CONNECTION_ID ) ) {
          throw new DatasourceMgmtServiceException();
        }
      }
      return null;
    }

    @Override
    public IDatabaseConnection getDatasourceByName( String connectionName ) throws DatasourceMgmtServiceException {
      for ( Iterator<IDatabaseConnection> iterator = connections.iterator(); iterator.hasNext(); ) {
        IDatabaseConnection connection = (IDatabaseConnection) iterator.next();
        if ( connection.getName().equals( connectionName ) ) {
          return connection;
        }
        ;
        if ( connectionName.equals( ERROR_CONNECTION_NAME ) ) {
          throw new DatasourceMgmtServiceException();
        }
      }
      return null;
    }

    @Override
    public List<String> getDatasourceIds() throws DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public List<IDatabaseConnection> getDatasources() throws DatasourceMgmtServiceException {
      return connections;
    }

    @Override
    public String updateDatasourceById( String arg0, IDatabaseConnection arg1 ) throws NonExistingDatasourceException,
      DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public String updateDatasourceByName( String connectionName, IDatabaseConnection connection )
      throws NonExistingDatasourceException, DatasourceMgmtServiceException {
      for ( IDatabaseConnection conn : connections ) {
        if ( conn.getName().equals( connectionName ) ) {
          connections.remove( conn );
          connections.add( connection );
        }
        ;
        if ( connectionName.equals( NON_EXIST_CONNECTION_NAME ) ) {
          throw new NonExistingDatasourceException();
        }
        if ( connectionName.equals( ERROR_CONNECTION_NAME ) ) {
          throw new DatasourceMgmtServiceException();
        }
      }
      return connectionName;
    }
  }
}
