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
* Copyright (c) 2002-2024 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.metadata.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableColumnNames;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.commons.connection.marshal.MarshallableRow;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.query.model.CombinationType;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.metadata.model.impl.Column;
import org.pentaho.platform.dataaccess.metadata.model.impl.Condition;
import org.pentaho.platform.dataaccess.metadata.model.impl.Model;
import org.pentaho.platform.dataaccess.metadata.model.impl.ModelInfo;
import org.pentaho.platform.dataaccess.metadata.model.impl.Order;
import org.pentaho.platform.dataaccess.metadata.model.impl.Parameter;
import org.pentaho.platform.dataaccess.metadata.model.impl.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class MetadataServiceTest {
  private static final String DOMAIN_ID = "DOMAIN_ID";
  private static final String LOGICAL_MODEL_ID = "MODEL_ID";
  private static final String LOGICAL_MODEL_2_ID = "MODEL_2_ID";
  private static final String LOGICAL_MODEL_NAME = "A_MODEL_NAME";
  private static final String LOGICAL_MODEL_2_NAME = "Z_MODEL_2_NAME";
  private static final String CATEGORY_ID = "CATEGORY_ID";
  private static final String CATEGORY_NAME = "CATEGORY_NAME";
  private static final String COLUMN_ID = "COLUMN_ID";
  private static final String COLUMN_NAME = "COLUMN_NAME";
  private static final String VALUE = "VALUE1";
  private static final String RESULT = "RESULT";
  private static final Integer ROWS = 10;
  private static final Integer COLUMNS = 1;

  private LogicalModel logicalModel;
  private LogicalModel logicalModel2;
  private MetadataService metadataService;
  private MetadataServiceUtil metadataServiceUtil;
  private IMetadataDomainRepository iMetadataDomainRepository;

  @Before
  public void initialize() {

    List<Category> categoryList = new ArrayList();
    Category category = mock( Category.class );
    when( category.getId() ).thenReturn( CATEGORY_ID );
    categoryList.add( category );

    LogicalColumn logicalColumn = mock( LogicalColumn.class );
    when( logicalColumn.getId() ).thenReturn( COLUMN_ID );
    when( logicalColumn.getDataType() ).thenReturn( DataType.STRING );
    when( category.findLogicalColumn( anyString() ) ).thenReturn( logicalColumn );

    logicalModel = mock( LogicalModel.class );
    when( logicalModel.getId() ).thenReturn( LOGICAL_MODEL_ID );
    when( logicalModel.getName( any() ) ).thenReturn( LOGICAL_MODEL_NAME );
    when( logicalModel.getCategories() ).thenReturn( categoryList );
    when( logicalModel.findLogicalColumn( anyString() ) ).thenReturn( logicalColumn );
    when( logicalModel.getProperty( anyString() ) ).thenReturn( null );

    logicalModel2 = mock( LogicalModel.class );
    when( logicalModel2.getId() ).thenReturn( LOGICAL_MODEL_2_ID );
    when( logicalModel2.getName( anyString() ) ).thenReturn( LOGICAL_MODEL_2_NAME );
    when( logicalModel2.getProperty( anyString() ) ).thenReturn( null );

    Domain domainOnlyReportingModel = new Domain();
    domainOnlyReportingModel.setId( DOMAIN_ID );
    domainOnlyReportingModel.setLogicalModels( new ArrayList<LogicalModel>() { { add( logicalModel2 ); } { add( logicalModel ); } } );

    iMetadataDomainRepository = mock( IMetadataDomainRepository.class );
    when( iMetadataDomainRepository.getDomain( DOMAIN_ID ) ).thenReturn( domainOnlyReportingModel );
    Set<String> domains = new HashSet<String>();
    domains.add( DOMAIN_ID );
    when( iMetadataDomainRepository.getDomainIds() ).thenReturn( domains );

    metadataService = mock( MetadataService.class );
    when( metadataService.getMetadataRepository() ).thenReturn( iMetadataDomainRepository );

    metadataServiceUtil = mock( MetadataServiceUtil.class );
    when( metadataServiceUtil.getMetadataRepository() ).thenReturn( iMetadataDomainRepository );
    when( metadataServiceUtil.createThinModel( Mockito.<LogicalModel>any(), anyString() ) ).thenCallRealMethod();
    when( metadataService.getMetadataServiceUtil() ).thenReturn( metadataServiceUtil );
  }

  @After
  public void finalize() {

    metadataService = null;
    metadataServiceUtil = null;
    logicalModel = null;
    logicalModel2 = null;
    iMetadataDomainRepository = null;
  }

  @Test
  public void testGetDatasourcePermissions() {

    when( metadataService.getDatasourcePermissions() ).thenCallRealMethod();

    //Test view permissions
    when( metadataService.hasViewAccess() ).thenReturn( true );
    Assert.assertThat( metadataService.getDatasourcePermissions(), is( "\"VIEW\"" ) );

    //Test edit permissions
    when( metadataService.hasManageAccess() ).thenReturn( true );
    Assert.assertThat( metadataService.getDatasourcePermissions(), is( "\"EDIT\"" ) );

    //No permissions test
    when( metadataService.hasViewAccess() ).thenReturn( false );
    when( metadataService.hasManageAccess() ).thenReturn( false );
    Assert.assertThat( metadataService.getDatasourcePermissions(), is( "\"NONE\"" ) );

  }

  @Test
  public void testListBusinessModels() {

    try {
      when( metadataService.listBusinessModels( any(), any() ) ).thenCallRealMethod();

      ModelInfo[] businessModels = metadataService.listBusinessModels( DOMAIN_ID, "" );
      //Test business models array length
      Assert.assertTrue( businessModels.length == 2 );
      //Test business models array sort
      Assert.assertTrue( businessModels[0].getModelName() == LOGICAL_MODEL_NAME );
      Assert.assertTrue( businessModels[1].getModelName() == LOGICAL_MODEL_2_NAME );

      businessModels = metadataService.listBusinessModels( null, null );
      //Test business models array length
      Assert.assertTrue( businessModels.length == 2 );
      //Test business models array sort
      Assert.assertTrue( businessModels[0].getModelName() == LOGICAL_MODEL_NAME );
      Assert.assertTrue( businessModels[1].getModelName() == LOGICAL_MODEL_2_NAME );

    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testListBusinessModelsJson() {

    final String expectedBusinessModelsJson = "[{\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl"
      + ".ModelInfo\",\"domainId\":\"" + DOMAIN_ID + "\",\"modelDescription\":null,\"modelId\":\"" + LOGICAL_MODEL_ID + "\","
      + "\"modelName\":\"" + LOGICAL_MODEL_NAME + "\"},{\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl"
      + ".ModelInfo\",\"domainId\":\"" + DOMAIN_ID + "\",\"modelDescription\":null,\"modelId\":\"" + LOGICAL_MODEL_2_ID + "\","
      + "\"modelName\":\"" + LOGICAL_MODEL_2_NAME + "\"}]";

    try {
      when( metadataService.listBusinessModels( anyString(), nullable( String.class ) ) ).thenCallRealMethod();
      when( metadataService.listBusinessModelsJson( anyString(), nullable( String.class ) ) ).thenCallRealMethod();

      String businessModelsJson = metadataService.listBusinessModelsJson( DOMAIN_ID, "" );

      Assert.assertEquals( expectedBusinessModelsJson, businessModelsJson );

    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testLoadModel() {

    when( metadataService.loadModel( anyString(), anyString() ) ).thenCallRealMethod();

    Model model = metadataService.loadModel( DOMAIN_ID, LOGICAL_MODEL_ID );
    //Test if the name of the model returned is correct
    Assert.assertTrue( model.getName() == LOGICAL_MODEL_NAME );
  }

  @Test
  public void testLoadModelJson() {

    final String expectedModelJson = "{\"categories\":[{\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl"
      + ".Category\",\"columns\":[],\"id\":\"" + CATEGORY_ID + "\",\"name\":null}],\"class\":\"org.pentaho.platform"
      + ".dataaccess.metadata.model.impl.Model\",\"description\":null,\"domainId\":\"" + DOMAIN_ID + "\",\"id\":\"" + LOGICAL_MODEL_ID
      + "\",\"name\":\"" + LOGICAL_MODEL_NAME + "\"}";

    when( metadataService.loadModel( anyString(), anyString() ) ).thenCallRealMethod();
    when( metadataService.loadModelJson( anyString(), anyString() ) ).thenCallRealMethod();

    String modelJson = metadataService.loadModelJson( DOMAIN_ID, LOGICAL_MODEL_ID );

    Assert.assertEquals( expectedModelJson, modelJson );
  }

  @Test
  public void testDoQuery() {

    when( metadataService.doQuery( any( Query.class ), anyInt() ) ).thenCallRealMethod();
    when( metadataService.doXmlQuery( anyString(), any( Integer.class ) ) ).thenCallRealMethod();
    when( metadataServiceUtil.convertQuery( any( Query.class ) ) ).thenCallRealMethod();
    when( metadataServiceUtil.getCategory( anyString(), any( LogicalModel.class ) ) ).thenCallRealMethod();

    MarshallableResultSet marshallableResultSet = getMarshallableResultSet();
    when( metadataService.getMarshallableResultSet() ).thenReturn( marshallableResultSet );

    Query query = buildQuery();
    MarshallableResultSet marshallableResultSetReturned = metadataService.doQuery( query, ROWS );

    MarshallableRow[] rows = marshallableResultSetReturned.getRows();
    String[] cell = rows[0].getCell();
    MarshallableColumnNames columnNames = marshallableResultSetReturned.getColumnNames();
    String[] columnName = columnNames.getColumnName();

    // Check the result rows lenght
    Assert.assertTrue( rows.length <= ROWS );

    // Check the result row value
    Assert.assertTrue( cell[0].equals( RESULT ) );

    // Check the result column name
    Assert.assertTrue( columnName[0].equals( COLUMN_NAME ) );


  }

  @Test
  public void testDoXmlQuery() {

    String xmlQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>" + DOMAIN_ID + "</domain_id><model_id"
      + ">" + LOGICAL_MODEL_ID + "</model_id><options><disable_distinct>false</disable_distinct><limit>-1</limit></options"
      + "><parameters><parameter defaultValue=\"" + VALUE + "\" name=\"" + COLUMN_NAME + "\" "
      + "type=\"STRING\"/></parameters><selections><selection><view>" + CATEGORY_ID + "</view><column>" + COLUMN_ID + "</column"
      + "><aggregation>NONE</aggregation></selection></selections><constraints><constraint><operator>AND</operator"
      + "><condition>[" + CATEGORY_NAME + "." + COLUMN_NAME + "] = " + VALUE + "</condition></constraint></constraints><orders/></mql>";

    when( metadataService.doXmlQuery( anyString(), any( Integer.class ) ) ).thenCallRealMethod();

    MarshallableResultSet marshallableResultSet = getMarshallableResultSet();
    when( metadataService.getMarshallableResultSet() ).thenReturn( marshallableResultSet );

    MarshallableResultSet marshallableResultSetReturned = metadataService.doXmlQuery( xmlQuery, ROWS );

    MarshallableRow[] rows = marshallableResultSetReturned.getRows();
    String[] cell = rows[0].getCell();
    MarshallableColumnNames columnNames = marshallableResultSetReturned.getColumnNames();
    String[] columnName = columnNames.getColumnName();

    // Check the result rows lenght
    Assert.assertTrue( rows.length <= ROWS );

    // Check the result row value
    Assert.assertTrue( cell[0].equals( RESULT ) );

    // Check the result column name
    Assert.assertTrue( columnName[0].equals( COLUMN_NAME ) );


  }

  @Test
  public void testDoXmlQueryToCdaJson() {

    String expectedQueryJson = "{\"metadata\":[{\"colName\":\"" + COLUMN_NAME + "\",\"colType\":\"" + DataType.STRING + "\",\"colIndex\":0,"
      + "\"colLabel\":\"" + COLUMN_NAME + "\"}],\"resultset\":[[null],[null],[null],[null],[null],[null],[null],[null],"
      + "[null],[null]]}";

    String xmlQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>" + DOMAIN_ID + "</domain_id><model_id"
      + ">" + LOGICAL_MODEL_ID + "</model_id><options><disable_distinct>false</disable_distinct><limit>-1</limit></options"
      + "><parameters><parameter defaultValue=\"" + VALUE + "\" name=\"" + COLUMN_NAME + "\" "
      + "type=\"STRING\"/></parameters><selections><selection><view>" + CATEGORY_ID + "</view><column>" + COLUMN_ID + "</column"
      + "><aggregation>NONE</aggregation></selection></selections><constraints><constraint><operator>AND</operator"
      + "><condition>[" + CATEGORY_NAME + "." + COLUMN_NAME + "] = " + VALUE + "</condition></constraint></constraints><orders/></mql>";

    try {
      when( metadataService.doXmlQuery( anyString(), any( Integer.class ) ) ).thenCallRealMethod();
      when( metadataService.doXmlQueryToCdaJson( anyString(), anyInt() ) ).thenCallRealMethod();
      when( metadataServiceUtil.createCdaJson( any( IPentahoResultSet.class ), anyString() ) ).thenCallRealMethod();
      when( metadataServiceUtil.getDomainObject( anyString() ) ).thenCallRealMethod();

      MarshallableResultSet marshallableResultSet = getMarshallableResultSet();
      when( metadataService.getMarshallableResultSet() ).thenReturn( marshallableResultSet );

      String queryJson = metadataService.doXmlQueryToCdaJson( xmlQuery, ROWS );

      Assert.assertEquals( expectedQueryJson, queryJson );
    } catch ( Exception ex ) {
      fail();
    }

  }

  private MarshallableResultSet getMarshallableResultSet() {

    //Build the IPentahoResultSet
    Object[][] result = new Object[ 1 ][ COLUMNS ];
    result[ 0 ][ 0 ] = COLUMN_NAME;
    Object[] values = new Object[ 1 ];
    values[ 0 ] = RESULT;
    LocalizedString localizedString = mock( LocalizedString.class );
    when( localizedString.getString( anyString() ) ).thenReturn( COLUMN_NAME );
    IPentahoMetaData iPentahoMetaData = mock( IPentahoMetaData.class );
    when( iPentahoMetaData.getColumnHeaders() ).thenReturn( result );
    when( iPentahoMetaData.getAttribute( anyInt(), anyInt(), eq( IPhysicalColumn.DATATYPE_PROPERTY ) ) ).thenReturn( DataType.STRING );
    when( iPentahoMetaData.getAttribute( anyInt(), anyInt(), eq( Concept.NAME_PROPERTY ) ) ).thenReturn( localizedString );
    IPentahoResultSet iPentahoResultSet = mock( IPentahoResultSet.class );
    when( iPentahoResultSet.getRowCount() ).thenReturn( ROWS );
    when( iPentahoResultSet.getColumnCount() ).thenReturn( COLUMNS );
    when( iPentahoResultSet.getMetaData() ).thenReturn( iPentahoMetaData );
    when( iPentahoResultSet.next() ).thenReturn( values ).thenReturn( null );
    when( metadataService.executeQuery( anyString(), any( Integer.class ) ) ).thenReturn( iPentahoResultSet );

    //Build the MarshallableResultSet
    MarshallableResultSet marshallableResultSet = mock( MarshallableResultSet.class );
    doCallRealMethod().when( marshallableResultSet ).setResultSet( any( IPentahoResultSet.class ) );
    when( marshallableResultSet.getRows() ).thenCallRealMethod();
    when( marshallableResultSet.getColumnNames() ).thenCallRealMethod();
    return marshallableResultSet;
  }

  private Query buildQuery() {

    //Build the query Columns
    AggregationType defaultAggType = AggregationType.NONE;
    Column[] columns = new Column[ 1 ];
    Column column = mock( Column.class );
    when( column.getId() ).thenReturn( COLUMN_ID );
    when( column.getName() ).thenReturn( COLUMN_NAME );
    when( column.getSelectedAggType() ).thenReturn( defaultAggType.name() );
    columns[0] = column;

    //Build the query Conditions
    CombinationType combinationType = CombinationType.AND;
    Condition[] conditions = new Condition[ 1 ];
    Condition condition = mock( Condition.class );
    when( condition.getCombinationType() ).thenReturn( combinationType.name() );
    when( condition.getColumn() ).thenReturn( COLUMN_NAME );
    when( condition.getCondition( anyString(), anyString() ) ).thenReturn( "[" + CATEGORY_NAME + "." + COLUMN_NAME + "] = " + VALUE );
    conditions[0] = condition;

    //Build the query Parameters
    String[] values = {VALUE};
    Parameter[] parameters = new Parameter[ 1 ];
    Parameter parameter = mock( Parameter.class );
    when( parameter.getColumn() ).thenReturn( COLUMN_NAME );
    when( parameter.getValue() ).thenReturn( values );
    parameters[0] = parameter;

    //Build the query Order
    Order[] orders = new Order[ 1 ];
    Order order = mock( Order.class );
    orders[0] = order;

    //Build the Query
    Query query = mock( Query.class );
    when( query.getDomainName() ).thenReturn( DOMAIN_ID );
    when( query.getModelId() ).thenReturn( LOGICAL_MODEL_ID );
    when( query.getColumns() ).thenReturn( columns );
    when( query.getConditions() ).thenReturn( conditions );
    when( query.getParameters() ).thenReturn( parameters );
    when( query.getOrders() ).thenReturn( orders );

    return query;
  }
}
