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
 * Copyright (c) 2002-2019 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.platform.dataaccess.datasource.beans.AutobeanUtilities;
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
import org.pentaho.ui.xul.containers.XulHbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;

//TODO: move to the relational datasource package
public class ConnectionController extends AbstractXulEventHandler {

  public static final String CONTENT_TYPE = "Content-Type";
  public static final String JSON = "application/json";
  public static final String ERROR = "ERROR";

  private List<ConnectionDialogListener> listeners = new ArrayList<ConnectionDialogListener>();

  private DatasourceModel datasourceModel;

  private XulDialog removeConfirmationDialog;

  private XulDialog saveConnectionConfirmationDialog;

  private XulDialog overwriteConnectionConfirmationDialog;

  private XulDialog renameConnectionConfirmationDialog;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  GwtXulAsyncDatabaseDialectService dialectService = new GwtXulAsyncDatabaseDialectService();

  GwtDatabaseDialog databaseDialog;

  DatabaseTypeHelper databaseTypeHelper;

  IDatabaseConnection currentConnection;

  DatabaseConnectionSetter connectionSetter;

  protected IConnectionAutoBeanFactory connectionAutoBeanFactory;

  protected String previousConnectionName;
  protected String existingConnectionName;
  protected String existingConnectionId;

  private XulDialog successDetailsDialog;

  public static final String ATTRIBUTE_STANDARD_CONNECTION = "STANDARD_CONNECTION"; //$NON-NLS-1$

  public ConnectionController( Document document ) {
    this.document = document;
    connectionAutoBeanFactory = GWT.create( IConnectionAutoBeanFactory.class );
    init();
  }

