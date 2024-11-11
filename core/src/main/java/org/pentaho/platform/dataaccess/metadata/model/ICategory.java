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


package org.pentaho.platform.dataaccess.metadata.model;

import java.io.Serializable;

import org.pentaho.platform.dataaccess.metadata.model.impl.Column;


/**
 * Represents a Metadata Category containing a collection of {@see IColumn}s A category is a logical grouping of columns
 * that makes it easier for the user to understand the model. It does not have to mirror the underlying physical model.
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
