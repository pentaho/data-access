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

package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.database.event.IConnectionAutoBeanFactory;
import org.pentaho.ui.database.event.IDatabaseConnectionList;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;

public class JdbcDatasourceService implements IUIDatasourceAdminService{
  
  public static final String TYPE = "JDBC";
  private boolean editable = true;
  private boolean removable = true;
  private boolean importable = true;
  private boolean exportable = true;
  private boolean creatable = true;
  private String newUI = "builtin:";
  private String editUI = "builtin:";

  public static final String ATTRIBUTE_STANDARD_CONNECTION = "STANDARD_CONNECTION"; //$NON-NLS-1$

  protected IConnectionAutoBeanFactory connectionAutoBeanFactory;  

  public JdbcDatasourceService(/*IXulAsyncConnectionService connectionService*/) {
    connectionAutoBeanFactory = GWT.create(IConnectionAutoBeanFactory.class);
  }
  @Override
  public String getType() {
    return TYPE;
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
  
  public static String getMetadataBaseURL() {
    String moduleUrl = GWT.getModuleBaseURL();
    //
    //Set the base url appropriately based on the context in which we are running this client
    //
    if (moduleUrl.indexOf("content") > -1) {
      //we are running the client in the context of a BI Server plugin, so 
      //point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring(0, moduleUrl.indexOf("content"));
      return baseUrl + "plugin/data-access/api/metadataDA/";
    }

    return moduleUrl + "plugin/data-access/api/metadataDA/";
  }
 
  
  @Override
  public void getIds(final XulServiceCallback<List<IDatasourceInfo>> callback) {

    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, URL.encode(getMetadataBaseURL() + "getDatasourcePermissions"));
    requestBuilder.setHeader("Content-Type", "application/json");
    try {
      requestBuilder.sendRequest(null, new RequestCallback() {
        public void onError(Request request, Throwable exception) {
            callback.error(exception.getMessage(), exception);
        }

        public void onResponseReceived(Request request, Response response) {

          if (response.getText().equals("EDIT")) {
            String cacheBuster = "?ts=" + new java.util.Date().getTime();
            RequestBuilder listConnectionBuilder = new RequestBuilder(RequestBuilder.GET, URL.encode(getBaseURL() + "list" + cacheBuster));
            listConnectionBuilder.setHeader("Content-Type", "application/json");
            try {
                listConnectionBuilder.sendRequest(null, new RequestCallback() {

                @Override
                public void onError(Request request, Throwable exception) {
                  callback.error(exception.getMessage(), exception);
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                  AutoBean<IDatabaseConnectionList> bean = AutoBeanCodex.decode(connectionAutoBeanFactory, IDatabaseConnectionList.class, response.getText());
                  List<IDatabaseConnection> connections = bean.as().getDatabaseConnections();
                  List<IDatasourceInfo> datasourceInfos = new ArrayList<IDatasourceInfo>();
                  for(IDatabaseConnection connection:connections) {
                    // check attributes to make sure we only return "standard" connections which can be managed
                    Map<String,String> attributes = connection.getAttributes();
                    if(attributes.containsKey( ATTRIBUTE_STANDARD_CONNECTION )){
                      if(attributes.get( ATTRIBUTE_STANDARD_CONNECTION ).equals( Boolean.FALSE.toString() )){
                        continue;
                      }
                    }
                    datasourceInfos.add(new DatasourceInfo(connection.getName(), connection.getName(), TYPE, editable, removable, importable, exportable));
                  }
                  callback.success(datasourceInfos);
                }
             });
            } catch (RequestException e) {
              callback.error(e.getMessage(), e);
            }
          }
        }
      });
    } catch (RequestException e) {
      callback.error(e.getMessage(), e);
    }

  }

  @Override
  public String getNewUI() {
    return newUI;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#getEditUI(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public String getEditUI(IDatasourceInfo dsInfo) {
    return editUI;
  }

  
  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#export(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void export(IDatasourceInfo dsInfo) {
    // TODO Auto-generated method stub
    
  }
  
  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#remove(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void remove(IDatasourceInfo dsInfo, Object callback) {
	final XulServiceCallback<Boolean> responseCallback = (XulServiceCallback<Boolean>) callback;
    RequestBuilder deleteConnectionBuilder = new RequestBuilder(RequestBuilder.DELETE, URL.encode(getBaseURL() + "deletebyname?name=" + dsInfo.getName())); 
    try {
      deleteConnectionBuilder.sendRequest(null, new RequestCallback() {
    	  public void onResponseReceived(Request request, Response response) {
    		  responseCallback.success(response.getStatusCode() == Response.SC_OK);
          }

          public void onError(Request request, Throwable error) {
        	  responseCallback.error(error.getLocalizedMessage(), error);
          }
      });
    } catch (RequestException e) {
    	 responseCallback.error(e.getLocalizedMessage(), e);
    }
  }
  
//  /* (non-Javadoc)
//   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#remove(org.pentaho.platform.dataaccess.datasource.IDatasourceInfo)
//   */
//  @Override
//  public void remove(IDatasourceInfo dsInfo, XulServiceCallback<Boolean> callback) {
//    connectionService.deleteConnection(dsInfo.getName(), callback);
//  }

  /**
   * Return editable flag
   * @return
   */
  @Override public boolean isEditable() {
    return editable;
  }

  /**
   * Return removable flag
   * @return
   */
  @Override public boolean isRemovable() {
    return removable;
  }

  /**
   * Return importable flag
   * @return
   */
  @Override public boolean isImportable() {
    return importable;
  }

  /**
   * Return exportable flag
   * @return
   */
  @Override public boolean isExportable() {
    return exportable;
  }

  /**
   * Return creatable flag
   * @return
   */
  @Override public boolean isCreatable() {
    return creatable;
  }
}
