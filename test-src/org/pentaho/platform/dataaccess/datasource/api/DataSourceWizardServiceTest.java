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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.metadata.IAclAwarePentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;

public class DataSourceWizardServiceTest {

  private static DataSourceWizardService dataSourceWizardService;

  private class DataSourceWizardServiceMock extends DataSourceWizardService {
    @Override protected IUnifiedRepository getRepository() {
      return mock( IUnifiedRepository.class );
    }

    @Override protected IDSWDatasourceService getDswDatasourceService() {
      return mock( IDSWDatasourceService.class );
    }
  }

  @Before
  public void setUp() {
    dataSourceWizardService = spy( new DataSourceWizardServiceMock() );
    dataSourceWizardService.metadataDomainRepository = mock( IMetadataDomainRepository.class );
    dataSourceWizardService.mondrianCatalogService = mock( IMondrianCatalogService.class );
    dataSourceWizardService.datasourceMgmtSvc = mock( IDatasourceMgmtService.class );
    dataSourceWizardService.modelerService = mock( IModelerService.class );
    dataSourceWizardService.aclAwarePentahoMetadataDomainRepositoryImporter = mock( IAclAwarePentahoMetadataDomainRepositoryImporter.class );
    dataSourceWizardService.aclAwareMondrianCatalogService = mock( IAclAwareMondrianCatalogService.class );
  }

  @After
  public void cleanup() {
    dataSourceWizardService = null;
  }

  @Test
  public void testDoGetDSWFilesAsDownload() throws Exception {
    Map<String, InputStream> mockFileData = mock( Map.class );
    ModelerWorkspace mockModelerWorkspace = mock( ModelerWorkspace.class );
    MondrianCatalogRepositoryHelper mockMondrianCatalogRepositoryHelper = mock( MondrianCatalogRepositoryHelper.class );
    LogicalModel mockLogicalModel = mock( LogicalModel.class );
    String mockObject = "not null";
    String dswId = "dswId";

    doReturn( true ).when( dataSourceWizardService ).canManageACL();
    doReturn( mockFileData ).when( dataSourceWizardService ).getMetadataFiles( dswId );
    doReturn( mockModelerWorkspace ).when( dataSourceWizardService ).createModelerWorkspace();
    doReturn( null ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.ANALYSIS );
    doReturn( mockLogicalModel ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.REPORTING );
    doReturn( mockObject ).when( mockLogicalModel ).getProperty( "MondrianCatalogRef" );
    doReturn( mockMondrianCatalogRepositoryHelper ).when( dataSourceWizardService ).createMondrianCatalogRepositoryHelper();
    doNothing().when( dataSourceWizardService ).parseMondrianSchemaNameWrapper( dswId, mockFileData );

    Map<String, InputStream> response = dataSourceWizardService.doGetDSWFilesAsDownload( "dswId" );
    assertEquals( mockFileData, response );

    verify( dataSourceWizardService, times( 1 ) ).doGetDSWFilesAsDownload( "dswId" );
  }

  @Test
  public void testDoGetDSWFilesAsDownloadError() throws Exception {
    Map<String, InputStream> mockFileData = mock( Map.class );
    ModelerWorkspace mockModelerWorkspace = mock( ModelerWorkspace.class );
    String dswId = "dswId";

    //Test 1
    doReturn( false ).when( dataSourceWizardService ).canManageACL();

    try {
      Map<String, InputStream> response = dataSourceWizardService.doGetDSWFilesAsDownload( "dswId" );
      fail();
    } catch ( PentahoAccessControlException pace ) {
      //expected
    }

    verify( dataSourceWizardService, times( 1 ) ).doGetDSWFilesAsDownload( "dswId" );
  }

