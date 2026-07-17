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


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import org.apache.commons.text.StringEscapeUtils;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.utils.Base64PasswordUtils;

/**
 * Created by Yury_Bakhmutski on 8/11/2016.
 */
public class UtilHtmlSanitizer {

  private UtilHtmlSanitizer() {
  }

  private static UtilHtmlSanitizer sanitizerInstance;

  public static synchronized UtilHtmlSanitizer getInstance() {
    if ( sanitizerInstance == null ) {
      sanitizerInstance = new UtilHtmlSanitizer();
    }
    return sanitizerInstance;
  }

  public void sanitizeConnectionParameters( IDatabaseConnection connection ) {
    String safeName = safeEscapeHtml( connection.getName() );
    connection.setName( safeName );

    String safeDbName = safeEscapeHtml( connection.getDatabaseName() );
    connection.setDatabaseName( safeDbName );

    String safeDbPort = safeEscapeHtml( connection.getDatabasePort() );
    connection.setDatabasePort( safeDbPort );

    String safeHostname = safeEscapeHtml( connection.getHostname() );
    connection.setHostname( safeHostname );

    String safePassword = safeEscapeHtml( Base64PasswordUtils.decodePassword( connection.getPassword() ) );
    connection.setPassword( safePassword );

    String safeUsername = safeEscapeHtml( connection.getUsername() );
    connection.setUsername( safeUsername );
  }

  public void unsanitizeConnectionParameters( IDatabaseConnection connection ) {
    String safeName = StringEscapeUtils.unescapeHtml4( connection.getName() );
    connection.setName( safeName );

    String safeDbName = StringEscapeUtils.unescapeHtml4( connection.getDatabaseName() );
    connection.setDatabaseName( safeDbName );

    String safeDbPort = StringEscapeUtils.unescapeHtml4( connection.getDatabasePort() );
    connection.setDatabasePort( safeDbPort );

    String safeHostname = StringEscapeUtils.unescapeHtml4( connection.getHostname() );
    connection.setHostname( safeHostname );

    String safePassword = StringEscapeUtils.unescapeHtml4( connection.getPassword() );
    connection.setPassword( safePassword );

    String safeUsername = StringEscapeUtils.unescapeHtml4( connection.getUsername() );
    connection.setUsername( safeUsername );
  }

  public String safeEscapeHtml( String html ) {
    return escapeHtml( StringEscapeUtils.unescapeHtml4( html ) );
  }

  public String escape( String stringToEscape ) {
    return escapeHtml( stringToEscape );
  }

  private String escapeHtml( String html ) {
    if ( html == null ) {
      return null;
    }

    StringBuilder escapedHtml = new StringBuilder( html.length() );
    for ( int index = 0; index < html.length(); ) {
      int codePoint = html.codePointAt( index );
      if ( codePoint > 127 ) {
        escapedHtml.append( "&#" ).append( codePoint ).append( ';' );
      } else {
        escapedHtml.append( StringEscapeUtils.escapeHtml4( new String( Character.toChars( codePoint ) ) ) );
      }
      index += Character.charCount( codePoint );
    }
    return escapedHtml.toString();
  }

}
