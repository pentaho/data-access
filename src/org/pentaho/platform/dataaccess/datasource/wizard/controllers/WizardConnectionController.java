/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.dataaccess.datasource.beans.AutobeanUtilities;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.ConnectionDialogListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.ui.database.event.DatabaseDialogListener;
import org.pentaho.ui.database.event.IConnectionAutoBeanFactory;
import org.pentaho.ui.database.gwt.GwtDatabaseDialog;
import org.pentaho.ui.database.gwt.GwtXulAsyncDatabaseDialectService;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

public class WizardConnectionController extends AbstractXulEventHandler {

  //  private IXulAsyncConnectionService connectionService;

  private List<ConnectionDialogListener> listeners = new ArrayList<ConnectionDialogListener>();

  private DatasourceModel datasourceModel;

  private XulDialog removeConfirmationDialog;

  private XulDialog saveConnectionConfirmationDialog;

  private XulDialog overwriteConnectionConfirmationDialog;

  private XulDialog renameConnectionConfirmationDialog;

  private XulDialog errorDialog;

  private XulDialog errorDetailsDialog;

  private XulDialog successDialog;

  private XulDialog successDetailsDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  //  GwtXulAsyncDatabaseConnectionService connService = new GwtXulAsyncDatabaseConnectionService();

  GwtXulAsyncDatabaseDialectService dialectService = new GwtXulAsyncDatabaseDialectService();

  GwtDatabaseDialog databaseDialog;

  DatabaseTypeHelper databaseTypeHelper;

  IDatabaseConnection currentConnection;

  ConnectionSetter connectionSetter = new ConnectionSetter();

  protected IConnectionAutoBeanFactory connectionAutoBeanFactory;

  protected String previousConnectionName, existingConnectionName;

  public WizardConnectionController( Document document ) {
    this.document = document;
    connectionAutoBeanFactory = GWT.create( IConnectionAutoBeanFactory.class );
    init();
  }

  protected void copyDatabaseConnectionProperties( IDatabaseConnection source, IDatabaseConnection target ) {
    target.setId( source.getId() );
    target.setAccessType( source.getAccessType() );
    target.setDatabaseType( source.getDatabaseType() );
    target.setExtraOptions( source.getExtraOptions() );
    target.setName( source.getName() );
    target.setHostname( source.getHostname() );
    target.setDatabaseName( source.getDatabaseName() );
    target.setDatabasePort( source.getDatabasePort() );
    target.setUsername( source.getUsername() );
    target.setPassword( source.getPassword() );
    target.setStreamingResults( source.isStreamingResults() );
    target.setDataTablespace( source.getDataTablespace() );
    target.setIndexTablespace( source.getIndexTablespace() );
    target.setUsingDoubleDecimalAsSchemaTableSeparator( source.isUsingDoubleDecimalAsSchemaTableSeparator() );
    target.setInformixServername( source.getInformixServername() );
    //target.addExtraOption(String databaseTypeCode, String option, String value);
    target.setAttributes( source.getAttributes() );
    target.setChanged( source.getChanged() );
    target.setQuoteAllFields( source.isQuoteAllFields() );
    // advanced option (convert to enum with upper, lower, none?)
    target.setForcingIdentifiersToLowerCase( source.isForcingIdentifiersToLowerCase() );
    target.setForcingIdentifiersToUpperCase( source.isForcingIdentifiersToUpperCase() );
    target.setConnectSql( source.getConnectSql() );
    target.setUsingConnectionPool( source.isUsingConnectionPool() );
    target.setInitialPoolSize( source.getInitialPoolSize() );
    target.setMaximumPoolSize( source.getMaximumPoolSize() );
    target.setPartitioned( source.isPartitioned() );
    target.setConnectionPoolingProperties( source.getConnectionPoolingProperties() );
    target.setPartitioningInformation( source.getPartitioningInformation() );
  }

  protected AutoBean<IDatabaseConnection> createIDatabaseConnectionBean( IDatabaseConnection connection ) {
    AutoBean<IDatabaseConnection> bean = connectionAutoBeanFactory.iDatabaseConnection();
    IDatabaseConnection connectionBean = bean.as();
    copyDatabaseConnectionProperties( connection, connectionBean );
    return AutoBeanUtils.getAutoBean( connectionBean );
    //return connectionBean;
  }

