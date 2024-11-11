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

