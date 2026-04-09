package org.pentaho.platform.dataaccess.datasource.utils;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.PartitionDatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for DatabaseConnectionUtils
 */
public class DatabaseConnectionUtilsTest {

  private VariableSpace variableSpace;
  private IDatabaseConnection connection;

  @Before
  public void setUp() {
    variableSpace = new Variables();
    variableSpace.setVariable( "DB_HOST", "prod-host.example.com" );
    variableSpace.setVariable( "DB_NAME", "production_db" );
    variableSpace.setVariable( "DB_PORT", "5432" );
    variableSpace.setVariable( "DB_USER", "prod_user" );
    variableSpace.setVariable( "DB_PASS", "secret123" );
    variableSpace.setVariable( "SQL_INIT", "SET SESSION max_connections = 10;" );
    variableSpace.setVariable( "POOL_SIZE", "20" );
    variableSpace.setVariable( "CONN_NAME", "prod_db_connection" );

    connection = new DatabaseConnection();
  }

  @Test
  public void testSubstituteConnectionName() {
    connection.setName( "${CONN_NAME}" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "prod_db_connection", connection.getName() );
  }

  @Test
  public void testSubstituteBasicConnectionFields() {
    connection.setHostname( "${DB_HOST}" );
    connection.setDatabaseName( "${DB_NAME}" );
    connection.setDatabasePort( "${DB_PORT}" );
    connection.setUsername( "${DB_USER}" );
    connection.setPassword( "${DB_PASS}" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "prod-host.example.com", connection.getHostname() );
    assertEquals( "production_db", connection.getDatabaseName() );
    assertEquals( "5432", connection.getDatabasePort() );
    assertEquals( "prod_user", connection.getUsername() );
    assertEquals( "secret123", connection.getPassword() );
  }

  @Test
  public void testSubstituteConnectSql() {
    connection.setConnectSql( "${SQL_INIT}" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "SET SESSION max_connections = 10;", connection.getConnectSql() );
  }

  @Test
  public void testSubstituteSQLServerInstance() {
    connection.setSQLServerInstance( "${DB_HOST}\\SQLEXPRESS" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "prod-host.example.com\\SQLEXPRESS", connection.getSQLServerInstance() );
  }

  @Test
  public void testSubstituteDataTablespace() {
    variableSpace.setVariable( "DATA_TS", "users_tablespace" );
    connection.setDataTablespace( "${DATA_TS}" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "users_tablespace", connection.getDataTablespace() );
  }

  @Test
  public void testSubstituteIndexTablespace() {
    variableSpace.setVariable( "INDEX_TS", "indexes_tablespace" );
    connection.setIndexTablespace( "${INDEX_TS}" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "indexes_tablespace", connection.getIndexTablespace() );
  }

  @Test
  public void testSubstituteTablespacesWithMixedContent() {
    variableSpace.setVariable( "DATA_TS", "users_ts" );
    variableSpace.setVariable( "INDEX_TS", "indexes_ts" );
    connection.setDataTablespace( "prefix_${DATA_TS}_suffix" );
    connection.setIndexTablespace( "idx_${INDEX_TS}_end" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "prefix_users_ts_suffix", connection.getDataTablespace() );
    assertEquals( "idx_indexes_ts_end", connection.getIndexTablespace() );
  }

  @Test
  public void testSubstituteConnectionNameWithPrefix() {
    connection.setName( "env_${CONN_NAME}_db" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "env_prod_db_connection_db", connection.getName() );
  }

  @Test
  public void testSubstituteConnectionPoolingProperties() {
    Map<String, String> poolingProps = new HashMap<>();
    poolingProps.put( "maxPoolSize", "${POOL_SIZE}" );
    poolingProps.put( "minPoolSize", "5" );
    poolingProps.put( "timeout", "${DB_PORT}" ); // reuse a variable

    connection.setConnectionPoolingProperties( poolingProps );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    Map<String, String> result = connection.getConnectionPoolingProperties();
    assertEquals( "20", result.get( "maxPoolSize" ) );
    assertEquals( "5", result.get( "minPoolSize" ) );
    assertEquals( "5432", result.get( "timeout" ) );
  }

