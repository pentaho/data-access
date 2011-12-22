/*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright 2008 - 2009 Pentaho Corporation. All rights reserved.
*
*
* Created April 21, 2009
* @author rmansoor
*/
package org.pentaho.platform.dataaccess.datasource.wizard;


import java.util.List;

import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.impl.GwtModelerServiceImpl;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.modeler.ModelerDialog;
import org.pentaho.platform.dataaccess.datasource.ui.admindialog.GwtDatasourceAdminDialog;
import org.pentaho.platform.dataaccess.datasource.ui.importing.AnalysisImportDialogModel;
import org.pentaho.platform.dataaccess.datasource.ui.importing.GwtImportDialog;
import org.pentaho.platform.dataaccess.datasource.ui.importing.MetadataImportDialogModel;
import org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceManageDialog;
import org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.AnalysisDatasourceServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceManagerGwtImpl;
import org.pentaho.ui.database.event.DatabaseDialogListener;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseConnectionService;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseDialectService;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.util.DialogController.DialogListener;
import org.pentaho.ui.xul.util.XulDialogCallback;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;

/**
* Creates the singleton datasource wizard and sets up native JavaScript functions to show the wizard.
*/
public class GwtDatasourceEditorEntryPoint implements EntryPoint {

  private EmbeddedWizard wizard;
  // TODO: make this lazily loaded when the modelerMessages issue is fixed
  private ModelerDialog modeler;
  private IXulAsyncDSWDatasourceService datasourceService;
  private IXulAsyncConnectionService connectionService;
  private IModelerServiceAsync modelerService;
  private IXulAsyncDatasourceServiceManager datasourceServiceManager;
  private ICsvDatasourceServiceAsync csvService;
  private GwtDatasourceSelectionDialog gwtDatasourceSelectionDialog;
  private GwtDatabaseDialog gwtDatabaseDialog;
  private DatabaseTypeHelper databaseTypeHelper;
  private DatabaseConnectionConverter databaseConnectionConverter;
  private EmbeddedWizard gwtDatasourceEditor;
  private GwtDatasourceManageDialog manageDialog;
  private GwtDatasourceSelectionDialog selectDialog ;
  private GwtDatasourceAdminDialog adminDialog;
  private GwtImportDialog importDialog;
  private boolean asyncConstructorDone;
  private boolean hasPermissions;
  private boolean isAdmin;
  private Timer timer;
  private GwtXulAsyncDatabaseConnectionService connService = new GwtXulAsyncDatabaseConnectionService();
  private GwtXulAsyncDatabaseDialectService dialectService = new GwtXulAsyncDatabaseDialectService();

  public void onModuleLoad() {
    datasourceServiceManager = new DatasourceServiceManagerGwtImpl();
    datasourceServiceManager.isAdmin(new XulServiceCallback<Boolean>() {
      public void error(String message, Throwable error) {
      }
      public void success(Boolean retVal) {
        isAdmin = retVal;
        datasourceService = new DSWDatasourceServiceGwtImpl();
        modelerService = new GwtModelerServiceImpl();
        BogoPojo bogo = new BogoPojo();
        modelerService.gwtWorkaround(bogo, new XulServiceCallback<BogoPojo>(){
          public void success(BogoPojo retVal) {

          }

          public void error(String message, Throwable error) {

          }
        });

        // only init the app if the user has permissions

        datasourceService.hasPermission(new XulServiceCallback<Boolean>() {
          public void error(String message, Throwable error) {
            setupStandardNativeHooks(GwtDatasourceEditorEntryPoint.this);
            initDashboardButtons(false);
          }
          public void success(Boolean retVal) {
            hasPermissions = retVal;
            setupStandardNativeHooks(GwtDatasourceEditorEntryPoint.this);
            if (isAdmin || hasPermissions) {
              connectionService = new ConnectionServiceGwtImpl();
              csvService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
              setupPrivilegedNativeHooks(GwtDatasourceEditorEntryPoint.this);
              loadOverlay("startup.dataaccess");
            }
            initDashboardButtons(retVal);
          }
        });
      }
    });
    
    XulServiceCallback<List<IDatabaseType>> callback = new XulServiceCallback<List<IDatabaseType>>() {
      public void error(String message, Throwable error) {
        error.printStackTrace();
      }

      public void success(List<IDatabaseType> retVal) {
        databaseTypeHelper = new DatabaseTypeHelper(retVal);
        databaseConnectionConverter = new DatabaseConnectionConverter(databaseTypeHelper);

      }
    };
    dialectService.getDatabaseTypes(callback);
  }
  private native static void loadOverlay( String overlayId) /*-{
  if(!$wnd.mantle_loadOverlay){
  setTimeout(function(){
  @org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::loadOverlay(Ljava/lang/String;)(overlayId);
  }, 200);
  return;
  }
  $wnd.mantle_loadOverlay(overlayId)
  }-*/;

