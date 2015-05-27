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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

public class ConnectionServiceImplTest {

  private static ConnectionServiceImpl connectionServiceImpl;

  @Mock private IDBDatasourceService datasourceService;

  @Mock private IDatasourceMgmtService datasourceMgmtService;


  @Before
  public void setUp() throws ConnectionServiceException {
    MockitoAnnotations.initMocks( this );
    connectionServiceImpl = spy( new ConnectionServiceImpl() );
    connectionServiceImpl.datasourceMgmtSvc = datasourceMgmtService;
    connectionServiceImpl.datasourceService = datasourceService;
  }

  @After
  public void cleanup() {
    connectionServiceImpl = null;
  }

  @Test
  public void testDeleteConnection() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    doNothing().when( connectionServiceImpl.datasourceMgmtSvc ).deleteDatasourceByName( "Test Connection" );

    boolean tmp = connectionServiceImpl.deleteConnection( "Test Connection" );

    verify( connectionServiceImpl, times( 1 ) ).deleteConnection( "Test Connection" );
    verify( datasourceService, times( 1 ) ).clearDataSource( "Test Connection" );
    assertEquals( tmp, true );
  }

  @Test
  public void testDeleteConnectionError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    //Test 1
    NonExistingDatasourceException mockNonExistingDatasourceException = mock( NonExistingDatasourceException.class );
    doThrow( mockNonExistingDatasourceException ).when(
      connectionServiceImpl.datasourceMgmtSvc ).deleteDatasourceByName( "Test Connection" );

    try {
      connectionServiceImpl.deleteConnection( "Test Connection" );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    //Test 2
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( connectionServiceImpl.datasourceMgmtSvc ).deleteDatasourceByName( "Test Connection" );

    try {
      connectionServiceImpl.deleteConnection( "Test Connection" );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    verify( connectionServiceImpl, times( 2 ) ).deleteConnection( anyString() );
  }

  @Test
  public void testGetConnections() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    List<IDatabaseConnection> mockConnectionList = mock( List.class );
    doReturn( mockConnectionList ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasources();

    List<IDatabaseConnection> connectionList = connectionServiceImpl.getConnections();

    verify( connectionServiceImpl, times( 1 ) ).getConnections();
    assertEquals( connectionList, mockConnectionList );
  }

  @Test
  public void testGetConnectionsError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    //Test 1
    DatasourceMgmtServiceException mockDatasourceMgmtServiceException = mock( DatasourceMgmtServiceException.class );
    doThrow( mockDatasourceMgmtServiceException ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasources();

    try {
      connectionServiceImpl.getConnections();
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    verify( connectionServiceImpl, times( 1 ) ).getConnections();
  }

  @Test
  public void testGetConnectionById() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );
    doReturn( mockIDatabaseConnection ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceById( "Connection Id" );

    IDatabaseConnection connection = connectionServiceImpl.getConnectionById( "Connection Id" );

    verify( connectionServiceImpl, times( 1 ) ).getConnectionById( "Connection Id" );
    assertEquals( mockIDatabaseConnection, connection );
  }

  @Test
  public void testGetConnectionByIdError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    //Test 1
    doReturn( null ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceById( "Connection Id" );

    try {
      connectionServiceImpl.getConnectionById( "Connection Id" );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    //Test 2
    DatasourceMgmtServiceException mockDatasourceMgmtServiceException = mock( DatasourceMgmtServiceException.class );
    doThrow( mockDatasourceMgmtServiceException ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceById( "Connection Id" );

    try {
      connectionServiceImpl.getConnectionById( "Connection Id" );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    verify( connectionServiceImpl, times( 2 ) ).getConnectionById( "Connection Id" );
  }

  @Test
  public void testGetConnectionByName() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );
    doReturn( mockIDatabaseConnection ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( "Connection Name" );

    IDatabaseConnection connection = connectionServiceImpl.getConnectionByName( "Connection Name" );

    verify( connectionServiceImpl, times( 1 ) ).getConnectionByName( "Connection Name" );
    assertEquals( mockIDatabaseConnection, connection );
  }

  @Test
  public void testGetConnectionByNameError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    //Test 1
    doReturn( null ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( "Connection Name" );

    try {
      connectionServiceImpl.getConnectionByName( "Connection Name" );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    //Test 2
    DatasourceMgmtServiceException mockDatasourceMgmtServiceException = mock( DatasourceMgmtServiceException.class );
    doThrow( mockDatasourceMgmtServiceException ).when( connectionServiceImpl.datasourceMgmtSvc ).getDatasourceByName( "Connection Name" );

    try {
      connectionServiceImpl.getConnectionByName( "Connection Name" );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    verify( connectionServiceImpl, times( 2 ) ).getConnectionByName( "Connection Name" );
  }

  @Test
  public void testAddConnection() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );
    boolean connectionCreated = connectionServiceImpl.addConnection( mockIDatabaseConnection );

    verify( connectionServiceImpl, times( 1 ) ).addConnection( mockIDatabaseConnection );
    assertEquals( connectionCreated, true );
  }

  @Test
  public void testAddConnectionError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();
    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );

    //Test 1
    DuplicateDatasourceException mockDuplicateDatasourceException = mock( DuplicateDatasourceException.class );
    doThrow( mockDuplicateDatasourceException ).when( connectionServiceImpl.datasourceMgmtSvc ).createDatasource(
        mockIDatabaseConnection );

    try {
      connectionServiceImpl.addConnection( mockIDatabaseConnection );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    //Test 2
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( connectionServiceImpl.datasourceMgmtSvc ).createDatasource(
        mockIDatabaseConnection );

    try {
      connectionServiceImpl.addConnection( mockIDatabaseConnection );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    verify( connectionServiceImpl, times( 2 ) ).addConnection( mockIDatabaseConnection );
  }

  @Test
  public void testUpdateConnection() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );
    doNothing().when( mockIDatabaseConnection ).setPassword( anyString() );
    doReturn( "DB Name" ).when( mockIDatabaseConnection ).getName();
    doReturn( "" ).when( connectionServiceImpl ).getConnectionPassword( anyString(), anyString() );

    boolean connectionUpdated = connectionServiceImpl.updateConnection( mockIDatabaseConnection );

    verify( connectionServiceImpl, times( 1 ) ).updateConnection( mockIDatabaseConnection );
    verify( datasourceService, times( 1 ) ).clearDataSource( "DB Name" );
    assertEquals( connectionUpdated, true );
  }

  @Test
  public void testUpdateConnectionError() throws Exception {
    doNothing().when( connectionServiceImpl ).ensureDataAccessPermission();

    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );
    doNothing().when( mockIDatabaseConnection ).setPassword( anyString() );
    doReturn( "Name" ).when( mockIDatabaseConnection ).getName();
    doReturn( "" ).when( connectionServiceImpl ).getConnectionPassword( anyString(), anyString() );

    //Test 1
    NonExistingDatasourceException mockNonExistingDatasourceException = mock( NonExistingDatasourceException.class );
    doThrow( mockNonExistingDatasourceException ).when( connectionServiceImpl.datasourceMgmtSvc ).updateDatasourceByName( "Name", mockIDatabaseConnection );

    try {
      connectionServiceImpl.updateConnection( mockIDatabaseConnection );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    //Test 2
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( connectionServiceImpl.datasourceMgmtSvc ).updateDatasourceByName( "Name", mockIDatabaseConnection );

    try {
      connectionServiceImpl.updateConnection( mockIDatabaseConnection );
      fail(); //This line should never be reached
    } catch ( ConnectionServiceException e ) {
      //Expected exception
    }

    verify( connectionServiceImpl, times( 2 ) ).updateConnection( mockIDatabaseConnection );
  }
}
