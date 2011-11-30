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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created July 10, 2009
 * @author mlowery
 */
package org.pentaho.platform.dataaccess.datasource.beans;

import java.io.Serializable;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

/**
 * A summary of a logical model consisting of domain id, model id, and localized model name. This thin object is for UI
 * purposes only.
 * 
 * @author mlowery
 */
public class LogicalModelSummary extends XulEventSourceAdapter implements Comparable<LogicalModelSummary>, Serializable {

  private static final long serialVersionUID = -2876155341724009295L;

  /**
   * The id of the domain to which this model belongs.
   */
  private String domainId;

  /**
   * The unique id of the model.
   */
  private String modelId;

  /**
   * The localized name of the model.
   */
  private String modelName;

  /**
   * Constructor. Required by GWT.
   */
  public LogicalModelSummary() {
    super();
  }
  
  /**
   * Constructor.
   * 
   * @param domainId
   *          domain id
   * @param modelId
   *          model id
   * @param modelName
   *          localized model name
   */
  public LogicalModelSummary(final String domainId, final String modelId, final String modelName) {
    super();
    this.domainId = domainId;
    this.modelId = modelId;
    this.modelName = modelName;
  }

  public String getDomainId() {
    return domainId;
  }

  public String getModelId() {
    return modelId;
  }

  @Bindable
  public String getModelName() {
    return modelName;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("LogicalModelSummary[").append("domainId=").append(domainId).append(", ").append("modelId=").append( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        modelId).append(", ").append("modelName=").append(modelName).append("]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return buf.toString();
  }

  public int compareTo(final LogicalModelSummary other) {
    if (other == null) {
      return 1;
    } else {
      return modelName.compareToIgnoreCase(other.modelName);
    }
  }

}
