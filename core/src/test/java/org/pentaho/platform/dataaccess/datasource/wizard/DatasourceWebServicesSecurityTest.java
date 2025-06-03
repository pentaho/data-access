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


package org.pentaho.platform.dataaccess.datasource.wizard;

import static org.junit.Assert.assertTrue;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.Response;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MetadataDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.cache.CacheManager;
import org.pentaho.platform.util.KettlePasswordService;
import org.pentaho.test.platform.MethodTrackingData;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Integration test. Tests {@link DefaultUnifiedRepository} and {@link IAuthorizationPolicy} fully configured behind
 * Spring Security's method security and Spring's transaction interceptor.
 * 
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to setup a Spring application context. The
 * application context config files are listed in the ContextConfiguration annotation. By implementing
 * {@link ApplicationContextAware}, this unit test can access various beans defined in the application context,
 * including the bean under test.
 * </p>
 * 
 * @author mlowery
 */
@SuppressWarnings( "nls" )
public class DatasourceWebServicesSecurityTest {

  private String tenantAdminAuthorityNamePattern = "{0}_Admin";

  private String tenantAuthenticatedAuthorityNamePattern = "{0}_Authenticated";

  private static final String USER_PARAMETER = "user";

  private IBackingRepositoryLifecycleManager manager;

  private Mockery context = new JUnit4Mockery();

  private IUnifiedRepository repo;

  private MicroPlatform booter;

  @Before
  public void setUp() throws Exception {

    manager = new MockBackingRepositoryLifecycleManager( new MockSecurityHelper() );
    repo = context.mock( IUnifiedRepository.class );

    booter = new MicroPlatform( "target/test-classes/solution1" );
    booter.define( IPasswordService.class, KettlePasswordService.class, Scope.GLOBAL );
    booter.define( IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL );
    booter.define( IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL );
    booter.define( IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL );
    booter.define( ICacheManager.class, CacheManager.class, Scope.GLOBAL );
    booter.defineInstance( IUserRoleListService.class, context.mock( IUserRoleListService.class ) );
    final IAuthorizationPolicy policy = context.mock( IAuthorizationPolicy.class );
    booter.defineInstance( IAuthorizationPolicy.class, policy );
    booter.defineInstance( IUnifiedRepository.class, repo );
    booter.setSettingsProvider( new SystemSettings() );
    booter.start();

    context.checking( new Expectations() {
      {

        oneOf( policy );
        will( returnValue( false ) );
        oneOf( policy );
        will( returnValue( false ) );
        oneOf( policy );
        will( returnValue( false ) );

      }
    } );

    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @After
  public void tearDown() throws Exception {
    booter.stop();
  }

  // TODO This is a broken test, we are providing null as input stream to postMondrianSchema
  @Test
  public void testPostMondrainSchema() throws Exception {
    login( "joe", "duff", false );

    // Response res = analysisDatasourceService.postMondrainSchema(null, schemaFileInfo, catalogName, datasourceName,
    // overwrite, xmlaEnabledFlag, parameters);
    // Assert.assertTrue(res.getEntity().equals("org.pentaho.platform.api.engine.PentahoAccessControlException: Access
    // Denied"));
    // Assert.assertTrue(res.getEntity().equals("Access Denied"));
    logout();
  }

  @Test
  public void testPutMondrainSchema() throws Exception {
    login( "joe", "duff", false );
    // analysisDatasourceService.importAnalysisSchemaFile(domainId, dataInputStream, domainId, "true","false");
    // Assert.assertTrue(res.getEntity().equals("Access Denied"));

    // res = analysisDatasourceService.postMondrainSchema(dataInputStream, schemaFileInfo, domainId, datasourceName,
    // overwrite, xmlaEnabledFlag, parameters);
    // Assert.assertTrue(res.getEntity().equals("org.pentaho.platform.api.engine.PentahoAccessControlException: Access
    // Denied"));
    logout();
  }

  @Test
  public void testImportMetadataDatasource() throws Exception {
    login( "joe", "duff", false );
    MetadataDatasourceService analysisDatasourceService = new MetadataDatasourceService();

    Response res = analysisDatasourceService.storeDomain( null, null );
    assertTrue( res.getEntity().equals(
        "org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied" ) );

    // res = analysisDatasourceService.importMetadataDatasourceLegacy(null, null, null);
    assertTrue( res.getEntity().equals(
        "org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied" ) );

    res = analysisDatasourceService.addLocalizationFile( null, null, null );
    assertTrue( res.getEntity().equals(
        "org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied" ) );

    logout();
  }

  protected void login( final String username, final String tenantId, final boolean tenantAdmin ) {
    StandaloneSession pentahoSession = new StandaloneSession( username );
    pentahoSession.setAuthenticated( username );
    pentahoSession.setAttribute( IPentahoSession.TENANT_ID_KEY, tenantId );
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
    authList.add( new SimpleGrantedAuthority( MessageFormat.format( tenantAuthenticatedAuthorityNamePattern,
        tenantId ) ) );
    if ( tenantAdmin ) {
      authList.add( new SimpleGrantedAuthority( MessageFormat.format( tenantAdminAuthorityNamePattern, tenantId ) ) );
    }

    UserDetails userDetails = new User( username, password, true, true, true, true, authList );
    Authentication auth = new UsernamePasswordAuthenticationToken( userDetails, password, authList );
    PentahoSessionHolder.setSession( pentahoSession );
    // this line necessary for Spring Security's MethodSecurityInterceptor
    SecurityContextHolder.getContext().setAuthentication( auth );

    manager.newTenant();
    manager.newUser();
  }

  protected void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication( null );
  }

