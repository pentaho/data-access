package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.http.client.RequestCallback;

public class JSUIDatasourceService implements IUIDatasourceAdminService {
  private JavaScriptObject datasourceServiceObject;
  // Overlay types always have protected, zero argument constructors.
  public JSUIDatasourceService(JavaScriptObject datasourceServiceObject) {
    this.datasourceServiceObject = datasourceServiceObject;
  }
  
  @Override
  public final String getType() {
    return getDelegateType(this.datasourceServiceObject);
  }

  @Override
  public final void getIds(XulServiceCallback<List<IDatasourceInfo>> callback) {
    List<IDatasourceInfo> datasourceInfoList = new ArrayList<IDatasourceInfo>();
    JsArray<JavaScriptObject> infos = getDelegateIds(this.datasourceServiceObject);
    for(int i=0;i<infos.length();i++) {
      JSDatasourceInfo info = new JSDatasourceInfo(infos.get(i));
      datasourceInfoList.add(new DatasourceInfo(info.getName(), info.getName(), info.getType(), info.isEditable(), info.isRemovable(), info.isImportable(), info.isExportable()));
    }
    callback.success(datasourceInfoList);
  }

  @Override
  public final String getNewUI() {
    return getDelegateNewUI(this.datasourceServiceObject);
  }

  @Override
  public final String getEditUI(IDatasourceInfo dsInfo) {
    return getDelegateEditUI(this.datasourceServiceObject, dsInfo.getId());
  }
  
  @Override
  public final void export(IDatasourceInfo dsInfo) {
    getDelegateExport(this.datasourceServiceObject, dsInfo.getId());
    
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#remove(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo, java.lang.Object)
   */
  @Override
  public void remove(IDatasourceInfo dsInfo, Object callback) {
    XulServiceCallbackJavascriptObject xulServiceCallbackJsObject = new XulServiceCallbackJavascriptObject((XulServiceCallback<Boolean>) callback);
    getDelegateRemove(this.datasourceServiceObject, dsInfo.getId(), xulServiceCallbackJsObject.getJavascriptObject());
  }


  private final native JsArray<JavaScriptObject> getDelegateIds(JavaScriptObject datasourceServiceObject) /*-{
    return datasourceServiceObject.getIds();
  }-*/;
  private final native String getDelegateNewUI(JavaScriptObject datasourceServiceObject) /*-{
    return datasourceServiceObject.getNewUI();
  }-*/;

  private final native String getDelegateEditUI(JavaScriptObject datasourceServiceObject, String id) /*-{
    return datasourceServiceObject.getEditUI(id);
  }-*/;
  
  private final native String getDelegateType(JavaScriptObject datasourceServiceObject) /*-{
    return datasourceServiceObject.getType();
  }-*/;
  
  private final native String getDelegateExport(JavaScriptObject datasourceServiceObject, String id) /*-{
    return datasourceServiceObject.doExport(id);
  }-*/;

  private final native String getDelegateRemove(JavaScriptObject datasourceServiceObject, String id, JavaScriptObject callback) /*-{
    return datasourceServiceObject.doRemove(id, callback);
  }-*/;
}
