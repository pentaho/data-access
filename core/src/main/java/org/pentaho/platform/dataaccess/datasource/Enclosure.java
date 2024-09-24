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

public enum Enclosure {
  SINGLEQUOTE( "Enclosure.USER_SINGLE_QUOTE", "'" ), //$NON-NLS-1$  //$NON-NLS-2$
  DOUBLEQUOTE( "Enclosure.USER_DOUBLE_QUOTE", "\"" ), //$NON-NLS-1$ //$NON-NLS-2$
  NONE( "Enclosure.USER_NONE", "" ); //$NON-NLS-1$ //$NON-NLS-2$

  private String name;
  private String value;

  Enclosure( String name, String value ) {
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

  public static Enclosure lookupValue( String encl ) {
    for ( Enclosure enclObj : Enclosure.values() ) {
      if ( enclObj.getValue().equals( encl ) ) {
        return enclObj;
      }
    }
    return null;
  }

  public static Enclosure lookupName( String encl ) {
    for ( Enclosure enclObj : Enclosure.values() ) {
      if ( enclObj.getName().equals( encl ) ) {
        return enclObj;
      }
    }
    return null;
  }
}
