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

import java.sql.Connection;

import javax.sql.DataSource;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.data.IDBDatasourceService;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.dataaccess.datasource.IStagingDatabase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class PlatformStagingDatabase implements IStagingDatabase {

  AgileMartDatasourceHelper agileMartDatasourceHelper;

  public PlatformStagingDatabase() {
    IPluginResourceLoader resLoader =
      PentahoSystem.get( IPluginResourceLoader.class, PentahoSessionHolder.getSession() );
    this.agileMartDatasourceHelper = new AgileMartDatasourceHelper( resLoader );
  }

  @Override
  public Connection getConnection() throws Exception {
    IDBDatasourceService datasourceService = PentahoSystem.get( IDBDatasourceService.class );
    DataSource dataSource = datasourceService.getDataSource( getName() );
    return dataSource.getConnection();
  }

  @Override
  public IDatabaseConnection getDatbaseMetadata() {
    return agileMartDatasourceHelper.getAgileMartDatasource();
  }

  @Override
  public String getName() {
    return agileMartDatasourceHelper.getAgileMartDatasource().getName();
  }

}
