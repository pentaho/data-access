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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.gwt.GwtModelerMessages;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.modeler.ModelerDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.FileImportController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.IWizardController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.LinearWizardController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileTransformStats;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.steps.PhysicalStep;
import org.pentaho.platform.dataaccess.datasource.wizard.steps.StageDataStep;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulExpandPanel;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.tags.GwtRadioGroup;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.core.client.GWT;


@SuppressWarnings("unchecked")
public class EmbeddedWizard extends AbstractXulDialogController<Domain> implements IXulLoaderCallback, IResourceBundleLoadCallback{
  protected static final String MAIN_WIZARD_PANEL = "main_wizard_panel.xul"; //$NON-NLS-1$

  protected static final String MAIN_WIZARD_PANEL_PACKAGE = "main_wizard_panel"; //$NON-NLS-1$

  protected static final String WIZARD_DIALOG_ID = "main_wizard_window"; //$NON-NLS-1$

  protected static final String MSG_STAGING_DATA = "physicalDatasourceDialog.STAGING_DATA"; //$NON-NLS-1$

  protected static final String MSG_CREATING_DATA_SOURCE = "waiting.creatingDataSource"; //$NON-NLS-1$
  
  private XulDomContainer mainWizardContainer;

  private XulDialog dialog;

  private XulDialog successDialog = null;
  
  private XulLabel successLabel = null;

  private XulDialog waitingDialog = null;
  
  private XulLabel waitingLabel = null;

  private LinearWizardController wizardController;

  private DatasourceModel datasourceModel = new DatasourceModel();
  
  private WizardDatasourceController datasourceController;

  private ConnectionController connectionController = new ConnectionController();

  private IXulAsyncConnectionService connectionService;

  private IXulAsyncDatasourceService datasourceService;

  private DatasourceMessages datasourceMessages;

  private boolean initialized;

  private PhysicalStep physicalStep = null;

  private StageDataStep stageDataStep;
  
  private AsyncConstructorListener asyncConstructorListener;
  
  private EmbeddedWizardFinishHandler embeddedWizardFinishHandler;
  
  private Boolean editing;
  
  private ResourceBundle bundle;

  private XulDialog errorDialog;
  private XulDialog errorDetailsDialog;
  private XulDialog summaryDialog;
  private XulLabel summaryDialogRowsLoaded;
  private XulVbox showModelerCheckboxHider;
  private XulLabel summaryDialogDetails;
  private XulVbox csvSummaryContainer;
  private XulServiceCallback<Domain> editFinishedCallback;

  private ModelerDialog modeler;
  private GwtRadioGroup modelerDecision;
  private static final String MSG_OPENING_MODELER = "waiting.openingModeler";
  private static final String MSG_GENERAL_WAIT = "waiting.generalWaiting";
  private XulExpandPanel errorLogExpander;


  /**
   * @param datasourceService
   * @param connectionService
   * @param constructorListener
   * @param checkHasAccess
   */
  public EmbeddedWizard(final IXulAsyncDatasourceService datasourceService,
      final IXulAsyncConnectionService connectionService, final AsyncConstructorListener<EmbeddedWizard> constructorListener,
      boolean checkHasAccess) {
    
    asyncConstructorListener = constructorListener;
    
    if (checkHasAccess) {
      datasourceService.hasPermission(new XulServiceCallback<Boolean>() {
        public void error(String message, Throwable error) {
          showErrorDialog(datasourceMessages.getString("DatasourceEditor.ERROR"), //$NON-NLS-1$
              datasourceMessages.getString(
                  "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", error.getLocalizedMessage())); //$NON-NLS-1$
        }

        public void success(Boolean retVal) {
          if (retVal) {
            init(datasourceService, connectionService);
          } else {
            if (constructorListener != null) {
              constructorListener.asyncConstructorDone(EmbeddedWizard.this);
            }
            onDialogReady();
          }
        }
      });
    } else {
      init(datasourceService, connectionService);
    }
  }

