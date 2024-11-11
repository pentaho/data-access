/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource;

import java.io.Serializable;

/**
 * Basic information about a datasource object
 */
public interface IDatasourceInfo extends Serializable {
  /**
   * Returns a datasource name
   *
   * @return name
   */
  public String getName();

  /**
   * Returns a datasource id
   *
   * @return id
   */
  public String getId();

  /**
   * Returns a datasource type
   *
   * @return type
   */
  public String getType();

  /**
   * Returns whether a datasource is editable
   *
   * @return editable or not
   */
  public boolean isEditable();

  /**
   * Returns whether a datasource can be removed
   *
   * @return removable or not
   */
  public boolean isRemovable();

  /**
   * Returns whether a datasource can be imported or not
   *
   * @return importable or not
   */
  public boolean isImportable();

  /**
   * Returns whether a datasource can be exported or not
   *
   * @return exportable or not
   */
  public boolean isExportable();

  /**
   * Returns the name to display for this type
   *
   * @return
   */
  public String getDisplayType();

  /**
   * Returns whether a datasource can be created
   *
   * @return
   */
  public boolean isCreatable();
}
