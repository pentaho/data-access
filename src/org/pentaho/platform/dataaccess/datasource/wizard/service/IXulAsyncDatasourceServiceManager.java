package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.datasource.Datasource;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncDatasourceServiceManager {

  void getAll(XulServiceCallback<List<IDatasourceInfo>> callback);
  void getTypes(XulServiceCallback<List<String>> callback);
  void getNewUI(String datasourceType, XulServiceCallback<String> callback);
  void getEditUI(String datasourceType, String datasourceName, XulServiceCallback<String> callback);
  void isAdmin(XulServiceCallback<Boolean> callback);
  void add(Datasource datasource, boolean overwrite, XulServiceCallback<String> callback);
  void remove(String id, XulServiceCallback<String> callback);
  void update(Datasource datasource, XulServiceCallback<String> callback);
}