  public void bundleLoaded(String bundleName) {
    try{
      ModelerMessagesHolder.setMessages(new GwtModelerMessages(bundle));
    } catch(Exception ignored){
      // Messages may have been set earlier, ignore.
    }
  }


  private void init(final IXulAsyncDatasourceService datasourceService,
      final IXulAsyncConnectionService connectionService) {
    setConnectionService(connectionService);
    setDatasourceService(datasourceService);
    
    bundle = new ResourceBundle("", "modeler", true, this);

    AsyncXulLoader.loadXulFromUrl(MAIN_WIZARD_PANEL, MAIN_WIZARD_PANEL_PACKAGE, EmbeddedWizard.this);
  }

  private class CancelHandler implements PropertyChangeListener {
    public void propertyChange(final PropertyChangeEvent evt) {
      if (wizardController.isCancelled()) {
        dialog.hide();
        datasourceModel.clearModel();
        wizardController.setCancelled(false);
        wizardController.setActiveStep(0);
      }
    }
  }
  
  private void checkInitialized() {
    if (!initialized) {
      throw new IllegalStateException(datasourceMessages
          .getString("DatasourceEditor.ERROR_0003_CONSTRUCTOR_NOT_INITIALIZED_ERROR")); //$NON-NLS-1$
    }
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener(org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener) {
    checkInitialized();
    super.addDialogListener(listener);
    listener.onDialogReady();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {

    if (datasourceModel.getGuiStateModel().getConnections() == null
        || datasourceModel.getGuiStateModel().getConnections().size() <= 0) {
      checkInitialized();
    }
    setEditFinishedCallback(null);
    editing = false;
    datasourceModel.getGuiStateModel().setEditing(false);
    
    /* BISERVER-5153: Work around where XulGwtButton is getting its disabled state and style
     * confused.  The only way to get the train on the track is to flip-flop it.
     */
    XulButton nextButton = (XulButton)mainWizardContainer.getDocumentRoot().getElementById("next_btn"); //$NON-NLS-1$
    nextButton.setDisabled(false);
    nextButton.setDisabled(true);
    /* end of work around */
    
    dialog.show();
    physicalStep.setFocus();
  }

  public void showEditDialog(final Domain domain, XulServiceCallback<Domain> editFinishedCallback) {

    // initialize connections
    if (datasourceModel.getGuiStateModel().getConnections() == null
        || datasourceModel.getGuiStateModel().getConnections().size() <= 0) {
      checkInitialized();
      reloadConnections();
    }
    editing = true;
    datasourceModel.getGuiStateModel().setEditing(true);

    setEditFinishedCallback(editFinishedCallback);

    String modelState = (String) domain.getLogicalModels().get(0).getProperty("datasourceModel");
    datasourceService.deSerializeModelState(modelState, new XulServiceCallback<DatasourceDTO>() {
      public void success(DatasourceDTO datasourceDTO) {
        DatasourceDTO.populateModel(datasourceDTO, datasourceModel);
        dialog.show();
        datasourceModel.getGuiStateModel().setDirty(false);
        physicalStep.setFocus();        
      }

      public void error(String s, Throwable throwable) {
        showErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString(
              "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", throwable.getLocalizedMessage()));
      }
    });

  }

  private void reloadConnections() {
    if (connectionService != null) {
      connectionService.getConnections(new XulServiceCallback<List<IConnection>>() {

        public void error(String message, Throwable error) {
          showErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString(
              "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", error.getLocalizedMessage()));
        }

        public void success(List<IConnection> connections) {
          datasourceModel.getGuiStateModel().setConnections(connections);
        }

      });
    } else {
      showErrorDialog(datasourceMessages.getString("ERROR"),
          "DatasourceEditor.ERROR_0004_CONNECTION_SERVICE_NULL");
    }

  }

  protected void showErrorDialog(String title, String message) {
    XulDialog errorDialog = (XulDialog) mainWizardContainer.getDocumentRoot().getElementById("errorDialog");
    errorDialog.setTitle(title);
    
    XulLabel errorLabel = (XulLabel) mainWizardContainer.getDocumentRoot().getElementById("errorLabel");
    errorLabel.setValue(message);

    errorDialog.show();
  }
  
