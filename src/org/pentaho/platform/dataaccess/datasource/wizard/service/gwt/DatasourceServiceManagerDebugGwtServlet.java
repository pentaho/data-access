package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.api.datasource.IDatasource;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DatasourceServiceManagerDebugGwtServlet extends RemoteServiceServlet implements IGwtDatasourceServiceManager{

  @Override
  public void registerService(IDatasourceService service) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public IDatasourceService getService(String serviceType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IDatasourceInfo> getIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Boolean isAdmin() {
    // TODO Auto-generated method stub
    return true;
  }

}
