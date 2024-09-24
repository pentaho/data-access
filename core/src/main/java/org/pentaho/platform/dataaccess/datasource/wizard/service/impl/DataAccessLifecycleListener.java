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
