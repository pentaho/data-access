package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DatasourceAdminDialogModel extends XulEventSourceAdapter {

  private List<IDatasourceInfo> datasources;
  private List<String> datasourceTypes;
  private IDatasourceInfo selectedDatasource;
  private int selectedIndex = -1;
  
  

  public DatasourceAdminDialogModel() {
    datasources = new ArrayList<IDatasourceInfo>();
  }
  public void addDatasource(IDatasourceInfo datasource) {
    datasources.add(datasource);
    this.firePropertyChange("datasources", null, datasources); //$NON-NLS-1$
  }

  public void removeDatasource(IDatasourceInfo datasource) {
    datasources.remove(datasource);
    this.firePropertyChange("datasources", null, datasources); //$NON-NLS-1$
  }

  
  @Bindable
  public List<IDatasourceInfo> getDatasourcesList() {
    return datasources;
  }

  @Bindable
  public void setDatasourcesList(List<IDatasourceInfo> datasourceList) {
    this.datasources = new ArrayList<IDatasourceInfo>(datasourceList);
    this.firePropertyChange("datasources", null, datasourceList); //$NON-NLS-1$
  }

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
  
  @Bindable
  public void setDatasourceTypeList(List<String> datasourceTypes) {
    this.datasourceTypes = new ArrayList<String>(datasourceTypes);
    this.firePropertyChange("datasourceTypes", null, datasourceTypes); //$NON-NLS-1$
  }
  
  @Bindable
  public List<String> getDatasourceTypeList() {
    return datasourceTypes;
  }
  
  @Bindable
  public void setSelectedDatasource(IDatasourceInfo selectedDatasource) {
    this.selectedDatasource = selectedDatasource;
    this.firePropertyChange("selectedDatasource", null, selectedDatasource); //$NON-NLS-1$
  }
  
  @Bindable
  public IDatasourceInfo getSelectedDatasource() {
    return selectedDatasource;
  }
}
