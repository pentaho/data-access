package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncDatasourceServiceManager {

  void getAll(XulServiceCallback<List<IDatasourceInfo>> callback);
  void isAdmin(XulServiceCallback<Boolean> callback);
}
