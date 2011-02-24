package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.ArrayList;
import java.util.List;

/**
 * User: nbaker
 * Date: Jul 2, 2010
 */
public class CsvSourcedRelationalModel extends XulEventSourceAdapter {
  private RelationalModelValidationListenerCollection relationalModelValidationListeners;
  private boolean validated;
  private boolean previewValidated;
  private boolean applyValidated;
  private String datasourceName;
  public static enum ConnectionEditType {ADD, EDIT};
  private List<ModelDataRow> dataRows = new ArrayList<ModelDataRow>();
  private String previewLimit;
  private ConnectionEditType editType = ConnectionEditType.ADD;
  private BusinessData businessData;
  private String fileName;

  public CsvSourcedRelationalModel() {
    previewLimit = "10";
  }

  @Bindable
  public ConnectionEditType getEditType() {
    return editType;
  }

  @Bindable
  public void setEditType(ConnectionEditType value) {
    this.editType = value;
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
    if (getBusinessData() != null &&
        getBusinessData().getDomain() != null) {
      Domain domain = getBusinessData().getDomain();
      domain.setId(datasourceName);
      LogicalModel model = domain.getLogicalModels().get(0);
      String localeCode = domain.getLocales().get(0).getCode();
      model.getName().setString(localeCode, datasourceName);
    }

    this.firePropertyChange("datasourcename", previousVal, datasourceName); //$NON-NLS-1$
  }
  
  @Bindable
  public String getPreviewLimit() {
    return previewLimit;
  }

  @Bindable
  public void setPreviewLimit(String value) {
    String previousVal = this.previewLimit;
    this.previewLimit = value;
    this.firePropertyChange("previewLimit", previousVal, value); //$NON-NLS-1$
  }


  @Bindable
  public boolean isValidated() {
    return validated;
  }

  @Bindable
  private void setValidated(boolean value) {
    if(value != this.validated) {
      this.validated = value;
      this.firePropertyChange("validated", !value, value);
    }
  }

  @Bindable
  public BusinessData getBusinessData() {
    return businessData;
  }

  @Bindable
  public void setBusinessData(BusinessData value) {
    this.businessData = value;
    if (value != null) {
      Domain domain = value.getDomain();
      List<List<String>> data = value.getData();
      List<LogicalModel> logicalModels = domain.getLogicalModels();
      int columnNumber = 0;
      for (LogicalModel logicalModel : logicalModels) {
        List<Category> categories = logicalModel.getCategories();
        for (Category category : categories) {
          List<LogicalColumn> logicalColumns = category.getLogicalColumns();
          for (LogicalColumn logicalColumn : logicalColumns) {
            addModelDataRow(logicalColumn, getColumnData(columnNumber++, data), domain.getLocales().get(0).getCode());
          }
        }
      }
      firePropertyChange("dataRows", null, dataRows);
    } else {
      if (this.dataRows != null) {
        this.dataRows.removeAll(dataRows);
        List<ModelDataRow> previousValue = this.dataRows;
        firePropertyChange("dataRows", previousValue, null);
      }
    }
  }

  private void addModelDataRow(LogicalColumn column, List<String> columnData, String locale) {
    if (dataRows == null) {
      dataRows = new ArrayList<ModelDataRow>();
    }
    this.dataRows.add(new ModelDataRow(column, columnData, locale));
  }

  @Bindable
  public List<ModelDataRow> getDataRows() {
    return dataRows;
  }

  @Bindable
  public void setDataRows(List<ModelDataRow> value) {
    this.dataRows = value;
    firePropertyChange("dataRows", null, dataRows);
  }

  private List<String> getColumnData(int columnNumber, List<List<String>> data) {
    List<String> column = new ArrayList<String>();
    for (List<String> row : data) {
      if (columnNumber < row.size()) {
        column.add(row.get(columnNumber));
      }
    }
    return column;
  }

  /*
   * Clears out the model
   */
  @Bindable
  public void clearModel() {
    setBusinessData(null);
    setDataRows(null);
    setPreviewLimit("10");
    setDatasourceName("");
  }

  public void addRelationalModelValidationListener(IRelationalModelValidationListener listener) {
    if (relationalModelValidationListeners == null) {
      relationalModelValidationListeners = new RelationalModelValidationListenerCollection();
    }
    relationalModelValidationListeners.add(listener);
  }

  public void removeRelationalListener(IRelationalModelValidationListener listener) {
    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.remove(listener);
    }
  }

  /**
   * Fire all current {@link IRelationalModelValidationListener}.
   */
  void fireRelationalModelValid() {

    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.fireRelationalModelValid();
    }
  }

  /**
   * Fire all current {@link IRelationalModelValidationListener}.
   */
  void fireRelationalModelInValid() {

    if (relationalModelValidationListeners != null) {
      relationalModelValidationListeners.fireRelationalModelInValid();
    }
  }
  public void setPreviewValidated(boolean value) {
    if (value != this.previewValidated) {
      this.previewValidated = value;
      this.firePropertyChange("previewValidated", !value, this.previewValidated);
    }
  }
  public boolean isPreviewValidated() {
    return this.previewValidated;
  }

  public boolean isApplyValidated() {
    return applyValidated;
  }

  public void setApplyValidated(boolean value) {
    if (value != this.applyValidated) {
      this.applyValidated = value;
      this.firePropertyChange("applyValidated", !value, this.applyValidated);
    }
  }
}
