package org.pentaho.platform.dataaccess.datasource;


import com.google.gwt.core.client.JavaScriptObject;

public class JSDatasourceInfo extends JavaScriptObject implements IDatasourceInfo{

  // Overlay types always have protected, zero argument constructors.
  protected JSDatasourceInfo() {
  }
  // JSNI methods to get datasource info data.
  public final native String getDatasourceName() /*-{ return this.name; }-*/;

  public final native String getDatasourceId() /*-{ return this.id; }-*/; //

  public final native String getDatasourceType() /*-{ return this.type; }-*/; 

  private final native boolean isDatasourceEditable() /*-{ return this.editable; }-*/; //
  
  private final native boolean isDatasourceRemovable() /*-{ return this.removable; }-*/; //

  private final native boolean isDatasourceImportable() /*-{ return this.importable; }-*/; //
  
  private final native boolean isDatasourceExportable() /*-{ return this.exportable; }-*/; //

  
  @Override
  public final String getName() {
    return getDatasourceName();
  }

  @Override
  public final String getId() {
    return getDatasourceId();
  }

  @Override
  public final String getType() {
    return getDatasourceType();
  }

  @Override
  public final boolean isEditable() {
    return isEditable();
  }

  @Override
  public final boolean isRemovable() {
    return isRemovable();
  }

  @Override
  public final boolean isImportable() {
    return isDatasourceImportable();
  }

  @Override
  public final boolean isExportable() {
    return isDatasourceExportable();
  }

}
