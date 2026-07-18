/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections;

import jakarta.ws.rs.core.MediaType;

import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionList;

/**
 * Regression coverage for {@link DatabaseConnectionListReaderWriter} (same interface-deserialization
 * concern as the BACKLOG-50609 / PPP-6483 flexjson &rarr; Jackson migration).
 *
 * <p>{@link DefaultDatabaseConnectionList#getDatabaseConnections()} is a {@code List<IDatabaseConnection>}
 * and each connection exposes an {@code IDatabaseType} (both interfaces). The reader/writer therefore
 * relies on {@code JacksonObjectMapperUtil.createDatabaseConnectionObjectMapper()}, which registers the
 * {@code IDatabaseConnection -> DatabaseConnection} and {@code IDatabaseType -> DatabaseType} abstract-type
 * mappings.</p>
 *
 * <p>Given a connection list holding a {@link DatabaseConnection} that references a {@link DatabaseType},<br>
 * When it is written and then read back through {@link DatabaseConnectionListReaderWriter},<br>
 * Then the connection and its nested database type survive the round trip.</p>
 */
public class DatabaseConnectionListReaderWriterTest {

  @Test
  public void readFromPopulatedConnectionListRoundTrips() throws Exception {
    // Given
    DatabaseType type = new DatabaseType( "MySQL", "mysql", Collections.emptyList(), 3306, null );
    DatabaseConnection connection = new DatabaseConnection();
    connection.setName( "myConnection" );
    connection.setDatabaseType( type );

    DefaultDatabaseConnectionList list = new DefaultDatabaseConnectionList();
    list.setDatabaseConnections( Collections.singletonList( connection ) );

    DatabaseConnectionListReaderWriter readerWriter = new DatabaseConnectionListReaderWriter();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    readerWriter.writeTo( list, IDatabaseConnectionList.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, out );

    // When
    IDatabaseConnectionList result = readerWriter.readFrom( IDatabaseConnectionList.class, null, null,
      MediaType.APPLICATION_JSON_TYPE, null, new ByteArrayInputStream( out.toByteArray() ) );

    // Then
    assertNotNull( result );
    assertNotNull( result.getDatabaseConnections() );
    assertEquals( 1, result.getDatabaseConnections().size() );

    IDatabaseConnection restored = result.getDatabaseConnections().get( 0 );
    assertEquals( "myConnection", restored.getName() );
    assertNotNull( restored.getDatabaseType() );
    assertEquals( "MySQL", restored.getDatabaseType().getName() );
  }
}
