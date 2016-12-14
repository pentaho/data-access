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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.metadata.model;

import java.io.Serializable;

/**
 * Represents a condition (filter/constraint)
 *
 * @author jamesdixon
 */
public interface ICondition extends Serializable {

  /**
   * Returns the id of the column for the condition
   *
   * @return
   */
  public String getColumn();

  /**
   * Returns the category for the condition
   *
   * @return
   */
  public String getCategory();

  /**
   * Returns the operator for the condition. This value is a string representation of one of the possible values of
   * Operator
   *
   * @return
   */
  public String getOperator();

  /**
   * @return if isParameterized() then the name of the parameter whose value will be substituted before query execution,
   * else the literal value
   */
  public String[] getValue();

  /**
   * Returns the combiner to be used for combining subsequent conditions
   *
   * @return
   */
  public String getCombinationType();

  /**
   * Returns the comparision plus value, i.e "= 'Atlanta'"
   *
   * @return a string formatted to support parameters
   */
  public String getCondition( String type );

  /**
   * Returns the comparision plus value, i.e "= 'Atlanta'"
   *
   * @param paramname If set, this value is to be used as the name of the parameter for this condition
   * @return a string formatted to support parameters based on the enforceParams flag
   */
  public String getCondition( String type, String paramName );

  /**
   * Value in this condition is not static, but rather supplied for each execution of this query.
   *
   * @return true if value denotes parameter name rather than a literal value
   */
  //  public boolean isParameterized();

  //  public String getDefaultValue();

  //  public String getSelectedAggType();
}
