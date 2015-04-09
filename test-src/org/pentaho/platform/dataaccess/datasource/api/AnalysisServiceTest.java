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

package org.pentaho.platform.dataaccess.datasource.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;

import com.sun.jersey.core.header.FormDataContentDisposition;

public class AnalysisServiceTest {

  private static AnalysisService analysisService;

  private class AnalysisServiceMock extends AnalysisService {
    @Override protected IUnifiedRepository getRepository() {
      return mock( IUnifiedRepository.class );
    }
  }

  @Before
  public void setUp() {
    analysisService = spy( new AnalysisServiceMock());
    analysisService.metadataDomainRepository = mock( IMetadataDomainRepository.class );
    analysisService.importer = mock( IPlatformImporter.class );
    analysisService.aclAwareMondrianCatalogService = mock( IAclAwareMondrianCatalogService.class );
    analysisService.mondrianCatalogService = analysisService.aclAwareMondrianCatalogService;
  }

  @After
  public void cleanup() {
    analysisService = null;
  }

  @Test
  public void testDoGetAnalysisFilesAsDownload() throws Exception {
    final String analysisId = "analysisId";
    Map<String, InputStream> mockMap = mock( Map.class );
    MondrianCatalogRepositoryHelper
        mockMondrianCatalogRepositoryHelpere =
        mock( MondrianCatalogRepositoryHelper.class );

    doReturn( true ).when( analysisService ).canManageACL();
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( analysisService.aclAwareMondrianCatalogService.getCatalog( eq( analysisId ), any( IPentahoSession.class ) ) ).thenReturn(
        mondrianCatalog );
    doReturn( mockMondrianCatalogRepositoryHelpere ).when( analysisService ).createNewMondrianCatalogRepositoryHelper();
    doReturn( mockMap ).when( mockMondrianCatalogRepositoryHelpere ).getModrianSchemaFiles( analysisId );

    Map<String, InputStream> response = analysisService.doGetAnalysisFilesAsDownload( analysisId );

    verify( analysisService, times( 1 ) ).doGetAnalysisFilesAsDownload( analysisId );
    assertEquals( mockMap, response );
  }

  @Test
  public void testDoGetAnalysisFilesAsDownloadError() throws Exception {
    final String analysisId = "analysisId";
    doReturn( false ).when( analysisService ).canManageACL();
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( analysisService.aclAwareMondrianCatalogService.getCatalog( eq( analysisId ), any( IPentahoSession.class ) ) ).thenReturn(
        mondrianCatalog );
    try {
      analysisService.doGetAnalysisFilesAsDownload( analysisId );
      fail();
    } catch ( PentahoAccessControlException e ) {
      //expected
    }

    verify( analysisService, times( 1 ) ).doGetAnalysisFilesAsDownload( analysisId );
  }

  @Test
  public void testRemoveAnalysis() throws Exception {
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );

    doReturn( true ).when( analysisService ).canAdministerCheck();
    doReturn( "param" ).when( analysisService ).fixEncodedSlashParam( "analysisId" );
    doReturn( mockIPentahoSession ).when( analysisService ).getSession();
    doNothing().when( analysisService.mondrianCatalogService ).removeCatalog( "param", mockIPentahoSession );

    analysisService.removeAnalysis( "analysisId" );

