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


package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.io.Serializable;

public class DatasourceServiceException extends Exception implements Serializable {


  private static final long serialVersionUID = 1L;

  public DatasourceServiceException() {
    super();
  }

  public DatasourceServiceException( String message ) {
    super( message );
  }

  public DatasourceServiceException( Throwable cause ) {
    super( cause );
  }

  public DatasourceServiceException( String message, Throwable cause ) {
    super( message, cause );
  }

}
