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

package org.pentaho.platform.dataaccess.datasource.ui.service;


import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;

import com.google.gwt.core.client.JavaScriptObject;

public class JSDatasourceInfo implements IDatasourceInfo {
  private JavaScriptObject jsDatasourceInfo;

  public JSDatasourceInfo( JavaScriptObject jsDatasourceInfo ) {
    this.jsDatasourceInfo = jsDatasourceInfo;
  }

  // JSNI methods to get datasource info data.
  private final native String getDatasourceName( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.name;
  }-*/;

  private final native String getDatasourceId( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.id;
  }-*/;

  private final native String getDatasourceType( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.type;
  }-*/;

  private final native String getDatasourceDisplayType( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.displayType;
  }-*/;

  private final native boolean isDatasourceEditable( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.editable;
  }-*/;

  private final native boolean isDatasourceRemovable( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.removable;
  }-*/;

  private final native boolean isDatasourceImportable( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.importable;
  }-*/;

  private final native boolean isDatasourceExportable( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.exportable;
  }-*/;

  private final native boolean isDatasourceCreatable( JavaScriptObject jsDatasourceInfo ) /*-{
    return jsDatasourceInfo.creatable;
  }-*/;


  @Override
  public final String getName() {
    return getDatasourceName( this.jsDatasourceInfo );
  }

  @Override
  public final String getId() {
    return getDatasourceId( this.jsDatasourceInfo );
  }

  @Override
  public final String getType() {
    return getDatasourceType( this.jsDatasourceInfo );
  }

  @Override
  public final boolean isEditable() {
    return isDatasourceEditable( this.jsDatasourceInfo );
  }

  @Override
  public final boolean isRemovable() {
    return isDatasourceRemovable( this.jsDatasourceInfo );
  }

  @Override
  public final boolean isImportable() {
    return isDatasourceImportable( this.jsDatasourceInfo );
  }

  @Override
  public final boolean isExportable() {
    return isDatasourceExportable( this.jsDatasourceInfo );
  }

  @Override
  public final boolean isCreatable() {
    return isDatasourceCreatable( this.jsDatasourceInfo );
  }

  @Override
  public String getDisplayType() {
    String type = null;
    try {
      type = getDatasourceDisplayType( this.jsDatasourceInfo );
    } catch ( Exception e ) {
      // no displayType defined, fall back to type
      type = getType();
    }
    return type;
  }

}
