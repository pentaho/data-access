package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class JSUIDatasourceService extends JavaScriptObject implements IUIDatasourceAdminService {

  // Overlay types always have protected, zero argument constructors.
  protected JSUIDatasourceService() {
  }
  @Override
  public final String getType() {
    return getDelegateType();
  }

  @Override
  public final void getIds(XulServiceCallback<List<IDatasourceInfo>> callback) {
    List<IDatasourceInfo> datasourceInfoList = new ArrayList<IDatasourceInfo>();
    JsArray<JSDatasourceInfo> infos = getDelegateIds();
    for(int i=0;i<infos.length();i++) {
      JSDatasourceInfo info = infos.get(i);
      datasourceInfoList.add(new DatasourceInfo(info.getName(), info.getName(), info.getType(), info.isEditable(), info.isRemovable(), info.isImportable(), info.isExportable()));
    }
    callback.success(datasourceInfoList);
  }

  @Override
  public final String getNewUI() {
    return getDelegateNewUI();
  }

  @Override
  public final String getEditUI() {
    return getDelegateEditUI();
  }

  private final native JsArray<JSDatasourceInfo> getDelegateIds() /*-{
    return this.getIds();
  }-*/;
  private final native String getDelegateNewUI() /*-{
    return this.getNewUI();
  }-*/;

  private final native String getDelegateEditUI() /*-{
    return this.getEditUI();
  }-*/;
  
  private final native String getDelegateType() /*-{
    return this.type;
  }-*/;
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#export(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void export(IDatasourceInfo dsInfo) {
    // TODO Auto-generated method stub
    
  }
}
