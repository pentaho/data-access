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

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.MetadataService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceImpl;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.web.http.api.resources.FileResource;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class MetadataResourceTest {

  private static MetadataResource metadataResource;

  @Before
  public void setUp() {
    metadataResource = spy( new MetadataResource() );
    metadataResource.service = mock( MetadataService.class );
  }

  @After
  public void cleanup() {
    metadataResource = null;
  }

  @Test
  public void testDoGetMetadataFilesAsDownload() throws Exception {
    Response mockResponse = mock( Response.class );
    Map<String, InputStream> mockFileData = mock( Map.class );

    doReturn( true ).when( metadataResource ).canAdminister();
    doReturn( true ).when( metadataResource).isInstanceOfIPentahoMetadataDomainRepositoryExporter( metadataResource.metadataDomainRepository );
    doReturn( mockFileData ).when( metadataResource ).getDomainFilesData( "metadataId" );
    doReturn( mockResponse ).when( metadataResource ).createAttachment( mockFileData, "metadataId" );

    Response response = metadataResource.doGetMetadataFilesAsDownload( "metadataId" );

    verify( metadataResource, times( 1 ) ).doGetMetadataFilesAsDownload(  "metadataId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testDoGetMetadataFilesAsDownloadError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    doReturn( false ).when( metadataResource ).canAdminister();
    doReturn( mockResponse ).when( metadataResource ).buildUnauthorizedResponse();

    Response response = metadataResource.doGetMetadataFilesAsDownload( "metadataId" );
    assertEquals( mockResponse, response );

    //Test 2
    doReturn( true ).when( metadataResource ).canAdminister();
    doReturn( mockResponse ).when( metadataResource ).buildServerErrorResponse();

    response = metadataResource.doGetMetadataFilesAsDownload( "metadataId" );
    assertEquals( mockResponse, response );

    verify( metadataResource, times( 2 ) ).doGetMetadataFilesAsDownload( "metadataId" );
  }

  @Test
  public void testDoRemoveMetadata() throws Exception {
    Response mockResponse = mock( Response.class );
    doReturn( mockResponse ).when( metadataResource ).buildOkResponse();

    Response response = metadataResource.doRemoveMetadata( "metadataId" );

    verify( metadataResource, times( 1 ) ).doRemoveMetadata( "metadataId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testDoRemoveMetadataError() throws Exception {
    Response mockResponse = mock( Response.class );
    PentahoAccessControlException mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( metadataResource.service ).removeMetadata( "metadataId" );
    doReturn( mockResponse ).when( metadataResource ).buildUnauthorizedResponse();

    Response response = metadataResource.doRemoveMetadata( "metadataId" );

    verify( metadataResource, times( 1 ) ).doRemoveMetadata( "metadataId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testGetMetadataDatasourceIds() throws Exception {
    List<String> mockDSWDatasourceIds = mock( List.class );
    JaxbList<String> mockJaxbList = mock( JaxbList.class );
    doReturn( mockDSWDatasourceIds ).when( metadataResource.service ).getMetadataDatasourceIds();
    doReturn( mockJaxbList ).when( metadataResource ).createNewJaxbList( mockDSWDatasourceIds );

    JaxbList<String> response = metadataResource.getMetadataDatasourceIds();

    verify( metadataResource, times( 1 ) ).getMetadataDatasourceIds();
    assertEquals( mockJaxbList, response );
  }

  @Test
  public void testImportMetadataDatasource() throws Exception {
    Response mockResponse = mock( Response.class );

    String domainId = "domainId";
    InputStream metadataFile = mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = mock( FormDataContentDisposition.class );
    String overwrite = "overwrite";
    List<FormDataBodyPart> localeFiles = mock( List.class );
    List<FormDataContentDisposition> localeFilesInfo = mock( List.class );


    doNothing().when( metadataResource.service ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    doReturn( mockResponse ).when( metadataResource ).buildOkResponse( "3" );

    Response response = metadataResource.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );

    verify( metadataResource, times( 1 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testImportMetadataDatasourceError() throws Exception {
    Response mockResponse = mock( Response.class );
    FileResource mockFileResource = mock( FileResource.class );

    String domainId = "domainId";
    InputStream metadataFile = mock( InputStream.class );
    FormDataContentDisposition metadataFileInfo = mock( FormDataContentDisposition.class );
    String overwrite = "overwrite";
    List<FormDataBodyPart> localeFiles = mock( List.class );
    List<FormDataContentDisposition> localeFilesInfo = mock( List.class );

    //Test 1
    PentahoAccessControlException mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( metadataResource.service ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    doReturn( mockResponse ).when( metadataResource ).buildServerErrorResponse( mockPentahoAccessControlException );

    Response response = metadataResource.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    assertEquals( mockResponse, response );

    //Test 2
    PlatformImportException mockPlatformImportException = mock( PlatformImportException.class );
    doThrow( mockPlatformImportException ).when( metadataResource.service ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    doReturn( 10 ).when( mockPlatformImportException ).getErrorStatus();
    doReturn( mockFileResource ).when( metadataResource ).createFileResource();
    doReturn( mockResponse ).when( metadataResource ).buildServerError003Response( domainId, mockFileResource );

    response = metadataResource.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    assertEquals( mockResponse, response );

    //Test 3
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockPlatformImportException ).when( metadataResource.service ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    doReturn( 1 ).when( mockPlatformImportException ).getErrorStatus();
    doReturn( mockResponse ).when( metadataResource ).buildOkResponse( "1" );
    doReturn( mockException ).when( mockPlatformImportException ).getCause();

    response = metadataResource.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    assertEquals( mockResponse, response );

    //Test
    doThrow( mockException ).when( metadataResource.service ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    doReturn( mockResponse ).when( metadataResource ).buildServerError001Response();

    response = metadataResource.importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
    assertEquals( mockResponse, response );

    verify( metadataResource, times( 4 ) ).importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles,
      localeFilesInfo );
  }
}

