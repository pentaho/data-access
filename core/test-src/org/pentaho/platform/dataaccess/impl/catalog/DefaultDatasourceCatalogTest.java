package org.pentaho.platform.dataaccess.impl.catalog;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.pathPropertyPair;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetChildren;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetData;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFile;
import static org.pentaho.platform.repository2.unified.UnifiedRepositoryTestUtils.stubGetFolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.di.core.encryption.Encr;
import org.pentaho.di.core.encryption.TwoWayPasswordEncoderPluginType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.dataaccess.api.catalog.IDatasource;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceCatalog;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceProvider;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceType;
import org.pentaho.platform.dataaccess.datasource.provider.AnalysisDatasourceProvider;
import org.pentaho.platform.dataaccess.datasource.provider.AnalysisDatasourceType;
import org.pentaho.platform.dataaccess.datasource.provider.JDBCDatasourceProvider;
import org.pentaho.platform.dataaccess.datasource.provider.JDBCDatasourceType;
import org.pentaho.platform.dataaccess.datasource.provider.MetadataDatasourceProvider;
import org.pentaho.platform.dataaccess.datasource.provider.MetadataDatasourceType;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.repository.JcrBackedDatasourceMgmtService;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.test.platform.engine.core.MicroPlatform;

public class DefaultDatasourceCatalogTest {

  private IDatasourceCatalog datasourceCatalog;
  private static final String EXP_DBMETA_NAME = "haha";
  private static final List<Character> reservedChars = Collections.emptyList();
  IUnifiedRepository repo;
  MicroPlatform mp;

  @Before
  public void setUp() throws Exception {
    repo = mock( IUnifiedRepository.class );
    mp = new MicroPlatform();
    mp.defineInstance( IUnifiedRepository.class, repo );
    mp.start();
    List<IDatasourceProvider> datasourceProviders = new ArrayList<IDatasourceProvider>();
    datasourceProviders.add( createJDBCProvider() );
    datasourceProviders.add( createAnalysisDatasourceProvider() );
    datasourceProviders.add( createMetadataDatasourceProvider() );
    datasourceCatalog = new DatasourceCatalog( datasourceProviders );

    PluginRegistry.addPluginType( TwoWayPasswordEncoderPluginType.getInstance() );
    PluginRegistry.init();
    Encr.init( "Kettle" );
  }

  @After
  public void tearDown() throws Exception {
    mp.stop();
  }

