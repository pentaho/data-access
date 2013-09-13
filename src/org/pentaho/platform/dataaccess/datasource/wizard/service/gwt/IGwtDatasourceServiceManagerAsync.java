package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtDatasourceServiceManagerAsync {

  void getAnalysisDatasourceIds(AsyncCallback<List<String>> callback);
  void getMetadataDatasourceIds(AsyncCallback<List<String>> callback);
  void getDSWDatasourceIds(AsyncCallback<List<String>> callback);
  void isAdmin(AsyncCallback<Boolean> callback);
}
