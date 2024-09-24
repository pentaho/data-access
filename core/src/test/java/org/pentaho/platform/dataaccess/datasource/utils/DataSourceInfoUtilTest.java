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
