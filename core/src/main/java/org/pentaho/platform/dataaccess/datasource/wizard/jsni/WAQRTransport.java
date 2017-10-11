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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

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
