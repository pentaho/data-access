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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncJoinSelectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtJoinSelectionServiceAsync;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class JoinSelectionServiceGwtImpl implements IXulAsyncJoinSelectionService {

  static IGwtJoinSelectionServiceAsync SERVICE;

  static {
    SERVICE = (org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtJoinSelectionServiceAsync) GWT
      .create( org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtJoinSelectionService.class );
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    endpoint.setServiceEntryPoint( getBaseUrl() );
  }

  /**
   * Returns the context-aware URL to the rpc service
   */
  private static String getBaseUrl() {
    String moduleUrl = GWT.getModuleBaseURL();

    //
    // Set the base url appropriately based on the context in which we are
    // running this client
    //
    if ( moduleUrl.indexOf( "content" ) > -1 ) { //$NON-NLS-1$
      // we are running the client in the context of a BI Server plugin,
      // so
      // point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring( 0, moduleUrl.indexOf( "content" ) ); //$NON-NLS-1$
      // NOTE: the dispatch URL ("connectionService") must match the bean
      // id for
      // this service object in your plugin.xml. "gwtrpc" is the servlet
      // that handles plugin gwt rpc requests in the BI Server.
      return baseUrl + "gwtrpc/joinSelectionService"; //$NON-NLS-1$
    }
    // we are running this client in hosted mode, so point to the servlet
    // defined in war/WEB-INF/web.xml
    return moduleUrl + "JoinSelectionService"; //$NON-NLS-1$
  }

  public static void setServiceEntryPoint( String serviceEntryPointBase ) {
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    endpoint.setServiceEntryPoint( serviceEntryPointBase + "gwtrpc/joinSelectionService" );
  }

  public JoinSelectionServiceGwtImpl() {

  }

  public void getDatabaseTables( final IDatabaseConnection connection, final String schema,
                                 final XulServiceCallback<List> xulCallback ) {

    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( AsyncCallback callback ) {
        SERVICE.getDatabaseTables( connection, schema, callback );
      }
    }, new AsyncCallback<List>() {
      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( List arg0 ) {
        xulCallback.success( arg0 );
      }
    } );
  }

  public void retrieveSchemas( final IDatabaseConnection connection, final XulServiceCallback<List> xulCallback ) {

    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( AsyncCallback callback ) {
        SERVICE.retrieveSchemas( connection, callback );
      }
    }, new AsyncCallback<List>() {
      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( List arg0 ) {
        xulCallback.success( arg0 );
      }
    } );
  }

  public void getTableFields( final String table, final IDatabaseConnection connection,
                              final XulServiceCallback<List> xulCallback ) {

    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( AsyncCallback callback ) {
        SERVICE.getTableFields( table, connection, callback );
      }
    }, new AsyncCallback<List>() {
      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( List arg0 ) {
        xulCallback.success( arg0 );
      }
    } );
  }

  public void serializeJoins( final MultiTableDatasourceDTO dto, final IDatabaseConnection selectedConnection,
                              final XulServiceCallback<IDatasourceSummary> xulCallback ) {

    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( AsyncCallback callback ) {
        SERVICE.serializeJoins( dto, selectedConnection, callback );
      }
    }, new AsyncCallback<IDatasourceSummary>() {
      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( IDatasourceSummary arg0 ) {
        xulCallback.success( arg0 );
      }
    } );
  }

  public void deSerializeModelState( final String source,
                                     final XulServiceCallback<MultiTableDatasourceDTO> xulCallback ) {

    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( AsyncCallback callback ) {
        SERVICE.deSerializeModelState( source, callback );
      }
    }, new AsyncCallback<MultiTableDatasourceDTO>() {
      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( MultiTableDatasourceDTO arg0 ) {
        xulCallback.success( arg0 );
      }
    } );
  }

  public void gwtWorkaround( final BogoPojo pojofinal, final XulServiceCallback<BogoPojo> xulCallback ) {

    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( AsyncCallback callback ) {
        SERVICE.gwtWorkaround( pojofinal, callback );
      }
    }, new AsyncCallback<BogoPojo>() {
      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( BogoPojo arg0 ) {
        xulCallback.success( arg0 );
      }
    } );
  }
}
