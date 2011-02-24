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
 * Created June 7, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.InlineEtlPhysicalModel;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.Delimiter;
import org.pentaho.platform.dataaccess.datasource.Enclosure;
import org.pentaho.platform.dataaccess.datasource.utils.ExceptionParser;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.WaitingDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.models.Aggregation;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvModelDataRow;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulCheckbox;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.components.XulTreeCol;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.TreeCellEditor;
import org.pentaho.ui.xul.util.TreeCellEditorCallback;
import org.pentaho.ui.xul.util.TreeCellRenderer;

import com.google.gwt.user.client.Window;

public class CsvDatasourceController extends AbstractXulEventHandler implements IDatasourceTypeController {
  public static final int MAX_SAMPLE_DATA_ROWS = 5;

  public static final int MAX_COL_SIZE = 13;

  public static final String EMPTY_STRING = "";//$NON-NLS-1$

  public static final String COMMA = ",";//$NON-NLS-1$

  private DatasourceMessages datasourceMessages;

  private WaitingDialog waitingDialogBox;

  private IXulAsyncDatasourceService service;

  private XulDialog regenerateModelConfirmationDialog = null;

  private XulDialog waitingDialog = null;

  private XulLabel waitingDialogLabel = null;

  private DatasourceModel datasourceModel;

  private BindingFactory bf;

  private XulTextbox datasourceName = null;

  private XulDialog errorDialog;

  private XulDialog successDialog;

  private XulLabel errorLabel = null;

  private XulLabel successLabel = null;

  private XulTree csvDataTable = null;

  private XulTextbox selectedFile = null;

  private XulCheckbox headersPresent = null;

  private XulTreeCol columnNameTreeCol = null;

  private XulTreeCol columnTypeTreeCol = null;

  //private XulTreeCol columnFormatTreeCol = null;
  private XulDialog aggregationEditorDialog = null;

  private XulDialog sampleDataDialog = null;

  private XulTree sampleDataTree = null;

  private CustomAggregateCellEditor aggregationCellEditor = null;

  private CustomSampleDataCellEditor sampleDataCellEditor = null;

  private CustomAggregationCellRenderer aggregationCellRenderer = null;

  private XulDialog applyCsvConfirmationDialog = null;

  private XulVbox csvAggregationEditorVbox = null;

  private CustomSampleDataCellRenderer sampleDataCellRenderer = null;

  private XulMenuList delimiterList = null;

  private XulMenuList enclosureList = null;

  private XulFileUpload fileUpload = null;

  private XulButton applyCsvButton = null;

  public CsvDatasourceController() {

  }

