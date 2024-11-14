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

import org.apache.commons.lang.StringEscapeUtils;
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
    String safeName = StringEscapeUtils.unescapeHtml( connection.getName() );
    connection.setName( safeName );

    String safeDbName = StringEscapeUtils.unescapeHtml( connection.getDatabaseName() );
    connection.setDatabaseName( safeDbName );

    String safeDbPort = StringEscapeUtils.unescapeHtml( connection.getDatabasePort() );
    connection.setDatabasePort( safeDbPort );

    String safeHostname = StringEscapeUtils.unescapeHtml( connection.getHostname() );
    connection.setHostname( safeHostname );

    String safePassword = StringEscapeUtils.unescapeHtml( connection.getPassword() );
    connection.setPassword( safePassword );

    String safeUsername = StringEscapeUtils.unescapeHtml( connection.getUsername() );
    connection.setUsername( safeUsername );
  }

  public String safeEscapeHtml( String html ) {
    return StringEscapeUtils.escapeHtml( StringEscapeUtils.unescapeHtml( html ) );
  }

  public String escape( String stringToEscape ) {
    return StringEscapeUtils.escapeHtml( stringToEscape );
  }

}
