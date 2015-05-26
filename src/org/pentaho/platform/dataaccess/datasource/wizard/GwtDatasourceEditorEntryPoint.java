/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
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
import org.pentaho.platform.dataaccess.datasource.ui.importing.AnalysisImportDialogController;
import org.pentaho.platform.dataaccess.datasource.ui.importing.AnalysisImportDialogModel;
import org.pentaho.platform.dataaccess.datasource.ui.importing.GwtImportDialog;
import org.pentaho.platform.dataaccess.datasource.ui.importing.IOverwritableController;
import org.pentaho.platform.dataaccess.datasource.ui.importing.MetadataImportDialogController;
import org.pentaho.platform.dataaccess.datasource.ui.importing.MetadataImportDialogModel;
import org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceManageDialog;
import org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialog;
import org.pentaho.platform.dataaccess.datasource.ui.service.DSWUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.JSUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.JdbcDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.MetadataUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.MondrianUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.UIDatasourceServiceManager;
import org.pentaho.platform.dataaccess.datasource.utils.WaitPopupUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.AnalysisDatasourceServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceManagerGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MetadataDatasourceServiceGwtImpl;
import org.pentaho.ui.database.event.IConnectionAutoBeanFactory;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
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
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

/**
* Creates the singleton datasource wizard and sets up native JavaScript functions to show the wizard.
*/
public class GwtDatasourceEditorEntryPoint implements EntryPoint {

  private static final String OVERWRITE_8 = "8";

  private static final String OVERWRITE__CATALOG_7 = "7";

  private static final String SUCCESS_3 = "3";

  private EmbeddedWizard wizard;

  // TODO: make this lazily loaded when the modelerMessages issue is fixed
  private ModelerDialog modeler;

  private IXulAsyncDSWDatasourceService datasourceService;

  //  private IXulAsyncConnectionService connectionService;
  private IModelerServiceAsync modelerService;

  private IXulAsyncDatasourceServiceManager datasourceServiceManager;

  private ICsvDatasourceServiceAsync csvService;

  private GwtDatasourceSelectionDialog gwtDatasourceSelectionDialog;

  private GwtDatabaseDialog gwtDatabaseDialog;

  private DatabaseTypeHelper databaseTypeHelper;

  private DatabaseConnectionConverter databaseConnectionConverter;

  private EmbeddedWizard gwtDatasourceEditor;

  private GwtDatasourceManageDialog manageDialog;

  private GwtDatasourceSelectionDialog selectDialog;

  private GwtDatasourceAdminDialog adminDialog;

  private GwtImportDialog importDialog;

  private boolean asyncConstructorDone;

  private boolean hasPermissions;

  private boolean isAdmin;

  private Timer timer;

  //  private GwtXulAsyncDatabaseConnectionService connService = new GwtXulAsyncDatabaseConnectionService();
  private GwtXulAsyncDatabaseDialectService dialectService = new GwtXulAsyncDatabaseDialectService();

  private IConnectionAutoBeanFactory connectionAutoBeanFactory = GWT.create(IConnectionAutoBeanFactory.class);

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
        modelerService.gwtWorkaround(bogo, new XulServiceCallback<BogoPojo>() {
          public void success(BogoPojo retVal) {

          }

          public void error(String message, Throwable error) {

          }
        });

        // only init the app if the user has permissions

          final String url = GWT.getHostPageBaseURL() + "plugin/data-access/api/permissions/hasDataAccess"; //$NON-NLS-1$

          RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
          builder.setHeader("accept", "application/json");

          try {
              builder.sendRequest(null, new RequestCallback() {

                  public void onError(Request request, Throwable exception) {
                      setupStandardNativeHooks(GwtDatasourceEditorEntryPoint.this);
                      initDashboardButtons(false);
                  }

                  public void onResponseReceived(Request request, Response response) {
                      hasPermissions = new Boolean(response.getText());
                      setupStandardNativeHooks(GwtDatasourceEditorEntryPoint.this);
                      if (hasPermissions) {
                          csvService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
                          setupPrivilegedNativeHooks(GwtDatasourceEditorEntryPoint.this);
                          loadOverlay("startup.dataaccess");
                      }
                      initDashboardButtons(hasPermissions);

                  }
              });
          } catch (RequestException e) {

          }
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

