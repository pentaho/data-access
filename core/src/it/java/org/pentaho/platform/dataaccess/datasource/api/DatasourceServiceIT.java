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
 * Copyright (c) 2002-2022 Hitachi Vantara..  All rights reserved.
 */
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
