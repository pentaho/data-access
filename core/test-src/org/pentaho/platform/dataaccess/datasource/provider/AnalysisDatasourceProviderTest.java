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

package org.pentaho.platform.dataaccess.datasource.provider;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.api.catalog.IDatasource;
import org.pentaho.platform.dataaccess.api.catalog.IDatasourceProvider;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;

public class AnalysisDatasourceProviderTest {

  private static final String ANALYSIS_TYPE = new AnalysisDatasourceType().getId();
  private static final String MONDRIAN_DS_PROVIDER = "Provider=mondrian;DataSource=SampleData;";

  private static final String STEEL_WHEELS = "SteelWheels";
  private static final String SAMPLE_DATA = "SampleData";

  private static final MondrianCube mockCube = new MondrianCube( "cubeName", "cubeId" );

  IPentahoSession standaloneSession;
  MondrianCatalogHelper mondrianCatalogHelper;
  IDatasourceProvider datasourceProvider;

  /**
   * 
   * @throws Exception
   */
  @Before
  public void setUp() throws Exception {
    standaloneSession = new StandaloneSession( "admin" );
    standaloneSession.setAttribute( "MONDRIAN_SCHEMA_XML_CONTENT", "<mock-schema></mock-schema>" );
  }

  @After
  public void tearDown() throws Exception {
    // do nothing
  }

  @Test
  public void testDatasourceProvider() throws Exception {

    mondrianCatalogHelper = mock( MondrianCatalogHelper.class );

    // steelwheels mondrian catalog
    MondrianSchema steelWheelsSchema =
        new MondrianSchema( STEEL_WHEELS, Arrays.asList( new MondrianCube[] { mockCube } ) );
    MondrianCatalog steelWheelsCatalog =
        new MondrianCatalog( STEEL_WHEELS, MONDRIAN_DS_PROVIDER, STEEL_WHEELS + ".mondrian.xml", steelWheelsSchema );

    // sampledata mondrian catalog
    MondrianSchema sampleDataSchema =
        new MondrianSchema( SAMPLE_DATA, Arrays.asList( new MondrianCube[] { mockCube } ) );
    MondrianCatalog sampleDataCatalog =
        new MondrianCatalog( SAMPLE_DATA, MONDRIAN_DS_PROVIDER, SAMPLE_DATA + ".mondrian.xml", sampleDataSchema );

    // add catalogs to mondrianCatalogHelper
    mondrianCatalogHelper.addCatalog( steelWheelsCatalog, false, standaloneSession );
    mondrianCatalogHelper.addCatalog( sampleDataCatalog, false, standaloneSession );

    List<MondrianCatalog> returnList = new ArrayList<MondrianCatalog>();
    returnList.add( sampleDataCatalog );
    returnList.add( steelWheelsCatalog );

    // mock mondrianCatalogHelper.listCatalogs return
    doReturn( returnList ).when( mondrianCatalogHelper ).listCatalogs( Mockito.any( IPentahoSession.class ),
        Mockito.anyBoolean() );

    // create AnalysisDatasourceProvider
    IDatasourceProvider analysisDatasourceProvider = new AnalysisDatasourceProvider( mondrianCatalogHelper );
    List<IDatasource> datasources = analysisDatasourceProvider.getDatasources();

    assertEquals( 2, datasources.size() );

    assertEquals( SAMPLE_DATA, datasources.get( 0 ).getName() );
    assertEquals( ANALYSIS_TYPE, datasources.get( 0 ).getType().getId() );

    assertEquals( STEEL_WHEELS, datasources.get( 1 ).getName() );
    assertEquals( ANALYSIS_TYPE, datasources.get( 1 ).getType().getId() );
  }

}
