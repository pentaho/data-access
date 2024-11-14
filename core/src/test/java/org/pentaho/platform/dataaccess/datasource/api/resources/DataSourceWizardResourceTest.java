/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.api.resources;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
  public void publishDswFromTemp() throws Exception {
    String domainId = "domainId";
    MetadataTempFilesListDto dto = new MetadataTempFilesListDto( );
    dto.setXmiFileName( "fileName" );
    String fileList = "{xmiFileName :fileList}";
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = false;
    boolean checkConnection = false;
    Response mockResponse = mock( Response.class );
    doReturn( "dswId" ).when( dataSourceWizardResource.service ).publishDswFromTemp( eq(domainId), any(), eq(overwrite), eq(checkConnection), eq(null ) );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildOkResponse( "dswId" );

    Response response = dataSourceWizardResource.publishDswFromTemp( domainId, fileList, overwrite, checkConnection, null );

    verify( dataSourceWizardResource, times( 1 ) ).publishDswFromTemp( domainId, fileList, overwrite, checkConnection, null );
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
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildBadRequestResponse( any() );

    response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    assertEquals( mockResponse, response );

    //Test 3
    DataSourceWizardService.DswPublishValidationException mockDataSourceWizardServiceDswPublishValidationException = mock( DataSourceWizardService.DswPublishValidationException.class );
    doThrow( mockDataSourceWizardServiceDswPublishValidationException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildConfilictResponse( any() );

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
