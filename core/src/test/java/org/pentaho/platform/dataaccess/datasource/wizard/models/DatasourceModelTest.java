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
* Copyright (c) 2002-2018 Hitachi Vantara.  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.messages.LocaleHelper;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.wizard.TestUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryDSWDatasourceServiceImpl;
import org.pentaho.platform.engine.core.TestObjectFactory;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

@SuppressWarnings( "nls" )
public class DatasourceModelTest {

  @Test
  public void test() {
    DatasourceModel datasourceModel = new DatasourceModel();
    //    Assert.assertNull(datasourceModel.getCsvModel().getSelectedFile());
    Assert.assertNull( datasourceModel.getQuery() );
    assertNull( datasourceModel.getModelInfo().getFileInfo().getTmpFilename() );
    Assert.assertEquals( DatasourceType.NONE, datasourceModel.getDatasourceType() );
    datasourceModel.setDatasourceType( DatasourceType.SQL );
    Assert.assertEquals( false, datasourceModel.isValidated() );
    datasourceModel.setGuiStateModel( contructRelationalModel( datasourceModel.getGuiStateModel() ) );
    Assert.assertEquals( true, datasourceModel.isValidated() );
    datasourceModel.setDatasourceType( DatasourceType.CSV );
    Assert.assertEquals( false, datasourceModel.isValidated() );
    //    datasourceModel.setCsvModel(constructCsvModel(datasourceModel.getCsvModel()));    
    Assert.assertEquals( false, datasourceModel.isValidated() );
    datasourceModel.setModelInfo( TestUtil.createModel() );
    assertEquals( false, datasourceModel.isValidated() );
  }

  private GuiStateModel contructRelationalModel( GuiStateModel guiStateModel ) {
    IDatabaseConnection connection = new DatabaseConnection();
    connection.setAccessType( DatabaseAccessType.NATIVE );
    //    connection.setDriverClass("org.hsqldb.jdbcDriver");
    connection.setName( "SampleData" );
    connection.setPassword( "password" );
    //    connection.setUrl("jdbc:hsqldb:file:target/test-classes/solution1/system/data/sampledata");
    connection.setUsername( "pentaho_user" );
    List<IDatabaseConnection> connectionList = new ArrayList<IDatabaseConnection>();
    connectionList.add( connection );
    guiStateModel.setConnections( connectionList );
    guiStateModel.setPreviewLimit( "10" );
    guiStateModel.validateRelational();
    LogicalColumn logColumn = new LogicalColumn();
    logColumn.setDataType( DataType.NUMERIC );
    List<AggregationType> aggTypeList = new ArrayList<AggregationType>();
    aggTypeList.add( AggregationType.AVERAGE );
    logColumn.setAggregationList( aggTypeList );
    logColumn.setName( new LocalizedString( "En", "Column1" ) );
    BusinessData businessData = new BusinessData();
    List<List<String>> dataSample = new ArrayList<List<String>>();
    List<String> rowData = new ArrayList<String>();
    rowData.add( "Data1" );
    rowData.add( "Data2" );
    rowData.add( "Data3" );
    rowData.add( "Data4" );
    dataSample.add( rowData );

    String locale = LocaleHelper.getLocale().toString();

    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName( "SampleData" );
    model.setDatasource( dataSource );
    SqlPhysicalTable table = new SqlPhysicalTable( model );
    model.getPhysicalTables().add( table );
    table.setTargetTableType( TargetTableType.INLINE_SQL );
    table.setTargetTable( "select * from customers" );

    SqlPhysicalColumn column = new SqlPhysicalColumn( table );
    column.setTargetColumn( "customername" );
    column.setName( new LocalizedString( locale, "Customer Name" ) );
    column.setDescription( new LocalizedString( locale, "Customer Name Desc" ) );
    column.setDataType( DataType.STRING );

    table.getPhysicalColumns().add( column );

    LogicalModel logicalModel = new LogicalModel();
    model.setId( "MODEL" );
    model.setName( new LocalizedString( locale, "My Model" ) );
    model.setDescription( new LocalizedString( locale, "A Description of the Model" ) );

    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setPhysicalTable( table );

    logicalModel.getLogicalTables().add( logicalTable );

    LogicalColumn logicalColumn = new LogicalColumn();
    logicalColumn.setId( "LC_CUSTOMERNAME" );
    logicalColumn.setPhysicalColumn( column );

    logicalTable.addLogicalColumn( logicalColumn );

    Category mainCategory = new Category();
    mainCategory.setId( "CATEGORY" );
    mainCategory.setName( new LocalizedString( locale, "Category" ) );
    mainCategory.addLogicalColumn( logicalColumn );

    logicalModel.getCategories().add( mainCategory );

    Domain domain = new Domain();
    domain.addPhysicalModel( model );
    domain.addLogicalModel( logicalModel );
    List<LocaleType> localeTypeList = new ArrayList<LocaleType>();
    localeTypeList.add( new LocaleType( "Code", "Locale Description" ) );
    domain.setLocales( localeTypeList );
    businessData.setData( dataSample );
    businessData.setDomain( domain );
    guiStateModel.setLocaleCode( "en" );
    guiStateModel.setLogicalModels( domain.getLogicalModels() );
    guiStateModel.validateRelational();
    return guiStateModel;
  }

