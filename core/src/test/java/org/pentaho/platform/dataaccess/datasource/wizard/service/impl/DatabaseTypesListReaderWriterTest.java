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
import java.util.Collections;

import jakarta.ws.rs.core.MediaType;

import org.junit.Test;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.ui.database.event.DefaultDatabaseTypesList;
import org.pentaho.ui.database.event.IDatabaseTypesList;

/**
 * PPP-6483 at-risk validation for {@link DatabaseTypesListReaderWriter}.
 *
 * <p>The reader deserializes into {@link DefaultDatabaseTypesList}, whose {@code types} field is
 * declared as {@code List<IDatabaseType>} (an interface). The Jackson mapper created by
 * {@code JacksonObjectMapperUtil.createObjectMapper()} registers <em>no</em> abstract-type
 * resolver, unlike the sibling connection reader/writers.</p>
 *
 * <p>Given a {@code DefaultDatabaseTypesList} that contains a concrete {@link DatabaseType},<br>
 * When it is written and then read back through {@link DatabaseTypesListReaderWriter},<br>
 * Then the type should survive the round trip.</p>
 *
 * <p>Expected result: FAIL &mdash; deserializing a populated {@code List<IDatabaseType>} without a
 * resolver throws {@code InvalidDefinitionException} (same root cause as BACKLOG-50609). This is a
 * latent defect: today the read path is only exercised server&rarr;client, but the code is
 * reachable and incorrect.</p>
 */
public class DatabaseTypesListReaderWriterTest {

  @Test
  public void readFromPopulatedTypesListRoundTrips() throws Exception {
    // Given
    DatabaseType type = new DatabaseType( "MySQL", "mysql", Collections.emptyList(), 3306, null );
    DefaultDatabaseTypesList list = new DefaultDatabaseTypesList();
    list.setDbTypes( Collections.singletonList( type ) );

    DatabaseTypesListReaderWriter readerWriter = new DatabaseTypesListReaderWriter();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    readerWriter.writeTo( list, IDatabaseTypesList.class, null, null, MediaType.APPLICATION_JSON_TYPE, null, out );

    // When
    IDatabaseTypesList result = readerWriter.readFrom( IDatabaseTypesList.class, null, null,
      MediaType.APPLICATION_JSON_TYPE, null, new ByteArrayInputStream( out.toByteArray() ) );

    // Then
    assertNotNull( result );
    assertNotNull( result.getDbTypes() );
    assertEquals( 1, result.getDbTypes().size() );
    assertEquals( "MySQL", result.getDbTypes().get( 0 ).getName() );
  }
}
