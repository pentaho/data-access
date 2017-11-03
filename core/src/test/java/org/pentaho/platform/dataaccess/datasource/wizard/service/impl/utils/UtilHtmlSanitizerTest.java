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

import org.junit.Before;
import org.junit.Test;
import org.pentaho.database.model.DatabaseConnection;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;

/**
 * Created by Yury_Bakhmutski on 8/11/2016.
 */
public class UtilHtmlSanitizerTest {

  UtilHtmlSanitizer sanitizer;

  @Before
  public void setUp() {
    sanitizer = UtilHtmlSanitizer.getInstance();
  }

  @Test
  public void testSanitizeConnectionParameters() throws Exception {
    DatabaseConnection connection = new DatabaseConnection();

    //see http://jira.pentaho.com/browse/PPP-3546
    connection.setName( "<font color=\"red\">\"AAAAAAAAAAAA\"" );
    sanitizer.sanitizeConnectionParameters( connection );
    assertEquals( "&lt;font color=&quot;red&quot;&gt;&quot;AAAAAAAAAAAA&quot;", connection.getName() );
    connection.setName( "<input type=\"button\" onClick=\"dangerous code\">" );
    sanitizer.sanitizeConnectionParameters( connection );
    assertEquals( "&lt;input type=&quot;button&quot; onClick=&quot;dangerous code&quot;&gt;", connection.getName() );
    connection.setName( "<a href=\"url\">link text" );
    sanitizer.sanitizeConnectionParameters( connection );
    assertEquals( "&lt;a href=&quot;url&quot;&gt;link text", connection.getName() );

    //check that null is not transformed to an empty string
    assertNull( connection.getDatabaseName() );
  }

  @Test
  public void testUnsanitizeConnectionParameters() throws Exception {
    DatabaseConnection connection = new DatabaseConnection();

    connection.setName( "<font color=\"red\">\"AAAAAAAAAAAA\"" );
    assertEquals( "<font color=\"red\">\"AAAAAAAAAAAA\"", connection.getName() );
    sanitizer.sanitizeConnectionParameters( connection );
    assertEquals( "&lt;font color=&quot;red&quot;&gt;&quot;AAAAAAAAAAAA&quot;", connection.getName() );
    sanitizer.unsanitizeConnectionParameters( connection );
    assertEquals( "<font color=\"red\">\"AAAAAAAAAAAA\"", connection.getName() );

    assertNull( connection.getDatabaseName() );
  }

  @Test
  public void testSafeEscapeHtml() {
    String unsanitizedName = "<font color=\"red\">\"AAAAAAAAAAAA\"";
    String sanitizedName = "&lt;font color=&quot;red&quot;&gt;&quot;AAAAAAAAAAAA&quot;";

    assertEquals( sanitizedName, sanitizer.safeEscapeHtml( unsanitizedName ) );
    assertEquals( sanitizedName, sanitizer.safeEscapeHtml( sanitizedName ) );
  }
}