  private class MockBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {
    public static final String UNIT_TEST_EXCEPTION_MESSAGE = "Unit Test Exception";
    private ArrayList<MethodTrackingData> methodTrackerHistory = new ArrayList<MethodTrackingData>();
    private boolean throwException = false;
    private MockSecurityHelper securityHelper;

    private MockBackingRepositoryLifecycleManager( final MockSecurityHelper securityHelper ) {
      assert ( null != securityHelper );
      this.securityHelper = securityHelper;
    }

    public void startup() {
      methodTrackerHistory.add( new MethodTrackingData( "startup" ).addParameter( USER_PARAMETER, securityHelper
          .getCurrentUser() ) );
      if ( throwException ) {
        throw new RuntimeException( UNIT_TEST_EXCEPTION_MESSAGE );
      }
    }

    public void shutdown() {
      methodTrackerHistory.add( new MethodTrackingData( "shutdown" ).addParameter( USER_PARAMETER, securityHelper
          .getCurrentUser() ) );
      if ( throwException ) {
        throw new RuntimeException( UNIT_TEST_EXCEPTION_MESSAGE );
      }
    }

    public void newTenant( final ITenant tenant ) {
      methodTrackerHistory.add( new MethodTrackingData( "newTenant" ).addParameter( USER_PARAMETER, securityHelper
          .getCurrentUser() ).addParameter( "tenant", tenant ) );
      if ( throwException ) {
        throw new RuntimeException( UNIT_TEST_EXCEPTION_MESSAGE );
      }
    }

    public void newTenant() {
      methodTrackerHistory.add( new MethodTrackingData( "newTenant" ).addParameter( USER_PARAMETER, securityHelper
          .getCurrentUser() ) );
      if ( throwException ) {
        throw new RuntimeException( UNIT_TEST_EXCEPTION_MESSAGE );
      }
    }

    public void newUser( final ITenant tenant, final String username ) {
      methodTrackerHistory.add( new MethodTrackingData( "newUser" ).addParameter( USER_PARAMETER, securityHelper
          .getCurrentUser() ).addParameter( "tenant", tenant ).addParameter( "username", username ) );
      if ( throwException ) {
        throw new RuntimeException( UNIT_TEST_EXCEPTION_MESSAGE );
      }
    }

    public void newUser() {
      methodTrackerHistory.add( new MethodTrackingData( "newUser" ).addParameter( USER_PARAMETER, securityHelper
          .getCurrentUser() ) );
      if ( throwException ) {
        throw new RuntimeException( UNIT_TEST_EXCEPTION_MESSAGE );
      }
    }

    @Override
    public void addMetadataToRepository( String arg0 ) {
    }

    @Override
    public Boolean doesMetadataExists( String arg0 ) {
      return null;
    }
  }

}
