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
import java.util.Map;

/**
 * Represents an MQL Query. Contains a list of selected columns, conditions and order information.
 */
public interface IQuery extends Serializable {

  public IColumn[] getColumns();

  public ICondition[] getConditions();

  public IOrder[] getOrders();

  public String getDomainName();

  public String getModelId();

  public Map<String, String> getDefaultParameterMap();

  public Boolean getDisableDistinct();
}
