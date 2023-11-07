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

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.metadata.util.SerializationService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultitableDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.LegacyDatasourceConverter;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepositoryInfo;
import org.pentaho.platform.repository2.unified.ServerRepositoryPaths;
import org.pentaho.platform.repository2.unified.jcr.SimpleJcrTestUtils;
import org.pentaho.test.platform.MethodTrackingData;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;
import org.pentaho.test.platform.repository2.unified.MockUnifiedRepository;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("nls")
public class MetadataDatasourceServiceTest extends TestCase  {

	// ~ Instance fields
	// =================================================================================================

	private MicroPlatform booter;

	private IUnifiedRepository repository;
	
	private String repositoryAdminUsername = "pentahoRepoAdmin";
	
	private static final String USER_PARAMETER = "user";

	private JcrTemplate testJcrTemplate;
	

	private IBackingRepositoryLifecycleManager manager;

	private String tenantAdminAuthorityNamePattern = "{0}_Admin";

	private String tenantAuthenticatedAuthorityNamePattern = "{0}_Authenticated";

	public static final String UNIT_TEST_EXCEPTION_MESSAGE = "Unit Test Exception";

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
		Mockery context = new JUnit4Mockery();
		manager = new MockBackingRepositoryLifecycleManager(new MockSecurityHelper());
		booter = new MicroPlatform("test-res");

		// Clear up the cache
		final ICacheManager cacheMgr = PentahoSystem.getCacheManager(null);
		cacheMgr.clearRegionCache(MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION);

		// Define a repository for testing
		repository = new MockUnifiedRepository(new MockUserProvider());
		repository.createFolder(repository.getFile("/etc").getId(), new RepositoryFile.Builder("metadata").folder(true).build(), "initialization");

		final IAuthorizationPolicy policy =  context.mock(IAuthorizationPolicy.class);
		booter.defineInstance(IAuthorizationPolicy.class, policy);
		booter.defineInstance(IUnifiedRepository.class, repository);
		booter.start();
		logout();
		manager.startup();
		
