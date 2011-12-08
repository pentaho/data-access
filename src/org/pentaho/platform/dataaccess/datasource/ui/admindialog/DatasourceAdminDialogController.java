package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingException;
import org.pentaho.ui.xul.binding.BindingExceptionHandler;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

public class DatasourceAdminDialogController extends AbstractXulDialogController<IDatasourceInfo> implements BindingExceptionHandler {

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================
  public final static String ADD_A_DATASOURCE = "Add Data source";
  private BindingFactory bf;
  
  private IXulAsyncDatasourceServiceManager datasourceServiceManager;

  private DatasourceAdminDialogModel datasourceAdminDialogModel = new DatasourceAdminDialogModel();

  private XulDialog datasourceAdminDialog;
  
  private XulDialog datasourceAdminErrorDialog;
  private XulLabel datasourceAdminErrorLabel = null;
  
  private XulMenuList datasourceTypeMenuList;

  private XulButton exportDatasourceButton;
  private XulButton editDatasourceButton;
  private XulButton removeDatasourceButton;
  
  private Binding editDatasourceButtonBinding; 
  private Binding removeDatasourceButtonBinding;
  private Binding exportDatasourceButtonBinding;
  
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
    datasourceAdminErrorDialog = (XulDialog) document.getElementById("datasourceAdminErrorDialog"); //$NON-NLS-1$
    datasourceAdminErrorLabel = (XulLabel) document.getElementById("datasourceAdminErrorLabel");//$NON-NLS-1$
    datasourceTypeMenuList = (XulMenuList) document.getElementById("datasourceTypeMenuList");//$NON-NLS-1$
    exportDatasourceButton = (XulButton) document.getElementById("exportDatasourceButton"); //$NON-NLS-1$
    editDatasourceButton = (XulButton) document.getElementById("editDatasourceButton"); //$NON-NLS-1$
    removeDatasourceButton = (XulButton) document.getElementById("removeDatasourceButton"); //$NON-NLS-1$

    bf.setBindingType(Binding.Type.ONE_WAY);
    try {
      Binding datasourceBinding = bf.createBinding(datasourceAdminDialogModel, "datasourceTypes", datasourceTypeMenuList, "elements");
      BindingConvertor<IDatasourceInfo, Boolean> removeDatasourceButtonConvertor = new BindingConvertor<IDatasourceInfo, Boolean>() {
        @Override
        public Boolean sourceToTarget(final IDatasourceInfo datasourceInfo) {
          return datasourceInfo.isRemovable();
        }

        @Override
        public IDatasourceInfo targetToSource(final Boolean value) {
          throw new UnsupportedOperationException();
        }
      };

      BindingConvertor<IDatasourceInfo, Boolean> editDatasourceButtonConvertor = new BindingConvertor<IDatasourceInfo, Boolean>() {
        @Override
        public Boolean sourceToTarget(final IDatasourceInfo datasourceInfo) {
          return datasourceInfo.isEditable();
        }

        @Override
        public IDatasourceInfo targetToSource(final Boolean value) {
          throw new UnsupportedOperationException();
        }
      };
      
      BindingConvertor<IDatasourceInfo, Boolean> exportDatasourceButtonConvertor = new BindingConvertor<IDatasourceInfo, Boolean>() {
        @Override
        public Boolean sourceToTarget(final IDatasourceInfo datasourceInfo) {
          return datasourceInfo.isExportable();
        }

        @Override
        public IDatasourceInfo targetToSource(final Boolean value) {
          throw new UnsupportedOperationException();
        }
      };
      removeDatasourceButtonBinding = bf.createBinding(datasourceAdminDialogModel, "selectedDatasource", //$NON-NLS-1$
          removeDatasourceButton, "!disabled", removeDatasourceButtonConvertor); //$NON-NLS-1$
      editDatasourceButtonBinding = bf.createBinding(datasourceAdminDialogModel, "selectedDatasource", //$NON-NLS-1$
          editDatasourceButton, "!disabled", editDatasourceButtonConvertor); //$NON-NLS-1$
      exportDatasourceButtonBinding = bf.createBinding(datasourceAdminDialogModel, "selectedDatasource", //$NON-NLS-1$
          exportDatasourceButton, "!disabled", exportDatasourceButtonConvertor); //$NON-NLS-1$

      bf.createBinding(datasourceAdminDialogModel, "datasources", datasourceTable, "elements");
      bf.createBinding(datasourceTable, "selectedItems", datasourceAdminDialogModel, "selectedDatasource", //$NON-NLS-1$//$NON-NLS-2$
      new BindingConvertor<List, IDatasourceInfo>() { 

        @Override
        public IDatasourceInfo sourceToTarget( List datasources) {
          if(datasources != null && datasources.size() > 0) {
            return (IDatasourceInfo) datasources.get(0);
          }
          return null;
        }

        @Override
        public List targetToSource( IDatasourceInfo arg0 ) {
          throw new UnsupportedOperationException();
        }

      }).fireSourceChanged();

      bf.createBinding(datasourceTable, "selectedItem", datasourceAdminDialogModel, "selectedDatasource").fireSourceChanged();
      
      removeDatasourceButtonBinding.fireSourceChanged();
      editDatasourceButtonBinding.fireSourceChanged();
      exportDatasourceButtonBinding.fireSourceChanged();
      setupNativeHooks(this);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
  }
  private native void setupNativeHooks(DatasourceAdminDialogController controller)/*-{
    $wnd.pho.showDatasourceAdminErrorDialog = function(title, errorMessage) {
      controller.@org.pentaho.platform.dataaccess.datasource.ui.admindialog.DatasourceAdminDialogController::openErrorDialog(Ljava/lang/String;Ljava/lang/String;)(title, errorMessage);
    }
    $wnd.pho.refreshDatasourceList = function() {
      controller.@org.pentaho.platform.dataaccess.datasource.ui.admindialog.DatasourceAdminDialogController::refreshDatasourceList()();
    }
    
  }-*/;
  
