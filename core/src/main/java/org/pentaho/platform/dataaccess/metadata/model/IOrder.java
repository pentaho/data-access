/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
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