  @Test
  public void testRemoveDSW() throws Exception {
    Domain mockDomain = mock( Domain.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    ModelerWorkspace mockModelerWorkspace = mock( ModelerWorkspace.class );
    LogicalModel mockLogicalModel = mock( LogicalModel.class );
    String mockObject = "not null";
    String dswId = "dswId";

    doReturn( true ).when( dataSourceWizardService ).canAdministerCheck();
    doReturn( dswId ).when( dataSourceWizardService ).parseMondrianSchemaNameWrapper( dswId );
    doReturn( mockDomain ).when( dataSourceWizardService.metadataDomainRepository ).getDomain( dswId );
    doReturn( mockModelerWorkspace ).when( dataSourceWizardService ).createModelerWorkspace();
    doReturn( null ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.ANALYSIS );
    doReturn( mockLogicalModel ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.REPORTING );
    doReturn( mockObject ).when( mockLogicalModel ).getProperty( "MondrianCatalogRef" );
    doReturn( mockIPentahoSession ).when( dataSourceWizardService ).getSession();
    doNothing().when( dataSourceWizardService.mondrianCatalogService ).removeCatalog(
        "not null", mockIPentahoSession );
    doNothing().when( dataSourceWizardService.metadataDomainRepository ).removeDomain( dswId );

    dataSourceWizardService.removeDSW( "dswId" );

    verify( dataSourceWizardService, times( 1 ) ).removeDSW( "dswId" );
  }

  @Test
  public void testErrorRemovingMondrianDoesNotStopXMIRemoval() throws Exception {
    Domain mockDomain = mock( Domain.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    ModelerWorkspace mockModelerWorkspace = mock( ModelerWorkspace.class );
    LogicalModel mockLogicalModel = mock( LogicalModel.class );
    String mockObject = "not null";
    String dswId = "dswId";

    doReturn( true ).when( dataSourceWizardService ).canAdministerCheck();
    doReturn( dswId ).when( dataSourceWizardService ).parseMondrianSchemaNameWrapper( dswId );
    doReturn( mockDomain ).when( dataSourceWizardService.metadataDomainRepository ).getDomain( dswId );
    doReturn( mockModelerWorkspace ).when( dataSourceWizardService ).createModelerWorkspace();
    doReturn( null ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.ANALYSIS );
    doReturn( mockLogicalModel ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.REPORTING );
    doReturn( mockObject ).when( mockLogicalModel ).getProperty( "MondrianCatalogRef" );
    doReturn( mockIPentahoSession ).when( dataSourceWizardService ).getSession();
    doThrow( new MondrianCatalogServiceException( "who cares" ) )
      .when( dataSourceWizardService.mondrianCatalogService )
      .removeCatalog( "not null", mockIPentahoSession );
    doNothing().when( dataSourceWizardService.metadataDomainRepository ).removeDomain( dswId );

    dataSourceWizardService.removeDSW( "dswId" );

    verify( dataSourceWizardService, times( 1 ) ).removeDSW( "dswId" );
    verify( dataSourceWizardService.metadataDomainRepository ).removeDomain( dswId );
  }

  @Test
  public void testRemoveDSWError() throws Exception {
    Domain mockDomain = mock( Domain.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    ModelerWorkspace mockModelerWorkspace = mock( ModelerWorkspace.class );
    LogicalModel mockLogicalModel = mock( LogicalModel.class );
    String mockObject = "not null";
    String dswId = "dswId";

    //Test 1
    doReturn( false ).when( dataSourceWizardService ).canAdministerCheck();

    try {
      dataSourceWizardService.removeDSW( "dswId" );
      fail();
    } catch ( PentahoAccessControlException pace ) {
      //expected
    }

    //Test 2
    DatasourceServiceException mockDatasourceServiceException = mock( DatasourceServiceException.class );
    doReturn( true ).when( dataSourceWizardService ).canAdministerCheck();
    doReturn( dswId ).when( dataSourceWizardService ).parseMondrianSchemaNameWrapper( dswId );
    doReturn( mockDomain ).when( dataSourceWizardService.metadataDomainRepository ).getDomain( dswId );
    doReturn( mockModelerWorkspace ).when( dataSourceWizardService ).createModelerWorkspace();
    doReturn( null ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.ANALYSIS );
    doReturn( mockLogicalModel ).when( mockModelerWorkspace ).getLogicalModel( ModelerPerspective.REPORTING );
    doReturn( mockObject ).when( mockLogicalModel ).getProperty( "MondrianCatalogRef" );
    doReturn( mockIPentahoSession ).when( dataSourceWizardService ).getSession();
    doNothing().when( dataSourceWizardService.mondrianCatalogService ).removeCatalog(
        "not null", mockIPentahoSession );
    doThrow( mockDatasourceServiceException ).when( dataSourceWizardService.dswService ).deleteLogicalModel( null,
        null );

    dataSourceWizardService.removeDSW( "dswId" );

    verify( dataSourceWizardService, times( 2 ) ).removeDSW( "dswId" );
  }

  @Test
  public void testGetDSWDatasourceIds() throws Exception {
    Domain mockDomain = mock( Domain.class );
    LogicalModelSummary mockLogicalModelSummary  = mock( LogicalModelSummary.class );
    List<LogicalModelSummary> mockLogicalModelSummaryList = new ArrayList<LogicalModelSummary>();
    mockLogicalModelSummaryList.add( mockLogicalModelSummary );
    List<LogicalModel> mockLogicalModelList = new ArrayList<LogicalModel>();
    LogicalModel mockLogicalModel = mock( LogicalModel.class );
    mockLogicalModelList.add( mockLogicalModel );
    Object mockObject = mock( Object.class );

    List<String> datasourceList = new ArrayList<String>();
    datasourceList.add( mockLogicalModelSummary.getDomainId() );

    doReturn( mockLogicalModelSummaryList ).when( dataSourceWizardService.dswService ).getLogicalModels( null );
    doReturn( mockDomain ).when( dataSourceWizardService.modelerService ).loadDomain( anyString() );
    doReturn( mockLogicalModelList ).when( mockDomain ).getLogicalModels();
    doReturn( mockObject ).when( mockLogicalModel ).getProperty( "AGILE_BI_GENERATED_SCHEMA" );

    List<String> response = dataSourceWizardService.getDSWDatasourceIds();
    assertEquals( datasourceList, response );

    verify( dataSourceWizardService, times( 1 ) ).getDSWDatasourceIds();
  }

  @Test
  public void testGetDSWDatasourceIdsError() throws Exception {
    LogicalModelSummary mockLogicalModelSummary  = mock( LogicalModelSummary.class );
    List<LogicalModelSummary> mockLogicalModelSummaryList = new ArrayList<LogicalModelSummary>();
    mockLogicalModelSummaryList.add( mockLogicalModelSummary );

    doReturn( mockLogicalModelSummaryList ).when( dataSourceWizardService.dswService ).getLogicalModels( null );
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( dataSourceWizardService.modelerService ).loadDomain( anyString() );

    List<String> response = dataSourceWizardService.getDSWDatasourceIds();
    assertEquals( null, response );

    verify( dataSourceWizardService, times( 1 ) ).getDSWDatasourceIds();
  }

  @Test
  public void testPublishDsw() throws Exception {
    String domainId = "domainId.xmi";
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = true;
    boolean checkConnection = false;

    XmiParser mockXmiParser = mock( XmiParser.class );
    Domain mockDomain = mock( Domain.class );
    InputStream mockInputStream = mock( InputStream.class );
    IPlatformImportBundle mockMetadataBundle = mock( IPlatformImportBundle.class );
    IPlatformImportBundle mockMondrianBundle = mock( IPlatformImportBundle.class );
    IPlatformImporter mockIPlatformImporter = mock( IPlatformImporter.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    String mockObject = "not null";
    String dswId = "dswId";

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    doReturn( true ).when( dataSourceWizardService ).hasManageAccessCheck();
    doReturn( true ).when( dataSourceWizardService ).endsWith( anyString(), anyString() );
    doReturn( mockXmiParser ).when( dataSourceWizardService ).createXmiParser();
    doReturn( mockDomain ).when( mockXmiParser ).parseXmi( metadataFile );
    doReturn( mockInputStream ).when( dataSourceWizardService ).toInputStreamWrapper( mockDomain, mockXmiParser );
    doReturn( mockMetadataBundle ).when( dataSourceWizardService ).createMetadataDswBundle( mockDomain, mockInputStream,
        overwrite, aclDto );
    doReturn( mockMondrianBundle ).when( dataSourceWizardService ).createMondrianDswBundle( mockDomain, aclDto );
    doReturn( mockIPlatformImporter ).when( dataSourceWizardService ).getIPlatformImporter();
    doReturn( mockIPentahoSession ).when( dataSourceWizardService ).getSession();

    String response = dataSourceWizardService.publishDsw( domainId, metadataFile, overwrite, checkConnection, aclDto );

    verify( dataSourceWizardService, times( 1 ) ).publishDsw( domainId, metadataFile, overwrite, checkConnection, aclDto );
    assertEquals( domainId, response );
  }

  @Test
  public void testPublishDswError() throws Exception {
    String domainId = "domainId";
    InputStream metadataFile = mock( InputStream.class );
    boolean overwrite = true;
    boolean checkConnection = false;

    XmiParser mockXmiParser = mock( XmiParser.class );
    Domain mockDomain = mock( Domain.class );
    InputStream mockInputStream = mock( InputStream.class );
    IPlatformImportBundle mockMetadataBundle = mock( IPlatformImportBundle.class );
    IPlatformImportBundle mockMondrianBundle = mock( IPlatformImportBundle.class );
    IPlatformImporter mockIPlatformImporter = mock( IPlatformImporter.class );
    IPentahoSession mockIPentahoSession = mock( IPentahoSession.class );
    String mockObject = "not null";
    String dswId = "dswId";

    //Test 1
    doReturn( false ).when( dataSourceWizardService ).hasManageAccessCheck();

    try {
      dataSourceWizardService.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
      fail();
    } catch ( PentahoAccessControlException e ) {
      //expected
    }

    //Test 2
    doReturn( true ).when( dataSourceWizardService ).hasManageAccessCheck();
    doReturn( false ).when( dataSourceWizardService ).endsWith( anyString(), anyString() );

    try {
      dataSourceWizardService.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
      fail();
    } catch ( IllegalArgumentException e ) {
      //expected
    }

    //Test 3
    doReturn( true ).when( dataSourceWizardService ).endsWith( anyString(), anyString() );
    try {
      dataSourceWizardService.publishDsw( domainId, null, overwrite, checkConnection, null );
      fail();
    } catch ( IllegalArgumentException e ) {
      //expected
    }

    //Test 4
    List<String> mockList = new ArrayList<String>();
    mockList.add( "string1" );
    doReturn( mockList ).when( dataSourceWizardService ).getOverwrittenDomains( domainId );
    try {
      dataSourceWizardService.publishDsw( domainId, metadataFile, false, checkConnection, null );
      fail();
    } catch ( Exception e ) {
      //expected
    }

    //Test 5
    doReturn( mockXmiParser ).when( dataSourceWizardService ).createXmiParser();
    RuntimeException mockException = mock( RuntimeException.class );
    doThrow( mockException ).when( mockXmiParser ).parseXmi( metadataFile );
    try {
      dataSourceWizardService.publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
      fail();
    } catch ( Exception e ) {
      //expected
    }

    //Test 6
    doReturn( mockDomain ).when( mockXmiParser ).parseXmi( metadataFile );
    doReturn( null ).when( dataSourceWizardService ).getMondrianDatasourceWrapper( mockDomain );
    try {
      dataSourceWizardService.publishDsw( domainId, metadataFile, overwrite, true, null );
      fail();
    } catch ( Exception e ) {
      //expected
    }

    verify( dataSourceWizardService, times( 3 ) ).publishDsw( domainId, metadataFile, overwrite, checkConnection, null );
    verify( dataSourceWizardService, times( 1 ) ).publishDsw( domainId, null, overwrite, checkConnection, null );
    verify( dataSourceWizardService, times( 1 ) ).publishDsw( domainId, metadataFile, false, checkConnection, null );
    verify( dataSourceWizardService, times( 1 ) ).publishDsw( domainId, metadataFile, overwrite, true, null );
  }

  @Test
  public void testGetDSWAcl() throws Exception {
    String domainId = "domainId";

    final RepositoryFileAcl acl = new RepositoryFileAcl.Builder( "owner" ).build();

    doReturn( true ).when( dataSourceWizardService ).canManageACL();
    when( dataSourceWizardService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) ).thenReturn(
        acl );
    final IUnifiedRepository repository = mock( IUnifiedRepository.class );
    final RepositoryFile repositoryFile = mock( RepositoryFile.class );
    when( repository.getFileById( anyString() ) ).thenReturn( repositoryFile );
    doReturn( new HashMap<String, InputStream>() ).when( dataSourceWizardService ).doGetDSWFilesAsDownload( domainId );

    final RepositoryFileAclDto aclDto = dataSourceWizardService.getDSWAcl( domainId );

    verify( dataSourceWizardService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertEquals( acl, new RepositoryFileAclAdapter().unmarshal( aclDto ) );
  }

  @Test
  public void testGetDSWAclNoAcl() throws Exception {
    String domainId = "domainId";

    doReturn( true ).when( dataSourceWizardService ).canManageACL();
    when( dataSourceWizardService.aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( domainId ) )
        .thenReturn( null );
    doReturn( new HashMap<String, InputStream>() ).when( dataSourceWizardService ).doGetDSWFilesAsDownload( domainId );

    final RepositoryFileAclDto aclDto = dataSourceWizardService.getDSWAcl( domainId );

    verify( dataSourceWizardService.aclAwarePentahoMetadataDomainRepositoryImporter ).getAclFor( eq( domainId ) );

    assertNull( aclDto );
  }

  @Test
  public void testSetMetadataDatasourceAcl() throws Exception {
    String domainId = "domainId.xmi";
    String domainIdWithoutExt = "domainId";

    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );

    doReturn( true ).when( dataSourceWizardService ).canManageACL();
    doReturn( new HashMap<String, InputStream>() ).when( dataSourceWizardService ).doGetDSWFilesAsDownload( domainId );

    dataSourceWizardService.setDSWAcl( domainId, aclDto );

    final RepositoryFileAcl acl = new RepositoryFileAclAdapter().unmarshal( aclDto );
    verify( dataSourceWizardService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
        eq( acl ) );
    verify( dataSourceWizardService.aclAwareMondrianCatalogService ).setAclFor( eq( domainIdWithoutExt ), eq( acl ) );
  }

  @Test
  public void testSetMetadataDatasourceAclNoAcl() throws Exception {
    String domainId = "domainId.xmi";
    String domainIdWithoutExt = "domainId";

    doReturn( true ).when( dataSourceWizardService ).canManageACL();
    doReturn( new HashMap<String, InputStream>() ).when( dataSourceWizardService ).doGetDSWFilesAsDownload( domainId );

    dataSourceWizardService.setDSWAcl( domainId, null );

    verify( dataSourceWizardService.aclAwarePentahoMetadataDomainRepositoryImporter ).setAclFor( eq( domainId ),
        (RepositoryFileAcl) isNull() );
    verify( dataSourceWizardService.aclAwareMondrianCatalogService ).setAclFor( eq( domainIdWithoutExt ),
        (RepositoryFileAcl) isNull() );
  }
}
