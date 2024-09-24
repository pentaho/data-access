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

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Vadim_Polynkov
 */
public class DataSourceInfoUtil {

  public static String escapeQuotes( final String text ) {
    return text.replaceAll( "\"", "&quot;" );
  }

  public static String unescapeQuotes( final String text ) {
    return text.replaceAll( "&quot;", "\"" );
  }

  public static Map<String, String> parseDataSourceInfo( final String text ) {
    Map<String, String> parameters = new LinkedHashMap<String, String>();

    StringBuilder name = new StringBuilder();
    StringBuilder value = new StringBuilder();
    int state = 0;
    char ch;
    int i, len = text.length();
    for ( i = 0; i < len; i++ ) {
      ch = text.charAt( i );
      switch ( state ) {
        case 0: //new name/value pair
          if ( name.length() != 0 ) {
            parameters.put( name.toString(), value.toString() );
            name.setLength( 0 );
            value.setLength( 0 );
          }
          switch ( ch ) {
            case ';':
              break;
            default:
              state = 1;
              name.append( ch );
          }
          break;
        case 1: //looking for equals
          switch ( ch ) {
            case '=':
              state = 2;
              break;
            default:
              name.append( ch );
          }
          break;
        case 2: //about to parse the value
          switch ( ch ) {
            case '"':
              state = 3;
              break;
            case ';':
              state = 0;
              break;
            default:
              value.append( ch );
              state = 4;
          }
          break;
        case 3: //parse value till closing quote.
          switch ( ch ) {
            case '"':
              state = 0;
              break;
            default:
              value.append( ch );
          }
          break;
        case 4:
          switch ( ch ) {
            case ';':
              state = 0;
              break;
            default:
              value.append( ch );
          }
          break;
        default:

      }
    }
    if ( name.length() != 0 ) {
      parameters.put( name.toString(), value.toString() );
    }
    return parameters;
  }

}
