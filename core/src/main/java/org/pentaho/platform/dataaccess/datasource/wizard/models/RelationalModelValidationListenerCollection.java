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

import java.util.ArrayList;

public class RelationalModelValidationListenerCollection extends ArrayList<IRelationalModelValidationListener> {
  private static final long serialVersionUID = 1L;

  /**
   * Fires a relational model valid event to all listeners.
   */
  public void fireRelationalModelValid() {
    for ( IRelationalModelValidationListener listener : this ) {
      listener.onRelationalModelValid();
    }
  }

  /**
   * Fires a relational model valid event to all listeners.
   */
  public void fireRelationalModelInValid() {
    for ( IRelationalModelValidationListener listener : this ) {
      listener.onRelationalModelInValid();
    }
  }
}