  @Bindable
  public void init() {

    XulServiceCallback<List<IDatabaseType>> callback = new XulServiceCallback<List<IDatabaseType>>() {
      public void error( String message, Throwable error ) {
        error.printStackTrace();
      }

      public void success( List<IDatabaseType> retVal ) {
        databaseTypeHelper = new DatabaseTypeHelper( retVal );
      }
    };
    dialectService.getDatabaseTypes( callback );

    saveConnectionConfirmationDialog =
      (XulDialog) document.getElementById( "saveConnectionConfirmationDialog" ); //$NON-NLS-1$
    overwriteConnectionConfirmationDialog =
      (XulDialog) document.getElementById( "overwriteConnectionConfirmationDialog" );
    renameConnectionConfirmationDialog = (XulDialog) document.getElementById( "renameConnectionConfirmationDialog" );
    errorDialog = (XulDialog) document.getElementById( "errorDialog" ); //$NON-NLS-1$
    errorDetailsDialog = (XulDialog) document.getElementById( "errorDetailsDialog" ); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById( "errorLabel" ); //$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById( "successDialog" ); //$NON-NLS-1$
    successDetailsDialog = (XulDialog) document.getElementById( "successDetailsDialog" ); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById( "successLabel" ); //$NON-NLS-1$
    removeConfirmationDialog = (XulDialog) document.getElementById( "removeConfirmationDialog" ); //$NON-NLS-1$
  }

  @Bindable
  public void openErrorDialog( String title, String message ) {
    errorDialog.setTitle( title );
    errorLabel.setValue( message );
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if ( !errorDialog.isHidden() ) {
      errorDialog.hide();
    }
  }

  @Bindable
  public void closeSuccessDetailsDialog() {
    if ( !successDetailsDialog.isHidden() ) {
      successDetailsDialog.hide();
    }
  }

  @Bindable
  public void toggleDetails() {
    XulHbox details = (XulHbox) document.getElementById( "details_hider" ); //$NON-NLS-1$
    details.setVisible( !details.isVisible() );
  }

  @Bindable
  public void openSuccesDialog( String title, String message ) {
    successDialog.setTitle( title );
    successLabel.setValue( message );
    successDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    if ( !successDialog.isHidden() ) {
      successDialog.hide();
    }
  }

  @Bindable
  public void toggleSuccessDetails() {
    XulHbox details = (XulHbox) document.getElementById( "success_details_hider" ); //$NON-NLS-1$
    details.setVisible( !details.isVisible() );
  }

  @Bindable
  public void closeErrorDetailsDialog() {
    if ( !errorDetailsDialog.isHidden() ) {
      errorDetailsDialog.hide();
    }
  }

