package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.platform.datasource.Datasource;
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
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class DatasourceServiceManagerGwtImpl implements IXulAsyncDatasourceServiceManager{

  
  String getAllURL = getWebAppRoot() + "plugin/data-access/api/datasource/ids"; //$NON-NLS-1$
  
  String isAdminURL = getWebAppRoot() + "api/repo/files/canAdminister"; //$NON-NLS-1$
  
  String getTypesURL = getWebAppRoot() + "plugin/data-access/api/datasource/types"; //$NON-NLS-1$
  
  String getUIURL = getWebAppRoot() + "plugin/data-access/api/datasource/";//$NON-NLS-1$
  
  String UIUrlFragment = "/editor";  //$NON-NLS-1$
  
  String datasourceUrl = getWebAppRoot() + "plugin/data-access/api/datasource/{name}:{type}?overwrite={overwrite}";//$NON-NLS-1$
  
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
  @Override
  public void getTypes(final XulServiceCallback<List<String>> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, getTypesURL);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              xulCallback.error(exception.getLocalizedMessage(), exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                List<String> types = new ArrayList<String>();
                Document document = (Document) XMLParser.parse(response.getText());
                Element element = document.getDocumentElement();
                Node node = element.getFirstChild();
                boolean done = false;
                do {
                    try {
                    types.add(getNodeValueByTagName(node, "Item"));
                    node = (node.getNextSibling() != null) ? node.getNextSibling() : null;
  
                    if(node == null) {
                      done = true;
                    }
                  } catch(Exception e) {
                    done = true;
                  }
                } while(!done);
                callback.onSuccess(types);
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
  public void getNewUI(final String datasourceType, final XulServiceCallback<String> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, getUIURL + datasourceType + UIUrlFragment);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              xulCallback.error(exception.getLocalizedMessage(), exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(response.getText());
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
  @Override
  public void getEditUI(final String datasourceType, final String datasourceName, final XulServiceCallback<String> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, getUIURL + datasourceName + ":" + datasourceType + UIUrlFragment);
        try {
          requestBuilder.sendRequest(null, new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              xulCallback.error(exception.getLocalizedMessage(), exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(response.getText());
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
  @Override
  public void add(final Datasource datasource, final boolean overwrite, final XulServiceCallback<String> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(final AsyncCallback callback) {
        IDatasourceInfo info = datasource.getDatasourceInfo();
        String id = info.getId();
        String type = info.getType();
        
        datasourceUrl = datasourceUrl.replaceAll("{type}", type);
        datasourceUrl = datasourceUrl.replaceAll("{name}", id);
        datasourceUrl = datasourceUrl.replaceAll("{overwrite}", Boolean.toString(overwrite));
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.PUT, datasourceUrl);
        requestBuilder.setHeader("accept", "text/*");
        requestBuilder.setHeader("Content-Type", "application/xml");
        try {
          requestBuilder.sendRequest(datasource.getDatasource(), new RequestCallback() {
            @Override
            public void onError(Request request, Throwable exception) {
              xulCallback.error(exception.getLocalizedMessage(), exception);
            }

            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == Response.SC_OK) {
                callback.onSuccess(response.getText());
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
  @Override
  public void remove(String id, XulServiceCallback<String> callback) {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void update(Datasource datasource, XulServiceCallback<String> callback) {
    // TODO Auto-generated method stub
    
  }
  
}
