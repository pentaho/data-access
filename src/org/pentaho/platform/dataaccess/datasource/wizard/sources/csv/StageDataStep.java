package org.pentaho.platform.dataaccess.datasource.wizard.sources.csv;

import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.*;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.AbstractWizardStep;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.XulException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.FactoryBasedBindingProvider;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTreeCell;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.containers.XulTreeItem;
import org.pentaho.ui.xul.containers.XulTreeRow;
import org.pentaho.ui.xul.stereotype.Bindable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class StageDataStep extends AbstractWizardStep implements IModelInfoValidationListener {


  private ICsvDatasourceServiceAsync csvDatasourceService;
  
  private static final String MSG_STAGING_DATA = "physicalDatasourceDialog.STAGING_DATA"; //$NON-NLS-1$
  private static final String MSG_ROWS_STAGED = "physicalDatasourceDialog.ROWS_STAGED"; //$NON-NLS-1$
  private static final String MSG_STAGING_FILE = "physicalDatasourceDialog.STAGING_FILE"; //$NON-NLS-1$
  private static final String MSG_STAGING_ERRORS = "physicalDatasourceDialog.STAGING_ERRORS"; //$NON-NLS-1$


  private XulDialog errorDialog = null;

  private XulLabel errorLabel = null;

  private XulDialog waitingDialog = null;

  private XulLabel waitingLabel = null;

  private XulDialog successDialog = null;

  private XulLabel successLabel = null;

  private XulDialog previewDialog = null;

  private XulLabel previewLabel = null;

  
  public StageDataStep(ICsvDatasourceServiceAsync csvDatasourceService ) {
    this.csvDatasourceService = csvDatasourceService;
    setFinishable(true);
  }
  
  public String getStepName() {
    return MessageHandler.getString("wizardStepName.STAGE"); //$NON-NLS-1$
  }

  public void setBindings() {
  }

  @Override
  public XulComponent getUIComponent() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void init(DatasourceModel model) throws XulException {
    super.init(model);
    
    
    waitingDialog = (XulDialog) document.getElementById("waitingDialog"); //$NON-NLS-1$
    waitingLabel = (XulLabel) document.getElementById("waitingDialogLabel"); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById("errorLabel"); //$NON-NLS-1$
    successDialog = (XulDialog) document.getElementById("successDialog"); //$NON-NLS-1$
    successLabel = (XulLabel) document.getElementById("successLabel"); //$NON-NLS-1$
    previewDialog = (XulDialog) document.getElementById("csvPreviewDialog"); //$NON-NLS-1$
    previewLabel = (XulLabel) document.getElementById("csvTextPreviewLabel"); //$NON-NLS-1$

    datasourceModel.getModelInfo().addModelInfoValidationListener(this);
  }

  @Override
  public void stepActivatingForward() {
    setStepImageVisible(true);
    showWaitingFileStageDialog();
    loadColumnData(datasourceModel.getModelInfo().getFileInfo().getTmpFilename());
  }

  @Override
  public void stepActivatingReverse() {
    setStepImageVisible(true);
  }
  
  private void loadColumnData(String selectedFile){
	String encoding = datasourceModel.getModelInfo().getFileInfo().getEncoding();
    try {
      clearColumnGrid();
    } catch (XulException e) {
      // couldn't clear the tree out
      e.printStackTrace();
    }

    if (datasourceModel.getGuiStateModel().isDirty()) {

      csvDatasourceService.stageFile(selectedFile,
          datasourceModel.getModelInfo().getFileInfo().getDelimiter(),
          datasourceModel.getModelInfo().getFileInfo().getEnclosure(),
          datasourceModel.getModelInfo().getFileInfo().getHeaderRows() > 0,
          encoding,
          new StageFileCallback());
    } else {
      refreshColumnGrid();
      closeWaitingDialog();
    }
  }

  public void onCsvValid() {
    //don't care about csv on this step
  }
  public void onCsvInValid() {
    //don't care about csv on this step
  }

  public void onModelInfoValid() {
    setFinishable(true);
  }

  public void onModelInfoInvalid() {
    setFinishable(false);
  }

  public class StageFileCallback implements AsyncCallback<ModelInfo> {

    public void onSuccess(ModelInfo aModelInfo) {      
      datasourceModel.getModelInfo().setColumns(aModelInfo.getColumns());
      datasourceModel.getModelInfo().setData(aModelInfo.getData());
      datasourceModel.getModelInfo().getFileInfo().setEncoding(aModelInfo.getFileInfo().getEncoding());
      refreshColumnGrid();
      closeWaitingDialog();
    }

    public void onFailure(Throwable caught) {
      closeWaitingDialog();
      if (caught instanceof CsvParseException) {
        CsvParseException e = (CsvParseException) caught;
        showErrorDialog(MessageHandler.getString(caught.getMessage(), String.valueOf(e.getLineNumber()), e.getOffendingLine()));
      } else {
        showErrorDialog(caught.getMessage());
      }
    }
  }

  @Override
  public boolean stepDeactivatingForward() {
    super.stepDeactivatingForward();
    return true;
  }
  @Override
  public boolean stepDeactivatingReverse() {
    setStepImageVisible(false);
    return true;
  }

    @Bindable
  public void closePreviewDialog() {
    previewDialog.hide();
  }
  
  @Bindable
  public void showPreviewDialog() throws Exception {
    previewLabel.setValue(datasourceModel.getModelInfo().getFileInfo().formatSampleContents());
    previewDialog.show();
  }

  public void clearColumnGrid() throws XulException {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    tree.setElements(null);
    tree.update();
  }

  @Bindable
  public void refreshColumnGrid() {
    generateDataTypeDisplay_horizontal();
  }
  
  private void generateDataTypeDisplay_horizontal() {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    tree.setRows(datasourceModel.getModelInfo().getColumns().length);

    tree.setBindingProvider(new FactoryBasedBindingProvider(bf) {
      @Override
      public BindingConvertor getConvertor(XulEventSource source, String prop1, XulEventSource target, String prop2) {
        if (source instanceof ColumnInfo) {
          if (prop1.equals("length") || prop1.equals("precision")) { //$NON-NLS-1$ //$NON-NLS-2$
            return BindingConvertor.integer2String();
          }
          else if (prop1.equals("include") && prop2.equals("value")) {  //$NON-NLS-1$//$NON-NLS-2$
            // this is the binding from the cell to the value of the checkbox
            return null;
          } else if (prop1.equals("include")) { //$NON-NLS-1$
            // this binding is from the model to the checkbox
            return BindingConvertor.boolean2String();
          } else if (prop1.equals("availableDataTypes")) { //$NON-NLS-1$
            return new BindingConvertor<List, Vector>() {
              @SuppressWarnings("unchecked")
              public Vector sourceToTarget(List value) {
               return new Vector(value);
              }
              @SuppressWarnings("unchecked")
              public List targetToSource(Vector value) {
                return new ArrayList(value);
              }
            };
          } else if (prop1.equals("formatStrings")) { //$NON-NLS-1$
              return new BindingConvertor<List, Vector>() {
                @SuppressWarnings("unchecked")
                public Vector sourceToTarget(List value) {
                 return new Vector(value);
                }
                @SuppressWarnings("unchecked")
                public List targetToSource(Vector value) {
                  return new ArrayList(value);
                }
              };
          } else if (prop1.equals("dataType") && prop2.equals("selectedIndex")) { //$NON-NLS-1$ //$NON-NLS-2$
            return new BindingConvertor<DataType, Integer>() {
              @Override
              public Integer sourceToTarget(DataType value) {
                List<DataType> types = ColumnInfo.getAvailableDataTypes();
                for(int i = 0; i < types.size(); i++) {
                  if (types.get(i).equals(value)) {
                    return i;
                  }
                }
                return 0;
              }
              @Override
              public DataType targetToSource(Integer value) {
                return ColumnInfo.getAvailableDataTypes().get(value);
              }

            };
          } else if(prop1.equals("formatStringsDisabled")){
            return null;
          } else {
            return BindingConvertor.string2String();
          }
        } else {
          return null;
        }
      }
    });

    tree.setElements(Arrays.asList(datasourceModel.getModelInfo().getColumns()));
    if(datasourceModel.getModelInfo().getColumns().length > 0){
      tree.setSelectedRows(new int[]{0});
    }
    tree.update();
  }

  @Bindable
  public void selectAll() {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    for (XulComponent component : tree.getRootChildren().getChildNodes()) {
      XulTreeItem item = (XulTreeItem)component;
      for (XulComponent childComp : item.getChildNodes()) {
        XulTreeRow row = (XulTreeRow)childComp;
        XulTreeCell cell = row.getCell(0);
        cell.setValue(true);
      }
    }
    datasourceModel.getModelInfo().validate();
  }
  
  @Bindable
  public void deselectAll() {
    XulTree tree = (XulTree) document.getElementById("csvModelDataTable"); //$NON-NLS-1$
    for (XulComponent component : tree.getRootChildren().getChildNodes()) {
      XulTreeItem item = (XulTreeItem)component;
      for (XulComponent childComp : item.getChildNodes()) {
        XulTreeRow row = (XulTreeRow)childComp;
        XulTreeCell cell = row.getCell(0);
        cell.setValue(false);
      }
    }
    datasourceModel.getModelInfo().validate();
  }
  
  @Bindable
  public void closeErrorDialog() {
	  errorDialog.hide();
  }
  public void showErrorDialog(String message) {
    errorLabel.setValue(message);
    errorDialog.show();
  }

  public void closeWaitingDialog() {
    waitingDialog.hide();
  }
  public void showWaitingDataStageDialog() {
    MessageHandler.getInstance().showWaitingDialog(MessageHandler.getString(MSG_STAGING_DATA));
  }
  public void showWaitingFileStageDialog() {
    MessageHandler.getInstance().showWaitingDialog(MessageHandler.getString(MSG_STAGING_FILE));
  }

  @Bindable
  public void closeSuccessDialog() {
    successDialog.hide();
  }
  public void showSuccessDialog(String message) {
    successLabel.setValue(message);
    successDialog.show();
  }
  
}
