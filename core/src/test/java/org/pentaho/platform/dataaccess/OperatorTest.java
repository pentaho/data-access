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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Test;
import org.junit.Assert;
import org.pentaho.platform.dataaccess.metadata.model.Operator;

public class OperatorTest {

  @Test
  public void testParse() {

    Assert.assertThat( Operator.parse( ">" ), is( Operator.GREATER_THAN ) );
    Assert.assertThat( Operator.parse( "<" ), is( Operator.LESS_THAN ) );
    Assert.assertThat( Operator.parse( "=" ), is( Operator.EQUAL ) );
    Assert.assertThat( Operator.parse( ">=" ), is( Operator.GREATOR_OR_EQUAL ) );
    Assert.assertThat( Operator.parse( "<=" ), is( Operator.LESS_OR_EQUAL ) );

    Assert.assertThat( Operator.parse( "EXACTLY MATCHES" ), is( Operator.EXACTLY_MATCHES ) );
    Assert.assertThat( Operator.parse( "CONTAINS" ), is( Operator.CONTAINS ) );
    Assert.assertThat( Operator.parse( "DOES NOT CONTAIN" ), is( Operator.DOES_NOT_CONTAIN ) );
    Assert.assertThat( Operator.parse( "BEGINS WITH" ), is( Operator.BEGINS_WITH ) );
    Assert.assertThat( Operator.parse( "ENDS WITH" ), is( Operator.ENDS_WITH ) );

    Assert.assertThat( Operator.parse( "IS NULL" ), is( Operator.IS_NULL ) );
    Assert.assertThat( Operator.parse( "IS NOT NULL" ), is( Operator.IS_NOT_NULL ) );

    Assert.assertThat( Operator.parse( null ), is( Operator.EQUAL ) );
    Assert.assertThat( Operator.parse( "" ), is( Operator.EQUAL ) );
    Assert.assertThat( Operator.parse( "DUMMY STRING" ), is( Operator.EQUAL ) );

  }


  @Test
  public void testToString() {

    Assert.assertThat( Operator.GREATER_THAN.toString(), is( ">" ) );
    Assert.assertThat( Operator.LESS_THAN.toString(), is( "<" ) );
    Assert.assertThat( Operator.EQUAL.toString(), is( "=" ) );
    Assert.assertThat( Operator.GREATOR_OR_EQUAL.toString(), is( ">=" ) );
    Assert.assertThat( Operator.LESS_OR_EQUAL.toString(), is( "<=" ) );

    Assert.assertThat( Operator.EXACTLY_MATCHES.toString(), is( "EXACTLY MATCHES" ) );
    Assert.assertThat( Operator.CONTAINS.toString(), is( "CONTAINS" ) );
    Assert.assertThat( Operator.DOES_NOT_CONTAIN.toString(), is( "DOES NOT CONTAIN" ) );
    Assert.assertThat( Operator.BEGINS_WITH.toString(), is( "BEGINS WITH" ) );
    Assert.assertThat( Operator.ENDS_WITH.toString(), is( "ENDS WITH" ) );

    Assert.assertThat( Operator.IS_NULL.toString(), is( "IS NULL" ) );
    Assert.assertThat( Operator.IS_NOT_NULL.toString(), is( "IS NOT NULL" ) );
  }

  @Test
  public void testRequiresValue() {

    Assert.assertThat( Operator.GREATER_THAN.requiresValue(), is( true ) );
    Assert.assertThat( Operator.LESS_THAN.requiresValue(), is( true ) );
    Assert.assertThat( Operator.EQUAL.requiresValue(), is( true ) );
    Assert.assertThat( Operator.GREATOR_OR_EQUAL.requiresValue(), is( true ) );
    Assert.assertThat( Operator.LESS_OR_EQUAL.requiresValue(), is( true ) );

    Assert.assertThat( Operator.EXACTLY_MATCHES.requiresValue(), is( true ) );
    Assert.assertThat( Operator.CONTAINS.requiresValue(), is( true ) );
    Assert.assertThat( Operator.DOES_NOT_CONTAIN.requiresValue(), is( true ) );
    Assert.assertThat( Operator.BEGINS_WITH.requiresValue(), is( true ) );
    Assert.assertThat( Operator.ENDS_WITH.requiresValue(), is( true ) );

    Assert.assertThat( Operator.IS_NULL.requiresValue(), is( false ) );
    Assert.assertThat( Operator.IS_NOT_NULL.requiresValue(), is( false ) );

  }

