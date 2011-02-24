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

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.ui.xul.XulServiceCallback;

public class DatasourceServiceDebugImpl implements IXulAsyncDatasourceService{

  InMemoryDatasourceServiceImpl SERVICE;
  public DatasourceServiceDebugImpl(){
    SERVICE = new InMemoryDatasourceServiceImpl();
  }
 
  public void doPreview(String connectionName, String query, String previewLimit, XulServiceCallback<SerializedResultSet> callback){
    try {    
      callback.success(SERVICE.doPreview(connectionName, query, previewLimit));
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }    
  }
  public void generateLogicalModel(String modelName, String connectionName, String dbType, String query, String previewLimit,
      XulServiceCallback<BusinessData> callback) {
      try {
        callback.success(SERVICE.generateLogicalModel(modelName, connectionName, dbType, query, previewLimit));
      } catch (DatasourceServiceException e) {
        callback.error(e.getLocalizedMessage(), e);
      }
  }

  public void saveLogicalModel(Domain domain, boolean overwrite, XulServiceCallback<Boolean> callback) {
    try {
      callback.success(SERVICE.saveLogicalModel(domain, overwrite));
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void hasPermission(XulServiceCallback<Boolean> callback) {
    callback.success(SERVICE.hasPermission());
  }

  public void deleteLogicalModel(String domainId, String modelName, XulServiceCallback<Boolean> callback) {
    try {
      Boolean res = SERVICE.deleteLogicalModel(domainId, modelName);
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void getLogicalModels(XulServiceCallback<List<LogicalModelSummary>> callback) {
    try {
      List<LogicalModelSummary> res = SERVICE.getLogicalModels();
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void loadBusinessData(String domainId, String modelId, XulServiceCallback<BusinessData> callback) {
    try {
      BusinessData res = SERVICE.loadBusinessData(domainId, modelId);
      callback.success(res);
    } catch (DatasourceServiceException e) {
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void serializeModelState(DatasourceDTO dto, XulServiceCallback<String> callback) {
    try{
      callback.success(SERVICE.serializeModelState(dto));
    } catch (DatasourceServiceException e){
      callback.error(e.getLocalizedMessage(), e);
    }
  }

  public void deSerializeModelState(String dtoStr, XulServiceCallback<DatasourceDTO> callback) {
    try{
      callback.success(SERVICE.deSerializeModelState(dtoStr));
    } catch (DatasourceServiceException e){
      callback.error(e.getLocalizedMessage(), e);
    }
  }
}

  