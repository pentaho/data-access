/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.metadata.model.impl;

import org.pentaho.platform.dataaccess.metadata.model.IColumn;

/**
 * Concrete, lightweight, serializable implementation of an {@see IColumn} object
 *
 * @author jamesdixon
 */
public class Column implements IColumn {

  private static final long serialVersionUID = 3751750093446278893L;
  private String id, name, description;
  private String type;
  private String[] aggTypes = new String[ 0 ];
  private String defaultAggType;
  private String selectedAggType;
  private String fieldType;
  private String category;
  private String getHorizontalAlignment;
  private String formatMask;
  private boolean hiddenForUser;

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
    return aggTypes;
  }

  public void setAggTypes( String[] aggTypes ) {
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

  public void setDescription( String description ) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  public void setHiddenForUser( boolean hiddenForUser ) {
    this.hiddenForUser = hiddenForUser;
  }

  @Override
  public boolean isHiddenForUser() {
    return hiddenForUser;
  }
}
