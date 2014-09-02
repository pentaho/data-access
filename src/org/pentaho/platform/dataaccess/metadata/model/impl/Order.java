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
