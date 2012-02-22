/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License, version 2 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */
package org.pentaho.platform.dataaccess.datasource.wizard;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.AnalysisDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MetadataDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepository;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.platform.security.policy.rolebased.IRoleAuthorizationPolicyRoleBindingDao;
import org.pentaho.platform.web.http.api.resources.RepositoryImportResource;
import org.pentaho.test.platform.engine.core.MicroPlatform;
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

/**
 * Integration test. Tests {@link DefaultUnifiedRepository} and
 * {@link IAuthorizationPolicy} fully configured behind Spring Security's method
 * security and Spring's transaction interceptor.
 * 
 * <p>
 * Note the RunWith annotation that uses a special runner that knows how to
 * setup a Spring application context. The application context config files are
 * listed in the ContextConfiguration annotation. By implementing
 * {@link ApplicationContextAware}, this unit test can access various beans
 * defined in the application context, including the bean under test.
 * </p>
 * 
 * @author mlowery
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring.xml", "classpath:/repository-test-override.spring.xml" })
@SuppressWarnings("nls")
public class DatasourceWebServicesSecurityTest implements ApplicationContextAware {

	// ~ Instance fields
	// =================================================================================================

	private IUnifiedRepository repo;

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

	private MicroPlatform mp;

	// ~ Constructors
	// ====================================================================================================

	public DatasourceWebServicesSecurityTest() throws Exception {
		super();
	}

	// ~ Methods
	// =========================================================================================================

	@BeforeClass
	public static void setUpClass() throws Exception {
		// folder cannot be deleted at teardown shutdown hooks have not yet
		// necessarily completed
		// parent folder must match jcrRepository.homeDir bean property in
		// repository-test-override.spring.xml
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
		mp = new MicroPlatform();
		// used by DefaultPentahoJackrabbitAccessControlHelper
		mp.defineInstance(IAuthorizationPolicy.class, authorizationPolicy);

		// Start the micro-platform
		// mp.start();
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
		repo = null;
	}

	protected void clearRoleBindings() throws Exception {
		loginAsRepositoryAdmin();
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath("duff") + ".authz");
	}

	@Test
	public void testImportAnalysisDatasource() throws Exception {
		login("joe", "duff", false);
		AnalysisDatasourceService analysisDatasourceService = new AnalysisDatasourceService();
		Response res = analysisDatasourceService.importAnalysisDatasource(null, null, null);
		Assert.assertTrue(res.getEntity().equals("Access Denied"));
		
		res = analysisDatasourceService.addSchema(null, null, null);
		Assert.assertTrue(res.getEntity().equals("org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied"));
		logout();
	}

	@Test
	public void testImportMetadataDatasource() throws Exception {
		login("joe", "duff", false);
		MetadataDatasourceService analysisDatasourceService = new MetadataDatasourceService();

		Response res = analysisDatasourceService.storeDomain(null, null);
		Assert.assertTrue(res.getEntity().equals("org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied"));

		res = analysisDatasourceService.importMetadataDatasource(null, null, null);
		Assert.assertTrue(res.getEntity().equals("org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied"));

		res = analysisDatasourceService.addLocalizationFile(null, null, null);
		Assert.assertTrue(res.getEntity().equals("org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied"));

		logout();
	}
	
	@Test
	public void testRepositoryImportResource() throws Exception {
		login("joe", "duff", false);
		RepositoryImportResource repositoryImportResource = new RepositoryImportResource();
		Response res = repositoryImportResource.doPostImport(null, null, null);
		Assert.assertTrue(res.getEntity().equals("org.pentaho.platform.api.engine.PentahoAccessControlException: Access Denied"));
		logout();
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
		repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
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
