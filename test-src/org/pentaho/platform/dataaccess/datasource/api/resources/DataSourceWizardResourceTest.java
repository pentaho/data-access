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
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class DataSourceWizardResourceTest {

  private static DataSourceWizardResource dataSourceWizardResource;

  private class DataSourceWizardResourceMock extends DataSourceWizardResource {
    @Override protected DataSourceWizardService createDataSourceWizardService() {
      return mock( DataSourceWizardService.class );
    }
  }

  @Before
  public void setUp() {
    dataSourceWizardResource = spy( new DataSourceWizardResourceMock() );
  }

  @After
  public void cleanup() {
    dataSourceWizardResource = null;
  }

  @Test
  public void testDownload() throws Exception {
    Response mockResponse = mock( Response.class );
    Map<String, InputStream> mockFileData = mock( Map.class );

    doReturn( mockFileData ).when( dataSourceWizardResource.service ).doGetDSWFilesAsDownload( "dswId" );
    doReturn( mockResponse ).when( dataSourceWizardResource ).createAttachment( mockFileData, "dswId" );

    Response response = dataSourceWizardResource.downloadDsw( "dswId" );

    verify( dataSourceWizardResource, times( 1 ) ).downloadDsw( "dswId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testDownloadError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    PentahoAccessControlException mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( dataSourceWizardResource.service ).doGetDSWFilesAsDownload(
        "dswId" );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildUnauthorizedResponse();

    Response response = dataSourceWizardResource.downloadDsw( "dswId" );
    assertEquals( mockResponse, response );

    verify( dataSourceWizardResource, times( 1 ) ).downloadDsw( "dswId" );
  }

  @Test
  public void testRemove() throws Exception {
    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildOkResponse();

    Response response = dataSourceWizardResource.remove( "dswId" );

    verify( dataSourceWizardResource, times( 1 ) ).remove( "dswId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testRemoveError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    PentahoAccessControlException mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( dataSourceWizardResource.service ).removeDSW( "dswId" );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildUnauthorizedResponse();

    try {
      Response response = dataSourceWizardResource.remove( "dswId" );
      fail( "should have thrown an exception" );
    } catch ( Exception e ) {
      //good
    }
    verify( dataSourceWizardResource, times( 1 ) ).remove( "dswId" );
  }

  @Test
  public void testGetDSWDatasourceIds() throws Exception {
    List<String> mockDSWDatasources = mock( List.class );
    JaxbList<String> mockJaxbList = mock( JaxbList.class );
    doReturn( mockDSWDatasources ).when( dataSourceWizardResource.service ).getDSWDatasourceIds();
    doReturn( mockJaxbList ).when( dataSourceWizardResource ).createNewJaxbList( mockDSWDatasources );

    JaxbList<String> response = dataSourceWizardResource.getDSWDIds();

    verify( dataSourceWizardResource, times( 1 ) ).getDSWDIds();
    assertEquals( mockJaxbList, response );
  }

  @Test
  public void testPublishDsw() throws Exception {
    String domainId = "domainId";
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = false;
    boolean checkConnection = false;
    Response mockResponse = mock( Response.class );
    doReturn( "dswId" ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildOkResponse( "dswId" );

    Response response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );

    verify( dataSourceWizardResource, times( 1 ) ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testPublishDswError() throws Exception {
    String domainId = "domainId";
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = false;
    boolean checkConnection = false;
    Response mockResponse = mock( Response.class );

    //Test 1
    PentahoAccessControlException mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildUnauthorizedResponse();

    Response response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    assertEquals( mockResponse, response );

    //Test 2
    IllegalArgumentException mockIllegalArgumentException = mock( IllegalArgumentException.class );
    doThrow( mockIllegalArgumentException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildBadRequestResponse( anyString() );

    response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    assertEquals( mockResponse, response );

    //Test 3
    DataSourceWizardService.DswPublishValidationException mockDataSourceWizardServiceDswPublishValidationException = mock( DataSourceWizardService.DswPublishValidationException.class );
    doThrow( mockDataSourceWizardServiceDswPublishValidationException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildConfilictResponse( anyString() );

    response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    assertEquals( mockResponse, response );

    //Test 4
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildServerErrorResponse();

    response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    assertEquals( mockResponse, response );

    verify( dataSourceWizardResource, times( 4 ) ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
  }

  @Test
  public void doGetAnalysisAcl() throws Exception {
    String domainId = "domainId";

    doReturn( new RepositoryFileAclDto() ).when( dataSourceWizardResource.service ).getDSWAcl( domainId );

    dataSourceWizardResource.doGetDSWAcl( domainId ); // no exception thrown

    //
    doThrow( new PentahoAccessControlException() ).when( dataSourceWizardResource.service ).getDSWAcl( domainId );

    try {
      dataSourceWizardResource.doGetDSWAcl( domainId );
      fail();
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), e.getResponse().getStatus() );
    }

    //
    doThrow( new FileNotFoundException() ).when( dataSourceWizardResource.service ).getDSWAcl( domainId );

    try {
      dataSourceWizardResource.doGetDSWAcl( domainId );
      fail();
    } catch ( WebApplicationException e ) {
      assertEquals( Response.Status.CONFLICT.getStatusCode(), e.getResponse().getStatus() );
    }
  }

  @Test
  public void doSetMetadataAcl() throws Exception {
    String domainId = "domainId";

    Response response = dataSourceWizardResource.doSetDSWAcl( domainId, null );
    assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );

    //
    doThrow( new PentahoAccessControlException() ).when( dataSourceWizardResource.service ).setDSWAcl( domainId, null );

    response = dataSourceWizardResource.doSetDSWAcl( domainId, null );
    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus() );

    //
    doThrow( new FileNotFoundException() ).when( dataSourceWizardResource.service ).setDSWAcl( domainId, null );

    response = dataSourceWizardResource.doSetDSWAcl( domainId, null );
    assertEquals( Response.Status.CONFLICT.getStatusCode(), response.getStatus() );
  }
}
