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

/**
 * Represents a Metadata Model object containing one or more {@see ICategory}s
 */
public interface IModel extends Serializable {

  /**
   * Returns the id of the model
   *
   * @return
   */
  public String getId();

  /**
   * Returns the id of the domain of the model
   *
   * @return
   */
  public String getDomainId();

  /**
   * Returns the name of the model for the current locale
   *
   * @return
   */
  public String getName();

  /**
   * Returns an array of categories for the model
   *
   * @return
   */
  public ICategory[] getCategories();

  /**
   * Returns the description of the model for the current locale
   *
   * @return
   */
  public String getDescription();
}
