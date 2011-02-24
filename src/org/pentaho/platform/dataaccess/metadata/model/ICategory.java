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
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved.
 * 
 * Created Jan, 2011
 * @author jdixon
*/
package org.pentaho.platform.dataaccess.metadata.model;

import java.io.Serializable;

import org.pentaho.platform.dataaccess.metadata.model.impl.Column;


/**
 * Represents a Metadata Category containing a collection of {@see IColumn}s
 * A category is a logical grouping of columns that makes it easier for the user
 * to understand the model. It does not have to mirror the underlying physical model.
 *
 */
public interface ICategory extends Serializable {

  /**
   * Returns the id of the category
   */
  public String getId();

  /**
   * Returns the name of the cateogry for the current locale
   */
  public String getName();

  /**
   * Returns the array of {@see IColumn}s for this category
   */
  public Column[] getColumns();
  
}
