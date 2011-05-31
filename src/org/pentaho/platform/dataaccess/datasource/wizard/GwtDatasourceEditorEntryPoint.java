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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard;


import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.modeler.ModelerDialog;
import org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceManageDialog;
import org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.util.DialogController.DialogListener;
import org.pentaho.ui.xul.util.XulDialogCallback;

/**
 * Creates the singleton datasource wizard and sets up native JavaScript functions to show the wizard.
 */
public class GwtDatasourceEditorEntryPoint implements EntryPoint {

  private EmbeddedWizard wizard;
  // TODO: make this lazily loaded when the modelerMessages issue is fixed
  private ModelerDialog modeler;
  private IXulAsyncDatasourceService datasourceService;
  private IXulAsyncConnectionService connectionService;
  private ICsvDatasourceServiceAsync csvService;
  private GwtDatasourceSelectionDialog gwtDatasourceSelectionDialog;
  private EmbeddedWizard gwtDatasourceEditor;
  private GwtDatasourceManageDialog manageDialog;
  private GwtDatasourceSelectionDialog selectDialog ;
  private boolean asyncConstructorDone;

  public void onModuleLoad() {

    datasourceService = new DatasourceServiceGwtImpl();
    // only init the app if the user has permissions
    datasourceService.hasPermission(new XulServiceCallback<Boolean>() {
      public void error(String message, Throwable error) {
        initDashboardButtons(false);
      }
      public void success(Boolean retVal) {
        if (retVal) {
          connectionService = new ConnectionServiceGwtImpl();
          csvService =  (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
          setupNativeHooks(GwtDatasourceEditorEntryPoint.this);
        }
        initDashboardButtons(retVal);
      }
    });
  }
  
  public native void initDashboardButtons(boolean val) /*-{
    $wnd.initDataAccess(val);
  }-*/;


  private native void setupNativeHooks(GwtDatasourceEditorEntryPoint wizard)/*-{
    if(!$wnd.pho){
      $wnd.pho = {};
    }
    $wnd.pho.openDatasourceEditor= function(callback) {
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showWizard(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
    $wnd.pho.openEditDatasourceEditor= function(domainId, modelId, callback, perspective) {
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showWizardEdit(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(domainId, modelId, perspective, callback);
    }
    $wnd.pho.deleteModel=function(domainId, modelName, callback) {
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::deleteLogicalModel(Ljava/lang/String;Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(domainId, modelName, callback);
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

    $wnd.addDataAccessGlassPaneListener = function(callback) {
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::addGlassPaneListener(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }

    $wnd.pho.showDatasourceSelectionDialog = function(callback) {
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showSelectionDialog(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)("true", callback);
    }
    $wnd.pho.showDatasourceManageDialog = function(callback) {
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceEditorEntryPoint::showSelectionDialog(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)("false", callback);
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

  /**
   * Entry-point from Javascript, responds to provided callback with the following:
   *
   *    onOk(String JSON, String mqlString);
   *    onCancel();
   *    onError(String errorMessage);
   *
   * @param callback
   *
   */
  private void showWizard(final JavaScriptObject callback) {

    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
        wizard.removeDialogListener(this);
        notifyCallbackCancel(callback);
      }
      public void onDialogAccept(final Domain domain) {
        wizard.removeDialogListener(this);
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
      }
      public void onDialogReady() {
        notifyCallbackReady(callback);
      }
    };

    if(wizard == null){
      wizard = new EmbeddedWizard(false);
      wizard.setDatasourceService(datasourceService);
      wizard.setConnectionService(connectionService);
      wizard.setCsvDatasourceService(csvService);
      wizard.init(new AsyncConstructorListener<EmbeddedWizard>() {
        @Override
        public void asyncConstructorDone(EmbeddedWizard source) {
          wizard.addDialogListener(listener);
          wizard.showDialog();
        }
      });
    } else {
      wizard.addDialogListener(listener);
      wizard.showDialog();
    }

  }

  private void showWizardEdit(final String domainId, final String modelId, final JavaScriptObject callback) {
    showWizardEdit(domainId, modelId, ModelerPerspective.REPORTING.name(), callback);
  }
  /**
   * edit entry-point from Javascript, responds to provided callback with the following:
   *
   *    onOk(String JSON, String mqlString);
   *    onCancel();
   *    onError(String errorMessage);
   *
   * @param callback
   *
   */
  private void showWizardEdit(final String domainId, final String modelId, final String perspective, final JavaScriptObject callback) {
    final String modelPerspective;
    if (perspective == null) {
      modelPerspective = ModelerPerspective.REPORTING.name();
    } else {
      modelPerspective = perspective;
    }
    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
        modeler.removeDialogListener(this);
        notifyCallbackCancel(callback);
      }
      public void onDialogAccept(final Domain domain) {
        modeler.removeDialogListener(this);
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
      }
      public void onDialogReady() {
        notifyCallbackReady(callback);
      }
    };

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


  /**
   * Deletes the selected model
   *
   *    onOk(Boolean value);
   *    onCancel();
   *    onError(String errorMessage);
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
  private void showSelectionDialog(final String selectDatasource, final JavaScriptObject callback) {
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
    };

    if(wizard == null){
      wizard = new EmbeddedWizard(false);

      wizard.setDatasourceService(datasourceService);
      wizard.setConnectionService(connectionService);
      wizard.setCsvDatasourceService(csvService);
      wizard.init(new AsyncConstructorListener<EmbeddedWizard>() {
        public void asyncConstructorDone(EmbeddedWizard source) {
          showSelectionDialog(selectDs, listener);
        }
      });
    } else {
      showSelectionDialog(selectDs, listener);
    }
  }

  private void showSelectionDialog(final boolean selectDs, final DialogListener<LogicalModelSummary> listener) {
    if (selectDs) {
      // selection dialog
      if (selectDialog == null) {

        final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener = getSelectionDialogListener(listener);
        selectDialog = new GwtDatasourceSelectionDialog(datasourceService, wizard, constructorListener);

      } else {
        selectDialog.addDialogListener(listener);
        selectDialog.reset();
        selectDialog.showDialog();
      }

    } else {
      // manage dialog
      if (manageDialog == null) {

        final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener = getSelectionDialogListener(listener);
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
  private native void notifyCallbackSuccess(JavaScriptObject callback, String domainId,
    String modelId, String modelName) /*-{
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
