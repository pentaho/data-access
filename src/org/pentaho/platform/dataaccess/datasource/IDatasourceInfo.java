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
 * Copyright 2005 - 2008 Pentaho Corporation.  All rights reserved.
 *
 * @created Nov 12, 2011 
 * @author Ramaiz Mansoor
 */
package org.pentaho.platform.dataaccess.datasource;

import java.io.Serializable;

/**
 * Basic information about a datasource object
 * 
 *
 */
public interface IDatasourceInfo extends Serializable{
  /**
   * Returns a datasource name
   * @return name
   */
  public String getName();
  
  /**
   * Returns a datasource id
   * @return id
   */
  public String getId();

  /**
   * Returns a datasource type
   * @return type
   */
  public String getType();
  
  /**
   * Returns whether a datasource is editable
   * @return editable or not
   */
  public boolean isEditable();
  
  /**
   * Returns whether a datasource can be removed
   * @return removable or not
   */
  public boolean isRemovable();
  
  /**
   * Returns whether a datasource can be imported or not
   * @return importable or not
   */
  public boolean isImportable();
  
  /**
   * Returns whether a datasource can be exported or not
   * @return exportable or not
   */
  public boolean isExportable();

  /**
   * Returns the name to display for this type
   * @return
   */
  public String getDisplayType();
}