  protected void showErrorDetailsDialog(String title, String message, String detailMessage) {
    XulDialog errorDialog = (XulDialog) mainWizardContainer.getDocumentRoot().getElementById("errorDetailsDialog");
    errorDialog.setTitle(title);
	    
    XulLabel errorLabel = (XulLabel) mainWizardContainer.getDocumentRoot().getElementById("errorDetailsLabel");
    errorLabel.setValue(message);

    XulLabel detailMessageBox = (XulLabel) mainWizardContainer.getDocumentRoot().getElementById("error_dialog_details");
    detailMessageBox.setValue(detailMessage);
	    
    errorDialog.show();
  }  

  @Bindable
  public void closeErrorDetailsDialog() {
    if (!errorDetailsDialog.isHidden()) {
      errorDetailsDialog.hide();
    }
    dialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    if (!errorDialog.isHidden()) {
      errorDialog.hide();
    }
    dialog.show();
  }

  @Bindable
  public void closeSuccessDetailsDialog() {
    XulDialog detailedSuccessDialog = (XulDialog) mainWizardContainer.getDocumentRoot().getElementById("successDetailsDialog");
    detailedSuccessDialog.hide();
  }
  
  @Bindable
  public void showDetailedSuccessDialog(String message, String detailMessage) {
    XulDialog detailedSuccessDialog = (XulDialog) mainWizardContainer.getDocumentRoot().getElementById("successDetailsDialog");

    XulLabel successLabel = (XulLabel) mainWizardContainer.getDocumentRoot().getElementById("success_details_label");
    successLabel.setValue(message);
    
    XulTextbox detailMessageBox = (XulTextbox) mainWizardContainer.getDocumentRoot().getElementById("success_dialog_details");
    detailMessageBox.setValue(detailMessage);

    detailedSuccessDialog.show();
  }  

  @Bindable
  public void closeSuccessDialog() {
    successDialog.hide();
  }

