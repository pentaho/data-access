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


package org.pentaho.platform.dataaccess.datasource.wizard.jsni;

import java.util.Iterator;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * This class makes available the created business model to the calling javascript.
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
public class WAQRTransport extends JavaScriptObject {

  public static WAQRTransport createFromMetadata( Domain domain ) {

    // this assumes a single logical model with a single logical category

    LogicalModel model = domain.getLogicalModels().get( 0 );
    Iterator<String> iter = model.getName().getLocales().iterator();
    String locale = iter.next();
    Category category = model.getCategories().get( 0 );

    String domainId = domain.getId();
    String modelId = model.getId();
    String modelName = model.getName() != null ? model.getName().getString( locale ) : null;
    String categoryId = category.getId();
    String categoryName = category.getName() != null ? category.getName().getString( locale ) : null;
    String schemaName = model.getName( locale );

    return createDomain( domainId, modelId, modelName, categoryId, categoryName, schemaName );
  }

  private static native WAQRTransport createDomain( String domainId, String modelId, String modelName,
                                                    String categoryId, String categoryName, String schemaName ) /*-{
    var waqrTransport = {};
    waqrTransport.domainId = domainId;
    waqrTransport.modelId = modelId;
    waqrTransport.modelName = modelName;
    waqrTransport.categoryId = categoryId;
    waqrTransport.categoryName = categoryName;
    waqrTransport.schemaName = schemaName;
    return waqrTransport;
  }-*/;

  protected WAQRTransport() {

  }
}
