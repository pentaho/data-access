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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.ui.service;


import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;

import com.google.gwt.core.client.JavaScriptObject;

public class JSDatasourceInfo implements IDatasourceInfo{
  private JavaScriptObject jsDatasourceInfo;
  
  public JSDatasourceInfo(JavaScriptObject jsDatasourceInfo) {
    this.jsDatasourceInfo = jsDatasourceInfo;
  }

  // JSNI methods to get datasource info data.
  private final native String getDatasourceName(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.name; }-*/;

  private final native String getDatasourceId(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.id; }-*/; 

  private final native String getDatasourceType(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.type; }-*/;

  private final native String getDatasourceDisplayType(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.displayType; }-*/;

  private final native boolean isDatasourceEditable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.editable; }-*/;
  
  private final native boolean isDatasourceRemovable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.removable; }-*/; 

  private final native boolean isDatasourceImportable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.importable; }-*/; 
  
  private final native boolean isDatasourceExportable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.exportable; }-*/;

  private final native boolean isDatasourceCreatable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.creatable; }-*/;

  
  @Override
  public final String getName() {
    return getDatasourceName(this.jsDatasourceInfo);
  }

  @Override
  public final String getId() {
    return getDatasourceId(this.jsDatasourceInfo);
  }

  @Override
  public final String getType() {
    return getDatasourceType(this.jsDatasourceInfo);
  }

  @Override
  public final boolean isEditable() {
    return isDatasourceEditable(this.jsDatasourceInfo);
  }

  @Override
  public final boolean isRemovable() {
    return isDatasourceRemovable(this.jsDatasourceInfo);
  }

  @Override
  public final boolean isImportable() {
    return isDatasourceImportable(this.jsDatasourceInfo);
  }

  @Override
  public final boolean isExportable() {
    return isDatasourceExportable(this.jsDatasourceInfo);
  }

  @Override
  public final boolean isCreatable() {
    return isDatasourceCreatable(this.jsDatasourceInfo);
  }

  @Override
  public String getDisplayType() {
    String type = null;
    try {
      type = getDatasourceDisplayType(this.jsDatasourceInfo);
    } catch (Exception e) {
      // no displayType defined, fall back to type
      type = getType();
    }
    return type;
  }

}
