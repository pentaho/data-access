/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.dataaccess;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.query.model.CombinationType;
import org.pentaho.platform.dataaccess.metadata.model.Operator;
import org.pentaho.platform.dataaccess.metadata.model.ICondition;
import org.pentaho.platform.dataaccess.metadata.model.impl.Condition;

public class ConditionTest {

  private ICondition icondition = null;
  private Condition condition = null;
  private final String categoryName = "categoryName";
  private final String columnName = "column_name";
  private final String paramName = "param_name";
  private final String[] values = { "value1" };
  private final String comboName = CombinationType.AND.name();
  private final String operator = Operator.EQUAL.name();

  @Before
  public void Initialization() {

    icondition = mock( Condition.class );
    when( icondition.getCategory() ).thenReturn( categoryName );
    when( icondition.getColumn() ).thenReturn( columnName );
    when( icondition.getValue() ).thenReturn( values );
    when( icondition.getCondition( any(), any() ) ).thenCallRealMethod();

    condition = mock( Condition.class );
  }

  @After
  public void finalize() {
    icondition = null;
    condition = null;
  }

  @Test
  public void testSetAndGetColumn() {

    doCallRealMethod().when( condition ).setColumn( anyString() );
    when( condition.getColumn() ).thenCallRealMethod();
    condition.setColumn( columnName );

    Assert.assertEquals( condition.getColumn(), columnName );
  }

  @Test
  public void testSetAndGetCombinationType() {

    doCallRealMethod().when( condition ).setCombinationType( anyString() );
    when( condition.getCombinationType() ).thenCallRealMethod();
    condition.setCombinationType( comboName );

    Assert.assertEquals( condition.getCombinationType(), comboName );
  }

  @Test
  public void testSetAndGetOperator() {

    doCallRealMethod().when( condition ).setOperator( anyString() );
    when( condition.getOperator() ).thenCallRealMethod();
    condition.setOperator( operator );

    Assert.assertEquals( condition.getOperator(), operator );
  }

  @Test
  public void testSetAndGetValue() {

    doCallRealMethod().when( condition ).setValue( any( String[].class ) );
    when( condition.getValue() ).thenCallRealMethod();
    condition.setValue( values );

    Assert.assertEquals( condition.getValue(), values );
  }

  @Test
  public void testGetConditionWithParametersForAllDataTypes() {

    Operator[] operatorsType1Array = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    for ( Operator operator : operatorsType1Array ) {

      when( icondition.getOperator() ).thenReturn( operator.toString() );

      for ( DataType dataType: DataType.values() ) {
        if ( dataType.getName().equals( DataType.STRING.getName() ) && operator.equals( Operator.EQUAL ) ) {
          //Exception for DataType STRING
          Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
            is( "[" + categoryName + "." + columnName + "] = [param:" + paramName + "]" ) );
        } else if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
          Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
            is( "[" + categoryName + "." + columnName + "] " + operator.toString() + "DATEVALUE([param:" + values[ 0 ] + "])" ) );
        } else {
          Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
            is( "[" + categoryName + "." + columnName + "] " + operator.toString() + "[param:" + paramName + "]" ) );
        }
      }
    }

    for ( DataType dataType: DataType.values() ) {

      when( icondition.getOperator() ).thenReturn( Operator.EXACTLY_MATCHES.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "[" + categoryName + "." + columnName + "] = \"DATEVALUE([param:" + values[ 0 ] + "])\"" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "[" + categoryName + "." + columnName + "] = " + "[param:" + paramName + "]" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.CONTAINS.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "CONTAINS([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\")" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "CONTAINS([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "])" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.DOES_NOT_CONTAIN.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "NOT(CONTAINS([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\"))" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "NOT(CONTAINS([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "]))" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.BEGINS_WITH.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "BEGINSWITH([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\")" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "BEGINSWITH([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "])" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.ENDS_WITH.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "ENDSWITH([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\")" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "ENDSWITH([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "])" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.IS_NULL.toString() );

      Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
        is( "ISNA([" + categoryName + "." + columnName + "])" ) );

      when( icondition.getOperator() ).thenReturn( Operator.IS_NOT_NULL.toString() );

      Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
        is( "NOT(ISNA([" + categoryName + "." + columnName + "]))" ) );
    }

  }

  @Test
  public void testGetConditionWithoutParametersForAllDataTypes() {

    Operator[] operatorsType1Array = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    for ( Operator operator: operatorsType1Array ) {

      when( icondition.getOperator() ).thenReturn( operator.toString() );

      for ( DataType dataType : DataType.values() ) {
        if ( dataType.getName().equals( DataType.STRING.getName() ) ) {
          //Exception for DataType STRING
          if ( operator.equals( Operator.EQUAL ) ) {
            Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
              is( "[" + categoryName + "." + columnName + "] = \"" + values[ 0 ] + "\"" ) );
          } else {
            Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
              is( "[" + categoryName + "." + columnName + "] " + operator.toString() + "\"" + values[ 0 ] + "\"" ) );
          }
        } else if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
          Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
            is( "[" + categoryName + "." + columnName + "] " + operator.toString() + "DATEVALUE(\"" + values[ 0 ] + "\")" ) );
        } else {
          Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
            is( "[" + categoryName + "." + columnName + "] " + operator.toString() + values[ 0 ] ) );
        }
      }
    }

    for ( DataType dataType : DataType.values() ) {

      when( icondition.getOperator() ).thenReturn( Operator.EXACTLY_MATCHES.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "[" + categoryName + "." + columnName + "] = \"DATEVALUE([param:" + values[ 0 ] + "])\"" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
          is( "[" + categoryName + "." + columnName + "] = \"" + values[ 0 ] + "\"" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.CONTAINS.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "CONTAINS([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\")" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
          is( "CONTAINS([" + categoryName + "." + columnName + "];\"" + values[ 0 ] + "\")" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.DOES_NOT_CONTAIN.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "NOT(CONTAINS([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\"))" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
          is( "NOT(CONTAINS([" + categoryName + "." + columnName + "];\"" + values[ 0 ] + "\"))" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.BEGINS_WITH.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "BEGINSWITH([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\")" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
          is( "BEGINSWITH([" + categoryName + "." + columnName + "];\"" + values[ 0 ] + "\")" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.ENDS_WITH.toString() );

      if ( dataType.getName().equals( DataType.DATE.getName() ) ) {
        Assert.assertThat( icondition.getCondition( dataType.getName(), paramName ),
          is( "ENDSWITH([" + categoryName + "." + columnName + "];\"DATEVALUE([param:" + values[ 0 ] + "])\")" ) );
      } else {
        Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
          is( "ENDSWITH([" + categoryName + "." + columnName + "];\"" + values[ 0 ] + "\")" ) );
      }

      when( icondition.getOperator() ).thenReturn( Operator.IS_NULL.toString() );

      Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
        is( "ISNA([" + categoryName + "." + columnName + "])" ) );

      when( icondition.getOperator() ).thenReturn( Operator.IS_NOT_NULL.toString() );

      Assert.assertThat( icondition.getCondition( dataType.getName(), null ),
        is( "NOT(ISNA([" + categoryName + "." + columnName + "]))" ) );

    }
  }
}