  @Test
  public void testNullFieldsAreNotModified() {
    connection.setHostname( null );
    connection.setDatabaseName( null );
    connection.setDatabasePort( null );
    connection.setUsername( null );
    connection.setPassword( null );
    connection.setConnectSql( null );
    connection.setSQLServerInstance( null );

    // Should not throw any exception
    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertNull( connection.getHostname() );
    assertNull( connection.getDatabaseName() );
    assertNull( connection.getDatabasePort() );
    assertNull( connection.getUsername() );
    assertNull( connection.getPassword() );
    assertNull( connection.getConnectSql() );
    assertNull( connection.getSQLServerInstance() );
  }

  @Test
  public void testEmptyConnectionPoolingProperties() {
    Map<String, String> poolingProps = new HashMap<>();
    connection.setConnectionPoolingProperties( poolingProps );

    // Should not throw any exception
    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertNotNull( connection.getConnectionPoolingProperties() );
    assertEquals( 0, connection.getConnectionPoolingProperties().size() );
  }

  @Test
  public void testNullConnectionPoolingProperties() {
    connection.setConnectionPoolingProperties( null );

    // Should not throw any exception
    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertNull( connection.getConnectionPoolingProperties() );
  }

  @Test
  public void testPartitionedConnectionSubstitution() {
    connection.setHostname( "${DB_HOST}" );
    connection.setPartitioned( true );

    // Create partitions
    List<PartitionDatabaseMeta> partitions = new ArrayList<>();

    PartitionDatabaseMeta partition1 = new PartitionDatabaseMeta();
    partition1.setHostname( "partition1-${DB_NAME}" );
    partition1.setPort( "${DB_PORT}" );
    partition1.setDatabaseName( "db_${DB_PORT}" );
    partitions.add( partition1 );

    PartitionDatabaseMeta partition2 = new PartitionDatabaseMeta();
    partition2.setHostname( "${DB_HOST}" );
    partition2.setPort( "3306" );
    partition2.setDatabaseName( "${DB_NAME}" );
    partitions.add( partition2 );

    connection.setPartitioningInformation( partitions );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "prod-host.example.com", connection.getHostname() );
    assertEquals( "partition1-production_db", partition1.getHostname() );
    assertEquals( "5432", partition1.getPort() );
    assertEquals( "db_5432", partition1.getDatabaseName() );

    assertEquals( "prod-host.example.com", partition2.getHostname() );
    assertEquals( "3306", partition2.getPort() );
    assertEquals( "production_db", partition2.getDatabaseName() );
  }

  @Test
  public void testNullConnectionIsIgnored() {
    // Should not throw any exception
    DatabaseConnectionUtils.applyEnvironmentSubstitution( null, variableSpace );
  }

  @Test
  public void testNullVariableSpaceIsIgnored() {
    connection.setHostname( "${DB_HOST}" );

    // Should not throw any exception
    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, null );

    // Connection should remain unchanged
    assertEquals( "${DB_HOST}", connection.getHostname() );
  }

  @Test
  public void testMixedSubstitutionAndLiterals() {
    connection.setHostname( "host-${DB_HOST}.local" );
    connection.setDatabaseName( "db_prefix_${DB_NAME}" );
    connection.setDatabasePort( "3${DB_PORT}" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    assertEquals( "host-prod-host.example.com.local", connection.getHostname() );
    assertEquals( "db_prefix_production_db", connection.getDatabaseName() );
    assertEquals( "35432", connection.getDatabasePort() );
  }

  @Test
  public void testUnresolvedVariablesRemainUnchanged() {
    connection.setHostname( "${UNKNOWN_VAR}" );
    connection.setDatabaseName( "db_${ANOTHER_UNKNOWN}" );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    // Kettle's environmentSubstitute returns the original string if variable is not found
    assertEquals( "${UNKNOWN_VAR}", connection.getHostname() );
    assertEquals( "db_${ANOTHER_UNKNOWN}", connection.getDatabaseName() );
  }

  @Test
  public void testConnectionPoolingPropertiesWithNullValues() {
    Map<String, String> poolingProps = new HashMap<>();
    poolingProps.put( "key1", "${POOL_SIZE}" );
    poolingProps.put( "key2", null );

    connection.setConnectionPoolingProperties( poolingProps );

    DatabaseConnectionUtils.applyEnvironmentSubstitution( connection, variableSpace );

    Map<String, String> result = connection.getConnectionPoolingProperties();
    assertEquals( "20", result.get( "key1" ) );
    assertNull( result.get( "key2" ) );
  }

}