  public void showSuccessDialog(String message) {
    successLabel.setValue(message);
    successDialog.show();
  }
  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.gwt.util.IXulLoaderCallback#overlayLoaded()
   */
  public void overlayLoaded() {

  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.gwt.util.IXulLoaderCallback#overlayRemoved()
   */
  public void overlayRemoved() {
    // TODO Auto-generated method stub

  }

  public XulDomContainer getMainWizardContainer() {
    return mainWizardContainer;
  }

  private void setConnectionService(IXulAsyncConnectionService service) {
    this.connectionService = service;
    connectionController.setService(service);
    reloadConnections();
  }

  public IXulAsyncConnectionService getConnectionService() {
    return connectionService;
  }

  public IXulAsyncDatasourceService getDatasourceService() {
    return datasourceService;
  }

  public LinearWizardController getWizardController() {
    return wizardController;
  }

  public void setDatasourceService(IXulAsyncDatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.gwt.util.IXulLoaderCallback#xulLoaded(org.pentaho.ui.xul.gwt.GwtXulRunner)
   */
  public void xulLoaded(GwtXulRunner runner) {
    
    mainWizardContainer = runner.getXulDomContainers().get(0);
    mainWizardContainer.addEventHandler(this);

    Document rootDocument = mainWizardContainer.getDocumentRoot();
    BindingFactory bf = new GwtBindingFactory(rootDocument);
    wizardController = new LinearWizardController(bf, datasourceModel);

    ResourceBundle resBundle = (ResourceBundle) mainWizardContainer.getResourceBundles().get(0);
    
    summaryDialog = (XulDialog) mainWizardContainer.getDocumentRoot().getElementById("summaryDialog");
    summaryDialogRowsLoaded = (XulLabel) mainWizardContainer.getDocumentRoot().getElementById("summaryDialogRowsLoaded");
    summaryDialogDetails = (XulLabel) mainWizardContainer.getDocumentRoot().getElementById("summaryDialogDetails");
    csvSummaryContainer = (XulVbox) mainWizardContainer.getDocumentRoot().getElementById("csvSummaryContainer");
    modelerDecision = (GwtRadioGroup) mainWizardContainer.getDocumentRoot().getElementById("modelerDecision");
    showModelerCheckboxHider = (XulVbox) mainWizardContainer.getDocumentRoot().getElementById("showModelerCheckboxHider");
    errorLogExpander = (XulExpandPanel) mainWizardContainer.getDocumentRoot().getElementById("errorLogExpander");

    errorDialog = (XulDialog) document.getElementById("errorDialog"); //$NON-NLS-1$
    errorDetailsDialog = (XulDialog) document.getElementById("errorDetailsDialog"); //$NON-NLS-1$


    datasourceMessages = new GwtDatasourceMessages();
    datasourceMessages.setMessageBundle(resBundle);
    datasourceController = new WizardDatasourceController();
    datasourceController.setBindingFactory(bf);
    datasourceController.setDatasourceMessages(datasourceMessages);
    mainWizardContainer.addEventHandler(datasourceController);

    // set the defaults
    datasourceModel.getModelInfo().getFileInfo().setDelimiter(",");
    datasourceModel.getModelInfo().getFileInfo().setEnclosure("\"");
    datasourceModel.getModelInfo().getFileInfo().setHeaderRows(1);    

    // add the steps ..
    physicalStep = new PhysicalStep(datasourceService, connectionService, datasourceMessages, this);
    physicalStep.setModel(datasourceModel);
    stageDataStep = new StageDataStep(bf, datasourceMessages);
    stageDataStep.setModel(datasourceModel);

    wizardController.addStep(physicalStep);
    wizardController.addStep(stageDataStep);
    
    embeddedWizardFinishHandler = new EmbeddedWizardFinishHandler(this, datasourceService);

    wizardController.addPropertyChangeListener(IWizardController.CANCELLED_PROPERTY_NAME, new CancelHandler());
    wizardController.addPropertyChangeListener(IWizardController.FINISHED_PROPERTY_NAME, embeddedWizardFinishHandler);

    // Create the gui
    try {
      new WizardContentPanel(wizardController).addContent(mainWizardContainer);
      wizardController.registerMainXULContainer(mainWizardContainer);
      wizardController.onLoad();
      final XulComponent root = rootDocument.getElementById(WIZARD_DIALOG_ID);

      if (!(root instanceof XulDialog)) {
        throw new XulException("" /*Messages.getInstance().getString("EMBEDDED_WIZARD.Root_Error") + " " + root */); //$NON-NLS-1$ //$NON-NLS-2$
      }

      dialog = (XulDialog) root;
      successDialog = (XulDialog) rootDocument.getElementById("successDialog"); //$NON-NLS-1$
      successLabel = (XulLabel) rootDocument.getElementById("successLabel"); //$NON-NLS-1$
      waitingDialog = (XulDialog) rootDocument.getElementById("waitingDialog"); //$NON-NLS-1$
      waitingLabel = (XulLabel) rootDocument.getElementById("waitingDialogLabel"); //$NON-NLS-1$

      initialized = true;
    } catch (Exception throwable) {
      throwable.printStackTrace();
    }

    // Controller for the File Import functionality
    FileImportController fileImportController = new FileImportController(datasourceModel, datasourceMessages);
    mainWizardContainer.addEventHandler(fileImportController);
    fileImportController.init();
    
    // Remap the upload action in development mode
    if(GWT.isScript() == false){
      XulFileUpload upload = (XulFileUpload) rootDocument.getElementById("fileUpload"); //$NON-NLS-1$
      upload.setAction(GWT.getModuleBaseURL()+ "UploadService"); //$NON-NLS-1$
    }

    if (asyncConstructorListener != null) {
      asyncConstructorListener.asyncConstructorDone(this);
    }

  }

  public DatasourceModel getDatasourceModel() {
    return datasourceModel;
  }

  public void setDatasourceModel(DatasourceModel datasourceModel) {
    this.datasourceModel = datasourceModel;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.util.AbstractXulDialogController#getDialog()
   */
  @Override
  protected XulDialog getDialog() {
    return dialog;
  }

  /* (non-Javadoc)
   * @see org.pentaho.ui.xul.util.AbstractXulDialogController#getDialogResult()
   */
  @Override
  protected Domain getDialogResult() {
    return embeddedWizardFinishHandler.getDomain();
  }

  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  public void setWizardController(LinearWizardController wizardController) {
    this.wizardController = wizardController;
  }
  
  public Boolean getEditing() {
    return editing;
  }

  public void setEditing(Boolean editing) {
    this.editing = editing;
  }

  public void showWaitingDialog() {
    waitingLabel.setValue(datasourceMessages.getString(MSG_CREATING_DATA_SOURCE));
    waitingDialog.show();
  }
  public void showWaitingDialog(String msg) {
    waitingLabel.setValue(msg);
    waitingDialog.show();
  }
  public void closeWaitingDialog() {
    waitingDialog.hide();
  }

  public void setEditFinishedCallback(XulServiceCallback<Domain> editFinishedCallback) {
    this.editFinishedCallback = editFinishedCallback;
  }

  public void showSummaryDialog() {
    dialog.hide();

    errorLogExpander.setExpanded(false);
    modelerDecision.setValue("DEFAULT");

    // only show csv related stuff if it is a csv data source (it will have stats)
    FileTransformStats stats = embeddedWizardFinishHandler.getFileTransformStats();
    if (stats != null && datasourceModel.getDatasourceType() == DatasourceType.CSV) {
      long errors = stats.getCsvInputErrorCount() + stats.getTableOutputErrorCount();
      long total = stats.getRowsDone() > 0 ? stats.getRowsDone() : errors;

      long successRows = total > errors ? total - errors : 0;

      summaryDialogRowsLoaded.setValue(datasourceMessages.getString("summaryDialog.rowsLoaded", String.valueOf(successRows), String.valueOf(total)));
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
      summaryDialogRowsLoaded.setValue(datasourceMessages.getString("summaryDialog.generalSuccess"));
      errorLogExpander.setVisible(false);
    }

    showModelerCheckboxHider.setVisible(!datasourceModel.getGuiStateModel().isEditing());

    summaryDialog.show();
  }
  @Bindable
  public void closeSummaryDialog() {
    boolean editModeler = modelerDecision.getValue() != null && modelerDecision.getValue().equals("EDIT");
    if (editModeler) {
      showWaitingDialog(datasourceMessages.getString(MSG_OPENING_MODELER));
    } else {
      showWaitingDialog(datasourceMessages.getString(MSG_GENERAL_WAIT));
    }

    summaryDialog.hide();
    errorLogExpander.setExpanded(false);
    
    getWizardController().setFinished(false);
    getWizardController().setActiveStep(0);

    if (editFinishedCallback != null) {
      editFinishedCallback.success(embeddedWizardFinishHandler.getDomain());
    }
    datasourceModel.clearModel();
    physicalStep.setFinishable(false);

    if (editModeler) {
      showModelEditor();
    } else {
      onDialogAccept();
      closeWaitingDialog();
    }
  }

  public String getName() {
    return "datasourceWizardController";
  }

  @Bindable
  public void editFieldSettings() {
    dialog.show();
    getWizardController().setFinished(false);
    summaryDialog.hide();
  }

  @Bindable
  public void showModelEditor() {
    // open up the modeler
    final DialogListener<Domain> listener = new DialogListener<Domain>(){
      public void onDialogCancel() {
      }
      public void onDialogAccept(final Domain domain) {
        EmbeddedWizard.this.onDialogAccept();
      }
      public void onDialogReady() {
      }
    };
    final Domain domain = embeddedWizardFinishHandler.getDomain();
  
    modeler = ModelerDialog.getInstance(new AsyncConstructorListener<ModelerDialog>(){
        public void asyncConstructorDone(ModelerDialog dialog) {
          dialog.addDialogListener(listener);
          closeWaitingDialog();                    
          dialog.showDialog(domain);
        }
      });

  }

}
