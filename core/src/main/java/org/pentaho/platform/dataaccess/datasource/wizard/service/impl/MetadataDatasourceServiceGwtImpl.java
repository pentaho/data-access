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

import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;

import static org.pentaho.mantle.client.environment.EnvironmentHelper.getFullyQualifiedURL;

@SuppressWarnings( "all" )
public class MetadataDatasourceServiceGwtImpl {

  String datasourceUrl = getFullyQualifiedURL()
    + "plugin/data-access/api/metadata/import?domainId={domainId}&metadataFile={metadataFile}&overwrite=false";

  public void importMetadataDatasource( final String domainId, final String metadataFile,
                                        final String localizeBundleEntries,
                                        final XulServiceCallback<String> xulCallback ) {
    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( final AsyncCallback callback ) {

        datasourceUrl = datasourceUrl.replaceAll( "{domainId}", domainId );
        datasourceUrl = datasourceUrl.replaceAll( "{metadataFile}", metadataFile );

        RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.PUT, datasourceUrl );
        requestBuilder.setHeader( "accept", "text/*" );
        requestBuilder.setHeader( "Content-Type", "text/plain" );
        try {
          requestBuilder.sendRequest( localizeBundleEntries, new RequestCallback() {
            @Override
            public void onError( Request request, Throwable exception ) {
              xulCallback.error( exception.getLocalizedMessage(), exception );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              if ( response.getStatusCode() == Response.SC_OK ) {
                callback.onSuccess( response.getText() );
              }
              if ( response.getStatusCode() == Response.SC_INTERNAL_SERVER_ERROR ) {
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
}
