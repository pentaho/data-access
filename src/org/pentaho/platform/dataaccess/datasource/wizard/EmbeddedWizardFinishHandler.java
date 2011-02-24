package org.pentaho.platform.dataaccess.datasource.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.google.gwt.user.client.Window;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.impl.GwtModelerServiceImpl;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.PhysicalDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTOUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileTransformStats;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class EmbeddedWizardFinishHandler implements PropertyChangeListener {

  private EmbeddedWizard wizard = null;
  private DatasourceMessages messages = null;
  private DatasourceModel datasourceModel = null;
  private ModelerWorkspace modelerWorkspace = null;
  private IModelerServiceAsync modelerService = null;
  private ICsvDatasourceServiceAsync csvModelService = null; 
  private IXulAsyncDatasourceService datasourceService;
//  private XulServiceCallback<Domain> editFinishedCallback;
  private FileTransformStats stats;

  public EmbeddedWizardFinishHandler(EmbeddedWizard wizard, IXulAsyncDatasourceService datasourceService) {
    this.wizard = wizard;
    this.datasourceService = datasourceService;

    messages = wizard.getDatasourceMessages();
    datasourceModel = wizard.getDatasourceModel();
    modelerWorkspace = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
    modelerService = new GwtModelerServiceImpl();    
    csvModelService = (ICsvDatasourceServiceAsync) GWT.create(ICsvDatasourceService.class);
    ServiceDefTarget endpoint = (ServiceDefTarget) csvModelService;
    endpoint.setServiceEntryPoint(PhysicalDatasourceController.getDatasourceURL());
  }

  public void propertyChange(final PropertyChangeEvent evt) {
    if (wizard.getWizardController().isFinished()) {
      wizard.hideDialog();
      wizard.showWaitingDialog();
      if (datasourceModel.getGuiStateModel().isRelationalValidated()) {
        save();
      } else if (datasourceModel.getModelInfo().isValidated()) {
        stageData();
      }      
    }
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

  public class DataStagingCallback implements AsyncCallback<FileTransformStats> {
    
    public void onFailure(Throwable th) {
      wizard.getWizardController().setFinished(false);
      wizard.closeWaitingDialog();
      if (th instanceof CsvTransformGeneratorException) {
        wizard.showErrorDetailsDialog(messages.getString("ERROR"), ((CsvTransformGeneratorException)th).getMessage(), ((CsvTransformGeneratorException)th).getCauseMessage() + ((CsvTransformGeneratorException)th).getCauseStackTrace());
      } else {
        wizard.showErrorDialog(messages.getString("ERROR"), th.getMessage());
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
        wizard.closeWaitingDialog();
        wizard.showSummaryDialog();
      }

      public void error( String s, Throwable throwable ) {
        wizard.getWizardController().setFinished(false);
        wizard.closeWaitingDialog();

        throwable.printStackTrace();
        wizard.showErrorDialog("Error saving models", "An error was encountered while saving the model files. Check your " +
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
        
        datasourceService.serializeModelState(DatasourceDTOUtil.generateDTO(datasourceModel), new XulServiceCallback<String>(){
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
              wizard.closeWaitingDialog();
              wizard.showErrorDialog("ModelerException", e.getMessage());
              wizard.getWizardController().setFinished(false);
              e.printStackTrace();
            }
          }

          public void error(String message, Throwable error) {
            wizard.closeWaitingDialog();
            wizard.showErrorDialog(message, error.getMessage());
            wizard.getWizardController().setFinished(false);
            error.printStackTrace();
          }
        });

      }

      public void error(String s, Throwable throwable ) {
        wizard.closeWaitingDialog();
        wizard.showErrorDialog(s, throwable.getMessage());
        wizard.getWizardController().setFinished(false);
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

}
