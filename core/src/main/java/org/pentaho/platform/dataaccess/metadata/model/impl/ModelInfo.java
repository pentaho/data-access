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
