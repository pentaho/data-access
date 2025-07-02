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


package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.IClientRepositoryPathsStrategy;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.api.repository2.unified.IBackingRepositoryLifecycleManager;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ModelerService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianOneToOneUserRoleListMapper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXOlap4jConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.unified.DefaultUnifiedRepository;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.MethodTrackingData;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
@SuppressWarnings("nls")
public class SerializeMultiTableServiceIT {

  private String tenantAdminAuthorityNamePattern = "{0}_Admin";

  private String tenantAuthenticatedAuthorityNamePattern = "{0}_Authenticated";
  
  private static final String USER_PARAMETER = "user";

  private IBackingRepositoryLifecycleManager manager;
  
  private MicroPlatform booter;
  
  @Before
  public void setUp() throws Exception {
    
    manager = new MockBackingRepositoryLifecycleManager(new MockSecurityHelper());
    IAuthorizationPolicy mockAuthorizationPolicy = mock(IAuthorizationPolicy.class);
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );

    IUserRoleListService mockUserRoleListService = mock(IUserRoleListService.class);

    IPasswordService mockPasswordService = mock( IPasswordService.class );
    when(mockPasswordService.encrypt( anyString() )).then(returnsFirstArg());

    System.setProperty("org.osjava.sj.root", "target/test-classes/solution1/system/simple-jndi");
    booter = new MicroPlatform("target/test-classes/solution1");
    
    booter.define(ISolutionEngine.class, SolutionEngine.class, Scope.GLOBAL);
    booter.define(IUnifiedRepository.class, TestFileSystemBackedUnifiedRepository.class, Scope.GLOBAL);
    booter.define(IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL);
    booter.define("connection-SQL", SQLConnection.class);
    booter.define("connection-MDX", MDXConnection.class);
    booter.define("connection-MDXOlap4j", MDXOlap4jConnection.class);
    booter.define(IDBDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL);
    booter.define(MDXConnection.MDX_CONNECTION_MAPPER_KEY, MondrianOneToOneUserRoleListMapper.class, Scope.GLOBAL);
    booter.define(IDatasourceMgmtService.class, MockDatasourceMgmtService.class);
    booter.define(IClientRepositoryPathsStrategy.class, MockClientRepositoryPathsStrategy.class);
    booter.defineInstance(IMetadataDomainRepository.class, createMetadataDomainRepository());
    booter.define(ISecurityHelper.class, MockSecurityHelper.class);
    booter.define(UserDetailsService.class, MockUserDetailService.class);
    booter.define("singleTenantAdminUserName", "admin" );
    booter.defineInstance( IAuthorizationPolicy.class, mockAuthorizationPolicy );
     booter.defineInstance(IPluginResourceLoader.class, new PluginResourceLoader() {
       @Override
        protected PluginClassLoader getOverrideClassloader() {
          return new PluginClassLoader(new File(".", "target/test-classes/solution1/system/simple-jndi"), this);
        }
      });

    booter.defineInstance(IUserRoleListService.class, mockUserRoleListService);
    booter.defineInstance( IPasswordService.class, mockPasswordService );
    