  /**
   * ID of this controller. This is how event handlers are referenced in <code>.xul</code> files.
   */
  @Override
  public String getName() {
    return "datasourceAdminDialogController"; //$NON-NLS-1$
  }

  public void setBindingFactory(final BindingFactory bf) {
    this.bf = bf;
    this.bf.setExceptionHandler(this);
  }

  public void setDatasourceServiceManager(final IXulAsyncDatasourceServiceManager datasourceServiceManager) {
    this.datasourceServiceManager = datasourceServiceManager;
  }
  
  private void refreshDatasourceList() {
    datasourceServiceManager.getAll(new XulServiceCallback<List<IDatasourceInfo>>() {
      public void error(final String message, final Throwable error) {
      
      }

      public void success(final List<IDatasourceInfo> datasourceInfoList) {
        datasourceAdminDialogModel.setDatasourcesList(datasourceInfoList);
      }
    });
  }
  
  private void getDatasourceTypes() {
    datasourceServiceManager.getTypes(new XulServiceCallback<List<String>>() {
      public void error(final String message, final Throwable error) {
      
      }

      public void success(final List<String> types) {
        List<String> updatedTypeList = new ArrayList<String>();
        updatedTypeList.add(ADD_A_DATASOURCE);
        updatedTypeList.addAll(types);
        datasourceAdminDialogModel.setDatasourceTypeList(updatedTypeList);
      }
    });
  }
  
  
  @Bindable
  public void launchNewUI() {
    String datasourceType = datasourceTypeMenuList.getSelectedItem();
    if(!datasourceType.equals(ADD_A_DATASOURCE)) {
      datasourceServiceManager.getNewUI(datasourceType, new XulServiceCallback<String>() {
        public void error(final String message, final Throwable error) {
        
        }

        public void success(final String javascriptString) {
          executeJavaScript(javascriptString);
        }
      });
    }
  }

  private native void executeJavaScript(String script) /*-{
    try{
      var callback = "{ onCancel: function(){} ,onOk: function(){$wnd.pho.refreshDatasourceList();},onReady: function(){}}";
      var updatedScript = script.replace(/\{callback\}/g, callback);
      eval(updatedScript);
    } catch (e){
      $wnd.pho.showDatasourceAdminErrorDialog("Error", e.message);
    }
  }-*/;
  
  
  @Bindable
  public void launchEditUI() {
    IDatasourceInfo datasourceInfo = datasourceAdminDialogModel.getSelectedDatasource();
    
    datasourceServiceManager.getEditUI(datasourceInfo.getType(), datasourceInfo.getName(), new XulServiceCallback<String>() {
      public void error(final String message, final Throwable error) {
      
      }

      public void success(final String javascriptString) {
        System.out.println("JavaScriptString is = " + javascriptString);
      }
    });
  }

  @Override
  public void showDialog() {    
    super.showDialog();
    refreshDatasourceList();
    getDatasourceTypes();
  }

  @Override
  protected XulDialog getDialog() {
    return datasourceAdminDialog;
  }

  @Override
  protected IDatasourceInfo getDialogResult() {
    return datasourceAdminDialogModel.getDatasourcesList().get(datasourceAdminDialogModel.getSelectedIndex());
  }

  @Bindable
  public void openErrorDialog(String title, String message) {
    datasourceAdminErrorDialog.setTitle(title);
    datasourceAdminErrorLabel.setValue(message);
    datasourceAdminErrorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!datasourceAdminErrorDialog.isHidden()) {
      datasourceAdminErrorDialog.hide();
    }
  }
  
  @Override
  public void handleException(BindingException t) {
    t.printStackTrace();
  }
}
