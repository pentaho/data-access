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


package org.pentaho.platform.dataaccess.datasource.utils;

import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class Base64PasswordUtils {

  private Base64PasswordUtils() { }

  public static String decodePassword( String encodedPassword ) {
    if ( !StringUtils.isEmpty( encodedPassword ) && encodedPassword.startsWith( "ENC:" ) ) {
      return new String( Base64Utils.decodeFromString( encodedPassword.substring( 4 ) ), StandardCharsets.UTF_8 );
    } else {
      return encodedPassword;
    }
  }

  public static String encodePassword( String password ) {
    return Base64Utils.encodeToString(
      password.getBytes( StandardCharsets.UTF_8 ) );
  }

}
