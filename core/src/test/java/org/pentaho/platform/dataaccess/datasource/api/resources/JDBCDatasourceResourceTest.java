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
 * Copyright (c) 2002-2018 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api.resources;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@PowerMockIgnore( "jdk.internal.reflect.*" )
@RunWith( PowerMockRunner.class )
@PrepareForTest( { JDBCDatasourceResource.class, DatasourceService.class, LogFactory.class } )
public class JDBCDatasourceResourceTest {

  private JDBCDatasourceResource resource;
  private ConnectionServiceImpl service;
  private IDatabaseConnection connection;
  private static Log logger = mock( Log.class );

  @BeforeClass
  public static void once() {
    mockStatic( LogFactory.class );
    when( LogFactory.getLog( JDBCDatasourceResource.class ) ).thenReturn( logger );
  }

  @Before
  public void setup() throws Exception {
    mockStatic( DatasourceService.class );

    service = mock( ConnectionServiceImpl.class );
    whenNew( ConnectionServiceImpl.class ).withAnyArguments().thenReturn( service );

    resource = spy( new JDBCDatasourceResource() );

    connection = new DatabaseConnection();
    connection.setName( "Name" );
    connection.setPassword( "Password!" );
  }

  @Test
  public void testDeleteConnection() throws Exception {
    doReturn( true ).when( service ).deleteConnection( "Name" );

    assertEquals( Response.Status.NO_CONTENT.getStatusCode(), resource.deleteConnection( "Name" ).getStatus() );
    verify( service, times( 1 ) ).deleteConnection(  "Name" );
  }

  @Test
  public void testDeleteConnectionNotModified() throws Exception {
    doReturn( false ).when( service ).deleteConnection( "Name" );

    assertEquals( Response.Status.NOT_MODIFIED.getStatusCode(), resource.deleteConnection( "Name" ).getStatus() );
    verify( service, times( 1 ) ).deleteConnection(  "Name" );
  }

  @Test
  public void testDeleteConnectionServerError() throws Exception {
    doThrow( mock( RuntimeException.class ) ).when( service ).deleteConnection( "Name" );

    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resource.deleteConnection( "Name" ).getStatus() );
    verify( service, times( 1 ) ).deleteConnection(  "Name" );
  }

  @Test
  public void testGetConnectionIDs() throws Exception {
    doReturn( Arrays.asList( connection ) ).when( service ).getConnections();

    assertEquals( "Name", resource.getConnectionIDs().getList().get( 0 ) );
    verify( service, times( 1 ) ).getConnections();
  }

  @Test( expected = WebApplicationException.class )
  public void testGetConnectionIDsError() throws Exception {
    doThrow( mock( ConnectionServiceException.class ) ).when( service ).getConnections();
    resource.getConnectionIDs();
  }

  @Test
  public void testGetConnection() throws Exception {
    doReturn( connection ).when( service ).getConnectionByName( "Name" );

    Response response = resource.getConnection( "Name" );

    assertEquals( "Name", ( (IDatabaseConnection) response.getEntity() ).getName() );
    assertNull( ( (IDatabaseConnection) response.getEntity() ).getPassword() ); // confirm that the password is never returned
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    verify( service, times( 1 ) ).getConnectionByName( "Name" );
  }

  @Test
  public void testGetConnectionError() throws Exception {
    doThrow( mock( ConnectionServiceException.class ) ).when( service ).getConnectionByName( "Name" );

    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resource.getConnection( "Name" ).getStatus() );
    verify( service, times( 1 ) ).getConnectionByName( "Name" );
  }

  @Test
  public void testAdd() throws Exception {
    doThrow( mock( NullPointerException.class ) ).when( service ).getConnectionByName( "Name" );
    doReturn( true ).when( service ).addConnection( connection );

    Response response = resource.addOrUpdate( "Name", (DatabaseConnection) connection );

    assertEquals( Response.Status.NO_CONTENT.getStatusCode(), response.getStatus() );
    verify( service, times( 1 ) ).addConnection( connection );
  }

  @Test
  public void testAddNotModified() throws Exception {
    doThrow( mock( ConnectionServiceException.class ) ).when( service ).getConnectionByName( "Name" );
    doReturn( false ).when( service ).addConnection( any( DatabaseConnection.class ) );

    Response response = resource.addOrUpdate( "Name", (DatabaseConnection) connection );

    assertEquals( Response.Status.NOT_MODIFIED.getStatusCode(), response.getStatus() );
    verify( service, times( 1 ) ).addConnection( connection );
  }

  @Test
  public void testAddServerError() throws Exception {
    doThrow( mock( RuntimeException.class ) ).when( service ).addConnection( connection );

    Response response = resource.addOrUpdate( "Name", (DatabaseConnection) connection );

    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    verify( service, times( 1 ) ).addConnection( connection );
  }

  @Test
  public void testUpdate() throws Exception {
    connection.setPassword( null );

    IDatabaseConnection saved = mock( DatabaseConnection.class );
    doReturn( "***" ).when( saved ).getPassword();

    doReturn( saved ).when( service ).getConnectionByName( "Name" );
    doReturn( true ).when( service ).updateConnection( connection );

    Response response = resource.addOrUpdate( "Name", (DatabaseConnection) connection );

    assertEquals( Response.Status.NO_CONTENT.getStatusCode(), response.getStatus() );
    verify( service, times( 1 ) ).updateConnection( connection );
  }

  @Test
  public void testUpdateNotModified() throws Exception {
    doReturn( mock( DatabaseConnection.class ) ).when( service ).getConnectionByName( "Name" );
    doReturn( false ).when( service ).updateConnection( connection );

    Response response = resource.addOrUpdate( "Name", (DatabaseConnection) connection );

    assertEquals( Response.Status.NOT_MODIFIED.getStatusCode(), response.getStatus() );
    verify( service, times( 1 ) ).updateConnection( connection );
  }

  @Test
  public void testUpdateServerError() throws Exception {
    doReturn( mock( DatabaseConnection.class ) ).when( service ).getConnectionByName( "Name" );
    doThrow( mock( RuntimeException.class ) ).when( service ).updateConnection( connection );

    Response response = resource.addOrUpdate( "Name", (DatabaseConnection) connection );

    assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    verify( service, times( 1 ) ).updateConnection( connection );
  }

  @Test
  public void testAddOrUpdateNoPublishPermission() throws Exception {
    PowerMockito.doThrow( new PentahoAccessControlException() ).when( DatasourceService.class, "validateAccess" );

    Response response = resource.addOrUpdate( "Name", (DatabaseConnection) connection );

    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus() );
    verify( service, never() ).addConnection( any() );
    verify( service, never() ).updateConnection( any() );
  }
}
