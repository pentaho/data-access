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
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import jakarta.ws.rs.core.MediaType;

import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;

/**
 * Regression coverage for {@link DatabaseConnectionReaderWriter} (same interface-deserialization
 * concern as the BACKLOG-50609 / PPP-6483 flexjson &rarr; Jackson migration).
 *
 * <p>{@link DatabaseConnection#getDatabaseType()} is typed as the {@code IDatabaseType} interface, so the
 * reader/writer relies on {@code JacksonObjectMapperUtil.createDatabaseTypeObjectMapper()} to resolve it to
 * the concrete {@link DatabaseType} on read.</p>
 */
public class DatabaseConnectionReaderWriterTest {

  /**
   * Verifies deserialization preserves databaseType mapping when using the runtime reader.
   */
  @Test
  public void testJsonDeserializationMapsDatabaseType() throws Exception {
    String json = "{\"name\":\"conn\",\"databaseType\":{\"shortName\":\"GENERIC\",\"name\":\"Generic\"}}";

    DatabaseConnectionReaderWriter reader = new DatabaseConnectionReaderWriter();
    DatabaseConnection connection = reader.readFrom(
      DatabaseConnection.class,
      null,
      null,
      MediaType.APPLICATION_JSON_TYPE,
      null,
      new ByteArrayInputStream( json.getBytes( StandardCharsets.UTF_8 ) )
    );

    assertNotNull( "Connection should be deserialized", connection );
    assertNotNull( "DatabaseType should be deserialized", connection.getDatabaseType() );
  }

  @Test
  public void readFromConnectionWithDatabaseTypeRoundTrips() throws Exception {
    // Given
    DatabaseType type = new DatabaseType( "MySQL", "mysql", Collections.emptyList(), 3306, null );
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( "myConnection" );
    connection.setDatabaseType( type );

    DatabaseConnectionReaderWriter readerWriter = new DatabaseConnectionReaderWriter();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    readerWriter.writeTo( connection, DatabaseConnection.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, out );

    // When
    DatabaseConnection result = readerWriter.readFrom( DatabaseConnection.class, null, null,
      MediaType.APPLICATION_JSON_TYPE, null, new ByteArrayInputStream( out.toByteArray() ) );

    // Then
    assertNotNull( result );
    assertEquals( "myConnection", result.getName() );
    assertNotNull( result.getDatabaseType() );
    assertEquals( "MySQL", result.getDatabaseType().getName() );
  }
}
