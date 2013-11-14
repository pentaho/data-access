/**
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.dataaccess.catalog;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.dataaccess.api.catalog.IDatasource;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceProvider;
import org.pentaho.platform.dataaccess.impl.catalog.Datasource;
import org.pentaho.platform.dataaccess.impl.catalog.DatasourceProvider;
import org.pentaho.platform.dataaccess.impl.catalog.DatasourceType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class DatasourceProviderTest {

  IDatasourceProvider datasourceProvider;
  DatasourceType csvDatasourceType;

  /**
   *
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {

    csvDatasourceType = new DatasourceType( "CSV", "Comma separated values (CSV)" );

    Datasource datasource1 = new Datasource(  );
    datasource1.setName( "CSV Data source 1" );
    datasource1.setType( csvDatasourceType );

    Datasource datasource2 = new Datasource(  );
    datasource2.setName( "CSV Data source 2" );
    datasource2.setType( csvDatasourceType );

    List<IDatasource> datasourceList = new ArrayList( Arrays.asList( datasource1, datasource2 ) );

    datasourceProvider = new DatasourceProvider( csvDatasourceType, datasourceList );
  }

  /**
   *
   * @throws Exception
   */
  @Test
  public void testGetDatasources() throws Exception {
    assertTrue( datasourceProvider.getType().equals( csvDatasourceType ) );

    assertTrue( datasourceProvider.getDatasources().size() == 2 );

    assertTrue( datasourceProvider.getDatasources().get( 0 ).getType().equals( csvDatasourceType ) );
  }
}
