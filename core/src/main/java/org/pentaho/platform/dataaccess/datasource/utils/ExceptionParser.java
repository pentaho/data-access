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


package org.pentaho.platform.dataaccess.datasource.utils;

public class ExceptionParser {
  public static String DELIMETER = "-"; //$NON-NLS-1$

  public static String getErrorMessage( Throwable throwable, String defaultErrorMessage ) {
    String message = throwable.getLocalizedMessage();
    if ( message != null && message.length() > 0 ) {
      int index = message.indexOf( DELIMETER );
      if ( index > 0 ) {
        return message.substring( index + 1 );
      } else {
        return message;
      }
    } else {
      return defaultErrorMessage;
    }

  }

  public static String getErrorHeader( Throwable throwable, String defaultErrorHeader ) {
    String message = throwable.getLocalizedMessage();
    if ( message != null && message.length() > 0 ) {
      int index = message.indexOf( DELIMETER );
      if ( index > 0 ) {
        return message.substring( 0, index - 1 );
      } else {
        return defaultErrorHeader;
      }
    } else {
      return defaultErrorHeader;
    }

  }
}
