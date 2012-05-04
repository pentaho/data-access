package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class DatasourceServiceManagerDebugGwtServlet extends RemoteServiceServlet implements IGwtDatasourceServiceManager{

  @Override
  public Boolean isAdmin() {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public List<String> getAnalysisDatasourceIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getMetadataDatasourceIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getDSWDatasourceIds() {
    // TODO Auto-generated method stub
    return null;
  }

}
