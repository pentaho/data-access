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



package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.io.Serializable;

public class SqlQueriesNotSupportedException extends Exception implements Serializable {

  private static final long serialVersionUID = 1L;

  public SqlQueriesNotSupportedException() {
    super();
  }

  public SqlQueriesNotSupportedException( String message ) {
    super( message );
  }

}
