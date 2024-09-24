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
public class LogicalModelSummary extends XulEventSourceAdapter
  implements Comparable<LogicalModelSummary>, Serializable {

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
   * @param domainId  domain id
   * @param modelId   model id
   * @param modelName localized model name
   */
  public LogicalModelSummary( final String domainId, final String modelId, final String modelName ) {
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
    buf.append( "LogicalModelSummary[" ).append( "domainId=" ).append( domainId ).append( ", " ).append( "modelId=" )
      .append( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        modelId ).append( ", " ).append( "modelName=" ).append( modelName )
      .append( "]" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    return buf.toString();
  }

  public int compareTo( final LogicalModelSummary other ) {
    if ( other == null ) {
      return 1;
    } else {
      return modelName.compareToIgnoreCase( other.modelName );
    }
  }

}