    verify( analysisService, times( 1 ) ).removeAnalysis( "analysisId" );
  }

  @Test
  public void testRemoveAnalysisError() throws Exception {
    doReturn( false ).when( analysisService ).canAdministerCheck();
    try {
      analysisService.removeAnalysis( "analysisId" );
      fail();
    } catch ( PentahoAccessControlException e ) {
      //expected
    }
    verify( analysisService, times( 1 ) ).removeAnalysis( "analysisId" );
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
    doReturn( mondrianCatalogList ).when( analysisService.mondrianCatalogService )
        .listCatalogs( mockIPentahoSession, false );
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
    boolean xmlaEnabledFlag = true;
    String parameters = "parameters";

    doNothing().when( analysisService ).accessValidation();
    doNothing().when( analysisService )
        .processMondrianImport( dataInputStream, catalogName, origCatalogName, true, xmlaEnabledFlag, parameters, null,
            null );

    analysisService
        .putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, dataSourceName, true,
            xmlaEnabledFlag, parameters, null );

    verify( analysisService, times( 1 ) )
        .putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, dataSourceName, true,
            xmlaEnabledFlag, parameters, null );
  }

  @Test
  public void testPutMondrianSchemaWithACL() throws Exception {
    InputStream dataInputStream = mock( InputStream.class );
    when( dataInputStream.read( any( byte[].class ) ) ).thenReturn( 10, -1 );

    FormDataContentDisposition schemaFileInfo = mock( FormDataContentDisposition.class );
    String catalogName = "catalogName";
    String origCatalogName = "";
    String dataSourceName = "dataSourceName";
    boolean xmlaEnabledFlag = true;
    String parameters = "parameters";

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    doNothing().when( analysisService ).accessValidation();

    analysisService
        .putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, dataSourceName, true,
            xmlaEnabledFlag, parameters, aclDto );

    verify( analysisService, times( 1 ) )
        .putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, dataSourceName, true,
            xmlaEnabledFlag, parameters, aclDto );

    verify( analysisService.importer ).importFile( argThat( new ArgumentMatcher<IPlatformImportBundle>() {
      @Override public boolean matches( Object argument ) {
        IPlatformImportBundle bundle = (IPlatformImportBundle) argument;
        return new RepositoryFileAclAdapter().unmarshal( aclDto ).equals( bundle.getAcl() );
      }
    } ) );
  }

  @Test
  public void testGetAnalysisDatasourceAcl() throws Exception {
    String catalogName = "catalogName";

    final RepositoryFileAcl acl = new RepositoryFileAcl.Builder( "owner" ).build();

    doReturn( true ).when( analysisService ).canManageACL();
    when( analysisService.aclAwareMondrianCatalogService.getAclFor( catalogName ) ).thenReturn( acl );
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( analysisService.aclAwareMondrianCatalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn(
        mondrianCatalog );
    final IUnifiedRepository repository = mock( IUnifiedRepository.class );
    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repository.getFileById( anyString() ) ).thenReturn( repositoryFile );
    doReturn( new HashMap<String, InputStream>() { {
        put( "test", null );
      } } ).when( analysisService )
        .doGetAnalysisFilesAsDownload( catalogName );

    final RepositoryFileAclDto aclDto = analysisService.getAnalysisDatasourceAcl( catalogName );

    verify( analysisService.aclAwareMondrianCatalogService ).getAclFor( eq( catalogName ) );

    assertEquals( acl, new RepositoryFileAclAdapter().unmarshal( aclDto ) );
  }

  @Test
  public void testGetAnalysisDatasourceAclNoAcl() throws Exception {
    String catalogName = "catalogName";

    doReturn( true ).when( analysisService ).canManageACL();
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( analysisService.aclAwareMondrianCatalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn(
        mondrianCatalog );
    when( analysisService.aclAwareMondrianCatalogService.getAclFor( catalogName ) ).thenReturn( null );
    doReturn( new HashMap<String, InputStream>() { {
        put( "test", null );
      } } ).when( analysisService )
        .doGetAnalysisFilesAsDownload( catalogName );

    final RepositoryFileAclDto aclDto = analysisService.getAnalysisDatasourceAcl( catalogName );

    verify( analysisService.aclAwareMondrianCatalogService ).getAclFor( eq( catalogName ) );

    assertNull( aclDto );
  }

  @Test
  public void testSetAnalysisDatasourceAcl() throws Exception {
    String catalogName = "catalogName";

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    doReturn( true ).when( analysisService ).canManageACL();
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( analysisService.aclAwareMondrianCatalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn(
        mondrianCatalog );
    doReturn( new HashMap<String, InputStream>() { {
        put( "test", null );
      } } ).when( analysisService )
        .doGetAnalysisFilesAsDownload( catalogName );

    analysisService.setAnalysisDatasourceAcl( catalogName, aclDto );

    verify( analysisService.aclAwareMondrianCatalogService ).setAclFor( eq( catalogName ),
        eq( new RepositoryFileAclAdapter().unmarshal( aclDto ) ) );
  }

  @Test
  public void testSetAnalysisDatasourceAclNoAcl() throws Exception {
    String catalogName = "catalogName";

    doReturn( true ).when( analysisService ).canManageACL();
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( analysisService.aclAwareMondrianCatalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn(
        mondrianCatalog );
    doReturn( new HashMap<String, InputStream>() { {
        put( "test", null );
      } } ).when( analysisService )
        .doGetAnalysisFilesAsDownload( catalogName );

    analysisService.setAnalysisDatasourceAcl( catalogName, null );

    verify( analysisService.aclAwareMondrianCatalogService ).setAclFor( eq( catalogName ),
        (RepositoryFileAcl) isNull() );
  }
}
