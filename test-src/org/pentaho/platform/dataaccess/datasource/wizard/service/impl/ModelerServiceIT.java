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
* Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.junit.Test;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.automodel.importing.strategy.CsvDatasourceImportStrategy;
import org.pentaho.metadata.model.thin.Column;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class ModelerServiceIT extends DataAccessServiceTestBase {
  private final String TEST_CATALOG_NAME = "TEST_CATALOG_NAME";

  @Test
  public void testRemoveCatalogDuringSerializeModels() throws Exception {
    when( policy.isAllowed( anyString() ) ).thenReturn( Boolean.TRUE );
    when( pluginResourceLoader.getPluginSetting( (Class) anyObject(), anyString(), anyString() ) )
        .thenReturn( SimpleDataAccessPermissionHandler.class.getName() );
    MondrianCatalog mockCatalog = mock( MondrianCatalog.class );
    when( mondrianCatalogService.getCatalog( anyString(), (IPentahoSession) anyObject() ) ).thenReturn( mockCatalog );

    modelerService.serializeModels( domain, TEST_CATALOG_NAME );

    // verify removeCatalog is called
    verify( mondrianCatalogService, times( 1 ) ).removeCatalog( anyString(), (IPentahoSession) anyObject() );
  }

  @Test
  public void domainForCsvDatasource_GeneratedWithCsvDatasourceImportStrategy() throws Exception {
    ModelInfo modelInfo = new ModelInfo();
    ColumnInfo[] columnInfos = new ColumnInfo[] { createColumnInfo( "id", "title" ) };
    modelInfo.setColumns( columnInfos );

    modelerService = spy( modelerService );
    DatabaseMeta dbMeta = mock( DatabaseMeta.class );
    doReturn( dbMeta ).when( modelerService ).getDatabaseMeta();

    TableModelerSource source = mock( TableModelerSource.class );
    doReturn( source ).when( modelerService )
      .createTableModelerSource( any( DatabaseMeta.class ), anyString(), anyString(), anyString() );

    modelerService.generateCSVDomain( modelInfo );

    verify( modelerService ).toColumns( columnInfos );
    // most important thing here, is that domain is generated with CsvDatasourceImportStrategy
    verify( source ).generateDomain( any( CsvDatasourceImportStrategy.class ) );
  }

  @Test
  public void columnsConvertedCorrectly_FromColumnInfos() throws Exception {
    final String columnId = "id";
    final String columnTitle = "title";

    ColumnInfo[] columnInfos = new ColumnInfo[] { createColumnInfo( columnId, columnTitle ) };
    Column[] columns = modelerService.toColumns( columnInfos );

    assertEquals( 1, columns.length );
    Column column = columns[ 0 ];

    assertEquals( columnId, column.getId() );
    assertEquals( columnTitle, column.getName() );
  }


  private ColumnInfo createColumnInfo( String id, String title ) {
    ColumnInfo columnInfo = new ColumnInfo();

    columnInfo.setId( id );
    columnInfo.setTitle( title );

    return columnInfo;
  }
}
