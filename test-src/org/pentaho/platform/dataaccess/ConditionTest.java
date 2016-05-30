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

package org.pentaho.platform.dataaccess;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.dataaccess.metadata.model.Operator;
import org.pentaho.platform.dataaccess.metadata.model.ICondition;
import org.pentaho.platform.dataaccess.metadata.model.impl.Condition;

public class ConditionTest {

  private ICondition condition = null;
  private final String categoryName = "categoryName";
  private final String columnName = "column_name";
  private final String paramName = "param_name";
  private final String[] values = { "value1" };

  @Before
  public void Initialization() {

    condition = mock( Condition.class );
    when( condition.getCategory() ).thenReturn( categoryName );
    when( condition.getColumn() ).thenReturn( columnName );
    when( condition.getValue() ).thenReturn( values );
    when( condition.getCondition( anyString(), anyString() ) ).thenCallRealMethod();

  }

  @Test
  public void testGetConditionWithParametersForAllDataTypes() {

    Operator[] operatorsArray = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    for ( Operator operator : operatorsArray ) {

      when( condition.getOperator() ).thenReturn( operator.toString() );

      for ( DataType dataType: DataType.values() ) {
        if ( dataType.name().equals( DataType.STRING.name() ) && operator.equals( Operator.EQUAL ) ) {
          //Exception for DataType STRING
          Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
            is( "[" + categoryName + "." + columnName + "] = [param:" + paramName + "]" ) );
        } else {
          Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
            is( "[" + categoryName + "." + columnName + "] " + operator.toString() + "[param:" + paramName + "]" ) );
        }
      }
    }

    for ( DataType dataType: DataType.values() ) {

      when( condition.getOperator() ).thenReturn( Operator.EXACTLY_MATCHES.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
        is( "[" + categoryName + "." + columnName + "] = " + "[param:" + paramName + "]" ) );

      when( condition.getOperator() ).thenReturn( Operator.CONTAINS.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
        is( "CONTAINS([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "])" ) );

      when( condition.getOperator() ).thenReturn( Operator.DOES_NOT_CONTAIN.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
        is( "NOT(CONTAINS([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "]))" ) );

      when( condition.getOperator() ).thenReturn( Operator.BEGINS_WITH.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
        is( "BEGINSWITH([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "])" ) );

      when( condition.getOperator() ).thenReturn( Operator.ENDS_WITH.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
        is( "ENDSWITH([" + categoryName + "." + columnName + "];" + "[param:" + paramName + "])" ) );

      when( condition.getOperator() ).thenReturn( Operator.IS_NULL.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
        is( "ISNA([" + categoryName + "." + columnName + "])" ) );

      when( condition.getOperator() ).thenReturn( Operator.IS_NOT_NULL.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), paramName ),
        is( "NOT(ISNA([" + categoryName + "." + columnName + "]))" ) );
    }

  }

  @Test
  public void testGetConditionWithoutParametersForAllDataTypes() {

    Operator[] operatorsArray = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    for ( Operator operator: operatorsArray ) {

      when( condition.getOperator() ).thenReturn( operator.toString() );

      for ( DataType dataType : DataType.values() ) {
        if ( dataType.name().equals( DataType.STRING.name() ) && operator.equals( Operator.EQUAL ) ) {
          //Exception for DataType STRING
          Assert.assertThat( condition.getCondition( dataType.name(), null ),
            is( "[" + categoryName + "." + columnName + "] = " + values[ 0 ] ) );
        } else {
          Assert.assertThat( condition.getCondition( dataType.name(), null ),
            is( "[" + categoryName + "." + columnName + "] " + operator.toString() + values[ 0 ] ) );
        }
      }
    }

    for ( DataType dataType : DataType.values() ) {

      when( condition.getOperator() ).thenReturn( Operator.EXACTLY_MATCHES.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), null ),
        is( "[" + categoryName + "." + columnName + "] = " + values[ 0 ] ) );

      when( condition.getOperator() ).thenReturn( Operator.CONTAINS.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), null ),
        is( "CONTAINS([" + categoryName + "." + columnName + "];" + values[ 0 ] + ")" ) );

      when( condition.getOperator() ).thenReturn( Operator.DOES_NOT_CONTAIN.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), null ),
        is( "NOT(CONTAINS([" + categoryName + "." + columnName + "];" + values[ 0 ] + "))" ) );

      when( condition.getOperator() ).thenReturn( Operator.BEGINS_WITH.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), null ),
        is( "BEGINSWITH([" + categoryName + "." + columnName + "];" + values[ 0 ] + ")" ) );

      when( condition.getOperator() ).thenReturn( Operator.ENDS_WITH.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), null ),
        is( "ENDSWITH([" + categoryName + "." + columnName + "];" + values[ 0 ] + ")" ) );

      when( condition.getOperator() ).thenReturn( Operator.IS_NULL.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), null ),
        is( "ISNA([" + categoryName + "." + columnName + "])" ) );

      when( condition.getOperator() ).thenReturn( Operator.IS_NOT_NULL.toString() );

      Assert.assertThat( condition.getCondition( dataType.name(), null ),
        is( "NOT(ISNA([" + categoryName + "." + columnName + "]))" ) );

    }
  }
}
