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
* Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import org.apache.commons.lang.StringEscapeUtils;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;

/**
 * Created by Yury_Bakhmutski on 8/11/2016.
 */
public class UtilHtmlSanitizer {

  public void sanitizeConnectionParameters( DatabaseConnection connection ) {
    String safeName = StringEscapeUtils.escapeHtml( connection.getName() );
    connection.setName( safeName );

    String safeDbName = StringEscapeUtils.escapeHtml( connection.getDatabaseName() );
    connection.setDatabaseName( safeDbName );

    String safeDbPort = StringEscapeUtils.escapeHtml( connection.getDatabasePort() );
    connection.setDatabasePort( safeDbPort );

    String safeHostname = StringEscapeUtils.escapeHtml( connection.getHostname() );
    connection.setHostname( safeHostname );

    String safePassword = StringEscapeUtils.escapeHtml( connection.getPassword() );
    connection.setPassword( safePassword );

    String safeUsername = StringEscapeUtils.escapeHtml( connection.getUsername() );
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

}
