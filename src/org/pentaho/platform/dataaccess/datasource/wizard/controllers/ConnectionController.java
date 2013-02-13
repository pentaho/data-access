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
package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.ConnectionDialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.ui.database.event.DatabaseDialogListener;
import org.pentaho.ui.database.event.IConnectionAutoBeanFactory;
import org.pentaho.ui.database.event.IDatabaseConnectionList;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseDialectService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

//TODO: move to the relational datasource package
public class ConnectionController extends AbstractXulEventHandler {

  //  private IXulAsyncConnectionService service;

  private List<ConnectionDialogListener> listeners = new ArrayList<ConnectionDialogListener>();

  private DatasourceModel datasourceModel;

  private XulDialog removeConfirmationDialog;

  private XulDialog saveConnectionConfirmationDialog;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  //  GwtXulAsyncDatabaseConnectionService connService = new GwtXulAsyncDatabaseConnectionService();

  GwtXulAsyncDatabaseDialectService dialectService = new GwtXulAsyncDatabaseDialectService();

  GwtDatabaseDialog databaseDialog;

  DatabaseTypeHelper databaseTypeHelper;

  IDatabaseConnection currentConnection;

//  DialogListener listener;

  DatabaseConnectionSetter connectionSetter;

  protected IConnectionAutoBeanFactory connectionAutoBeanFactory;
  
  public ConnectionController() {
    connectionAutoBeanFactory = GWT.create(IConnectionAutoBeanFactory.class);
  }

  @Bindable
  public void init() {
    XulServiceCallback<List<IDatabaseType>> callback = new XulServiceCallback<List<IDatabaseType>>() {
      public void error(String message, Throwable error) {
        error.printStackTrace();
      }

      public void success(List<IDatabaseType> retVal) {
        databaseTypeHelper = new DatabaseTypeHelper(retVal);
      }
    };
    dialectService.getDatabaseTypes(callback);
    saveConnectionConfirmationDialog = (XulDialog) document.getElementById("saveConnectionConfirmationDialog"); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    removeConfirmationDialog = (XulDialog) document.getElementById("removeConfirmationDialog"); //$NON-NLS-1$
  }

