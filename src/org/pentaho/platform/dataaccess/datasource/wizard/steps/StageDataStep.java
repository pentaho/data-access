package org.pentaho.platform.dataaccess.datasource.wizard.steps;

import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.PhysicalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.StageDataController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvParseException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IModelInfoValidationListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class StageDataStep extends AbstractWizardStep<DatasourceModel> implements IModelInfoValidationListener {

  private DatasourceMessages datasourceMessages = null;
  private StageDataController stageDataController = null;
  private ICsvDatasourceServiceAsync csvDatasourceService = null;
  
  public StageDataStep(BindingFactory bf, DatasourceMessages messages) {   
    setFinishable(true);
    datasourceMessages = messages;
    setBindingFactory(bf);    
    csvDatasourceService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) csvDatasourceService;
    endpoint.setServiceEntryPoint(PhysicalDatasourceController.getDatasourceURL());    
  }
  
  public String getStepName() {
    return datasourceMessages.getString("wizardStepName.STAGE"); //$NON-NLS-1$
  }

  public void setBindings() {
  }

  @Override
  public void createPresentationComponent(XulDomContainer mainWizardContainer) throws XulException {
    super.createPresentationComponent(mainWizardContainer);
    setDocument(mainContainer.getDocumentRoot());
       
    stageDataController = new StageDataController();
    stageDataController.setDatasourceMessages(datasourceMessages);
    stageDataController.setXulDomContainer(mainWizardContainer);
    stageDataController.setBindingFactory(getBindingFactory());
    stageDataController.setDatasourceModel(getModel());    
    stageDataController.init();
    mainWizardContainer.addEventHandler(stageDataController);

    getModel().getModelInfo().addModelInfoValidationListener(this);
  }

  @Override
  public void stepActivatingForward() {
    setStepImageVisible(true);
    stageDataController.showWaitingFileStageDialog();
    loadColumnData(getModel().getModelInfo().getFileInfo().getTmpFilename());
  }

  @Override
  public void stepActivatingReverse() {
    setStepImageVisible(true);
  }
  
  private void loadColumnData(String selectedFile){
	String encoding = getModel().getModelInfo().getFileInfo().getEncoding();
    try {
      stageDataController.clearColumnGrid();
    } catch (XulException e) {
      // couldn't clear the tree out
      e.printStackTrace();
    }

    if (getModel().getGuiStateModel().isDirty()) {

      csvDatasourceService.stageFile(selectedFile,
          getModel().getModelInfo().getFileInfo().getDelimiter(),
          getModel().getModelInfo().getFileInfo().getEnclosure(),
          getModel().getModelInfo().getFileInfo().getHeaderRows() > 0,
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
      getModel().getModelInfo().setColumns(aModelInfo.getColumns());
      getModel().getModelInfo().setData(aModelInfo.getData());
      getModel().getModelInfo().getFileInfo().setEncoding(aModelInfo.getFileInfo().getEncoding());
      stageDataController.refreshColumnGrid();
      stageDataController.closeWaitingDialog();
    }

    public void onFailure(Throwable caught) {
      stageDataController.closeWaitingDialog();
      if (caught instanceof CsvParseException) {
        CsvParseException e = (CsvParseException) caught;
        stageDataController.showErrorDialog(datasourceMessages.getString(caught.getMessage(), String.valueOf(e.getLineNumber()), e.getOffendingLine()));        
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
