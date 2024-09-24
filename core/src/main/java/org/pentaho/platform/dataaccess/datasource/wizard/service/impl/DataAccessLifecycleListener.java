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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPlatformReadyListener;
import org.pentaho.platform.api.engine.IPluginLifecycleListener;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.PluginLifecycleException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.repository2.unified.lifecycle.DelegatingBackingRepositoryLifecycleManager;

public class DataAccessLifecycleListener implements IPluginLifecycleListener, IPlatformReadyListener {

  private static final Log log = LogFactory.getLog( DataAccessLifecycleListener.class );
  private static final String ENABLE_AGILEMART_DATASOURCE = "enable-agile-mart-datasource";

  @Override
  public void init() throws PluginLifecycleException {
  }

  @Override
  public void loaded() throws PluginLifecycleException {
  }

  @Override
  public void ready() throws PluginLifecycleException {
    // the platform is booted, spring initialized, all plugins init and loaded
    boolean enableAgilemartDatasource = false;
    try {
      IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
      enableAgilemartDatasource = Boolean.parseBoolean(
        resLoader.getPluginSetting( DataAccessLifecycleListener.class, ENABLE_AGILEMART_DATASOURCE, "false" ) );
    } catch ( Throwable t ) {
      log.warn( t.getMessage(), t );
    }
    if ( enableAgilemartDatasource ) {
      try {
        SecurityHelper.getInstance().runAsSystem( new Callable<Void>() {
          public Void call() throws Exception {
            AgileMartDatasourceLifecycleManager.getInstance().startup();
            return null;
          }
        } );
      } catch ( Exception e ) {
        log.warn( e.getMessage(), e );
      }

      DelegatingBackingRepositoryLifecycleManager manager =
        PentahoSystem
          .get( DelegatingBackingRepositoryLifecycleManager.class, "backingRepositoryLifecycleManager", null );
      manager.addLifeCycleManager( AgileMartDatasourceLifecycleManager.getInstance() );
    }
  }

  @Override
  public void unLoaded() throws PluginLifecycleException {
  }

}
