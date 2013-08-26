package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import java.util.Collections;
import java.util.List;

import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.ui.service.DSWUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService;
import org.pentaho.platform.dataaccess.datasource.ui.service.JdbcDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.MetadataUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.MondrianUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.UIDatasourceServiceManager;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingException;
import org.pentaho.ui.xul.binding.BindingExceptionHandler;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulMenupopup;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeCols;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.user.client.Window;

public class DatasourceAdminDialogController extends AbstractXulDialogController<IDatasourceInfo> implements BindingExceptionHandler{

  // ~ Static fields/initializers ======================================================================================

  // ~ Instance fields =================================================================================================
  private BindingFactory bf;
  
  private IXulAsyncDatasourceServiceManager datasourceServiceManager;
  private IModelerServiceAsync modelerService;
  private IXulAsyncDSWDatasourceService dswService;
  private DatasourceAdminDialogModel datasourceAdminDialogModel = new DatasourceAdminDialogModel();

  private XulDialog datasourceAdminDialog;
  private XulDialog datasourceAdminErrorDialog;
  private XulDialog removeDatasourceConfirmationDialog;
  private XulLabel datasourceAdminErrorLabel = null;
  
  private XulButton datasourceAddButton;
  private XulMenupopup datasourceTypeMenuPopup;
  private XulButton exportDatasourceButton;
  private XulButton editDatasourceButton;
  private XulButton removeDatasourceButton;

  private Binding editDatasourceButtonBinding; 
  private Binding removeDatasourceButtonBinding;
  private Binding exportDatasourceButtonBinding;
  
  private GwtDatabaseDialog databaseDialog;
  private boolean administrator;
  XulTree datasourceTable = null;

  XulTreeCols datasourceTreeCols = null;
  UIDatasourceServiceManager manager;
  private GwtDatasourceEditorEntryPoint entryPoint;
  private DialogListener adminDatasourceListener;
  private GwtDatasourceMessages messageBundle;

