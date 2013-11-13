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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.catalog.impl;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.dataaccess.catalog.api.IDatasourceChild;

/**
 * Provides the structure for a hierarchy of children that belong to a datasource.
 * 
 * @author wseyler
 */
@XmlRootElement
public class DatasourceChild implements IDatasourceChild {

  private String id;
  private String name;
  @XmlAnyElement
  private List<IDatasourceChild> children;

  public DatasourceChild() {
    super();
  }

  public DatasourceChild( String id ) {
    this();
    this.id = id;
  }

  public DatasourceChild( String id, String name ) {
    this( id );
    this.name = name;
  }

  public DatasourceChild( String id, String name, List<IDatasourceChild> children ) {
    this( id, name );
    this.children = children;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceChild#getId()
   */
  @Override
  public String getId() {
    return id;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceChild#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.platform.dataaccess.catalog.api.IDatasourceChild#getChildren()
   */
  @Override
  public List<IDatasourceChild> getChildren() {
    return children;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setChildren( List<IDatasourceChild> children ) {
    this.children = children;
  }

}
