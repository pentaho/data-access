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

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;

public class CsvTransformGeneratorException extends Exception implements Serializable {

  private static final long serialVersionUID = 1L;
  private String causeMessage;
  private String causeStackTrace;
  private String localizedMessage;

  public CsvTransformGeneratorException() {
    super();
  }

  public CsvTransformGeneratorException( String message ) {
    super( message );
  }

  public CsvTransformGeneratorException( Throwable cause ) {
    super( cause );
  }

  public CsvTransformGeneratorException( String message, Throwable cause ) {
    super( message, cause );
  }

  public CsvTransformGeneratorException( String message, Throwable cause, String causeStackTrace ) {
    super( message, cause );
    this.causeMessage = cause.getMessage();
    this.causeStackTrace = causeStackTrace;
  }

  public CsvTransformGeneratorException( String message, Throwable cause, String causeMessage,
                                         String causeStackTrace ) {
    super( message, cause );
    this.causeMessage = causeMessage;
    this.causeStackTrace = causeStackTrace;
  }

  public CsvTransformGeneratorException( String message, Throwable cause, String causeMessage,
      String causeStackTrace, String localizedMessage ) {
    this( message, cause, causeMessage, causeStackTrace );
    this.localizedMessage = localizedMessage;
  }

  public String getCauseMessage() {
    return causeMessage;
  }

  public void setCauseMessage( String causeMessage ) {
    this.causeMessage = causeMessage;
  }

  public String getCauseStackTrace() {
    return causeStackTrace;
  }

  public void setCauseStackTrace( String causeStackTrace ) {
    this.causeStackTrace = causeStackTrace;
  }

  @Override
  public String getLocalizedMessage() {
    if ( localizedMessage != null ) {
      return localizedMessage;
    }
    return super.getLocalizedMessage();
  }

}
