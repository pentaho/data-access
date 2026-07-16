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

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.Test;

public class JacksonObjectMapperUtilTest {

  @Test( expected = UnrecognizedPropertyException.class )
  public void createObjectMapperRejectsUnknownProperties() throws Exception {
    JacksonObjectMapperUtil.createObjectMapper().readValue( "{\"known\":\"value\",\"unexpected\":true}",
      TestModel.class );
  }

  public static class TestModel {
    public String known;
  }
}