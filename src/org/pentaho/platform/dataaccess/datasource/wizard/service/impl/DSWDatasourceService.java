package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.api.datasource.DatasourceServiceException;
import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.platform.api.datasource.IDatasourceService;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.datasource.DatasourceInfo;

public class DSWDatasourceService implements IDatasourceService{

  IDSWDatasourceService dswService;
  IModelerService modelerService;
  public final static String EXT = ".xmi";
  public final static String TYPE = "Data Source Wizard";
  
  String defaultNewUI = "$wnd.pho.openDatasourceEditor({callback})";
  String defaultEditUI = "$wnd.pho.openEditDatasourceEditor({domainId}, {modelId}, {perspective} {callback})";
  boolean editable;
  boolean removable;
  boolean importable;
  boolean exportable;
  String newUI;
  String editUI;
  
  public  DSWDatasourceService() {
    dswService = new DSWDatasourceServiceImpl();
    modelerService = new ModelerService();
    this.editable = true;
    this.removable = true;
    this.importable = false;
    this.exportable = true;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public void add(String datasourceXml, boolean overwrite) throws DatasourceServiceException {

  }

  @Override
  public String get(String id) {
    return null;
  }

  @Override
  public void remove(String id) throws DatasourceServiceException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void update(String datasourceXml) throws DatasourceServiceException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<IDatasourceInfo> getIds() {
    List<IDatasourceInfo> datasourceList = new ArrayList<IDatasourceInfo>();
    try {
      for(LogicalModelSummary summary:dswService.getLogicalModels(null)) {
        Domain domain = modelerService.loadDomain(summary.getDomainId());
        List<LogicalModel> logicalModelList = domain.getLogicalModels();
        if(logicalModelList != null && logicalModelList.size() >= 1) {
          LogicalModel logicalModel = logicalModelList.get(0);
          Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
          if(property != null) {
            String id = summary.getDomainId();
            String name = null;
            int index = id.indexOf(EXT);
            if( index >=0) {
              name = id.substring(0, index);
            }
            datasourceList.add(new DatasourceInfo(name, summary.getDomainId(), TYPE, editable, removable, importable, exportable));
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
  public boolean exists(String id) throws PentahoAccessControlException {
    // TODO Auto-generated method stub
    return false;
  }
  @Override
  public void registerNewUI(String newUI) throws PentahoAccessControlException {
    this.newUI = newUI;    
  }
  @Override
  public void registerEditUI(String editUI) throws PentahoAccessControlException {
    this.editUI = editUI;
  }
  @Override
  public String getNewUI() throws PentahoAccessControlException {
    if(newUI == null) {
      return defaultNewUI;
    } else {
      return newUI;
    }
  }
  @Override
  public String getEditUI() throws PentahoAccessControlException {
    if(newUI == null) {
      return defaultNewUI;
    } else {
      return newUI;
    }
  }

}
