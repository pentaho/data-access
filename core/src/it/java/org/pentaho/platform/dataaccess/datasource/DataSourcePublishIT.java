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


package org.pentaho.platform.dataaccess.datasource;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.apache.commons.io.FileUtils;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAclVoter;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ISystemConfig;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.api.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.config.SystemConfig;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.IDataAccessPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.IDataAccessViewPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.AnalysisDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MetadataDatasourceService;
import org.pentaho.platform.dataaccess.datasource.api.resources.AnalysisResource;
import org.pentaho.platform.dataaccess.datasource.api.resources.DataSourceWizardResource;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataResource;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.security.acls.voter.PentahoAllowAllAclVoter;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogCache;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepositoryInfo;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.web.http.security.PentahoBasicAuthenticationEntryPoint;
import org.pentaho.platform.web.http.security.PentahoBasicProcessingFilter;
import org.pentaho.platform.web.http.api.resources.JaxbList;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.test.JerseyTest;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * Created by Aliaksei_Haidukou on 12/12/2014.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/jackrabbit-test-repo.xml",
    "classpath:/solutionACL/system/repository-test-override.spring.xml",
    "classpath:/solutionACL/system/importExport.xml", "classpath:/solutionACL/system/pentahoObjects.spring.xml" } )
public class iden DataSourcePublishIT extends JerseyTest implements ApplicationContextAware {

  private static final String USERNAME_SUZY = "suzy";
  private static final String USERNAME_TIFFANY = "tiffany";
  private static final String PASSWORD = "password";
  private static final String AUTHENTICATED_ROLE_NAME = "Authenticated";

  private static final String DATA_ACCESS_API_DATASOURCE_METADATA = "data-access/api/datasource/metadata/";
  private static final String DATA_ACCESS_API_DATASOURCE_DSW = "data-access/api/datasource/dsw/";

  private static ResourceConfig config = new ResourceConfig()
      .register( JacksonFeature.class )
      .register( AnalysisResource.class )
      .register( MetadataResource.class )
      .register( DataSourceWizardResource.class )
      .register( AnalysisDatasourceService.class )
      .register( MetadataDatasourceService.class );
  private static ServletDeploymentContext webAppDescriptor = ServletDeploymentContext.forServlet( new ServletContainer( config ) )
      .addFilter( TestBasicProcessingFilter.class, "basicProcessingFilter" )
      .contextPath( "plugin" )
      .build();

  private static AuthenticationManager authenticationManager;
  private ApplicationContext applicationContext;
  private ITenant defaultTenant;
  private DefaultUnifiedRepositoryBase repositoryBase;
  private String singleTenantAdminUserName;
  public static final String DATA_ACCESS_API_DATASOURCE_ANALYSIS = "data-access/api/datasource/analysis/";

  public DataSourcePublishIT() throws TestContainerException {
    repositoryBase = new DefaultUnifiedRepositoryBase() {
      @Override
      protected String getSolutionPath() {
        return "target/test-classes/solutionACL";
      }

      @Override public void login( String username, ITenant tenant, String[] roles ) {
        super.login( username, tenant, roles );
        try {
          PentahoSystem.get( IMetadataDomainRepository.class ).flushDomains();
          PentahoSystem.get( IMondrianCatalogService.class ).reInit( PentahoSessionHolder.getSession() );
        } catch ( Exception e ) {
          // do nothing
        }
      }
    };
  }