  @Bindable
  public void init() {
    fileUpload = (XulFileUpload) document.getElementById("fileUpload"); //$NON-NLS-1$
    applyCsvButton = (XulButton) document.getElementById("applyCsvButton"); //$NON-NLS-1$
    csvAggregationEditorVbox = (XulVbox) document.getElementById("csvAggregationEditorVbox"); //$NON-NLS-1$
    applyCsvConfirmationDialog = (XulDialog) document.getElementById("applyCsvConfirmationDialog"); //$NON-NLS-1$
    csvDataTable = (XulTree) document.getElementById("csvDataTable");//$NON-NLS-1$
    sampleDataTree = (XulTree) document.getElementById("csvSampleDataTable");//$NON-NLS-1$
    aggregationEditorDialog = (XulDialog) document.getElementById("csvAggregationEditorDialog");//$NON-NLS-1$
    aggregationCellEditor = new CustomAggregateCellEditor(aggregationEditorDialog, datasourceMessages, document, bf);
    csvDataTable.registerCellEditor("aggregation-cell-editor", aggregationCellEditor);//$NON-NLS-1$
    aggregationCellRenderer = new CustomAggregationCellRenderer();
    csvDataTable.registerCellRenderer("aggregation-cell-editor", aggregationCellRenderer);//$NON-NLS-1$
    sampleDataDialog = (XulDialog) document.getElementById("csvSampleDataDialog");//$NON-NLS-1$
    sampleDataCellEditor = new CustomSampleDataCellEditor(sampleDataDialog);
    csvDataTable.registerCellEditor("sample-data-cell-editor", sampleDataCellEditor);//$NON-NLS-1$
    sampleDataCellRenderer = new CustomSampleDataCellRenderer();
    csvDataTable.registerCellRenderer("sample-data-cell-editor", sampleDataCellRenderer);//$NON-NLS-1$
    regenerateModelConfirmationDialog = (XulDialog) document.getElementById("regenerateModelConfirmationDialog"); //$NON-NLS-1$
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    waitingDialogLabel = (XulLabel) document.getElementById("waitingDialogLabel");//$NON-NLS-1$    
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$    
    errorLabel = (XulLabel) document.getElementById("errorLabel");//$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel");//$NON-NLS-1$
    headersPresent = (XulCheckbox) document.getElementById("headersPresent"); //$NON-NLS-1$
    datasourceName = (XulTextbox) document.getElementById("datasourcename"); //$NON-NLS-1$
    selectedFile = (XulTextbox) document.getElementById("selectedFile"); //$NON-NLS-1$
    columnNameTreeCol = (XulTreeCol) document.getElementById("csvColumnNameTreeCol"); //$NON-NLS-1$
    columnTypeTreeCol = (XulTreeCol) document.getElementById("csvColumnTypeTreeCol"); //$NON-NLS-1$
    delimiterList = (XulMenuList) document.getElementById("delimiterList"); //$NON-NLS-1$
    enclosureList = (XulMenuList) document.getElementById("enclosureList"); //$NON-NLS-1$
    datasourceName = (XulTextbox) document.getElementById("relationalDatasourceName"); //$NON-NLS-1$

    //columnFormatTreeCol = (XulTreeCol) document.getElementById("csvColumnFormatTreeCol"); //$NON-NLS-1$    
    bf.setBindingType(Binding.Type.BI_DIRECTIONAL);
    final Binding domainBinding = bf.createBinding(datasourceModel.getModelInfo().getFileInfo(),
        "headerRows", headersPresent, "checked", BindingConvertor.integer2Boolean()); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getModelInfo(), "data", csvDataTable, "elements");//$NON-NLS-1$ //$NON-NLS-2$
//    bf.createBinding(datasourceModel.getCsvModel(), "delimiterList", delimiterList, "elements");//$NON-NLS-1$ //$NON-NLS-2$
//    bf.createBinding(datasourceModel.getCsvModel(), "enclosureList", enclosureList, "elements");//$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding(datasourceModel.getGuiStateModel().getSelectedCsvFile(), "name", fileUpload, "selectedFile");//$NON-NLS-1$ //$NON-NLS-2$
    BindingConvertor<String, Boolean> buttonConverter = new BindingConvertor<String, Boolean>() {

      @Override
      public Boolean sourceToTarget(String value) {
        return (value != null && value.length() > 0);
      }

      @Override
      public String targetToSource(Boolean value) {
        return null;
      }
    };

    bf.createBinding(datasourceModel.getModelInfo(), "stageTableName", applyCsvButton, "!disabled", buttonConverter); //$NON-NLS-1$ //$NON-NLS-2$
    BindingConvertor<Integer, Enclosure> indexToEnclosureConverter = new BindingConvertor<Integer, Enclosure>() {

      @Override
      public Enclosure sourceToTarget(Integer value) {
        if (value == 0) {
          return Enclosure.SINGLEQUOTE;
        } else if (value == 1) {
          return Enclosure.DOUBLEQUOTE;
        }
        return Enclosure.DOUBLEQUOTE;
      }

      @Override
      public Integer targetToSource(Enclosure value) {
        if (value == Enclosure.SINGLEQUOTE) {
          return 0;
        } else if (value == Enclosure.DOUBLEQUOTE) {
          return 1;
        }
        return 1;
      }
    };

