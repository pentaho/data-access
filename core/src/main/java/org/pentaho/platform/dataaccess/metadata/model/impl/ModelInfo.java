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

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Concrete, lightweight, serializable object that holds the envelope information of an {@see IModel} object
 *
 * @author jamesdixon
 */
@XmlRootElement
public class ModelInfo implements Serializable {

  private static final long serialVersionUID = -8341925508348437605L;

  private String domainId;

  private String modelId;

  private String modelName;

  private String modelDescription;

  /**
   * Returns the id of the domain of the model
   *
   * @return
   */
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
   * Returns the id of the model
   *
   * @return
   */
  public String getModelId() {
    return modelId;
  }

  /**
   * Sets the id of the model
   *
   * @param id
   */
  public void setModelId( String modelId ) {
    this.modelId = modelId;
  }

  /**
   * Returns the name of the model for the current locale
   *
   * @return
   */
  public String getModelName() {
    return modelName;
  }

  /**
   * Sets the name of the model for the current locale
   *
   * @param name
   */
  public void setModelName( String modelName ) {
    this.modelName = modelName;
  }

  /**
   * Returns the description of the model for the current locale
   *
   * @return
   */
  public String getModelDescription() {
    return modelDescription;
  }

  /**
   * Sets the description of the model
   *
   * @param description
   */
  public void setModelDescription( String modelDescription ) {
    this.modelDescription = modelDescription;
  }

}
