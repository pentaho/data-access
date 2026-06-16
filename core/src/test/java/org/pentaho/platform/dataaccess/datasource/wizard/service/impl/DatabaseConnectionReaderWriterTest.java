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

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;

import jakarta.ws.rs.core.MediaType;

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
}
