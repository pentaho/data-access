package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface IGwtDatasourceServiceManager extends RemoteService{
  
  public Boolean isAdmin();  
  public List<String> getAnalysisDatasourceIds();
  public List<String> getMetadataDatasourceIds();
  public List<String> getDSWDatasourceIds();
}
