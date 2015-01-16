/*!
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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.gwt.GwtModelerMessages;
import org.pentaho.gwt.widgets.client.utils.i18n.IResourceBundleLoadCallback;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.modeler.ModelerDialog;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.ConnectionController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.FileImportController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MainWizardController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.SummaryDialogController;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.WizardDatasourceController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.WizardModel;
//import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.CsvDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable.MultiTableDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasource;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulTextbox;
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
import com.google.gwt.user.client.Window;

@SuppressWarnings( "unchecked" )
public class EmbeddedWizard extends AbstractXulDialogController<Domain> implements IXulLoaderCallback,
  IResourceBundleLoadCallback, IWizardListener, XulServiceCallback<Domain> {
  protected static final String MAIN_WIZARD_PANEL = "main_wizard_panel.xul"; //$NON-NLS-1$

  protected static final String MAIN_WIZARD_PANEL_PACKAGE = "main_wizard_panel"; //$NON-NLS-1$

  protected static final String WIZARD_DIALOG_ID = "main_wizard_window"; //$NON-NLS-1$

  private XulDomContainer mainWizardContainer;

  private XulDialog dialog;

  private MainWizardController wizardController;

  private DatasourceModel datasourceModel = new DatasourceModel();

  private WizardDatasourceController datasourceController;

  // TODO: need to move this to the relational data source
  private ConnectionController connectionController;

  // private IXulAsyncConnectionService connectionService;
  private boolean checkHasAccess;

  private IXulAsyncDSWDatasourceService datasourceService;

  private DatasourceMessages datasourceMessages;

  private boolean initialized;

  private AsyncConstructorListener asyncConstructorListener;

  private ResourceBundle bundle;

  private IDatasourceSummary summary;
  private SummaryDialogController summaryDialogController = new SummaryDialogController();
  private IWizardModel wizardModel = new WizardModel();

  private ICsvDatasourceServiceAsync csvDatasourceService;
  private DialogListener<Domain> modelerDialogListener;
  private boolean reportingOnlyValid = true;

  /**
   * /**
   * 
   * @param checkHasAccess
   */
  public EmbeddedWizard( boolean checkHasAccess ) {
    this.checkHasAccess = checkHasAccess;

  }

  public void bundleLoaded( String bundleName ) {
    try {
      ModelerMessagesHolder.setMessages( new GwtModelerMessages( bundle ) );
    } catch ( Exception ignored ) {
      // Messages may have been set earlier, ignore.
    }
  }

  public void init( final AsyncConstructorListener<EmbeddedWizard> constructorListener ) {
    asyncConstructorListener = constructorListener;
    // setConnectionService(connectionService);
    // setDatasourceService(datasourceService);

    wizardModel.addDatasource( new CsvDatasource( datasourceModel, datasourceService, csvDatasourceService ) );
    wizardModel.addDatasource( new QueryDatasource( datasourceService, datasourceModel ) );
    wizardModel.addDatasource( new MultiTableDatasource( datasourceModel ) );

    if ( checkHasAccess ) {
      datasourceService.hasPermission( new XulServiceCallback<Boolean>() {
        public void error( String message, Throwable error ) {
          MessageHandler.getInstance().showErrorDialog(
            datasourceMessages.getString( "DatasourceEditor.ERROR" ), //$NON-NLS-1$
            datasourceMessages.getString(
              "DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", error.getLocalizedMessage() ) ); //$NON-NLS-1$
        }

        public void success( Boolean retVal ) {
          loadXul();
          onDialogReady();
        }
      } );
    } else {
      loadXul();
    }

  }

  private void loadXul() {
    String url = GWT.getModuleBaseURL();
    bundle = new ResourceBundle( url, "modeler", true, this );
    AsyncXulLoader.loadXulFromUrl( url + MAIN_WIZARD_PANEL, GWT.getModuleBaseURL() + MAIN_WIZARD_PANEL_PACKAGE,
      EmbeddedWizard.this );
  }

  public void onCancel() {
    dialog.hide();
    datasourceModel.clearModel();
    wizardController.setActiveStep( 0 );
    wizardController.resetSelectedDatasource();
  }

  @Override
  public void onFinish( final IDatasourceSummary summary ) {
    this.summary = summary;
    if ( wizardModel.isEditing() && summary.getErrorCount() == 0 ) {
      // biserver-6210 - manage modeler dialog listener separate from the wizard's listener
      handleModelerDialog();
      return;
    }
    final boolean showModelerDecision = !wizardModel.isEditing();
    summaryDialogController.showSummaryDialog( summary, showModelerDecision,
      new XulServiceCallback<IDatasourceSummary>() {
        @Override
        public void error( String s, Throwable throwable ) {
          wizardController.resetSelectedDatasource();
          MessageHandler.getInstance().closeWaitingDialog();
          MessageHandler.getInstance().showErrorDialog( s, throwable.getMessage() );
        }

        @Override
        public void success( IDatasourceSummary iDatasourceSummary ) {
          if ( !showModelerDecision ) {
            handleModelerDialog();
            wizardController.resetSelectedDatasource();
            return;
          } else {
            if ( iDatasourceSummary.isShowModeler() ) {
              showModelEditor();
            } else {
              onDialogAccept();
              wizardController.resetSelectedDatasource();
            }
          }
          MessageHandler.getInstance().closeWaitingDialog();
        }
      } );
  }

  private void handleModelerDialog() {
    MessageHandler.getInstance().closeWaitingDialog();

    // biserver-6210 - manage modeler dialog listener separate from the wizard's listener
    if ( modelerDialogListener != null ) {
      modelerDialogListener.onDialogAccept( getDialogResult() );
    }
    modelerDialogListener = null;
  }

  private void checkInitialized() {
    if ( !initialized ) {
      throw new IllegalStateException( datasourceMessages
        .getString( "DatasourceEditor.ERROR_0003_CONSTRUCTOR_NOT_INITIALIZED_ERROR" ) ); //$NON-NLS-1$
    }
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener( org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener ) {
    checkInitialized();
    super.addDialogListener( listener );
    listener.onDialogReady();
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void showDialog() {
    this.modelerDialogListener = null;
    if ( connectionController != null ) {
      connectionController.reloadConnections();
    }

    if ( datasourceModel.getGuiStateModel().getConnections() == null
      || datasourceModel.getGuiStateModel().getConnections().size() <= 0 ) {
      checkInitialized();
    }
    wizardModel.setEditing( false );
    wizardController.setActiveStep( 0 );
    wizardModel.reset();
    wizardController.resetSelectedDatasource();
    wizardModel.setReportingOnlyValid( this.reportingOnlyValid );

    /*
     * BISERVER-5153: Work around where XulGwtButton is getting its disabled state and style confused. The only way to
     * get the train on the track is to flip-flop it.
     */
    XulButton nextButton =
      (XulButton) mainWizardContainer.getDocumentRoot().getElementById( "main_wizard_window_extra2" ); //$NON-NLS-1$
    nextButton.setDisabled( false );
    nextButton.setDisabled( true );
    /* end of work around */

    dialog.show();

    // BISERVER-6473
    XulTextbox datasourceName = (XulTextbox) mainWizardContainer.getDocumentRoot().getElementById( "datasourceName" ); //$NON-NLS-1$
    datasourceName.setFocus();
  }

  public void showEditDialog( final Domain domain, DialogListener<Domain> listener ) {
    checkInitialized();

    // biserver-6210
    this.modelerDialogListener = listener;

    String datasourceType = (String) domain.getLogicalModels().get( 0 ).getProperty( "DatasourceType" );

    // previous versions of Data-access would leave this property blank for Query datasources.
    if ( datasourceType == null ) {
      datasourceType = "SQL-DS";
    }

    IWizardDatasource selectedDatasource = null;
    for ( IWizardDatasource datasource : wizardModel.getDatasources() ) {
      if ( datasource.getId().equals( datasourceType ) ) {
        selectedDatasource = datasource;
        break;
      }
    }
    if ( selectedDatasource == null ) {
      Window.alert( "bad one: " + datasourceType );
      MessageHandler.getInstance().showErrorDialog(
        datasourceMessages.getString( "datasourceDialog.ERROR_INCOMPATIBLE_DOMAIN_TITLE" ),
        datasourceMessages.getString( "datasourceDialog.ERROR_INCOMPATIBLE_DOMAIN" ) );
      return;
    }

    wizardModel.reset();
    wizardModel.setReportingOnlyValid( this.reportingOnlyValid );
    wizardModel.setSelectedDatasource( selectedDatasource );
    wizardModel.setEditing( true );
    wizardController.reset();
    selectedDatasource.restoreSavedDatasource( domain, new XulServiceCallback<Void>() {
      @Override
      public void error( String s, Throwable throwable ) {
        MessageHandler.getInstance().showErrorDialog(
          datasourceMessages.getString( "datasourceDialog.ERROR_INCOMPATIBLE_DOMAIN" ), throwable.getMessage() );
      }

      @Override
      public void success( Void aVoid ) {
        dialog.show();
      }
    } );
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.gwt.util.IXulLoaderCallback#overlayLoaded()
   */
  public void overlayLoaded() {

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.gwt.util.IXulLoaderCallback#overlayRemoved()
   */
  public void overlayRemoved() {
    // TODO Auto-generated method stub

  }

  public XulDomContainer getMainWizardContainer() {
    return mainWizardContainer;
  }

  // public void setConnectionService(IXulAsyncConnectionService service) {
  // this.connectionService = service;
  // if(connectionController != null){
  // connectionController.setService(service);
  // connectionController.reloadConnections();
  // }
  // }

  // public IXulAsyncConnectionService getConnectionService() {
  // return connectionService;
  // }

  public IXulAsyncDSWDatasourceService getDatasourceService() {
    return datasourceService;
  }

  public MainWizardController getWizardController() {
    return wizardController;
  }

  public void setDatasourceService( IXulAsyncDSWDatasourceService datasourceService ) {
    this.datasourceService = datasourceService;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.gwt.util.IXulLoaderCallback#xulLoaded(org.pentaho.ui.xul.gwt.GwtXulRunner)
   */
  public void xulLoaded( GwtXulRunner runner ) {

    mainWizardContainer = runner.getXulDomContainers().get( 0 );

    Document rootDocument = mainWizardContainer.getDocumentRoot();
    BindingFactory bf = new GwtBindingFactory( rootDocument );

    ResourceBundle resBundle = (ResourceBundle) mainWizardContainer.getResourceBundles().get( 0 );

    datasourceMessages = new GwtDatasourceMessages();
    datasourceMessages.setMessageBundle( resBundle );
    MessageHandler.getInstance().setMessages( datasourceMessages );

    connectionController = new ConnectionController( rootDocument );
    connectionController.setDatasourceModel( datasourceModel );
    // connectionController.setService(connectionService);
    mainWizardContainer.addEventHandler( connectionController );

    summaryDialogController.setBindingFactory( bf );
    mainWizardContainer.addEventHandler( summaryDialogController );

    wizardController = new MainWizardController( bf, wizardModel, datasourceService );
    mainWizardContainer.addEventHandler( wizardController );

    dialog = (XulDialog) rootDocument.getElementById( WIZARD_DIALOG_ID );
    MessageHandler.getInstance().setWizardDialog( dialog );

    datasourceController = new WizardDatasourceController();
    datasourceController.setBindingFactory( bf );
    datasourceController.setDatasourceMessages( datasourceMessages );
    mainWizardContainer.addEventHandler( datasourceController );

    mainWizardContainer.addEventHandler( MessageHandler.getInstance() );

    // add the steps ..
    // physicalStep = new RelationalPhysicalStep(datasourceService, connectionService, datasourceMessages, this);

    wizardController.addWizardListener( this );

    // Controller for the File Import functionality
    FileImportController fileImportController = new FileImportController( datasourceModel, datasourceMessages );
    mainWizardContainer.addEventHandler( fileImportController );

    // init other controllers
    fileImportController.init();
    MessageHandler.getInstance().init();
    summaryDialogController.init();

    // Create the gui
    try {
      // new WizardContentPanel(wizardController).addContent(mainWizardContainer);
      wizardController.init();
      initialized = true;
    } catch ( Exception throwable ) {
      throwable.printStackTrace();
    }

    // Remap the upload action in development mode
    if ( GWT.isScript() == false ) {
      XulFileUpload upload = (XulFileUpload) rootDocument.getElementById( "fileUpload" ); //$NON-NLS-1$
      upload.setAction( GWT.getModuleBaseURL() + "UploadService" ); //$NON-NLS-1$
    }

    initialized = true;

    if ( asyncConstructorListener != null ) {
      asyncConstructorListener.asyncConstructorDone( this );
    }

  }

  public DatasourceModel getDatasourceModel() {
    return datasourceModel;
  }

  public void setDatasourceModel( DatasourceModel datasourceModel ) {
    this.datasourceModel = datasourceModel;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.util.AbstractXulDialogController#getDialog()
   */
  @Override
  protected XulDialog getDialog() {
    return dialog;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.pentaho.ui.xul.util.AbstractXulDialogController#getDialogResult()
   */
  @Override
  protected Domain getDialogResult() {
    return summary.getDomain();
  }

  public DatasourceMessages getDatasourceMessages() {
    return datasourceMessages;
  }

  public void setDatasourceMessages( DatasourceMessages datasourceMessages ) {
    this.datasourceMessages = datasourceMessages;
  }

  public void setWizardController( MainWizardController wizardController ) {
    this.wizardController = wizardController;
  }

  @Bindable
  public void showModelEditor() {
    // open up the modeler
    final DialogListener<Domain> listener = new DialogListener<Domain>() {
      public void onDialogCancel() {
        EmbeddedWizard.this.onDialogAccept();
        wizardController.resetSelectedDatasource();
      }

      public void onDialogAccept( final Domain domain ) {
        EmbeddedWizard.this.onDialogAccept();
        wizardController.resetSelectedDatasource();
      }

      public void onDialogReady() {
      }

      @Override
      public void onDialogError( String errorMessage ) {
        wizardController.resetSelectedDatasource();
      }
    };
    final Domain domain = summary.getDomain();

    ModelerDialog.getInstance( this, new AsyncConstructorListener<ModelerDialog>() {
      public void asyncConstructorDone( ModelerDialog dialog ) {
        dialog.addDialogListener( listener );
        MessageHandler.getInstance().closeWaitingDialog();
        dialog.showDialog( domain );
      }
    } );

  }

  @Override
  public void error( String s, Throwable throwable ) {
    // To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void success( Domain domain ) {
    MessageHandler.getInstance().closeWaitingDialog();
    if ( summary.isShowModeler() ) {
      showModelEditor();
    } else {
      onDialogAccept();
    }

  }

  /**
   * Upload path is by necessity relative. as such it will differ where the module is based.
   * 
   * @param path
   */
  public void setUploadPath( String path ) {
    Document rootDocument = mainWizardContainer.getDocumentRoot();
    XulFileUpload upload = (XulFileUpload) rootDocument.getElementById( "fileUpload" ); //$NON-NLS-1$
    upload.setAction( path );
  }

  public void addDatasource( IWizardDatasource datasource ) {
    wizardModel.addDatasource( datasource );
  }

  public void removeDatasourceOfType( Class<? extends IWizardDatasource> datasource ) {
    wizardModel.removeDatasourceByType( datasource );
  }

  public IWizardModel getWizardModel() {
    return wizardModel;
  }

  public void setWizardModel( IWizardModel wizardModel ) {
    this.wizardModel = wizardModel;
  }

  public ICsvDatasourceServiceAsync getCsvDatasourceService() {
    return csvDatasourceService;
  }

  public void setCsvDatasourceService( ICsvDatasourceServiceAsync csvDatasourceService ) {
    this.csvDatasourceService = csvDatasourceService;
  }

  public void setReportingOnlyValid( boolean reportingOnlyValid ) {
    this.reportingOnlyValid = reportingOnlyValid;
  }

  public ConnectionController getConnectionController() {
    return connectionController;
  }

  public boolean isInitialized() {
    return initialized;
  }
}
