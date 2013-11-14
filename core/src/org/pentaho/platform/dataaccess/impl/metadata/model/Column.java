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

package org.pentaho.platform.dataaccess.impl.metadata.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.platform.dataaccess.api.metadata.model.IColumn;

/**
 * Concrete, lightweight, serializable implementation of an {@see IColumn} object
 * 
 * @author jamesdixon
 * 
 */
@XmlRootElement
public class Column implements IColumn {

  private static final long serialVersionUID = 3751750093446278893L;
  private String id, name;
  private String type;
  private List<String> aggTypes = new ArrayList<String>();
  private String defaultAggType;
  private String selectedAggType;
  private String fieldType;
  private String category;
  private String getHorizontalAlignment;
  private String formatMask;

  @Override
  public String getHorizontalAlignment() {
    return getHorizontalAlignment;
  }

  public void setHorizontalAlignment( String getHorizontalAlignment ) {
    this.getHorizontalAlignment = getHorizontalAlignment;
  }

  @Override
  public String getFormatMask() {
    return formatMask;
  }

  public void setFormatMask( String formatMask ) {
    this.formatMask = formatMask;
  }

  @Override
  public String getCategory() {
    return category;
  }

  public void setCategory( String category ) {
    this.category = category;
  }

  @Override
  public String getFieldType() {
    return fieldType;
  }

  public void setFieldType( String fieldType ) {
    this.fieldType = fieldType;
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getType() {
    return this.type;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public void setName( String name ) {
    this.name = name;
  }

  public void setType( String type ) {
    this.type = type;
  }

  @Override
  public String getDefaultAggType() {
    return defaultAggType;
  }

  @Override
  public String[] getAggTypes() {
    return aggTypes.toArray( new String[aggTypes.size()] );
  }

  public void setAggTypes( List<String> aggTypes ) {
    this.aggTypes = aggTypes;
  }

  public void setDefaultAggType( String defaultAggType ) {
    this.defaultAggType = defaultAggType;
  }

  public void setSelectedAggType( String aggType ) {
    this.selectedAggType = aggType;
  }

  @Override
  public String getSelectedAggType() {
    return this.selectedAggType;
  }

}
