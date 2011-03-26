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

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.gwt.GwtModelerMessages;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.modeler.ModelerDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.*;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.CsvDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.dummy.DummyDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.RelationalPhysicalStep;
import org.pentaho.platform.dataaccess.datasource.wizard.steps.StageDataStep;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.core.client.GWT;


@SuppressWarnings("unchecked")
public class EmbeddedWizard extends AbstractXulDialogController<Domain> implements IXulLoaderCallback, IResourceBundleLoadCallback, IWizardListener, XulServiceCallback<Domain>{
  protected static final String MAIN_WIZARD_PANEL = "main_wizard_panel.xul"; //$NON-NLS-1$

  protected static final String MAIN_WIZARD_PANEL_PACKAGE = "main_wizard_panel"; //$NON-NLS-1$

  protected static final String WIZARD_DIALOG_ID = "main_wizard_window"; //$NON-NLS-1$

  private XulDomContainer mainWizardContainer;

  private XulDialog dialog;

  private MainWizardController wizardController;

  private DatasourceModel datasourceModel = new DatasourceModel();
  
  private WizardDatasourceController datasourceController;

  //TODO: need to move this to the relational data source
  private ConnectionController connectionController;

  private IXulAsyncConnectionService connectionService;

  private IXulAsyncDatasourceService datasourceService;

  private DatasourceMessages datasourceMessages;

  private boolean initialized;

  private RelationalPhysicalStep physicalStep = null;

  private StageDataStep stageDataStep;
  
  private AsyncConstructorListener asyncConstructorListener;
  

  
  private Boolean editing;
  
  private ResourceBundle bundle;

  private XulServiceCallback<Domain> editFinishedCallback;
  private IDatasourceSummary summary;
  private ModelerDialog modeler;
  private SummaryDialogController summaryDialogController = new SummaryDialogController();

