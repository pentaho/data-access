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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;

/**
 * Factory for pre-configured {@link ObjectMapper} instances used by JAX-RS reader/writer providers in this package.
 */
class JacksonObjectMapperUtil {

  private JacksonObjectMapperUtil() {
    // utility class
  }

  /**
   * Returns a new {@link ObjectMapper} with a common baseline configuration.
   *
   * <p><b>Why FAIL_ON_UNKNOWN_PROPERTIES is disabled:</b> the prior serialization library (flexjson 2.1) silently
   * ignored unknown JSON fields. Disabling this feature preserves that behaviour and prevents breaking existing
   * clients that may send payloads containing extra or legacy fields (e.g. a {@code "class"} discriminator that
   * flexjson used to emit).</p>
   *
   * <p>To re-enable strict mode in the future, audit every caller to confirm that no client sends fields that are
   * absent from the target model, then remove this call.</p>
   *
   * @return a configured {@link ObjectMapper}; callers may register additional modules before use.
   */
  static ObjectMapper createObjectMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES );
    return mapper;
  }

  /**
   * Baseline mapper that can deserialize {@link IDatabaseType}-typed fields into their concrete
   * {@link DatabaseType} implementation.
   *
   * @return a configured {@link ObjectMapper} with the {@code IDatabaseType -> DatabaseType} abstract-type mapping.
   */
  static ObjectMapper createDatabaseTypeObjectMapper() {
    SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
    resolver.addMapping( IDatabaseType.class, DatabaseType.class );
    return withAbstractTypes( resolver );
  }

  /**
   * Baseline mapper that can deserialize {@link IDatabaseConnection}- and {@link IDatabaseType}-typed fields into
   * their concrete {@link DatabaseConnection} and {@link DatabaseType} implementations.
   *
   * @return a configured {@link ObjectMapper} with the connection and type abstract-type mappings.
   */
  static ObjectMapper createDatabaseConnectionObjectMapper() {
    SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
    resolver.addMapping( IDatabaseConnection.class, DatabaseConnection.class );
    resolver.addMapping( IDatabaseType.class, DatabaseType.class );
    return withAbstractTypes( resolver );
  }

  private static ObjectMapper withAbstractTypes( SimpleAbstractTypeResolver resolver ) {
    ObjectMapper mapper = createObjectMapper();
    SimpleModule module = new SimpleModule();
    module.setAbstractTypes( resolver );
    mapper.registerModule( module );
    return mapper;
  }
}
