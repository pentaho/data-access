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
