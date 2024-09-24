/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.models;


import org.junit.Assert;
import org.junit.Test;

public class CsvTransformGeneratorExceptionTest {

  @Test
  public void test() {
    final Exception causeException = new Exception( "cause msg" );

    Assert.assertEquals( null, ( new CsvTransformGeneratorException() ).getLocalizedMessage() );

    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg" ) ).getLocalizedMessage() );

    Assert.assertEquals( causeException.toString(), ( new CsvTransformGeneratorException( causeException ) ).getLocalizedMessage() );

    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", null ) ).getLocalizedMessage() );
    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", causeException ) ).getLocalizedMessage() );

    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", causeException, "explicit cause message" ) ).getLocalizedMessage() );
    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", causeException, "explicit cause message", "cause stack trace" ) ).getLocalizedMessage() );

    Assert.assertEquals( "localized", ( new CsvTransformGeneratorException( "a message", null, null, null, "localized" ) ).getLocalizedMessage() );
    Assert.assertEquals( "localized", ( new CsvTransformGeneratorException( "a message", new Exception(), "a", "b", "localized" ) ).getLocalizedMessage() );
  }
}
