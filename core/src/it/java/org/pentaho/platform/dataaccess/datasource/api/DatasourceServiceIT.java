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

package org.pentaho.platform.dataaccess.datasource.api;

import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.plugin.services.pluginmgr.PluginClassLoader;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;


public class DatasourceServiceIT {

  private static final String SOLUTION_ROOT_PATH = "src/it/resources";
  private static final String DATA_ACCESS_PATH = SOLUTION_ROOT_PATH + File.separator + "/system/data-access";

  @BeforeClass
  public static void setup() {
    PentahoSystem.init( new StandaloneApplicationContext( new File( SOLUTION_ROOT_PATH ).getAbsolutePath(), "" ) );
    PentahoSystem.setSystemSettingsService( new PathBasedSystemSettings() );
  }

  @Test
  public void testGetDatasourceLoadThreadCountIntegration() {
    PluginClassLoader pluginClassLoader =
        new PluginClassLoader( new File( DATA_ACCESS_PATH ).getAbsoluteFile(), this.getClass().getClassLoader() );
    PluginResourceLoader pluginResourceLoader = new PluginResourceLoader();
    pluginResourceLoader.setOverrideClassloader( pluginClassLoader ); // necessary for testing purposes
    DatasourceService datasourceService = new DatasourceService( null, null, null, pluginResourceLoader );
    int threadCount = datasourceService.getDatasourceLoadThreadCount();
    assertEquals( 10, threadCount );
  }

}
