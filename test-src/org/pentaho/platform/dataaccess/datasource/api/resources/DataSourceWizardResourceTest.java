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

package org.pentaho.platform.dataaccess.datasource.api.resources;
import com.sun.jersey.multipart.FormDataParam;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class DataSourceWizardResourceTest {

  private static DataSourceWizardResource dataSourceWizardResource;

  @Before
  public void setUp() {
    dataSourceWizardResource = spy( new DataSourceWizardResource() );
    dataSourceWizardResource.service = mock( DataSourceWizardService.class );
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

    Response response = dataSourceWizardResource.download( "dswId" );

    verify( dataSourceWizardResource, times( 1 ) ).download(  "dswId" );
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

    Response response = dataSourceWizardResource.download( "dswId" );
    assertEquals( mockResponse, response );

    verify( dataSourceWizardResource, times( 1 ) ).download( "dswId" );
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

    Response response = dataSourceWizardResource.remove( "dswId" );
    assertEquals( mockResponse, response );

    verify( dataSourceWizardResource, times( 1 ) ).remove( "dswId" );
  }

  @Test
  public void testGetDSWDatasourceIds() throws Exception {
    List<String> mockDSWDatasources = mock( List.class );
    JaxbList<String> mockJaxbList = mock( JaxbList.class );
    doReturn( mockDSWDatasources ).when( dataSourceWizardResource.service ).getDSWDatasourceIds();
    doReturn( mockJaxbList ).when( dataSourceWizardResource ).createNewJaxbList( mockDSWDatasources );

    JaxbList<String> response = dataSourceWizardResource.getDSWDatasourceIds();

    verify( dataSourceWizardResource, times( 1 ) ).getDSWDatasourceIds();
    assertEquals( mockJaxbList, response );
  }

  @Test
  public void testPublishDsw() throws Exception {
    String domainId = "domainId";
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = false;
    boolean checkConnection = false;
    Response mockResponse = mock( Response.class );
    doReturn( "dswId" ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildOkResponse( "dswId" );

    Response response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection );

    verify( dataSourceWizardResource, times( 1 ) ).publishDsw( domainId, metadataFile, overwrite, checkConnection );
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
    doThrow( mockPentahoAccessControlException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildUnauthorizedResponse();

    Response response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection );
    assertEquals( mockResponse, response );

    //Test 2
    IllegalArgumentException mockIllegalArgumentException = mock( IllegalArgumentException.class );
    doThrow( mockIllegalArgumentException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildBadRequestResponse( anyString() );

    response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection );
    assertEquals( mockResponse, response );

    //Test 3
    DataSourceWizardService.DswPublishValidationException mockDataSourceWizardServiceDswPublishValidationException = mock( DataSourceWizardService.DswPublishValidationException.class );
    doThrow( mockDataSourceWizardServiceDswPublishValidationException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildConfilictResponse( anyString() );

    response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection );
    assertEquals( mockResponse, response );

    //Test 4
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( dataSourceWizardResource.service ).publishDsw( domainId, metadataFile, overwrite, checkConnection );
    doReturn( mockResponse ).when( dataSourceWizardResource ).buildServerErrorResponse();

    response = dataSourceWizardResource.publishDsw( domainId, metadataFile, overwrite, checkConnection );
    assertEquals( mockResponse, response );

    verify( dataSourceWizardResource, times( 4 ) ).publishDsw( domainId, metadataFile, overwrite, checkConnection );
  }
}
