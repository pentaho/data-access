package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncGenericDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class GenericDatasourceServiceManagerGwtImpl implements IXulAsyncGenericDatasourceServiceManager{


  String getAllURL = getWebAppRoot() + "api/datasourcemgr/datasource/ids"; //$NON-NLS-1$
  
  String isAdminURL = getWebAppRoot() + "api/repo/files/canAdminister"; //$NON-NLS-1$
  @Override
  public void getAll(final XulServiceCallback<List<IGenericDatasourceInfo>> xulCallback) {
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
                final XMLToGenericDatasourceInfoConverter converter = new XMLToGenericDatasourceInfoConverter(response.getText());
                callback.onSuccess(converter.convert());
              }
            }

          });
        } catch (RequestException e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }        
      }
    }, new AsyncCallback<List<IGenericDatasourceInfo>>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(List<IGenericDatasourceInfo> arg0) {
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
