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
 * Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
 */


package org.pentaho.platform.dataaccess.datasource.utils;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Vadim_Polynkov.
 */
public class DataSourceInfoUtilTest {

  @Test
  public void testEscapeQuotes() {
    String value = "DS \"Test's\" & <Fun>";
    String expectedValue = "DS &quot;Test's&quot; & <Fun>";
    assertEquals( DataSourceInfoUtil.escapeQuotes( value ), expectedValue );
  }

  @Test
  public void testUnescapeQuotes() {
    String value = "DS &quot;Test's&quot; & <Fun>";
    String expectedValue = "DS \"Test's\" & <Fun>";
    assertEquals( DataSourceInfoUtil.unescapeQuotes( value ), expectedValue );
  }

  @Test
  public void testParseDataSourceInfo() {
    String value = "DataSource=\"DS &quot;Test's&quot; & <Fun>\";Param=\"TestValue";
    Map map = DataSourceInfoUtil.parseDataSourceInfo( value );
    assertEquals( map.get( "DataSource" ), "DS &quot;Test's&quot; & <Fun>" );
    assertEquals( map.get( "Param" ), "TestValue" );
  }
}
