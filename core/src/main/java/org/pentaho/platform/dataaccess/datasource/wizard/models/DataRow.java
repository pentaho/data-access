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



package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;

public class DataRow implements Serializable {

  private static final long serialVersionUID = 2498165533349885182L;

  private Object[] cells;

  public Object[] getCells() {
    return cells;
  }

  public void setCells( Object[] cells ) {
    this.cells = cells;
  }

}
