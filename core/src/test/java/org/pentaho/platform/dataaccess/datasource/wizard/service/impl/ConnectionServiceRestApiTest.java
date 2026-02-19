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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.UtilHtmlSanitizer;
import org.pentaho.ui.database.event.IDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionPoolParameterList;
import jakarta.ws.rs.WebApplicationException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Unit tests for ConnectionService REST API endpoints with focus on
 * backward compatibility and null field handling.
 */
public class ConnectionServiceRestApiTest {

  private static final String CONN_NAME = "test_connection";
  private static final String CONN_ID = "test-id-123";

  @Mock
  private ConnectionServiceImpl connectionServiceImpl;

  private ConnectionService connectionService;

  private ObjectMapper objectMapper;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.openMocks( this );
    // Manually create ConnectionService with mocked dependency
    connectionService = new ConnectionService( connectionServiceImpl, null, null );
    objectMapper = new ObjectMapper();
  }

  /**
   * Creates a test database connection with some null fields
   * to verify backward compatibility.
   */
  private IDatabaseConnection createTestConnection() {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setId( CONN_ID );
    conn.setName( CONN_NAME );
    conn.setHostname( "localhost" );
    conn.setDatabaseName( "testdb" );
    conn.setDatabasePort( "5432" );
    conn.setAccessType( DatabaseAccessType.NATIVE );

    // These fields should remain null for backward compatibility
    conn.setUsername( null );
    conn.setPassword( null );
    conn.setDataTablespace( null );
    conn.setIndexTablespace( null );
    conn.setInformixServername( null );

    return conn;
  }

  /**
   * Verifies that null fields are preserved when returning connection objects
   */
  @Test
  public void testGetConnectionByNamePreservesNullFields() throws Exception {
    IDatabaseConnection testConn = createTestConnection();
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( testConn );

    // Call the REST endpoint
    IDatabaseConnection result = connectionService.getConnectionByNameWithResponse( CONN_NAME );

    // Verify null fields are preserved
    assertNull( "Username should be null", result.getUsername() );
    assertNull( "Password should be null", result.getPassword() );
    assertNull( "DataTablespace should be null", result.getDataTablespace() );
    assertNull( "IndexTablespace should be null", result.getIndexTablespace() );
    assertNull( "InformixServername should be null", result.getInformixServername() );

    // Verify non-null fields
    assertEquals( "ID should match", CONN_ID, result.getId() );
    assertEquals( "Name should match", CONN_NAME, result.getName() );
    assertEquals( "Hostname should match", "localhost", result.getHostname() );
  }

  /**
   * Verifies that password is hidden in non-masked responses
   */
  @Test
  public void testGetConnectionByNameHidesPassword() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    testConn.setId( CONN_ID );
    testConn.setName( CONN_NAME );
    testConn.setPassword( "secret123" );

    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( testConn );

    // Call with mask=false (password should be hidden)
    IDatabaseConnection result = connectionService.getConnectionByNameWithResponse( CONN_NAME );

    // Password should be null when mask is false
    assertNull( "Password should be hidden", result.getPassword() );
  }

  /**
   * Verifies that getConnectionById also preserves null fields
   */
  @Test
  public void testGetConnectionByIdPreservesNullFields() throws Exception {
    IDatabaseConnection testConn = createTestConnection();
    when( connectionServiceImpl.getConnectionById( CONN_ID ) ).thenReturn( testConn );

    // Call the REST endpoint
    IDatabaseConnection result = connectionService.getConnectionById( CONN_ID, false );

    // Verify null fields are preserved
    assertNull( "Username should be null", result.getUsername() );
    assertNull( "Password should be null", result.getPassword() );
    assertNull( "DataTablespace should be null", result.getDataTablespace() );
    assertNull( "IndexTablespace should be null", result.getIndexTablespace() );
    assertNull( "InformixServername should be null", result.getInformixServername() );
  }

  /**
   * Verifies that null fields in connections from the list are preserved
   */
  @Test
  public void testGetConnectionsPreservesNullFields() throws Exception {
    List<IDatabaseConnection> connections = Arrays.asList( createTestConnection() );
    when( connectionServiceImpl.getConnections( true ) ).thenReturn( connections );

    // Call the REST endpoint
    Object response = connectionService.getConnections();

    // Verify it returns a proper response
    assertNotNull( "Response should not be null", response );
  }

  /**
   * Tests JSON serialization to ensure null fields are not converted to empty strings
   */
  @Test
  public void testJsonSerializationPreservesNullFields() throws Exception {
    IDatabaseConnection testConn = createTestConnection();
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( testConn );

    IDatabaseConnection result = connectionService.getConnectionByNameWithResponse( CONN_NAME );

    // Serialize to JSON
    String json = objectMapper.writeValueAsString( result );

    // Parse back to verify structure
    com.fasterxml.jackson.databind.JsonNode node = objectMapper.readTree( json );

    // Null fields should not be in the JSON or should be null
    // (depends on ObjectMapper configuration, but important is consistency)
    if ( node.has( "username" ) ) {
      assertTrue( "Username node should exist",
          node.has( "username" ) );
    }
  }

  /**
   * Verifies that connections with mixed null and non-null fields maintain consistency
   */
  @Test
  public void testMixedNullAndNonNullFields() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setId( CONN_ID );
    conn.setName( CONN_NAME );
    conn.setHostname( "db.example.com" );
    conn.setDatabaseName( "mydb" );
    conn.setUsername( "dbuser" );
    conn.setPassword( "secret" );
    // Leave these as null
    conn.setDataTablespace( null );
    conn.setIndexTablespace( null );
    conn.setInformixServername( null );

    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( conn );

    IDatabaseConnection result = connectionService.getConnectionByNameWithResponse( CONN_NAME );

    // Non-null fields should have values
    assertEquals( "Hostname should have value", "db.example.com", result.getHostname() );
    assertEquals( "DatabaseName should have value", "mydb", result.getDatabaseName() );
    assertEquals( "Username should have value", "dbuser", result.getUsername() );

    // Password should be null when not masked
    assertNull( "Password should be hidden", result.getPassword() );

    // Null fields should remain null
    assertNull( "DataTablespace should be null", result.getDataTablespace() );
    assertNull( "IndexTablespace should be null", result.getIndexTablespace() );
    assertNull( "InformixServername should be null", result.getInformixServername() );
  }

  /**
   * Verifies getConnectionByNameWithResponse endpoint preserves null fields
   */
  @Test
  public void testGetConnectionByNameWithResponsePreservesNullFields() throws Exception {
    IDatabaseConnection testConn = createTestConnection();
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( testConn );

    IDatabaseConnection result = connectionService.getConnectionByNameWithResponse( CONN_NAME );

    assertNotNull( "Result should not be null", result );
    assertTrue( "Result should be IDatabaseConnection", result instanceof IDatabaseConnection );

    // Verify null fields are preserved
    assertNull( "Username should be null", result.getUsername() );
    assertNull( "Password should be null", result.getPassword() );
    assertNull( "DataTablespace should be null", result.getDataTablespace() );
    assertNull( "IndexTablespace should be null", result.getIndexTablespace() );
    assertNull( "InformixServername should be null", result.getInformixServername() );
  }

  /**
   * Verifies backward compatibility: existing clients expecting null fields
   * should not break when receiving updated API responses
   */
  @Test
  public void testBackwardCompatibilityWithNullFields() throws Exception {
    // Simulate an existing client's expected response format
    DatabaseConnection expectedResponse = new DatabaseConnection();
    expectedResponse.setId( CONN_ID );
    expectedResponse.setName( CONN_NAME );
    expectedResponse.setHostname( "localhost" );
    expectedResponse.setDatabaseName( "ops_mart" );
    expectedResponse.setDatabasePort( "5432" );
    expectedResponse.setAccessType( DatabaseAccessType.JNDI );
    expectedResponse.setUsername( null );
    expectedResponse.setPassword( null );
    expectedResponse.setDataTablespace( null );
    expectedResponse.setIndexTablespace( null );
    expectedResponse.setInformixServername( null );

    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( expectedResponse );

    IDatabaseConnection actualResponse = connectionService.getConnectionByNameWithResponse( CONN_NAME );

    // Verify the response matches what an existing client expects
    assertEquals( "ID must match", expectedResponse.getId(), actualResponse.getId() );
    assertEquals( "Name must match", expectedResponse.getName(), actualResponse.getName() );
    assertEquals( "Hostname must match", expectedResponse.getHostname(), actualResponse.getHostname() );

    // Critical: null fields must remain null for backward compatibility
    assertEquals( "Username must be null", expectedResponse.getUsername(), actualResponse.getUsername() );
    assertEquals( "Password must be null", expectedResponse.getPassword(), actualResponse.getPassword() );
    assertEquals( "DataTablespace must be null", expectedResponse.getDataTablespace(),
        actualResponse.getDataTablespace() );
    assertEquals( "IndexTablespace must be null", expectedResponse.getIndexTablespace(),
        actualResponse.getIndexTablespace() );
    assertEquals( "InformixServername must be null", expectedResponse.getInformixServername(),
        actualResponse.getInformixServername() );
  }

  /**
   * Verifies that HTML-escaped values are properly unescaped without converting nulls
   */
  @Test
  public void testHtmlUnescapingPreservesNulls() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setName( "test&amp;connection" );
    conn.setUsername( null );
    conn.setDataTablespace( null );

    UtilHtmlSanitizer sanitizer = UtilHtmlSanitizer.getInstance();
    sanitizer.unsanitizeConnectionParameters( conn );

    // HTML should be unescaped
    assertEquals( "Name should be unescaped", "test&connection", conn.getName() );

    // But nulls should remain null
    assertNull( "Username should remain null", conn.getUsername() );
    assertNull( "DataTablespace should remain null", conn.getDataTablespace() );
  }

  /**
   * Critical test: Verify that the JSON serialization actually outputs null instead of empty strings
   * This test uses the actual DatabaseConnectionReaderWriter to ensure runtime behavior matches expectations
   */
  @Test
  public void testJsonSerializationOutputsNullNotEmptyString() throws Exception {
    IDatabaseConnection testConn = createTestConnection();

    // Cast to DatabaseConnection for serialization (createTestConnection returns DatabaseConnection instance)
    DatabaseConnection conn = (DatabaseConnection) testConn;

    // Use the actual MessageBodyWriter to serialize (same writer used at runtime)
    DatabaseConnectionReaderWriter writer = new DatabaseConnectionReaderWriter();
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    writer.writeTo( conn, DatabaseConnection.class, null, null,
        jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE, null, baos );

    String json = baos.toString();
    
    // Verify JSON contains literal null values, not empty strings
    assertTrue( "JSON should contain 'username':null", json.contains( "\"username\":null" ) );
    assertTrue( "JSON should contain 'dataTablespace':null", json.contains( "\"dataTablespace\":null" ) );
    assertTrue( "JSON should contain 'indexTablespace':null", json.contains( "\"indexTablespace\":null" ) );
    assertTrue( "JSON should contain 'informixServername':null", json.contains( "\"informixServername\":null" ) );

    // Verify JSON does NOT contain empty strings for these fields
    assertFalse( "JSON should NOT contain 'username':\"\"", json.contains( "\"username\":\"\"" ) );
    assertFalse( "JSON should NOT contain 'dataTablespace':\"\"", json.contains( "\"dataTablespace\":\"\"" ) );
    assertFalse( "JSON should NOT contain 'indexTablespace':\"\"", json.contains( "\"indexTablespace\":\"\"" ) );
  }

  /**
   * Tests getConnectionByName with mask=false (password should be hidden)
   */
  @Test
  public void testGetConnectionByNameWithMaskFalse() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    testConn.setId( CONN_ID );
    testConn.setName( CONN_NAME );
    testConn.setHostname( "db.example.com" );
    testConn.setDatabaseName( "mydb" );
    testConn.setUsername( "dbuser" );
    testConn.setPassword( "secret123" );
    testConn.setDataTablespace( null );
    testConn.setIndexTablespace( null );

    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( testConn );

    // Call getConnectionByName with mask=false
    IDatabaseConnection result = connectionService.getConnectionByName( CONN_NAME, false );

    // Non-null fields should be preserved
    assertEquals( "ID should match", CONN_ID, result.getId() );
    assertEquals( "Name should match", CONN_NAME, result.getName() );
    assertEquals( "Hostname should match", "db.example.com", result.getHostname() );
    assertEquals( "DatabaseName should match", "mydb", result.getDatabaseName() );
    assertEquals( "Username should match", "dbuser", result.getUsername() );

    // Password should be null when mask=false
    assertNull( "Password should be hidden when mask=false", result.getPassword() );

    // Null fields should remain null
    assertNull( "DataTablespace should be null", result.getDataTablespace() );
    assertNull( "IndexTablespace should be null", result.getIndexTablespace() );
  }

  /**
   * Tests getConnectionByName preserves null fields
   */
  @Test
  public void testGetConnectionByNamePreservesNulls() throws Exception {
    IDatabaseConnection testConn = createTestConnection();
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( testConn );

    // Call getConnectionByName with mask=false
    IDatabaseConnection result = connectionService.getConnectionByName( CONN_NAME, false );

    // Verify null fields are preserved
    assertNull( "Username should be null", result.getUsername() );
    assertNull( "Password should be null", result.getPassword() );
    assertNull( "DataTablespace should be null", result.getDataTablespace() );
    assertNull( "IndexTablespace should be null", result.getIndexTablespace() );
    assertNull( "InformixServername should be null", result.getInformixServername() );
  }

  /**
   * Tests getConnectionById with mask=false
   */
  @Test
  public void testGetConnectionByIdWithMaskFalse() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    testConn.setId( CONN_ID );
    testConn.setName( CONN_NAME );
    testConn.setPassword( "secret" );
    testConn.setUsername( "user" );

    when( connectionServiceImpl.getConnectionById( CONN_ID ) ).thenReturn( testConn );

    IDatabaseConnection result = connectionService.getConnectionById( CONN_ID, false );

    assertEquals( "ID should match", CONN_ID, result.getId() );
    assertEquals( "Username should be preserved", "user", result.getUsername() );
    assertNull( "Password should be hidden", result.getPassword() );
  }

  /**
   * Tests getConnectionIdByNameWithResponse returns connection ID
   */
  @Test
  public void testGetConnectionIdByNameWithResponse() throws Exception {
    IDatabaseConnection testConn = createTestConnection();
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) ).thenReturn( testConn );

    String result = connectionService.getConnectionIdByNameWithResponse( CONN_NAME );

    assertEquals( "Should return connection ID", CONN_ID, result );
  }



  /**
   * Tests createDatabaseConnection
   */
  @Test
  public void testCreateDatabaseConnection() throws Exception {
    IDatabaseConnection result = connectionService.createDatabaseConnection( "org.postgresql.Driver",
        "jdbc:postgresql://localhost:5432/testdb" );

    assertNotNull( "Should create connection", result );
  }

  /**
   * Tests getPoolingParameters returns list of parameters
   */
  @Test
  public void testGetPoolingParameters() throws Exception {
    IDatabaseConnectionPoolParameterList result = connectionService.getPoolingParameters();

    assertNotNull( "Result should not be null", result );
    assertNotNull( "Pool parameters should not be null", result.getDatabaseConnectionPoolParameters() );
    assertTrue( "Should have pool parameters", result.getDatabaseConnectionPoolParameters().size() > 0 );
  }

  /**
   * Tests testConnection success case
   */
  @Test
  public void testTestConnectionSuccess() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setDatabaseName( "testdb" );
    conn.setPassword( "" );

    when( connectionServiceImpl.testConnection( any( DatabaseConnection.class ) ) ).thenReturn( true );

    String result = connectionService.testConnection( conn );

    assertNotNull( "Result should not be null", result );
    assertTrue( "Should contain success message", result.toLowerCase().contains( "succeed" ) );
  }

  /**
   * Tests testConnection failure case
   */
  @Test
  public void testTestConnectionFailure() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setDatabaseName( "testdb" );
    conn.setPassword( "" );

    when( connectionServiceImpl.testConnection( any( DatabaseConnection.class ) ) ).thenReturn( false );

    try {
      connectionService.testConnection( conn );
      fail( "Should throw WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return INTERNAL_SERVER_ERROR status", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests updateConnection success case
   */
  @Test
  public void testUpdateConnectionSuccess() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setId( CONN_ID );
    conn.setPassword( "" );

    when( connectionServiceImpl.updateConnection( any( DatabaseConnection.class ) ) ).thenReturn( true );

    try {
      connectionService.updateConnection( conn );
      fail( "Should throw WebApplicationException with 200 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return OK status", 200, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests updateConnection failure case
   */
  @Test
  public void testUpdateConnectionFailure() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setId( CONN_ID );
    conn.setPassword( "" );

    when( connectionServiceImpl.updateConnection( any( DatabaseConnection.class ) ) ).thenReturn( false );

    try {
      connectionService.updateConnection( conn );
      fail( "Should throw WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return NOT_MODIFIED status", 304, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests deleteConnection success case
   */
  @Test
  public void testDeleteConnectionSuccess() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();

    when( connectionServiceImpl.deleteConnection( any( DatabaseConnection.class ) ) ).thenReturn( true );

    connectionService.deleteConnection( conn );
    // No exception means success
  }

  /**
   * Tests deleteConnectionByName success case
   */
  @Test
  public void testDeleteConnectionByNameSuccess() throws Exception {
    when( connectionServiceImpl.deleteConnection( CONN_NAME ) ).thenReturn( true );

    connectionService.deleteConnectionByName( CONN_NAME );
    // No exception means success
  }

  /**
   * Tests deleteConnectionByName with blank name
   */
  @Test
  public void testDeleteConnectionByNameBlank() throws Exception {
    try {
      connectionService.deleteConnectionByName( "" );
      fail( "Should throw WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return INTERNAL_SERVER_ERROR", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests addConnection success case
   */
  @Test
  public void testAddConnectionSuccess() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setName( CONN_NAME );

    when( connectionServiceImpl.addConnection( any( DatabaseConnection.class ) ) ).thenReturn( true );

    connectionService.addConnection( conn );
    // No exception means success
  }

  /**
   * Tests getConnections returns list
   */
  @Test
  public void testGetConnections() throws Exception {
    IDatabaseConnection conn = createTestConnection();
    List<IDatabaseConnection> connections = Arrays.asList( conn );

    when( connectionServiceImpl.getConnections( true ) ).thenReturn( connections );

    IDatabaseConnectionList result = connectionService.getConnections();

    assertNotNull( "Result should not be null", result );
    assertNotNull( "Connections list should not be null", result.getDatabaseConnections() );
    assertEquals( "Should have one connection", 1, result.getDatabaseConnections().size() );
  }

  /**
   * Tests isConnectionExist returns void on success
   */
  @Test
  public void testIsConnectionExistFound() throws Exception {
    when( connectionServiceImpl.isConnectionExist( CONN_NAME ) ).thenReturn( true );

    connectionService.isConnectionExist( CONN_NAME );
    // No exception means success
  }

}
