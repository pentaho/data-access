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
* Copyright (c) 2002-2021 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.gwt.widgets.client.utils.NameUtils;
import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.mantle.client.csrf.CsrfRequestBuilder;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.ui.service.DSWUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.MetadataUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.ui.service.MondrianUIDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.XMLParser;

public class DatasourceServiceManagerGwtImpl implements IXulAsyncDatasourceServiceManager {


  String getAnalysisDatasourceIdsURL = getWebAppRoot() + "plugin/data-access/api/datasource/analysis/ids"; //$NON-NLS-1$
  String getMetadataDatasourceIdsURL = getWebAppRoot() + "plugin/data-access/api/datasource/metadata/ids"; //$NON-NLS-1$
  String getDSWDatasourceIdsURL = getWebAppRoot() + "plugin/data-access/api/datasource/dsw/ids"; //$NON-NLS-1$

  String isAdminURL = getWebAppRoot() + "api/repo/files/canAdminister"; //$NON-NLS-1$

  @Override
  public void getAnalysisDatasourceIds( final XulServiceCallback<List<String>> xulCallback ) {
    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( final AsyncCallback callback ) {
        String cacheBuster = "?ts=" + new java.util.Date().getTime();
        RequestBuilder requestBuilder =
          new RequestBuilder( RequestBuilder.GET, getAnalysisDatasourceIdsURL + cacheBuster );
        try {
          requestBuilder.sendRequest( null, new RequestCallback() {
            @Override
            public void onError( Request request, Throwable exception ) {
              callback.onFailure( exception );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              if ( response.getStatusCode() == Response.SC_OK ) {
                callback.onSuccess( convertReponseToList( response ) );
              }
            }

          } );
        } catch ( RequestException e ) {
          xulCallback.error( e.getLocalizedMessage(), e );
        }
      }
    }, new AsyncCallback<List<String>>() {

      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( List<String> arg0 ) {
        xulCallback.success( arg0 );
      }

    } );
  }

  @Override
  public void getMetadataDatasourceIds( final XulServiceCallback<List<String>> xulCallback ) {
    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( final AsyncCallback callback ) {
        String cacheBuster = "?ts=" + new java.util.Date().getTime();
        RequestBuilder requestBuilder =
          new RequestBuilder( RequestBuilder.GET, getMetadataDatasourceIdsURL + cacheBuster );
        try {
          requestBuilder.sendRequest( null, new RequestCallback() {
            @Override
            public void onError( Request request, Throwable exception ) {
              callback.onFailure( exception );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              if ( response.getStatusCode() == Response.SC_OK ) {
                callback.onSuccess( convertReponseToList( response ) );
              }
            }

          } );
        } catch ( RequestException e ) {
          xulCallback.error( e.getLocalizedMessage(), e );
        }
      }
    }, new AsyncCallback<List<String>>() {

      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( List<String> arg0 ) {
        xulCallback.success( arg0 );
      }

    } );
  }


  @Override
  public void isAdmin( final XulServiceCallback<Boolean> xulCallback ) {
    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( final AsyncCallback callback ) {
        RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, isAdminURL );
        try {
          requestBuilder.sendRequest( null, new RequestCallback() {
            @Override
            public void onError( Request request, Throwable exception ) {
              callback.onFailure( exception );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              if ( response.getStatusCode() == Response.SC_OK ) {
                callback.onSuccess( Boolean.parseBoolean( response.getText() ) );
              }
            }

          } );
        } catch ( RequestException e ) {
          xulCallback.error( e.getLocalizedMessage(), e );
        }
      }
    }, new AsyncCallback<Boolean>() {

      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( Boolean arg0 ) {
        xulCallback.success( arg0 );
      }

    } );
  }

  public native String getWebAppRoot()/*-{
    if ($wnd.CONTEXT_PATH) {
      return $wnd.CONTEXT_PATH;
    }
    return "";
  }-*/;

  private List<String> convertReponseToList( Response response ) {
    List<String> dataList = new ArrayList<String>();
    Document document = (Document) XMLParser.parse( response.getText() );
    Element element = document.getDocumentElement();
    Node node = element.getFirstChild();
    boolean done = false;
    do {
      try {
        dataList.add( getNodeValueByTagName( node, "Item" ) );
        node = ( node.getNextSibling() != null ) ? node.getNextSibling() : null;

        if ( node == null ) {
          done = true;
        }
      } catch ( Exception e ) {
        done = true;
      }
    } while ( !done );

    return dataList;
  }

  /*
   * Get Node Value of the element matching the tag name
   */
  private String getNodeValueByTagName( Node node, String tagName ) {
    if ( node != null && node.getFirstChild() != null ) {
      return node.getFirstChild().getNodeValue();
    } else {
      return null;
    }
  }

  @Override
  public void getDSWDatasourceIds( final XulServiceCallback<List<String>> xulCallback ) {
    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand() {
      public void execute( final AsyncCallback callback ) {
        String cacheBuster = "?ts=" + new java.util.Date().getTime();
        RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, getDSWDatasourceIdsURL + cacheBuster );
        try {
          requestBuilder.sendRequest( null, new RequestCallback() {
            @Override
            public void onError( Request request, Throwable exception ) {
              callback.onFailure( exception );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              if ( response.getStatusCode() == Response.SC_OK ) {
                callback.onSuccess( convertReponseToList( response ) );
              }
            }

          } );
        } catch ( RequestException e ) {
          xulCallback.error( e.getLocalizedMessage(), e );
        }
      }
    }, new AsyncCallback<List<String>>() {

      public void onFailure( Throwable arg0 ) {
        xulCallback.error( arg0.getLocalizedMessage(), arg0 );
      }

      public void onSuccess( List<String> arg0 ) {
        xulCallback.success( arg0 );
      }

    } );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager#export(org
   * .pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void export( IDatasourceInfo dsInfo ) {
    String exportURL = null;
    String datasourceId = NameUtils.URLEncode( dsInfo.getId() );

    if ( dsInfo.getType() == MetadataUIDatasourceService.TYPE ) {
      exportURL = getWebAppRoot() + "plugin/data-access/api/datasource/metadata/" + datasourceId + "/download";
    } else if ( dsInfo.getType() == MondrianUIDatasourceService.TYPE ) {
      exportURL = getWebAppRoot() + "plugin/data-access/api/datasource/analysis/" + datasourceId + "/download";
    } else if ( dsInfo.getType() == DSWUIDatasourceService.TYPE ) {
      exportURL = getWebAppRoot() + "plugin/data-access/api/datasource/dsw/" + datasourceId + "/download";
    }
    Window.open( exportURL, "_new", "" );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager#remove(org
   * .pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void remove( IDatasourceInfo dsInfo, final Object xulCallback ) {
    final String removeURL;
    String datasourceId = NameUtils.URLEncode( dsInfo.getId() );
    if ( dsInfo.getType() == MetadataUIDatasourceService.TYPE ) {
      removeURL = getWebAppRoot() + "plugin/data-access/api/datasource/metadata/" + datasourceId + "/remove";
    } else if ( dsInfo.getType() == MondrianUIDatasourceService.TYPE ) {
      removeURL = getWebAppRoot() + "plugin/data-access/api/datasource/analysis/" + datasourceId + "/remove";
    } else if ( dsInfo.getType() == DSWUIDatasourceService.TYPE ) {
      removeURL = getWebAppRoot() + "plugin/data-access/api/datasource/dsw/" + datasourceId + "/remove";
    } else {
      removeURL = null;
    }

    AuthenticatedGwtServiceUtil.invokeCommand( new IAuthenticatedGwtCommand<Boolean>() {
      public void execute( final AsyncCallback<Boolean> callback ) {
        RequestBuilder requestBuilder = new CsrfRequestBuilder( RequestBuilder.POST, removeURL );
        try {

          requestBuilder.sendRequest( null, new RequestCallback() {
            @Override
            public void onError( Request request, Throwable exception ) {
              callback.onFailure( exception );
            }

            @Override
            public void onResponseReceived( Request request, Response response ) {
              callback.onSuccess( response.getStatusCode() == Response.SC_OK );
            }

          } );
        } catch ( RequestException e ) {
          XulServiceCallback<Boolean> responseCallback = (XulServiceCallback<Boolean>) xulCallback;
          responseCallback.error( e.getLocalizedMessage(), e );
        }
      }
    }, new AsyncCallback<Boolean>() {

      public void onFailure( Throwable e ) {
        XulServiceCallback<Boolean> responseCallback = (XulServiceCallback<Boolean>) xulCallback;
        responseCallback.error( e.getLocalizedMessage(), e );
      }

      public void onSuccess( Boolean arg ) {
        XulServiceCallback<Boolean> responseCallback = (XulServiceCallback<Boolean>) xulCallback;
        responseCallback.success( arg );
      }
    } );
  }
}
