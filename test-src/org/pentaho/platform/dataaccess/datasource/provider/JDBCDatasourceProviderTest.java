package org.pentaho.platform.dataaccess.datasource.provider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFile;
import org.pentaho.platform.api.repository2.unified.data.node.DataNode;
import org.pentaho.platform.api.repository2.unified.data.node.NodeRepositoryFileData;
import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider;
import org.pentaho.platform.repository.JcrBackedDatasourceMgmtService;

public class JDBCDatasourceProviderTest {

  private static final String EXP_DBMETA_NAME = "haha";

  private static final List<Character> reservedChars = Collections.emptyList();

  public JDBCDatasourceProviderTest() {
    super();
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    KettleEnvironment.init();
    if ( !KettleEnvironment.isInitialized() ) {
      throw new Exception( "Kettle Environment not initialized" );
    }
  }

  @After
  public void tearDown() throws Exception {
  }
  
  @Test
  public void testGetDatasources() throws Exception {
    final String fileId = "456";
    final String databasesFolderPath = "/etc/pdi/databases";
    final String dotKdb = ".kdb";
    IUnifiedRepository repo = mock( IUnifiedRepository.class );
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
    
    RepositoryFile file1 = new RepositoryFile.Builder( "123", "mydb1" + dotKdb ).path(
        databasesFolderPath + RepositoryFile.SEPARATOR + "mydb1" + dotKdb ).build(); 
    RepositoryFile file2 = new RepositoryFile.Builder( "234", "mydb2" + dotKdb ).path(
        databasesFolderPath + RepositoryFile.SEPARATOR + "mydb2" + dotKdb ).build(); 
    List<RepositoryFile> fileList = new ArrayList<RepositoryFile>();
    fileList.add( file1 );
    fileList.add( file2 );

    doReturn( fileList ).when( repo ).getChildren(Mockito.anyString(),  Mockito.anyString(), Mockito.anyBoolean()  );
    
    IDatasourceMgmtService datasourceMgmtService =
      new JcrBackedDatasourceMgmtService( repo, new DatabaseDialectService() );

    IDatasourceProvider jdbcDatasourceProvider = new JDBCDatasourceProvider( datasourceMgmtService );
    
    List<IDatasource> datasources = jdbcDatasourceProvider.getDatasources();
    assertEquals( 2, datasources.size() );
  }
}