  @Override
  protected DeploymentContext configureDeployment() {
    return webAppDescriptor;
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  public static class TestBasicProcessingFilter extends PentahoBasicProcessingFilter {
    public TestBasicProcessingFilter() throws Exception {
      super( authenticationManager, createEntryPoint() );
    }

    private static PentahoBasicAuthenticationEntryPoint createEntryPoint() throws Exception {
      PentahoBasicAuthenticationEntryPoint entryPoint = new PentahoBasicAuthenticationEntryPoint();
      entryPoint.setRealmName( "Pentaho Realm" );
      entryPoint.afterPropertiesSet();
      return entryPoint;
    }

  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    // folder cannot be deleted at teardown shutdown hooks have not yet necessarily completed
    // parent folder must match jcrRepository.homeDir bean property in repository-test-override.spring.xml
    FileUtils.deleteDirectory( new File( "/tmp/repository-future/jackrabbit-test-TRUNK" ) );
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );

    // register repository spring context for correct work of <pen:list>
    final StandaloneSpringPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader( appCtx );
    xmlReader.loadBeanDefinitions( "classpath:/repository.spring.xml" );
    xmlReader.loadBeanDefinitions( "classpath:/solutionACL/system/repository-test-override.spring.xml" );
    xmlReader.loadBeanDefinitions( "classpath:/solutionACL/system/importExport.xml" );
    xmlReader.loadBeanDefinitions( "classpath:/solutionACL/system/pentahoObjects.spring.xml" );
    xmlReader.loadBeanDefinitions( "classpath:/jackrabbit-test-repo.xml" );
    pentahoObjectFactory.init( "target/test-classes/solutionACL", appCtx );
    PentahoSystem.registerObjectFactory( pentahoObjectFactory );

    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
    FileUtils.deleteDirectory( new File( "/tmp/data-access/jackrabbit-test-TRUNK" ) );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL );
  }

  @Before
  public void setUp() throws Exception {
    repositoryBase.setUp();

    repositoryBase.loginAsRepositoryAdmin();

    defaultTenant = repositoryBase.createTenant( repositoryBase.getSystemTenant(), TenantUtils.getDefaultTenant() );

    singleTenantAdminUserName = (String) applicationContext.getBean( "singleTenantAdminUserName" );
    repositoryBase.createUser( defaultTenant, singleTenantAdminUserName, PASSWORD, new String[] { repositoryBase.getTenantAdminRoleName() } );
    final String singleTenantAuthenticatedAuthorityName =
        (String) applicationContext.getBean( "singleTenantAuthenticatedAuthorityName" );
    repositoryBase.createUser( defaultTenant, USERNAME_SUZY, PASSWORD, new String[] { singleTenantAuthenticatedAuthorityName } );
    repositoryBase.createUser( defaultTenant, USERNAME_TIFFANY, PASSWORD, new String[] { singleTenantAuthenticatedAuthorityName } );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant, new String[] { repositoryBase.getTenantAdminRoleName() } );

    final IUnifiedRepository repo = PentahoSystem.get( IUnifiedRepository.class );

    String etcID = String.valueOf( repo.getFile( ClientRepositoryPaths.getEtcFolderPath() ).getId() );
    repo.createFolder( etcID, new RepositoryFile.Builder( MondrianCatalogHelper.MONDRIAN_DATASOURCE_FOLDER ).folder( true ).build(), "initialization" );
    repo.createFolder( etcID, new RepositoryFile.Builder( PentahoMetadataDomainRepositoryInfo.getMetadataFolderName() ).folder( true ).build(), "initialization" );

    final MicroPlatform mp = repositoryBase.getMp();
    mp.define( IMondrianCatalogService.class, MondrianCatalogHelper.class );
    mp.define( ISystemConfig.class, SystemConfig.class );
    mp.defineInstance( IPlatformMimeResolver.class, applicationContext.getBean( "IPlatformImportMimeResolver" ) );
    mp.defineInstance( IPlatformImporter.class, applicationContext.getBean( "IPlatformImporter" ) );
    mp.defineInstance( IPasswordService.class, applicationContext.getBean( "IPasswordService", IPasswordService.class ) );

    ICacheManager cacheManager = mock( ICacheManager.class );
    when( cacheManager.getFromRegionCache( anyString(), any() ) ).thenReturn( new MondrianCatalogCache() );
    mp.defineInstance( ICacheManager.class, cacheManager );
    mp.defineInstance( IMetadataDomainRepository.class, applicationContext.getBean( "IMetadataDomainRepository" ) );

    final PluginResourceLoader pluginResourceLoader = (PluginResourceLoader) applicationContext.getBean( "IPluginResourceLoader" );
    pluginResourceLoader.setRootDir( new File( "target/test-classes/solutionACL/system/data-access" ) );
    mp.defineInstance( IPluginResourceLoader.class, pluginResourceLoader );
    mp.define( IDataAccessPermissionHandler.class, SimpleDataAccessPermissionHandler.class );
    mp.define( IDataAccessViewPermissionHandler.class, SimpleDataAccessViewPermissionHandler.class );
    mp.defineInstance( IAclVoter.class, new PentahoAllowAllAclVoter() );

    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
    super.setUp();
  }

  @After
  public void tearDown() throws Exception {
    repositoryBase.loginAsRepositoryAdmin();
    PentahoSystem.get( IMetadataDomainRepository.class ).flushDomains();

    repositoryBase.cleanupUserAndRoles( defaultTenant );
    applicationContext = null;
    defaultTenant = null;

    repositoryBase.tearDown();

    super.tearDown();
  }

  @Test
  public void testPublishAnalysis() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String catalogID = "FoodMart";
    final InputStream uploadAnalysis = new FileInputStream( "target/test-classes/schema.xml" );
    final boolean overwrite = true;
    final boolean xmlaEnabledFlag = false;
    final String parameters = "DataSource=" + catalogID + ";EnableXmla=" + xmlaEnabledFlag + ";overwrite=" + overwrite;

    final RepositoryFileAclDto acl = generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER );

    AnalysisResource analysisResource = new AnalysisResource();
    Response postAnalysis = analysisResource.importMondrianSchema( uploadAnalysis,
      FormDataContentDisposition.name( "uploadAnalysis" ).fileName( "schema.xml" )
        .size( uploadAnalysis.available() ).build(), catalogID, null, catalogID,
      String.valueOf( overwrite ), String.valueOf( xmlaEnabledFlag ), parameters, null );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    assertEquals( Response.Status.OK.getStatusCode(), analysisResource.doSetAnalysisDatasourceAcl( catalogID, acl ).getStatus() );

    final RepositoryFileAclDto savedACL = analysisResource.doGetAnalysisDatasourceAcl( catalogID );
    assertNotNull( savedACL );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( analysisResource, catalogID, true );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( analysisResource, catalogID, false );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final Response changeACL = analysisResource.doSetAnalysisDatasourceAcl( catalogID,
      generateACL( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( analysisResource, catalogID, true );
  }

  @Test
  public void testAnalysis_ACL() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String catalogID = "FoodMart";
    final InputStream uploadAnalysis = new FileInputStream( "target/test-classes/schema.xml" );
    final boolean overwrite = true;
    final boolean xmlaEnabledFlag = false;
    final String parameters = "DataSource=" + catalogID + ";EnableXmla=" + xmlaEnabledFlag + ";overwrite=" + overwrite;

    AnalysisResource analysisResource = new AnalysisResource();
    assertAnalysisAclStatus( Response.Status.CONFLICT.getStatusCode(), analysisResource, catalogID );

    Response postAnalysis = analysisResource.importMondrianSchema( uploadAnalysis,
      FormDataContentDisposition.name( "uploadAnalysis" ).fileName( "schema.xml" )
        .size( uploadAnalysis.available() ).build(), catalogID, null, catalogID,
      String.valueOf( overwrite ), String.valueOf( xmlaEnabledFlag ), parameters, null );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    assertAnalysisAclStatus( Response.Status.NOT_FOUND.getStatusCode(), analysisResource, catalogID );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( analysisResource, catalogID, true );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final Response changeACL = analysisResource.doSetAnalysisDatasourceAcl( catalogID,
      generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( analysisResource, catalogID, true );

    assertAnalysisAclStatus( Response.Status.UNAUTHORIZED.getStatusCode(), analysisResource, catalogID );
    assertAnalysisAclStatus( Response.Status.UNAUTHORIZED.getStatusCode(), analysisResource, catalogID + "_not_exist" );
  }

    private void assertAnalysisAclStatus( int expectedStatus, AnalysisResource analysisResource, String catalogId ) {
    try {
      analysisResource.doGetAnalysisDatasourceAcl( catalogId );
    } catch ( WebApplicationException e ) {
      assertEquals( expectedStatus, e.getResponse().getStatus() );
      return;
    }
    throw new AssertionError( "Expected analysis ACL lookup to fail with status " + expectedStatus );
    }

  private void checkAnalysis( WebTarget webTarget, String catalogID, boolean hasAccess ) {
    final JaxbList analysisDatasourceIds = webTarget
          .path( DATA_ACCESS_API_DATASOURCE_ANALYSIS + "ids" ).request()
          .get( JaxbList.class );

    final List list = analysisDatasourceIds.getList();
    if ( hasAccess ) {
      assertTrue( list != null && list.contains( catalogID ) );
    } else if ( list != null ) {
      assertFalse( list.contains( catalogID ) );
    }
  }

  private void checkAnalysis( AnalysisResource analysisResource, String catalogID, boolean hasAccess ) {
    final List<String> list = analysisResource.getAnalysisDatasourceIds().getList();
    if ( hasAccess ) {
      assertTrue( list != null && list.contains( catalogID ) );
    } else if ( list != null ) {
      assertFalse( list.contains( catalogID ) );
    }
  }

  @Test
  public void testPublishMetadata() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String domainID = "domainID.xmi";
    final FileInputStream metadataFile = new FileInputStream( "target/test-classes/Sample_SQL_Query.xmi" );
    final String overwrite = "true";
    final RepositoryFileAclDto acl = generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER );

    MetadataResource metadataResource = new MetadataResource();
    Response postAnalysis = metadataResource.doImportMetadataDatasource( domainID, metadataFile,
      FormDataContentDisposition.name( "metadataFile" ).fileName( "Sample_SQL_Query.xmi" )
        .size( metadataFile.available() ).build(), overwrite, null, null, null );
    assertEquals( 3, postAnalysis.getStatus() );

    assertEquals( Response.Status.OK.getStatusCode(), metadataResource.doSetMetadataAcl( domainID, acl ).getStatus() );

    final RepositoryFileAclDto savedACL = metadataResource.doGetMetadataAcl( domainID );
    assertNotNull( savedACL );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( metadataResource, domainID, true );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( metadataResource, domainID, false );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final Response changeACL = metadataResource.doSetMetadataAcl( domainID,
      generateACL( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( metadataResource, domainID, true );
  }

  @Test
  public void testMetadata_ACL() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String domainID = "domainID.xmi";
    final FileInputStream metadataFile = new FileInputStream( "target/test-classes/Sample_SQL_Query.xmi" );
    final String overwrite = "true";

    MetadataResource metadataResource = new MetadataResource();

    assertMetadataAclStatus( Response.Status.CONFLICT.getStatusCode(), metadataResource, domainID );

    Response postAnalysis = metadataResource.doImportMetadataDatasource( domainID, metadataFile,
        FormDataContentDisposition.name( "metadataFile" ).fileName( "Sample_SQL_Query.xmi" )
            .size( metadataFile.available() ).build(), overwrite, null, null, null );
    assertEquals( 3, postAnalysis.getStatus() );

    assertMetadataAclStatus( Response.Status.NOT_FOUND.getStatusCode(), metadataResource, domainID );

    checkMetadata( metadataResource, domainID, true );
    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( metadataResource, domainID, true );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final Response changeACL = metadataResource.doSetMetadataAcl( domainID,
        generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( metadataResource, domainID, true );

    assertMetadataAclStatus( Response.Status.UNAUTHORIZED.getStatusCode(), metadataResource, domainID );
    assertMetadataAclStatus( Response.Status.UNAUTHORIZED.getStatusCode(), metadataResource, domainID + "_not_exist" );
  }

  private void assertMetadataAclStatus( int expectedStatus, MetadataResource metadataResource, String domainId ) {
    try {
      metadataResource.doGetMetadataAcl( domainId );
    } catch ( WebApplicationException e ) {
      assertEquals( expectedStatus, e.getResponse().getStatus() );
      return;
    }
    throw new AssertionError( "Expected metadata ACL lookup to fail with status " + expectedStatus );
  }

  private void checkMetadata( MetadataResource metadataResource, String domainID, boolean hasAccess ) {
    final List<String> list = metadataResource.getMetadataDatasourceIds().getList();
    if ( hasAccess ) {
      assertTrue( list != null && list.contains( domainID ) );
    } else if ( list != null ) {
      assertFalse( list.contains( domainID ) );
    }
  }

  private void checkMetadata( WebTarget webTarget, String domainID, boolean hasAccess ) {
    final JaxbList metadataDatasourceIds = webTarget
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + "ids" )
        .request()
        .get( JaxbList.class );

    final List list = metadataDatasourceIds.getList();
    if ( hasAccess ) {
      assertTrue( list != null && list.contains( domainID ) );
    } else if ( list != null ) {
      assertFalse( list.contains( domainID ) );
    }
  }

  @Test
  public void testPublishDSW() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String domainID = "test.xmi";
    final FileInputStream metadataFile = new FileInputStream( "target/test-classes/test.xmi" );
    final boolean overwrite = true;
    final RepositoryFileAclDto acl = generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER );

    DataSourceWizardResource dswResource = new DataSourceWizardResource();
    Response postAnalysis = dswResource.publishDsw( domainID, metadataFile, overwrite, false, null );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    assertEquals( Response.Status.OK.getStatusCode(), dswResource.doSetDSWAcl( domainID, acl ).getStatus() );

    final RepositoryFileAclDto savedACL = dswResource.doGetDSWAcl( domainID );
    assertNotNull( savedACL );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( dswResource, domainID, true );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( dswResource, domainID, false );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final Response changeACL = dswResource.doSetDSWAcl( domainID,
      generateACL( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( dswResource, domainID, true );
  }

  @Test
  public void testDSW_ACL() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String domainID = "test.xmi";
    final FileInputStream metadataFile = new FileInputStream( "target/test-classes/test.xmi" );
    final boolean overwrite = true;

    DataSourceWizardResource dswResource = new DataSourceWizardResource();
    assertDswAclStatus( Response.Status.CONFLICT.getStatusCode(), dswResource, domainID );

    Response postAnalysis = dswResource.publishDsw( domainID, metadataFile, overwrite, false, null );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    assertDswAclStatus( Response.Status.NOT_FOUND.getStatusCode(), dswResource, domainID );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( dswResource, domainID, true );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final Response setSuzyACL = dswResource.doSetDSWAcl( domainID,
      generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER ) );
    assertEquals( Response.Status.OK.getStatusCode(), setSuzyACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( dswResource, domainID, true );

    assertDswAclStatus( Response.Status.UNAUTHORIZED.getStatusCode(), dswResource, domainID );
    assertDswAclStatus( Response.Status.UNAUTHORIZED.getStatusCode(), dswResource, domainID + "_not_exist" );
  }

    private void assertDswAclStatus( int expectedStatus, DataSourceWizardResource dswResource, String domainId ) {
    try {
      dswResource.doGetDSWAcl( domainId );
    } catch ( WebApplicationException e ) {
      assertEquals( expectedStatus, e.getResponse().getStatus() );
      return;
    }
    throw new AssertionError( "Expected DSW ACL lookup to fail with status " + expectedStatus );
    }

  private void checkDSW( WebTarget webTarget, String domainID, boolean hasAccess ) {
    final JaxbList dswIds = webTarget
        .path( DATA_ACCESS_API_DATASOURCE_DSW + "ids" )
        .request()
        .get( JaxbList.class );

    final List list = dswIds.getList();
    if ( hasAccess ) {
      assertTrue( list != null && list.contains( domainID ) );
    } else if ( list != null ) {
      assertFalse( list.contains( domainID ) );
    }
  }

  private void checkDSW( DataSourceWizardResource dswResource, String domainID, boolean hasAccess ) {
    final List<String> list = dswResource.getDSWDatasourceIds().getList();
    if ( hasAccess ) {
      assertTrue( list != null && list.contains( domainID ) );
    } else if ( list != null ) {
      assertFalse( list.contains( domainID ) );
    }
  }

  private RepositoryFileAclDto generateACL( String userOrRole, RepositoryFileSid.Type type ) {
    final RepositoryFileAclDto aclDto = new RepositoryFileAclDto();
    aclDto.setOwnerType( RepositoryFileSid.Type.USER.ordinal() );
    aclDto.setOwner( singleTenantAdminUserName );
    aclDto.setEntriesInheriting( false );

    final ArrayList<RepositoryFileAclAceDto> aces = new ArrayList<>();
    final RepositoryFileAclAceDto aceDto = new RepositoryFileAclAceDto();
    aceDto.setRecipient( userOrRole );
    aceDto.setRecipientType( type.ordinal() );

    final ArrayList<Integer> permissions = new ArrayList<>();
    permissions.add( RepositoryFilePermission.ALL.ordinal() );
    aceDto.setPermissions( permissions );
    aces.add( aceDto );

    aclDto.setAces( aces );
    return aclDto;
  }

  private static String marshalACL( RepositoryFileAclDto acl ) throws JAXBException {
    JAXBContext context = JAXBContext.newInstance( RepositoryFileAclDto.class );
    Marshaller marshaller = context.createMarshaller();
    StringWriter sw = new StringWriter();
    marshaller.marshal( acl, sw );

    return sw.toString();
  }

  private static String serializeAclAsJson( RepositoryFileAclDto acl ) throws Exception {
    return new ObjectMapper().writeValueAsString( acl );
  }

  @Override
  public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
    this.applicationContext = applicationContext;
    authenticationManager = applicationContext.getBean( "authenticationManager", AuthenticationManager.class );
    repositoryBase.setApplicationContext( applicationContext );
  }
}
