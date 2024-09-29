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


package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.io.Serializable;

public class QueryValidationException extends Exception implements Serializable {


  private static final long serialVersionUID = 1L;

  public QueryValidationException() {
    super();
  }

  public QueryValidationException( String message ) {
    super( message );
  }

  public QueryValidationException( Throwable cause ) {
    super( cause );
  }

  public QueryValidationException( String message, Throwable cause ) {
    super( message, cause );
  }

}