  private JDBCDatasourceProvider createJDBCProvider() {
    final String fileId = "456";
    final String databasesFolderPath = "/etc/pdi/databases";
    final String dotKdb = ".kdb";
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "123", "databases" ).folder( true ).build() ).when( repo ).getFile(
        databasesFolderPath );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    // stub out get file to update
    RepositoryFile f =
        new RepositoryFile.Builder( fileId, EXP_DBMETA_NAME + dotKdb ).path(
            databasesFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotKdb ).build();
    doReturn( f ).when( repo ).getFile( databasesFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotKdb );

    final String EXP_HOST_NAME = "hello";
    DataNode rootNode = new DataNode( "databaseMeta" );
    rootNode.setProperty( "TYPE", "Hypersonic" ); // required
    rootNode.setProperty( "HOST_NAME", EXP_HOST_NAME );
    rootNode.addNode( "attributes" ); // required
    doReturn( new NodeRepositoryFileData( rootNode ) ).when( repo ).getDataForRead( eq( fileId ),
        eq( NodeRepositoryFileData.class ) );

    DataNode secondNode = new DataNode( "databaseMeta" );
    rootNode.setProperty( "TYPE", "Hypersonic" ); // required
    rootNode.setProperty( "HOST_NAME", EXP_HOST_NAME );
    rootNode.addNode( "attributes" ); // required
    doReturn( new NodeRepositoryFileData( secondNode ) ).when( repo ).getDataForRead( eq( "234" ),
        eq( NodeRepositoryFileData.class ) );
    doReturn( new NodeRepositoryFileData( rootNode ) ).when( repo ).getDataForRead( eq( "123" ),
        eq( NodeRepositoryFileData.class ) );
    RepositoryFile file1 =
        new RepositoryFile.Builder( "123", "mydb1" + dotKdb ).path(
            databasesFolderPath + RepositoryFile.SEPARATOR + "mydb1" + dotKdb ).build();
    RepositoryFile file2 =
        new RepositoryFile.Builder( "234", "mydb2" + dotKdb ).path(
            databasesFolderPath + RepositoryFile.SEPARATOR + "mydb2" + dotKdb ).build();
    List<RepositoryFile> fileList = new ArrayList<RepositoryFile>();
    fileList.add( file1 );
    fileList.add( file2 );

    doReturn( fileList ).when( repo ).getChildren( Mockito.anyString(),
        eq( "*" + RepositoryObjectType.DATABASE.getExtension() ) );
    IDatasourceMgmtService datasourceMgmtService =
        new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    return new JDBCDatasourceProvider( datasourceMgmtService );
  }

  private AnalysisDatasourceProvider createAnalysisDatasourceProvider() {
    final String fileId = "456";
    final String mondrianFolderPath = "/etc/mondrian";
    final String olap4jFolderPath = "/etc/olap-servers";

    // Stub the olap servers folder
    stubGetFolder( repo, olap4jFolderPath );
    stubGetChildren( repo, olap4jFolderPath );

    doReturn( new RepositoryFile.Builder( "123", "mondrian" ).folder( true ).build() ).when( repo ).getFile(
        mondrianFolderPath );
    doReturn( reservedChars ).when( repo ).getReservedChars();
    String[] names = new String[2];
    names[0] = "SampleData";
    names[1] = "SteelWheels";
    stubGetChildren( repo, mondrianFolderPath, names );

    File file1 = new File( "test-res/solution1/system/steelwheels.mondrian.xml" );
    String mondrianSchema1 = null;
    String mondrianSchema2 = null;
    try {
      mondrianSchema1 = IOUtils.toString( new FileInputStream( file1 ) );
      File file2 = new File( "test-res/solution1/system/SampleData.mondrian.xml" );
      mondrianSchema2 = IOUtils.toString( new FileInputStream( file2 ) );
    } catch ( FileNotFoundException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch ( IOException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    stubGetFolder( repo, mondrianFolderPath );
    stubGetChildren( repo, mondrianFolderPath, "SampleData/", "SteelWheels/" ); // return two child folders

    final String sampleDataFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SampleData";
    final String sampleDataMetadataPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String sampleDataSchemaPath = sampleDataFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    stubGetFile( repo, sampleDataMetadataPath );
    stubGetData( repo, sampleDataMetadataPath, "catalog", pathPropertyPair( "/catalog/definition",
        "mondrian:/SampleData" ), pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SampleData;" ) );
    stubGetFile( repo, sampleDataSchemaPath );
    stubGetData( repo, sampleDataSchemaPath, mondrianSchema2 );

    final String steelWheelsFolderPath = mondrianFolderPath + RepositoryFile.SEPARATOR + "SteelWheels";
    final String steelWheelsMetadataPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "metadata";
    final String steelWheelsSchemaPath = steelWheelsFolderPath + RepositoryFile.SEPARATOR + "schema.xml";
    stubGetFile( repo, steelWheelsMetadataPath );
    stubGetData( repo, steelWheelsMetadataPath, "catalog", pathPropertyPair( "/catalog/definition",
        "mondrian:/SteelWheels" ), pathPropertyPair( "/catalog/datasourceInfo",
        "Provider=mondrian;DataSource=SteelWheels;" ) );
    stubGetFile( repo, steelWheelsSchemaPath );
    stubGetData( repo, steelWheelsSchemaPath, mondrianSchema1 );

    IPentahoSession session = new StandaloneSession( "admin" );
    MondrianCatalogHelper helper = new MondrianCatalogHelper();

    List<MondrianCatalog> cats = helper.listCatalogs( session, false );

    return new AnalysisDatasourceProvider( helper );
  }

  private MetadataDatasourceProvider createMetadataDatasourceProvider() {
    final String fileId = "456";
    final String metadataFolderPath = "/etc/metadata";
    final String dotXMI = ".xmi";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
    // stub out get parent folder
    doReturn( new RepositoryFile.Builder( "456", "metadata" ).folder( true ).build() ).when( repo ).getFile(
        metadataFolderPath );
    // stub out get file to update
    RepositoryFile f =
        new RepositoryFile.Builder( fileId, EXP_DBMETA_NAME + dotXMI ).path(
            metadataFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotXMI ).build();
    doReturn( f ).when( repo ).getFile( metadataFolderPath + RepositoryFile.SEPARATOR + EXP_DBMETA_NAME + dotXMI );
    Map<String, Serializable> fileMetadata1 = new HashMap<String, Serializable>();
    fileMetadata1.put( "file-type", "domain" );
    fileMetadata1.put( "domain-id", "ba-pom" );
    Map<String, Serializable> fileMetadata2 = new HashMap<String, Serializable>();
    fileMetadata2.put( "file-type", "domain" );
    fileMetadata2.put( "domain-id", "steel-wheels" );
    Map<String, Serializable> fileMetadata3 = new HashMap<String, Serializable>();
    fileMetadata3.put( "file-type", "domain" );
    fileMetadata3.put( "domain-id", "PDI Operations Mart Sample Reports/metadata.xmi" );

    RepositoryFile file1 =
        new RepositoryFile.Builder( "123", "mydb1" + dotXMI ).path(
            metadataFolderPath + RepositoryFile.SEPARATOR + "mydb1" + dotXMI ).build();
    RepositoryFile file2 =
        new RepositoryFile.Builder( "234", "mydb2" + dotXMI ).path(
            metadataFolderPath + RepositoryFile.SEPARATOR + "mydb2" + dotXMI ).build();
    RepositoryFile file3 =
        new RepositoryFile.Builder( "345", "mydb3" + dotXMI ).path(
            metadataFolderPath + RepositoryFile.SEPARATOR + "mydb3" + dotXMI ).build();

    doReturn( file1 ).when( repo ).getFile( metadataFolderPath + RepositoryFile.SEPARATOR + "123" + dotXMI );
    doReturn( file2 ).when( repo ).getFile( metadataFolderPath + RepositoryFile.SEPARATOR + "234" + dotXMI );
    doReturn( file3 ).when( repo ).getFile( metadataFolderPath + RepositoryFile.SEPARATOR + "345" + dotXMI );

    doReturn( fileMetadata1 ).when( repo ).getFileMetadata( "123" );
    doReturn( fileMetadata2 ).when( repo ).getFileMetadata( "234" );
    doReturn( fileMetadata3 ).when( repo ).getFileMetadata( "345" );

    List<RepositoryFile> fileList = new ArrayList<RepositoryFile>();
    fileList.add( file1 );
    fileList.add( file2 );
    fileList.add( file3 );

    doReturn( fileList ).when( repo ).getChildren( Mockito.anyString(), eq( "*" ) );

    PentahoMetadataDomainRepository domainRepository = new PentahoMetadataDomainRepository( repo );

    return new MetadataDatasourceProvider( domainRepository );
  }

  @Test
  public void testGetDatasources() throws Exception {
    List<IDatasource> datasources = datasourceCatalog.getDatasources();
    assertEquals( 7, datasources.size() );
    List<IDatasourceType> types = datasourceCatalog.getDatasourceTypes();
    assertEquals( 3, types.size() );
    Locale locale = LocaleHelper.getDefaultLocale();

    assertEquals( types.get( 0 ).getDisplayName( locale ), "SQL Connection" );
    assertEquals( types.get( 1 ).getDisplayName( locale ), "Analysis" );
    assertEquals( types.get( 2 ).getDisplayName( locale ), "Metadata" );

    assertEquals( types.get( 0 ).getId(), "JDBC" );
    assertEquals( types.get( 1 ).getId(), "ANALYSIS" );
    assertEquals( types.get( 2 ).getId(), "METADATA" );

    List<IDatasource> jdbcDatasources = datasourceCatalog.getDatasourcesOfType( new JDBCDatasourceType() );
    assertEquals( 2, jdbcDatasources.size() );
    List<IDatasource> metadataDatasources = datasourceCatalog.getDatasourcesOfType( new MetadataDatasourceType() );
    assertEquals( 3, metadataDatasources.size() );
    List<IDatasource> analysisDatasources = datasourceCatalog.getDatasourcesOfType( new AnalysisDatasourceType() );
    assertEquals( 2, analysisDatasources.size() );
  }

}
