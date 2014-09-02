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
