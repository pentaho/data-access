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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceChild;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;

/**
 * Provides a concrete Implementation of IDatasource suitable for serialization using Jersey.
 * 
 * @author wseyler
 */
@XmlRootElement
public class Datasource implements IDatasource {

  private String name;
  private IDatasourceType datasourceType;
  @XmlAnyElement
  private List<IDatasourceChild> children = new ArrayList<IDatasourceChild>();

  public Datasource() {
    super();
  }

  public Datasource( String name ) {
    this();
    this.name = name;
  }

  public Datasource( String name, IDatasourceType datasourceType ) {
    this( name );
    this.datasourceType = datasourceType;
  }

  public Datasource( String name, IDatasourceType datasourceType, List<IDatasourceChild> children ) {
    this( name, datasourceType );
    this.children = children;
  }

  @Override
  @XmlAnyElement
  public IDatasourceType getType() {
    return datasourceType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<IDatasourceChild> getChildren() {
    return children;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setType( IDatasourceType datasourceType ) {
    this.datasourceType = datasourceType;
  }

  public void setChildren( List<IDatasourceChild> children ) {
    this.children = children;
  }

}