  public void setDatasourceModel( DatasourceModel model ) {
    this.datasourceModel = model;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public String getName() {
    return "wizardConnectionController"; //$NON-NLS-1$
  }

  @Bindable
  public void closeDialog() {
    for ( ConnectionDialogListener listener : listeners ) {
      listener.onDialogCancel();
    }
  }

  @Bindable
  public void handleDialogAccept() {
    //first, test the connection
    RequestBuilder testConnectionBuilder =
      new RequestBuilder( RequestBuilder.PUT, ConnectionController.getServiceURL( "test" ) );
    testConnectionBuilder.setHeader( "Content-Type", "application/json" );
    try {
      //AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection);
      AutoBean<IDatabaseConnection> bean = createIDatabaseConnectionBean( currentConnection );
      testConnectionBuilder.sendRequest( AutoBeanCodex.encode( bean ).getPayload(), new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          saveConnectionConfirmationDialog.show();
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          try {
            if ( response.getStatusCode() == Response.SC_OK ) {
              //test is ok, now check if we are renaming
              renameCheck();
            } else {
              //confirm if we should continu saving this invalid connection.
              saveConnectionConfirmationDialog.show();
            }
          } catch ( Exception e ) {
            displayErrorMessage( e );
          }
        }

      } );
    } catch ( RequestException e ) {
      displayErrorMessage( e );
    }
  }

  @Bindable
  public void renameCheck() {
    if ( !saveConnectionConfirmationDialog.isHidden() ) {
      closeSaveConnectionConfirmationDialog();
    }

    if ( datasourceModel.isEditing() && !previousConnectionName.equals( currentConnection.getName() ) ) {
      showRenameConnectionConfirmationDialog();
    } else {
      overwriteCheck();
    }

  }

  @Bindable
  public void overwriteCheck() {
    if ( !saveConnectionConfirmationDialog.isHidden() ) {
      closeSaveConnectionConfirmationDialog();
    }
    if ( !renameConnectionConfirmationDialog.isHidden() ) {
      closeRenameConnectionConfirmationDialog();
    }

    if ( datasourceModel.isEditing() && previousConnectionName.equals( currentConnection.getName() ) ) {
      //if editing and no name change, proceed.
      updateConnection();
    } else {
      String cacheBuster = String.valueOf( new java.util.Date().getTime() );
      //either new connection, or editing involved a name change.
      RequestBuilder getConnectionBuilder =
        new RequestBuilder( RequestBuilder.GET, ConnectionController.getServiceURL( "get",
          new String[][] { { "name", currentConnection.getName() }, { "ts", cacheBuster } } ) );
      getConnectionBuilder.setHeader( "Content-Type", "application/json" );
      try {
        AutoBean<IDatabaseConnection> bean = createIDatabaseConnectionBean( currentConnection );
        getConnectionBuilder.sendRequest( AutoBeanCodex.encode( bean ).getPayload(), new RequestCallback() {

          public void onResponseReceived( Request request, Response response ) {
            switch( response.getStatusCode() ) {
              case Response.SC_OK:
                showOverwriteConnectionConfirmationDialog();
                break;
              case Response.SC_NOT_FOUND:
                saveConnection();
                break;
              default:
                //TODO: error message
                saveConnection();
            }
          }

          public void onError( Request request, Throwable exception ) {
            displayErrorMessage( exception );
          }

        } );
      } catch ( Exception e ) {
        displayErrorMessage( e );
      }
    }
  }

  @Bindable
  public void updateConnection() {
    RequestBuilder updateConnectionBuilder =
      new RequestBuilder( RequestBuilder.POST, ConnectionController.getServiceURL( "update" ) );
    updateConnectionBuilder.setHeader( "Content-Type", "application/json" );
    try {
      //AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection); 
      AutoBean<IDatabaseConnection> bean = createIDatabaseConnectionBean( currentConnection );
      updateConnectionBuilder.sendRequest( AutoBeanCodex.encode( bean ).getPayload(), new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          displayErrorMessage( exception );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          try {
            if ( response.getStatusCode() == Response.SC_OK ) {
              currentConnection = AutobeanUtilities.connectionBeanToImpl( currentConnection );
              datasourceModel.getGuiStateModel().updateConnection( existingConnectionName, currentConnection );
              datasourceModel.setSelectedRelationalConnection( currentConnection );
            } else if ( response.getStatusCode()
              == Response.SC_INTERNAL_SERVER_ERROR ) { // We assume that this means the connection doesn't exist to
              // update so we'll add it
              RequestBuilder deleteConnectionBuilder =
                new RequestBuilder( RequestBuilder.DELETE, ConnectionController.getServiceURL( "deletebyname",
                  new String[][] { { "name", previousConnectionName } } ) );
              try {
                deleteConnectionBuilder.sendRequest( null, new RequestCallback() {

                  @Override
                  public void onError( Request request, Throwable exception ) {
                    displayErrorMessage( exception );
                  }

                  @Override
                  public void onResponseReceived( Request request, Response response ) {
                    try {
                      if ( response.getStatusCode() == Response.SC_OK ) {
                        datasourceModel.getGuiStateModel().deleteConnection(
                          datasourceModel.getSelectedRelationalConnection().getName() );
                        List<IDatabaseConnection> connections = datasourceModel.getGuiStateModel().getConnections();
                        if ( connections != null && connections.size() > 0 ) {
                          datasourceModel.setSelectedRelationalConnection( connections.get( connections.size() - 1 ) );
                        } else {
                          datasourceModel.setSelectedRelationalConnection( null );
                        }
                        addConnection();
                      } else {
                        openErrorDialog( MessageHandler.getString( "ERROR" ), MessageHandler//$NON-NLS-1$
                          .getString( "ConnectionController.ERROR_0002_UNABLE_TO_DELETE_CONNECTION" ) ); //$NON-NLS-1$
                      }

                    } catch ( Exception e ) {
                      displayErrorMessage( e );
                    }
                  }
                } );
              } catch ( RequestException e ) {
                displayErrorMessage( e );
              }
            } else {
              openErrorDialog( MessageHandler.getString( "ERROR" ), MessageHandler//$NON-NLS-1$
                .getString( "ConnectionController.ERROR_0004_UNABLE_TO_UPDATE_CONNECTION" ) ); //$NON-NLS-1$
            }
          } catch ( Exception e ) {
            displayErrorMessage( e );
          }
        }

      } );
    } catch ( RequestException e ) {
      displayErrorMessage( e );
    }
  }

  @Bindable
  public void addConnection() {
    RequestBuilder addConnectionBuilder =
      new RequestBuilder( RequestBuilder.POST, ConnectionController.getServiceURL( "add" ) );
    addConnectionBuilder.setHeader( "Content-Type", "application/json" );
    try {
      //AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection); 
      AutoBean<IDatabaseConnection> bean = createIDatabaseConnectionBean( currentConnection );
      addConnectionBuilder.sendRequest( AutoBeanCodex.encode( bean ).getPayload(), new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          displayErrorMessage( exception );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          try {
            if ( response.getStatusCode() == Response.SC_OK ) {
              IDatabaseConnection conn = AutobeanUtilities.connectionBeanToImpl( currentConnection );
              datasourceModel.getGuiStateModel().addConnection( conn );
              datasourceModel.setSelectedRelationalConnection( conn );
            } else {
              openErrorDialog( MessageHandler.getString( "ERROR" ), MessageHandler//$NON-NLS-1$
                .getString( "ConnectionController.ERROR_0001_UNABLE_TO_ADD_CONNECTION" ) ); //$NON-NLS-1$
            }
          } catch ( Exception e ) {
            displayErrorMessage( e );
          }
        }

      } );
    } catch ( RequestException e ) {
      displayErrorMessage( e );
    }
  }

  @Bindable
  public void overwriteConnection() {
    if ( !saveConnectionConfirmationDialog.isHidden() ) {
      closeSaveConnectionConfirmationDialog();
    }
    if ( !renameConnectionConfirmationDialog.isHidden() ) {
      closeRenameConnectionConfirmationDialog();
    }
    if ( !overwriteConnectionConfirmationDialog.isHidden() ) {
      overwriteConnectionConfirmationDialog.hide();
    }
    existingConnectionName = currentConnection.getName();
    updateConnection();
  }

  @Bindable
  public void saveConnection() {
    if ( !saveConnectionConfirmationDialog.isHidden() ) {
      closeSaveConnectionConfirmationDialog();
    }
    if ( !renameConnectionConfirmationDialog.isHidden() ) {
      closeRenameConnectionConfirmationDialog();
    }
    if ( !overwriteConnectionConfirmationDialog.isHidden() ) {
      overwriteConnectionConfirmationDialog.hide();
    }

    if ( datasourceModel.isEditing() ) {
      updateConnection();
    } else {
      addConnection();
    }
  }

  @Bindable
  public void testConnection() {
    RequestBuilder testConnectionBuilder =
      new RequestBuilder( RequestBuilder.PUT, ConnectionController.getServiceURL( "test" ) );
    testConnectionBuilder.setHeader( "Content-Type", "application/json" );
    try {
      //AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean(currentConnection);
      AutoBean<IDatabaseConnection> bean = createIDatabaseConnectionBean( currentConnection );
      testConnectionBuilder.sendRequest( AutoBeanCodex.encode( bean ).getPayload(), new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          displayErrorMessage( exception );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          Boolean testPassed = new Boolean( response.getText() );
          try {
            if ( testPassed ) {
              openSuccesDialog( MessageHandler.getString( "SUCCESS" ), MessageHandler//$NON-NLS-1$
                .getString( "ConnectionController.CONNECTION_TEST_SUCCESS" ) ); //$NON-NLS-1$
            } else {
              openErrorDialog( MessageHandler.getString( "ERROR" ), MessageHandler//$NON-NLS-1$
                .getString( "ConnectionController.ERROR_0003_CONNECTION_TEST_FAILED" ) ); //$NON-NLS-1$
            }
          } catch ( Exception e ) {
            displayErrorMessage( e );
          }
        }

      } );
    } catch ( RequestException e ) {
      displayErrorMessage( e );
    }
  }

  @Bindable
  public void deleteConnection() {
    removeConfirmationDialog.hide();
    RequestBuilder deleteConnectionBuilder =
      new RequestBuilder( RequestBuilder.DELETE, ConnectionController.getServiceURL( "deletebyname",
        new String[][] { { "name", datasourceModel.getSelectedRelationalConnection().getName() } } ) );
    try {
      deleteConnectionBuilder.sendRequest( null, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          displayErrorMessage( exception );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          try {
            if ( response.getStatusCode() == Response.SC_OK ) {
              openSuccesDialog( MessageHandler.getString( "SUCCESS" ), MessageHandler//$NON-NLS-1$
                .getString( "ConnectionController.CONNECTION_DELETED" ) ); //$NON-NLS-1$
              datasourceModel.getGuiStateModel().deleteConnection(
                datasourceModel.getSelectedRelationalConnection().getName() );
              List<IDatabaseConnection> connections = datasourceModel.getGuiStateModel().getConnections();
              if ( connections != null && connections.size() > 0 ) {
                datasourceModel.setSelectedRelationalConnection( connections.get( connections.size() - 1 ) );
              } else {
                datasourceModel.setSelectedRelationalConnection( null );
              }

            } else {
              openErrorDialog( MessageHandler.getString( "ERROR" ), MessageHandler//$NON-NLS-1$
                .getString( "ConnectionController.ERROR_0002_UNABLE_TO_DELETE_CONNECTION" ) ); //$NON-NLS-1$
            }

          } catch ( Exception e ) {
            displayErrorMessage( e );
          }
        }
      } );
    } catch ( RequestException e ) {
      displayErrorMessage( e );
    }
  }

  public void addConnectionDialogListener( ConnectionDialogListener listener ) {
    if ( listeners.contains( listener ) == false ) {
      listeners.add( listener );
    }
  }

  public void removeConnectionDialogListener( ConnectionDialogListener listener ) {
    if ( listeners.contains( listener ) ) {
      listeners.remove( listener );
    }
  }

  public void displayErrorMessage( Throwable th ) {
    errorDialog.setTitle( ExceptionParser.getErrorHeader( th, MessageHandler
      .getString( "DatasourceEditor.USER_ERROR_TITLE" ) ) ); //$NON-NLS-1$
    errorLabel.setValue( ExceptionParser.getErrorMessage( th, MessageHandler
      .getString( "DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED" ) ) ); //$NON-NLS-1$
    errorDialog.show();
  }

  @Bindable
  public void onDialogAccept( IDatabaseConnection databaseConnection ) {
    currentConnection = databaseConnection;
    addConnection();
  }

  @Bindable
  public void onDialogCancel() {
    // do nothing
  }

  @Bindable
  public void onDialogReady() {
    if ( datasourceModel.isEditing() ) {
      showEditConnectionDialog();
    } else {
      showAddConnectionDialog();
    }
  }

  @Bindable
  public void showAddConnectionDialog() {
    datasourceModel.setEditing( false );
    if ( databaseDialog != null ) {
      previousConnectionName = null;
      existingConnectionName = previousConnectionName;
      databaseDialog.setDatabaseConnection( null );
      databaseDialog.show();
    } else {
      databaseDialog =
        new GwtDatabaseDialog( databaseTypeHelper,
          GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter ); //$NON-NLS-1$
    }
  }

  @Bindable
  public void showEditConnectionDialog() {
    datasourceModel.setEditing( true );
    if ( databaseDialog != null ) {
      IDatabaseConnection connection = datasourceModel.getSelectedRelationalConnection();
      previousConnectionName = connection.getName();
      existingConnectionName = previousConnectionName;
      DatabaseConnection editConnection = new DatabaseConnection();
      copyDatabaseConnectionProperties( connection, editConnection );
      databaseDialog.setDatabaseConnection( editConnection );
      databaseDialog.show();
    } else {
      databaseDialog =
        new GwtDatabaseDialog( databaseTypeHelper,
          GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter ); //$NON-NLS-1$
    }
  }

  @Bindable
  public void showRemoveConnectionDialog() {
    // Display the warning message. 
    // If ok then remove the connection from the list
    removeConfirmationDialog.show();
  }

  @Bindable
  public void closeSaveConnectionConfirmationDialog() {
    saveConnectionConfirmationDialog.hide();
  }

  @Bindable
  public void closeRemoveConfirmationDialog() {
    removeConfirmationDialog.hide();
  }

  public void showRenameConnectionConfirmationDialog() {
    renameConnectionConfirmationDialog.show();
  }

  @Bindable
  public void closeRenameConnectionConfirmationDialog() {
    renameConnectionConfirmationDialog.hide();
  }

  public void showOverwriteConnectionConfirmationDialog() {
    overwriteConnectionConfirmationDialog.show();
  }

  @Bindable
  public void closeOverwriteConnectionConfirmationDialog() {
    overwriteConnectionConfirmationDialog.hide();
  }

  class ConnectionSetter implements DatabaseDialogListener {

    /* (non-Javadoc)
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogAccept(org.pentaho.database.model
     * .IDatabaseConnection)
     */
    public void onDialogAccept( IDatabaseConnection connection ) {
      currentConnection = connection;
      handleDialogAccept();
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
      if ( datasourceModel.isEditing() ) {
        showEditConnectionDialog();
      } else {
        showAddConnectionDialog();
      }
    }

  }
}
