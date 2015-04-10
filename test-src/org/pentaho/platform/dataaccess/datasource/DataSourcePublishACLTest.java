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

package org.pentaho.platform.dataaccess.datasource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
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
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.mimetype.IPlatformMimeResolver;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFilePermission;
import org.pentaho.platform.api.repository2.unified.RepositoryFileSid;
import org.pentaho.platform.config.SystemConfig;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.IDataAccessPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.IDataAccessViewPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.TenantUtils;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepositoryInfo;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.ClientRepositoryPaths;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepositoryBase;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAceDto;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.web.http.api.resources.JaxbList;
import org.pentaho.platform.web.http.filters.PentahoRequestContextFilter;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerException;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly.web.GrizzlyWebTestContainerFactory;

/**
 * Created by Aliaksei_Haidukou on 12/12/2014.
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = { "classpath:/repository.spring.xml",
    "classpath:/solutionACL/system/repository-test-override.spring.xml",
    "classpath:/solutionACL/system/importExport.xml", "classpath:/solutionACL/system/pentahoObjects.spring.xml" } )
public class DataSourcePublishACLTest extends JerseyTest implements ApplicationContextAware {
  private static final String USERNAME_SUZY = "suzy";
  private static final String USERNAME_TIFFANY = "tiffany";
  private static final String PASSWORD = "password";
  private static final String AUTHENTICATED_ROLE_NAME = "Authenticated";

  private static final String DATA_ACCESS_API_DATASOURCE_METADATA = "data-access/api/datasource/metadata/";
  private static final String DATA_ACCESS_API_DATASOURCE_DSW = "data-access/api/datasource/dsw/";

  private static WebAppDescriptor webAppDescriptor = new WebAppDescriptor.Builder(
      new String[] {
          "org.pentaho.platform.dataaccess.datasource.api.resources",
          "org.pentaho.platform.dataaccess.datasource.wizard.service.impl"
      } ).contextPath( "plugin" ).addFilter(
      PentahoRequestContextFilter.class, "pentahoRequestContextFilter" ).build();

  private ApplicationContext applicationContext;
  private ITenant defaultTenant;
  private DefaultUnifiedRepositoryBase repositoryBase;
  private String singleTenantAdminUserName;
  public static final String DATA_ACCESS_API_DATASOURCE_ANALYSIS = "data-access/api/datasource/analysis/";

  public DataSourcePublishACLTest() throws TestContainerException {
    repositoryBase = new DefaultUnifiedRepositoryBase() {
      @Override
      protected String getSolutionPath() {
        return "test-src/solutionACL";
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
  protected AppDescriptor configure() {
    return webAppDescriptor;
  }

  @Override
  protected TestContainerFactory getTestContainerFactory() {
    return new GrizzlyWebTestContainerFactory();
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    DefaultUnifiedRepositoryBase.setUpClass();
    FileUtils.deleteDirectory( new File( "/tmp/data-access/jackrabbit-test-TRUNK" ) );
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    DefaultUnifiedRepositoryBase.tearDownClass();
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

    mp.defineInstance( ICacheManager.class, applicationContext.getBean( "ICacheManager" ) );
    mp.defineInstance( IMetadataDomainRepository.class, applicationContext.getBean( "IMetadataDomainRepository" ) );

    final PluginResourceLoader pluginResourceLoader = (PluginResourceLoader) applicationContext.getBean( "IPluginResourceLoader" );
    pluginResourceLoader.setRootDir( new File( "test-src/solutionACL/system/data-access" ) );
    mp.defineInstance( IPluginResourceLoader.class, pluginResourceLoader );
    mp.define( IDataAccessPermissionHandler.class, SimpleDataAccessPermissionHandler.class );
    mp.define( IDataAccessViewPermissionHandler.class, SimpleDataAccessViewPermissionHandler.class );
    mp.defineInstance( IAclVoter.class, applicationContext.getBean( "IAclVoter" ) );

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
    final InputStream uploadAnalysis = new FileInputStream( "test-res/schema.xml" );
    final boolean overwrite = true;
    final boolean xmlaEnabledFlag = false;
    final String parameters = "DataSource=" + catalogID + ";EnableXmla=" + xmlaEnabledFlag + ";overwrite=" + overwrite;

    final RepositoryFileAclDto acl = generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER );

    MultiPart part = new FormDataMultiPart()
        .field( "catalogName", catalogID )
        .field( "datasourceName", catalogID )
        .field( "overwrite", String.valueOf( overwrite ) )
        .field( "xmlaEnabledFlag", String.valueOf( xmlaEnabledFlag ) )
        .field( "acl", marshalACL( acl ) )
        .field( "parameters", parameters )
        .bodyPart(
            new FormDataBodyPart(
                FormDataContentDisposition.name( "uploadAnalysis" )
                    .fileName( "schema.xml" )
                    .size( uploadAnalysis.available() )
                    .build()
                , uploadAnalysis, MediaType.TEXT_XML_TYPE )
        );

    WebResource webResource = resource();

    ClientResponse postAnalysis = webResource.path( "data-access/api/mondrian/postAnalysis" )
        .type( MediaType.MULTIPART_FORM_DATA_TYPE )
        .post( ClientResponse.class, part );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    final RepositoryFileAclDto savedACL = webResource
        .path( "data-access/api/datasource/analysis/" + catalogID + "/acl" )
        .get( ClientResponse.class ).getEntity( RepositoryFileAclDto.class );
    assertNotNull( savedACL );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( webResource, catalogID, true );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( webResource, catalogID, false );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final ClientResponse changeACL = webResource
        .path( "data-access/api/datasource/analysis/" + catalogID + "/acl" )
        .put( ClientResponse.class, generateACL( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( webResource, catalogID, true );
  }

  @Test
  public void testAnalysis_ACL() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String catalogID = "FoodMart";
    final InputStream uploadAnalysis = new FileInputStream( "test-res/schema.xml" );
    final boolean overwrite = true;
    final boolean xmlaEnabledFlag = false;
    final String parameters = "DataSource=" + catalogID + ";EnableXmla=" + xmlaEnabledFlag + ";overwrite=" + overwrite;

    MultiPart part = new FormDataMultiPart()
        .field( "catalogName", catalogID )
        .field( "datasourceName", catalogID )
        .field( "overwrite", String.valueOf( overwrite ) )
        .field( "xmlaEnabledFlag", String.valueOf( xmlaEnabledFlag ) )
        .field( "parameters", parameters )
        .bodyPart(
            new FormDataBodyPart(
                FormDataContentDisposition.name( "uploadAnalysis" )
                    .fileName( "schema.xml" )
                    .size( uploadAnalysis.available() )
                    .build()
                , uploadAnalysis, MediaType.TEXT_XML_TYPE )
        );

    WebResource webResource = resource();

    final ClientResponse noAnalysis = webResource
        .path( DATA_ACCESS_API_DATASOURCE_ANALYSIS + catalogID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.CONFLICT.getStatusCode(), noAnalysis.getStatus() );

    ClientResponse postAnalysis = webResource.path( "data-access/api/mondrian/postAnalysis" )
        .type( MediaType.MULTIPART_FORM_DATA_TYPE )
        .post( ClientResponse.class, part );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    final ClientResponse noACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_ANALYSIS + catalogID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.NOT_FOUND.getStatusCode(), noACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( webResource, catalogID, true );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final ClientResponse changeACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_ANALYSIS + catalogID + "/acl" )
        .put( ClientResponse.class, generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkAnalysis( webResource, catalogID, true );

    final ClientResponse noAccessACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_ANALYSIS + catalogID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), noAccessACL.getStatus() );

    final ClientResponse noAccessACLNoDS = webResource
        .path( DATA_ACCESS_API_DATASOURCE_ANALYSIS + catalogID + "_not_exist/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), noAccessACLNoDS.getStatus() );
  }

  private void checkAnalysis( WebResource webResource, String catalogID, boolean hasAccess ) {
    final JaxbList analysisDatasourceIds = webResource
          .path( DATA_ACCESS_API_DATASOURCE_ANALYSIS + "ids" )
          .get( JaxbList.class );

    final List list = analysisDatasourceIds.getList();
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

    final String domainID = "domainID";
    final FileInputStream metadataFile = new FileInputStream( "test-res/Sample_SQL_Query.xmi" );
    final String overwrite = "true";
    final RepositoryFileAclDto acl = generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER );

    MultiPart part = new FormDataMultiPart()
        .field( "domainId", domainID )
        .field( "overwrite", String.valueOf( overwrite ) )
        .field( "acl", marshalACL( acl ) )
        .bodyPart(
            new FormDataBodyPart(
                FormDataContentDisposition.name( "metadataFile" )
                    .fileName( "Sample_SQL_Query.xmi" )
                    .size( metadataFile.available() )
                    .build()
                , metadataFile, MediaType.TEXT_XML_TYPE )
        );

    WebResource webResource = resource();

    ClientResponse postAnalysis = webResource.path( "data-access/api/metadata/import" )
        .type( MediaType.MULTIPART_FORM_DATA_TYPE )
        .put( ClientResponse.class, part );
    assertEquals( 3, postAnalysis.getStatus() );

    final RepositoryFileAclDto savedACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + domainID + "/acl" )
        .get( ClientResponse.class ).getEntity( RepositoryFileAclDto.class );
    assertNotNull( savedACL );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( webResource, domainID, true );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( webResource, domainID, false );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final ClientResponse changeACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + domainID + "/acl" )
        .put( ClientResponse.class, generateACL( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( webResource, domainID, true );
  }

  @Test
  public void testMetadata_ACL() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String domainID = "domainID";
    final FileInputStream metadataFile = new FileInputStream( "test-res/Sample_SQL_Query.xmi" );
    final String overwrite = "true";

    MultiPart part = new FormDataMultiPart()
        .field( "domainId", domainID )
        .field( "overwrite", String.valueOf( overwrite ) )
        .bodyPart(
            new FormDataBodyPart(
                FormDataContentDisposition.name( "metadataFile" )
                    .fileName( "Sample_SQL_Query.xmi" )
                    .size( metadataFile.available() )
                    .build()
                , metadataFile, MediaType.TEXT_XML_TYPE )
        );

    WebResource webResource = resource();

    final ClientResponse noMetadata = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + domainID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.CONFLICT.getStatusCode(), noMetadata.getStatus() );

    ClientResponse postAnalysis = webResource.path( "data-access/api/metadata/import" )
        .type( MediaType.MULTIPART_FORM_DATA_TYPE )
        .put( ClientResponse.class, part );
    assertEquals( 3, postAnalysis.getStatus() );

    final ClientResponse noACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + domainID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.NOT_FOUND.getStatusCode(), noACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( webResource, domainID, true );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final ClientResponse changeACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + domainID + "/acl" )
        .put( ClientResponse.class, generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkMetadata( webResource, domainID, true );

    final ClientResponse noAccessACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + domainID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), noAccessACL.getStatus() );

    final ClientResponse noAccessACLNoDS = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + domainID + "_not_exist/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), noAccessACLNoDS.getStatus() );
  }

  private void checkMetadata( WebResource webResource, String domainID, boolean hasAccess ) {
    final JaxbList metadataDatasourceIds = webResource
        .path( DATA_ACCESS_API_DATASOURCE_METADATA + "ids" )
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
    final FileInputStream metadataFile = new FileInputStream( "test-res/test.xmi" );
    final boolean overwrite = true;
    final RepositoryFileAclDto acl = generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER );

    MultiPart part = new FormDataMultiPart()
        .field( "domainId", domainID )
        .field( "overwrite", String.valueOf( overwrite ) )
        .field( "acl", marshalACL( acl ) )
        .bodyPart(
            new FormDataBodyPart(
                FormDataContentDisposition.name( "metadataFile" )
                    .fileName( "test.xmi" )
                    .size( metadataFile.available() )
                    .build()
                , metadataFile, MediaType.TEXT_XML_TYPE )
        );

    WebResource webResource = resource();

    ClientResponse postAnalysis = webResource.path( DATA_ACCESS_API_DATASOURCE_DSW + "import" )
        .type( MediaType.MULTIPART_FORM_DATA_TYPE )
        .put( ClientResponse.class, part );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    final RepositoryFileAclDto savedACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + domainID + "/acl" )
        .get( ClientResponse.class ).getEntity( RepositoryFileAclDto.class );
    assertNotNull( savedACL );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( webResource, domainID, true );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( webResource, domainID, false );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final ClientResponse changeACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + domainID + "/acl" )
        .put( ClientResponse.class, generateACL( AUTHENTICATED_ROLE_NAME, RepositoryFileSid.Type.ROLE ) );
    assertEquals( Response.Status.OK.getStatusCode(), changeACL.getStatus() );

    repositoryBase.login( USERNAME_TIFFANY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( webResource, domainID, true );
  }

  @Test
  public void testDSW_ACL() throws Exception {
    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );

    final String domainID = "test.xmi";
    final FileInputStream metadataFile = new FileInputStream( "test-res/test.xmi" );
    final boolean overwrite = true;

    MultiPart part = new FormDataMultiPart()
        .field( "domainId", domainID )
        .field( "overwrite", String.valueOf( overwrite ) )
        .bodyPart(
            new FormDataBodyPart(
                FormDataContentDisposition.name( "metadataFile" )
                    .fileName( "test.xmi" )
                    .size( metadataFile.available() )
                    .build()
                , metadataFile, MediaType.TEXT_XML_TYPE )
        );

    WebResource webResource = resource();

    final ClientResponse noDSW = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + domainID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.CONFLICT.getStatusCode(), noDSW.getStatus() );

    ClientResponse postAnalysis = webResource.path( DATA_ACCESS_API_DATASOURCE_DSW + "import" )
        .type( MediaType.MULTIPART_FORM_DATA_TYPE )
        .put( ClientResponse.class, part );
    assertEquals( Response.Status.OK.getStatusCode(), postAnalysis.getStatus() );

    final ClientResponse noACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + domainID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.NOT_FOUND.getStatusCode(), noACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( webResource, domainID, true );

    repositoryBase.login( singleTenantAdminUserName, defaultTenant,
        new String[] { repositoryBase.getTenantAdminRoleName(), AUTHENTICATED_ROLE_NAME } );
    final ClientResponse setSuzyACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + domainID + "/acl" )
        .put( ClientResponse.class, generateACL( USERNAME_SUZY, RepositoryFileSid.Type.USER ) );
    assertEquals( Response.Status.OK.getStatusCode(), setSuzyACL.getStatus() );

    repositoryBase.login( USERNAME_SUZY, defaultTenant, new String[] { AUTHENTICATED_ROLE_NAME } );
    checkDSW( webResource, domainID, true );

    final ClientResponse noAccessACL = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + domainID + "/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), noAccessACL.getStatus() );

    final ClientResponse noAccessACLNoDS = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + domainID + "_not_exist/acl" )
        .get( ClientResponse.class );
    assertEquals( Response.Status.UNAUTHORIZED.getStatusCode(), noAccessACLNoDS.getStatus() );
  }

  private void checkDSW( WebResource webResource, String domainID, boolean hasAccess ) {
    final JaxbList dswIds = webResource
        .path( DATA_ACCESS_API_DATASOURCE_DSW + "ids" )
        .get( JaxbList.class );

    final List list = dswIds.getList();
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

    final ArrayList<RepositoryFileAclAceDto> aces = new ArrayList<RepositoryFileAclAceDto>();
    final RepositoryFileAclAceDto aceDto = new RepositoryFileAclAceDto();
    aceDto.setRecipient( userOrRole );
    aceDto.setRecipientType( type.ordinal() );

    final ArrayList<Integer> permissions = new ArrayList<Integer>();
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

  @Override
  public void setApplicationContext( ApplicationContext applicationContext ) throws BeansException {
    this.applicationContext = applicationContext;
    repositoryBase.setApplicationContext( applicationContext );
  }
}
