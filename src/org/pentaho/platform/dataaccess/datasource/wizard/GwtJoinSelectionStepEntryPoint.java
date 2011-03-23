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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 * 
 * @author Ezequiel Cuellar
 */
package org.pentaho.platform.dataaccess.datasource.wizard;


import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.jsni.WAQRTransport;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * Creates the singleton datasource wizard and sets up native JavaScript functions to show the wizard.
 */
public class GwtJoinSelectionStepEntryPoint implements EntryPoint {

  private JoinSelectionStepController joinSelectionStepController;
  private IXulAsyncDatasourceService datasourceService;
  private IXulAsyncConnectionService connectionService;

  public void onModuleLoad() {

    datasourceService = new DatasourceServiceGwtImpl();
    datasourceService.hasPermission(new XulServiceCallback<Boolean>() {
      public void error(String message, Throwable error) {
      }
      public void success(Boolean retVal) {
        if (retVal) {
          connectionService = new ConnectionServiceGwtImpl();
          connectionService.getConnectionByName("SampleData", new XulServiceCallback<IConnection>() {
              public void error(String message, Throwable error) {
              }
              public void success(IConnection iConnection) {
            	joinSelectionStepController = new JoinSelectionStepController(iConnection);
              }
            });
          setupNativeHooks(GwtJoinSelectionStepEntryPoint.this);
        }
      }
    });
  }

  private native void setupNativeHooks(GwtJoinSelectionStepEntryPoint wizard)/*-{
    $wnd.showJoinSelectionStep= function(callback) {
      wizard.@org.pentaho.platform.dataaccess.datasource.wizard.GwtJoinSelectionStepEntryPoint::show(Lcom/google/gwt/core/client/JavaScriptObject;)(callback);
    }
  }-*/;

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
  private void show(final JavaScriptObject callback) {
    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
        notifyCallbackCancel(callback);
      }
      public void onDialogAccept(final Domain domain) {
        WAQRTransport transport = WAQRTransport.createFromMetadata(domain);
        notifyCallbackSuccess(callback, true, transport);
      }
      public void onDialogReady() {
        notifyCallbackReady(callback);
      }
    };
    joinSelectionStepController.show();
  }
  
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
