package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;

public class DSWUIDatasourceService implements IUIDatasourceAdminService {
  private IXulAsyncDatasourceServiceManager datasourceService;

  public final static String EXT = ".xmi"; //$NON-NLS-1$

  public final static String TYPE = "Data Source Wizard"; //$NON-NLS-1$

  private boolean editable = true;

  private boolean removable = true;

  private boolean importable = false;

  private boolean exportable = true;

  private String newUI = "builtin:"; //$NON-NLS-1$

  private String editUI = "builtin:"; //$NON-NLS-1$

  @Override
  public String getType() {
    return TYPE;
  }

  public DSWUIDatasourceService(IXulAsyncDatasourceServiceManager datasourceService) {
    this.datasourceService = datasourceService;
  }

  @Override
  public void getIds(final XulServiceCallback<List<IDatasourceInfo>> callback) {
    datasourceService.getDSWDatasourceIds(new XulServiceCallback<List<String>>() {

      @Override
      public void success(List<String> ids) {
        List<IDatasourceInfo> datasourceInfos = new ArrayList<IDatasourceInfo>();
        for (String id : ids) {
          datasourceInfos.add(new DatasourceInfo(id, id, TYPE, editable, removable, importable, exportable));
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
    datasourceService.export(dsInfo);

  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#remove(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void remove(IDatasourceInfo dsInfo, Object callback) {
    datasourceService.remove(dsInfo, callback);
  }

}
