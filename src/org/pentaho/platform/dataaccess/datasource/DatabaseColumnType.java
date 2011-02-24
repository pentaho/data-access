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
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource;

import java.io.Serializable;

/**
 * DatabaseColumnType represents the data type
 *
 */
public enum DatabaseColumnType implements Serializable{
  BOOLEAN(16, "BOOLEAN"), DATE(91, "DATE"), DECIMAL(3, "DECIMAL"), DOUBLE(8, "DOUBLE"), INTEGER(4, "INTEGER"), NUMERIC(2, "NUMERIC"), TIMESTAMP(93, "TIMESTAMP") , VARCHAR(12, "VARCHAR") ; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$

  private final int colType;
  private String name;
  DatabaseColumnType(int colType, String name) {
    this.colType = colType;
    this.name = name;
  }

  public String toString() {
    return name;
  }
  
  public int getType() {
    return colType;
  }
}