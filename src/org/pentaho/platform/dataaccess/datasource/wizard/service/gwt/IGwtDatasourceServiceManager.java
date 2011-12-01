package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import org.pentaho.platform.api.datasource.IDatasourceServiceManager;

import com.google.gwt.user.client.rpc.RemoteService;

public interface IGwtDatasourceServiceManager extends IDatasourceServiceManager, RemoteService{
  
  public Boolean isAdmin();  

}