  protected void copyDatabaseConnectionProperties( IDatabaseConnection source, IDatabaseConnection target ) {
    target.setId( source.getId() );
    target.setAccessType( source.getAccessType() );
    target.setDatabaseType( source.getDatabaseType() );
    target.setExtraOptions( source.getExtraOptions() );
    target.setExtraOptionsOrder( source.getExtraOptionsOrder() );
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
    target.setWarehouse( source.getWarehouse() );
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
        (XulDialog) document.getElementById( "saveConnectionConfirmationDialogConnectionController" ); //$NON-NLS-1$
    overwriteConnectionConfirmationDialog =
        (XulDialog) document.getElementById( "overwriteConnectionConfirmationDialogConnectionController" );
    renameConnectionConfirmationDialog =
        (XulDialog) document.getElementById( "renameConnectionConfirmationDialogConnectionController" );
    errorDialog = (XulDialog) document.getElementById( "errorDialog" ); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById( "errorLabel" ); //$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById( "successDialog" ); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById( "successLabel" ); //$NON-NLS-1$
    removeConfirmationDialog = (XulDialog) document.getElementById( "removeConfirmationDialogConnectionController" ); //$NON-NLS-1$
    successDetailsDialog = (XulDialog) document.getElementById( "successDetailsDialogConnectionController" ); //$NON-NLS-1$
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
  public void toggleDetails() {
    XulHbox details = (XulHbox) document.getElementById( "details_hider" ); //$NON-NLS-1$
    details.setVisible( !details.isVisible() );
  }

  @Bindable
  public void toggleSuccessDetails() {
    XulHbox details = (XulHbox) document.getElementById( "success_details_hider" ); //$NON-NLS-1$
    details.setVisible( !details.isVisible() );
  }

  public void setDatasourceModel( DatasourceModel model ) {
    this.datasourceModel = model;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  @Override
  public String getName() {
    return "connectionController"; //$NON-NLS-1$
  }

  @Bindable
  public void closeDialog() {
    for ( ConnectionDialogListener listener : listeners ) {
      listener.onDialogCancel();
    }
  }

  @Bindable
  public void handleDialogAccept() {
    // first, test the connection
    RequestBuilder testConnectionBuilder =
        new RequestBuilder( RequestBuilder.PUT, ConnectionController.getServiceURL( "test" ) );
    testConnectionBuilder.setHeader( CONTENT_TYPE, JSON );
    try {
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
              // test is ok, now check if we are renaming
              renameCheck();
            } else {
              // confirm if we should continu saving this invalid connection.
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
      // if editing and no name change, proceed.
      updateConnection();
    } else {
      // either new connection, or editing involved a name change.

      RequestBuilder checkConnectionBuilder =
          new RequestBuilder( RequestBuilder.GET, getServiceURL( "getid", new String[][] { { "name", currentConnection.getName() } } ) );
      checkConnectionBuilder.setHeader( CONTENT_TYPE, JSON );

      try {
        checkConnectionBuilder.sendRequest( null, new RequestCallback() {

          public void onResponseReceived( Request request, Response response ) {
            switch ( response.getStatusCode() ) {
              case Response.SC_OK:
                existingConnectionId = response.getText();
                showOverwriteConnectionConfirmationDialog();
                break;
              case Response.SC_NOT_FOUND:
                saveConnection();
                break;
              default:
                // TODO: error message
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
    updateConnectionBuilder.setHeader( CONTENT_TYPE, JSON );
    try {
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
              datasourceModel.getGuiStateModel().updateConnection( existingConnectionName, currentConnection );
              datasourceModel.setSelectedRelationalConnection( currentConnection );
              DialogListener dialogListener = connectionSetter.getOuterListener();
              if ( dialogListener != null ) {
                dialogListener.onDialogAccept( currentConnection );
              }
            } else {
              openErrorDialog( MessageHandler.getString( ERROR ), MessageHandler//$NON-NLS-1$
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
    addConnectionBuilder.setHeader( CONTENT_TYPE, JSON );
    try {
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
              datasourceModel.getGuiStateModel().addConnection( currentConnection );
              datasourceModel.setSelectedRelationalConnection( currentConnection );
              DialogListener dialogListener = connectionSetter.getOuterListener();
              if ( dialogListener != null ) {
                dialogListener.onDialogAccept( currentConnection );
              }
            } else {
              openErrorDialog( MessageHandler.getString( ERROR ), MessageHandler//$NON-NLS-1$
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
    currentConnection.setId( existingConnectionId );

    if ( previousConnectionName != null ) {
      RequestBuilder deleteConnectionBuilder =
          new RequestBuilder( RequestBuilder.DELETE, getServiceURL( "deletebyname", new String[][] { { "name", previousConnectionName } } ) );
      try {
        deleteConnectionBuilder.sendRequest( null, new RequestCallback() {

          @Override
          public void onError( Request request, Throwable exception ) {
            displayErrorMessage( exception );
          }

          @Override
          public void onResponseReceived( Request request, Response response ) {
            try {
              if ( response.getStatusCode() != Response.SC_OK ) {
                openErrorDialog( MessageHandler.getString( ERROR ), MessageHandler//$NON-NLS-1$
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
  public void closeSaveConnectionConfirmationDialog() {
    saveConnectionConfirmationDialog.hide();
  }

  @Bindable
  public void testConnection() {
    RequestBuilder testConnectionBuilder = new RequestBuilder( RequestBuilder.PUT, getServiceURL( "test" ) );
    testConnectionBuilder.setHeader( CONTENT_TYPE, JSON );
    try {
      AutoBean<IDatabaseConnection> bean = AutoBeanUtils.getAutoBean( currentConnection );
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
              openErrorDialog( MessageHandler.getString( ERROR ), MessageHandler//$NON-NLS-1$
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
        new RequestBuilder( RequestBuilder.DELETE, getServiceURL( "deletebyname", new String[][] { { "name", datasourceModel.getSelectedRelationalConnection().getName() } } ) );
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
              openErrorDialog( MessageHandler.getString( ERROR ), MessageHandler//$NON-NLS-1$
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
    if ( !listeners.contains( listener ) ) {
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

  public void showAddConnectionDialog( DialogListener listener ) {
    previousConnectionName = null;
    existingConnectionName = previousConnectionName;
    connectionSetter = new DatabaseConnectionSetter( listener );
    showAddConnectionDialog();
  }

  @Bindable
  public void showAddConnectionDialog() {
    datasourceModel.setEditing( false );
    if ( databaseDialog != null ) {
      databaseDialog.setDatabaseConnection( null );
      databaseDialog.show();
    } else {
      createNewDatabaseDialog();
    }
  }

  private void createNewDatabaseDialog() {
    if ( databaseTypeHelper == null ) {
      XulServiceCallback<List<IDatabaseType>> callback = new XulServiceCallback<List<IDatabaseType>>() {
        public void error( String message, Throwable error ) {
          Window.alert( message + ":  " + error.getLocalizedMessage() );
        }

        public void success( List<IDatabaseType> retVal ) {
          databaseTypeHelper = new DatabaseTypeHelper( retVal );
          databaseDialog =
              new GwtDatabaseDialog( databaseTypeHelper,
                  GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter ); //$NON-NLS-1$
        }
      };
      dialectService.getDatabaseTypes( callback );
    } else {
      databaseDialog =
          new GwtDatabaseDialog( databaseTypeHelper,
              GWT.getModuleBaseURL() + "dataaccess-databasedialog.xul", connectionSetter ); //$NON-NLS-1$
    }
  }

  @SuppressWarnings( "deprecation" )
  @Bindable
  public void showEditConnectionDialog() {
    showEditConnectionDialog( null );
  }

  @SuppressWarnings( "deprecation" )
  public void showEditConnectionDialog( DialogListener dialogListener ) {
    IDatabaseConnection connection = datasourceModel.getSelectedRelationalConnection();
    showEditConnectionDialog( dialogListener, connection );
  }

  @SuppressWarnings( "deprecation" )
  public void showEditConnectionDialog( DialogListener dialogListener, IDatabaseConnection connection ) {
    connectionSetter = new DatabaseConnectionSetter( dialogListener );
    datasourceModel.setEditing( true );
    if ( databaseDialog != null ) {
      if ( connection != null ) {
        databaseDialog.setDatabaseConnection( connection );
        previousConnectionName = connection.getName();
        existingConnectionName = previousConnectionName;
        databaseDialog.show();
      } else {
        openErrorDialog( MessageHandler.getString( "DatasourceEditor.USER_ERROR_TITLE" ), MessageHandler
          .getString( "DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED" ) );
      }
    } else {
      createNewDatabaseDialog();
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
    existingConnectionId = null;
    overwriteConnectionConfirmationDialog.hide();
  }

  @Bindable
  public void closeSuccessDetailsDialog() {
    if ( !successDetailsDialog.isHidden() ) {
      successDetailsDialog.hide();
    }
  }

  public void reloadConnections() {
    String cacheBuster = String.valueOf( new java.util.Date().getTime() );
    String[][] params = new String[][] { { "ts", cacheBuster } };
    RequestBuilder listConnectionBuilder = new RequestBuilder( RequestBuilder.GET, getServiceURL( "list", params ) );
    listConnectionBuilder.setHeader( CONTENT_TYPE, JSON );
    try {
      listConnectionBuilder.sendRequest( null, new RequestCallback() {

        @Override
        public void onError( Request request, Throwable exception ) {
          MessageHandler.getInstance().showErrorDialog(
              MessageHandler.getString( ERROR ),
              MessageHandler.getString( "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", exception
                  .getLocalizedMessage() ) );
        }

        @Override
        public void onResponseReceived( Request request, Response response ) {
          AutoBean<IDatabaseConnectionList> bean =
              AutoBeanCodex.decode( connectionAutoBeanFactory, IDatabaseConnectionList.class, response.getText() );
          List<IDatabaseConnection> connectionBeanList = bean.as().getDatabaseConnections();
          List<IDatabaseConnection> connectionImplList = new ArrayList<IDatabaseConnection>();

          for ( IDatabaseConnection connectionBean : connectionBeanList ) {
            try {
              // take anything except connections where STANDARD_CONNECTION == false
              if ( ( connectionBean.getAttributes() == null )
                  || ( connectionBean.getAttributes().get( ATTRIBUTE_STANDARD_CONNECTION ) == null )
                  || ( connectionBean.getAttributes().get( ATTRIBUTE_STANDARD_CONNECTION ) == Boolean.TRUE.toString() ) ) {
                connectionImplList.add( AutobeanUtilities.connectionBeanToImpl( connectionBean ) );
              }

            } catch ( Exception e ) {
              // skip invalid connections that couldn't be converted to IDatabaseConnection
            }
          }
          if ( datasourceModel != null ) {
            datasourceModel.getGuiStateModel().setConnections( connectionImplList );
          }
        }
      } );
    } catch ( RequestException e ) {
      MessageHandler.getInstance().showErrorDialog( MessageHandler.getString( ERROR ),
          "DatasourceEditor.ERROR_0004_CONNECTION_SERVICE_NULL" );
    }
  }

  public static String getBaseURL() {
    String moduleUrl = GWT.getModuleBaseURL();
    //
    // Set the base url appropriately based on the context in which we are running this client
    //
    if ( moduleUrl.indexOf( "content" ) > -1 ) {
      // we are running the client in the context of a BI Server plugin, so
      // point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring( 0, moduleUrl.indexOf( "content" ) );
      return baseUrl + "plugin/data-access/api/connection/";
    }

    return moduleUrl + "plugin/data-access/api/connection/";
  }

  public static String getServiceURL( String action ) {
    return getBaseURL() + action;
  }

  public static String getServiceURL( String action, String[][] parameters ) {
    StringBuilder stringBuilder = new StringBuilder( action );
    String[] encodeArguments = new String[parameters.length];
    for ( int i = 0; i < parameters.length; i++ ) {
      stringBuilder.append( i == 0 ? "?" : "&" );
      stringBuilder.append( parameters[i][0] );
      stringBuilder.append( "=" );
      stringBuilder.append( "{" + i + "}" );
      encodeArguments[i] = parameters[i][1];
    }

    return getServiceURL( NameUtils.URLEncode( stringBuilder.toString(), encodeArguments ) );
  }

  class DatabaseConnectionSetter implements DatabaseDialogListener {

    final DialogListener wrappedListener;

    public DatabaseConnectionSetter( DialogListener listener ) {
      super();
      this.wrappedListener = listener;
    }

    public DialogListener getOuterListener() {
      return wrappedListener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.pentaho.ui.database.event.DatabaseDialogListener#onDialogAccept(org.pentaho.database.model.IDatabaseConnection
     * )
     */
    public void onDialogAccept( final IDatabaseConnection connection ) {
      currentConnection = connection;
      handleDialogAccept();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogCancel()
     */
    public void onDialogCancel() {
      wrappedListener.onDialogCancel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.pentaho.ui.database.event.DatabaseDialogListener#onDialogReady()
     */
    public void onDialogReady() {
      if ( !datasourceModel.isEditing() ) {
        showAddConnectionDialog( wrappedListener );
      } else {
        showEditConnectionDialog( wrappedListener );
      }
    }
  }
}
