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

import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceChild;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;
import org.pentaho.platform.dataaccess.catalog.impl.Datasource;
import org.pentaho.platform.dataaccess.catalog.impl.DatasourceChild;
import org.pentaho.platform.dataaccess.catalog.impl.DatasourceType;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;

public class AnalysisDatasourceProvider implements IDatasourceProvider {

  private MondrianCatalogHelper mondrianCatalogHelper;
  private IDatasourceType datasourceType = new AnalysisDatasourceType();

  public AnalysisDatasourceProvider() {
    this.mondrianCatalogHelper = (MondrianCatalogHelper) PentahoSystem.get( IMondrianCatalogService.class );
  }

  public AnalysisDatasourceProvider( final MondrianCatalogHelper mondrianCatalogHelper ) {
    this.mondrianCatalogHelper = mondrianCatalogHelper;
  }

  @Override
  public List<IDatasource> getDatasources() {
    List<IDatasource> datasources = new ArrayList<IDatasource>();

    for ( MondrianCatalog mondrianCatalog : mondrianCatalogHelper.listCatalogs( PentahoSessionHolder.getSession(),
        false ) ) {
      List<IDatasourceChild> datasourceChildren = new ArrayList<IDatasourceChild>();

      for ( MondrianCube cube : mondrianCatalog.getSchema().getCubes() ) {
        datasourceChildren.add( new DatasourceChild( cube.getId(), cube.getName(), null ) );
      }

      IDatasourceChild datasourceChild =
          new DatasourceChild( mondrianCatalog.getSchema().getName(), mondrianCatalog.getSchema().getName(),
              datasourceChildren );

      List<DatasourceChild> children = new ArrayList<DatasourceChild>();
      children.add( (DatasourceChild) datasourceChild );
      datasources.add( new Datasource( mondrianCatalog.getName(), (DatasourceType) getType(), children ) );
    }
    return datasources;
  }

  @Override
  public IDatasourceType getType() {
    return datasourceType;
  }

}
