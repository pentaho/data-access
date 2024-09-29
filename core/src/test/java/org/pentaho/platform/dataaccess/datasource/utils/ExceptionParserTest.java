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


package org.pentaho.platform.dataaccess.datasource.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ExceptionParserTest {

  @Test
  public void testGetErrorHeader() {
    Exception ex = new Exception( "my test message" );
    assertEquals( "default", ExceptionParser.getErrorHeader( ex, "default" ) );
    ex = new Exception( "" );
    assertEquals( "default", ExceptionParser.getErrorHeader( ex, "default" ) );
    ex = new Exception( "header - body" );
    assertEquals( "header", ExceptionParser.getErrorHeader( ex, "default" ) );
  }

  @Test
  public void testGetErrorMessage() {
    Exception ex = new Exception( "my test message" );
    assertEquals( "my test message", ExceptionParser.getErrorMessage( ex, "default" ) );
    ex = new Exception( "" );
    assertEquals( "default", ExceptionParser.getErrorMessage( ex, "default" ) );
    ex = new Exception( "header - body" );
    assertEquals( " body", ExceptionParser.getErrorMessage( ex, "default" ) );
  }
}
