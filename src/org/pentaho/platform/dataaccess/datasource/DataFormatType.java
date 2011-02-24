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
 * Created May 5, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource;

import java.io.Serializable;

/**
 * DataFormatType represents the data format type
 *
 */
public enum DataFormatType implements Serializable{
  CURRENCY("$XXX,XXX.XX"), MMDDYYYY("MM-DD-YYYY"), DDMMYYYY("DD-MM-YYYY"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$

  private String formatString;
  DataFormatType(String formatString) {
    this.formatString = formatString;
  }

  public String toString() {
    return formatString;
  }
}