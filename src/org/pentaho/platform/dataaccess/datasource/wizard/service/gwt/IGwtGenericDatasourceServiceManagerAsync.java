package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.datasource.GenericDatasourceInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtGenericDatasourceServiceManagerAsync {

  void getAll(AsyncCallback<List<GenericDatasourceInfo>> callback);
  
  void isAdmin(AsyncCallback<Boolean> callback);
}
