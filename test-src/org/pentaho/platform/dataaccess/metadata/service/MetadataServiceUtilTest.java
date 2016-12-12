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

package org.pentaho.platform.dataaccess.metadata.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.IPhysicalColumn;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.Alignment;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.FieldType;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.query.model.CombinationType;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.metadata.model.impl.Column;
import org.pentaho.platform.dataaccess.metadata.model.impl.Condition;
import org.pentaho.platform.dataaccess.metadata.model.impl.Model;
import org.pentaho.platform.dataaccess.metadata.model.impl.Order;
import org.pentaho.platform.dataaccess.metadata.model.impl.Parameter;
import org.pentaho.platform.dataaccess.metadata.model.impl.Query;
import org.pentaho.metadata.query.model.Order.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class MetadataServiceUtilTest {

  private static final String DOMAIN_LOCALE_US = "en_US";
  private static final String DOMAIN_ID = "DOMAIN_ID";
  private static final String LOGICAL_MODEL_ID = "MODEL_ID";
  private static final String CATEGORY_ID = "CATEGORY_ID";
  private static final String CATEGORY_NAME = "CATEGORY_NAME";
  private static final String COLUMN_ID = "COLUMN_ID";
  private static final String COLUMN_ID_2 = "COLUMN_ID_2";
  private static final String COLUMN_NAME = "COLUMN_NAME";
  private static final String VALUE = "VALUE1";
  private static final String MASK = "###-####";
  private static final String RESULT = "RESULT";
  private static final Integer ROWS = 10;
  private static final Integer COLUMNS = 1;

  private LogicalModel logicalModel;
  private MetadataServiceUtil metadataServiceUtil;
  private IMetadataDomainRepository iMetadataDomainRepository;

  @Before
  public void initialize() {

    List<AggregationType> aggregationTypes = new ArrayList();
    AggregationType aggregationType = AggregationType.AVERAGE;
    aggregationTypes.add( aggregationType );

    List<LogicalColumn> logicalColumns = new ArrayList();
    LogicalColumn logicalColumn = mock( LogicalColumn.class );
    when( logicalColumn.getId() ).thenReturn( COLUMN_ID );
    when( logicalColumn.getDataType() ).thenReturn( DataType.STRING );
    when( logicalColumn.getProperty( "alignment" ) ).thenReturn( Alignment.CENTERED );
    when( logicalColumn.getAggregationList() ).thenReturn( aggregationTypes );
    logicalColumns.add( logicalColumn );

    LogicalColumn logicalColumn2 = mock( LogicalColumn.class );
    when( logicalColumn2.getId() ).thenReturn( COLUMN_ID_2 );
    when( logicalColumn2.getDataType() ).thenReturn( DataType.STRING );
    when( logicalColumn2.getFieldType() ).thenReturn( FieldType.DIMENSION );
    when( logicalColumn2.getProperty( "mask" ) ).thenReturn( MASK );
    logicalColumns.add( logicalColumn2 );

    List<Category> categoryList = new ArrayList();
    Category category = mock( Category.class );
    when( category.getId() ).thenReturn( CATEGORY_ID );
    when( category.getLogicalColumns() ).thenReturn( logicalColumns );
    when( category.findLogicalColumn( anyString() ) ).thenCallRealMethod();
    categoryList.add( category );

    logicalModel = mock( LogicalModel.class );
    when( logicalModel.getId() ).thenReturn( LOGICAL_MODEL_ID );
    when( logicalModel.getName( anyString() ) ).thenCallRealMethod();
    when( logicalModel.getDescription( anyString() ) ).thenCallRealMethod();
    when( logicalModel.getCategories() ).thenReturn( categoryList );
    when( logicalModel.findLogicalColumn( anyString() ) ).thenReturn( logicalColumn );
    when( logicalModel.getProperty( anyString() ) ).thenReturn( null );


    Domain domain = new Domain();
    domain.setId( DOMAIN_ID );
    domain.setLocales(  Arrays.asList( new LocaleType( DOMAIN_LOCALE_US, "Test locale" ) )  );
    domain.setLogicalModels( new ArrayList<LogicalModel>() { { add( logicalModel ); } } );

    iMetadataDomainRepository = mock( IMetadataDomainRepository.class );
    when( iMetadataDomainRepository.getDomain( DOMAIN_ID ) ).thenReturn( domain );
    Set<String> domains = new HashSet<String>();
    domains.add( DOMAIN_ID );
    when( iMetadataDomainRepository.getDomainIds() ).thenReturn( domains );

    metadataServiceUtil = mock( MetadataServiceUtil.class );
    when( metadataServiceUtil.getLocale() ).thenCallRealMethod();
    when( metadataServiceUtil.getDomain() ).thenReturn( domain );
    when( metadataServiceUtil.getMetadataRepository() ).thenReturn( iMetadataDomainRepository );

  }

  @After
  public void finalize() {

    metadataServiceUtil = null;
    logicalModel = null;
  }

  @Test
  public void testGetLocale() {

    String locale = metadataServiceUtil.getLocale();

    // Check if the locale value is correct
    Assert.assertTrue( locale.equals( DOMAIN_LOCALE_US ) );

  }

  @Test
  public void testCreateThinModel() {

    when( metadataServiceUtil.createThinModel( any( LogicalModel.class ), anyString() ) ).thenCallRealMethod();

    Model model = metadataServiceUtil.createThinModel( logicalModel, DOMAIN_ID );

    // Check if the model was created with the correct Domain Id
    Assert.assertTrue( model.getDomainId().equals( DOMAIN_ID ) );
  }

  @Test
  public void testCreateCdaJson() {

    String expectedCdaJson = "{\"metadata\":[{\"colName\":\"" + COLUMN_NAME + "\",\"colType\":\"" + DataType.STRING + "\",\"colIndex\":0,"
      + "\"colLabel\":\"" + COLUMN_NAME + "\"}],\"resultset\":[[null],[null],[null],[null],[null],[null],[null],[null],"
      + "[null],[null]]}";

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

    try {

      when( metadataServiceUtil.createCdaJson( any( IPentahoResultSet.class ), anyString() ) ).thenCallRealMethod();

      String cdaJson = metadataServiceUtil.createCdaJson( iPentahoResultSet, DOMAIN_LOCALE_US );

      Assert.assertEquals( expectedCdaJson, cdaJson );

    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testGetDomainObject() {

    String xmlQuery = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>" + DOMAIN_ID + "</domain_id><model_id"
      + ">" + LOGICAL_MODEL_ID + "</model_id><options><disable_distinct>false</disable_distinct><limit>-1</limit></options"
      + "><parameters><parameter defaultValue=\"" + VALUE + "\" name=\"" + COLUMN_NAME + "\" "
      + "type=\"STRING\"/></parameters><selections><selection><view>" + CATEGORY_ID + "</view><column>" + COLUMN_ID + "</column"
      + "><aggregation>NONE</aggregation></selection></selections><constraints><constraint><operator>AND</operator"
      + "><condition>[" + CATEGORY_NAME + "." + COLUMN_NAME + "] = " + VALUE + "</condition></constraint></constraints><orders/></mql>";

    try {

      when( metadataServiceUtil.getDomainObject( anyString() ) ).thenCallRealMethod();
      Domain domain = metadataServiceUtil.getDomainObject( xmlQuery );

      //Test the domain id
      Assert.assertEquals( domain.getId(), DOMAIN_ID );

    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testConvertQuery() {

    when( metadataServiceUtil.convertQuery( any( Query.class ) ) ).thenCallRealMethod();
    when( metadataServiceUtil.getCategory( anyString(), any( LogicalModel.class ) ) ).thenCallRealMethod();

    Query inputQuery = buildQuery();
    org.pentaho.metadata.query.model.Query query = metadataServiceUtil.convertQuery( inputQuery );

    //Test the query parameters array length
    Assert.assertTrue( query.getParameters().size() == 1 );
    //Test the query selections array length
    Assert.assertTrue( query.getSelections().size() == 1 );
    //Test the query constraints array length
    Assert.assertTrue( query.getConstraints().size() == 1 );
    //Test the query orders array length
    Assert.assertTrue( query.getOrders().size() == 1 );
    //Test the query domain id
    Assert.assertTrue( query.getDomain().getId().equals( DOMAIN_ID ) );

  }

  @Test
  public void testGetCategory() {

    when( metadataServiceUtil.getCategory( anyString(), any( LogicalModel.class ) ) ).thenCallRealMethod();

    Category category = metadataServiceUtil.getCategory( COLUMN_ID, logicalModel );

    //Test the domain id
    Assert.assertEquals( category.getId(), CATEGORY_ID );
  }

  @Test
  public void testGetCategoryNull() {

    when( metadataServiceUtil.getCategory( anyString(), any( LogicalModel.class ) ) ).thenCallRealMethod();

    Category category = metadataServiceUtil.getCategory( "Invalid ID", logicalModel );

    //Test there is no category match
    Assert.assertNull( category );
  }

  @Test
  public void testDeserializeJsonQuery() {

    final String jsonQuery = "{\"columns\": [ {\"id\":\"" + COLUMN_NAME + "\", \"name\":\"" + COLUMN_ID
      + "\", \"selectedAggType\":\"" + AggregationType.NONE.toString() + "\", \"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Column\"}],"
      + "\"conditions\":[ {\"column\":\"" + COLUMN_NAME + "\", \"comboType\":\"" + CombinationType.AND.toString()
      + "\", \"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Condition\"}],"
      + "\"orders\":[ {\"column\":\"" + COLUMN_ID + "\", \"orderType\":\"" + Type.ASC.toString() + "\","
      + "\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Order\"}],"
      + "\"parameters\":[ {\"column\":\"" + COLUMN_NAME + "\", \"value\":[\"" + VALUE + "\"],"
      + "\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Parameter\"}],"
      + "\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Query\",\"domainName\":\"" + DOMAIN_ID + "\","
      + "\"modelId\":\"" + LOGICAL_MODEL_ID + "\",\"disableDistinct\":null,\"defaultParameterMap\":null}";

    when( metadataServiceUtil.deserializeJsonQuery( anyString() ) ).thenCallRealMethod();

    Query query = metadataServiceUtil.deserializeJsonQuery( jsonQuery );

    //Test the query columns array length
    Assert.assertTrue( query.getColumns().length == 1 );
    //Test the query conditions array length
    Assert.assertTrue( query.getConditions().length == 1 );
    //Test the query orders array length
    Assert.assertTrue( query.getOrders().length == 1 );
    //Test the query parameters array length
    Assert.assertTrue( query.getParameters().length == 1 );
    //Test the query domain name
    Assert.assertTrue( query.getDomainName().equals( DOMAIN_ID ) );

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
    when( order.getColumn() ).thenReturn( COLUMN_ID );
    when( order.getOrderType() ).thenReturn( Type.ASC.toString() );
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