    BindingConvertor<Integer, Delimiter> indexToDelimiterConverter = new BindingConvertor<Integer, Delimiter>() {

      @Override
      public Delimiter sourceToTarget(Integer value) {
        if (value == 0) {
          return Delimiter.COMMA;
        } else if (value == 1) {
          return Delimiter.TAB;
        } else if (value == 2) {
          return Delimiter.SEMICOLON;
        } else if (value == 3) {
          return Delimiter.SPACE;
        }
        return Delimiter.COMMA;
      }

      @Override
      public Integer targetToSource(Delimiter value) {
        if (value == Delimiter.COMMA) {
          return 0;
        } else if (value == Delimiter.TAB) {
          return 1;
        } else if (value == Delimiter.SEMICOLON) {
          return 2;
        } else if (value == Delimiter.SPACE) {
          return 3;
        }
        return 0;
      }
    };
    bf.createBinding(enclosureList, "selectedIndex", datasourceModel.getModelInfo().getFileInfo(), "enclosure", //$NON-NLS-1$ //$NON-NLS-2$
        indexToEnclosureConverter);
    bf.createBinding(delimiterList, "selectedIndex", datasourceModel.getModelInfo().getFileInfo(), "delimiter", //$NON-NLS-1$ //$NON-NLS-2$
        indexToDelimiterConverter);
    bf.setBindingType(Binding.Type.ONE_WAY);
    bf.createBinding(csvDataTable, "selectedIndex", this, "selectedCsvDataRow"); //$NON-NLS-1$ //$NON-NLS-2$
    try {
      domainBinding.fireSourceChanged();

    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
    }
    
