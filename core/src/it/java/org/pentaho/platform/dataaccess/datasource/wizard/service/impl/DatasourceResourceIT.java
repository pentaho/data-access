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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.ISecurityHelper;
import org.pentaho.platform.api.engine.ISolutionEngine;
import org.pentaho.platform.api.engine.IUserRoleListService;
import org.pentaho.platform.api.mimetype.IMimeType;
import org.pentaho.platform.api.repository.IClientRepositoryPathsStrategy;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.DuplicateDatasourceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository.datasource.NonExistingDatasourceException;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.core.mimetype.MimeType;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.dataaccess.datasource.api.resources.DataSourceWizardResource;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.core.system.boot.PlatformInitializationException;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.engine.services.solution.SolutionEngine;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.mapper.MondrianOneToOneUserRoleListMapper;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXConnection;
import org.pentaho.platform.plugin.services.connections.mondrian.MDXOlap4jConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.MetadataImportHandler;
import org.pentaho.platform.plugin.services.importer.MondrianImportHandler;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.unified.RepositoryUtils;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.pentaho.test.platform.engine.security.MockSecurityHelper;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.sun.jersey.core.header.FormDataContentDisposition;

public class DatasourceResourceIT {
  private static MicroPlatform mp;

  @BeforeClass
  public static void setUp() throws Exception {
    System.setProperty( "org.osjava.sj.root", "target/test-classes/solution1/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$
    mp = new MicroPlatform( "target/test-classes/solution1" );

    IAuthorizationPolicy mockAuthorizationPolicy = mock( IAuthorizationPolicy.class );
    when( mockAuthorizationPolicy.isAllowed( anyString() ) ).thenReturn( true );

    IUserRoleListService mockUserRoleListService = mock( IUserRoleListService.class );

    IDataAccessPermissionHandler mockDataAccessPermHandler = mock( IDataAccessPermissionHandler.class );
    when( mockDataAccessPermHandler.hasDataAccessPermission( any( IPentahoSession.class ) ) ).thenReturn( true );

    mp.define( ISolutionEngine.class, SolutionEngine.class, IPentahoDefinableObjectFactory.Scope.GLOBAL );
    mp.define( IUnifiedRepository.class, TestFileSystemBackedUnifiedRepository.class, IPentahoDefinableObjectFactory.Scope.GLOBAL );
    mp.define( IMondrianCatalogService.class, MondrianCatalogHelper.class, IPentahoDefinableObjectFactory.Scope.GLOBAL );
    mp.define( "connection-SQL", SQLConnection.class );
    mp.define( "connection-MDX", MDXConnection.class );
    mp.define( "connection-MDXOlap4j", MDXOlap4jConnection.class );
    mp.define( IDBDatasourceService.class, JndiDatasourceService.class, IPentahoDefinableObjectFactory.Scope.GLOBAL );
    mp.define( MDXConnection.MDX_CONNECTION_MAPPER_KEY, MondrianOneToOneUserRoleListMapper.class, IPentahoDefinableObjectFactory.Scope.GLOBAL );
    mp.define( IDatasourceMgmtService.class, MockDatasourceMgmtService.class );
    mp.define( IClientRepositoryPathsStrategy.class, MockClientRepositoryPathsStrategy.class );
    mp.define( ISecurityHelper.class, MockSecurityHelper.class );
    mp.define( UserDetailsService.class, MockUserDetailService.class );
    mp.define( "singleTenantAdminUserName", "admin" );
    mp.defineInstance( IMetadataDomainRepository.class, createMetadataDomainRepository() );
    mp.defineInstance( IAuthorizationPolicy.class, mockAuthorizationPolicy );
    mp.defineInstance( IPluginResourceLoader.class, new PluginResourceLoader() {
      protected PluginClassLoader getOverrideClassloader() {
        return new PluginClassLoader( new File( ".", "target/test-classes/solution1/system/simple-jndi" ), this );
      }
    } );
    mp.defineInstance( IUserRoleListService.class, mockUserRoleListService );
    mp.defineInstance( IDataAccessPermissionHandler.class, mockDataAccessPermHandler );

    mp.setSettingsProvider( new SystemSettings() );
    mp.start();

    PentahoSessionHolder.setStrategyName( PentahoSessionHolder.MODE_GLOBAL );
    SecurityContextHolder.setStrategyName( SecurityContextHolder.MODE_GLOBAL );
  }

  @Before
  @After
  public void clearDSWData() {
    File repoData = new File( "target/test-classes/dsw/etc" );
    if ( repoData.exists() && repoData.isDirectory() ) {
      clearDir( repoData );
    }
  }

  private void clearDir( File dir ) {
    if ( dir.isDirectory() ) {
      for ( File file : dir.listFiles() ) {
        if ( file.isDirectory() ) {
          clearDir( file );
        } else {
          file.delete();
        }
      }
    }
  }

  @Test
  public void DummyTest() throws Exception {

  }
  @Test
  public void testMondrianImportExport() throws Exception {
    final String domainName = "SalesData";
    List<IMimeType> mimeTypeList = new ArrayList<IMimeType>();
    mimeTypeList.add( new MimeType( "Mondrian", "mondrian.xml" ) );
    System.setProperty( "org.osjava.sj.root", "target/test-classes/solution1/system/simple-jndi" ); //$NON-NLS-1$ //$NON-NLS-2$

    File mondrian = new File( "target/test-classes/dsw/testData/SalesData.mondrian.xml" );
    RepositoryFile repoMondrianFile = new RepositoryFile.Builder( mondrian.getName() ).folder( false ).hidden( false )
        .build();
    RepositoryFileImportBundle bundle1 = new RepositoryFileImportBundle.Builder()
      .file( repoMondrianFile ).charSet( "UTF-8" ).input( new FileInputStream( mondrian ) ).mime( "mondrian.xml" )
        .withParam( "parameters", "Datasource=Pentaho;overwrite=true" ).withParam( "domain-id", "SalesData" ).build();
    MondrianImportHandler mondrianImportHandler = new MondrianImportHandler( mimeTypeList,
        PentahoSystem.get( IMondrianCatalogService.class ) );
    mondrianImportHandler.importFile( bundle1 );

    try {
      KettleEnvironment.init();
      Props.init( Props.TYPE_PROPERTIES_EMPTY );
    } catch ( Exception e ) {
      // may already be initialized by another test
    }

    Domain domain = generateModel();

    ModelerWorkspace model = new ModelerWorkspace( new GwtModelerWorkspaceHelper() );
    model.setModelName( "ORDERS" );
    model.setDomain( domain );
    model.getWorkspaceHelper().populateDomain( model );

    new ModelerService().serializeModels( domain, domainName );

    final Response salesData = new DataSourceWizardResource().doGetDSWFilesAsDownload( domainName + ".xmi" );
    Assert.assertEquals( salesData.getStatus(), Response.Status.OK.getStatusCode() );
    Assert.assertNotNull( salesData.getMetadata() );
    Assert.assertNotNull( salesData.getMetadata().getFirst( "Content-Disposition" ) );
    Assert.assertEquals( salesData.getMetadata().getFirst( "Content-Disposition" ).getClass(), String.class );
    Assert.assertTrue( ( (String) salesData.getMetadata().getFirst( "Content-Disposition" ) ).endsWith( domainName + ".zip\"" ) );

    File file = File.createTempFile( domainName, ".zip" );
    final FileOutputStream fileOutputStream = new FileOutputStream( file );
    ( (StreamingOutput) salesData.getEntity() ).write( fileOutputStream );
    fileOutputStream.close();

    final ZipFile zipFile = new ZipFile( file );
    final Enumeration<? extends ZipEntry> entries = zipFile.entries();
    while ( entries.hasMoreElements() ) {
      final ZipEntry zipEntry = entries.nextElement();
      Assert.assertTrue( zipEntry.getName().equals( domainName + ".xmi" ) || zipEntry.getName().equals( domainName + ".mondrian.xml" ) );
    }
    zipFile.close();
    file.delete();
  }

  @Test
  public void testMetadataImportExport() throws PlatformInitializationException, IOException, PlatformImportException {
    List<IMimeType> mimeTypeList = new ArrayList<IMimeType>();
    mimeTypeList.add( new MimeType( "Metadata", ".xmi" ) );

    File metadata = new File( "target/test-classes/dsw/testData/metadata.xmi" );
    RepositoryFile repoMetadataFile = new RepositoryFile.Builder( metadata.getName() ).folder( false ).hidden( false )
        .build();
    MetadataImportHandler metadataImportHandler = new MetadataImportHandler( mimeTypeList,
        (IPentahoMetadataDomainRepositoryImporter) PentahoSystem.get( IMetadataDomainRepository.class ) );
    RepositoryFileImportBundle bundle1 = new RepositoryFileImportBundle.Builder()
      .file( repoMetadataFile ).charSet( "UTF-8" ).input( new FileInputStream( metadata ) ).mime( ".xmi" ).withParam(
            "domain-id", "SalesData" ).build();
    metadataImportHandler.importFile( bundle1 );

    final Response salesData = new DataSourceWizardResource().doGetDSWFilesAsDownload( "SalesData" );
    Assert.assertEquals( salesData.getStatus(), Response.Status.OK.getStatusCode() );
    Assert.assertNotNull( salesData.getMetadata() );
    Assert.assertNotNull( salesData.getMetadata().getFirst( "Content-Disposition" ) );
    Assert.assertEquals( salesData.getMetadata().getFirst( "Content-Disposition" ).getClass(), String.class );
    Assert.assertTrue( ( (String) salesData.getMetadata().getFirst( "Content-Disposition" ) ).endsWith( ".xmi\"" ) );
  }


  @Test
  public void testPublishDsw() throws Exception {
    DataSourceWizardResource service = new DataSourceWizardResource();
    Mockery mockery = new Mockery();
    final IPlatformImporter mockImporter = mockery.mock( IPlatformImporter.class );
    mp.defineInstance( IPlatformImporter.class, mockImporter );
    mockery.checking( new Expectations() {
      {
        oneOf( mockImporter ).importFile( with( match( new TypeSafeMatcher<IPlatformImportBundle>() {
          public boolean matchesSafely( IPlatformImportBundle bundle ) {
            return bundle.isPreserveDsw() && bundle.getProperty( "domain-id" ).equals( "AModel.xmi" )
                && bundle.getMimeType().equals( "text/xmi+xml" );
          }
          public void describeTo( Description description ) {
            description.appendText( "bundle with xmi" );
          }
        } ) ) );
        oneOf( mockImporter ).importFile( with( match( new TypeSafeMatcher<IPlatformImportBundle>() {
          public boolean matchesSafely( IPlatformImportBundle bundle ) {
            return bundle.getProperty( "domain-id" ).equals( "AModel" )
                && bundle.getMimeType().equals( "application/vnd.pentaho.mondrian+xml" );
          }
          public void describeTo( Description description ) {
            description.appendText( "bundle with mondrian schema" );
          }
        } ) ) );
      }
    } );
    FileInputStream in = new FileInputStream( new File( new File( "target/test-classes" ), "SampleDataOlap.xmi" ) );
    try {
      Response resp = service.publishDsw( "AModel.xmi", in, true, false, null );
      Assert.assertEquals(
          Response.Status.Family.SUCCESSFUL,
          Response.Status.fromStatusCode( resp.getStatus() ).getFamily() );
      mockery.assertIsSatisfied();
    } finally {
      IOUtils.closeQuietly( in );
    }
  }

  private void testImportFile( String filePath, final String expectedSchemaName ) throws Exception {
    FormDataContentDisposition schemaFileInfo = mock( FormDataContentDisposition.class );
    when( schemaFileInfo.getFileName() ).thenReturn( "stubFileName" );
    Mockery mockery = new Mockery();
    final IPlatformImporter mockImporter = mockery.mock( IPlatformImporter.class );
    mp.defineInstance( IPlatformImporter.class, mockImporter );
    mockery.checking( new Expectations() {
      {
        oneOf( mockImporter ).importFile( with( match( new TypeSafeMatcher<IPlatformImportBundle>() {
          public boolean matchesSafely( IPlatformImportBundle bundle ) {
            return bundle.getProperty( "domain-id" ).equals( expectedSchemaName )
                && bundle.getMimeType().equals( "application/vnd.pentaho.mondrian+xml" );
          }

          public void describeTo( Description description ) {
            description.appendText( "bundle with mondrian schema" );
          }
        } ) ) );
      }
    } );
    AnalysisService service = new AnalysisService();
    FileInputStream in = new FileInputStream( filePath );
    try {
      service.putMondrianSchema( in, schemaFileInfo, null, null, null, false, false,
          "Datasource=SampleData;overwrite=false", null );

      mockery.assertIsSatisfied();
    } finally {
      IOUtils.closeQuietly( in );
    }
  }

  private void testImportZipFile( String filePath, final String expectedSchemaName, boolean annotated ) throws Exception {
    FormDataContentDisposition schemaFileInfo = mock( FormDataContentDisposition.class );
    File f = new File( filePath );
    when( schemaFileInfo.getFileName() ).thenReturn( f.getName() );
    Mockery mockery = new Mockery();
    final IPlatformImporter mockImporter = mockery.mock( IPlatformImporter.class );
    mp.defineInstance( IPlatformImporter.class, mockImporter );
    if ( annotated ) {
      mockery.checking( new Expectations() {
        {
          exactly( 2 ).of( mockImporter ).importFile( with( match( new TypeSafeMatcher<IPlatformImportBundle>() {
            public boolean matchesSafely( IPlatformImportBundle bundle ) {
              return true;
            }
            public void describeTo( Description description ) {
              description.appendText( "bundle with zipped mondrian schema" );
            }
          } ) ) );
        }
      } );
    } else {
      mockery.checking( new Expectations() {
        {
          oneOf( mockImporter ).importFile( with( match( new TypeSafeMatcher<IPlatformImportBundle>() {
            public boolean matchesSafely( IPlatformImportBundle bundle ) {
              return bundle.getProperty( "domain-id" ).equals( expectedSchemaName );
            }
            public void describeTo( Description description ) {
              description.appendText( "bundle with zipped mondrian schema" );
            }
          } ) ) );
        }
      } );
    }
    AnalysisService service = new AnalysisService();
    FileInputStream in = new FileInputStream( filePath );
    try {
      service.putMondrianSchema( in, schemaFileInfo, null, null, null, false, false,
          "Datasource=SampleData;overwrite=false", null );

      mockery.assertIsSatisfied();
    } finally {
      IOUtils.closeQuietly( in );
    }
  }

  @Test
  public void testImportMondrianSchemaWithEncodingIssue() throws Exception {
    testImportFile( "target/test-classes/mysql_steelwheels.mondri_xyz-invalid_encoding.xml", "SteelWheels_xyzxx" );
  }

  @Test
  public void testImportMondrianSchemaWithXmlFormatIssue() throws Exception {
    testImportFile( "target/test-classes/mysql_steelwheels.mondri_xyz-invalid_xml.xml", "SteelWheels_xyzxx1" );
  }

  @Test
  public void testImportMondrianSchemaWithDeclaringEncodingIssue() throws Exception {
    testImportFile( "target/test-classes/mysql_steelwheels.mondri_xyz-invalid_declare_encoding.xml", "SteelWheels_xyzxx" );
  }

  @Test
  public void testImportMondrianSchemaFromZip() throws Exception {
    testImportZipFile( "target/test-classes/mysql_steelwheels_no_annot.zip", "SteelWheels_no_annot", false );
  }

  @Test
  public void testImportAnnotatedMondrianSchemaFromZip() throws Exception {
    testImportZipFile( "target/test-classes/mysql_steelwheels_qq.zip", "SteelWheels_qq", true );
  }

  @Factory
  public static <T> Matcher<T> match( Matcher<T> matcher ) {
    return matcher;
  }

  private static PentahoMetadataDomainRepository createMetadataDomainRepository() throws Exception {
    IUnifiedRepository repository = new FileSystemBackedUnifiedRepository( "target/test-classes/dsw" );
    mp.defineInstance( IUnifiedRepository.class, repository );
    Assert.assertNotNull( new RepositoryUtils( repository ).getFolder( "/etc/metadata", true, true, null ) );
    Assert.assertNotNull( new RepositoryUtils( repository ).getFolder( "/etc/mondrian", true, true, null ) );
    PentahoMetadataDomainRepository pentahoMetadataDomainRepository = new PentahoMetadataDomainRepository( repository );
    return pentahoMetadataDomainRepository;
  }

  private Domain generateModel() {
    Domain domain = null;
    try {

      DatabaseMeta database = new DatabaseMeta();
      database.setDatabaseType( "Hypersonic" ); //$NON-NLS-1$
      database.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
      database.setDBName( "SampleData" ); //$NON-NLS-1$
      database.setName( "SampleData" ); //$NON-NLS-1$

      System.out.println( database.testConnection() );

      TableModelerSource source = new TableModelerSource( database, "ORDERS", null ); //$NON-NLS-1$
      domain = source.generateDomain();

      List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
      OlapDimension dimension = new OlapDimension();
      dimension.setName( "test" ); //$NON-NLS-1$
      dimension.setTimeDimension( false );
      olapDimensions.add( dimension );
      domain.getLogicalModels().get( 1 ).setProperty( "olap_dimensions", olapDimensions ); //$NON-NLS-1$

    } catch ( Exception e ) {
      e.printStackTrace();
    }
    return domain;
  }

  public static class MockDatasourceMgmtService implements IDatasourceMgmtService {

    @Override
    public void init( IPentahoSession arg0 ) {
    }

    @Override
    public String createDatasource( IDatabaseConnection arg0 ) throws DuplicateDatasourceException,
        DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public void deleteDatasourceById( String arg0 ) throws NonExistingDatasourceException, DatasourceMgmtServiceException {

    }

    @Override
    public void deleteDatasourceByName( String arg0 ) throws NonExistingDatasourceException,
        DatasourceMgmtServiceException {

    }

    @Override
    public IDatabaseConnection getDatasourceById( String arg0 ) throws DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public IDatabaseConnection getDatasourceByName( String arg0 ) throws DatasourceMgmtServiceException {
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
    public String updateDatasourceById( String arg0, IDatabaseConnection arg1 ) throws NonExistingDatasourceException,
        DatasourceMgmtServiceException {
      return null;
    }

    @Override
    public String updateDatasourceByName( String arg0, IDatabaseConnection arg1 ) throws NonExistingDatasourceException,
        DatasourceMgmtServiceException {
      return null;
    }
  }

  public static class MockUserDetailService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername( String name ) throws UsernameNotFoundException, DataAccessException {

      List<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
      authList.add( new SimpleGrantedAuthority( "Authenticated" ) );
      authList.add( new SimpleGrantedAuthority( "Administrator" ) );

      UserDetails user = new User( name, "password", true, true, true, true, authList );

      return user;
    }

  }

  public static class TestFileSystemBackedUnifiedRepository extends FileSystemBackedUnifiedRepository {
    public TestFileSystemBackedUnifiedRepository() {
      super( "bin/test-solutions/solution" );
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
    public String getUserHomeFolderName( String arg0 ) {

      return null;
    }

    @Override
    public String getUserHomeFolderPath( String arg0 ) {

      return null;
    }

  }
}
