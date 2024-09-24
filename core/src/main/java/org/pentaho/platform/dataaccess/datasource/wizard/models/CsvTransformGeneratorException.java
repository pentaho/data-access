/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

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
