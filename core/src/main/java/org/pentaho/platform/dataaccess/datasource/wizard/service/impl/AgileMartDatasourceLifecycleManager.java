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
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.mt.ITenant;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.repository2.unified.jcr.IPathConversionHelper;
import org.pentaho.platform.repository2.unified.lifecycle.AbstractBackingRepositoryLifecycleManager;
import org.springframework.extensions.jcr.JcrTemplate;
import org.springframework.transaction.support.TransactionTemplate;

public class AgileMartDatasourceLifecycleManager extends AbstractBackingRepositoryLifecycleManager {

  private IDatasourceMgmtService datasourceMgmtService;
  public static final String PLUGIN_NAME = "data-access"; //$NON-NLS-1$
  private static final String AGILE_MART_STAGING_DATASOURCE_NAME = "agile-mart-staging-datasource"; //$NON-NLS-1$
  private static final String SETTINGS_FILE = PLUGIN_NAME + "/settings.xml"; //$NON-NLS-1$
  private AgileMartDatasourceHelper agileMartDatasourceHelper;

  private static final Log log = LogFactory.getLog( AgileMartDatasourceLifecycleManager.class );

  public AgileMartDatasourceLifecycleManager( final TransactionTemplate txnTemplate,
                                              final JcrTemplate adminJcrTemplate,
                                              final IPathConversionHelper pathConversionHelper,
                                              IDatasourceMgmtService datasourceMgmtService,
                                              IPluginResourceLoader resLoader ) {
    super( txnTemplate, adminJcrTemplate, pathConversionHelper );
    this.datasourceMgmtService = datasourceMgmtService;
    this.agileMartDatasourceHelper = new AgileMartDatasourceHelper( resLoader );
  }

  public static AgileMartDatasourceLifecycleManager getInstance() {
    if ( instance == null ) {

      TransactionTemplate txnTemplate =
        PentahoSystem.get( TransactionTemplate.class, "jcrTransactionTemplate", PentahoSessionHolder.getSession() );
      JcrTemplate adminJcrTemplate =
        PentahoSystem.get( JcrTemplate.class, "adminJcrTemplate", PentahoSessionHolder.getSession() );
      IPathConversionHelper pathConversionHelper =
        PentahoSystem.get( IPathConversionHelper.class, "pathConversionHelper", PentahoSessionHolder.getSession() );
      IPluginResourceLoader resLoader =
        PentahoSystem.get( IPluginResourceLoader.class, PentahoSessionHolder.getSession() );
      IDatasourceMgmtService datasourceMgmtService =
        PentahoSystem.get( IDatasourceMgmtService.class, PentahoSessionHolder.getSession() );
      instance =
        new AgileMartDatasourceLifecycleManager( txnTemplate, adminJcrTemplate, pathConversionHelper,
          datasourceMgmtService, resLoader );
    }

    return instance;
  }

  private static AgileMartDatasourceLifecycleManager instance;

  @Override
  public void startup() {
    try {
      String agileMartDatasourceName =
        PentahoSystem.getSystemSetting( SETTINGS_FILE, AGILE_MART_STAGING_DATASOURCE_NAME, null );
      IDatabaseConnection agileMartDatasource = datasourceMgmtService.getDatasourceByName( agileMartDatasourceName );
      IDatabaseConnection newAgileMartDatasource = agileMartDatasourceHelper.getAgileMartDatasource();

      if ( agileMartDatasource != null ) {
        newAgileMartDatasource.setId( agileMartDatasource.getId() );
        datasourceMgmtService.updateDatasourceByName( agileMartDatasource.getName(), newAgileMartDatasource );
      } else {
        datasourceMgmtService.createDatasource( newAgileMartDatasource );
      }
    } catch ( Throwable th ) {
      log.warn( "Error during the create/update of AgileMart Datasource", th );
    }
  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public void newTenant() {

  }

  @Override
  public void newUser() {
    // TODO Auto-generated method stub

  }

  @Override
  public void newTenant( ITenant tenant ) {

  }

  @Override
  public void newUser( ITenant tenant, String username ) {
    // TODO Auto-generated method stub

  }

}
