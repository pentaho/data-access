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


/**
 * Event listener interface for datasource model validation events.
 */

public interface IRelationalModelValidationListener {

  /**
   * Fired when the the model is valid
   */
  void onRelationalModelValid();

  /**
   * Fired when the the model is valid
   */
  void onRelationalModelInValid();
}

