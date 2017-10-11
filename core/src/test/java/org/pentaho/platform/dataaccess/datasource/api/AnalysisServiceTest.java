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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api;

import com.google.common.collect.Sets;
import com.sun.jersey.core.header.FormDataContentDisposition;
import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.IDataAccessPermissionHandler;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.test.platform.engine.core.MicroPlatform;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.*;

public class AnalysisServiceTest {

  private static IMetadataDomainRepository metadataRepository;
  private static IPlatformImporter importer;
  private static IAuthorizationPolicy policy;
  private static IAclAwareMondrianCatalogService catalogService;
  private static IDataAccessPermissionHandler permissionHandler;
  private static final RepositoryFileAclDto acl = new RepositoryFileAclDto();

  private AnalysisService analysisService;

  @BeforeClass
  public static void initPlatform() throws Exception {
    MicroPlatform platform = new MicroPlatform();
    metadataRepository = mock( IMetadataDomainRepository.class );
    platform.defineInstance( IMetadataDomainRepository.class, metadataRepository );
    importer = mock( IPlatformImporter.class );
    platform.defineInstance( IPlatformImporter.class, importer );
    policy = mock( IAuthorizationPolicy.class );
    platform.defineInstance( IAuthorizationPolicy.class, policy );
    catalogService = mock( IAclAwareMondrianCatalogService.class );
    platform.defineInstance( IMondrianCatalogService.class, catalogService );
    permissionHandler = mock( IDataAccessPermissionHandler.class );
    platform.defineInstance( IDataAccessPermissionHandler.class, permissionHandler );
    final IUnifiedRepository unifiedRepository = new FileSystemBackedUnifiedRepository( "target/test-classes/solution1" );
    platform.defineInstance( IUnifiedRepository.class, unifiedRepository );
    platform.start();
    acl.setOwner( "owner" );
    acl.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );
  }

  @Before
  public void setUp() {
    analysisService = new AnalysisService();
  }

  @After
  public void tearDown() throws Exception {
    Mockito.reset( metadataRepository, importer, policy, catalogService, permissionHandler );
  }

  private void policyAccess() {
    when( policy.isAllowed( any( String.class ) ) ).thenReturn( true );
  }

  private void allAccess() {
    policyAccess();
    when( permissionHandler.hasDataAccessPermission( any( IPentahoSession.class ) ) ).thenReturn( true );
  }

  private InputStream getSchemaAsStream() {
    return getClass().getResourceAsStream( "schema.xml" );
  }

  @Test
  public void testImportingSchemaRemovesExistingAnnotationsByDefault() throws Exception {
    MondrianCatalog otherCatalog = new MondrianCatalog( "other", "", "", null );
    MondrianCatalog salesCatalog = new MondrianCatalog( "sales", "", "", null );
    when( catalogService.listCatalogs( any( IPentahoSession.class ), eq( false ) ) )
        .thenReturn( Arrays.asList( otherCatalog, salesCatalog ) );
    putMondrianSchemaWithSchemaFileName( "stubFileName", "overwrite=true" );
    verify( importer ).importFile( argThat( matchBundle( true, acl ) ) );
    verify( catalogService ).removeCatalog( eq( "sales" ), any( IPentahoSession.class ) );
  }

  @Test
  public void testImportingSkipsRemoveWhenNotPresent() throws Exception {
    MondrianCatalog otherCatalog = new MondrianCatalog( "other", "", "", null );
    when( catalogService.listCatalogs( any( IPentahoSession.class ), eq( false ) ) )
        .thenReturn( Collections.singletonList( otherCatalog ) );
    putMondrianSchemaWithSchemaFileName( "stubFileName", "overwrite=true" );
    verify( importer ).importFile( argThat( matchBundle( true, acl ) ) );
    verify( catalogService, never() ).removeCatalog( eq( "sales" ), any( IPentahoSession.class ) );
  }

  @Test
  public void testImportingSchemaCanRetainAnnotations() throws Exception {
    putMondrianSchemaWithSchemaFileName( "stubFileName" );
    verify( importer ).importFile( argThat( matchBundle( true, acl ) ) );
    verify( catalogService, Mockito.times( 0 ) ).removeCatalog( eq( "sales" ), any( IPentahoSession.class ) );
  }

  @Test
  public void testImportingSchemaWillNotOverwrite() throws Exception {
    putMondrianSchemaWithSchemaFileName( "stubFileName", "overwrite=false;retainInlineAnnotations=true" );
    verify( importer ).importFile( argThat( matchBundle( false, acl ) ) );
    verify( catalogService, never() ).removeCatalog( eq( "sales" ), any( IPentahoSession.class ) );
  }

  @Test
  public void testPutMondrianSchemaNoPublishPermissions() throws Exception {
    analysisService = new AnalysisService() {
      @Override
      protected void accessValidation() throws PentahoAccessControlException {
        throw new PentahoAccessControlException();
      }
    };

    try {
      putMondrianSchemaWithSchemaFileName( "stubFileName" );
      fail();
    } catch ( PentahoAccessControlException e ) {
      // expected
    }
    verify( importer, never() ).importFile( any() );
    verify( catalogService, never() ).removeCatalog( anyString(), any( IPentahoSession.class ) );
  }

  private BaseMatcher<IPlatformImportBundle> matchBundle( final boolean overwrite, final RepositoryFileAclDto acl ) {
    return new BaseMatcher<IPlatformImportBundle>() {
      @Override
      public void describeTo( final Description description ) {
      }

      @Override
      public boolean matches( final Object item ) {
        RepositoryFileImportBundle bundle = (RepositoryFileImportBundle) item;
        return bundle.getName().equals( "sales" ) && bundle.isOverwriteInRepository() == overwrite && bundle.getAcl()
            .equals( new RepositoryFileAclAdapter().unmarshal( acl ) );
      }
    };
  }

  @Test
  public void testDoGetAnalysisFilesAsDownload() throws Exception {
    allAccess();
    final Map<String, InputStream> sampleData = analysisService.doGetAnalysisFilesAsDownload( "SampleData" );
    assertEquals( 1, sampleData.size() );
    final FileInputStream inputStream = new FileInputStream( "target/test-classes/solution1/etc/mondrian/SampleData/schema.xml" );
    assertEquals( IOUtils.toString( inputStream ), IOUtils.toString( sampleData.get( "SampleData.mondrian.xml" ) ) );
  }

  @Test( expected = PentahoAccessControlException.class )
  public void testDoGetAnalysisFilesAsDownloadError() throws Exception {
    when( policy.isAllowed( any( String.class ) ) ).thenReturn( true );
    when( permissionHandler.hasDataAccessPermission( any( IPentahoSession.class ) ) ).thenReturn( false );
    analysisService.doGetAnalysisFilesAsDownload( "SampleData" );
  }

  @Test
  public void testRemoveAnalysisRequiresMultiplePermissions() throws Exception {
    testRemove( true, true, true );
    testRemove( false, false, false );
    testRemove( true, true, false );
    testRemove( true, false, true );
    testRemove( false, true, true );
    testRemove( false, false, true );
    testRemove( true, false, false );
  }

  private void testRemove( final boolean hasRead, final boolean hasCreate, final boolean hasAdmin )
      throws PentahoAccessControlException {
    when( policy.isAllowed( RepositoryReadAction.NAME ) ).thenReturn( hasRead );
    when( policy.isAllowed( RepositoryCreateAction.NAME ) ).thenReturn( hasCreate );
    when( policy.isAllowed( AdministerSecurityAction.NAME ) ).thenReturn( hasAdmin );
    if ( hasRead && hasCreate && hasAdmin ) {
      when( policy.isAllowed( any( String.class ) ) ).thenReturn( true );
    }
    try {
      analysisService.removeAnalysis( "analysisId" );
      if ( hasRead && hasCreate && hasAdmin ) {
        verify( catalogService ).removeCatalog( eq( "analysisId" ), any( IPentahoSession.class ) );
      } else {
        fail( "should have got exception" );
      }
    } catch ( PentahoAccessControlException e ) {
      if ( hasRead && hasCreate && hasAdmin ) {
        fail( "should not have got exception" );
      } else {
        verify( catalogService, never() ).removeCatalog( any( String.class ), any( IPentahoSession.class ) );
      }
    }
    Mockito.reset( policy, catalogService );
  }

  @Test
  public void testGetAnalysisDatasourceIds() throws Exception {
    final MondrianCatalog foodmartCatalog = new MondrianCatalog( "foodmart", "info", "file:///place",
        new MondrianSchema( "foodmart", Collections.emptyList() ) );
    final MondrianCatalog foodmartCatalog2 = new MondrianCatalog( "foodmart2", "info", "file:///place",
        new MondrianSchema( "foodmart2", Collections.emptyList() ) );
    final List<MondrianCatalog> catalogs = Arrays.asList( foodmartCatalog, foodmartCatalog2 );
    doReturn( catalogs ).when( catalogService ).listCatalogs( any( IPentahoSession.class ), eq( false ) );
    final HashSet<String> domainIds = Sets.newHashSet( "foodmart.xmi", "sample.xmi" );
    doReturn( domainIds ).when( metadataRepository ).getDomainIds();
    final List<String> response = analysisService.getAnalysisDatasourceIds();
    assertEquals( Collections.singletonList( "foodmart2" ), response );
  }

  @Test
  public void testGetAnalysisDatasourceAcl() throws Exception {
    allAccess();
    final String catalogName = "SampleData";
    final RepositoryFileAcl expectedAcl = new RepositoryFileAcl.Builder( "owner" ).build();
    when( catalogService.getAclFor( catalogName ) ).thenReturn( expectedAcl );
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( catalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn( mondrianCatalog );
    final RepositoryFileAclDto actualAcl = analysisService.getAnalysisDatasourceAcl( catalogName );
    assertEquals( expectedAcl, new RepositoryFileAclAdapter().unmarshal( actualAcl ) );
  }

  @Test
  public void testGetAnalysisDatasourceAclNoAcl() throws Exception {
    allAccess();
    final String catalogName = "catalogName";
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( catalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn( mondrianCatalog );
    when( catalogService.getAclFor( catalogName ) ).thenReturn( null );
    final RepositoryFileAclDto aclDto = analysisService.getAnalysisDatasourceAcl( catalogName );
    assertNull( aclDto );
  }

  @Test
  public void testSetAnalysisDatasourceAcl() throws Exception {
    allAccess();
    final String catalogName = "catalogName";
    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwner( "owner" );
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( catalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn( mondrianCatalog );
    analysisService.setAnalysisDatasourceAcl( catalogName, aclDto );
    verify( catalogService ).setAclFor( eq( catalogName ), eq( new RepositoryFileAclAdapter().unmarshal( aclDto ) ) );
  }

  @Test
  public void testSetAnalysisDatasourceAclNoAcl() throws Exception {
    allAccess();
    String catalogName = "catalogName";
    final MondrianCatalog mondrianCatalog = mock( MondrianCatalog.class );
    when( catalogService.getCatalog( eq( catalogName ), any( IPentahoSession.class ) ) ).thenReturn( mondrianCatalog );
    analysisService.setAnalysisDatasourceAcl( catalogName, null );
    verify( catalogService ).setAclFor( eq( catalogName ), (RepositoryFileAcl) isNull() );
  }

  @Test( expected = PlatformImportException.class )
  public void testPutNullMondrianSchema() throws Exception {
    putMondrianSchemaWithSchemaFileName( null );
  }

  @Test( expected = PlatformImportException.class )
  public void testPutXmiMondrianSchema() throws Exception {
    putMondrianSchemaWithSchemaFileName( "sample.xmi" );
  }

  @Test
  public void testPutEmptyMondrianSchema() throws Exception {
    putMondrianSchemaWithSchemaFileName( "" );
  }

  @Test
  public void testGetSchemaName() throws Exception {
    AnalysisService analysis = Mockito.spy( new AnalysisService() );
    String xml = "<Schema name=\"Test4\"></Schema>";
    InputStream schema = new ByteArrayInputStream( xml.getBytes() );
    doReturn( new com.sun.xml.stream.ZephyrParserFactory() ).when( analysis ).getXMLInputFactory();
    String domainId = analysis.getSchemaName( null, schema );
    assertEquals( "Test4", domainId);
  }
  private void putMondrianSchemaWithSchemaFileName( String fileName ) throws Exception {
    String params = "overwrite=true;retainInlineAnnotations=true";
    putMondrianSchemaWithSchemaFileName( fileName, params );
  }

  private void putMondrianSchemaWithSchemaFileName( String fileName, String parameters ) throws Exception {
    policyAccess();
    FormDataContentDisposition schemaFileInfoMock = mock( FormDataContentDisposition.class );
    if ( fileName != null ) {
      when( schemaFileInfoMock.getFileName() ).thenReturn( fileName );
    }
    InputStream schema = getSchemaAsStream();
    analysisService.putMondrianSchema( schema, schemaFileInfoMock, "sample", null, "sample", true, false, parameters, acl );
  }
}
