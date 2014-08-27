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

package org.pentaho.platform.dataaccess.datasource.wizard;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.Response;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.IClientRepositoryPathsStrategy;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IRepositoryFileData;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.SerializeMultiTableServiceTest.MockUserDetailService;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.SerializeMultiTableServiceTest.TestFileSystemBackedUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MetadataDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianOneToOneUserRoleListMapper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepositoryInfo;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.test.platform.MethodTrackingData;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;

@SuppressWarnings("nls")
public class MetadataDatasourceServiceTest extends TestCase  {

	// ~ Instance fields
	// =================================================================================================

	private MicroPlatform booter;

	private FileTrackingRepository repository;
	
	private String repositoryAdminUsername = "pentahoRepoAdmin";
	
	private static final String USER_PARAMETER = "user";

	private JcrTemplate testJcrTemplate;
	

	private IBackingRepositoryLifecycleManager manager;

	private String tenantAdminAuthorityNamePattern = "{0}_Admin";

	private String tenantAuthenticatedAuthorityNamePattern = "{0}_Authenticated";

	@BeforeClass
	public static void setUpClass() throws Exception {
		FileUtils.deleteDirectory(new File("/tmp/jackrabbit-test-TRUNK"));
		PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
		PentahoSystemHelper.init();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL);
	}

  @Before
  public void setUp() throws Exception {
    Mockery context = new JUnit4Mockery();
    manager = new MockBackingRepositoryLifecycleManager( new MockSecurityHelper() );
    IAuthorizationPolicy mockAuthorizationPolicy = mock( IAuthorizationPolicy.class );
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );
    // not just where to write, needs the right configs
    repository = new FileTrackingRepository( "test-res/solution1" );
    IUserRoleListService mockUserRoleListService = mock( IUserRoleListService.class );

    booter = new MicroPlatform( "test-res/solution1" );

    booter.define( ISolutionEngine.class, SolutionEngine.class );
    booter.define( IUnifiedRepository.class, TestFileSystemBackedUnifiedRepository.class );
    booter.define( IMondrianCatalogService.class, MondrianCatalogHelper.class );

    booter.define( MDXConnection.MDX_CONNECTION_MAPPER_KEY, MondrianOneToOneUserRoleListMapper.class );
    booter.defineInstance( IDatasourceMgmtService.class, context.mock( IDatasourceMgmtService.class ) );
    booter.defineInstance( IClientRepositoryPathsStrategy.class, context.mock( IClientRepositoryPathsStrategy.class ) );
    booter.defineInstance( IMetadataDomainRepository.class, createMetadataDomainRepository() );
    booter.define( ISecurityHelper.class, MockSecurityHelper.class );
    booter.define( UserDetailsService.class, MockUserDetailService.class );
    booter.define( "singleTenantAdminUserName", new String( "admin" ) );
    booter.defineInstance( IAuthorizationPolicy.class, mockAuthorizationPolicy );

    booter.defineInstance( IUserRoleListService.class, mockUserRoleListService );

    booter.setSettingsProvider( new SystemSettings() );
    booter.start();

    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

	@Test
	// XXX doesn't really test import
	public void testImportSchema() throws Exception {
		try {
			login("joe", "duff", false);
//			service.importMetadataDatasource(localizeBundleEntries, "steel-wheels", "metadata.xmi");
			logout();
		} catch (Exception e) {
			final RepositoryFile etcMetadata = repository.getFile(PentahoMetadataDomainRepositoryInfo.getMetadataFolderPath());
			assertNotNull(etcMetadata);
			assertTrue(etcMetadata.isFolder());
			final List<RepositoryFile> children = repository.getChildren(etcMetadata.getId());
			assertNotNull(children);
			assertEquals(3, children.size());
		}
	}

  @Test
  public void testImportDswMetadata() throws Exception {
    login( "joe", "duff", false );
    MetadataDatasourceService service = new MetadataDatasourceService();
    FileInputStream in = new FileInputStream( new File( new File( "test-res" ), "Sample_CSV.xmi" ) );
    try {
      repository.clearFileLists();
      Response resp = service.importMetadataDatasource( "Sample CSV", in, null, "True", null, null, true );
      assertEquals( Response.Status.CREATED, Response.Status.fromStatusCode( resp.getStatus() ) );
      Collection<Serializable> createdFiles = repository.getCreatedFiles();
      Collection<Serializable> updatedFiles = repository.getUpdatedFiles();
      // xmi, schema, schema meta
      assertEquals( 3, createdFiles.size() + updatedFiles.size() );
      logout();
    } finally {
      IOUtils.closeQuietly( in );
      repository.deleteCreatedFiles();
      repository.clearFileLists();
    }
  }

	protected void clearRoleBindings() throws Exception {
		loginAsRepositoryAdmin();
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(new Tenant("duff", true)) + ".authz");
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(new Tenant("duff", true)) + ".authz");
	}

	/**
	 * Logs in with given username.
	 * 
	 * @param username
	 *            username of user
	 * @param tenantId
	 *            tenant to which this user belongs
	 * @tenantAdmin true to add the tenant admin authority to the user's roles
	 */
	protected void login(final String username, final String tenantId, final boolean tenantAdmin) {
		StandaloneSession pentahoSession = new StandaloneSession(username);
		pentahoSession.setAuthenticated(username);
		pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenantId);
		final String password = "password";

		List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
		authList.add(new GrantedAuthorityImpl(MessageFormat.format(tenantAuthenticatedAuthorityNamePattern, tenantId)));
		if (tenantAdmin) {
			authList.add(new GrantedAuthorityImpl(MessageFormat.format(tenantAdminAuthorityNamePattern, tenantId)));
		}
		GrantedAuthority[] authorities = authList.toArray(new GrantedAuthority[0]);
		UserDetails userDetails = new User(username, password, true, true, true, true, authorities);
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, password, authorities);
		PentahoSessionHolder.setSession(pentahoSession);
		// this line necessary for Spring Security's MethodSecurityInterceptor
		SecurityContextHolder.getContext().setAuthentication(auth);

		manager.newTenant();
		manager.newUser();
	}

	protected void loginAsRepositoryAdmin() {
		StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
		pentahoSession.setAuthenticated(repositoryAdminUsername);
		final GrantedAuthority[] repositoryAdminAuthorities = new GrantedAuthority[0];
		final String password = "ignored";
		UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true, repositoryAdminAuthorities);
		Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(repositoryAdminUserDetails, password, repositoryAdminAuthorities);
		PentahoSessionHolder.setSession(pentahoSession);
		// this line necessary for Spring Security's MethodSecurityInterceptor
		SecurityContextHolder.getContext().setAuthentication(repositoryAdminAuthentication);
	}

	protected void logout() {
		PentahoSessionHolder.removeSession();
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	protected void login(final String username, final String tenantId) {
		login(username, tenantId, false);
	}

  public PentahoMetadataDomainRepository createMetadataDomainRepository() throws Exception {
    booter.defineInstance(IUnifiedRepository.class, repository);
    Assert.assertNotNull(new RepositoryUtils(repository).getFolder("/etc/metadata", true, true, null));
    Assert.assertNotNull(new RepositoryUtils(repository).getFolder("/etc/mondrian", true, true, null));
    Assert.assertNotNull(new RepositoryUtils(repository).getFolder("/savetest", true, true, null));    
    PentahoMetadataDomainRepository pentahoMetadataDomainRepository = new PentahoMetadataDomainRepository(repository);
    return pentahoMetadataDomainRepository;
  }

  /**
   * Keeps tracks of files that were created and updated via the api.
   */
  public static class FileTrackingRepository extends FileSystemBackedUnifiedRepository {

    private Set<Serializable> createdFiles = new HashSet<Serializable>();
    private Set<Serializable> updatedFiles = new HashSet<Serializable>();

    public FileTrackingRepository( final String baseDir ) {
      super( baseDir );
    }

    @Override
    public RepositoryFile createFile( Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
        RepositoryFileAcl acl, String versionMessage ) {
      RepositoryFile created = super.createFile( parentFolderId, file, data, acl, versionMessage );
      if (created != null && created.getId() != null ) {
        createdFiles.add( created.getId() );
      }
      return created;
    }
    @Override
    public RepositoryFile createFile( Serializable parentFolderId, RepositoryFile file, IRepositoryFileData data,
        String versionMessage ) {
      RepositoryFile created = super.createFile( parentFolderId, file, data, versionMessage );
      if ( created != null && created.getId() != null ) {
        createdFiles.add( created.getId() );
      }
      return created;
    }
    @Override
    public RepositoryFile updateFile( RepositoryFile file, IRepositoryFileData data, String versionMessage ) {
      RepositoryFile updated = super.updateFile( file, data, versionMessage );
      if ( updated != null && updated.getId() != null ) {
        updatedFiles.add( updated.getId() );
      }
      return updated;
    }

    public Collection<Serializable> getCreatedFiles() {
      return createdFiles;
    }
    public Collection<Serializable> getUpdatedFiles() {
      return updatedFiles;
    }

    public int deleteCreatedFiles() {
      int deleted = 0;
      for ( Serializable fileId : createdFiles ) {
        try {
          deleteFile( fileId, "cleanup" );
          deleted++;
        }
        catch (Throwable t) {
          t.printStackTrace( System.err );
        }
      }
      return deleted;
    }

    public void clearFileLists() {
      createdFiles.clear();
      updatedFiles.clear();
    }
  }
	
	private class MockBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {
	    public static final String UNIT_TEST_EXCEPTION_MESSAGE = "Unit Test Exception";
	    private ArrayList<MethodTrackingData> methodTrackerHistory = new ArrayList<MethodTrackingData>();
	    private boolean throwException = false;
	    private MockSecurityHelper securityHelper;

	    private MockBackingRepositoryLifecycleManager(final MockSecurityHelper securityHelper) {
	      assert (null != securityHelper);
	      this.securityHelper = securityHelper;
	    }

	    public void startup() {
	      methodTrackerHistory.add(new MethodTrackingData("startup")
	          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
	      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
	    }

	    public void shutdown() {
	      methodTrackerHistory.add(new MethodTrackingData("shutdown")
	          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
	      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
	    }

	    public void newTenant(final ITenant tenant) {
	      methodTrackerHistory.add(new MethodTrackingData("newTenant")
	          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser())
	          .addParameter("tenant", tenant));
	      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
	    }

	    public void newTenant() {
	      methodTrackerHistory.add(new MethodTrackingData("newTenant")
	          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
	      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
	    }

	    public void newUser(final ITenant tenant, final String username) {
	      methodTrackerHistory.add(new MethodTrackingData("newUser")
	          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser())
	          .addParameter("tenant", tenant)
	          .addParameter("username", username));
	      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
	    }

	    public void newUser() {
	      methodTrackerHistory.add(new MethodTrackingData("newUser")
	          .addParameter(USER_PARAMETER, securityHelper.getCurrentUser()));
	      if (throwException) throw new RuntimeException(UNIT_TEST_EXCEPTION_MESSAGE);
	    }

      @Override
      public void addMetadataToRepository(String arg0) {
        // TODO Auto-generated method stub
        
      }

      @Override
      public Boolean doesMetadataExists(String arg0) {
        // TODO Auto-generated method stub
        return null;
      }
	  }
}
