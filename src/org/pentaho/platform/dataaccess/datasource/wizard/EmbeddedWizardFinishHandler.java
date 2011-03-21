package org.pentaho.platform.dataaccess.datasource.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.impl.GwtModelerServiceImpl;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.IWizardController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MainWizardController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.PhysicalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.*;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulExpandPanel;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.gwt.tags.GwtRadioGroup;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class EmbeddedWizardFinishHandler extends AbstractXulEventHandler implements IWizardListener {

  private DatasourceModel datasourceModel = null;
  private ModelerWorkspace modelerWorkspace = null;
  private IModelerServiceAsync modelerService = null;
  private ICsvDatasourceServiceAsync csvModelService = null;
  private MainWizardController mainController;
  private IXulAsyncDatasourceService datasourceService;
//  private XulServiceCallback<Domain> editFinishedCallback;
  private FileTransformStats stats;
  private MessageHandler messageHandler;

  private XulDialog wizardDialog;

  private GwtRadioGroup modelerDecision;

  private XulExpandPanel errorLogExpander;

  private XulDialog summaryDialog;
  private XulLabel summaryDialogRowsLoaded;
  private XulVbox showModelerCheckboxHider;
  private XulLabel summaryDialogDetails;
  private XulServiceCallback<Domain> editFinishedCallback;
  private static final String MSG_OPENING_MODELER = "waiting.openingModeler";
  private static final String MSG_GENERAL_WAIT = "waiting.generalWaiting";
  private boolean showModeler;

  public EmbeddedWizardFinishHandler(XulDialog wizardDialog, DatasourceModel datasourceModel, MessageHandler messageHandler, MainWizardController mainController, IXulAsyncDatasourceService datasourceService) {
    this.wizardDialog = wizardDialog;
    this.messageHandler = messageHandler;
    this.mainController = mainController;
    this.datasourceService = datasourceService;

    this.datasourceModel = datasourceModel;
    modelerWorkspace = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
    modelerService = new GwtModelerServiceImpl();
    csvModelService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) csvModelService;
    endpoint.setServiceEntryPoint(PhysicalDatasourceController.getDatasourceURL());
  }

  public String getName(){
    return "finishHandler";
  }

  public void init(){

    modelerDecision = (GwtRadioGroup) document.getElementById("modelerDecision");
    errorLogExpander = (XulExpandPanel) document.getElementById("errorLogExpander");
    showModelerCheckboxHider = (XulVbox) document.getElementById("showModelerCheckboxHider");

    summaryDialog = (XulDialog) document.getElementById("summaryDialog");
    summaryDialogRowsLoaded = (XulLabel) document.getElementById("summaryDialogRowsLoaded");
    summaryDialogDetails = (XulLabel) document.getElementById("summaryDialogDetails");
  }
  @Bindable
  public void stageData() {
    datasourceModel.getGuiStateModel().setDataStagingComplete(false);
    setColumnIdsToColumnNames();

    // set the modelInfo.stageTableName to the database table name generated from the datasourceName
    datasourceModel.getModelInfo().setStageTableName(datasourceModel.generateTableName());
    
    csvModelService.stageData(datasourceModel.getModelInfo(), true, new DataStagingCallback());
  }

  private void setColumnIdsToColumnNames() {
    for (ColumnInfo ci : datasourceModel.getModelInfo().getColumns()) {
      ci.setId(ci.getTitle());
    }
  }

  public void setEditFinishedCallback(XulServiceCallback<Domain> editFinishedCallback) {
    this.editFinishedCallback = editFinishedCallback;
  }

  public boolean isShowModeler() {
    return showModeler;
  }

  public class DataStagingCallback implements AsyncCallback<FileTransformStats> {
    
    public void onFailure(Throwable th) {
      messageHandler.closeWaitingDialog();
      if (th instanceof CsvTransformGeneratorException) {
        messageHandler.showErrorDetailsDialog(messageHandler.messages.getString("ERROR"), th.getMessage(), ((CsvTransformGeneratorException)th).getCauseMessage() + ((CsvTransformGeneratorException)th).getCauseStackTrace());
      } else {
        messageHandler.showErrorDialog(messageHandler.messages.getString("ERROR"), th.getMessage());
      }
      th.printStackTrace();
    }
    
    public void onSuccess(FileTransformStats stats) {
      EmbeddedWizardFinishHandler.this.stats = stats;
      save();
    }
  }

  public void save() {
    saveModels(new XulServiceCallback<String>(){
      public void success( String modelId ) {
        modelerWorkspace.getDomain().setId(modelId);
        messageHandler.closeWaitingDialog();
        showSummaryDialog();
      }

      public void error( String s, Throwable throwable ) {
        messageHandler.closeWaitingDialog();

        throwable.printStackTrace();
        messageHandler.showErrorDialog("Error saving models", "An error was encountered while saving the model files. Check your " +
            "server logs for details");
      }
    });
  }
  
  public void saveModels(XulServiceCallback<String> callback){
    String name = datasourceModel.getDatasourceName().replace(".", "_").replace(" ", "_");
    modelerWorkspace.setModelName(name);
    updateDomain(callback);
  }
  
  private String dbType = null;
  public void updateDomain(final XulServiceCallback<String> callback) {
    String tableName = null;
    String query = null;
    String connectionName = null;
    switch (datasourceModel.getDatasourceType()) {
      case CSV:
        tableName = datasourceModel.generateTableName();
        query = null;
        connectionName = null;
        generateDomain(connectionName, tableName, query, callback);
        break;
      case SQL:

        query = datasourceModel.getQuery();
        connectionName = datasourceModel.getSelectedRelationalConnection().getName();
        tableName = null;
        
        final String fConnectionName = connectionName;
        final String fTablename = tableName;
        final String fQuery = query;

        datasourceModel.getGuiStateModel().getConnectionService().convertFromConnection(datasourceModel.getSelectedRelationalConnection(), new XulServiceCallback<IDatabaseConnection>() {
          public void error(String message, Throwable error) {
            //TODO: handle exception
          }
          public void success(IDatabaseConnection conn) {
            dbType = conn.getDatabaseType().getShortName();
            generateDomain(fConnectionName, fTablename, fQuery, callback);
          }
        });

        break;
    }
  }

  private void generateDomain(String connectionName, String tableName, String query, final XulServiceCallback<String> callback){
    modelerService.generateDomain(connectionName, tableName, dbType, query, datasourceModel.getDatasourceName(), new XulServiceCallback<Domain>(){
      public void success( final Domain domain) {
        
        String tmpFileName = datasourceModel.getModelInfo().getFileInfo().getTmpFilename();
        String fileName = datasourceModel.getModelInfo().getFileInfo().getFileName();
        if(fileName == null && tmpFileName != null && tmpFileName.endsWith(".tmp")) {
          tmpFileName = tmpFileName.substring(0, tmpFileName.lastIndexOf(".tmp"));
          datasourceModel.getModelInfo().getFileInfo().setFileName(tmpFileName);
        }  
        
        datasourceService.serializeModelState(DatasourceDTO.generateDTO(datasourceModel), new XulServiceCallback<String>(){
          public void success(String retVal) {
            domain.getLogicalModels().get(0).setProperty("datasourceModel", retVal);
            modelerWorkspace.setDomain(domain);
            try {
              modelerWorkspace.getWorkspaceHelper().autoModelFlat(modelerWorkspace);
              modelerWorkspace.setModelName(datasourceModel.getDatasourceName());
              modelerWorkspace.getWorkspaceHelper().populateDomain(modelerWorkspace);
              Domain workspaceDomain = modelerWorkspace.getDomain();

              //
              // This is a temporary property until we serialize staging related information in the model.
              //

              if (datasourceModel.getDatasourceType() == DatasourceType.CSV) {
                workspaceDomain.getLogicalModels().get(0).setProperty("DatasourceType", "CSV");
              }

              modelerService.serializeModels(workspaceDomain, modelerWorkspace.getModelName(), callback);
            } catch (ModelerException e) {
              messageHandler.closeWaitingDialog();
              messageHandler.showErrorDialog("ModelerException", e.getMessage());
              e.printStackTrace();
            }
          }

          public void error(String message, Throwable error) {
            messageHandler.closeWaitingDialog();
            messageHandler.showErrorDialog(message, error.getMessage());
            error.printStackTrace();
          }
        });

      }

      public void error(String s, Throwable throwable ) {
        messageHandler.closeWaitingDialog();
        messageHandler.showErrorDialog(s, throwable.getMessage());
        throwable.printStackTrace();
      }
    });
  }
  
  public Domain getDomain() {
    return modelerWorkspace.getDomain();
  }

  public FileTransformStats getFileTransformStats() {
    return stats;
  }

  @Override
  public void onCancel() {

  }

  @Override
  public void onFinish() {
    wizardDialog.hide();
    messageHandler.showWaitingDialog();
    if (datasourceModel.getGuiStateModel().isRelationalValidated()) {
      save();
    } else if (datasourceModel.getModelInfo().isValidated()) {
      stageData();
    }
  }

  public void showSummaryDialog() {
    FileTransformStats stats = getFileTransformStats();
    showSummaryDialog(stats);
  }


  public void showSummaryDialog(FileTransformStats stats){

    wizardDialog.hide();

    errorLogExpander.setExpanded(false);
    modelerDecision.setValue("DEFAULT");

    // only show csv related stuff if it is a csv data source (it will have stats)
    if (stats != null && datasourceModel.getDatasourceType() == DatasourceType.CSV) {
      long errors = stats.getCsvInputErrorCount() + stats.getTableOutputErrorCount();
      long total = stats.getRowsDone() > 0 ? stats.getRowsDone() : errors;

      long successRows = total > errors ? total - errors : 0;

      summaryDialogRowsLoaded.setValue(messageHandler.messages.getString("summaryDialog.rowsLoaded", String.valueOf(successRows), String.valueOf(total)));
      String lf = "\n";
      if (errors > 0) {
        StringBuilder detailMsg = new StringBuilder();
        for (String error : stats.getCsvInputErrors()) {
          detailMsg.append(error);
          detailMsg.append(lf);
        }

        for (String error : stats.getTableOutputErrors()) {
          detailMsg.append(error);
          detailMsg.append(lf);
        }
        summaryDialogDetails.setValue(detailMsg.toString());
        errorLogExpander.setVisible(true);
      } else {
        summaryDialogDetails.setValue("");
        errorLogExpander.setVisible(false);
      }

    } else {
      summaryDialogRowsLoaded.setValue(messageHandler.messages.getString("summaryDialog.generalSuccess"));
      errorLogExpander.setVisible(false);
    }

    showModelerCheckboxHider.setVisible(!datasourceModel.getGuiStateModel().isEditing());

    summaryDialog.show();
  }

  @Bindable
  public void closeSummaryDialog() {
    summaryDialog.hide();
    boolean editModeler = modelerDecision.getValue() != null && modelerDecision.getValue().equals("EDIT");
    if (editModeler) {
      messageHandler.showWaitingDialog(messageHandler.messages.getString(MSG_OPENING_MODELER));
      showModeler = true;
    } else {
      messageHandler.showWaitingDialog(messageHandler.messages.getString(MSG_GENERAL_WAIT));
      showModeler = false;
    }

    errorLogExpander.setExpanded(false);

    if (editFinishedCallback != null) {
      editFinishedCallback.success(getDomain());
    }
    datasourceModel.clearModel();
  }
}
