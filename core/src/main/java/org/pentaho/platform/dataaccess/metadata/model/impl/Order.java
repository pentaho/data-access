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

package org.pentaho.platform.dataaccess.metadata.model.impl;

import org.pentaho.platform.dataaccess.metadata.model.IOrder;

public class Order implements IOrder {

  private static final long serialVersionUID = 4824503466813354111L;

  private String column;

  private String category;

  private String orderType;

  public String getColumn() {
    return column;
  }

  public String getOrderType() {
    return orderType;
  }

  public void setOrderType( String orderType ) {
    this.orderType = orderType;
  }

  public void setColumn( String column ) {
    this.column = column;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory( String category ) {
    this.category = category;
  }
}
