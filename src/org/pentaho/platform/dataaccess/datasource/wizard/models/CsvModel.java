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
 * Created May 26, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.Delimiter;
import org.pentaho.platform.dataaccess.datasource.Enclosure;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
/**
 * @deprecated Use {@link ModelInfo} instead
 * @author rfellows
 *
 */
@Deprecated
public class CsvModel extends XulEventSourceAdapter{
  private CsvModelValidationListenerCollection csvModelValidationListeners;
  private boolean validated;
  private String datasourceName;
  private BusinessData businessData;
  private boolean headersPresent = false;
  private List<CsvModelDataRow> dataRows = new ArrayList<CsvModelDataRow>();
  private String selectedFile = null;
  private Enclosure enclosure;
  private Delimiter delimiter;
  private List<String> enclosureList;
  private List<String> delimiterList;
  private DatasourceMessages messages;


  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  public CsvModel() {
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public List<String> getEnclosureList() {
    return enclosureList;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public void setEnclosureList() {
    enclosureList = new ArrayList<String>();
    Enclosure[] enclosureArray = Enclosure.values();
    for(int i=0;i<enclosureArray.length;i++) {
      enclosureList.add(getMessages().getString(enclosureArray[i].getName()));
    }
    this.firePropertyChange("enclosureList", null, enclosureList); //$NON-NLS-1$
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public List<String> getDelimiterList() {
    return delimiterList;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public void setDelimiterList() {
    delimiterList = new ArrayList<String>();
    Delimiter[] delimiterArray = Delimiter.values();
    for(int i=0;i<delimiterArray.length;i++) {
      delimiterList.add(getMessages().getString(delimiterArray[i].getName()));
    }
    this.firePropertyChange("delimiterList", null, delimiterList); //$NON-NLS-1$
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public String getDatasourceName() {
    return datasourceName;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
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
    validate();
  }
  
  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public BusinessData getBusinessData() {
    return businessData;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
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
            addCsvModelDataRow(logicalColumn, getColumnData(columnNumber++, data), domain.getLocales().get(0).getCode());
          }
        }
      }
      firePropertyChange("dataRows", null, dataRows);//$NON-NLS-1$
    } else {
      if (this.dataRows != null) {
        this.dataRows.removeAll(dataRows);
        List<CsvModelDataRow> previousValue = this.dataRows;
        firePropertyChange("dataRows", previousValue, null);//$NON-NLS-1$
      }
    }
    validate();
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public boolean isHeadersPresent() {
    return headersPresent;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public void setHeadersPresent(boolean value) {
    if(value != this.headersPresent) {
      this.headersPresent = value;
      this.firePropertyChange("headersPresent", !value, this.headersPresent); //$NON-NLS-1$
      validate();      
    }
  }


  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public String getSelectedFile() {
    return selectedFile;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public void setSelectedFile(String value) {
    String previousVal = this.selectedFile;
    this.selectedFile = value;
    this.firePropertyChange("selectedFile", previousVal, value); //$NON-NLS-1$
    validate();
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public boolean isValidated() {
    return validated;
  }


  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  private void setValidated(boolean value) {
    if(value != this.validated) {
      this.validated = value;
      this.firePropertyChange("validated", !value, this.validated);//$NON-NLS-1$
    }
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  public void validate() {
    if (datasourceName != null && datasourceName.length() > 0 && getSelectedFile() != null && getSelectedFile().length() > 0 && getBusinessData() != null) {
      fireCsvModelValid();
      this.setValidated(true);
    } else {
      fireCsvModelInValid();
      this.setValidated(false);
    }
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  private void addCsvModelDataRow(LogicalColumn column, List<String> columnData,String locale) {
    if (dataRows == null) {
      dataRows = new ArrayList<CsvModelDataRow>();
    }
    this.dataRows.add(new CsvModelDataRow(column, columnData, locale));
  }


  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public List<CsvModelDataRow> getDataRows() {
    return dataRows;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public void setDataRows(List<CsvModelDataRow> dataRows) {
    this.dataRows = dataRows;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  private List<String> getColumnData(int columnNumber, List<List<String>> data) {
    List<String> column = new ArrayList<String>();
    for (List<String> row : data) {
      if (columnNumber < row.size()) {
        column.add(row.get(columnNumber));
      }
    }
    return column;
  }
  
  
  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public Enclosure getEnclosure() {
    return enclosure;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public void setEnclosure(Enclosure value) {
    Enclosure previousValue = this.enclosure;
    this.enclosure = value;
    this.firePropertyChange("enclosure", previousValue, value); //$NON-NLS-1$
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public Delimiter getDelimiter() {
    return delimiter;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  @Bindable
  public void setDelimiter(Delimiter value) {
    Delimiter previousValue = this.delimiter;
    this.delimiter = value;
    this.firePropertyChange("delimiter", previousValue, value); //$NON-NLS-1$
  }

  
  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  public DatasourceMessages getMessages() {
    return messages;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  public void setMessages(DatasourceMessages value) {
    this.messages = value;
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  public void clearModel() {
    setBusinessData(null);
    setDataRows(null);
    setSelectedFile(null);
    setDelimiter(Delimiter.COMMA);
    setEnclosure(Enclosure.DOUBLEQUOTE);
    setHeadersPresent(true);
    setDatasourceName("");
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  public void addCsvModelValidationListener(ICsvModelValidationListener listener) {
    if (csvModelValidationListeners == null) {
      csvModelValidationListeners = new CsvModelValidationListenerCollection();
    }
    csvModelValidationListeners.add(listener);
  }

  /**
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  public void removeCsvModelValidationListener(IRelationalModelValidationListener listener) {
    if (csvModelValidationListeners != null) {
      csvModelValidationListeners.remove(listener);
    }
  }

  /**
   * Fire all current {@link ICsvModelValidationListener}.
   * @deprecated Use {@link ModelInfo} instead
   */
  @Deprecated
  void fireCsvModelValid() {

    if (csvModelValidationListeners != null) {
      csvModelValidationListeners.fireCsvModelValid();
    }
  }
  /**
   * Fire all current {@link ICsvModelValidationListener}.
   * @deprecated Use {@link ModelInfo} instead
   */
  void fireCsvModelInValid() {

    if (csvModelValidationListeners != null) {
      csvModelValidationListeners.fireCsvModelInValid();
    }
  }

}