  @Bindable
  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }

  @Bindable
  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    if (!successDialog.isHidden()) {
      successDialog.hide();
    }
  }

  public void setDatasourceModel(DatasourceModel model) {
    this.datasourceModel = model;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public String getName() {
    return "connectionController";//$NON-NLS-1$
  }

  @Bindable
  public void closeDialog() {
    for (ConnectionDialogListener listener : listeners) {
      listener.onDialogCancel();
    }
  }

  @Bindable
  public void closeSaveConnectionConfirmationDialog() {
    saveConnectionConfirmationDialog.hide();
  }

  @Bindable
  public void addConnection() {
    RequestBuilder testConnectionBuilder = new RequestBuilder(RequestBuilder.PUT, URL.encode(getBaseURL() + "test"));
    testConnectionBuilder.setHeader("Content-Type", "application/json");
    try {
      AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection);
      testConnectionBuilder.sendRequest(AutoBeanCodex.encode(bean).getPayload(), new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          saveConnectionConfirmationDialog.show();
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          try {
            if (response.getStatusCode() == Response.SC_OK) {
              saveConnection();
            } else {
              saveConnectionConfirmationDialog.show();
            }
          } catch (Exception e) {
            displayErrorMessage(e);
          }
        }

      });
    } catch (RequestException e) {
      displayErrorMessage(e);
    }
  }

  @Bindable
  public void testConnection() {
    RequestBuilder testConnectionBuilder = new RequestBuilder(RequestBuilder.PUT, URL.encode(getBaseURL() + "test"));
    testConnectionBuilder.setHeader("Content-Type", "application/json");
    try {
      AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection);
      testConnectionBuilder.sendRequest(AutoBeanCodex.encode(bean).getPayload(), new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          displayErrorMessage(exception);
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          Boolean testPassed = new Boolean(response.getText());
          try {
            if (testPassed) {
              openSuccesDialog(MessageHandler.getString("SUCCESS"), MessageHandler//$NON-NLS-1$
                  .getString("ConnectionController.CONNECTION_TEST_SUCCESS"));//$NON-NLS-1$
            } else {
              openErrorDialog(MessageHandler.getString("ERROR"), MessageHandler//$NON-NLS-1$
                  .getString("ConnectionController.ERROR_0003_CONNECTION_TEST_FAILED"));//$NON-NLS-1$
            }
          } catch (Exception e) {
            displayErrorMessage(e);
          }
        }

      });
    } catch (RequestException e) {
      displayErrorMessage(e);
    }
  }

  @Bindable
  public void deleteConnection() {
    removeConfirmationDialog.hide();
    RequestBuilder deleteConnectionBuilder = new RequestBuilder(RequestBuilder.DELETE, URL.encode(getBaseURL() + "deletebyname?name=" + datasourceModel.getSelectedRelationalConnection().getName()));
    try {
      deleteConnectionBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          displayErrorMessage(exception);
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          try {
            if (response.getStatusCode() == Response.SC_OK) {
              openSuccesDialog(MessageHandler.getString("SUCCESS"), MessageHandler//$NON-NLS-1$
                  .getString("ConnectionController.CONNECTION_DELETED"));//$NON-NLS-1$
//              Window.alert("In ConnectionController:Delete Connection");
              datasourceModel.getGuiStateModel().deleteConnection(datasourceModel.getSelectedRelationalConnection().getName());
              List<IDatabaseConnection> connections = datasourceModel.getGuiStateModel().getConnections();
              if (connections != null && connections.size() > 0) {
                datasourceModel.setSelectedRelationalConnection(connections.get(connections.size() - 1));
              } else {
                datasourceModel.setSelectedRelationalConnection(null);
              }

            } else {
              openErrorDialog(MessageHandler.getString("ERROR"), MessageHandler//$NON-NLS-1$
                  .getString("ConnectionController.ERROR_0002_UNABLE_TO_DELETE_CONNECTION"));//$NON-NLS-1$
            }

          } catch (Exception e) {
            displayErrorMessage(e);
          }
        }
      });
    } catch (RequestException e) {
      displayErrorMessage(e);
    }
  }

  @Bindable
  public void saveConnection() {
    if (!saveConnectionConfirmationDialog.isHidden()) {
      saveConnectionConfirmationDialog.hide();
    }

    RequestBuilder getConnectionBuilder = new RequestBuilder(RequestBuilder.GET, URL.encode(getBaseURL() + "get?name=" + currentConnection.getName()));
    getConnectionBuilder.setHeader("Content-Type", "application/json");
    try {
      getConnectionBuilder.sendRequest(null, new RequestCallback() {

        private void saveNew() {
          RequestBuilder addConnectionBuilder = new RequestBuilder(RequestBuilder.POST, URL.encode(getBaseURL() + "add"));
          addConnectionBuilder.setHeader("Content-Type", "application/json");
          try {
            AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection);
            addConnectionBuilder.sendRequest(AutoBeanCodex.encode(bean).getPayload(), new RequestCallback() {

              @Override
              public void onError(Request request, Throwable exception) {
                displayErrorMessage(exception);
              }

              @Override
              public void onResponseReceived(Request request, Response response) {
                try {
                  if (response.getStatusCode() == Response.SC_OK) {
                    datasourceModel.getGuiStateModel().addConnection(currentConnection);
                    datasourceModel.setSelectedRelationalConnection(currentConnection);
                    connectionSetter.getOuterListener().onDialogAccept(currentConnection);
                  } else {
                    openErrorDialog(MessageHandler.getString("ERROR"), MessageHandler//$NON-NLS-1$
                        .getString("ConnectionController.ERROR_0001_UNABLE_TO_ADD_CONNECTION"));//$NON-NLS-1$
                  }

                } catch (Exception e) {
                  displayErrorMessage(e);
                }
              }
            });
          } catch (RequestException e) {
            displayErrorMessage(e);
          }
        }

        @Override
        public void onError(Request request, Throwable exception) {
          saveNew();
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          if (response.getStatusCode() == Response.SC_OK) {
            RequestBuilder updateConnectionBuilder = new RequestBuilder(RequestBuilder.POST, URL.encode(getBaseURL() + "update"));
            updateConnectionBuilder.setHeader("Content-Type", "application/json");
            try {
              AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection);
              updateConnectionBuilder.sendRequest(AutoBeanCodex.encode(bean).getPayload(), new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                  displayErrorMessage(exception);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                  try {
                    if (response.getStatusCode() == Response.SC_OK) {
                      datasourceModel.getGuiStateModel().addConnection(currentConnection);
                      datasourceModel.setSelectedRelationalConnection(currentConnection);
                    } else {
                      openErrorDialog(MessageHandler.getString("ERROR"), MessageHandler//$NON-NLS-1$
                          .getString("ConnectionController.ERROR_0001_UNABLE_TO_ADD_CONNECTION"));//$NON-NLS-1$
                    }
                  } catch (Exception e) {
                    displayErrorMessage(e);
                  }
                }
              });
            } catch (RequestException e) {
              displayErrorMessage(e);
            }
          } else {
            saveNew();
          }
        }
      });
    } catch (RequestException e) {
      displayErrorMessage(e);
    }
  }

  public void addConnectionDialogListener(ConnectionDialogListener listener) {
    if (listeners.contains(listener) == false) {
      listeners.add(listener);
    }
  }

  public void removeConnectionDialogListener(ConnectionDialogListener listener) {
    if (listeners.contains(listener)) {
      listeners.remove(listener);
    }
  }

  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th, MessageHandler.getString("DatasourceEditor.USER_ERROR_TITLE")));//$NON-NLS-1$
    errorLabel.setValue(ExceptionParser.getErrorMessage(th, MessageHandler.getString("DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED")));//$NON-NLS-1$
    errorDialog.show();
  }

  public void showAddConnectionDialog(DialogListener listener) {
//    this.listener = listener;
    connectionSetter = new DatabaseConnectionSetter(listener);
    showAddConnectionDialog();
  }

  @Bindable
  public void showAddConnectionDialog() {
    datasourceModel.setEditing(false);
    if (databaseDialog != null) {
      databaseDialog.setDatabaseConnection(null);
      databaseDialog.show();
    } else {
      if (databaseTypeHelper == null) {
        XulServiceCallback<List<IDatabaseType>> callback = new XulServiceCallback<List<IDatabaseType>>() {
          public void error(String message, Throwable error) {
            Window.alert(message + ":  " + error.getLocalizedMessage());
            //error.printStackTrace();
          }

          public void success(List<IDatabaseType> retVal) {
            databaseTypeHelper = new DatabaseTypeHelper(retVal);
            databaseDialog = new GwtDatabaseDialog(databaseTypeHelper, GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter); //$NON-NLS-1$
          }
        };
        dialectService.getDatabaseTypes(callback);
      } else {
        databaseDialog = new GwtDatabaseDialog(databaseTypeHelper, GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter); //$NON-NLS-1$
      }
    }
  }

  @Bindable
  public void showEditConnectionDialog() {
    datasourceModel.setEditing(true);
    if(databaseDialog != null) {
      IDatabaseConnection connection = datasourceModel.getSelectedRelationalConnection();
      databaseDialog.setDatabaseConnection(connection);
      databaseDialog.show();
    } else {
      databaseDialog = new GwtDatabaseDialog(databaseTypeHelper,
          GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter); //$NON-NLS-1$
    }
  }

  @Bindable
  public void showRemoveConnectionDialog() {
    // Display the warning message.
    // If ok then remove the connection from the list
    removeConfirmationDialog.show();
  }

  @Bindable
  public void closeRemoveConfirmationDialog() {
    removeConfirmationDialog.hide();
  }

  public void reloadConnections() {
    RequestBuilder listConnectionBuilder = new RequestBuilder(RequestBuilder.GET, URL.encode(getBaseURL() + "list"));
    listConnectionBuilder.setHeader("Content-Type", "application/json");
    try {
      listConnectionBuilder.sendRequest(null, new RequestCallback() {

        @Override
        public void onError(Request request, Throwable exception) {
          MessageHandler.getInstance().showErrorDialog(MessageHandler.getString("ERROR"), MessageHandler.getString("DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", exception.getLocalizedMessage()));
        }

        @Override
        public void onResponseReceived(Request request, Response response) {
          String responseText = response.getText();
          AutoBean<IDatabaseConnectionList> bean = AutoBeanCodex.decode(connectionAutoBeanFactory, IDatabaseConnectionList.class, responseText);
          datasourceModel.getGuiStateModel().setConnections(bean.as().getDatabaseConnections());
        }
      });
    } catch (RequestException e) {
      MessageHandler.getInstance().showErrorDialog(MessageHandler.getString("ERROR"), "DatasourceEditor.ERROR_0004_CONNECTION_SERVICE_NULL");
    }
  }

  public static String getBaseURL() {
    String moduleUrl = GWT.getModuleBaseURL();
    //
    //Set the base url appropriately based on the context in which we are running this client
    //
    if (moduleUrl.indexOf("content") > -1) {
      //we are running the client in the context of a BI Server plugin, so 
      //point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring(0, moduleUrl.indexOf("content"));
      return baseUrl + "plugin/data-access/api/connection/";
    }

    return moduleUrl + "plugin/data-access/api/connection/";
  }

