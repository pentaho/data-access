package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.api.datasource.GenericDatasourceServiceException;
import org.pentaho.platform.api.datasource.IGenericDatasource;
import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.api.datasource.IGenericDatasourceService;
import org.pentaho.platform.dataaccess.datasource.DSWDatasource;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.datasource.GenericDatasourceInfo;

public class DSWDatasourceService implements IGenericDatasourceService{

  IDatasourceService dswService;
  IModelerService modelerService;
  
  public final static String TYPE = "Data Source Wizard";

  public  DSWDatasourceService() {
    dswService = new DatasourceServiceImpl();
    modelerService = new ModelerService();
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void add(IGenericDatasource datasource) throws GenericDatasourceServiceException {

  }

  @Override
  public IGenericDatasource get(String id) {
    return null;
  }

  @Override
  public void remove(String id) throws GenericDatasourceServiceException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void edit(IGenericDatasource datasource) throws GenericDatasourceServiceException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<IGenericDatasource> getAll() {
    List<IGenericDatasource> datasourceList = new ArrayList<IGenericDatasource>();
    try {
      for(LogicalModelSummary summary:dswService.getLogicalModels(null)) {
        Domain domain = modelerService.loadDomain(summary.getDomainId());
        List<LogicalModel> logicalModelList = domain.getLogicalModels();
        if(logicalModelList != null && logicalModelList.size() >= 1) {
          LogicalModel logicalModel = logicalModelList.get(0);
          Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
          if(property != null) {
            datasourceList.add(new DSWDatasource(summary, summary.getDomainId(), TYPE));    
          }
        }
      }
    } catch (DatasourceServiceException e) {
      return null;
    } catch (Throwable e) {
      return null;
    }
    return datasourceList;
  }

  @Override
  public List<IGenericDatasourceInfo> getIds() {
    List<IGenericDatasourceInfo> datasourceList = new ArrayList<IGenericDatasourceInfo>();
    try {
      for(LogicalModelSummary summary:dswService.getLogicalModels(null)) {
        Domain domain = modelerService.loadDomain(summary.getDomainId());
        List<LogicalModel> logicalModelList = domain.getLogicalModels();
        if(logicalModelList != null && logicalModelList.size() >= 1) {
          LogicalModel logicalModel = logicalModelList.get(0);
          Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
          if(property != null) {
            datasourceList.add(new GenericDatasourceInfo(summary.getDomainId(), TYPE));
          }
        }
      }
    } catch (DatasourceServiceException e) {
      return null;
    } catch (Throwable e) {
      return null;
    }
    return datasourceList;
  }

}
