/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.metadata.model.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.dataaccess.metadata.model.IQuery;

public class Query implements IQuery {

  private static final long serialVersionUID = 8616769258583080677L;

  public static final List<Class> CLASS_LIST = new ArrayList<Class>( Arrays.asList( Query.class, Column.class, Order.class, Parameter.class,
          Condition.class ) );
  private Column[] columns = new Column[0];

  private Condition[] conditions = new Condition[0];

  private Order[] orders = new Order[ 0 ];

  private Parameter[] parameters = new Parameter[ 0 ];

  private String domainName;

  private String modelId;

  private Boolean disableDistinct;

  /**
   * Keys are parameter names; values are defaults for those parameters.
   */
  private Map<String, String> defaultParameterMap;

  public Query() {
    super();
  }

  public Column[] getColumns() {
    return columns;
  }

  public Condition[] getConditions() {
    return conditions;
  }

  public String getDomainName() {
    return domainName;
  }

  public String getModelId() {
    return modelId;
  }

  public Order[] getOrders() {
    return orders;
  }

  public void setColumns( Column[] columns ) {

    this.columns = columns;
  }

  public void setConditions( Condition[] conditions ) {

    this.conditions = conditions;
  }

  public void setOrders( Order[] orders ) {

    this.orders = orders;
  }

  public void setDomainName( String domainName ) {

    this.domainName = domainName;
  }

  public void setModelId( String modelId ) {

    this.modelId = modelId;
  }

  public Map<String, String> getDefaultParameterMap() {
    return defaultParameterMap;
  }

  public void setDefaultParameterMap( Map<String, String> defaultParameterMap ) {
    this.defaultParameterMap = defaultParameterMap;
  }

  public Boolean getDisableDistinct() {
    return disableDistinct;
  }

  public void setDisableDistinct( Boolean disableDistinct ) {
    this.disableDistinct = disableDistinct;
  }

  public Parameter[] getParameters() {
    return parameters;
  }

  public void setParameters( Parameter[] parameters ) {
    this.parameters = parameters;
  }

}
