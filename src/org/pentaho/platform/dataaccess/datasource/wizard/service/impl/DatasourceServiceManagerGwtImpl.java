package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

public class DatasourceServiceManagerGwtImpl implements IXulAsyncDatasourceServiceManager{

  
  String getAnalysisDatasourceIdsURL = getWebAppRoot() + "plugin/data-access/api/datasource/analysis/ids"; //$NON-NLS-1$
  String getMetadataDatasourceIdsURL = getWebAppRoot() + "plugin/data-access/api/datasource/metadata/ids"; //$NON-NLS-1$
  String getDSWDatasourceIdsURL = getWebAppRoot() + "plugin/data-access/api/datasource/dsw/ids"; //$NON-NLS-1$
  
  String isAdminURL = getWebAppRoot() + "api/repo/files/canAdminister"; //$NON-NLS-1$
  
  
  @Override
  public void getAnalysisDatasourceIds(final XulServiceCallback<List<String>> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, getAnalysisDatasourceIdsURL);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              callback.onFailure(exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(convertReponseToList(response));
              }
            }

          });
        } catch (RequestException e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }        
      }
    }, new AsyncCallback<List<String>>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(List<String> arg0) {
        xulCallback.success(arg0);
      }

    });
  }
  
  @Override
  public void getMetadataDatasourceIds(final XulServiceCallback<List<String>> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, getMetadataDatasourceIdsURL);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              callback.onFailure(exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(convertReponseToList(response));
              }
            }

          });
        } catch (RequestException e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }        
      }
    }, new AsyncCallback<List<String>>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(List<String> arg0) {
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
              callback.onFailure(exception);
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

  private List<String> convertReponseToList(Response response) {
    List<String> dataList = new ArrayList<String>();
    Document document = (Document) XMLParser.parse(response.getText());
    Element element = document.getDocumentElement();
    Node node = element.getFirstChild();
    boolean done = false;
    do {
        try {
        dataList.add(getNodeValueByTagName(node, "Item"));
        node = (node.getNextSibling() != null) ? node.getNextSibling() : null;

        if(node == null) {
          done = true;
        }
      } catch(Exception e) {
        done = true;
      }
    } while(!done);
    
    return dataList;
  }
  
  /*
   * Get Node Value of the element matching the tag name
   */
  private String getNodeValueByTagName(Node node, String tagName) {
    if(node != null && node.getFirstChild() != null) {
      return node.getFirstChild().getNodeValue();
    } else {
      return null;
    }
  }

  @Override
  public void getDSWDatasourceIds(final XulServiceCallback<List<String>> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, getDSWDatasourceIdsURL);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              callback.onFailure(exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(convertReponseToList(response));
              }
            }

          });
        } catch (RequestException e) {
          xulCallback.error(e.getLocalizedMessage(), e);
        }        
      }
    }, new AsyncCallback<List<String>>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);
      }

      public void onSuccess(List<String> arg0) {
        xulCallback.success(arg0);
      }

    });
  }

}
