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

package org.pentaho.platform.dataaccess.datasource.api;

import com.sun.jersey.core.header.FormDataContentDisposition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AnalysisServiceTest {
  
  private static AnalysisService analysisService;

  @Before
  public void setUp() {
    analysisService = spy( new AnalysisService() );
    analysisService.metadataDomainRepository = mock( IMetadataDomainRepository.class );
    analysisService.mondrianCatalogService = mock( IMondrianCatalogService.class );
  }

  @After
  public void cleanup() {
    analysisService = null;
  }

  @Test
  public void testDoGetAnalysisFilesAsDownload() throws Exception {
    Map<String, InputStream> mockMap = mock( Map.class );
    MondrianCatalogRepositoryHelper mockMondrianCatalogRepositoryHelpere = mock( MondrianCatalogRepositoryHelper.class );

    doReturn( true ).when( analysisService ).canAdministerCheck();
    doReturn( mockMondrianCatalogRepositoryHelpere ).when( analysisService ).createNewMondrianCatalogRepositoryHelper();
    doReturn( mockMap ).when( mockMondrianCatalogRepositoryHelpere ).getModrianSchemaFiles( "analysisId" );

    Map<String, InputStream> response = analysisService.doGetAnalysisFilesAsDownload( "analysisId" );

    verify( analysisService, times( 1 ) ).doGetAnalysisFilesAsDownload(  "analysisId" );
    assertEquals( mockMap, response );
  }

  @Test
  public void testDoGetAnalysisFilesAsDownloadError() throws Exception {
    doReturn( false ).when( analysisService ).canAdministerCheck();
    try {
      Map<String, InputStream> response = analysisService.doGetAnalysisFilesAsDownload( "analysisId" );
    } catch ( PentahoAccessControlException e ) {
      //do nothing
    }

    verify( analysisService, times( 1 ) ).doGetAnalysisFilesAsDownload( "analysisId" );
  }

  @Test
  public void testRemoveAnalysis() throws Exception {
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );

    doReturn( true ).when( analysisService ).canAdministerCheck();
    doReturn( "param" ).when( analysisService ).fixEncodedSlashParam( "analysisId" );
    doReturn( mockIPentahoSession ).when( analysisService ).getSession();
    doNothing().when( analysisService.mondrianCatalogService ).removeCatalog( "param", mockIPentahoSession);

    analysisService.removeAnalysis( "analysisId" );

    verify( analysisService, times( 1 ) ).removeAnalysis( "analysisId" );
  }

  @Test
  public void testRemoveAnalysisError() throws Exception {
    doReturn( false ).when( analysisService ).canAdministerCheck();
    try {
      analysisService.removeAnalysis( "analysisId" );
    } catch ( PentahoAccessControlException e ) {
      //do nothing
    }
    verify( analysisService, times( 1 ) ).removeAnalysis(  "analysisId" );
  }

  @Test
  public void testGetAnalysisDatasourceIds() throws Exception {
    Set<String> mockSet = mock( Set.class );
    MondrianCatalog mockMondrianCatalog = mock( MondrianCatalog.class );
    List<MondrianCatalog> mondrianCatalogList = new ArrayList<MondrianCatalog>();
    mondrianCatalogList.add( mockMondrianCatalog );
    List<String> mockList = new ArrayList<String>();
    mockList.add( mockMondrianCatalog.getName() );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );

    doReturn( mockIPentahoSession ).when( analysisService ).getSession();
    doReturn( mondrianCatalogList ).when( analysisService.mondrianCatalogService ).listCatalogs( mockIPentahoSession, false );
    doReturn( mockSet ).when( analysisService.metadataDomainRepository ).getDomainIds();

    List<String> response = analysisService.getAnalysisDatasourceIds();

    verify( analysisService, times( 1 ) ).getAnalysisDatasourceIds();
    assertEquals( mockList, response );
  }

  @Test
  public void testPutMondrianSchema() throws Exception {
    InputStream dataInputStream = mock( InputStream.class );
    FormDataContentDisposition schemaFileInfo = mock( FormDataContentDisposition.class );
    String catalogName = "catalogName";
    String origCatalogName = "origCatalogName";
    String dataSourceName = "dataSourceName";
    String overwrite = "overwrite";
    String xmlaEnabledFlag = "xmlaEnabledFlag";
    String parameters = "parameters";

    doNothing().when( analysisService ).accessValidation();
    doNothing().when( analysisService ).processMondrianImport( dataInputStream, catalogName, origCatalogName, overwrite, xmlaEnabledFlag, parameters, null);

    analysisService.putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, dataSourceName, overwrite, xmlaEnabledFlag, parameters);

    verify( analysisService, times( 1 ) ).putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, dataSourceName, overwrite, xmlaEnabledFlag, parameters);
  }
}
