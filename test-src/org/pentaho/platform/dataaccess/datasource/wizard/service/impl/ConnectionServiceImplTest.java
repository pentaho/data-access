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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.platform.api.data.DBDatasourceServiceException;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.PooledDatasourceHelper;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import javax.sql.DataSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ConnectionServiceImplTest {

  private static final String CONN_NAME = "Connection Name";

  private static final String CONN_ID = "Connection ID";

  private static ConnectionServiceImpl connectionServiceImpl;

  private IDatabaseConnection mockDBConnection = mock( IDatabaseConnection.class );

  private IDBDatasourceService datasourceService = mock( IDBDatasourceService.class );

  private IPentahoObjectFactory pentahoObjectFactory = mock( IPentahoObjectFactory.class );

  private final IPluginResourceLoader loader = mock( IPluginResourceLoader.class );

  private IDatabaseType databaseType = mock( IDatabaseType.class );

  private final SQLConnection sqlConnection = mock( SQLConnection.class );

  private final Connection nativeConnection = mock( Connection.class );

  private final PooledDatasourceHelper pdh = mock(PooledDatasourceHelper.class);
  
  @Before
  public void setUp() throws ConnectionServiceException, ObjectFactoryException, DBDatasourceServiceException {
	doReturn( nativeConnection ).when( sqlConnection ).getNativeConnection();
    doReturn( SimpleDataAccessPermissionHandler.class.getName() ).when( loader ).getPluginSetting( this.anyClass(), anyString(), anyString() );

    when( pentahoObjectFactory.objectDefined( anyString() ) ).thenReturn( true );
    when( pentahoObjectFactory.get( this.anyClass(), anyString(), any( IPentahoSession.class ) ) ).thenAnswer(
        new Answer<Object>() {
          @Override
          public Object answer( InvocationOnMock invocation ) throws Throwable {
            if ( invocation.getArguments()[0].equals( IPluginResourceLoader.class ) ) {
              return loader;
            }
            if ( invocation.getArguments()[0].equals( IPentahoConnection.class ) ) {
              return sqlConnection;
            }
            if ( invocation.getArguments()[0].equals( IDBDatasourceService.class ) ) {
                return datasourceService;
              }
            if ( invocation.getArguments()[0].equals( PooledDatasourceHelper.class ) ) {
                return pdh;
              }
            return null;
          }
        } );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    doReturn( databaseType ).when( mockDBConnection ).getDatabaseType();
    doReturn( CONN_NAME ).when( mockDBConnection ).getName();
    doNothing().when( mockDBConnection ).setPassword( anyString() );

    connectionServiceImpl = spy( new ConnectionServiceImpl() );
    connectionServiceImpl.datasourceMgmtSvc = mock( IDatasourceMgmtService.class );
    connectionServiceImpl.dialectService = mock( DatabaseDialectService.class );
    connectionServiceImpl.datasourceService = datasourceService;
  }

  @After
  public void tearDown() {
    connectionServiceImpl = null;
    PentahoSystem.deregisterObjectFactory( pentahoObjectFactory );
    PentahoSystem.clearObjectFactory();
  }

  @Test
  public void testDeleteConnectionString() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doNothing().when( connectionServiceImpl.datasourceMgmtSvc ).deleteDatasourceByName( CONN_NAME );

    assertTrue( connectionServiceImpl.deleteConnection( CONN_NAME ) );
    verify( datasourceService ).clearDataSource( CONN_NAME );
  }

  @Test
  public void testDeleteConnectionStringError_NonExistingDatasourceException() throws Exception {
    testDeleteConnectionErrorString( mock( NonExistingDatasourceException.class ) );
  }

  @Test
  public void testDeleteConnectionStringError_RuntimeException() throws Exception {
    testDeleteConnectionErrorString( mock( RuntimeException.class ) );
  }

  private void testDeleteConnectionErrorString( Exception ex ) throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doThrow( ex ).when( connectionServiceImpl.datasourceMgmtSvc ).deleteDatasourceByName( CONN_NAME );
    try {
      connectionServiceImpl.deleteConnection( CONN_NAME );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testDeleteConnectionConn() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doNothing().when( connectionServiceImpl.datasourceMgmtSvc ).deleteDatasourceByName( CONN_NAME );

    assertTrue( connectionServiceImpl.deleteConnection( mockDBConnection ) );
    verify( datasourceService ).clearDataSource( CONN_NAME );
  }

  @Test
  public void testDeleteConnectionConnError_NonExistingDatasourceException() throws Exception {
    testDeleteConnectionErrorConn( mock( NonExistingDatasourceException.class ) );
  }

  @Test
  public void testDeleteConnectionConnError_RuntimeException() throws Exception {
    testDeleteConnectionErrorConn( mock( RuntimeException.class ) );
  }

  private void testDeleteConnectionErrorConn( Exception ex ) throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doThrow( ex ).when( connectionServiceImpl.datasourceMgmtSvc ).deleteDatasourceByName( CONN_NAME );
    try {
      connectionServiceImpl.deleteConnection( mockDBConnection );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testGetConnections() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    List<IDatabaseConnection> mockConnectionList = Collections.emptyList();
    doReturn( mockConnectionList ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasources();
    List<IDatabaseConnection> connectionList = connectionServiceImpl.getConnections();
    verify( connectionServiceImpl ).getConnections();
    assertEquals( connectionList, mockConnectionList );
  }

  @Test
  public void testGetConnectionsError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    DatasourceMgmtServiceException mockDatasourceMgmtServiceException = mock( DatasourceMgmtServiceException.class );
    doThrow( mockDatasourceMgmtServiceException ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasources();
    try {
      connectionServiceImpl.getConnections();
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testGetConnectionById() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( mockDBConnection ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceById( CONN_ID );
    IDatabaseConnection connection = connectionServiceImpl.getConnectionById( CONN_ID );
    verify( connectionServiceImpl ).getConnectionById( CONN_ID );
    assertEquals( mockDBConnection, connection );
  }

  @Test
  public void testGetConnectionByIdError_NullDatasource() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( null ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceById( CONN_ID );
    try {
      connectionServiceImpl.getConnectionById( CONN_ID );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testGetConnectionByIdError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    DatasourceMgmtServiceException dmsException = mock( DatasourceMgmtServiceException.class );
    doThrow( dmsException ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceById( CONN_ID );
    try {
      connectionServiceImpl.getConnectionById( CONN_ID );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testGetConnectionByName() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( mockDBConnection ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( CONN_NAME );
    IDatabaseConnection connection = connectionServiceImpl.getConnectionByName( CONN_NAME );
    verify( connectionServiceImpl ).getConnectionByName( CONN_NAME );
    assertEquals( mockDBConnection, connection );
  }

  @Test
  public void testGetConnectionByNameError_NullDataSource() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( null ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( CONN_NAME );
    try {
      connectionServiceImpl.getConnectionByName( CONN_NAME );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testGetConnectionByNameError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    DatasourceMgmtServiceException dmsException = mock( DatasourceMgmtServiceException.class );
    doThrow( dmsException ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( CONN_NAME );
    try {
      connectionServiceImpl.getConnectionByName( CONN_NAME );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testAddConnection() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    assertTrue( connectionServiceImpl.addConnection( mockDBConnection ) );
  }

  @Test
  public void testAddConnectionError_RuntimeException() throws Exception {
    testAddConnectionError( mock( RuntimeException.class ) );
  }

  @Test
  public void testAddConnectionError_DuplicateDatasourceException() throws Exception {
    testAddConnectionError( mock( DuplicateDatasourceException.class ) );
  }

  private void testAddConnectionError( Exception ex ) throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doThrow( ex ).when( connectionServiceImpl.datasourceMgmtSvc ).createDatasource( mockDBConnection );
    try {
      connectionServiceImpl.addConnection( mockDBConnection );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
  }

  @Test
  public void testUpdateConnection() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( "" ).when( connectionServiceImpl ).getConnectionPassword( anyString(), anyString() );

    assertTrue( connectionServiceImpl.updateConnection( mockDBConnection ) );

    verify( connectionServiceImpl ).updateConnection( mockDBConnection );
    verify( datasourceService ).clearDataSource( CONN_NAME );
  }

  @Test
  public void testUpdateConnectionError_RuntimeException() throws Exception {
    testUpdateConnectionError( mock( RuntimeException.class ) );
  }

  @Test
  public void testUpdateConnectionError_NonExistingDatasourceException() throws Exception {
    testUpdateConnectionError( mock( NonExistingDatasourceException.class ) );
  }

  private void testUpdateConnectionError( Exception ex ) throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( "" ).when( connectionServiceImpl ).getConnectionPassword( anyString(), anyString() );
    doThrow( ex ).when( connectionServiceImpl.datasourceMgmtSvc ).updateDatasourceByName( CONN_NAME,  mockDBConnection );
    try {
      connectionServiceImpl.updateConnection( mockDBConnection );
      fail(); // This line should never be reached
    } catch ( ConnectionServiceException e ) {
      // Expected exception
    }
    verify( connectionServiceImpl ).updateConnection( mockDBConnection );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testTestConnection_Null() throws Exception {
    testTestConnection( DatabaseAccessType.JNDI, null, "NONGENERIC", false );
  }

  @Test
  public void testTestConnection_Native() throws Exception {
    testTestConnection( DatabaseAccessType.NATIVE, mockDBConnection, "NONGENERIC", false );
  }

  @Test
  public void testTestConnection_JNDI() throws Exception {
    testTestConnection( DatabaseAccessType.JNDI, mockDBConnection, "NONGENERIC", false );
  }

  @Test
  public void testTestConnection_NativeGenericConnection() throws Exception {
    testTestConnection( DatabaseAccessType.NATIVE, mockDBConnection, "GENERIC", false );
  }
  
  @Test
   public void testTestConnection_NativeGenericConnectionPool() throws Exception {
     testTestConnection( DatabaseAccessType.NATIVE, mockDBConnection, "GENERIC", true );
  }
  
  private void testTestConnection( DatabaseAccessType accessType, IDatabaseConnection connection, String database, boolean isPool ) throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( "connectionPassword" ).when( connectionServiceImpl ).getConnectionPassword( anyString(), anyString() );

    IDatabaseDialect dialect = mock( IDatabaseDialect.class );
    doReturn( "NativeDriver" ).when( dialect ).getNativeDriver();
    doReturn( dialect ).when( connectionServiceImpl.dialectService ).getDialect( any( IDatabaseConnection.class ) );
    doReturn( database ).when( databaseType ).getShortName();
    doReturn( accessType ).when( mockDBConnection ).getAccessType();
    doReturn( "DATABASENAME" ).when( mockDBConnection ).getDatabaseName();
    doReturn( isPool ).when( mockDBConnection ).isUsingConnectionPool();
    assertTrue( connectionServiceImpl.testConnection( connection ) );
    verify( sqlConnection ).close();
  }

  @Test
  public void testIsConnectionExist_true() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doReturn( mockDBConnection ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( CONN_NAME );
    assertTrue( connectionServiceImpl.isConnectionExist( CONN_NAME ) );
  }

  @Test
  public void testIsConnectionExist_false() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    assertFalse( connectionServiceImpl.isConnectionExist( CONN_NAME ) );
  }

  @Test( expected = ConnectionServiceException.class )
  public void testIsConnectionExist_Error() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doThrow( mock( DatasourceMgmtServiceException.class ) ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( CONN_NAME );
    assertTrue( connectionServiceImpl.isConnectionExist( CONN_NAME ) );
  }

  private Class<?> anyClass() {
    return argThat( new AnyClassMatcher() );
  }

  private class AnyClassMatcher extends ArgumentMatcher<Class<?>> {

    @Override
    public boolean matches( final Object arg ) {
      return true;
    }
  }
}