    booter.setSettingsProvider(new SystemSettings());
    booter.start();
    
    
    PentahoSessionHolder.setStrategyName(PentahoSessionHolder.MODE_GLOBAL);
    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_GLOBAL);
  }

  @Test
  public void testSerialize() throws Exception {

    if (ModelerMessagesHolder.getMessages() == null) {
      ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    }

    try {
      KettleEnvironment.init();
      Props.init(Props.TYPE_PROPERTIES_EMPTY);
    } catch (Exception e) {
      // may already be initialized by another test
    }

    login("suzy", "", false);

    String solutionStorage = AgileHelper.getDatasourceSolutionStorage();
    String path = solutionStorage + RepositoryFile.SEPARATOR
        + "resources" + RepositoryFile.SEPARATOR + "metadata" + RepositoryFile.SEPARATOR;

    String olapPath = null;

    IApplicationContext appContext = PentahoSystem.getApplicationContext();
    if (appContext != null) {
      path = PentahoSystem.getApplicationContext().getSolutionPath(path);
      olapPath = PentahoSystem.getApplicationContext().getSolutionPath(
          "system" + RepositoryFile.SEPARATOR + "olap" + RepositoryFile.SEPARATOR);
    }

    File olap1 = new File(olapPath + "datasources.xml");
    File olap2 = new File(olapPath + "tmp_datasources.xml");

    FileUtils.copyFile(olap1, olap2);

    DatabaseMeta database = getDatabaseMeta();
    MultiTableModelerSource multiTable = new MultiTableModelerSource(database, getSchema(), database.getName(),
        Arrays.asList("CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE"));
    Domain domain = multiTable.generateDomain();

    List<OlapDimension> olapDimensions = new ArrayList<>();
    OlapDimension dimension = new OlapDimension();
    dimension.setName("test");
    dimension.setTimeDimension(false);
    olapDimensions.add(dimension);
    domain.getLogicalModels().get(0).setProperty("olap_dimensions", olapDimensions);

    ModelerService service = new ModelerService();
    service.serializeModels(domain, "test_file");

    assertEquals( "SampleData", domain.getLogicalModels().get(0).getProperty("MondrianCatalogRef") );
  }

  private SchemaModel getSchema() {
    List<JoinRelationshipModel> joins = new ArrayList<>();

    JoinTableModel joinTable1 = new JoinTableModel();
    joinTable1.setName("CUSTOMERS");

    JoinTableModel joinTable2 = new JoinTableModel();
    joinTable2.setName("PRODUCTS");

    JoinRelationshipModel join1 = new JoinRelationshipModel();
    JoinFieldModel lField1 = new JoinFieldModel();
    lField1.setName("CUSTOMERNAME");
    lField1.setParentTable(joinTable1);
    join1.setLeftKeyFieldModel(lField1);

    JoinFieldModel rField1 = new JoinFieldModel();
    rField1.setName("PRODUCTCODE");
    rField1.setParentTable(joinTable2);
    join1.setRightKeyFieldModel(rField1);

    joins.add(join1);
    SchemaModel model = new SchemaModel();
    model.setJoins(joins);
    return model;
  }

  private DatabaseMeta getDatabaseMeta() {
     DatabaseMeta database = new DatabaseMeta();
     try {
      database.setDatabaseType("Hypersonic");
      database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
      database.setDBName("SampleData");
      database.setName("SampleData");
    } catch (Exception e) {
      e.printStackTrace();
    }
    return database;
  }
  
  protected void login(final String username, final String tenantId, final boolean tenantAdmin) {
    StandaloneSession pentahoSession = new StandaloneSession(username);
    pentahoSession.setAuthenticated(username);
    pentahoSession.setAttribute(IPentahoSession.TENANT_ID_KEY, tenantId);
    final String password = "password";

    List<GrantedAuthority> authList = new ArrayList<>();
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

  protected void logout() {
    PentahoSessionHolder.removeSession();
    SecurityContextHolder.getContext().setAuthentication(null);
  }
  
  private class MockBackingRepositoryLifecycleManager implements IBackingRepositoryLifecycleManager {
      public static final String UNIT_TEST_EXCEPTION_MESSAGE = "Unit Test Exception";
      private ArrayList<MethodTrackingData> methodTrackerHistory = new ArrayList<>();
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
        return null;
      }
    }

  public static class MockUserDetailService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException, DataAccessException {
      
      List<GrantedAuthority> authList = new ArrayList<>();
      authList.add( new SimpleGrantedAuthority("Authenticated") );
      authList.add( new SimpleGrantedAuthority("Administrator") );

      return new User(name, "password", true, true, true, true, authList);
    }
    
  }

  public static class TestFileSystemBackedUnifiedRepository extends FileSystemBackedUnifiedRepository {
    public TestFileSystemBackedUnifiedRepository() {
      super("bin/test-solutions/solution");
    }
  }

  public static class MockDatasourceMgmtService implements IDatasourceMgmtService{

    @Override
    public void init(IPentahoSession arg0) {
    }

    @Override
    public String createDatasource(IDatabaseConnection arg0) throws DuplicateDatasourceException,
        DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public void deleteDatasourceById(String arg0) throws NonExistingDatasourceException, DatasourceMgmtServiceException {
     
    }

    @Override
    public void deleteDatasourceByName(String arg0) throws NonExistingDatasourceException,
        DatasourceMgmtServiceException {
      
    }

    @Override
    public IDatabaseConnection getDatasourceById(String arg0) throws DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public IDatabaseConnection getDatasourceByName(String arg0) throws DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public List<String> getDatasourceIds() throws DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public List<IDatabaseConnection> getDatasources() throws DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public String updateDatasourceById(String arg0, IDatabaseConnection arg1) throws NonExistingDatasourceException,
        DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public String updateDatasourceByName(String arg0, IDatabaseConnection arg1) throws NonExistingDatasourceException,
        DatasourceMgmtServiceException {
      return null;
    }
  }
  
  public static class MockClientRepositoryPathsStrategy implements IClientRepositoryPathsStrategy {

    @Override
    public String getEtcFolderName() {
      return null;
    }

    @Override
    public String getEtcFolderPath() {
      return null;
    }

    @Override
    public String getHomeFolderName() {
      return null;
    }

    @Override
    public String getHomeFolderPath() {
      return null;
    }

    @Override
    public String getPublicFolderName() {
      return null;
    }

    @Override
    public String getPublicFolderPath() {
      return null;
    }

    @Override
    public String getRootFolderPath() {
      return null;
    }

    @Override
    public String getUserHomeFolderName(String arg0) {
      return null;
    }

    @Override
    public String getUserHomeFolderPath(String arg0) {
      return null;
    }
    
  }

  public PentahoMetadataDomainRepository createMetadataDomainRepository() {
    IUnifiedRepository repository = new FileSystemBackedUnifiedRepository("target/test-classes/solution1");
    booter.defineInstance(IUnifiedRepository.class, repository);
    assertNotNull(new RepositoryUtils(repository).getFolder("/etc/metadata", true, true, null));
    assertNotNull(new RepositoryUtils(repository).getFolder("/etc/mondrian", true, true, null));
    assertNotNull(new RepositoryUtils(repository).getFolder("/savetest", true, true, null));
    return new PentahoMetadataDomainRepository(repository);
  }
}
