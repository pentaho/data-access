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

package org.pentaho.platform.dataaccess.datasource.provider;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.datasource.DatasourceMgmtServiceException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.dataaccess.api.catalog.IDatasource;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceProvider;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceType;
import org.pentaho.platform.dataaccess.impl.catalog.Datasource;
import org.pentaho.platform.dataaccess.impl.catalog.DatasourceType;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class JDBCDatasourceProvider implements IDatasourceProvider {

  private IDatasourceMgmtService datasourceMgmtService;
  private IDatasourceType datasourceType = new JDBCDatasourceType();
  IPentahoSession pentahoSession = PentahoSessionHolder.getSession();

  public JDBCDatasourceProvider() {
    this.datasourceMgmtService = PentahoSystem.get( IDatasourceMgmtService.class, pentahoSession );
  }

  public JDBCDatasourceProvider( final IDatasourceMgmtService datasourceMgmtService ) {
    this.datasourceMgmtService = datasourceMgmtService;
  }

  @Override
  public List<IDatasource> getDatasources() {
    List<IDatasource> datasources = new ArrayList<IDatasource>();
    try {
      for ( IDatabaseConnection databaseConnection : datasourceMgmtService.getDatasources() ) {
        datasources.add( new Datasource( databaseConnection.getName(), (DatasourceType) getType(), null ) );
      }

    } catch ( DatasourceMgmtServiceException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return datasources;
  }

  @Override
  public IDatasourceType getType() {
    return datasourceType;
  }

}