//  public static List<Connection> deserializeConnections(String json) {
//    IConnection[] jsonConnections = deserializeConnectionsFromJson(json);
//    ArrayList<Connection> connections = new ArrayList<Connection>();
//    for (IConnection jsonConnection : jsonConnections) {
//      connections.add(jsoConnectionToConnection(jsonConnection));
//    }
//    return connections;
//  }

//  public static Connection deserializeConnection(String json) {
//    IConnection[] jsonConnection = deserializeConnectionsFromJson(json);
//    return jsoConnectionToConnection(jsonConnection[0]);
//  }

//  private static Connection jsoConnectionToConnection(IConnection jsoConnection) {
//    Connection connection = new Connection();
//    connection.setDriverClass(jsoConnection.getDriverClass());
//    connection.setId(jsoConnection.getId());
//    connection.setName(jsoConnection.getName());
//    connection.setPassword(jsoConnection.getPassword());
//    connection.setUrl(jsoConnection.getUrl());
//    connection.setUsername(jsoConnection.getUsername());
//    return connection;
//  }

//  private static final native IConnection[] deserializeConnectionsFromJson(String json)/*-{
//		var jso;
//		jso = eval('(' + json + ')').connection;
//		if (jso instanceof Array) {
//			return jso;
//		} else {
//			var arr = new Array();
//			arr.push(jso);
//			return arr;
//		}
//  }-*/;

