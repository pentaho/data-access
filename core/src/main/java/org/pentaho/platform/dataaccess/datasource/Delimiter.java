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


package org.pentaho.platform.dataaccess.datasource;

public enum Delimiter {
  COMMA( "Delimiter.USER_COMMA_DESC", "," ), //$NON-NLS-1$ //$NON-NLS-2$
  TAB( "Delimiter.USER_TAB_DESC", "\t" ), //$NON-NLS-1$ //$NON-NLS-2$
  SEMICOLON( "Delimiter.USER_SEMI_COLON_DESC", ";" ), //$NON-NLS-1$ //$NON-NLS-2$
  SPACE( "Delimiter.USER_SPACE_DESC", " " ), //$NON-NLS-1$ //$NON-NLS-2$
  OTHER( "Delimiter.OTHER", "" ); //$NON-NLS-1$ //$NON-NLS-2$

  private String name;

  private String value;

  Delimiter( String name, String value ) {
    this.name = name;
    this.value = value;
  }

  public String toString() {
    return name;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public static Delimiter lookupValue( String delim ) {
    for ( Delimiter delimObj : Delimiter.values() ) {
      if ( delimObj.getValue().equals( delim ) ) {
        return delimObj;
      }
    }
    return null;
  }

  public static Delimiter lookupName( String delim ) {
    for ( Delimiter delimObj : Delimiter.values() ) {
      if ( delimObj.getName().equals( delim ) ) {
        return delimObj;
      }
    }
    return null;
  }
}
