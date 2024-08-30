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
* Copyright (c) 2002-2024 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.metadata.model.impl;

import org.pentaho.platform.dataaccess.metadata.model.IModel;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Concrete, lightweight, serializable implementation of an {@see IModel} object
 *
 * @author jamesdixon
 */
@XmlRootElement
public class Model implements IModel {

  private static final long serialVersionUID = 6865069259179116876L;

  private Category[] categories = new Category[ 0 ];

  private String id, name, domainId, description;

  /**
   * Returns an array of categories for the model
   *
   * @return
   */
  @Override
  public Category[] getCategories() {
    return categories;
  }

  /**
   * Returns the id of the model
   *
   * @return
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Returns the name of the model for the current locale
   *
   * @return
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * Sets the categories for the model
   *
   * @param categories
   */
  public void setCategories( Category[] categories ) {
    this.categories = categories;
  }

  /**
   * Sets the id of the model
   *
   * @param id
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * Sets the name of the model for the current locale
   *
   * @param name
   */
  public void setName( String name ) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( categories == null ) ? 0 : categories.hashCode() );
    result = prime * result + ( ( id == null ) ? 0 : id.hashCode() );
    result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
    return result;
  }

  /**
   * Determines whether two models are equal to each other
   */
  @Override
  public boolean equals( Object obj ) {
    if ( this == obj ) {
      return true;
    }
    if ( obj == null ) {
      return false;
    }
    if ( getClass() != obj.getClass() ) {
      return false;
    }
    Model other = (Model) obj;
    if ( categories == null ) {
      if ( other.categories != null ) {
        return false;
      }
    } else if ( categories.length != other.categories.length ) {
      return false;
    } else {
      int idx = 0;
      for ( Category category : categories ) {
        if ( !category.equals( other.categories[ idx ] ) ) {
          return false;
        }
        idx++;
      }
    }
    if ( id == null ) {
      if ( other.id != null ) {
        return false;
      }
    } else if ( !id.equals( other.id ) ) {
      return false;
    }
    if ( name == null ) {
      if ( other.name != null ) {
        return false;
      }
    } else if ( !name.equals( other.name ) ) {
      return false;
    }
    return true;
  }

  /**
   * Returns the id of the domain of the model
   *
   * @return
   */
  @Override
  public String getDomainId() {
    return domainId;
  }

  /**
   * Sets the domain id of the model
   *
   * @param domainId
   */
  public void setDomainId( String domainId ) {
    this.domainId = domainId;
  }

  /**
   * Returns the description of the model for the current locale
   *
   * @return
   */
  @Override
  public String getDescription() {
    return description;
  }

  /**
   * Sets the description of the model
   *
   * @param description
   */
  public void setDescription( String description ) {
    this.description = description;
  }

}