    UIDatasourceServiceManager manager = UIDatasourceServiceManager.getInstance();
    manager.registerService(new JdbcDatasourceService());
    manager.registerService(new MondrianUIDatasourceService(datasourceServiceManager));
    manager.registerService(new MetadataUIDatasourceService(datasourceServiceManager));
    manager.registerService(new DSWUIDatasourceService(datasourceServiceManager));
    manager.getIds(null);

  }

  private native static void loadOverlay(String overlayId) /*-{
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

                                                                                      $wnd.pho.registerUIDatasourceService = function(jsDatasourceService) {
                                                                                      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::registerDatasourceService(Lcom/google/gwt/core/client/JavaScriptObject;)(jsDatasourceService);
                                                                                      }
                                                                                      }-*/;

  private void registerDatasourceService(JavaScriptObject javascriptObject) {
    JSUIDatasourceService datasourceService = new JSUIDatasourceService(javascriptObject);
    UIDatasourceServiceManager.getInstance().registerService(datasourceService);
  }

  public void showConfirm(final JavaScriptObject callback, String message, String title, String okText,
      String cancelText) throws XulException {
    XulConfirmBox confirm = (XulConfirmBox) wizard.getMainWizardContainer().getDocumentRoot()
        .createElement("confirmbox");
    confirm.setTitle(title);
    confirm.setMessage(message);
    confirm.setAcceptLabel(okText);
    confirm.setCancelLabel(cancelText);
    confirm.addDialogCallback(new XulDialogCallback<String>() {
      public void onClose(XulComponent component, Status status, String value) {
        if (status == XulDialogCallback.Status.ACCEPT) {
          notifyDialogCallbackSuccess(callback, value);
        }
      }

      public void onError(XulComponent component, Throwable err) {
        notifyDialogCallbackError(callback, err.getMessage());
      }
    });
    confirm.open();
  }

  /**
   * used to handle the overwrite in repository message
   * @param parentFormPanel
   * @param message
   * @param controller
   */
  public void overwriteFileDialog(final FormPanel parentFormPanel, String message,
      final IOverwritableController controller) {
    //Experiment
    XulConfirmBox confirm = null;
    try {
      confirm = (XulConfirmBox) wizard.getMainWizardContainer().getDocumentRoot().createElement("confirmbox");
    } catch (XulException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    confirm.setTitle("Confirmation");
    confirm.setMessage(message);
    confirm.setAcceptLabel("Ok");
    confirm.setCancelLabel("Cancel");
    confirm.addDialogCallback(new XulDialogCallback<String>() {
      public void onClose(XulComponent component, Status status, String value) {
        if (status == XulDialogCallback.Status.ACCEPT) {
          controller.setOverwrite(true);
          controller.removeHiddenPanels();
          controller.buildAndSetParameters();
          parentFormPanel.submit();
        }
      }

      public void onError(XulComponent component, Throwable err) {
        return;
      }
    });
    confirm.open();
  }

  private void addGlassPaneListener(JavaScriptObject obj) {
    GlassPane.getInstance().addGlassPaneListener(new GlassPaneNativeListener(obj));
  }

  public void showWizard(final boolean reportingOnlyValid, final DialogListener<Domain> listener) {

    if (wizard == null) {
      wizard = new EmbeddedWizard(false);
      wizard.setDatasourceService(datasourceService);
      //      wizard.setConnectionService(connectionService);
      wizard.setCsvDatasourceService(csvService);
      wizard.setReportingOnlyValid(reportingOnlyValid);
      wizard.init(new AsyncConstructorListener<EmbeddedWizard>() {
        @Override
        public void asyncConstructorDone(EmbeddedWizard source) {
          wizard.addDialogListener(listener);
          wizard.showDialog();
        }
      });
    } else {
      wizard.addDialogListener(listener);
      wizard.setReportingOnlyValid(reportingOnlyValid);
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
  private void showWizard(final boolean reportingOnlyValid, final JavaScriptObject callback) {

    final DialogListener<Domain> listener = new DialogListener<Domain>() {
      public void onDialogCancel() {
        wizard.removeDialogListener(this);
        if (callback != null) {
          notifyCallbackCancel(callback);
        }
      }

      public void onDialogAccept(final Domain domain) {
        MessageHandler.getInstance().closeWaitingDialog();
        wizard.removeDialogListener(this);
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
        notifyDialogCallbackSuccess(callback, domain.getId());
      }

      public void onDialogReady() {
        if (callback != null) {
          notifyCallbackReady(callback);
        }
      }

      @Override
      public void onDialogError(String errorMessage) {
        notifyCallbackError(callback, errorMessage);
      }
    };
    showWizard(reportingOnlyValid, listener);

  }

  public void showWizardEdit(final String domainId, final String modelId, boolean reportingOnlyValid,
      final DialogListener<Domain> listener) {
    showWizardEdit(domainId, modelId, ModelerPerspective.ANALYSIS.name(), reportingOnlyValid, listener);
  }

  public void showWizardEdit(final String domainId, final String modelId, final String perspective,
      boolean reportingOnlyValid, final DialogListener<Domain> listener) {
    final String modelPerspective;
    if (perspective == null) {
      modelPerspective = ModelerPerspective.ANALYSIS.name();
    } else {
      modelPerspective = perspective;
    }

    modeler = ModelerDialog.getInstance(wizard, new AsyncConstructorListener<ModelerDialog>() {
      public void asyncConstructorDone(ModelerDialog dialog) {

        ModelerPerspective modelerPerspective;
        try {
          modelerPerspective = ModelerPerspective.valueOf(modelPerspective);
        } catch (IllegalArgumentException e) {
          modelerPerspective = ModelerPerspective.ANALYSIS;
        }
        dialog.addDialogListener(listener);
        dialog.showDialog(domainId, modelId, modelerPerspective);
      }
    });

  }

  private void showWizardEdit(final String domainId, final String modelId, boolean reportingOnlyValid,
      final JavaScriptObject callback) {
    showWizardEdit(domainId, modelId, ModelerPerspective.ANALYSIS.name(), reportingOnlyValid, callback);
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
  private void showWizardEdit(final String domainId, final String modelId, final String perspective,
      boolean reportingOnlyValid, final JavaScriptObject callback) {
    final DialogListener<Domain> listener = new DialogListener<Domain>() {
      public void onDialogCancel() {
        modeler.removeDialogListener(this);
        if (callback != null) {
          notifyCallbackCancel(callback);
        }
      }

      public void onDialogAccept(final Domain domain) {
        modeler.removeDialogListener(this);
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
      }

      public void onDialogReady() {
        if (callback != null) {
          notifyCallbackReady(callback);
        }
      }

      @Override
      public void onDialogError(String errorMessage) {
        notifyCallbackError(callback, errorMessage);
      }
    };
    showWizardEdit(domainId, modelId, perspective, reportingOnlyValid, listener);
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
    datasourceService.deleteLogicalModel(domainId, modelName, new XulServiceCallback<Boolean>() {
      public void success(Boolean value) {
        notifyCallbackSuccess(callback, value);
      }

      public void error(String s, Throwable throwable) {
        notifyCallbackError(callback, throwable.getMessage());
      }
    });
  }

  private void showAdminDialog(final DialogListener<IDatasourceInfo> listener) {
    if (adminDialog == null) {
      final AsyncConstructorListener<GwtDatasourceAdminDialog> constructorListener = getAdminDialogListener(listener);
      asyncConstructorDone = false;
      adminDialog = new GwtDatasourceAdminDialog(datasourceServiceManager, modelerService, datasourceService, this,
          constructorListener);
    } else {
      adminDialog.addDialogListener(listener);
      adminDialog.showDialog();
    }
  }

  private void showAdminDialog(final JavaScriptObject callback) {

    final DialogListener<IDatasourceInfo> listener = new DialogListener<IDatasourceInfo>() {
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

  private void showSelectionOrAdminDialog(final String context, final String selectDatasource,
      final JavaScriptObject callback, final DialogListener<LogicalModelSummary> listener) {
    if ("manage".equals(context) && (isAdmin || hasPermissions)) {
      showAdminDialog(callback);
    } else {
      showSelectionDialog(context, Boolean.valueOf(selectDatasource), listener);
    }
  }

  private void showSelectionDialog(final String context, final String selectDatasource, final JavaScriptObject callback) {
    final DialogListener<LogicalModelSummary> listener = new DialogListener<LogicalModelSummary>() {
      public void onDialogCancel() {
        selectDialog.removeDialogListener(this);
        asyncConstructorDone = false;
        notifyCallbackCancel(callback);
      }

      public void onDialogAccept(final LogicalModelSummary logicalModelSummary) {
        selectDialog.removeDialogListener(this);
        asyncConstructorDone = false;
        notifyCallbackSuccess(callback, logicalModelSummary.getDomainId(), logicalModelSummary.getModelId(),
            logicalModelSummary.getModelName());
      }

      public void onDialogReady() {
      }

      public void onDialogError(String errorMessage) {
        notifyDialogCallbackError(callback, errorMessage);
      }
    };

    if (wizard == null && this.hasPermissions) {
      WaitPopupUtil.showWaitPopup();

      wizard = new EmbeddedWizard(false);

      wizard.setDatasourceService(datasourceService);
      //      wizard.setConnectionService(connectionService);
      wizard.setCsvDatasourceService(csvService);
      wizard.init(new AsyncConstructorListener<EmbeddedWizard>() {
        public void asyncConstructorDone(EmbeddedWizard source) {
          showSelectionOrAdminDialog(context, selectDatasource, callback, listener);
          WaitPopupUtil.hideWaitPopup();
        }
      });
    } else {
      showSelectionOrAdminDialog(context, selectDatasource, callback, listener);
    }
  }

  private void showMetadataImportDialog(final JavaScriptObject callback) {
    final DialogListener<MetadataImportDialogModel> listener = new DialogListener<MetadataImportDialogModel>() {

      public void onDialogCancel() {
      }

      public void onDialogAccept(final MetadataImportDialogModel importDialogModel) {

        MetadataDatasourceServiceGwtImpl service = new MetadataDatasourceServiceGwtImpl();

        service.importMetadataDatasource(importDialogModel.getDomainId(), importDialogModel.getUploadedFile(),
            importDialogModel.getLocalizedBundleEntries(), new XulServiceCallback<String>() {

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
    showMetadataImportDialog(listener);
  }

  public void showMetadataImportDialog(final DialogListener listener) {
    final DialogListener<MetadataImportDialogModel> importDialoglistener = new DialogListener<MetadataImportDialogModel>() {

      public void onDialogCancel() {
        final MetadataImportDialogController controller = importDialog.getMetadataImportDialogController();
        final FormPanel analysisDataFormPanel = controller.getFormPanel();
        controller.removeHiddenPanels();
        controller.buildAndSetParameters();
        analysisDataFormPanel.removeFromParent();
        RootPanel.get().add(analysisDataFormPanel);
      }

      public void onDialogAccept(final MetadataImportDialogModel importDialogModel) {

        final MetadataImportDialogController controller = importDialog.getMetadataImportDialogController();
        final FormPanel metaDataFormPanel = controller.getFormPanel();
        controller.removeHiddenPanels();
        controller.buildAndSetParameters();
        metaDataFormPanel.removeFromParent();
        RootPanel.get().add(metaDataFormPanel);

        metaDataFormPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {

          @Override
          public void onSubmitComplete(SubmitCompleteEvent event) {
            String results = event.getResults();
            String message = controller.convertToNLSMessage(results, controller.getFileName());

            if (SUCCESS_3.equals(results)) {
              metaDataFormPanel.removeFromParent();
              listener.onDialogAccept(null);
            } else {
              if (OVERWRITE_8.equals(results)) {
                overwriteFileDialog(metaDataFormPanel, message, controller);
              } else {
                listener.onDialogError(message);
              }
            }
          }

        });
        metaDataFormPanel.submit();
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
        dialog.showMetadataImportDialog(importDialoglistener);
      }
    };

    if (importDialog == null) {
      importDialog = new GwtImportDialog(constructorListener);
    } else {
      importDialog.showMetadataImportDialog(importDialoglistener);
    }
  }

  private void showAnalysisImportDialog(final JavaScriptObject callback) {
    final DialogListener<AnalysisImportDialogModel> listener = new DialogListener<AnalysisImportDialogModel>() {

      public void onDialogCancel() {
      }

      public void onDialogAccept(final AnalysisImportDialogModel importDialogModel) {

        AnalysisDatasourceServiceGwtImpl service = new AnalysisDatasourceServiceGwtImpl();
        service.importAnalysisDatasource(importDialogModel.getUploadedFile(), importDialogModel.getConnection()
            .getName(), importDialogModel.getParameters(), new XulServiceCallback<String>() {

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

  public void showEditAnalysisDialog(final DialogListener listener, final IDatasourceInfo datasourceInfo) {
	  final DialogListener<AnalysisImportDialogModel> importDialoglistener = new DialogListener<AnalysisImportDialogModel>() {

	      public void onDialogCancel() {
	          final AnalysisImportDialogController controller = importDialog.getAnalysisImportDialogController();
	          final FormPanel analysisDataFormPanel = controller.getFormPanel();
	          controller.removeHiddenPanels();
	          controller.buildAndSetParameters();
	          analysisDataFormPanel.removeFromParent();
	          RootPanel.get().add(analysisDataFormPanel);
	      }

	      public void onDialogAccept(final AnalysisImportDialogModel importDialogModel) {
	          final AnalysisImportDialogController controller = importDialog.getAnalysisImportDialogController();
	          final FormPanel analysisDataFormPanel = controller.getFormPanel();
	          controller.removeHiddenPanels();
	          controller.buildAndSetParameters(true);
	          analysisDataFormPanel.removeFromParent();
	          RootPanel.get().add(analysisDataFormPanel);
	          analysisDataFormPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {

	            @Override
	            public void onSubmitComplete(SubmitCompleteEvent event) {
	              String results = event.getResults();
	              String message = controller.convertToNLSMessage(results, controller.getFileName());

	              if (!SUCCESS_3.equals(results)) {
	                if (OVERWRITE_8.equals(results) || OVERWRITE__CATALOG_7.equals(results)) {
	                  overwriteFileDialog(analysisDataFormPanel, message, controller);
	                } else {
	                  listener.onDialogError(message);
	                }
	              } else {
	                analysisDataFormPanel.removeFromParent();
	                listener.onDialogAccept(null);
	              }
	            }
	          });
	          analysisDataFormPanel.submit();
	      }

	      public void onDialogReady() {
	      }

	      public void onDialogError(String errorMessage) {
	    	  listener.onDialogError(errorMessage);
	      }
	  };

	  final AsyncConstructorListener<GwtImportDialog> constructorListener = new AsyncConstructorListener<GwtImportDialog>() {

	      public void asyncConstructorDone(GwtImportDialog dialog) {
	          dialog.showAnalysisImportDialog(importDialoglistener, datasourceInfo);
	      }
	  };

	  if (importDialog == null) {
	     importDialog = new GwtImportDialog(constructorListener);
	  } else {
	     importDialog.showAnalysisImportDialog(importDialoglistener, datasourceInfo);
	  }
  }

  public void showAnalysisImportDialog(final DialogListener listener) {
    final DialogListener<AnalysisImportDialogModel> importDialoglistener = new DialogListener<AnalysisImportDialogModel>() {

      public void onDialogCancel() {
          final AnalysisImportDialogController controller = importDialog.getAnalysisImportDialogController();
          final FormPanel analysisDataFormPanel = controller.getFormPanel();
          controller.removeHiddenPanels();
          controller.buildAndSetParameters();
          analysisDataFormPanel.removeFromParent();
          RootPanel.get().add(analysisDataFormPanel);
      }

      public void onDialogAccept(final AnalysisImportDialogModel importDialogModel) {
        final AnalysisImportDialogController controller = importDialog.getAnalysisImportDialogController();
        final FormPanel analysisDataFormPanel = controller.getFormPanel();
        controller.removeHiddenPanels();
        controller.buildAndSetParameters();
        analysisDataFormPanel.removeFromParent();
        RootPanel.get().add(analysisDataFormPanel);
        analysisDataFormPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {

          @Override
          public void onSubmitComplete(SubmitCompleteEvent event) {
            String results = event.getResults();
            String message = controller.convertToNLSMessage(results, controller.getFileName());

            if (!SUCCESS_3.equals(results)) {
              if (OVERWRITE_8.equals(results) || OVERWRITE__CATALOG_7.equals(results)) {
                overwriteFileDialog(analysisDataFormPanel, message, controller);
              } else {
                listener.onDialogError(message);
              }
            } else {
              analysisDataFormPanel.removeFromParent();
              listener.onDialogAccept(null);
            }
          }
        });
        analysisDataFormPanel.submit();
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

    if (importDialog == null) {
      importDialog = new GwtImportDialog(constructorListener);
    } else {
      importDialog.showAnalysisImportDialog(importDialoglistener);
    }
  }

  private void showSelectionDialog(final String context, final boolean selectDs,
      final DialogListener<LogicalModelSummary> listener) {
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

  private AsyncConstructorListener<GwtDatasourceSelectionDialog> getSelectionDialogListener(
      final DialogListener<LogicalModelSummary> listener) {
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

  private AsyncConstructorListener<GwtDatasourceAdminDialog> getAdminDialogListener(
      final DialogListener<IDatasourceInfo> listener) {
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

    final DialogListener<IDatabaseConnection> listener = new DialogListener<IDatabaseConnection>() {
      public void onDialogCancel() {
        if (callback != null) {
          notifyCallbackCancel(callback);
        }
      }

      public void onDialogAccept(final IDatabaseConnection connection) {
        if (callback != null) {
          notifyCallbackSuccess(callback, true);
        }
      }

      public void onDialogReady() {
        if (callback != null) {
          notifyCallbackCancel(callback);
        }
      }

      @Override
      public void onDialogError(String errorMessage) {
        if (callback != null) {
          notifyCallbackError(callback, errorMessage);
        }
      }
    };
    showDatabaseDialog(listener);
  }

  public void showDatabaseDialog(final DialogListener<IDatabaseConnection> listener) {
    ConnectionController connectionController = wizard.getConnectionController();
    connectionController.init();
    DatasourceModel datasourceModel = connectionController.getDatasourceModel();
    if (datasourceModel == null) {
      datasourceModel = new DatasourceModel();
    }
    connectionController.setDatasourceModel(datasourceModel);
    connectionController.showAddConnectionDialog(listener);
  }

  public void showEditDatabaseDialog(final DialogListener dialogListener, final String databaseName) {
    String cacheBuster = String.valueOf( new java.util.Date().getTime() );
    String url = ConnectionController.getServiceURL("get", new String[][] { { "name", databaseName }, { "ts", cacheBuster }  });
    RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
    builder.setHeader("Accept", "application/json");

    try {
      builder.sendRequest(null, new RequestCallback() {

        public void onError(Request request, Throwable exception) {
          Window.alert(exception.toString());
        }

        @SuppressWarnings("deprecation")
        public void onResponseReceived(Request request, Response response) {
          IDatabaseConnection conn = null;
          if (response.getStatusCode() == Response.SC_OK) {
            AutoBean<IDatabaseConnection> bean = AutoBeanCodex.decode(connectionAutoBeanFactory,
                IDatabaseConnection.class, response.getText());
            conn = bean.as();
          }

          ConnectionController connectionController = wizard.getConnectionController();
          connectionController.init();
          DatasourceModel datasourceModel = connectionController.getDatasourceModel();
          if (datasourceModel == null) {
            datasourceModel = new DatasourceModel();
          }
          datasourceModel.setSelectedRelationalConnection(conn);
          // This is important for edit mode of datasource model
          datasourceModel.setEditing(true);
          connectionController.setDatasourceModel(datasourceModel);
          connectionController.showEditConnectionDialog(dialogListener);
        }
      });
    } catch (Exception e) {
      Window.alert("Cannot edit datasource");
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
