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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
}
