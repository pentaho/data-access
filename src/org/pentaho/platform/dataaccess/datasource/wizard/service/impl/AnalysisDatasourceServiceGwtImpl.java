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
 * Created December 08, 2011
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.platform.dataaccess.datasource.ui.importing.GwtImportDialog;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("all")

public class AnalysisDatasourceServiceGwtImpl {

  String datasourceUrl = getWebAppRoot()
      + "plugin/data-access/api/mondrian/putSchema?analysisFile={analysisFile}&databaseConnection={databaseConnection}";//$NON-NLS-1$
  @Deprecated
  public void importAnalysisDatasource(final String analysisFile, final String databaseConnection,
      final String parameters, final XulServiceCallback<String> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {

        datasourceUrl = datasourceUrl.replaceAll("{analysisFile}", analysisFile);
        datasourceUrl = datasourceUrl.replaceAll("{databaseConnection}", databaseConnection);

        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.PUT, datasourceUrl);
        requestBuilder.setHeader("accept", "text/*");
        requestBuilder.setHeader("Content-Type", "text/plain");
        try {
          requestBuilder.sendRequest(parameters, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              xulCallback.error(exception.getLocalizedMessage(), exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(response.getText());
              } else {
                // if (response.getStatusCode() == Response.SC_INTERNAL_SERVER_ERROR) {
                xulCallback.error(response.getText(), new Exception(response.getText()));
              }
            }

          });
        } catch (RequestException e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }
      }
    }, new AsyncCallback<String>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(String arg0) {
        xulCallback.success(arg0);
      }

    });
  }

  public native String getWebAppRoot()/*-{
                                      if($wnd.CONTEXT_PATH){
                                      return $wnd.CONTEXT_PATH;
                                      }
                                      return "";
                                      }-*/;

  public void importAnalysisDatasource(final String uploadedFile, final String name, final String parameters,
      final GwtImportDialog importDialog, final XulServiceCallback<String> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        try {
         // importDialog.getAnalysisImportDialogController().removeHiddenPanels(); 
        //  importDialog.getAnalysisImportDialogController().buildAndSetParameters();
         // importDialog.getAnalysisImportDialogController().getFormPanel().submit();
           callback.onSuccess("SUCCESS");
        } catch (Exception e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }
      }
    }, new AsyncCallback<String>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(String arg0) {
        xulCallback.success(arg0);
      }

    });

  }

}
