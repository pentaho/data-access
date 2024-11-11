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


package org.pentaho.platform.dataaccess.metadata.model;

import java.io.Serializable;

/**
 * @author jamesdixon
 */
public interface IOrder extends Serializable {

  public String getColumn();

  public String getCategory();

  public String getOrderType();

}
