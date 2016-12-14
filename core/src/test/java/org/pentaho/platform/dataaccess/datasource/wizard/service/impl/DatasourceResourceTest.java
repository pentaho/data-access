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