		context.checking(new Expectations() {{
			oneOf (policy); will(returnValue(true));
			oneOf (policy); will(returnValue(true));
			oneOf (policy); will(returnValue(true));
	    }});
	}

	@Test
	public void testImportSchema() throws Exception {
		try {
			login("joe", "duff", false);
//			service.importMetadataDatasourceLegacy(localizeBundleEntries, "steel-wheels", "metadata.xmi");
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
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(new Tenant("duff", true)) + ".authz");
		SimpleJcrTestUtils.deleteItem(testJcrTemplate, ServerRepositoryPaths.getTenantRootFolderPath(new Tenant("duff", true)) + ".authz");
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
		authList.add(new SimpleGrantedAuthority(MessageFormat.format(tenantAuthenticatedAuthorityNamePattern, tenantId)));
		if (tenantAdmin) {
			authList.add(new SimpleGrantedAuthority(MessageFormat.format(tenantAdminAuthorityNamePattern, tenantId)));
		}

		UserDetails userDetails = new User(username, password, true, true, true, true, authList);
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, password, authList);
		PentahoSessionHolder.setSession(pentahoSession);
		// this line necessary for Spring Security's MethodSecurityInterceptor
		SecurityContextHolder.getContext().setAuthentication(auth);

		manager.newTenant();
		manager.newUser();
	}

	protected void loginAsRepositoryAdmin() {
		StandaloneSession pentahoSession = new StandaloneSession(repositoryAdminUsername);
		pentahoSession.setAuthenticated(repositoryAdminUsername);

		List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
		final String password = "ignored";
		UserDetails repositoryAdminUserDetails = new User(repositoryAdminUsername, password, true, true, true, true, authList);
		Authentication repositoryAdminAuthentication = new UsernamePasswordAuthenticationToken(repositoryAdminUserDetails, password, authList);
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

	/**
	 * Attempts to generate a DTO of type MultiTableDatasourceDTO. If failling, one of the classes is not expected
	 */
	@Test
	public void testDeSerializeModelState() {

		String dtoStr = "<org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO>\n"
			+ "  <datasourceName>datasourceForTestDeSerializeModelState</datasourceName>\n"
			+ "  <selectedConnection class=\"org.pentaho.database.model.DatabaseConnection\">\n"
			+ "    <id>anyId_12345</id>\n"
			+ "    <name>SampleData</name>\n"
			+ "    <databaseName>SampleData</databaseName>\n"
			+ "    <databasePort>1234</databasePort>\n"
			+ "    <hostname>localhost</hostname>\n"
			+ "    <username>user</username>\n"
			+ "    <dataTablespace></dataTablespace>\n"
			+ "    <indexTablespace></indexTablespace>\n"
			+ "    <streamingResults>false</streamingResults>\n"
			+ "    <quoteAllFields>false</quoteAllFields>\n"
			+ "    <changed>false</changed>\n"
			+ "    <usingDoubleDecimalAsSchemaTableSeparator>false</usingDoubleDecimalAsSchemaTableSeparator>\n"
			+ "    <informixServername></informixServername>\n"
			+ "    <forcingIdentifiersToLowerCase>false</forcingIdentifiersToLowerCase>\n"
			+ "    <forcingIdentifiersToUpperCase>false</forcingIdentifiersToUpperCase>\n"
			+ "    <connectSql></connectSql>\n"
			+ "    <usingConnectionPool>true</usingConnectionPool>\n"
			+ "    <accessTypeValue>NATIVE</accessTypeValue>\n"
			+ "    <accessType>NATIVE</accessType>\n"
			+ "    <databaseType class=\"org.pentaho.database.model.DatabaseType\">\n"
			+ "      <name>Hypersonic</name>\n"
			+ "      <shortName>HYPERSONIC</shortName>\n"
			+ "      <defaultPort>1235</defaultPort>\n"
			+ "      <supportedAccessTypes>\n"
			+ "        <org.pentaho.database.model.DatabaseAccessType>NATIVE</org.pentaho.database.model"
			+ ".DatabaseAccessType>\n"
			+ "        <org.pentaho.database.model.DatabaseAccessType>ODBC</org.pentaho.database.model.DatabaseAccessType>\n"
			+ "        <org.pentaho.database.model.DatabaseAccessType>JNDI</org.pentaho.database.model.DatabaseAccessType>\n"
			+ "      </supportedAccessTypes>\n"
			+ "      <extraOptionsHelpUrl>http://dummy.dummy.html#N11234</extraOptionsHelpUrl>\n"
			+ "    </databaseType>\n"
			+ "    <extraOptions/>\n"
			+ "    <extraOptionsOrder/>\n"
			+ "    <attributes>\n"
			+ "      <entry>\n"
			+ "        <string>PORT_NUMBER</string>\n"
			+ "        <string>1235</string>\n"
			+ "      </entry>\n"
			+ "    </attributes>\n"
			+ "    <connectionPoolingProperties/>\n"
			+ "    <initialPoolSize>0</initialPoolSize>\n"
			+ "    <maxPoolSize>1</maxPoolSize>\n"
			+ "    <partitioned>false</partitioned>\n"
			+ "  </selectedConnection>\n"
			+ "  <schemaModel>\n"
			+ "    <factTable>\n"
			+ "      <name>&quot;PUBLIC&quot;&quot;</name>\n"
			+ "      <fields>\n"
			+ "        <children/>\n"
			+ "      </fields>\n"
			+ "    </factTable>\n"
			+ "    <joins class=\"org.pentaho.ui.xul.util.AbstractModelList\">\n"
			+ "      <children/>\n"
			+ "    </joins>\n"
			+ "  </schemaModel>\n"
			+ "  <selectedTables>\n"
			+ "    <string>&quot;PUBLIC&quot;</string>\n"
			+ "  </selectedTables>\n"
			+ "  <doOlap>true</doOlap>\n"
			+ "</org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO>";
		String exception = null;
		MultiTableDatasourceDTO dto = null;
		MultitableDatasourceService service = new MultitableDatasourceService();
		try {
			dto = service.deSerializeModelState( dtoStr );
		} catch ( DatasourceServiceException e ) {
      exception = e.getMessage();
    }
    final String FAILED_XML_PARSE_MSG = "Failed to parse xml. Xstream probably not working, or error with the test xml.";
		assertNull( exception, exception );
		assertNotNull( FAILED_XML_PARSE_MSG, dto );
		assertNotNull( FAILED_XML_PARSE_MSG, dto.getDatasourceName() );
		assertNotNull( FAILED_XML_PARSE_MSG, dto.getSelectedConnection() );

	}

	/**
	 * Attempts to generate a DTO of type MultiTableDatasourceDTO. If failling, one of the classes is not expected
	 */
	@Test
	public void testFailDeSerializeModelState() {

		String dtoStr = "<org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO>\n"
			+ "  <datasourceName>datasourceForTestDeSerializeModelState</datasourceName>\n"
			+ "  <selectedConnection class=\"org.pentaho.database.model.DatabaseConnection\">\n"
			+ "    <id>anyId_12345</id>\n"
			+ "    <name>SampleData</name>\n"
			+ "    <databaseName>SampleData</databaseName>\n"
			+ "    <databasePort>1234</databasePort>\n"
			+ "    <hostname>localhost</hostname>\n"
			+ "    <username>user</username>\n"
			+ "    <dataTablespace></dataTablespace>\n"
			+ "    <indexTablespace></indexTablespace>\n"
			+ "    <streamingResults>false</streamingResults>\n"
			+ "    <quoteAllFields>false</quoteAllFields>\n"
			+ "    <changed>false</changed>\n"
			+ "    <usingDoubleDecimalAsSchemaTableSeparator>false</usingDoubleDecimalAsSchemaTableSeparator>\n"
			+ "    <informixServername></informixServername>\n"
			+ "    <forcingIdentifiersToLowerCase>false</forcingIdentifiersToLowerCase>\n"
			+ "    <forcingIdentifiersToUpperCase>false</forcingIdentifiersToUpperCase>\n"
			+ "    <connectSql></connectSql>\n"
			+ "    <usingConnectionPool>true</usingConnectionPool>\n"
			+ "    <accessTypeValue>NATIVE</accessTypeValue>\n"
			+ "    <accessType>NATIVE</accessType>\n"
			+ "		 <databaseType class=\"org.pentaho.database.model.JoinTablesModel\"/>\n"
			+ "    <databaseType class=\"org.pentaho.database.model.DatabaseType\">\n"
			+ "      <name>Hypersonic</name>\n"
			+ "      <shortName>HYPERSONIC</shortName>\n"
			+ "      <defaultPort>1235</defaultPort>\n"
			+ "      <supportedAccessTypes>\n"
			+ "        <org.pentaho.database.model.DatabaseAccessType>NATIVE</org.pentaho.database.model"
			+ ".DatabaseAccessType>\n"
			+ "        <org.pentaho.database.model.DatabaseAccessType>ODBC</org.pentaho.database.model.DatabaseAccessType>\n"
			+ "        <org.pentaho.database.model.DatabaseAccessType>JNDI</org.pentaho.database.model.DatabaseAccessType>\n"
			+ "      </supportedAccessTypes>\n"
			+ "      <extraOptionsHelpUrl>http://dummy.dummy.html#N11234</extraOptionsHelpUrl>\n"
			+ "    </databaseType>\n"
			+ "    <extraOptions/>\n"
			+ "    <extraOptionsOrder/>\n"
			+ "    <attributes>\n"
			+ "      <entry>\n"
			+ "        <string>PORT_NUMBER</string>\n"
			+ "        <string>1235</string>\n"
			+ "      </entry>\n"
			+ "    </attributes>\n"
			+ "    <connectionPoolingProperties/>\n"
			+ "    <initialPoolSize>0</initialPoolSize>\n"
			+ "    <maxPoolSize>1</maxPoolSize>\n"
			+ "    <partitioned>false</partitioned>\n"
			+ "  </selectedConnection>\n"
			+ "  <schemaModel>\n"
			+ "    <factTable>\n"
			+ "      <name>&quot;PUBLIC&quot;&quot;</name>\n"
			+ "      <fields>\n"
			+ "        <children/>\n"
			+ "      </fields>\n"
			+ "    </factTable>\n"
			+ "    <joins class=\"org.pentaho.ui.xul.util.AbstractModelList\">\n"
			+ "      <children/>\n"
			+ "    </joins>\n"
			+ "  </schemaModel>\n"
			+ "  <selectedTables>\n"
			+ "    <string>&quot;PUBLIC&quot;</string>\n"
			+ "  </selectedTables>\n"
			+ "  <doOlap>true</doOlap>\n"
			+ "</org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO>";
		String exception = null;
		MultiTableDatasourceDTO dto = null;
		MultitableDatasourceService service = new MultitableDatasourceService();
		try {
			dto = service.deSerializeModelState( dtoStr );
		} catch ( DatasourceServiceException e ) {
			exception = e.getMessage();
		}
		assertNotNull( exception );
		assertTrue( "Failed to generate exception!", exception.contains("com.thoughtworks.xstream.converters.ConversionException"));
	}


	private class MockBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {
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
