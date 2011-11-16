package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class GenericDatasourceServiceManagerDebugGwtServlet extends RemoteServiceServlet implements IGwtGenericDatasourceServiceManager{

  @Override
  public void registerService(IGenericDatasourceService service) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public IGenericDatasourceService getService(String serviceType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getTypes() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IGenericDatasource> getAll() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Boolean isAdmin() {
    // TODO Auto-generated method stub
    return true;
  }

}
