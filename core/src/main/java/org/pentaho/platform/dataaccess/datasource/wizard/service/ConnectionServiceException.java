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

import com.google.gwt.http.client.Response;

public class ConnectionServiceException extends Exception implements Serializable {


  private static final long serialVersionUID = 1L;
  protected int statusCode;

  public ConnectionServiceException() {
    super();
    this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
  }

  public ConnectionServiceException( String message ) {
    super( message );
    this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
  }

  public ConnectionServiceException( Throwable cause ) {
    super( cause );
    this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
  }

  public ConnectionServiceException( String message, Throwable cause ) {
    super( message, cause );
    this.statusCode = Response.SC_INTERNAL_SERVER_ERROR;
  }

  public ConnectionServiceException( int statusCode, String message, Throwable cause ) {
    super( message, cause );
    this.statusCode = statusCode;
  }

  public ConnectionServiceException( int statusCode, String message ) {
    this( message );
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

}
