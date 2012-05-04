/*
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
 * Copyright 2005 - 2009 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MetadataDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepositoryInfo;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.extensions.jcr.SessionFactory;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.userdetails.User;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml", "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class MetadataDatasourceServiceTest extends TestCase implements ApplicationContextAware {

	// ~ Instance fields
	// =================================================================================================

	private MicroPlatform booter;

	private IUnifiedRepository repository;
	
	private boolean startupCalled;

	private String repositoryAdminUsername;

	private String tenantAdminAuthorityNamePattern;

	private String tenantAuthenticatedAuthorityNamePattern;

	/**
	 * Used for state verification and test cleanup.
	 */
	private JcrTemplate testJcrTemplate;

	private IBackingRepositoryLifecycleManager manager;

	private IRoleAuthorizationPolicyRoleBindingDao roleBindingDao;

	private IAuthorizationPolicy authorizationPolicy;

	@BeforeClass
	public static void setUpClass() throws Exception {
		FileUtils.deleteDirectory(new File("/tmp/jackrabbit-test-TRUNK"));
		PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_INHERITABLETHREADLOCAL);
	}

	@Before
	public void setUp() throws Exception {

		booter = new MicroPlatform("test-res");

		// Clear up the cache
		final ICacheManager cacheMgr = PentahoSystem.getCacheManager(null);
		cacheMgr.clearRegionCache(MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION);

		// Define a repository for testing
		repository = new MockUnifiedRepository(new MockUserProvider());
		repository.createFolder(repository.getFile("/etc").getId(), new RepositoryFile.Builder("metadata").folder(true).build(), "initialization");

		booter.defineInstance(IUnifiedRepository.class, repository);
		booter.start();
		
		
		booter.defineInstance(IAuthorizationPolicy.class, authorizationPolicy);
		logout();
		manager.startup();
		startupCalled = true;
	}

	@After
	public void tearDown() throws Exception {
		clearRoleBindings();
		// null out fields to get back memory
		authorizationPolicy = null;
		loginAsRepositoryAdmin();
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getPentahoRootFolderPath());
		logout();
		repositoryAdminUsername = null;
		tenantAdminAuthorityNamePattern = null;
		tenantAuthenticatedAuthorityNamePattern = null;
		roleBindingDao = null;
		authorizationPolicy = null;
		testJcrTemplate = null;
		if (startupCalled) {
			manager.shutdown();
		}

		// null out fields to get back memory
		repository = null;		
	}

	@Test
	public void testImportSchema() throws Exception {
		try {
			login("joe", "duff", false);
			String localizeBundleEntries = "index_ja.properties=index_ja.properties;index_de.properties=index_de.properties";
			MetadataDatasourceService service = new MetadataDatasourceService();
			service.importMetadataDatasource(localizeBundleEntries, "steel-wheels", "metadata.xmi");
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
	
	protected void clearRoleBindings() throws Exception {
		loginAsRepositoryAdmin();
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
	}

	private class MockUserProvider implements MockUnifiedRepository.ICurrentUserProvider {
		@Override
		public String getUser() {
			return MockUnifiedRepository.root().getName();
		}

		@Override
		public List<String> getRoles() {
			return new ArrayList<String>();
		}
	}
	
	
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		manager = (IBackingRepositoryLifecycleManager) applicationContext.getBean("backingRepositoryLifecycleManager");
		SessionFactory jcrSessionFactory = (SessionFactory) applicationContext.getBean("jcrSessionFactory");
		testJcrTemplate = new JcrTemplate(jcrSessionFactory);
		testJcrTemplate.setAllowCreate(true);
		testJcrTemplate.setExposeNativeSession(true);
		repositoryAdminUsername = (String) applicationContext.getBean("repositoryAdminUsername");
		tenantAuthenticatedAuthorityNamePattern = (String) applicationContext.getBean("tenantAuthenticatedAuthorityNamePattern");
		tenantAdminAuthorityNamePattern = (String) applicationContext.getBean("tenantAdminAuthorityNamePattern");
		roleBindingDao = (IRoleAuthorizationPolicyRoleBindingDao) applicationContext.getBean("roleAuthorizationPolicyRoleBindingDao");
		authorizationPolicy = (IAuthorizationPolicy) applicationContext.getBean("authorizationPolicy");
		repository = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
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
}
