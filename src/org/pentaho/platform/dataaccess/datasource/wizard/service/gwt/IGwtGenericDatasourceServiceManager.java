package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import org.pentaho.platform.api.datasource.IGenericDatasourceServiceManager;

import com.google.gwt.user.client.rpc.RemoteService;

public interface IGwtGenericDatasourceServiceManager extends IGenericDatasourceServiceManager, RemoteService{
  
  public Boolean isAdmin();  

}
