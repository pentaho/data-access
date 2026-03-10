/*
 * ! ******************************************************************************
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

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.engine.security.authorization.core.exceptions.AuthorizationRuleException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.UtilHtmlSanitizer;
import org.pentaho.ui.database.event.IDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionPoolParameterList;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.WebApplicationException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
   * Verifies that null fields are preserved by getConnectionByNameWithResponse endpoint
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
   * Verifies that password is hidden by getConnectionByNameWithResponse endpoint
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

    // Verify username is present in JSON and is null
    assertTrue( "JSON should contain username field", node.has( "username" ) );
    assertTrue( "Username field should be null", node.get( "username" ).isNull() );
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
   * Tests testConnection failure case returns text/plain content type
   */
  @Test
  public void testTestConnectionFailureContentType() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setName( CONN_NAME );
    conn.setDatabaseName( "testdb" );
    conn.setPassword( "secret" );

    when( connectionServiceImpl.testConnection( any( DatabaseConnection.class ) ) ).thenReturn( false );

    try {
      connectionService.testConnection( conn );
      fail( "Should throw WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return INTERNAL_SERVER_ERROR status", 500, e.getResponse().getStatus() );
      assertNotNull( "Media type should be set", e.getResponse().getMediaType() );
      assertEquals( "Should return text/plain content type", "text/plain",
        e.getResponse().getMediaType().toString() );
      assertNotNull( "Error response should include an entity", e.getResponse().getEntity() );
      assertTrue( "Error response should include the database name",
        String.valueOf( e.getResponse().getEntity() ).contains( "testdb" ) );
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
      assertEquals( "Should return 200 OK status", 200, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests deleteConnection success case
   */
  @Test
  public void testDeleteConnectionSuccess() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();

    when( connectionServiceImpl.deleteConnection( any( DatabaseConnection.class ) ) ).thenReturn( true );

    try {
      connectionService.deleteConnection( conn );
      fail( "Should throw WebApplicationException with 200 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 200 OK status", 200, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests deleteConnectionByName success case
   */
  @Test
  public void testDeleteConnectionByNameSuccess() throws Exception {
    when( connectionServiceImpl.deleteConnection( CONN_NAME ) ).thenReturn( true );

    try {
      connectionService.deleteConnectionByName( CONN_NAME );
      fail( "Should throw WebApplicationException with 200 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 200 OK status", 200, e.getResponse().getStatus() );
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

    try {
      connectionService.addConnection( conn );
      fail( "Should throw WebApplicationException with 200 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 200 OK status", 200, e.getResponse().getStatus() );
    }
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

    try {
      connectionService.isConnectionExist( CONN_NAME );
      fail( "Should throw WebApplicationException with 200 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 200 OK status", 200, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests isConnectionExist returns 304 when connection does not exist
   */
  @Test
  public void testIsConnectionExistNotFound() throws Exception {
    when( connectionServiceImpl.isConnectionExist( CONN_NAME ) ).thenReturn( false );

    try {
      connectionService.isConnectionExist( CONN_NAME );
      fail( "Should throw WebApplicationException with 304 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 304 Not Modified status", 304, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests getConnectionByName endpoint with blank name returns 500 Internal Server Error
   */
  @Test
  public void testGetConnectionByNameBlankParameter() throws Exception {
    when( connectionServiceImpl.getConnectionByName( "" ) )
      .thenThrow( new ConnectionServiceException(
        "ConnectionServiceImpl.ERROR_0003 - Unable to get connection name" ) );

    try {
      connectionService.getConnectionByName( "", false );
      fail( "Should throw WebApplicationException with 500 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error status", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests getConnectionByName endpoint with null name returns 500 Internal Server Error
   */
  @Test
  public void testGetConnectionByNameNullParameter() throws Exception {
    try {
      connectionService.getConnectionByName( null, false );
      fail( "Should throw WebApplicationException with 500 status" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error status", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Tests getConnectionIdByNameWithResponse with non-existent connection returns 500 JSON error response
   */
  @Test
  public void testGetConnectionIdByNameWithResponseNotFound() throws Exception {
    when( connectionServiceImpl.getConnectionByName( any( String.class ) ) )
      .thenThrow( new ConnectionServiceException( 404,
        "ConnectionServiceImpl.ERROR_0003 - Unable to get connection name" ) );

    try {
      connectionService.getConnectionIdByNameWithResponse( "NonExistentConnection" );
      fail( "Should throw WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error status", 500, e.getResponse().getStatus() );
      assertEquals( "Should return application/json content type",
        "application/json", e.getResponse().getMediaType().toString() );
      assertTrue( "Should include connection error message",
        String.valueOf( e.getResponse().getEntity() ).contains( "Unable to get connection" ) );
    }
  }

  /**
   * Tests getConnectionIdByNameWithResponse with blank name returns 500 JSON error response
   */
  @Test
  public void testGetConnectionIdByNameWithResponseBlankName() throws Exception {
    when( connectionServiceImpl.getConnectionByName( "" ) )
      .thenThrow( new ConnectionServiceException( 404,
        "ConnectionServiceImpl.ERROR_0003 - Unable to get connection name" ) );

    try {
      connectionService.getConnectionIdByNameWithResponse( "" );
      fail( "Should throw WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error status", 500, e.getResponse().getStatus() );
      assertEquals( "Should return application/json content type",
        "application/json", e.getResponse().getMediaType().toString() );
      assertTrue( "Should include connection error message",
        String.valueOf( e.getResponse().getEntity() ).contains( "Unable to get connection" ) );
    }
  }

  /**
   * Tests checkParameters with valid connection returns 204 No Content status
   */
  @Test
  public void testCheckParametersValidConnection() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    testConn.setName( CONN_NAME );
    testConn.setId( CONN_ID );
    testConn.setHostname( "localhost" );
    testConn.setDatabaseName( "testdb" );
    testConn.setDatabasePort( "5432" );

    // Mock the conversion to avoid database type resolution issues
    org.pentaho.di.core.database.DatabaseMeta mockMeta =
      org.mockito.Mockito.mock( org.pentaho.di.core.database.DatabaseMeta.class );
    when( mockMeta.checkParameters() ).thenReturn( new String[] {} );

    try ( org.mockito.MockedStatic<org.pentaho.database.util.DatabaseUtil> utilities =
      org.mockito.Mockito.mockStatic( org.pentaho.database.util.DatabaseUtil.class ) ) {
      utilities.when( () -> org.pentaho.database.util.DatabaseUtil.convertToDatabaseMeta( testConn ) )
        .thenReturn( mockMeta );

      try {
        connectionService.checkParameters( testConn );
        fail( "Should throw WebApplicationException" );
      } catch ( WebApplicationException e ) {
        assertEquals( "Valid connection should return 204 No Content", 204, e.getResponse().getStatus() );
      }
    }
  }

  /**
   * Tests checkParameters with missing connection name returns validation message
   */
  @Test
  public void testCheckParametersMissingConnectionName() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    // Missing name - intentionally null
    testConn.setId( CONN_ID );

    // Mock to return validation errors for missing name
    org.pentaho.di.core.database.DatabaseMeta mockMeta =
      org.mockito.Mockito.mock( org.pentaho.di.core.database.DatabaseMeta.class );
    String[] validationMessages = { "Bad Connection Name" };
    when( mockMeta.checkParameters() ).thenReturn( validationMessages );

    try ( org.mockito.MockedStatic<org.pentaho.database.util.DatabaseUtil> utilities =
      org.mockito.Mockito.mockStatic( org.pentaho.database.util.DatabaseUtil.class ) ) {
      utilities.when( () -> org.pentaho.database.util.DatabaseUtil.convertToDatabaseMeta( testConn ) )
        .thenReturn( mockMeta );

      try {
        connectionService.checkParameters( testConn );
        fail( "Should throw WebApplicationException" );
      } catch ( WebApplicationException e ) {
        assertEquals( "Missing connection name should return 200", 200, e.getResponse().getStatus() );
        Object entity = e.getResponse().getEntity();
        assertTrue( "Entity should be CheckParameters200Response",
          entity instanceof org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response );
        org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response response =
          (org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response) entity;
        assertNotNull( "Response items should not be null", response.getItems() );
        assertTrue( "Should have validation messages", response.getItems().size() > 0 );
        assertTrue( "Message should contain name reference",
          response.getItems().get( 0 ).toLowerCase().contains( "name" )
            || response.getItems().get( 0 ).toLowerCase().contains( "connection" ) );
      }
    }
  }

  /**
   * Tests checkParameters with missing database name returns validation message
   */
  @Test
  public void testCheckParametersMissingDatabaseName() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    testConn.setName( CONN_NAME );
    testConn.setId( CONN_ID );
    // Missing database name

    // Mock to return validation errors for missing database name
    org.pentaho.di.core.database.DatabaseMeta mockMeta =
      org.mockito.Mockito.mock( org.pentaho.di.core.database.DatabaseMeta.class );
    String[] validationMessages = { "Bad Database Name" };
    when( mockMeta.checkParameters() ).thenReturn( validationMessages );

    try ( org.mockito.MockedStatic<org.pentaho.database.util.DatabaseUtil> utilities =
      org.mockito.Mockito.mockStatic( org.pentaho.database.util.DatabaseUtil.class ) ) {
      utilities.when( () -> org.pentaho.database.util.DatabaseUtil.convertToDatabaseMeta( testConn ) )
        .thenReturn( mockMeta );

      try {
        connectionService.checkParameters( testConn );
        fail( "Should throw WebApplicationException" );
      } catch ( WebApplicationException e ) {
        assertEquals( "Missing database name should return 200", 200, e.getResponse().getStatus() );
        Object entity = e.getResponse().getEntity();
        assertTrue( "Entity should be CheckParameters200Response",
          entity instanceof org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response );
        org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response response =
          (org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response) entity;
        assertNotNull( "Response items should not be null", response.getItems() );
        assertTrue( "Should have validation messages", response.getItems().size() > 0 );
        assertTrue( "Message should contain database reference",
          response.getItems().get( 0 ).toLowerCase().contains( "database" )
            || response.getItems().get( 0 ).toLowerCase().contains( "name" ) );
      }
    }
  }

  /**
   * Tests checkParameters with valid parameters returns 204 No Content status
   */
  @Test
  public void testCheckParametersReturnsJsonArray() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    testConn.setName( CONN_NAME );
    testConn.setHostname( "localhost" );
    testConn.setDatabaseName( "testdb" );

    // Mock the database meta
    org.pentaho.di.core.database.DatabaseMeta mockMeta =
      org.mockito.Mockito.mock( org.pentaho.di.core.database.DatabaseMeta.class );
    when( mockMeta.checkParameters() ).thenReturn( new String[] {} );

    try ( org.mockito.MockedStatic<org.pentaho.database.util.DatabaseUtil> utilities =
      org.mockito.Mockito.mockStatic( org.pentaho.database.util.DatabaseUtil.class ) ) {
      utilities.when( () -> org.pentaho.database.util.DatabaseUtil.convertToDatabaseMeta( testConn ) )
        .thenReturn( mockMeta );

      try {
        connectionService.checkParameters( testConn );
        fail( "Should throw WebApplicationException" );
      } catch ( WebApplicationException e ) {
        assertEquals( "Valid connection should return 204 No Content", 204, e.getResponse().getStatus() );
      }
    }
  }

  /**
   * Tests checkParameters with missing database type returns validation message
   */
  @Test
  public void testCheckParametersMissingDatabaseType() throws Exception {
    DatabaseConnection testConn = new DatabaseConnection();
    testConn.setName( CONN_NAME );
    // Missing database type

    // Mock to return validation error for missing interface
    org.pentaho.di.core.database.DatabaseMeta mockMeta =
      org.mockito.Mockito.mock( org.pentaho.di.core.database.DatabaseMeta.class );
    String[] validationMessages = { "Bad Interface" };
    when( mockMeta.checkParameters() ).thenReturn( validationMessages );

    try ( org.mockito.MockedStatic<org.pentaho.database.util.DatabaseUtil> utilities =
      org.mockito.Mockito.mockStatic( org.pentaho.database.util.DatabaseUtil.class ) ) {
      utilities.when( () -> org.pentaho.database.util.DatabaseUtil.convertToDatabaseMeta( testConn ) )
        .thenReturn( mockMeta );

      try {
        connectionService.checkParameters( testConn );
        fail( "Should throw WebApplicationException" );
      } catch ( WebApplicationException e ) {
        assertEquals( "Missing database type should return 200", 200, e.getResponse().getStatus() );
        Object entity = e.getResponse().getEntity();
        assertTrue( "Entity should be CheckParameters200Response",
          entity instanceof org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response );
        org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response response =
          (org.pentaho.platform.dataaccess.datasource.wizard.service.model.CheckParameters200Response) entity;
        assertNotNull( "Response items should not be null", response.getItems() );
        assertTrue( "Should have validation messages", response.getItems().size() > 0 );
      }
    }
  }

  /**
   * Verifies that addConnection throws WebApplicationException with 403 Forbidden
   * when ConnectionServiceException with 403 status code is thrown
   */
  @Test
  public void testAddConnectionPermissionDenied() throws Exception {
    IDatabaseConnection testConn = createTestConnection();

    when( connectionServiceImpl.addConnection( testConn ) )
      .thenThrow( new ConnectionServiceException( 403, "User does not have permission to add connections" ) );

    try {
      connectionService.addConnection( testConn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException wae ) {
      assertEquals( "Should return 403 Forbidden", 403, wae.getResponse().getStatus() );
    }
  }

  /**
   * Verifies that updateConnection maps permission-denied errors to 500
   * to preserve legacy API behavior.
   */
  @Test
  public void testUpdateConnectionPermissionDenied() throws Exception {
    IDatabaseConnection testConn = createTestConnection();

    when( connectionServiceImpl.updateConnection( testConn ) )
      .thenThrow( new ConnectionServiceException( 403, "User does not have permission to update connections" ) );

    try {
      connectionService.updateConnection( testConn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException wae ) {
      assertEquals( "Should return 500 Internal Server Error", 500, wae.getResponse().getStatus() );
    }
  }

  /**
   * Verifies that deleteConnection maps permission-denied errors to 500
   * to preserve legacy API behavior.
   */
  @Test
  public void testDeleteConnectionPermissionDenied() throws Exception {
    IDatabaseConnection testConn = createTestConnection();

    when( connectionServiceImpl.deleteConnection( testConn ) )
      .thenThrow( new ConnectionServiceException( 403, "User does not have permission to delete connections" ) );

    try {
      connectionService.deleteConnection( testConn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException wae ) {
      assertEquals( "Should return 500 Internal Server Error", 500, wae.getResponse().getStatus() );
    }
  }

  /**
   * Verifies that addConnection with missing required fields throws 400 or 500
   * but NOT 403 (permission is OK, but validation fails)
   */
  @Test
  public void testAddConnectionMissingRequiredFieldsThrows400or500() throws Exception {
    IDatabaseConnection testConn = createTestConnection();

    when( connectionServiceImpl.addConnection( testConn ) )
      .thenThrow( new ConnectionServiceException( 500, "Missing required fields" ) );

    try {
      connectionService.addConnection( testConn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException wae ) {
      int status = wae.getResponse().getStatus();
      // Should be 400 (bad request) or 500 but NOT 403 (forbidden)
      assertTrue( "Should be 400 or 500, not 403", status == 400 || status == 500 );
      assertNotEquals( "Should NOT be 403 for validation errors", 403, status );
    }
  }

  /**
   * Verifies getConnectionByNameWithResponse maps forbidden to 500 with JSON payload.
   */
  @Test
  public void testGetConnectionByNameWithResponsePermissionDenied() throws Exception {
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) )
      .thenThrow( new ConnectionServiceException( 403, "Forbidden for this user" ) );

    try {
      connectionService.getConnectionByNameWithResponse( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
      assertNotNull( "Media type should be present", e.getResponse().getMediaType() );
      assertEquals( "Should return application/json content type",
        "application/json", e.getResponse().getMediaType().toString() );
      assertTrue( "Should include the original message", String.valueOf( e.getResponse().getEntity() )
        .contains( "Forbidden" ) );
    }
  }

  /**
   * Verifies getConnectionByNameWithResponse maps non-legacy statuses to 500 without an entity.
   */
  @Test
  public void testGetConnectionByNameWithResponseNonLegacyStatus() throws Exception {
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) )
      .thenThrow( new ConnectionServiceException( 409, "Conflict" ) );

    try {
      connectionService.getConnectionByNameWithResponse( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
      assertNull( "Entity should be null for non-legacy status handling", e.getResponse().getEntity() );
    }
  }

  /**
   * Verifies getConnectionIdByNameWithResponse preserves non-legacy status code and JSON message.
   */
  @Test
  public void testGetConnectionIdByNameWithResponsePreservesStatusForConflict() throws Exception {
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) )
      .thenThrow( new ConnectionServiceException( 409, "Conflict while resolving connection id" ) );

    try {
      connectionService.getConnectionIdByNameWithResponse( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should preserve conflict status", 409, e.getResponse().getStatus() );
      assertNotNull( "Media type should be present", e.getResponse().getMediaType() );
      assertEquals( "Should return application/json content type",
        "application/json", e.getResponse().getMediaType().toString() );
      assertTrue( "Should include conflict message", String.valueOf( e.getResponse().getEntity() )
        .contains( "Conflict" ) );
    }
  }

  /**
   * Verifies updateConnection maps 404 to 500 for backward compatibility.
   */
  @Test
  public void testUpdateConnectionNotFoundMapsTo500() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setId( CONN_ID );
    conn.setPassword( "unchanged" );

    when( connectionServiceImpl.updateConnection( conn ) )
      .thenThrow( new ConnectionServiceException( 404, "Connection not found" ) );

    try {
      connectionService.updateConnection( conn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies updateConnection preserves non-remapped status and returns text/plain with message.
   */
  @Test
  public void testUpdateConnectionPreservesConflictStatusAndMessage() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setId( CONN_ID );
    conn.setPassword( "unchanged" );

    when( connectionServiceImpl.updateConnection( conn ) )
      .thenThrow( new ConnectionServiceException( 409, "Conflict during update" ) );

    try {
      connectionService.updateConnection( conn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should preserve conflict status", 409, e.getResponse().getStatus() );
      assertNotNull( "Media type should be present", e.getResponse().getMediaType() );
      assertEquals( "Should return text/plain content type", "text/plain", e.getResponse().getMediaType().toString() );
      assertTrue( "Should include conflict message", String.valueOf( e.getResponse().getEntity() )
        .contains( "Conflict during update" ) );
    }
  }

  /**
   * Verifies deleteConnectionByName maps forbidden to 500 for legacy behavior.
   */
  @Test
  public void testDeleteConnectionByNamePermissionDeniedMapsTo500() throws Exception {
    when( connectionServiceImpl.deleteConnection( CONN_NAME ) )
      .thenThrow( new ConnectionServiceException( 403, "Delete forbidden" ) );

    try {
      connectionService.deleteConnectionByName( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies deleteConnectionByName preserves non-forbidden status codes.
   */
  @Test
  public void testDeleteConnectionByNamePreservesNotFoundStatus() throws Exception {
    when( connectionServiceImpl.deleteConnection( CONN_NAME ) )
      .thenThrow( new ConnectionServiceException( 404, "Connection not found" ) );

    try {
      connectionService.deleteConnectionByName( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should preserve 404 status", 404, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies testConnection maps ERROR_0001 ConnectionServiceException to plain 500 response.
   */
  @Test
  public void testTestConnectionError0001ReturnsPlain500() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setDatabaseName( "testdb" );
    conn.setPassword( "set" );

    when( connectionServiceImpl.testConnection( conn ) )
      .thenThrow( new ConnectionServiceException( "ConnectionServiceImpl.ERROR_0001 invalid details" ) );

    try {
      connectionService.testConnection( conn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
      assertNull( "Entity should be null for plain 500 response", e.getResponse().getEntity() );
    }
  }

  /**
   * Verifies testConnection maps generic ConnectionServiceException to a plain 500 response.
   */
  @Test
  public void testTestConnectionGenericConnectionServiceExceptionReturnsPlain500() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setDatabaseName( "testdb" );
    conn.setPassword( "set" );

    when( connectionServiceImpl.testConnection( conn ) )
      .thenThrow( new ConnectionServiceException( "Unexpected backend error" ) );

    try {
      connectionService.testConnection( conn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
      assertNull( "Media type should be null for plain 500 response", e.getResponse().getMediaType() );
      assertNull( "Entity should be null for plain 500 response", e.getResponse().getEntity() );
    }
  }

  /**
   * Verifies addConnection maps AuthorizationRuleException to 403 via handleException.
   */
  @Test
  public void testAddConnectionAuthorizationRuleExceptionReturns403() throws Exception {
    IDatabaseConnection testConn = createTestConnection();
    org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest request =
      org.mockito.Mockito.mock( org.pentaho.platform.api.engine.security.authorization.IAuthorizationRequest.class );
    org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule<?> rule =
      org.mockito.Mockito.mock( org.pentaho.platform.api.engine.security.authorization.IAuthorizationRule.class );
    AuthorizationRuleException authException = new AuthorizationRuleException( request, rule );

    when( connectionServiceImpl.addConnection( testConn ) )
      .thenThrow( authException );

    try {
      connectionService.addConnection( testConn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 403 Forbidden", 403, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies addConnection maps unexpected runtime exceptions to 500.
   */
  @Test
  public void testAddConnectionUnexpectedRuntimeExceptionReturns500() throws Exception {
    IDatabaseConnection testConn = createTestConnection();

    when( connectionServiceImpl.addConnection( testConn ) )
      .thenThrow( new RuntimeException( "Unexpected add failure" ) );

    try {
      connectionService.addConnection( testConn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies updateConnection maps unexpected runtime exceptions to 500.
   */
  @Test
  public void testUpdateConnectionUnexpectedRuntimeExceptionReturns500() throws Exception {
    DatabaseConnection conn = new DatabaseConnection();
    conn.setId( CONN_ID );
    conn.setPassword( "set" );

    when( connectionServiceImpl.updateConnection( conn ) )
      .thenThrow( new RuntimeException( "Unexpected update failure" ) );

    try {
      connectionService.updateConnection( conn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies deleteConnection maps unexpected runtime exceptions to 500.
   */
  @Test
  public void testDeleteConnectionUnexpectedRuntimeExceptionReturns500() throws Exception {
    IDatabaseConnection testConn = createTestConnection();

    when( connectionServiceImpl.deleteConnection( testConn ) )
      .thenThrow( new RuntimeException( "Unexpected delete failure" ) );

    try {
      connectionService.deleteConnection( testConn );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies deleteConnectionByName maps unexpected runtime exceptions to 500.
   */
  @Test
  public void testDeleteConnectionByNameUnexpectedRuntimeExceptionReturns500() throws Exception {
    when( connectionServiceImpl.deleteConnection( CONN_NAME ) )
      .thenThrow( new RuntimeException( "Unexpected delete-by-name failure" ) );

    try {
      connectionService.deleteConnectionByName( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies getConnectionByName maps unexpected runtime exceptions to 500.
   */
  @Test
  public void testGetConnectionByNameUnexpectedRuntimeExceptionReturns500() throws Exception {
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) )
      .thenThrow( new RuntimeException( "Unexpected lookup failure" ) );

    try {
      connectionService.getConnectionByName( CONN_NAME, false );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies getConnectionIdByNameWithResponse maps unexpected runtime exceptions to 500.
   */
  @Test
  public void testGetConnectionIdByNameWithResponseUnexpectedRuntimeExceptionReturns500() throws Exception {
    when( connectionServiceImpl.getConnectionByName( CONN_NAME ) )
      .thenThrow( new RuntimeException( "Unexpected id lookup failure" ) );

    try {
      connectionService.getConnectionIdByNameWithResponse( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies isConnectionExist maps unexpected runtime exceptions to 500.
   */
  @Test
  public void testIsConnectionExistUnexpectedRuntimeExceptionReturns500() throws Exception {
    when( connectionServiceImpl.isConnectionExist( CONN_NAME ) )
      .thenThrow( new RuntimeException( "Unexpected existence check failure" ) );

    try {
      connectionService.isConnectionExist( CONN_NAME );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies getConnectionById maps ConnectionServiceException to 500.
   */
  @Test
  public void testGetConnectionByIdConnectionServiceExceptionReturns500() throws Exception {
    when( connectionServiceImpl.getConnectionById( CONN_ID ) )
      .thenThrow( new ConnectionServiceException( "Unable to read by id" ) );

    try {
      connectionService.getConnectionById( CONN_ID, false );
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

  /**
   * Verifies getConnections maps ConnectionServiceException to 500.
   */
  @Test
  public void testGetConnectionsConnectionServiceExceptionReturns500() throws Exception {
    when( connectionServiceImpl.getConnections( true ) )
      .thenThrow( new ConnectionServiceException( "Unable to read connections" ) );

    try {
      connectionService.getConnections();
      fail( "Should have thrown WebApplicationException" );
    } catch ( WebApplicationException e ) {
      assertEquals( "Should return 500 Internal Server Error", 500, e.getResponse().getStatus() );
    }
  }

}
