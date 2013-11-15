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
  private DatasourceType datasourceType;
  private List<DatasourceChild> children = new ArrayList<DatasourceChild>();

  public Datasource() {
    super();
  }

  public Datasource( String name ) {
    this();
    this.name = name;
  }

  public Datasource( String name, DatasourceType datasourceType ) {
    this( name );
    this.datasourceType = datasourceType;
  }

  public Datasource( String name, DatasourceType datasourceType, List<DatasourceChild> children ) {
    this( name, datasourceType );
    this.children = children;
  }

  @Override
  public IDatasourceType getType() {
    return datasourceType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<IDatasourceChild> getChildren() {
    List<IDatasourceChild> ivalues = new ArrayList<IDatasourceChild>();
    for ( DatasourceChild child : children ) {
      ivalues.add( child );
    }
    return ivalues;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setType( DatasourceType datasourceType ) {
    this.datasourceType = datasourceType;
  }

  public void setChildren( List<DatasourceChild> children ) {
    this.children = children;
  }

}
