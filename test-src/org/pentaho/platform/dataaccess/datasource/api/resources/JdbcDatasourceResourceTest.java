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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceImpl;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class JdbcDatasourceResourceTest {

  private static JDBCDatasourceResource jdbcDatasourceResource;

  @Before
  public void setUp() {
    jdbcDatasourceResource = spy( new JDBCDatasourceResource() );
    jdbcDatasourceResource.service = mock( ConnectionServiceImpl.class );
  }

  @After
  public void cleanup() {
    jdbcDatasourceResource = null;
  }

  @Test
  public void testDeleteConnection() throws Exception {
    Response mockResponse = mock( Response.class );

    doReturn( true ).when( jdbcDatasourceResource.service ).deleteConnection( "Name" );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildOkResponse();

    Response response = jdbcDatasourceResource.deleteConnection( "Name" );

    verify( jdbcDatasourceResource, times( 1 ) ).deleteConnection(  "Name" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testDeleteConnectionError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    doReturn( false ).when( jdbcDatasourceResource.service ).deleteConnection( "Name" );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildNotModifiedResponse();

    Response response = jdbcDatasourceResource.deleteConnection( "Name" );
    assertEquals( mockResponse, response );

    //Test 2
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( jdbcDatasourceResource.service ).deleteConnection( "Name" );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildServerErrorResponse();

    response = jdbcDatasourceResource.deleteConnection( "Name" );
    assertEquals( mockResponse, response );

    verify( jdbcDatasourceResource, times( 2 ) ).deleteConnection(  "Name" );
  }

  @Test
  public void testGetConnectionIDs() throws Exception {
    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );
    List<IDatabaseConnection> conns = new ArrayList<IDatabaseConnection>();
    conns.add( mockIDatabaseConnection );
    List<String> connStrList = new ArrayList<String>();
    connStrList.add( conns.get( 0 ).getName() );
    doReturn( conns ).when( jdbcDatasourceResource.service ).getConnections();

    JaxbList<String> connections = jdbcDatasourceResource.getConnectionIDs();

    verify( jdbcDatasourceResource, times( 1 ) ).getConnectionIDs();
    assertEquals( connections.getList().get( 0 ), connStrList.get( 0 ) );
  }

  @Test
  public void testGetConnectionIDsError() throws Exception {
    ConnectionServiceException mockConnectionServiceException = mock( ConnectionServiceException.class );
    doThrow( mockConnectionServiceException ).when( jdbcDatasourceResource.service ).getConnections();

    try {
      JaxbList<String> connections = jdbcDatasourceResource.getConnectionIDs();
      fail( "Should get WebApplicationException" );
    } catch ( WebApplicationException e ) {
      // good
    }
  }

  @Test
  public void testGetConnection() throws Exception {
    IDatabaseConnection mockIDatabaseConnection = mock( IDatabaseConnection.class );
    doReturn( mockIDatabaseConnection ).when( jdbcDatasourceResource.service ).getConnectionByName( "Name" );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildOkResponse( mockIDatabaseConnection );

    Response response = jdbcDatasourceResource.getConnection( "Name" );

    verify( jdbcDatasourceResource, times( 1 ) ).getConnection( "Name" );
    assertEquals( response, mockResponse );
  }

  @Test
  public void testGetConnectionError() throws Exception {
    ConnectionServiceException mockConnectionServiceException = mock( ConnectionServiceException.class );
    doThrow( mockConnectionServiceException ).when( jdbcDatasourceResource.service ).getConnectionByName( "Name" );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildServerErrorResponse();

    Response response = jdbcDatasourceResource.getConnection( "Name" );

    verify( jdbcDatasourceResource, times( 1 ) ).getConnection( "Name" );
    assertEquals( response, mockResponse );
  }

  @Test
  public void testAdd() throws Exception {
    doNothing().when( jdbcDatasourceResource ).validateAccess();

    DatabaseConnection mockDatabaseConnection = mock( DatabaseConnection.class );
    doReturn( true ).when( jdbcDatasourceResource.service ).addConnection( mockDatabaseConnection );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildOkResponse();

    Response response = jdbcDatasourceResource.addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );

    verify( jdbcDatasourceResource, times( 1 ) ).addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
    assertEquals( response, mockResponse );
  }

  @Test
  public void testAddError() throws Exception {
    doNothing().when( jdbcDatasourceResource ).validateAccess();
    DatabaseConnection mockDatabaseConnection = mock( DatabaseConnection.class );

    //Test 1
    doReturn( false ).when( jdbcDatasourceResource.service ).addConnection( mockDatabaseConnection );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildNotModifiedResponse();

    Response response = jdbcDatasourceResource.addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
    assertEquals( response, mockResponse );

    //Test 2
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( jdbcDatasourceResource.service ).addConnection( mockDatabaseConnection );

    mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildServerErrorResponse();

    response = jdbcDatasourceResource.addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
    assertEquals( response, mockResponse );

    verify( jdbcDatasourceResource, times( 2 ) ).addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
  }

  @Test
  public void testUpdate() throws Exception {
    DatabaseConnection mockDatabaseConnection = mock( DatabaseConnection.class );
    doReturn( "" ).when( mockDatabaseConnection ).getPassword();
    doReturn( "id" ).when( mockDatabaseConnection ).getName();
    IDatabaseConnection mockSavedConn = mock( IDatabaseConnection.class );
    doReturn( mockSavedConn ).when( jdbcDatasourceResource.service ).getConnectionByName( "id" );
    doReturn( "password" ).when( mockSavedConn ).getPassword();

    doReturn( true ).when( jdbcDatasourceResource.service ).updateConnection( mockDatabaseConnection );
    doNothing().when( jdbcDatasourceResource ).validateAccess();


    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildOkResponse();

    Response response = jdbcDatasourceResource.addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );

    verify( jdbcDatasourceResource, times( 1 ) ).addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
    assertEquals( response, mockResponse );
  }

  @Test
  public void testUpdateError() throws Exception {
    DatabaseConnection mockDatabaseConnection = mock( DatabaseConnection.class );
    doReturn( "" ).when( mockDatabaseConnection ).getPassword();
    doReturn( "id" ).when( mockDatabaseConnection ).getId();
    IDatabaseConnection mockSavedConn = mock( IDatabaseConnection.class );
    doReturn( mockSavedConn ).when( jdbcDatasourceResource.service ).getConnectionById( "id" );
    doReturn( "password" ).when( mockSavedConn ).getPassword();

    //Test 1
    doReturn( false ).when( jdbcDatasourceResource.service ).updateConnection( mockDatabaseConnection );

    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildNotModifiedResponse();

    Response response = jdbcDatasourceResource.addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
    assertEquals( 500, response.getStatus() );

    //Test 2
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( jdbcDatasourceResource.service ).updateConnection( mockDatabaseConnection );

    doReturn( mockResponse ).when( jdbcDatasourceResource ).buildServerErrorResponse();

    response = jdbcDatasourceResource.addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
    assertEquals( response, mockResponse );

    verify( jdbcDatasourceResource, times( 2 ) ).addOrUpdate( mockDatabaseConnection.getName(), mockDatabaseConnection );
  }
}
