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


package org.pentaho.platform.dataaccess.metadata.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.pentaho.platform.dataaccess.metadata.model.impl.Column;
import org.pentaho.platform.dataaccess.metadata.model.impl.Query;

/**
 * PPP-6483 at-risk validation for {@link MetadataServiceUtil#deserializeJsonQuery(String)}.
 *
 * <p>After the Flexjson &rarr; Jackson migration this method reads a JSON payload into the
 * <em>thin</em> {@link org.pentaho.platform.dataaccess.metadata.model.impl.Query} model with a
 * bare {@code ObjectMapper} (no abstract-type resolver). This test verifies whether that thin
 * model is polymorphic (and therefore vulnerable to the {@code InvalidDefinitionException} seen
 * in BACKLOG-50609) or flat (and therefore safe).</p>
 *
 * <p>Given a serialized thin {@code Query} that carries a {@code Column},<br>
 * When it is deserialized through {@code deserializeJsonQuery},<br>
 * Then the query and its column should be preserved.</p>
 *
 * <p>Expected result: PASS &mdash; the thin {@code Query} graph
 * ({@code Column}/{@code Condition}/{@code Order}/{@code Parameter}) is composed of concrete
 * String-based fields with no interface/abstract members, so Jackson can deserialize it.</p>
 */
public class MetadataServiceUtilQueryDeserializationTest {

  @Test
  public void deserializeJsonQueryPreservesColumns() throws Exception {
    // Given
    Query query = new Query();
    query.setDomainName( "domain1" );
    query.setModelId( "model1" );
    Column column = new Column();
    column.setId( "col1" );
    column.setName( "Column One" );
    column.setCategory( "cat1" );
    column.setType( "STRING" );
    query.setColumns( new Column[] { column } );

    String json = new ObjectMapper().writeValueAsString( query );

    // When
    Query result = new MetadataServiceUtil().deserializeJsonQuery( json );

    // Then
    assertNotNull( "deserializeJsonQuery returned null (deserialization failed)", result );
    assertEquals( "domain1", result.getDomainName() );
    assertEquals( "model1", result.getModelId() );
    assertNotNull( result.getColumns() );
    assertEquals( 1, result.getColumns().length );
    assertEquals( "col1", result.getColumns()[ 0 ].getId() );
    assertEquals( "Column One", result.getColumns()[ 0 ].getName() );
  }

  @Test
  public void deserializeJsonQueryRejectsUnknownProperties() {
    Query result = new MetadataServiceUtil().deserializeJsonQuery(
      "{\"domainName\":\"domain1\",\"unexpected\":true}" );

    assertNull( result );
  }
}
