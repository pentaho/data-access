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
 * Copyright (c) 2021 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.utils;

import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

public class Base64PasswordUtils {

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
