package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DatasourceAdminDialogModel extends XulEventSourceAdapter {

  private List<IGenericDatasourceInfo> datasourceList;

  public DatasourceAdminDialogModel() {
    datasourceList = new ArrayList<IGenericDatasourceInfo>();
  }
  public void addDatasource(IGenericDatasourceInfo datasource) {
    datasourceList.add(datasource);
    this.firePropertyChange("datasourceList", null, datasourceList); //$NON-NLS-1$
  }

  public void removeDatasource(IGenericDatasourceInfo datasource) {
    datasourceList.remove(datasource);
    this.firePropertyChange("datasourceList", null, datasourceList); //$NON-NLS-1$
  }

  
  @Bindable
  public List<IGenericDatasourceInfo> getDatasourcesList() {
    return datasourceList;
  }

  @Bindable
  public void setDatasourcesList(List<IGenericDatasourceInfo> datasourceList) {
    this.datasourceList = new ArrayList<IGenericDatasourceInfo>(datasourceList);
    this.firePropertyChange("datasourceList", null, datasourceList); //$NON-NLS-1$
  }

  private int selectedIndex = -1;
  
  
  @Bindable
  public void setSelectedIndex(final int selectedIndex) {
    this.selectedIndex = selectedIndex;
    // we want this to fire every time. setting prevval to always be different.
    this.firePropertyChange("selectedIndex", selectedIndex+1, selectedIndex); //$NON-NLS-1$
  }

  @Bindable
  public int getSelectedIndex() {
    return selectedIndex;
  }
}
