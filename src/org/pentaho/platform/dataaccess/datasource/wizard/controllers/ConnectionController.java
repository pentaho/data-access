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
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.ConnectionDialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.GuiStateModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.ui.database.event.DatabaseDialogListener;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseConnectionService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.user.client.Window;

public class ConnectionController extends AbstractXulEventHandler implements DatabaseDialogListener {


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

  GwtDatabaseDialog databaseDialog;

  DatabaseTypeHelper databaseTypeHelper;

  IConnection currentConnection;
  private IXulAsyncConnectionService connectionService;

  public ConnectionController() {
  }

  @Bindable
  public void init() {
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
              openSuccesDialog(MessageHandler.getInstance().messages.getString("SUCCESS"), MessageHandler.getInstance().messages//$NON-NLS-1$
                  .getString("ConnectionController.CONNECTION_TEST_SUCCESS"));//$NON-NLS-1$
            } else {
              openErrorDialog(MessageHandler.getInstance().messages.getString("ERROR"), MessageHandler.getInstance().messages//$NON-NLS-1$
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
                openSuccesDialog(MessageHandler.getInstance().messages.getString("SUCCESS"), MessageHandler.getInstance().messages//$NON-NLS-1$
                    .getString("ConnectionController.CONNECTION_DELETED"));//$NON-NLS-1$
                datasourceModel.getGuiStateModel().deleteConnection(
                    datasourceModel.getSelectedRelationalConnection().getName());
                List<IConnection> connections = datasourceModel.getGuiStateModel().getConnections();
                if (connections != null && connections.size() > 0) {
                  datasourceModel.setSelectedRelationalConnection(connections.get(connections.size() - 1));
                } else {
                  datasourceModel.setSelectedRelationalConnection(null);
                }

              } else {
                openErrorDialog(MessageHandler.getInstance().messages.getString("ERROR"), MessageHandler.getInstance().messages//$NON-NLS-1$
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

    if (datasourceModel.isEditing() == false) {
        service.addConnection(currentConnection, new XulServiceCallback<Boolean>() {
          public void error(String message, Throwable error) {
            displayErrorMessage(error);
          }

          public void success(Boolean value) {
            try {
              if (value) {
                datasourceModel.getGuiStateModel().addConnection(currentConnection);
                datasourceModel.setSelectedRelationalConnection(currentConnection);
              } else {
                openErrorDialog(MessageHandler.getInstance().messages.getString("ERROR"), MessageHandler.getInstance().messages//$NON-NLS-1$
                    .getString("ConnectionController.ERROR_0001_UNABLE_TO_ADD_CONNECTION"));//$NON-NLS-1$
              }

            } catch (Exception e) {
              displayErrorMessage(e);
            }
          }
        });
    } else {
      service.updateConnection(currentConnection, new XulServiceCallback<Boolean>() {

        public void error(String message, Throwable error) {
          displayErrorMessage(error);
        }

        public void success(Boolean value) {
          try {
            if (value) {
              openSuccesDialog(MessageHandler.getInstance().messages.getString("SUCCESS"), MessageHandler.getInstance().messages//$NON-NLS-1$
                  .getString("ConnectionController.CONNECTION_UPDATED"));//$NON-NLS-1$
              datasourceModel.getGuiStateModel().updateConnection(currentConnection);
              datasourceModel.setSelectedRelationalConnection(currentConnection);
            } else {
              openErrorDialog(MessageHandler.getInstance().messages.getString("ERROR"), MessageHandler.getInstance().messages//$NON-NLS-1$
                  .getString("ConnectionController.ERROR_0004_UNABLE_TO_UPDATE_CONNECTION"));//$NON-NLS-1$
            }

          } catch (Exception e) {
          }
        }
      });
    }
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
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th, MessageHandler.getInstance().messages.getString("DatasourceEditor.USER_ERROR_TITLE")));//$NON-NLS-1$
    errorLabel.setValue(ExceptionParser.getErrorMessage(th, MessageHandler.getInstance().messages.getString("DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED")));//$NON-NLS-1$
    errorDialog.show();
  }

  @Bindable
  public void onDialogAccept(IDatabaseConnection arg0) {
    service.convertToConnection(arg0, new XulServiceCallback<IConnection>() {
      public void error(String message, Throwable error) {
        displayErrorMessage(error);
      }
      public void success(IConnection retVal) {
        currentConnection = retVal;
        addConnection();
      }
    });
  }

  @Bindable
  public void onDialogCancel() {
    // do nothing
  }

  @Bindable
  public void onDialogReady() {
    if (datasourceModel.isEditing() == false) {
      showAddConnectionDialog();
    } else {
      showEditConnectionDialog();
    }
  }

  @Bindable
  public void showAddConnectionDialog() {
    datasourceModel.setEditing(false);
    if(databaseDialog != null){
      databaseDialog.setDatabaseConnection(null);
      databaseDialog.show();
    } else {
      databaseDialog = new GwtDatabaseDialog(connService, databaseTypeHelper,
          "dataaccess-databasedialog.xul", this); //$NON-NLS-1$
    }
  }

  @Bindable
  public void showEditConnectionDialog() {
    datasourceModel.setEditing(true);
    if(databaseDialog != null){
      IConnection connection = datasourceModel.getSelectedRelationalConnection();
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
          "dataaccess-databasedialog.xul", this); //$NON-NLS-1$
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


  public void reloadConnections(){
    if (connectionService != null) {
      connectionService.getConnections(new XulServiceCallback<List<IConnection>>() {

        public void error(String message, Throwable error) {
          MessageHandler.getInstance().showErrorDialog(MessageHandler.getInstance().messages.getString("ERROR"), MessageHandler.getInstance().messages.getString(
              "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", error.getLocalizedMessage()));
        }

        public void success(List<IConnection> connections) {
          datasourceModel.getGuiStateModel().setConnections(connections);
        }

      });
    } else {
      MessageHandler.getInstance().showErrorDialog(MessageHandler.getInstance().messages.getString("ERROR"),
          "DatasourceEditor.ERROR_0004_CONNECTION_SERVICE_NULL");
    }
  }


}
