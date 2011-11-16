package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncGenericDatasourceServiceManager {

  void getAll(XulServiceCallback<List<IGenericDatasourceInfo>> callback);
  void isAdmin(XulServiceCallback<Boolean> callback);
}
