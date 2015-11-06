/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

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
