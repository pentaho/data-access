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

  private final native boolean isDatasourceEditable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.editable; }-*/; 
  
  private final native boolean isDatasourceRemovable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.removable; }-*/; 

  private final native boolean isDatasourceImportable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.importable; }-*/; 
  
  private final native boolean isDatasourceExportable(JavaScriptObject jsDatasourceInfo) /*-{ return jsDatasourceInfo.exportable; }-*/; 

  
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

}
