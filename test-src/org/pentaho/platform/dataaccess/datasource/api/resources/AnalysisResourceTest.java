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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

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

  @Before
  public void setUp() {
    analysisResource = spy( new AnalysisResource() );
    analysisResource.service = mock( AnalysisService.class );
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

    Response response = analysisResource.doGetAnalysisFilesAsDownload( "analysisId" );

    verify( analysisResource, times( 1 ) ).doGetAnalysisFilesAsDownload(  "analysisId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testDoGetAnalysisFilesAsDownloadError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    PentahoAccessControlException mockException = mock( PentahoAccessControlException.class );
    doThrow( mockException ).when( analysisResource.service ).doGetAnalysisFilesAsDownload( "analysisId" );
    doReturn( mockResponse ).when( analysisResource ).buildUnauthorizedResponse();

    Response response = analysisResource.doGetAnalysisFilesAsDownload( "analysisId" );
    assertEquals( mockResponse, response );

    verify( analysisResource, times( 1 ) ).doGetAnalysisFilesAsDownload( "analysisId" );
  }

  @Test
  public void testDoRemoveAnalysis() throws Exception {
    Response mockResponse = mock( Response.class );

    doNothing().when( analysisResource.service ).removeAnalysis( "analysisId" );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse();

    Response response = analysisResource.doRemoveAnalysis( "analysisId" );

    verify( analysisResource, times( 1 ) ).doRemoveAnalysis( "analysisId" );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testDoRemoveAnalysisError() throws Exception {
    Response mockResponse = mock( Response.class );

    //Test 1
    PentahoAccessControlException mockException = mock( PentahoAccessControlException.class );
    doThrow( mockException ).when( analysisResource.service ).removeAnalysis( "analysisId" );
    doReturn( mockResponse ).when( analysisResource ).buildUnauthorizedResponse();

    Response response = analysisResource.doRemoveAnalysis( "analysisId" );
    assertEquals( mockResponse, response );

    verify( analysisResource, times( 1 ) ).doRemoveAnalysis( "analysisId" );
  }

  @Test
  public void testGetAnalysisDatasourceIds() throws Exception {
    List<String> mockDSWDatasourceIds = mock( List.class );
    JaxbList<String> mockJaxbList = mock( JaxbList.class );
    doReturn( mockDSWDatasourceIds ).when( analysisResource.service ).getAnalysisDatasourceIds();
    doReturn( mockJaxbList ).when( analysisResource ).createNewJaxbList( mockDSWDatasourceIds );

    JaxbList<String> response = analysisResource.getAnalysisDatasourceIds();

    verify( analysisResource, times( 1 ) ).getAnalysisDatasourceIds();
    assertEquals( mockJaxbList, response );
  }

  @Test
  public void testImportMetadataDatasource() throws Exception {
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
      true, true, parameters );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse( "3" );

    Response response = analysisResource.putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName,
      origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters );

    verify( analysisResource, times( 1 ) ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName,
      origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters );
    assertEquals( mockResponse, response );
  }

  @Test
  public void testImportMetadataDatasourceError() throws Exception {
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
      false, false, parameters );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse( "5" );

    Response response = analysisResource.putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters );
    assertEquals( mockResponse, response );

    //Test 2
    PlatformImportException mockPlatformImportException = mock( PlatformImportException.class );
    doThrow( mockPlatformImportException ).when( analysisResource.service ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        true, true, parameters );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse( "0" );

    response = analysisResource.putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters );
    assertEquals( mockResponse, response );

    //Test 3
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( analysisResource.service ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        true, true, parameters );
    doReturn( mockResponse ).when( analysisResource ).buildOkResponse( "2" );

    response = analysisResource.putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters );
    assertEquals( mockResponse, response );

    verify( analysisResource, times( 3 ) ).putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
      overwrite, xmlaEnabledFlag, parameters );
  }
}