    datasourceModel.getModelInfo().getFileInfo().setHeaderRows(1);
    datasourceModel.getModelInfo().getFileInfo().setDelimiter(Delimiter.COMMA.getValue());
    datasourceModel.getModelInfo().getFileInfo().setEnclosure(Enclosure.DOUBLEQUOTE.getValue());
    datasourceModel.getGuiStateModel().setSelectedCsvFile(null);
  }

  @Bindable
  public void setSelectedCsvDataRow(int row) {

  }

  public void setBindingFactory(BindingFactory bf) {
    this.bf = bf;
  }

  public void setDatasourceModel(DatasourceModel model) {
    this.datasourceModel = model;
  }

  public DatasourceModel getDatasourceModel() {
    return this.datasourceModel;
  }

  public String getName() {
    return "csvDatasourceController";//$NON-NLS-1$
  }

  public void setService(IXulAsyncDatasourceService service) {
    this.service = service;
  }

  @Bindable
  public void submitCsv() {
    fileUpload.submit();
  }

  @Bindable
  public void closeApplyCsvConfirmationDialog() {
    applyCsvConfirmationDialog.hide();
  }

  @Bindable
  private boolean validateIputForCsv() {
    return (datasourceModel.getGuiStateModel().getSelectedCsvFile() != null
        && (datasourceModel.getModelInfo().getStageTableName() != null && datasourceModel.getModelInfo().getStageTableName().length() > 0));
  }

  private String getMissingInputs() {
    StringBuffer buffer = new StringBuffer();
    if (datasourceModel.getGuiStateModel().getSelectedCsvFile() == null
        && datasourceModel.getGuiStateModel().getSelectedCsvFile().getModified().length() <= 0) {
      buffer.append(datasourceMessages.getString("datasourceDialog.FileMissing"));//$NON-NLS-1$
      buffer.append(" \n");//$NON-NLS-1$
    }
    if (datasourceModel.getModelInfo().getStageTableName() == null || datasourceModel.getModelInfo().getStageTableName().length() <= 0) {
      buffer.append(datasourceMessages.getString("datasourceDialog.DatasourceNameMissing"));//$NON-NLS-1$
      buffer.append(" \n");//$NON-NLS-1$
    }
    return buffer.toString();
  }
  @Bindable
  public void uploadFailure(Throwable t) {
    openErrorDialog(datasourceMessages.getString("DatasourceController.ERROR_0005_UPLOAD_FAILED"), t.getLocalizedMessage()); //$NON-NLS-1$
  }

  @Bindable
  public void openErrorDialog(String title, String message) {
    errorDialog.setTitle(title);
    errorLabel.setValue(message);
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
  }

  @Bindable
  public void openSuccesDialog(String title, String message) {
    successDialog.setTitle(title);
    successLabel.setValue(message);
    successDialog.show();
  }

  @Bindable
  public void closeSuccessDialog() {
    if (!successDialog.isHidden()) {
      successDialog.hide();
    }
  }

  /* public void showWaitingDialog(String title, String message) {
     getWaitingDialog().setTitle(title);
     getWaitingDialog().setMessage(message);
     getWaitingDialog().show();
   }

   public void hideWaitingDialog() {
     getWaitingDialog().hide();
   }
  */
  @Bindable
  public void showWaitingDialog(String title, String message) {
    waitingDialog.setTitle(title);
    waitingDialogLabel.setValue(message);
    waitingDialog.show();

  }

  @Bindable
  public void hideWaitingDialog() {
    waitingDialog.hide();
  }

  @Bindable
  public void closeRegenerateModelConfirmationDialog() {
    regenerateModelConfirmationDialog.hide();
  }

  public void displayErrorMessage(Throwable th) {
    errorDialog.setTitle(ExceptionParser.getErrorHeader(th, getDatasourceMessages().getString("DatasourceEditor.USER_ERROR_TITLE"))); //$NON-NLS-1$
    errorLabel.setValue(ExceptionParser.getErrorMessage(th, getDatasourceMessages().getString("DatasourceEditor.ERROR_0001_UNKNOWN_ERROR_HAS_OCCURED")));//$NON-NLS-1$
    errorDialog.show();
  }

  /**
   * @param datasourceMessages the datasourceMessages to set
   */
  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  /**
   * @return the waitingDialog
   */
  public WaitingDialog getWaitingDialog() {
    return this.waitingDialogBox;
  }

  /**
   * @param waitingDialog the waitingDialog to set
   */
  public void setWaitingDialog(WaitingDialog waitingDialog) {
    this.waitingDialogBox = waitingDialog;
  }

  /**
   * @return the datasourceMessages
   */
  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  @Bindable
  public void closeAggregationEditorDialog() {
    aggregationCellEditor.hide();
  }

  @Bindable
  public void saveAggregationValues() {
    aggregationCellEditor.notifyListeners();
  }

  @Bindable
  public void closeSampleDataDialog() {
    sampleDataCellEditor.hide();
  }

  private class CustomSampleDataCellEditor implements TreeCellEditor {
    XulDialog dialog = null;

    TreeCellEditorCallback callback = null;

    public CustomSampleDataCellEditor(XulDialog dialog) {
      super();
      this.dialog = dialog;
    }

    public Object getValue() {
      // TODO Auto-generated method stub
      return null;
    }

    public void hide() {
      dialog.hide();
    }

    public void setValue(Object val) {

    }

    public void show(int row, int col, Object boundObj, String columnBinding, TreeCellEditorCallback callback) {
      this.callback = callback;
      CsvModelDataRow csvModelDataRow = (CsvModelDataRow) boundObj;
      XulTreeCol column = sampleDataTree.getColumns().getColumn(0);
      column.setLabel(csvModelDataRow.getColumnName());
      List<String> values = csvModelDataRow.getSampleDataList();
      List<String> sampleDataList = new ArrayList<String>();
      for (int i = 1; i < MAX_SAMPLE_DATA_ROWS && i < csvModelDataRow.getSampleDataList().size(); i++) {
        sampleDataList.add(values.get(i));
      }
      sampleDataTree.setElements(sampleDataList);
      sampleDataTree.update();
      dialog.setTitle(datasourceMessages.getString("DatasourceController.SAMPLE_DATA"));//$NON-NLS-1$
      dialog.show();
    }
  }

  private class CustomAggregationCellRenderer implements TreeCellRenderer {

    public Object getNativeComponent() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getText(Object value) {
      StringBuffer buffer = new StringBuffer();
      if (value instanceof Aggregation) {
        Aggregation aggregation = (Aggregation) value;
        List<AggregationType> aggregationList = aggregation.getAggregationList();
        for (int i = 0; i < aggregationList.size(); i++) {
          if (buffer.length() + datasourceMessages.getString(aggregationList.get(i).getDescription()).length() < MAX_COL_SIZE) {
            buffer.append(datasourceMessages.getString(aggregationList.get(i).getDescription()));
            if ((i < aggregationList.size() - 1 && (buffer.length()
                + datasourceMessages.getString(aggregationList.get(i + 1).getDescription()).length() + COMMA.length() < MAX_COL_SIZE))) {
              buffer.append(COMMA);
            }
          } else {
            break;
          }
        }
      }
      return buffer.toString();
    }

    public boolean supportsNativeComponent() {
      // TODO Auto-generated method stub
      return false;
    }
  }

  private class CustomSampleDataCellRenderer implements TreeCellRenderer {

    public Object getNativeComponent() {
      // TODO Auto-generated method stub
      return null;
    }

    public String getText(Object value) {
      if (value instanceof String) {
        return getSampleData((String) value);
      } else if (value instanceof Vector) {
        Vector<String> vectorValue = (Vector<String>) value;
        StringBuffer sampleDataBuffer = new StringBuffer();
        for (int i = 0; i < vectorValue.size(); i++) {
          sampleDataBuffer.append(vectorValue.get(i));
        }
        return getSampleData(sampleDataBuffer.toString());
      }
      return EMPTY_STRING;
    }

    public boolean supportsNativeComponent() {
      // TODO Auto-generated method stub
      return false;
    }

    private String getSampleData(String sampleData) {
      if (sampleData != null && sampleData.length() > 0) {
        if (sampleData.length() <= MAX_COL_SIZE) {
          return sampleData;
        } else {
          return sampleData.substring(0, MAX_COL_SIZE);
        }
      }
      return EMPTY_STRING;
    }
  }

  public void initializeBusinessData(DatasourceModel model) {
    // modelDataTable.update();
    InlineEtlPhysicalModel physicalModel = (InlineEtlPhysicalModel) model.getDomain().getPhysicalModels().get(0);
    datasourceModel.setDatasourceType(DatasourceType.CSV);
    datasourceModel.getModelInfo().setStageTableName(model.getDomain().getId());
    datasourceModel.getModelInfo().getFileInfo().setDelimiter(physicalModel.getDelimiter());
    datasourceModel.getModelInfo().getFileInfo().setEnclosure(physicalModel.getEnclosure());
//    datasourceModel.getCsvModel().setHeadersPresent(model.getHeaderPresent());

    // update business data
//    datasourceModel.getCsvModel().setBusinessData(null);
    // Setting the editable property to true so that the table can be populated with correct cell types
    columnNameTreeCol.setEditable(true);
    columnTypeTreeCol.setEditable(true);
    //columnFormatTreeCol.setEditable(true); 
//    datasourceModel.getCsvModel().setBusinessData(model);
    datasourceModel.onCsvModelValid();
  }

  public boolean supportsBusinessData(DatasourceModel businessData) {
    return (businessData.getDomain().getPhysicalModels().get(0) instanceof InlineEtlPhysicalModel);
  }

  public boolean finishing() {
    return true;
  }

}
