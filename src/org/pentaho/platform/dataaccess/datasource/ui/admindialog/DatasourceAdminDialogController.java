package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import java.util.List;

import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncGenericDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

public class DatasourceAdminDialogController extends AbstractXulDialogController<IGenericDatasourceInfo> {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================

  private BindingFactory bf;
  
  private IXulAsyncGenericDatasourceServiceManager datasourceServiceManager;

  private DatasourceAdminDialogModel datasourceAdminDialogModel = new DatasourceAdminDialogModel();

  private XulDialog datasourceAdminDialog;

  private boolean administrator;
  XulTree datasourceTable = null;

  XulTreeCols datasourceTreeCols = null;


  /**
   * Sets up bindings.
   */
  @Bindable
  public void init() {
    datasourceTable = (XulTree) document.getElementById("datasourcesListTable"); //$NON-NLS-1$
    datasourceTreeCols = (XulTreeCols) document.getElementById("datasourcesListCols"); //$NON-NLS-1$
    datasourceAdminDialog = (XulDialog) document.getElementById("datasourceAdminDialog"); //$NON-NLS-1$
    
    bf.setBindingType(Binding.Type.ONE_WAY);
    try {
      bf.createBinding(datasourceAdminDialogModel, "datasourceList", datasourceTable, "elements").fireSourceChanged();
      bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
      bf.createBinding(datasourceTable, "selectedIndex", datasourceAdminDialogModel, "selectedIndex");
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * ID of this controller. This is how event handlers are referenced in <code>.xul</code> files.
   */
  @Override
  public String getName() {
    return "datasourceAdminDialogController"; //$NON-NLS-1$
  }

  public void setBindingFactory(final BindingFactory bf) {
    this.bf = bf;
  }

  public void setGenericDatasourceServiceManager(final IXulAsyncGenericDatasourceServiceManager datasourceServiceManager) {
    this.datasourceServiceManager = datasourceServiceManager;
  }
  
  private void refreshDatasourceList() {
    datasourceServiceManager.getAll(new XulServiceCallback<List<IGenericDatasourceInfo>>() {
      public void error(final String message, final Throwable error) {
      
      }

      public void success(final List<IGenericDatasourceInfo> datasourceInfoList) {
        datasourceAdminDialogModel.setDatasourcesList(datasourceInfoList);
      }
    });
  }
  
  @Override
  public void showDialog() {    
    super.showDialog();
    refreshDatasourceList();
  }

  @Override
  protected XulDialog getDialog() {
    return datasourceAdminDialog;
  }

  @Override
  protected IGenericDatasourceInfo getDialogResult() {
    return datasourceAdminDialogModel.getDatasourcesList().get(datasourceAdminDialogModel.getSelectedIndex());
  }

}