//  public static final String serializeConnectionToJson(IConnection connection) {
//    JsoConnection jsoConnection = (JsoConnection) JavaScriptObject.createObject();
//    jsoConnection.setDriverClass(connection.getDriverClass());
//    jsoConnection.setId(connection.getId());
//    jsoConnection.setName(connection.getName());
//    jsoConnection.setPassword(connection.getPassword());
//    jsoConnection.setUrl(connection.getUrl());
//    jsoConnection.setUsername(connection.getUsername());
//
//    return new JSONObject(jsoConnection).toString();
//  }

//  public static class JsoConnection extends JavaScriptObject implements IConnection {
//    protected JsoConnection() {
//    }
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#setDriverClass(java.lang.String)
//     */
//    @Override
//    public final native void setDriverClass(String driverClass) /*-{
//			this.driverClass = driverClass;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#getDriverClass()
//     */
//    @Override
//    public final native String getDriverClass() /*-{
//			return this.driverClass;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#setId(java.lang.String)
//     */
//    @Override
//    public final native void setId(String id) /*-{
//			this.id = id;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#getId()
//     */
//    @Override
//    public final native String getId() /*-{
//			return this.id;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#setName(java.lang.String)
//     */
//    @Override
//    public final native void setName(String name) /*-{
//			this.name = name;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#getName()
//     */
//    @Override
//    public final native String getName() /*-{
//			return this.name;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#setPassword(java.lang.String)
//     */
//    @Override
//    public final native void setPassword(String password) /*-{
//			this.password = password;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#getPassword()
//     */
//    @Override
//    public final native String getPassword() /*-{
//			return this.password;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#setUrl(java.lang.String)
//     */
//    @Override
//    public final native void setUrl(String url) /*-{
//			this.url = url;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#getUrl()
//     */
//    @Override
//    public final native String getUrl() /*-{
//			return this.url;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#setUsername(java.lang.String)
//     */
//    @Override
//    public final native void setUsername(String username) /*-{
//			this.username = username;
//    }-*/;
//
//    /* (non-Javadoc)
//     * @see org.pentaho.platform.dataaccess.datasource.beans.IConnection#getUsername()
//     */
//    @Override
//    public final native String getUsername() /*-{
//			return this.username;
//    }-*/;
//  }

  class DatabaseConnectionSetter implements DatabaseDialogListener {

    final DialogListener wrappedListener;

    public DatabaseConnectionSetter(DialogListener listener) {
      super();
      this.wrappedListener = listener;
    }

    public DialogListener getOuterListener() {
      return wrappedListener;
    }

    /* (non-Javadoc)
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogAccept(org.pentaho.database.model.IDatabaseConnection)
     */
    public void onDialogAccept(final IDatabaseConnection connection) {
      currentConnection = connection;
      addConnection();
    }

    /* (non-Javadoc)
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogCancel()
     */
    public void onDialogCancel() {
      wrappedListener.onDialogCancel();
    }

    /* (non-Javadoc)
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogReady()
     */
    public void onDialogReady() {
      if (datasourceModel.isEditing() == false) {
        showAddConnectionDialog(wrappedListener);
      } else {
        showEditConnectionDialog();
      }
    }
  }
}