  /**
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
          MessageHandler.getInstance().showErrorDialog(datasourceMessages.getString("DatasourceEditor.ERROR"), //$NON-NLS-1$
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


  public void onCancel(){
    dialog.hide();
    datasourceModel.clearModel();
    wizardController.setActiveStep(0);
  }

  @Override
  public void onFinish(final IDatasourceSummary summary) {
    this.summary = summary;
    summaryDialogController.showSummaryDialog(summary, new XulServiceCallback<IDatasourceSummary>(){
      @Override
      public void error(String s, Throwable throwable) {
        MessageHandler.getInstance().showErrorDialog(s, throwable.getMessage());
      }

      @Override
      public void success(IDatasourceSummary iDatasourceSummary) {
        if(iDatasourceSummary.isShowModeler()){
          showModelEditor();
        } else if(editFinishedCallback != null){
          editFinishedCallback.success(summary.getDomain());
          MessageHandler.getInstance().closeWaitingDialog();
        }
      }
    });
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
    this.editFinishedCallback = null; // if previously have edited, clear-out old listener
    editing = false;
    datasourceModel.getGuiStateModel().setEditing(false);
    wizardController.setActiveStep(0);
    
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

  public void showEditDialog(final Domain domain, final XulServiceCallback<Domain> editFinishedCallback) {
    this.editFinishedCallback = editFinishedCallback;

    // initialize connections
    if (datasourceModel.getGuiStateModel().getConnections() == null
        || datasourceModel.getGuiStateModel().getConnections().size() <= 0) {
      checkInitialized();
      connectionController.reloadConnections();
    }
    editing = true;
    datasourceModel.getGuiStateModel().setEditing(true);
    wizardController.reset();

    String modelState = (String) domain.getLogicalModels().get(0).getProperty("datasourceModel");
    datasourceService.deSerializeModelState(modelState, new XulServiceCallback<DatasourceDTO>() {
      public void success(DatasourceDTO datasourceDTO) {
        DatasourceDTO.populateModel(datasourceDTO, datasourceModel);
        dialog.show();
        datasourceModel.getGuiStateModel().setDirty(false);
        physicalStep.setFocus();        
      }

      public void error(String s, Throwable throwable) {
        MessageHandler.getInstance().showErrorDialog(datasourceMessages.getString("ERROR"), datasourceMessages.getString(
            "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", throwable.getLocalizedMessage()));
      }
    });

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
    if(connectionController != null){
      connectionController.setService(service);
      connectionController.reloadConnections();
    }
  }

  public IXulAsyncConnectionService getConnectionService() {
    return connectionService;
  }

  public IXulAsyncDatasourceService getDatasourceService() {
    return datasourceService;
  }

  public MainWizardController getWizardController() {
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

    Document rootDocument = mainWizardContainer.getDocumentRoot();
    BindingFactory bf = new GwtBindingFactory(rootDocument);

    ResourceBundle resBundle = (ResourceBundle) mainWizardContainer.getResourceBundles().get(0);

    datasourceMessages = new GwtDatasourceMessages();
    datasourceMessages.setMessageBundle(resBundle);

    MessageHandler.getInstance().setMessages(datasourceMessages);
    connectionController = new ConnectionController();
    connectionController.setService(connectionService);

    summaryDialogController.setBindingFactory(bf);
    mainWizardContainer.addEventHandler(summaryDialogController);

    datasourceModel.addDatasource(new DummyDatasource());
    datasourceModel.addDatasource(new CsvDatasource(datasourceModel));

    wizardController = new MainWizardController(bf, datasourceModel, datasourceService);
    mainWizardContainer.addEventHandler(wizardController);

    dialog = (XulDialog) rootDocument.getElementById(WIZARD_DIALOG_ID);
    MessageHandler.getInstance().setWizardDialog(dialog);

    datasourceController = new WizardDatasourceController();
    datasourceController.setBindingFactory(bf);
    datasourceController.setDatasourceMessages(datasourceMessages);
    mainWizardContainer.addEventHandler(datasourceController);

    mainWizardContainer.addEventHandler(MessageHandler.getInstance());

    // add the steps ..
//    physicalStep = new RelationalPhysicalStep(datasourceService, connectionService, datasourceMessages, this);


    wizardController.addWizardListener(this);


    // Controller for the File Import functionality
    FileImportController fileImportController = new FileImportController(datasourceModel, datasourceMessages);
    mainWizardContainer.addEventHandler(fileImportController);

    // init other controllers
    fileImportController.init();
    MessageHandler.getInstance().init();
    summaryDialogController.init();


    // Create the gui
    try {
      //new WizardContentPanel(wizardController).addContent(mainWizardContainer);
      wizardController.init();
      initialized = true;
    } catch (Exception throwable) {
      throwable.printStackTrace();
    }

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
    return summary.getDomain();
  }

  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  public void setDatasourceMessages(DatasourceMessages datasourceMessages) {
    this.datasourceMessages = datasourceMessages;
  }

  public void setWizardController(MainWizardController wizardController) {
    this.wizardController = wizardController;
  }
  
  public Boolean getEditing() {
    return editing;
  }

  public void setEditing(Boolean editing) {
    this.editing = editing;
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
    final Domain domain = summary.getDomain();
  
    modeler = ModelerDialog.getInstance(this, new AsyncConstructorListener<ModelerDialog>(){
      public void asyncConstructorDone(ModelerDialog dialog) {
        dialog.addDialogListener(listener);
        MessageHandler.getInstance().closeWaitingDialog();
        dialog.showDialog(domain);
      }
    });

  }
  @Override
  public void error(String s, Throwable throwable) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void success(Domain domain) {
    if(editFinishedCallback != null){
      editFinishedCallback.success(domain);
    }
    MessageHandler.getInstance().closeWaitingDialog();
    if(summary.isShowModeler()){
      showModelEditor();
    } else {
      onDialogAccept();
    }

  }

}
