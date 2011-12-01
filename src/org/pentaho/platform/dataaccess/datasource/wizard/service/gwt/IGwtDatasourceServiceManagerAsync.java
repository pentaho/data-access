package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.datasource.DatasourceInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtDatasourceServiceManagerAsync {

  void getAll(AsyncCallback<List<DatasourceInfo>> callback);
  
  void isAdmin(AsyncCallback<Boolean> callback);
}
