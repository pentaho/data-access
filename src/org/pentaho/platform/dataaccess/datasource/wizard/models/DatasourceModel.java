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
package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DatasourceModel extends XulEventSourceAdapter 
    implements IWizardModel, IRelationalModelValidationListener, IModelInfoValidationListener {
  
  private boolean validated;
  private DatasourceType datasourceType = DatasourceType.NONE;
  private transient GuiStateModel guiStateModel;
  private ModelInfo modelInfo;
  private Domain domain;
  private String datasourceName;
  private IConnection selectedRelationalConnection;
  private String query;

  public DatasourceModel() {
    guiStateModel = new GuiStateModel();
    guiStateModel.addRelationalModelValidationListener(this);

    CsvFileInfo fileInfo = new CsvFileInfo();
    modelInfo = new ModelInfo();
    modelInfo.setFileInfo(fileInfo);
    modelInfo.addModelInfoValidationListener(this);
  }

  @Bindable
  public GuiStateModel getGuiStateModel() {
    return guiStateModel;
  }

  @Bindable
  public void setGuiStateModel(GuiStateModel guiStateModel) {
    this.guiStateModel = guiStateModel;
  }

  @Bindable
  public String getDatasourceName() {
    return datasourceName;
  }

  @Bindable
  public void setDatasourceName(String datasourceName) {
    String previousVal = this.datasourceName;
    this.datasourceName = datasourceName;
    
    // if we're editing a generated or already defined domain,
    // we need to keep the datasource name in sync
    if (domain != null) {
      domain.setId(datasourceName);
      LogicalModel model = domain.getLogicalModels().get(0);
      String localeCode = domain.getLocales().get(0).getCode();
      model.getName().setString(localeCode, datasourceName);
    }
   
    this.firePropertyChange("datasourceName", previousVal, datasourceName); //$NON-NLS-1$
    validate();
  }

  @Bindable
  public IConnection getSelectedRelationalConnection() {
    return selectedRelationalConnection;
  }

  @Bindable
  public void setSelectedRelationalConnection(IConnection value) {
    IConnection previousValue = this.selectedRelationalConnection;
    this.selectedRelationalConnection = value;
    this.firePropertyChange("selectedRelationalConnection", previousValue, value);
    validate();
  }

  @Bindable
  public String getQuery() {
    return query;
  }

  @Bindable
  public void setQuery(String value) {
    String previousVal = this.query;
    this.query = value;
    this.firePropertyChange("query", previousVal, value); //$NON-NLS-1$
    validate();
  }
  
  public Domain getDomain() {
    return domain;
  }

  public void setDomain(Domain domain) {
    this.domain = domain;
    if (domain != null) {
      guiStateModel.setLogicalModels(domain.getLogicalModels());
      guiStateModel.setLocaleCode(domain.getLocales().get(0).getCode());
    } else {
      guiStateModel.setLogicalModels(null);
      guiStateModel.setLocaleCode(null);
    }
  }

  @Bindable
  public ModelInfo getModelInfo() {
    return modelInfo;
  }

  @Bindable
  public void setModelInfo(ModelInfo modelInfo) {
    // to avoid un-wiring bindings, don't just blow away the modelinfo object

	this.modelInfo.getFileInfo().setSavedEncoding(modelInfo.getFileInfo().getEncoding());	  
    this.modelInfo.setColumns(modelInfo.getColumns());
    this.modelInfo.setData(modelInfo.getData());
    this.modelInfo.setStageTableName(modelInfo.getStageTableName());    
    this.modelInfo.getFileInfo().setDelimiter(modelInfo.getFileInfo().getDelimiter());
    this.modelInfo.getFileInfo().setContents(modelInfo.getFileInfo().getContents());
    this.modelInfo.getFileInfo().setCurrencySymbol(modelInfo.getFileInfo().getCurrencySymbol());
    this.modelInfo.getFileInfo().setEnclosure(modelInfo.getFileInfo().getEnclosure());
    this.modelInfo.getFileInfo().setEncoding(modelInfo.getFileInfo().getEncoding());
    this.modelInfo.getFileInfo().setFileName(modelInfo.getFileInfo().getFileName());
    this.modelInfo.getFileInfo().setTmpFilename(modelInfo.getFileInfo().getTmpFilename());
    this.modelInfo.getFileInfo().setGroupSymbol(modelInfo.getFileInfo().getGroupSymbol());
    this.modelInfo.getFileInfo().setHeaderRows(modelInfo.getFileInfo().getHeaderRows());
    this.modelInfo.getFileInfo().setIfNull(modelInfo.getFileInfo().getIfNull());
    this.modelInfo.getFileInfo().setNullStr(modelInfo.getFileInfo().getNullStr());
    this.modelInfo.getFileInfo().setProject(modelInfo.getFileInfo().getProject());
    this.modelInfo.getFileInfo().setFriendlyFilename(modelInfo.getFileInfo().getFriendlyFilename());

    modelInfo.validate();
    validate();
  }

  @Bindable
  public boolean isValidated() {
    return validated;
  }

  @Bindable
  private void setValidated(boolean validated) {
    boolean prevVal = this.validated;
    this.validated = validated;
    this.firePropertyChange("validated", prevVal, validated); //$NON-NLS-1$
  }
  
  @Bindable
  public DatasourceType getDatasourceType() {
    return this.datasourceType;
  }

  @Bindable
  public void setDatasourceType(DatasourceType datasourceType) {
    DatasourceType previousVal = this.datasourceType;
    this.datasourceType = datasourceType;
    this.firePropertyChange("datasourceType", previousVal, datasourceType); //$NON-NLS-1$
    validate();
  }

  public void validate() {
    boolean value = false;
    if (datasourceName != null && datasourceName.length() > 0) {
      if (DatasourceType.SQL == getDatasourceType() && getSelectedRelationalConnection() != null && query != null && query.length() > 0) {
        guiStateModel.validateRelational();
        value = guiStateModel.isRelationalValidated();
      } else if (DatasourceType.CSV == getDatasourceType()) {
        guiStateModel.invalidateRelational();
        value = modelInfo.isValidated();
      }
    } else {
      guiStateModel.invalidateRelational();
    }
    setValidated(value);
  }

  /*
   * Clears out the model
   */
  public void clearModel() {
    // clear the models before switching the datasource type, otherwise
    // an error is presented to the user.
    setQuery("");
    setDatasourceName("");
    guiStateModel.clearModel();
    
    // BISERVER-3664: Temporary solution for IE ListBoxs not accepting -1 selectedIndex.
    // Explicitly selecting the first connection object makes all browsers behave the same.
    IConnection firstConnection = guiStateModel.getConnections().size() > 0 ? guiStateModel.getConnections().get(0) : null;
    setSelectedRelationalConnection(firstConnection);

    modelInfo.clearModel();
    guiStateModel.setDataStagingComplete(false);
    setDatasourceType(DatasourceType.NONE);
    validate();
  }
  
//  @Bindable
//  public IDatasource getDatasource() {
//    IDatasource datasource = new Datasource();
//    if(DatasourceType.SQL == getDatasourceType()) {
//      datasource.setBusinessData(getRelationalModel().getBusinessData());
//      datasource.setConnections(getRelationalModel().getConnections());
//      datasource.setQuery(getRelationalModel().getQuery());
//      datasource.setSelectedConnection(getRelationalModel().getSelectedConnection());
//    } else {
//      datasource.setBusinessData(null);
//      datasource.setSelectedFile(getModelInfo().getFileInfo().getFilename());
//      datasource.setHeadersPresent(getModelInfo().getFileInfo().getHeaderRows() > 0);
//
//    }
//    return datasource;
//  }

  public void onRelationalModelInValid() {
    if(DatasourceType.SQL == getDatasourceType()) {
      setValidated(false);
    }
  }

  public void onRelationalModelValid() {
    if(DatasourceType.SQL == getDatasourceType()) {
      setValidated(true);
    }
  }
  
  /**
   * This is a utility method that looks into an old domain for the same column ids, and then 
   * copies over the old metadata into the new.
   * @param oldDomain
   * @param newDomain
   */
  public void copyOverMetadata(Domain oldDomain, Domain newDomain) {
    Category category = newDomain.getLogicalModels().get(0).getCategories().get(0);
    LogicalModel oldModel = oldDomain.getLogicalModels().get(0);
    for (LogicalColumn column : category.getLogicalColumns()) {
      LogicalColumn oldColumn = oldModel.findLogicalColumn(column.getId());
      if (oldColumn != null) {
        column.setDataType(oldColumn.getDataType());
        column.setName(oldColumn.getName());
        column.setAggregationList(oldColumn.getAggregationList());
        column.setAggregationType(oldColumn.getAggregationType());
      }
    }
  }

  public void onModelInfoInvalid() {
    if(DatasourceType.CSV == getDatasourceType()) {
      setValidated(false);
    }
  }

  public void onModelInfoValid() {
    if(DatasourceType.CSV == getDatasourceType()) {
      setValidated(true);
    }
  }


  /**
   * Strips all non-alphanumeric characters from the datasourceName, replaces
   * spaces with underscores, and finally lowercases the string for return making it
   * valid for use as a table name
   * @return
   */
  public String generateTableName() {
    if (datasourceName == null) {
      throw new IllegalStateException("DatasourceName must not be null, cannot generate a valid table name"); //$NON-NLS-1$
    }
    return datasourceName.trim().replace(" ", "_")  //$NON-NLS-1$ //$NON-NLS-2$
            .replaceAll("[^A-Za-z0-9_-]", "")          //$NON-NLS-1$ //$NON-NLS-2$
            .toLowerCase();       // change to lower to handle case sensitivity of quoted table names in postgresql (which defaults all tables to lowercase)... BISERVER-5231
  }

  public void onCsvInValid() {
  }

  public void onCsvValid() {
  }

}
