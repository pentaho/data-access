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

/**
 *
 * Represents a Metadata Model object containing one or more {@see ICategory}s
 *
 */
public interface IModel extends Serializable{

  /**
   * Returns the id of the model
   * @return
   */
  public String getId();

  /**
   * Returns the id of the domain of the model
   * @return
   */
  public String getDomainId();
  
  /**
   * Returns the name of the model for the current locale
   * @return
   */
  public String getName();

  /**
   * Returns an array of categories for the model
   * @return
   */
  public ICategory[] getCategories();
 
  /**
   * Returns the description of the model for the current locale
   * @return
   */
  public String getDescription();
}
