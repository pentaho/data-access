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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Vadim_Polynkov.
 */
public class DatasourceResourceTest {

  @Test
  public void testPrepareDataSourceInfo_OnlyQuotesEscaped() {
    StringBuilder dataSourceInfo = new StringBuilder();
    dataSourceInfo.append( "DataSource=" )
      .append( "\"DS &quot;Test&apos;s&quot; &amp; &lt;Fun&gt;\";" )
      .append( "DynamicSchemaProcessor=" )
      .append( "\"DSP&apos;s &amp; &quot;Other&quot; &lt;stuff&gt;\";" );

    StringBuilder expectedResult = new StringBuilder();
    expectedResult.append( "DataSource=" )
      .append( "\"DS &quot;Test's&quot; & <Fun>\";" )
      .append( "DynamicSchemaProcessor=" )
      .append( "\"DSP's & &quot;Other&quot; <stuff>\";" );

    String result = DatasourceResource.prepareDataSourceInfo( dataSourceInfo.toString() );
    assertEquals( result, expectedResult.toString() );
  }
}
