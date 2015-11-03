package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.model.PartitionDatabaseMeta;
import org.pentaho.database.util.DatabaseTypeHelper;

import com.google.gwtmockito.GwtMockitoTestRunner;

@RunWith(GwtMockitoTestRunner.class)
public class DatabaseConnectionConverterTest{

  @Test
  public void testConvertToXml() {
    DatabaseConnection dbConnection = new DatabaseConnection();
    dbConnection.setId( "my id" );
    dbConnection.setAccessType( DatabaseAccessType.NATIVE );
    List<DatabaseAccessType> accessTypes = new LinkedList<DatabaseAccessType>();
    accessTypes.add( DatabaseAccessType.NATIVE );
    DatabaseType dbType = new DatabaseType( "name", "short name", accessTypes, 100500, "helpUri" );
    dbConnection.setDatabaseType( dbType );
    Map<String, String> extraOptions = new HashMap<String, String>();
    extraOptions.put( "opt", "value" );
    dbConnection.setExtraOptions( extraOptions );
    dbConnection.setName( "Best name" );
    dbConnection.setHostname( "localhost" );
    dbConnection.setDatabaseName( "foodmart" );
    dbConnection.setDatabasePort( "2233" );
    dbConnection.setUsername( "username" );
    dbConnection.setPassword( "password" );
    dbConnection.setStreamingResults( true );
    dbConnection.setDataTablespace( "tables" );
    dbConnection.setIndexTablespace( "indexes" );
    dbConnection.setSQLServerInstance( "INSTANCE_0" );
    dbConnection.setUsingDoubleDecimalAsSchemaTableSeparator( true );
    dbConnection.setInformixServername( "INFORM_1" );
    dbConnection.addExtraOption( "100", "option", "value" );
    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put( "attr1", "value" );
    dbConnection.setAttributes( attributes );
    dbConnection.setChanged( true );
    dbConnection.setQuoteAllFields( true );
    dbConnection.setForcingIdentifiersToLowerCase( true );
    dbConnection.setForcingIdentifiersToUpperCase( true );
    dbConnection.setConnectSql( "select * from 1" );
    dbConnection.setUsingConnectionPool( true );
    dbConnection.setInitialPoolSize( 3 );
    dbConnection.setMaximumPoolSize( 9 );
    dbConnection.setPartitioned( true );
    Map<String, String> connectionPoolingProperties = new HashMap<String, String>();
    connectionPoolingProperties.put( "pool", "abc" );
    dbConnection.setConnectionPoolingProperties( connectionPoolingProperties );
    List<PartitionDatabaseMeta> partitioningInformation = new LinkedList<PartitionDatabaseMeta>();
    PartitionDatabaseMeta pdm = new PartitionDatabaseMeta();
    partitioningInformation.add( pdm );
    dbConnection.setPartitioningInformation( partitioningInformation );

    List<IDatabaseType> databaseTypes = new LinkedList<IDatabaseType>();
    databaseTypes.add( dbType );
    DatabaseTypeHelper dbh = new DatabaseTypeHelper( databaseTypes );
    DatabaseConnectionConverter dbcc = new DatabaseConnectionConverter( dbh );
    String xmlCOnnection = dbcc.convertToXml( dbConnection );
  }
}