  /**
   * Sets up bindings.
   */
  @Bindable
  public void init() {
    datasourceTable = (XulTree) document.getElementById("datasourcesListTable"); //$NON-NLS-1$
    datasourceTreeCols = (XulTreeCols) document.getElementById("datasourcesListCols"); //$NON-NLS-1$
    datasourceAdminDialog = (XulDialog) document.getElementById("datasourceAdminDialog"); //$NON-NLS-1$
    datasourceAdminErrorDialog = (XulDialog) document.getElementById("datasourceAdminErrorDialog"); //$NON-NLS-1$
    removeDatasourceConfirmationDialog = (XulDialog) document.getElementById("removeDatasourceConfirmationDialog"); //$NON-NLS-1$
    datasourceAdminErrorLabel = (XulLabel) document.getElementById("datasourceAdminErrorLabel");//$NON-NLS-1$

    datasourceAddButton = (XulButton) document.getElementById("datasourceAddButton"); //$NON-NLS-1$
    datasourceTypeMenuPopup = (XulMenupopup) document.getElementById("datasourceTypeMenuPopup"); //$NON-NLS-1$
    exportDatasourceButton = (XulButton) document.getElementById("exportDatasourceButton"); //$NON-NLS-1$
    editDatasourceButton = (XulButton) document.getElementById("editDatasourceButton"); //$NON-NLS-1$
    removeDatasourceButton = (XulButton) document.getElementById("removeDatasourceButton"); //$NON-NLS-1$
    bf.setBindingType(Binding.Type.ONE_WAY);
    try {
      
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
      
      //setup binding to disable edit datasource button until user selects a datasource
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
      // Initialize UI Datasource Service Manager
      adminDatasourceListener = new DialogListener() {

        @Override
        public void onDialogAccept(Object returnValue) {
          refreshDatasourceList();
        }

        @Override
        public void onDialogCancel() {
          // TODO Auto-generated method stub

        }

        @Override
        public void onDialogReady() {
          // TODO Auto-generated method stub
          
        }

        @Override
        public void onDialogError(String errorMessage) {
          openErrorDialog("Error", errorMessage);
        }
      };
      
      manager = UIDatasourceServiceManager.getInstance();

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

  public void setModelerService(final IModelerServiceAsync modelerService) {
    this.modelerService = modelerService;
  }

  public void setDSWService(final IXulAsyncDSWDatasourceService dswService) {
    this.dswService = dswService;
  }

  private void refreshDatasourceList() {
    if(messageBundle != null) {
      DatasourceInfo.setMessageBundle(messageBundle);
    }
    datasourceAdminDialogModel.setDatasourcesList(null);
      manager.getIds(new XulServiceCallback<List<IDatasourceInfo>>() {
  
        @Override
        public void success(List<IDatasourceInfo> infoList) {
          DatasourceAdminDialogController.super.showDialog();
          datasourceAdminDialogModel.setDatasourcesList(infoList);
          getDatasourceTypes();
          exportDatasourceButton.setDisabled(true);
          editDatasourceButton.setDisabled(true);
          removeDatasourceButton.setDisabled(true);
        }
  
        @Override
        public void error(String message, Throwable error) {
          openErrorDialog("Error", message + error.getMessage());
        }
      });
  }
  
  private void getDatasourceTypes() {
        List<String> datasourceTypes = manager.getTypes();
        // Clear out the current component list
        List<XulComponent> components = datasourceTypeMenuPopup.getChildNodes();
        for(XulComponent component:components) {
          datasourceTypeMenuPopup.removeComponent(component);
        }

        for(String datasourceType:datasourceTypes) {
          XulMenuitem menuItem;
          try {
            menuItem = (XulMenuitem) document.createElement("menuitem");
            menuItem.setLabel(DatasourceInfo.getDisplayType(datasourceType, messageBundle));
            menuItem.setCommand(getName() + ".launchNewUI(\""+ datasourceType + "\")");
            menuItem.setId(datasourceType);
            datasourceTypeMenuPopup.addChild(menuItem);
          } catch (XulException e) {
            throw new RuntimeException(e);
          }
        }
        datasourceAdminDialogModel.setDatasourceTypeList(datasourceTypes);
  }

  @Bindable
  public void launchNewUI(String datasourceType) {
      IUIDatasourceAdminService service = manager.getService(datasourceType);
      String newUI = service.getNewUI();
      if(newUI != null && newUI.length() > 0) {
        if(newUI.indexOf("builtin:") >= 0) {
          if(service.getType().equals(JdbcDatasourceService.TYPE)) {
            entryPoint.showDatabaseDialog(adminDatasourceListener);
          } else if (service.getType().equals(MondrianUIDatasourceService.TYPE)){
            entryPoint.showAnalysisImportDialog(adminDatasourceListener);
          } else if (service.getType().equals(MetadataUIDatasourceService.TYPE)){
            entryPoint.showMetadataImportDialog(adminDatasourceListener);
          } else if (service.getType().equals(DSWUIDatasourceService.TYPE)){
            entryPoint.showWizard(true, adminDatasourceListener);
          }
        } else if(newUI.indexOf("javascript:") >= 0) {
          String script = newUI.substring(newUI.indexOf(":") + 1);
          executeJavaScript(script);
        }
        		
      }
  }

  private native void executeJavaScript(String script) /*-{
    try{
      var callback = "{ onCancel: function(){} ,onOk: function(returnValue){$wnd.pho.refreshDatasourceList();},onError: function(errorMessage){$wnd.pho.showDatasourceAdminErrorDialog('Error', errorMessage);}}";
      var updatedScript = script.replace(/\{callback\}/g, callback);
      eval(updatedScript);
    } catch (e){
      $wnd.pho.showDatasourceAdminErrorDialog("Error", e.message);
    }
  }-*/;
  
  @Override
  public void showDialog() {
    refreshDatasourceList();
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
      refreshDatasourceList();
    }
  }
  
  public void setEntryPoint(GwtDatasourceEditorEntryPoint entryPoint) {
    this.entryPoint = entryPoint;
  }
  
  @Override
  public void handleException(BindingException t) {
    t.printStackTrace();
  }
  
  @Bindable
  public void export() {
    IDatasourceInfo dsInfo = datasourceAdminDialogModel.getSelectedDatasource();
    manager.exportDatasource(dsInfo);
  }
  
  @Bindable
  public void remove() {
    removeDatasourceConfirmationDialog.show();
  }
  
  @Bindable
  public void edit() {
    IDatasourceInfo dsInfo = datasourceAdminDialogModel.getSelectedDatasource();
    String type = dsInfo.getType();
    final String dsId = dsInfo.getId();
    if (DSWUIDatasourceService.TYPE.equals(type)) {
      dswService.getLogicalModels(dsId, new XulServiceCallback<List<LogicalModelSummary>>(){

        @Override
        public void success(List<LogicalModelSummary> retVal) {
          for (LogicalModelSummary logicalModelSummary : retVal){
            if (!dsId.equals(logicalModelSummary.getDomainId())) continue;
              entryPoint.showWizardEdit(
                logicalModelSummary.getDomainId(),
                logicalModelSummary.getModelId(),
                false,
                new DialogListener<Domain> (){

                  @Override
                  public void onDialogAccept(
                      Domain returnValue) {
                    // TODO Auto-generated method stub
                    
                  }

                  @Override
                  public void onDialogCancel() {
                    // TODO Auto-generated method stub
                    
                  }

                  @Override
                  public void onDialogReady() {
                    // TODO Auto-generated method stub
                    
                  }

                  @Override
                  public void onDialogError(String errorMessage) {
                    // TODO Auto-generated method stub
                    
                  }
                  
                }
              );          
          }
        }

        @Override
        public void error(String message, Throwable error) {
          // TODO Auto-generated method stub
          
        }
        
      });
    }
    else 
    if (JdbcDatasourceService.TYPE.equals(type)) {
      entryPoint.showEditDatabaseDialog(
        adminDatasourceListener, 
        dsId
      );
    }
    else
    if (MondrianUIDatasourceService.TYPE.equals(type)) {
      IDatasourceInfo datasourceInfo = datasourceAdminDialogModel.getSelectedDatasource();	
      entryPoint.showEditAnalysisDialog(adminDatasourceListener, datasourceInfo);
    }
  }
  
  @Bindable
  public void removeDatasourceAccept() {
    final IDatasourceInfo dsInfo = datasourceAdminDialogModel.getSelectedDatasource();
    manager.remove(dsInfo, new XulServiceCallback<Boolean>() {

       @Override
       public void success(Boolean isOk) {
    	   if (isOk) {
    		   refreshDatasourceList();
               editDatasourceButton.setDisabled(true);
           } else {
        	   Window.alert(
                 messageBundle.getString("datasourceAdminDialogController.COULD_NOT_REMOVE")
        			   + ": " + dsInfo.getId());
           }
        }

        @Override
        public void error(String message, Throwable error) {
        	Window.alert(
              messageBundle.getString("datasourceAdminDialogController.ERROR_REMOVING")
        			+  ": " + dsInfo.getId() + "." 
        					+ messageBundle.getString("ERROR") + "=" + error.getLocalizedMessage());
        }
    });
    removeDatasourceConfirmationDialog.hide();
  }
  
  @Bindable
  public void removeDatasourceCancel() {
    removeDatasourceConfirmationDialog.hide();
  }

  public void setMessageBundle(GwtDatasourceMessages messageBundle) {
    this.messageBundle = messageBundle;
  }
}
