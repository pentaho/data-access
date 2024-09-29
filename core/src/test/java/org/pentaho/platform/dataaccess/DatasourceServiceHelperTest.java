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
    List<List<String>> data = DatasourceServiceHelper.getCsvDataSample("target/test-classes/example.csv", true, ",", "\"", 10);
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
    List<List<String>> data = DatasourceServiceHelper.getCsvDataSample("target/test-classes/doesnotexist.csv", true, ",", "\"", 10);
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
