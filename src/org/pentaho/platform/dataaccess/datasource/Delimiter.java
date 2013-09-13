/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created June 22, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource;

public enum Delimiter {
  COMMA("Delimiter.USER_COMMA_DESC", ","), //$NON-NLS-1$ //$NON-NLS-2$
  TAB("Delimiter.USER_TAB_DESC", "\t"), //$NON-NLS-1$ //$NON-NLS-2$
  SEMICOLON("Delimiter.USER_SEMI_COLON_DESC", ";"), //$NON-NLS-1$ //$NON-NLS-2$ 
  SPACE("Delimiter.USER_SPACE_DESC", " "), //$NON-NLS-1$ //$NON-NLS-2$
  OTHER("Delimiter.OTHER", ""); //$NON-NLS-1$ //$NON-NLS-2$

  private String name;

  private String value;

  Delimiter(String name, String value) {
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

  public static Delimiter lookupValue(String delim) {
    for (Delimiter delimObj : Delimiter.values()) {
      if (delimObj.getValue().equals(delim)) {
        return delimObj;
      }
    }
    return null;
  }
  
  public static Delimiter lookupName(String delim) {
    for (Delimiter delimObj : Delimiter.values()) {
      if (delimObj.getName().equals(delim)) {
        return delimObj;
      }
    }
    return null;
  }  
}