  private native void removeOverlay(String overlayId) /*-{
  if($wnd.mantle_removeOverlay)
  $wnd.mantle_removeOverlay(overlayId)
  }-*/;

  public native void initDashboardButtons(boolean val) /*-{
  if($wnd.initDataAccess){
  $wnd.initDataAccess(val);
  }
  }-*/;

  private native void setupStandardNativeHooks(GwtDatasourceEditorEntryPoint wizard)/*-{
  if(!$wnd.pho){
    $wnd.pho = {};
  }
  $wnd.addDataAccessGlassPaneListener = function(callback) {
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::addGlassPaneListener(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
  }
  
  $wnd.pho.showDatasourceSelectionDialog = function(context, callback) {
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showSelectionDialog(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(context,"true", callback);
  }
  $wnd.pho.showDatabaseDialog = function(callback) {
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showDatabaseDialog(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
  }
  $wnd.gwtConfirm = function(message, callback, options){
    var title = options.title || $wnd.pho_messages.getMessage("prompt","Prompt");
    var accept = options.acceptLabel || $wnd.pho_messages.getMessage("okButton","OK");
    var cancel = options.cancelLabel || $wnd.pho_messages.getMessage("cancelButton","Cancel");
    try{
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showConfirm(Lcom/google/gwt/core/client/JavaScriptObject;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(callback, message, title, accept, cancel);
      } catch(e) {
      // if it fails just show browser prompt
      callback.okOk($wnd.confirm(message));
      }
    }
  }-*/;

  private native void setupPrivilegedNativeHooks(GwtDatasourceEditorEntryPoint wizard)/*-{
  $wnd.pho.openDatasourceEditor= function(callback, reportingOnlyValid) {
    if(typeof reportingOnlyValid == "undefined"){
      reportingOnlyValid = true;
    }
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showWizard(ZLcom/google/gwt/core/client/JavaScriptObject;)(reportingOnlyValid, callback);
  }
  $wnd.pho.openEditDatasourceEditor= function(domainId, modelId, callback, perspective, reportingOnlyValid) {
    if(typeof reportingOnlyValid == "undefined"){
      reportingOnlyValid = true;
    }
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showWizardEdit(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLcom/google/gwt/core/client/JavaScriptObject;)(domainId, modelId, perspective, reportingOnlyValid, callback);
  }
  $wnd.pho.deleteModel=function(domainId, modelName, callback) {
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::deleteLogicalModel(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(domainId, modelName, callback);
  }

  $wnd.pho.showDatasourceManageDialog = function(callback) {
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showSelectionDialog(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)("manage", "false", callback);
  }
  $wnd.pho.showMetadataImportDialog = function(callback) {
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showMetadataImportDialog(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
  }

  $wnd.pho.showAnalysisImportDialog = function(callback) {
    wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showAnalysisImportDialog(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
  }
}-*/;

  public void showConfirm(final JavaScriptObject callback, String message, String title, String okText, String cancelText) throws XulException{
    XulConfirmBox confirm = (XulConfirmBox) wizard.getMainWizardContainer().getDocumentRoot().createElement("confirmbox");
    confirm.setTitle(title);
    confirm.setMessage(message);
    confirm.setAcceptLabel(okText);
    confirm.setCancelLabel(cancelText);
    confirm.addDialogCallback(new XulDialogCallback<String>(){
      public void onClose(XulComponent component, Status status, String value) {
        if(status == XulDialogCallback.Status.ACCEPT){
          notifyDialogCallbackSuccess(callback, value);
        }
      }
      public void onError(XulComponent component, Throwable err) {
        notifyDialogCallbackError(callback, err.getMessage());
      }
    });
    confirm.open();
  }


