/*
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

package org.pentaho.platform.dataaccess.catalog.impl;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceCatalog;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;

public class DatasourceCatalogTest {
  private IDatasourceCatalog datasourceCatalog;

  @Before
  public void setUp() throws Exception {
    datasourceCatalog = new DatasourceCatalog();
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testGetDatasources() {
    IDatasource datasource1 = new Datasource( "datasource1", new MockDatasourceType( "1" ) );
    IDatasource datasource2 = new Datasource( "datasource2", new MockDatasourceType( "1" ) );
    List<IDatasource> datasources = new ArrayList<IDatasource>();
    datasources.add( datasource1 );
    datasources.add( datasource2 );

    IDatasourceProvider provider = new DatasourceProvider( datasource1.getType(), datasources );
    ( (DatasourceCatalog) datasourceCatalog ).registerDatasourceProvider( provider );

    List<IDatasource> result = datasourceCatalog.getDatasources();

    assertEquals( 2, result.size() );
    assertEquals( datasource1, result.get( 0 ) );
    assertEquals( datasource2, result.get( 1 ) );
  }

  @Test
  public void testGetDatasourcesOfType() {
    IDatasource datasource1 = new Datasource( "datasource1", new MockDatasourceType( "1" ) );
    IDatasource datasource2 = new Datasource( "datasource2", new MockDatasourceType( "1" ) );
    List<IDatasource> datasources1 = new ArrayList<IDatasource>();
    datasources1.add( datasource1 );
    datasources1.add( datasource2 );
    IDatasourceProvider provider = new DatasourceProvider( datasource1.getType(), datasources1 );
    ( (DatasourceCatalog) datasourceCatalog ).registerDatasourceProvider( provider );

    List<IDatasource> datasources2 = new ArrayList<IDatasource>();
    IDatasource datasource3 = new Datasource( "datasource3", new MockDatasourceType( "2" ) );
    IDatasource datasource4 = new Datasource( "datasource4", new MockDatasourceType( "2" ) );
    datasources2.add( datasource3 );
    datasources2.add( datasource4 );
    provider = new DatasourceProvider( datasource3.getType(), datasources2 );
    ( (DatasourceCatalog) datasourceCatalog ).registerDatasourceProvider( provider );

    List<IDatasource> result = datasourceCatalog.getDatasourcesOfType( datasource1.getType() );
    assertEquals( 2, result.size() );
    assertEquals( datasource1, result.get( 0 ) );
    assertEquals( datasource2, result.get( 1 ) );

    result = datasourceCatalog.getDatasourcesOfType( datasource3.getType() );
    assertEquals( 2, result.size() );
    assertEquals( datasource3, result.get( 0 ) );
    assertEquals( datasource4, result.get( 1 ) );
  }

  @Test
  public void testGetDatasourceTypes() {
    IDatasourceProvider provider = new DatasourceProvider( new MockDatasourceType( "1" ) );
    ( (DatasourceCatalog) datasourceCatalog ).registerDatasourceProvider( provider );

    provider = new DatasourceProvider( new MockDatasourceType( "2" ) );
    ( (DatasourceCatalog) datasourceCatalog ).registerDatasourceProvider( provider );

    List<IDatasourceType> result = datasourceCatalog.getDatasourceTypes();
    assertEquals( 2, result.size() );
    assertEquals( "1", result.get( 0 ).getId() );
    assertEquals( "2", result.get( 1 ).getId() );
  }

  public class MockDatasourceType implements IDatasourceType {
    String id;

    public MockDatasourceType( String id ) {
      this.id = id;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getDisplayName( Locale locale ) {
      return "testDatasource" + id;
    }

  }
}
