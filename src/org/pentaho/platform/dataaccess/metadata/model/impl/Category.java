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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.metadata.model.impl;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.dataaccess.metadata.model.ICategory;

/**
 * Concrete, lightweight, serializable implementation of an {@see ICategory} object
 * 
 * @author jamesdixon
 * 
 */
@XmlRootElement
public class Category implements ICategory, Serializable {

  private static final long serialVersionUID = -454688567483551796L;
  private String id, name;
  private Column[] columns = new Column[0];

  /**
   * Returns the id of the category
   */
  @Override
  public String getId() {
    return this.id;
  }

  /**
   * Returns the name of the cateogry for the current locale
   */
  @Override
  public String getName() {
    return this.name;
  }

  /**
   * Returns the id of the category
   * 
   * @param id
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * Sets the name of the category
   * 
   * @param name
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * Returns the array of {@see IColumn}s for this category
   */
  @Override
  public Column[] getColumns() {
    return columns;
  }

  /**
   * Sets the array of {@see IColumn}s for this category
   * 
   * @param columns
   */
  public void setColumns( Column columns[] ) {
    this.columns = columns;
  }

}
