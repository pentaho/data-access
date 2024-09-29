/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.platform.dataaccess.datasource.ui.importing.GwtImportDialog;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

import static org.pentaho.mantle.client.environment.EnvironmentHelper.getFullyQualifiedURL;

@SuppressWarnings( "all" )

public class AnalysisDatasourceServiceGwtImpl {

  String datasourceUrl = getFullyQualifiedURL()
    + "plugin/data-access/api/mondrian/putSchema?analysisFile={analysisFile}&databaseConnection={databaseConnection}";
  //$NON-NLS-1$

  @Deprecated
  public void importAnalysisDatasource( final String analysisFile, final String databaseConnection,
                                        final String parameters, final XulServiceCallback<String> xulCallback ) {
    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( final AsyncCallback callback ) {

        datasourceUrl = datasourceUrl.replaceAll( "{analysisFile}", NameUtils.URLEncode( analysisFile ) );
        datasourceUrl = datasourceUrl.replaceAll( "{databaseConnection}", NameUtils.URLEncode( databaseConnection ) );

        RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.PUT, datasourceUrl );
        requestBuilder.setHeader( "accept", "text/*" );
        requestBuilder.setHeader( "Content-Type", "text/plain" );
        try {
          requestBuilder.sendRequest( parameters, new RequestCallback() {
            @Override
            public void onError( Request request, Throwable exception ) {
              xulCallback.error( exception.getLocalizedMessage(), exception );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              if ( response.getStatusCode() == Response.SC_OK ) {
                callback.onSuccess( response.getText() );
              } else {
                // if (response.getStatusCode() == Response.SC_INTERNAL_SERVER_ERROR) {
                xulCallback.error( response.getText(), new Exception( response.getText() ) );
              }
            }

          } );
        } catch ( RequestException e ) {
          xulCallback.error( e.getLocalizedMessage(), e );
        }
      }
    }, new AsyncCallback<String>() {

      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( String arg0 ) {
        xulCallback.success( arg0 );
      }

    } );
  }

  public void importAnalysisDatasource( final String uploadedFile, final String name, final String parameters,
                                        final GwtImportDialog importDialog,
                                        final XulServiceCallback<String> xulCallback ) {
    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( final AsyncCallback callback ) {
        try {
          // importDialog.getAnalysisImportDialogController().removeHiddenPanels();
          //  importDialog.getAnalysisImportDialogController().buildAndSetParameters();
          // importDialog.getAnalysisImportDialogController().getFormPanel().submit();
          callback.onSuccess( "SUCCESS" );
        } catch ( Exception e ) {
          xulCallback.error( e.getLocalizedMessage(), e );
        }
      }
    }, new AsyncCallback<String>() {

      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( String arg0 ) {
        xulCallback.success( arg0 );
      }

    } );

  }

}
