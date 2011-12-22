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

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.ConnectionDialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardConnectionController.ConnectionSetter;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.ui.database.event.DatabaseDialogListener;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseConnectionService;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseDialectService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.GWT;

//TODO: move to the relational datasource package
public class ConnectionController extends AbstractXulEventHandler {


  private IXulAsyncConnectionService service;

  private List<ConnectionDialogListener> listeners = new ArrayList<ConnectionDialogListener>();

  private DatasourceModel datasourceModel;

  private XulDialog removeConfirmationDialog;

  private XulDialog saveConnectionConfirmationDialog;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  GwtXulAsyncDatabaseConnectionService connService = new GwtXulAsyncDatabaseConnectionService();
  
  GwtXulAsyncDatabaseDialectService dialectService = new GwtXulAsyncDatabaseDialectService();

  GwtDatabaseDialog databaseDialog;

  DatabaseTypeHelper databaseTypeHelper;

  Connection currentConnection;
  
  DialogListener listener;

  ConnectionSetter connectionSetter = new ConnectionSetter();
  
  public ConnectionController() {
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
    try {
      service.testConnection(currentConnection, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
           saveConnectionConfirmationDialog.show();
        }
        public void success(Boolean value) {
          if (value) {
            saveConnection();
          } else {
            saveConnectionConfirmationDialog.show();
          }
        }
      });
    } catch (Exception e) {
      saveConnectionConfirmationDialog.show();
    }
  }

  @Bindable
  public void testConnection() {
    try {
      service.testConnection(currentConnection, new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          displayErrorMessage(error);
        }

        public void success(Boolean value) {
          try {
            if (value) {
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
    } catch (Exception e) {
      displayErrorMessage(e);
    }
  }

  @Bindable
  public void deleteConnection() {
    removeConfirmationDialog.hide();
    service.deleteConnection(datasourceModel.getSelectedRelationalConnection().getName(),
        new XulServiceCallback<Boolean>() {

          public void error(String message, Throwable error) {
            displayErrorMessage(error);
          }

          public void success(Boolean value) {
            try {
              if (value) {
                openSuccesDialog(MessageHandler.getString("SUCCESS"), MessageHandler//$NON-NLS-1$
                    .getString("ConnectionController.CONNECTION_DELETED"));//$NON-NLS-1$
                datasourceModel.getGuiStateModel().deleteConnection(
                    datasourceModel.getSelectedRelationalConnection().getName());
                List<Connection> connections = datasourceModel.getGuiStateModel().getConnections();
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
  }

  @Bindable
  public void saveConnection() {
    if (!saveConnectionConfirmationDialog.isHidden()) {
      saveConnectionConfirmationDialog.hide();
    }

    service.getConnectionByName(currentConnection.getName(), new XulServiceCallback<Connection>() {
      public void error(String message, Throwable error) {
        // Connection not found. Create a new one.
        service.addConnection(currentConnection, new XulServiceCallback<Boolean>() {
          public void error(String message, Throwable error) {
            displayErrorMessage(error);
          }

          public void success(Boolean value) {
            try {
              if (value) {
                if(listener == null) {
                  datasourceModel.getGuiStateModel().addConnection(currentConnection);
                  datasourceModel.setSelectedRelationalConnection(currentConnection);
                } else {
                  listener.onDialogAccept(value);
                  datasourceModel.setEditing(false);
                }
              } else {
                openErrorDialog(MessageHandler.getString("ERROR"), MessageHandler//$NON-NLS-1$
                    .getString("ConnectionController.ERROR_0001_UNABLE_TO_ADD_CONNECTION"));//$NON-NLS-1$
              }

            } catch (Exception e) {
              displayErrorMessage(e);
            }
          }
        });
      }

      public void success(Connection value) {
        // Connection found. Update it.
        service.updateConnection(currentConnection, new XulServiceCallback<Boolean>() {

          public void error(String message, Throwable error) {
            displayErrorMessage(error);
          }

          public void success(Boolean value) {
            try {
              if (value) {
                if(listener == null) {
                  openSuccesDialog(MessageHandler.getString("SUCCESS"), MessageHandler//$NON-NLS-1$
                    .getString("ConnectionController.CONNECTION_UPDATED"));//$NON-NLS-1$
                  datasourceModel.getGuiStateModel().updateConnection(currentConnection);
                  datasourceModel.setSelectedRelationalConnection(currentConnection);
                } else {
                  listener.onDialogAccept(value);
                  datasourceModel.setEditing(false);
                }
              } else {
                openErrorDialog(MessageHandler.getString("ERROR"), MessageHandler//$NON-NLS-1$
                    .getString("ConnectionController.ERROR_0004_UNABLE_TO_UPDATE_CONNECTION"));//$NON-NLS-1$
              }
            } catch (Exception e) {
            }
          }
        });
      }
    });
  }


  public IXulAsyncConnectionService getService() {
    return service;
  }

  public void setService(IXulAsyncConnectionService service) {
    this.service = service;
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
    this.listener = listener;
    showAddConnectionDialog();
  }

  @Bindable
  public void showAddConnectionDialog() {
    datasourceModel.setEditing(false);
    if(databaseDialog != null){
      databaseDialog.setDatabaseConnection(null);
      databaseDialog.show();
    } else {
      if(databaseTypeHelper == null) {
        XulServiceCallback<List<IDatabaseType>> callback = new XulServiceCallback<List<IDatabaseType>>() {
          public void error(String message, Throwable error) {
            error.printStackTrace();
          }

          public void success(List<IDatabaseType> retVal) {
            databaseTypeHelper = new DatabaseTypeHelper(retVal);
            databaseDialog = new GwtDatabaseDialog(connService, databaseTypeHelper,
                GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter); //$NON-NLS-1$
          }
        };
        dialectService.getDatabaseTypes(callback);
      } else {
        databaseDialog = new GwtDatabaseDialog(connService, databaseTypeHelper,
            GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter); //$NON-NLS-1$
      }
    }
  }


  @Bindable
  public void showEditConnectionDialog() {
    datasourceModel.setEditing(true);
    if(databaseDialog != null){
      Connection connection = datasourceModel.getSelectedRelationalConnection();
      service.convertFromConnection(connection, new XulServiceCallback<IDatabaseConnection>() {
        public void error(String message, Throwable error) {
          displayErrorMessage(error);
        }
        public void success(IDatabaseConnection conn) {
          databaseDialog.setDatabaseConnection(conn);
          databaseDialog.show();
        }
      });
    } else {
      databaseDialog = new GwtDatabaseDialog(connService, databaseTypeHelper,
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


  class ConnectionSetter implements DatabaseDialogListener {

    /* (non-Javadoc)
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogAccept(org.pentaho.database.model.IDatabaseConnection)
     */
    public void onDialogAccept(IDatabaseConnection connection) {
      service.convertToConnection(connection, new XulServiceCallback<Connection>() {
        public void error(String message, Throwable error) {
          displayErrorMessage(error);
        }
        public void success(Connection retVal) {
          currentConnection = retVal;
          addConnection();
        }
      });
    }

    /* (non-Javadoc)
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogCancel()
     */
    public void onDialogCancel() {
      // do nothing
    }

    /* (non-Javadoc)
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogReady()
     */
    public void onDialogReady() {
      if (datasourceModel.isEditing() == false) {
        showAddConnectionDialog();  
      } else {
        showEditConnectionDialog();
      }
    }
    
  }

  public void reloadConnections(){
    if (service != null) {
      service.getConnections(new XulServiceCallback<List<Connection>>() {

        public void error(String message, Throwable error) {
          MessageHandler.getInstance().showErrorDialog(MessageHandler.getString("ERROR"), MessageHandler.getString(
              "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", error.getLocalizedMessage()));
        }

        public void success(List<Connection> connections) {
          datasourceModel.getGuiStateModel().setConnections(connections);
        }

      });
    } else {
      MessageHandler.getInstance().showErrorDialog(MessageHandler.getString("ERROR"),
          "DatasourceEditor.ERROR_0004_CONNECTION_SERVICE_NULL");
    }
  }


}
