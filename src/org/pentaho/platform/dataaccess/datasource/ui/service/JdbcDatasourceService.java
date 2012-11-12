package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.ui.xul.XulServiceCallback;

public class JdbcDatasourceService implements IUIDatasourceAdminService{
  
  public static final String TYPE = "JDBC";
  private boolean editable = false;
  private boolean removable = true;
  private boolean importable = true;
  private boolean exportable = true;
  private String newUI = "builtin:";
  private String editUI = "builtin:";
  private IXulAsyncConnectionService connectionService;
  

  public JdbcDatasourceService(IXulAsyncConnectionService connectionService) {
    this.connectionService = connectionService;
  }
  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void getIds(final XulServiceCallback<List<IDatasourceInfo>> callback) {
    connectionService.getConnections(new XulServiceCallback<List<Connection>>() {
      
      @Override
      public void success(List<Connection> connections) {
        List<IDatasourceInfo> datasourceInfos = new ArrayList<IDatasourceInfo>();
        for(Connection connection:connections) {
          datasourceInfos.add(new DatasourceInfo(connection.getName(), connection.getName(), TYPE, editable, removable, importable, exportable));
        }
        callback.success(datasourceInfos);
      }
      
      @Override
      public void error(String message, Throwable error) {
        callback.error(message, error);
        
      }
    });
  }

  @Override
  public String getNewUI() {
    return newUI;
  }

  @Override
  public String getEditUI(IDatasourceInfo dsInfo) {
    return editUI;
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#export(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void export(IDatasourceInfo dsInfo) {
    // TODO Auto-generated method stub
    
  }
  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#remove(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void remove(IDatasourceInfo dsInfo, XulServiceCallback<Boolean> callback) {
    connectionService.deleteConnection(dsInfo.getName(), callback);
  }

}
