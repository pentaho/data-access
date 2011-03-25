package org.pentaho.platform.dataaccess.datasource.wizard.steps;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.StageDataController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvParseException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IModelInfoValidationListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class StageDataStep extends AbstractWizardStep implements IModelInfoValidationListener {


  private StageDataController stageDataController = new StageDataController();
  private ICsvDatasourceServiceAsync csvDatasourceService;
  
  public StageDataStep(ICsvDatasourceServiceAsync csvDatasourceService ) {
    this.csvDatasourceService = csvDatasourceService;
    setFinishable(true);
  }
  
  public String getStepName() {
    return MessageHandler.getInstance().messages.getString("wizardStepName.STAGE"); //$NON-NLS-1$
  }

  public void setBindings() {
  }

  @Override
  public XulComponent getUIComponent() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void init(XulDomContainer mainWizardContainer) throws XulException {
    super.init(mainWizardContainer);
    setDocument(mainWizardContainer.getDocumentRoot());

    stageDataController.setXulDomContainer(mainWizardContainer);
    stageDataController.setBindingFactory(getBindingFactory());
    stageDataController.setDatasourceModel(getDatasourceModel());
    stageDataController.init();
    mainWizardContainer.addEventHandler(stageDataController);

    getDatasourceModel().getModelInfo().addModelInfoValidationListener(this);
  }

  @Override
  public void stepActivatingForward() {
    setStepImageVisible(true);
    stageDataController.showWaitingFileStageDialog();
    loadColumnData(getDatasourceModel().getModelInfo().getFileInfo().getTmpFilename());
  }

  @Override
  public void stepActivatingReverse() {
    setStepImageVisible(true);
  }
  
  private void loadColumnData(String selectedFile){
	String encoding = getDatasourceModel().getModelInfo().getFileInfo().getEncoding();
    try {
      stageDataController.clearColumnGrid();
    } catch (XulException e) {
      // couldn't clear the tree out
      e.printStackTrace();
    }

    if (getDatasourceModel().getGuiStateModel().isDirty()) {

      csvDatasourceService.stageFile(selectedFile,
          getDatasourceModel().getModelInfo().getFileInfo().getDelimiter(),
          getDatasourceModel().getModelInfo().getFileInfo().getEnclosure(),
          getDatasourceModel().getModelInfo().getFileInfo().getHeaderRows() > 0,
          encoding,
          new StageFileCallback());
    } else {
      stageDataController.refreshColumnGrid();
      stageDataController.closeWaitingDialog();
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
      getDatasourceModel().getModelInfo().setColumns(aModelInfo.getColumns());
      getDatasourceModel().getModelInfo().setData(aModelInfo.getData());
      getDatasourceModel().getModelInfo().getFileInfo().setEncoding(aModelInfo.getFileInfo().getEncoding());
      stageDataController.refreshColumnGrid();
      stageDataController.closeWaitingDialog();
    }

    public void onFailure(Throwable caught) {
      stageDataController.closeWaitingDialog();
      if (caught instanceof CsvParseException) {
        CsvParseException e = (CsvParseException) caught;
        stageDataController.showErrorDialog(MessageHandler.getInstance().messages.getString(caught.getMessage(), String.valueOf(e.getLineNumber()), e.getOffendingLine()));
      } else {
        stageDataController.showErrorDialog(caught.getMessage());
      }
    }
  }

  @Override
  public boolean stepDeactivatingForward() {
    super.stepDeactivatingForward();
    return stageDataController.finishing();
  }
  @Override
  public boolean stepDeactivatingReverse() {
    setStepImageVisible(false);
    return true;
  }

}