  @Test( expected = IllegalStateException.class )
  public void testGenerateTablename_nullDatasourceName() throws Exception {
    DatasourceModel model = new DatasourceModel();
    model.generateTableName();
  }

  @Test
  public void testRelationalDatasourceDTO() throws Exception {

    DatasourceModel datasourceModel = spy( new DatasourceModel() );
    doReturn( "testdatasource" ).when( datasourceModel ).generateTableName();
    datasourceModel.setDatasourceName( "testDatasource" );
    datasourceModel.setDatasourceType( DatasourceType.SQL );
    datasourceModel.setGuiStateModel( contructRelationalModel( datasourceModel.getGuiStateModel() ) );
    datasourceModel.setSelectedRelationalConnection( datasourceModel.getGuiStateModel().getConnections().get( 0 ) );

    DatasourceDTO dto = DatasourceDTO.generateDTO( datasourceModel );
    assertNotNull( dto );
    assertEquals( datasourceModel.getDatasourceName(), dto.getDatasourceName() );
    assertEquals( datasourceModel.getDatasourceType(), dto.getDatasourceType() );
    assertEquals( datasourceModel.getQuery(), dto.getQuery() );
    assertEquals( datasourceModel.getSelectedRelationalConnection().getName(), dto.getConnectionName() );

    DatasourceModel datasourceModel2 = spy( new DatasourceModel() );
    doReturn( "testdatasource" ).when( datasourceModel2 ).generateTableName();

    IDatabaseConnection connection = new DatabaseConnection();
    connection.setAccessType( DatabaseAccessType.NATIVE );
    //    connection.setDriverClass("org.hsqldb.jdbcDriver");
    connection.setName( "SampleData" );
    connection.setPassword( "password" );
    //    connection.setUrl("jdbc:hsqldb:file:target/test-classes/solution1/system/data/sampledata");
    connection.setUsername( "pentaho_user" );
    datasourceModel2.getGuiStateModel().setConnections( Collections.singletonList( connection ) );

    DatasourceDTO.populateModel( dto, datasourceModel2 );

    assertEquals( datasourceModel.getDatasourceName(), datasourceModel2.getDatasourceName() );
    assertEquals( datasourceModel.getDatasourceType(), datasourceModel2.getDatasourceType() );
    assertEquals( datasourceModel.getQuery(), datasourceModel2.getQuery() );
    assertEquals( datasourceModel.getSelectedRelationalConnection().getName(), datasourceModel2
        .getSelectedRelationalConnection().getName() );

  }

  @Test
  public void testDatasourceDTOSerialization() throws Exception {

    PentahoSystem.registerObjectFactory( new TestObjectFactory() );

    DatasourceModel datasourceModel = spy( new DatasourceModel() );
    doReturn( "testdatasource" ).when( datasourceModel ).generateTableName();
    datasourceModel.setDatasourceName( "testDatasource" );
    datasourceModel.setDatasourceType( DatasourceType.SQL );
    datasourceModel.setGuiStateModel( contructRelationalModel( datasourceModel.getGuiStateModel() ) );
    datasourceModel.setSelectedRelationalConnection( datasourceModel.getGuiStateModel().getConnections().get( 0 ) );

    DatasourceDTO dto = DatasourceDTO.generateDTO( datasourceModel );
    assertNotNull( dto );
    InMemoryDSWDatasourceServiceImpl service = new InMemoryDSWDatasourceServiceImpl();
    String dtoString = service.serializeModelState( dto );
    assertNotNull( dtoString );
    assertTrue( dtoString.contains( "testDatasource" ) );

    DatasourceDTO dto2 = service.deSerializeModelState( dtoString );

    assertEquals( dto, dto2 );
  }
}
