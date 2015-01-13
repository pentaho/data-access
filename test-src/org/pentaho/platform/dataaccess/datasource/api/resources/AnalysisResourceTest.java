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

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import com.sun.jersey.core.header.FormDataContentDisposition;

public class AnalysisResourceTest {

  private static AnalysisResource analysisResource;

  private class AnalysisResourceMock extends AnalysisResource {
    @Override protected AnalysisService createAnalysisService() {
      return mock( AnalysisService.class );
    }
  }

  @Before
  public void setUp() {
    analysisResource = spy( new AnalysisResourceMock() );
  }

  @After
  public void cleanup() {
    analysisResource = null;
  }

  @Test
  public void testDoGetAnalysisFilesAsDownload() throws Exception {
    Response mockResponse = mock( Response.class );
    Map<String, InputStream> mockFileData = mock( Map.class );

    doReturn( mockFileData ).when( analysisResource.service ).doGetAnalysisFilesAsDownload( "analysisId" );
    doReturn( mockResponse ).when( analysisResource ).createAttachment( mockFileData, "analysisId" );

    Response response = analysisResource.downloadSchema( "analysisId" );

    verify( analysisResource, times( 1 ) ).downloadSchema(  "analysisId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testDoGetAnalysisFilesAsDownloadError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    PentahoAccessControlException mockException = mock( PentahoAccessControlException.class );
    doThrow( mockException ).when( analysisResource.service ).doGetAnalysisFilesAsDownload( "analysisId" );


    try {
      Response response = analysisResource.downloadSchema( "analysisId" );
      fail( "Should have gotten a WebApplicationException" );
    } catch ( WebApplicationException e ) {
      // Good
    }

    verify( analysisResource, times( 1 ) ).downloadSchema( "analysisId" );
  }

  @Test
  public void testdeleteSchema() throws Exception {
    Response mockResponse = mock( Response.class );

    doNothing().when( analysisResource.service ).removeAnalysis( "analysisId" );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse();

    Response response = analysisResource.deleteSchema( "analysisId" );

    verify( analysisResource, times( 1 ) ).deleteSchema( "analysisId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testdeleteSchemaError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    PentahoAccessControlException mockException = mock( PentahoAccessControlException.class );
    doThrow( mockException ).when( analysisResource.service ).removeAnalysis( "analysisId" );

    try {
      Response response = analysisResource.deleteSchema( "analysisId" );
      fail( "should have gotten a WebApplicationException" );
    } catch ( WebApplicationException e ) {
      // Good
    }

    verify( analysisResource, times( 1 ) ).deleteSchema( "analysisId" );
  }

  @Test
  public void testGetAnalysisDatasourceIds() throws Exception {
    List<String> mockDSWDatasourceIds = mock( List.class );
    JaxbList<String> mockJaxbList = mock( JaxbList.class );
    doReturn( mockDSWDatasourceIds ).when( analysisResource.service ).getAnalysisDatasourceIds();
    doReturn( mockJaxbList ).when( analysisResource ).createNewJaxbList( mockDSWDatasourceIds );

    JaxbList<String> response = analysisResource.getSchemaIds();

    verify( analysisResource, times( 1 ) ).getSchemaIds();
    assertEquals( mockJaxbList, response );
  }

  @Test
  public void testImportAnalysisDatasource() throws Exception {
    Response mockResponse = mock( Response.class );

    InputStream uploadAnalysis = mock( InputStream.class );
    FormDataContentDisposition schemaFileInfo = mock( FormDataContentDisposition.class );
    String catalogName = "catalogName";
    String origCatalogName = "origCatalogName";
    String datasourceName = "datasourceName";
    String overwrite = "overwrite";
    String xmlaEnabledFlag = "xmlaEnabledFlag";
    String parameters = "parameters";

    doNothing().when( analysisResource.service ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName,
        origCatalogName, datasourceName,
        true, true, parameters, null );

    Response response = analysisResource.putSchema( catalogName, uploadAnalysis, schemaFileInfo, catalogName,
        origCatalogName,
        Boolean.valueOf( overwrite ), Boolean.valueOf( xmlaEnabledFlag ), parameters, null );

    verify( analysisResource, times( 1 ) ).putSchema( catalogName, uploadAnalysis, schemaFileInfo, catalogName,
        origCatalogName,
        Boolean.valueOf( overwrite ), Boolean.valueOf( xmlaEnabledFlag ), parameters, null );
    assertEquals( 201, response.getStatus() );
  }

  @Test
  public void testImportAnalysisDatasourceError() throws Exception {
    Response mockResponse = mock( Response.class );

    InputStream uploadAnalysis = mock( InputStream.class );
    FormDataContentDisposition schemaFileInfo = mock( FormDataContentDisposition.class );
    String catalogName = "catalogName";
    String origCatalogName = "origCatalogName";
    String datasourceName = "datasourceName";
    String overwrite = "overwrite";
    String xmlaEnabledFlag = "xmlaEnabledFlag";
    String parameters = "parameters";

    //Test 1
    PentahoAccessControlException mockPentahoAccessControlException = mock( PentahoAccessControlException.class );
    doThrow( mockPentahoAccessControlException ).when( analysisResource.service ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        false, false, parameters, null );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse( "5" );

    Response response = analysisResource.importMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        overwrite, xmlaEnabledFlag, parameters, null );
    assertEquals( mockResponse, response );

    //Test 2
    PlatformImportException mockPlatformImportException = mock( PlatformImportException.class );
    doThrow( mockPlatformImportException ).when( analysisResource.service ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        true, true, parameters, null );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse( "0" );

    response = analysisResource.importMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters, null );
    assertEquals( mockResponse, response );

    //Test 3
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( analysisResource.service ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        true, true, parameters, null );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse( "2" );

    response = analysisResource.importMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters, null );
    assertEquals( mockResponse, response );

    verify( analysisResource, times( 3 ) ).importMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        overwrite, xmlaEnabledFlag, parameters, null );
  }

}
