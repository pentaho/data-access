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
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import org.pentaho.gwt.widgets.login.client.AuthenticatedGwtServiceUtil;
import org.pentaho.gwt.widgets.login.client.IAuthenticatedGwtCommand;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtDatasourceServiceAsync;
import org.pentaho.ui.xul.XulServiceCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class DatasourceServiceGwtImpl implements IXulAsyncDatasourceService {
  final static String ERROR = "ERROR:";

  static IGwtDatasourceServiceAsync SERVICE;

  static {

    SERVICE = (org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtDatasourceServiceAsync) GWT
        .create(org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtDatasourceService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    endpoint.setServiceEntryPoint(getBaseUrl());

  }

  /** 
   * Returns the context-aware URL to the rpc service
   */
  private static String getBaseUrl() {
    String moduleUrl = GWT.getModuleBaseURL();

    //
    //Set the base url appropriately based on the context in which we are running this client
    //
    if (moduleUrl.indexOf("content") > -1) {
      //we are running the client in the context of a BI Server plugin, so 
      //point the request to the GWT rpc proxy servlet
      String baseUrl = moduleUrl.substring(0, moduleUrl.indexOf("content"));
      //NOTE: the dispatch URL ("connectionService") must match the bean id for 
      //this service object in your plugin.xml.  "gwtrpc" is the servlet 
      //that handles plugin gwt rpc requests in the BI Server.
      return baseUrl + "gwtrpc/DatasourceService";
    }
    //we are running this client in hosted mode, so point to the servlet 
    //defined in war/WEB-INF/web.xml
    return moduleUrl + "DatasourceService";
  }

  /** 
   * Override the service entry point (use only if you know what you are doing)
   */
  public static void setServiceEntryPoint(String serviceEntryPointBase) {
    ServiceDefTarget endpoint = (ServiceDefTarget) SERVICE;
    endpoint.setServiceEntryPoint(serviceEntryPointBase + "gwtrpc/DatasourceService");
  }  
  
  public DatasourceServiceGwtImpl() {

  }

  public void doPreview(final String connectionName, final String query, final String previewLimit,
      final XulServiceCallback<SerializedResultSet> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {

        SERVICE.doPreview(connectionName, query, previewLimit, callback);
      }
    }, new AsyncCallback<SerializedResultSet>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);//$NON-NLS-1$
      }

      public void onSuccess(SerializedResultSet arg0) {
        xulCallback.success(arg0);
      }

    });
  }

  public void generateLogicalModel(final String modelName, final String connectionName, final String dbType, final String query,
      final String previewLimit, final XulServiceCallback<BusinessData> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {
        
        SERVICE.generateLogicalModel(modelName, connectionName, dbType, query, previewLimit, callback);
      }
    }, new AsyncCallback<BusinessData>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0);//$NON-NLS-1$  
      }

      public void onSuccess(BusinessData arg0) {
        xulCallback.success(arg0);
      }

    });
  }

  public void saveLogicalModel(final Domain domain, final boolean overwrite, final XulServiceCallback<Boolean> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {

        SERVICE.saveLogicalModel(domain, overwrite, callback);
      }
    },  new AsyncCallback<Boolean>() {
      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        xulCallback.success(arg0);
      }

    });

  }

  public void hasPermission(final XulServiceCallback<Boolean> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {

        SERVICE.hasPermission(callback);
      }
    }, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        xulCallback.success(arg0);
      }

    });
  }

  public void deleteLogicalModel(final String domainId, final String modelName,
      final XulServiceCallback<Boolean> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {

        SERVICE.deleteLogicalModel(domainId, modelName, callback);
      }
    }, new AsyncCallback<Boolean>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(Boolean arg0) {
        xulCallback.success(arg0);
      }

    });
  }

  public void getLogicalModels(final XulServiceCallback<List<LogicalModelSummary>> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {

        SERVICE.getLogicalModels(callback);
      }
    },new AsyncCallback<List<LogicalModelSummary>>() {

      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(List<LogicalModelSummary> arg0) {
        xulCallback.success(arg0);
      }

    });
  }

  public void loadBusinessData(final String domainId, final String modelId, final XulServiceCallback<BusinessData> xulCallback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {

        SERVICE.loadBusinessData(domainId, modelId, callback);
      }
    },new AsyncCallback<BusinessData>() {
      public void onFailure(Throwable arg0) {
        xulCallback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(BusinessData arg0) {
        xulCallback.success(arg0);
      }
    });
  }

  public void serializeModelState(final DatasourceDTO dto, final XulServiceCallback<String> callback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {

        SERVICE.serializeModelState(dto, callback);
      }
    },new AsyncCallback<String>() {
      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(String arg0) {
        callback.success(arg0);
      }
    });
  }

  public void deSerializeModelState(final String dtoStr, final XulServiceCallback<DatasourceDTO> callback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {
        SERVICE.deSerializeModelState(dtoStr, callback);
      }
    },new AsyncCallback<DatasourceDTO>() {
      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(DatasourceDTO arg0) {
        callback.success(arg0);
      }
    });
  }

  @Override
  public void listDatasourceNames(final XulServiceCallback<List<String>> callback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {
        SERVICE.listDatasourceNames(callback);
      }
    },new AsyncCallback<List<String>>() {
      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(List<String> arg0) {
        callback.success(arg0);
      }
    });
  }

  @Override
  public void generateQueryDomain(final String name, final String query, final String datasourceId, final IConnection connection, final DatasourceDTO datasourceDTO, final XulServiceCallback<IDatasourceSummary> callback) {
    AuthenticatedGwtServiceUtil.invokeCommand(new IAuthenticatedGwtCommand() {
      public void execute(AsyncCallback callback) {
        SERVICE.generateQueryDomain(name, query, datasourceId, connection, datasourceDTO, callback);
      }
    },new AsyncCallback<IDatasourceSummary>() {
      public void onFailure(Throwable arg0) {
        callback.error(arg0.getLocalizedMessage(), arg0); //$NON-NLS-1$
      }

      public void onSuccess(IDatasourceSummary arg0) {
        callback.success(arg0);
      }
    });
  }
}