  @Test
  public void testFormatConditionForNonParameterizedSingleValues() {

    String columnName = "column_name";
    String paramName = "param_name";
    String[] values = { "value1" };
    Operator[] operatorsArray = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    //check with different number of values passed as argument
    for ( Operator operator : operatorsArray ) {
      Assert.assertThat( operator.formatCondition( columnName, paramName, values, false ),
        is( columnName + " " + operator.toString() + values[ 0 ] ) );
    }

    Assert.assertThat( Operator.EXACTLY_MATCHES.formatCondition( columnName, paramName, values, false ),
          is( columnName + " = " + values[ 0 ] ) );

    Assert.assertThat( Operator.CONTAINS.formatCondition( columnName, paramName, values, false ),
      is( "CONTAINS(" + columnName + ";" + values[ 0 ] + ")" ) );

    Assert.assertThat( Operator.DOES_NOT_CONTAIN.formatCondition( columnName, paramName, values, false ),
      is( "NOT(CONTAINS(" + columnName + ";" + values[ 0 ] + "))" ) );

    Assert.assertThat( Operator.BEGINS_WITH.formatCondition( columnName, paramName, values, false ),
      is( "BEGINSWITH(" + columnName + ";" + values[ 0 ] + ")" ) );

    Assert.assertThat( Operator.ENDS_WITH.formatCondition( columnName, paramName, values, false ),
      is( "ENDSWITH(" + columnName + ";" + values[ 0 ] + ")" ) );

    Assert.assertThat( Operator.IS_NULL.formatCondition( columnName, paramName, values, false ),
      is( "ISNA(" + columnName + ")" ) );

    Assert.assertThat( Operator.IS_NOT_NULL.formatCondition( columnName, paramName, values, false ),
      is( "NOT(ISNA(" + columnName + "))" ) );
  }

  @Test
  public void testFormatConditionForNonParameterizedMultipleValues() {

    String columnName = "column_name";
    String paramName = "param_name";
    String[] values = { "\"value1\"", "value2", "value3" };
    Operator[] operatorsArray = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    //check with different number of values passed as argument
    for ( Operator operator : operatorsArray ) {
      Assert.assertThat( operator.formatCondition( columnName, paramName, values, false ),
        is( columnName + " " + operator.toString() + values[ 0 ] ) );
    }

    String valuesList = "";
    for ( int idx = 0; idx < values.length; idx++ ) {
      if ( idx > 0 ) {
        valuesList += ";";
      }
      if ( !values[ idx ].startsWith( "\"" ) && !values[ idx ].endsWith( "\"" ) ) {
        valuesList += "\"" + values[ idx ] + "\"";
      } else {
        valuesList += values[ idx ];
      }
    }
    Assert.assertThat( Operator.EXACTLY_MATCHES.formatCondition( columnName, paramName, values, false ),
      is( "IN(" + columnName + "; " + valuesList + ")" ) );

    Assert.assertThat( Operator.CONTAINS.formatCondition( columnName, paramName, values, false ),
      is( "CONTAINS(" + columnName + ";" + values[ 0 ] + ")" ) );

    Assert.assertThat( Operator.DOES_NOT_CONTAIN.formatCondition( columnName, paramName, values, false ),
      is( "NOT(CONTAINS(" + columnName + ";" + values[ 0 ] + "))" ) );

    Assert.assertThat( Operator.BEGINS_WITH.formatCondition( columnName, paramName, values, false ),
      is( "BEGINSWITH(" + columnName + ";" + values[ 0 ] + ")" ) );

    Assert.assertThat( Operator.ENDS_WITH.formatCondition( columnName, paramName, values, false ),
      is( "ENDSWITH(" + columnName + ";" + values[ 0 ] + ")" ) );

    Assert.assertThat( Operator.IS_NULL.formatCondition( columnName, paramName, values, false ),
      is( "ISNA(" + columnName + ")" ) );

    Assert.assertThat( Operator.IS_NOT_NULL.formatCondition( columnName, paramName, values, false ),
      is( "NOT(ISNA(" + columnName + "))" ) );
  }