  @SuppressWarnings("unused")
  private void addGlassPaneListener(JavaScriptObject obj) {
    GlassPane.getInstance().addGlassPaneListener(new GlassPaneNativeListener(obj));
  }

  
  public void showWizard(final boolean relationalOnlyValid, final DialogListener<Domain> listener) {

    if(wizard == null){
      wizard = new EmbeddedWizard(false);
      wizard.setDatasourceService(datasourceService);
      wizard.setConnectionService(connectionService);
      wizard.setCsvDatasourceService(csvService);
      wizard.setReportingOnlyValid(relationalOnlyValid);
      wizard.init(new AsyncConstructorListener<EmbeddedWizard>() {
        @Override
        public void asyncConstructorDone(EmbeddedWizard source) {
          wizard.addDialogListener(listener);
          wizard.showDialog();
        }
      });
    } else {
      wizard.addDialogListener(listener);
      wizard.setReportingOnlyValid(relationalOnlyValid);
      wizard.showDialog();
    }    
  }
  /**
* Entry-point from Javascript, responds to provided callback with the following:
*
* onOk(String JSON, String mqlString);
* onCancel();
* onError(String errorMessage);
*
* @param callback
*
*/
  private void showWizard(final boolean relationalOnlyValid, final JavaScriptObject callback) {

    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
        wizard.removeDialogListener(this);
        if(callback != null) {
          notifyCallbackCancel(callback);
        }
      }
      public void onDialogAccept(final Domain domain) {
        wizard.removeDialogListener(this);
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
        notifyDialogCallbackSuccess(callback, domain.getId());
      }
      public void onDialogReady() {
        if(callback != null) {
          notifyCallbackCancel(callback);
        }
      }
      @Override
      public void onDialogError(String errorMessage) {
        notifyCallbackError(callback, errorMessage);
      }
    };
    showWizard(relationalOnlyValid, listener);

  }

  public void showWizardEdit(final String domainId, final String modelId, boolean relationalOnlyValid, final DialogListener<Domain> listener) {
    showWizardEdit(domainId, modelId, ModelerPerspective.REPORTING.name(), relationalOnlyValid, listener);
  }
  
  public void showWizardEdit(final String domainId, final String modelId, final String perspective, boolean relationalOnlyValid, final DialogListener<Domain> listener) {
    final String modelPerspective;
    if (perspective == null) {
      modelPerspective = ModelerPerspective.REPORTING.name();
    } else {
      modelPerspective = perspective;
    }

    modeler = ModelerDialog.getInstance(wizard, new AsyncConstructorListener<ModelerDialog>(){
      public void asyncConstructorDone(ModelerDialog dialog) {

        ModelerPerspective modelerPerspective;
        try {
          modelerPerspective = ModelerPerspective.valueOf(modelPerspective);
        } catch (IllegalArgumentException e) {
          modelerPerspective = ModelerPerspective.REPORTING;
  }
        dialog.addDialogListener(listener);
        dialog.showDialog(domainId, modelId, modelerPerspective);
      }
    });

    
  }
  private void showWizardEdit(final String domainId, final String modelId, boolean relationalOnlyValid, final JavaScriptObject callback) {
    showWizardEdit(domainId, modelId, ModelerPerspective.REPORTING.name(), relationalOnlyValid, callback);
  }
  /**
* edit entry-point from Javascript, responds to provided callback with the following:
*
* onOk(String JSON, String mqlString);
* onCancel();
* onError(String errorMessage);
*
* @param callback
*
*/
  private void showWizardEdit(final String domainId, final String modelId, final String perspective, boolean relationalOnlyValid, final JavaScriptObject callback) {
    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
        modeler.removeDialogListener(this);
        if(callback != null) {
          notifyCallbackCancel(callback);
        }
      }
      public void onDialogAccept(final Domain domain) {
        modeler.removeDialogListener(this);
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
      }
      public void onDialogReady() {
        if(callback != null) {
          notifyCallbackCancel(callback);
        }
      }
      @Override
      public void onDialogError(String errorMessage) {
        notifyCallbackError(callback, errorMessage);
      }
    };

  }


  /**
* Deletes the selected model
*
* onOk(Boolean value);
* onCancel();
* onError(String errorMessage);
*
* @param callback
*
*/
  private void deleteLogicalModel(String domainId, String modelName, final JavaScriptObject callback) {
    datasourceService.deleteLogicalModel(domainId, modelName, new XulServiceCallback<Boolean>(){
      public void success(Boolean value) {
        notifyCallbackSuccess(callback, value);
      }

      public void error(String s, Throwable throwable) {
        notifyCallbackError(callback, throwable.getMessage());
      }
    });
  }


  @SuppressWarnings("unused")
  private void showAdminDialog(final JavaScriptObject callback) {
    
    final DialogListener<IDatasourceInfo> listener = new DialogListener<IDatasourceInfo>(){
      public void onDialogCancel() {
        adminDialog.removeDialogListener(this);
        asyncConstructorDone = false;
        notifyCallbackCancel(callback);
      }

      public void onDialogAccept(final IDatasourceInfo genericDatasourceInfo) {
        adminDialog.removeDialogListener(this);
        asyncConstructorDone = false;
        notifyCallbackSuccess(callback, genericDatasourceInfo.getId(), genericDatasourceInfo.getType());
      }

      public void onDialogReady() {
      }

      @Override
      public void onDialogError(String errorMessage) {
        notifyCallbackError(callback, errorMessage);
        
      }
    };
    showAdminDialog(listener);
  }
  

  @SuppressWarnings("unused")
  private void showSelectionDialog(final String context, final String selectDatasource, final JavaScriptObject callback) {
    final boolean selectDs = Boolean.valueOf(selectDatasource);


    final DialogListener<LogicalModelSummary> listener = new DialogListener<LogicalModelSummary>(){
      public void onDialogCancel() {
        selectDialog.removeDialogListener(this);
        asyncConstructorDone = false;
        notifyCallbackCancel(callback);
      }

      public void onDialogAccept(final LogicalModelSummary logicalModelSummary) {
        selectDialog.removeDialogListener(this);
        asyncConstructorDone = false;
        notifyCallbackSuccess(callback, logicalModelSummary.getDomainId(), logicalModelSummary.getModelId(),logicalModelSummary.getModelName());
      }

      public void onDialogReady() {
      }

      public void onDialogError(String errorMessage) {
        notifyDialogCallbackError(callback, errorMessage);
      }
    };
    if(wizard == null && this.hasPermissions){
      wizard = new EmbeddedWizard(false);

      wizard.setDatasourceService(datasourceService);
      wizard.setConnectionService(connectionService);
      wizard.setCsvDatasourceService(csvService);
      wizard.init(new AsyncConstructorListener<EmbeddedWizard>() {
        public void asyncConstructorDone(EmbeddedWizard source) {
          if(context != null && context.equals("manage") && isAdmin) {
            showAdminDialog(callback);
          } else {
          showSelectionDialog(context, selectDs, listener);
          }
        }
      });
    } else {
      if(context != null && context.equals("manage") && isAdmin) {
        showAdminDialog(callback);
      } else {
        showSelectionDialog(context, selectDs, listener);
      }
    }
  }


  private void showAdminDialog(final DialogListener<IDatasourceInfo> listener) {
    if(adminDialog == null) {
      final AsyncConstructorListener<GwtDatasourceAdminDialog> constructorListener = getAdminDialogListener(listener);
      asyncConstructorDone = false;
      adminDialog = new GwtDatasourceAdminDialog(datasourceServiceManager, connectionService, modelerService, datasourceService, this, constructorListener);
    } else {
      adminDialog.addDialogListener(listener);
      adminDialog.showDialog();
    }
  }
  private void showMetadataImportDialog(final JavaScriptObject callback) {
    final DialogListener listener = new DialogListener(){

      public void onDialogCancel() {
      }
      
      public void onDialogAccept(final Object value) {
      }
      
      public void onDialogReady() {
      }

      @Override
      public void onDialogError(String errorMessage) {
        // TODO Auto-generated method stub
        
      }
    };
    showMetadataImportDialog(listener);
  }

  public void showMetadataImportDialog(final DialogListener listener) {
    final DialogListener<MetadataImportDialogModel> metadataImportListener = new DialogListener<MetadataImportDialogModel>(){

      public void onDialogCancel() {
      }
      
      public void onDialogAccept(final MetadataImportDialogModel value) {
     Window.alert("MetdataImport - pending");
      }
      
      public void onDialogReady() {
      }

      @Override
      public void onDialogError(String errorMessage) {
        // TODO Auto-generated method stub
        
      }
    };
    
    final AsyncConstructorListener<GwtImportDialog> constructorListener = new AsyncConstructorListener<GwtImportDialog>() {

      public void asyncConstructorDone(GwtImportDialog dialog) {
          dialog.showMetadataImportDialog(metadataImportListener);
      }
    };
    
    if(importDialog == null){
     importDialog = new GwtImportDialog(constructorListener);
    } else {
     importDialog.showMetadataImportDialog(metadataImportListener);
    }
  }
  
  private void showAnalysisImportDialog(final JavaScriptObject callback) {
    final DialogListener<AnalysisImportDialogModel> listener = new DialogListener<AnalysisImportDialogModel>(){
      
      public void onDialogCancel() {
      }

      public void onDialogAccept(final AnalysisImportDialogModel importDialogModel) {

     AnalysisDatasourceServiceGwtImpl service = new AnalysisDatasourceServiceGwtImpl();
     service.importAnalysisDatasource(importDialogModel.getUploadedFile(), 
         importDialogModel.getConnection().getName(), importDialogModel.getParameters(), new XulServiceCallback<String>() {

            @Override
            public void success(String retVal) {
              notifyDialogCallbackSuccess(callback, retVal);
            }

            @Override
            public void error(String message, Throwable error) {
              notifyDialogCallbackError(callback, message);
            }
          });
    
      }
      
      public void onDialogReady() {
      }

      @Override
      public void onDialogError(String errorMessage) {
        // TODO Auto-generated method stub
        
      }
    };
    showAnalysisImportDialog(listener);
  }
  
  public void showAnalysisImportDialog(final DialogListener listener) {
	    final DialogListener<AnalysisImportDialogModel> importDialoglistener = new DialogListener<AnalysisImportDialogModel>(){
	        
	        public void onDialogCancel() {
	        }

	        public void onDialogAccept(final AnalysisImportDialogModel importDialogModel) {

	       AnalysisDatasourceServiceGwtImpl service = new AnalysisDatasourceServiceGwtImpl();
	       service.importAnalysisDatasource(importDialogModel.getUploadedFile(), 
	           importDialogModel.getConnection().getName(), importDialogModel.getParameters(), new XulServiceCallback<String>() {

	              @Override
	              public void success(String retVal) {
	            	  listener.onDialogAccept(retVal);
	              }

	              @Override
	              public void error(String message, Throwable error) {
	            	  listener.onDialogError(message);
	              }
	            });
	      
	        }
	        
	        public void onDialogReady() {
	        }

	        @Override
	        public void onDialogError(String errorMessage) {
	        	listener.onDialogError(errorMessage);
	          
	        }
	      };
	  
     final AsyncConstructorListener<GwtImportDialog> constructorListener = new AsyncConstructorListener<GwtImportDialog>() {
  
       public void asyncConstructorDone(GwtImportDialog dialog) {
         dialog.showAnalysisImportDialog(importDialoglistener);
       }
     };
  
     if(importDialog == null){
         importDialog = new GwtImportDialog(constructorListener);
     } else {
         importDialog.showAnalysisImportDialog(importDialoglistener);
     }
  }

  
  private void showSelectionDialog(final String context, final boolean selectDs, final DialogListener<LogicalModelSummary> listener) {
    if (selectDs) {
      // selection dialog
      if (selectDialog == null) {

        final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener = getSelectionDialogListener(listener);
        asyncConstructorDone = false;
        selectDialog = new GwtDatasourceSelectionDialog(context, datasourceService, wizard, constructorListener);

      } else {
        selectDialog.addDialogListener(listener);
        selectDialog.reset();
        selectDialog.setContext(context);
        selectDialog.showDialog();
      }

    } else {
      // manage dialog
      if (manageDialog == null) {

        final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener = getSelectionDialogListener(listener);
        asyncConstructorDone = false;
        manageDialog = new GwtDatasourceManageDialog(datasourceService, wizard, constructorListener);
      } else {
        manageDialog.reset();
        manageDialog.showDialog();
      }
    }
  }

  private AsyncConstructorListener<GwtDatasourceSelectionDialog> getSelectionDialogListener(final DialogListener<LogicalModelSummary> listener){
     return new AsyncConstructorListener<GwtDatasourceSelectionDialog>() {

      public void asyncConstructorDone(GwtDatasourceSelectionDialog dialog) {
        dialog.removeDialogListener(listener);
        dialog.addDialogListener(listener);
        if (!asyncConstructorDone) {
          dialog.showDialog();
        }
        asyncConstructorDone = true;
      }
    };

  }
  
  private AsyncConstructorListener<GwtDatasourceAdminDialog> getAdminDialogListener(final DialogListener<IDatasourceInfo> listener){
    return new AsyncConstructorListener<GwtDatasourceAdminDialog>() {

     public void asyncConstructorDone(GwtDatasourceAdminDialog dialog) {
       dialog.removeDialogListener(listener);
       dialog.addDialogListener(listener);
       if (!asyncConstructorDone) {
         dialog.showDialog();
       }
       asyncConstructorDone = true;
     }
   };

 }
  private void showDatabaseDialog(final JavaScriptObject callback) {

    final DialogListener<IDatabaseConnection> listener = new DialogListener<IDatabaseConnection>(){
      public void onDialogCancel() {
        if(callback != null) {
          notifyCallbackCancel(callback);          
        }
      }
      public void onDialogAccept(final IDatabaseConnection connection) {
        if(callback != null) {
          notifyCallbackSuccess(callback, true);
        }
      }
      public void onDialogReady() {
        if(callback != null) {
          notifyCallbackCancel(callback);          
        }
      }
      @Override
      public void onDialogError(String errorMessage) {
        if(callback != null) {
          notifyCallbackError(callback, errorMessage);
        }
      }
    };
    showDatabaseDialog(listener);
  }

  public void showDatabaseDialog(final DialogListener<IDatabaseConnection> listener) {
      ConnectionController connectionController = wizard.getConnectionController();
      connectionController.init();
      DatasourceModel datasourceModel = new DatasourceModel();
      connectionController.setDatasourceModel(datasourceModel);
      connectionController.showAddConnectionDialog(listener);
  }

  private void showEditDatabaseDialog(final JavaScriptObject callback, final String databaseId) {
    if(gwtDatabaseDialog != null){
      gwtDatabaseDialog.setDatabaseConnection(null);
      gwtDatabaseDialog.show();
    } else {
      gwtDatabaseDialog = new GwtDatabaseDialog(connService, databaseTypeHelper,
          GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", new DatabaseDialogListener() {//$NON-NLS-1$
            
            @Override
            public void onDialogReady() {
              // TODO Auto-generated method stub
              
            }
            
            @Override
            public void onDialogCancel() {
              // TODO Auto-generated method stub
              
            }
            
            @Override
            public void onDialogAccept(IDatabaseConnection arg0) {
              // TODO Auto-generated method stub
              
            }
          });
    }
  }
  
  private native void notifyCallbackSuccess(JavaScriptObject callback, String domainId, String modelId, String modelName) /*-{
    callback.onFinish(domainId, modelId, modelName);
  }-*/;

  private native void notifyCallbackSuccess(JavaScriptObject callback, String domainId, String modelId) /*-{
    callback.onFinish(domainId, modelId);
  }-*/;

  private native void notifyCallbackSuccess(JavaScriptObject callback, Boolean value, WAQRTransport transport)/*-{
    callback.onFinish(value, transport);
  }-*/;

  private native void notifyCallbackSuccess(JavaScriptObject callback, Boolean value)/*-{
    callback.onFinish(value);
  }-*/;

  private native void notifyCallbackError(JavaScriptObject callback, String error)/*-{
    callback.onError(error);
  }-*/;

  private native void notifyCallbackCancel(JavaScriptObject callback)/*-{
    callback.onCancel();
  }-*/;

  private native void notifyCallbackReady(JavaScriptObject callback)/*-{
    callback.onReady();
  }-*/;

  private native void notifyDialogCallbackSuccess(JavaScriptObject callback, Object value)/*-{
    callback.onOk(value);
  }-*/;

  private native void notifyDialogCallbackCancel(JavaScriptObject callback)/*-{
    callback.onCancel();
  }-*/;

  private native void notifyDialogCallbackError(JavaScriptObject callback, String error)/*-{
    callback.onError(error);
  }-*/;

}