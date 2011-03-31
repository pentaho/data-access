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
 * Created June 2, 2009
 * @author mlowery
 */
package org.pentaho.platform.dataaccess.datasource.ui.selectdialog;


import org.pentaho.gwt.widgets.client.dialogs.GlassPane;
import org.pentaho.gwt.widgets.client.dialogs.GlassPaneNativeListener;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;

public class GwtDatasourceSelectionDialogEntryPoint implements EntryPoint {

  private GwtDatasourceSelectionDialog selectDialog;
  private GwtDatasourceManageDialog manageDialog;
  private GwtDatasourceSelectionDialog genericDialog;

  private EmbeddedWizard editor;

  private IXulAsyncDatasourceService datasourceService;

  private IXulAsyncConnectionService connectionService;

  private boolean asyncConstructorDone = false;
  
  public void onModuleLoad() {
    datasourceService = new DatasourceServiceGwtImpl();
    connectionService = new ConnectionServiceGwtImpl();
    setupNativeHooks(this);
  }

  public native void setupNativeHooks(final GwtDatasourceSelectionDialogEntryPoint d) /*-{
    $wnd.showDatasourceSelectionDialog = function(callback) {
      d.@org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialogEntryPoint::show(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)("true", callback);
    }
    $wnd.showDatasourceManageDialog = function(callback) {
      d.@org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialogEntryPoint::show(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)("false", callback);
    }

    $wnd.addDataAccessGlassPaneListener = function(callback) {
      d.@org.pentaho.platform.dataaccess.datasource.ui.selectdialog.GwtDatasourceSelectionDialogEntryPoint::addGlassPaneListener(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }

  }-*/;
  

  @SuppressWarnings("unused")
  private void addGlassPaneListener(JavaScriptObject obj) {
    GlassPane.getInstance().addGlassPaneListener(new GlassPaneNativeListener(obj));
  }

  @SuppressWarnings("unused")
  private void show(final String selectDatasource, final JavaScriptObject callback) {
    final boolean selectDs = Boolean.valueOf(selectDatasource);

    final DialogListener<Domain> wizardListener = new DialogListener<Domain>(){
      public void onDialogCancel() {
//        editor.removeDialogListener(this);
        notifyCallbackCancel(callback);
      }
      public void onDialogAccept(final Domain domain) {
//        editor.removeDialogListener(this);
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, domain.getId(), domain.getLogicalModels().get(0).getId());
      }

      public void onDialogReady() {
        notifyCallbackReady(callback);
      }
    };

    final DialogListener<LogicalModelSummary> listener = new DialogListener<LogicalModelSummary>(){
      public void onDialogCancel() {
        asyncConstructorDone = false;
      }

      public void onDialogAccept(final LogicalModelSummary logicalModelSummary) {
        asyncConstructorDone = false;
      }

      public void onDialogReady() {
      }
    };

    if(editor == null){
      editor = new EmbeddedWizard(datasourceService, connectionService, false);
      editor.init(new AsyncConstructorListener<EmbeddedWizard>() {
        public void asyncConstructorDone(EmbeddedWizard source) {
          source.addDialogListener(wizardListener);
          showDialog(selectDs, listener);
        }
      });
    } else {
      editor.addDialogListener(wizardListener);
      showDialog(selectDs, listener);
    }
  }

  private void showDialog(final boolean selectDs, final DialogListener<LogicalModelSummary> listener) {
    if (selectDs) {
      // selection dialog
      if (selectDialog == null) {

        final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener = getSelectionDialogListener(listener);
        selectDialog = new GwtDatasourceSelectionDialog(datasourceService, editor, constructorListener);

      } else {
        selectDialog.showDialog();
      }

    } else {
      // manage dialog
      if (manageDialog == null) {

        final AsyncConstructorListener<GwtDatasourceSelectionDialog> constructorListener = getSelectionDialogListener(listener);
        manageDialog = new GwtDatasourceManageDialog(datasourceService, editor, constructorListener);
      } else {
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

  private native void notifyCallbackSuccess(JavaScriptObject callback, String domainId, String modelId) /*-{
    callback.onFinish(domainId, modelId);
  }-*/;

  private native void notifyCallbackCancel(JavaScriptObject callback) /*-{
    callback.onCancel();
  }-*/;
  
  private native void notifyCallbackReady(JavaScriptObject callback) /*-{
  callback.onReady();
}-*/;

}