  @Test
  public void testFormatConditionForParameterizedSingleValues() {

    String columnName = "column_name";
    String paramName = "param_name";
    String[] values = { "value1" };
    Operator[] operatorsArray = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    //check with different number of values passed as argument
    for ( Operator operator : operatorsArray ) {
      Assert.assertThat( operator.formatCondition( columnName, paramName, values, true ),
        is( columnName + " " + operator.toString() + "[param:" + paramName + "]" ) );
    }

    Assert.assertThat( Operator.EXACTLY_MATCHES.formatCondition( columnName, paramName, values, true ),
      is( columnName + " = " + "[param:" + paramName + "]" ) );

    Assert.assertThat( Operator.CONTAINS.formatCondition( columnName, paramName, values, true ),
      is( "CONTAINS(" + columnName + ";" + "[param:" + paramName + "])" ) );

    Assert.assertThat( Operator.DOES_NOT_CONTAIN.formatCondition( columnName, paramName, values, true ),
      is( "NOT(CONTAINS(" + columnName + ";" + "[param:" + paramName + "]))" ) );

    Assert.assertThat( Operator.BEGINS_WITH.formatCondition( columnName, paramName, values, true ),
      is( "BEGINSWITH(" + columnName + ";" + "[param:" + paramName + "])" ) );

    Assert.assertThat( Operator.ENDS_WITH.formatCondition( columnName, paramName, values, true ),
      is( "ENDSWITH(" + columnName + ";" + "[param:" + paramName + "])" ) );

    Assert.assertThat( Operator.IS_NULL.formatCondition( columnName, paramName, values, true ),
      is( "ISNA(" + columnName + ")" ) );

    Assert.assertThat( Operator.IS_NOT_NULL.formatCondition( columnName, paramName, values, true ),
      is( "NOT(ISNA(" + columnName + "))" ) );
  }

  @Test
  public void testFormatConditionForParameterizedMultipleValues() {

    String columnName = "column_name";
    String paramName = "param_name";
    String[] values = { "\"value1\"", "value2", "value3" };
    Operator[] operatorsArray = { Operator.GREATER_THAN, Operator.LESS_THAN, Operator.EQUAL, Operator.GREATOR_OR_EQUAL, Operator.LESS_OR_EQUAL };

    //check with different number of values passed as argument
    for ( Operator operator : operatorsArray ) {
      Assert.assertThat( operator.formatCondition( columnName, paramName, values, true ),
        is( columnName + " " + operator.toString() + "[param:" + paramName + "]" ) );
    }

    Assert.assertThat( Operator.EXACTLY_MATCHES.formatCondition( columnName, paramName, values, true ),
      is( columnName + " = " + "[param:" + paramName + "]" ) );

    Assert.assertThat( Operator.CONTAINS.formatCondition( columnName, paramName, values, true ),
      is( "CONTAINS(" + columnName + ";" + "[param:" + paramName + "])" ) );

    Assert.assertThat( Operator.DOES_NOT_CONTAIN.formatCondition( columnName, paramName, values, true ),
      is( "NOT(CONTAINS(" + columnName + ";" + "[param:" + paramName + "]))" ) );

    Assert.assertThat( Operator.BEGINS_WITH.formatCondition( columnName, paramName, values, true ),
      is( "BEGINSWITH(" + columnName + ";" + "[param:" + paramName + "])" ) );

    Assert.assertThat( Operator.ENDS_WITH.formatCondition( columnName, paramName, values, true ),
      is( "ENDSWITH(" + columnName + ";" + "[param:" + paramName + "])" ) );

    Assert.assertThat( Operator.IS_NULL.formatCondition( columnName, paramName, values, true ),
      is( "ISNA(" + columnName + ")" ) );

    Assert.assertThat( Operator.IS_NOT_NULL.formatCondition( columnName, paramName, values, true ),
      is( "NOT(ISNA(" + columnName + "))" ) );
  }

}

