package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DatasourceServiceManagerGwtImpl implements IXulAsyncDatasourceServiceManager{

  String getAllURL = getWebAppRoot() + "plugin/data-access/api/datasource/listIds"; //$NON-NLS-1$
  
  String isAdminURL = getWebAppRoot() + "api/repo/files/canAdminister"; //$NON-NLS-1$
  @Override
  public void getAll(final XulServiceCallback<List<IDatasourceInfo>> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, getAllURL);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              xulCallback.error(exception.getLocalizedMessage(), exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                final XMLToDatasourceInfoConverter converter = new XMLToDatasourceInfoConverter(response.getText());
                callback.onSuccess(converter.convert());
              }
            }

          });
        } catch (RequestException e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }        
      }
    }, new AsyncCallback<List<IDatasourceInfo>>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(List<IDatasourceInfo> arg0) {
        xulCallback.success(arg0);
      }

    });
  }
  @Override
  public void isAdmin(final XulServiceCallback<Boolean> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, isAdminURL);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              xulCallback.error(exception.getLocalizedMessage(), exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(Boolean.parseBoolean(response.getText()));
              }
            }

          });
        } catch (RequestException e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }        
      }
    }, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(Boolean arg0) {
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
}
