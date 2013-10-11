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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class DatasourceServiceHelperTest {

 	static {
    if (!PentahoSystem.getInitializedOK()) {
      PentahoSystemHelper.init();
    }
  }

  @SuppressWarnings("nls")
  @Test
  public void testCsvSampleData() {
    List<List<String>> data = DatasourceServiceHelper.getCsvDataSample("test-res/example.csv", true, ",", "\"", 10);
    Assert.assertEquals(5, data.size());
    Assert.assertEquals(4, data.get(0).size());

    // row 1
    Assert.assertEquals("1", data.get(0).get(0));
    Assert.assertEquals("4.5", data.get(0).get(1));
    Assert.assertEquals("02/12/77", data.get(0).get(2));
    Assert.assertEquals("String - , Value", data.get(0).get(3));

  }
  
  @SuppressWarnings("nls")
  @Test
  public void testCsvDoesNotExist() {
    // all this does is return 0
    List<List<String>> data = DatasourceServiceHelper.getCsvDataSample("test-res/doesnotexist.csv", true, ",", "\"", 10);
    Assert.assertEquals(0, data.size());
  }

  @Test
  public void testGetGeoContext() throws Exception {
    try{
      KettleEnvironment.init();
      Props.init(Props.TYPE_PROPERTIES_EMPTY);
    } catch(Exception e){
      // may already be initialized by another test
    }

    GeoContext geo = DatasourceServiceHelper.getGeoContext();
    assertEquals("Geography", geo.getDimensionName());

    assertEquals(6, geo.size());
    assertNotNull(geo.getLocationRole());

    // make sure they are in the same order as entered in the props file
    String rolesCsv = "continent, country, state, city, postal_code";
    String[] tokens = rolesCsv.split(",");
    for(int i = 0; i < tokens.length; i++) {
      assertEquals(tokens[i].trim(), geo.getGeoRole(i).getName());
    }

  }

}
