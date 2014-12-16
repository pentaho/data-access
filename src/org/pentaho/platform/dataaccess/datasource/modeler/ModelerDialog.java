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

package org.pentaho.platform.dataaccess.datasource.modeler;

import org.pentaho.agilebi.modeler.ColResolverController;
import org.pentaho.agilebi.modeler.IModelerMessages;
import org.pentaho.agilebi.modeler.IModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.ModelerController;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerUiHelper;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.GwtModelerMessages;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerServiceAsync;
import org.pentaho.agilebi.modeler.services.impl.GwtModelerServiceImpl;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.platform.dataaccess.datasource.wizard.EmbeddedWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceServiceAsync;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceServiceGwtImpl;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.core.client.GWT;
//import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;

/**
 * User: nbaker Date: Aug 10, 2010
 */
public class ModelerDialog extends AbstractXulDialogController<Domain> implements IXulLoaderCallback {

  private XulDomContainer container;
  private DialogListener<Domain> listener;
  private IModelerServiceAsync service = new GwtModelerServiceImpl();

  private ModelerWorkspace model = new ModelerWorkspace( new GwtModelerWorkspaceHelper() );
  private ModelerController controller;

  private IModelerMessages messages;
  private XulDialog errorDialog;

  private EmbeddedWizard wizard;
  //  private IXulAsyncConnectionService connectionService;
  private IXulAsyncDSWDatasourceService datasourceService;
  private ICsvDatasourceServiceAsync csvService;
  private AsyncConstructorListener constructorListener;
  private static ModelerDialog instance;

  // button to edit the datasource
  private static String EDIT_TOOLBAR_BUTTON_ID = "edit-icon";

  private ModelerDialog( final AsyncConstructorListener<ModelerDialog> constructorListener ) {
    this( null, constructorListener );
  }

  private ModelerDialog( EmbeddedWizard wizard, final AsyncConstructorListener<ModelerDialog> constructorListener ) {
    this.wizard = wizard;
    this.constructorListener = constructorListener;
    AsyncXulLoader.loadXulFromUrl( GWT.getModuleBaseURL() + "modeler.xul", GWT.getModuleBaseURL() + "modeler", this );
  }

  public static ModelerDialog getInstance( final AsyncConstructorListener<ModelerDialog> constructorListener ) {
    if ( instance != null ) {
      constructorListener.asyncConstructorDone( instance );
      return instance;
    }
    instance = new ModelerDialog( constructorListener );
    return instance;
  }

  public static ModelerDialog getInstance( EmbeddedWizard wizard,
                                           final AsyncConstructorListener<ModelerDialog> constructorListener ) {
    if ( instance != null ) {
      constructorListener.asyncConstructorDone( instance );
      return instance;
    }
    instance = new ModelerDialog( wizard, constructorListener );
    return instance;
  }


  @Override
  protected XulDialog getDialog() {
    return (XulDialog) container.getDocumentRoot().getElementById( "modeler_dialog" );
  }

  @Override
  protected Domain getDialogResult() {
    return null;
  }

  @Bindable
  public void onAccept() {
    enableWaitCursor( true );
    try {
      model.getWorkspaceHelper().populateDomain( model );
    } catch ( ModelerException e ) {
      e.printStackTrace();
      showErrorDialog( messages.getString( "ModelEditor.ERROR" ),
        messages.getString( "ModelEditor.ERROR_0001_SAVING_MODELS" ) );
    }
    boolean doOlap = true;
    LogicalModel lModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( lModel == null ) {
      lModel = model.getLogicalModel( ModelerPerspective.REPORTING );
    }
    if ( lModel.getProperty( "MondrianCatalogRef" ) == null
      && ( lModel.getProperty( "DUAL_MODELING_SCHEMA" ) == null || "false"
        .equals( (String) lModel.getProperty( "DUAL_MODELING_SCHEMA" ).getValue() ) ) ) {
      doOlap = false;
    }
    service.serializeModels( model.getDomain(), model.getModelName(), doOlap, new XulServiceCallback<String>() {
      public void success( String retVal ) {
        enableWaitCursor( false );
        hideDialog();
        model.getDomain().setId( retVal );
        ModelerDialog.this.listener.onDialogAccept( model.getDomain() );
      }

      public void error( String message, Throwable error ) {
        enableWaitCursor( false );
        showErrorDialog( messages.getString( "ModelEditor.ERROR" ),
          messages.getString( "ModelEditor.ERROR_0001_SAVING_MODELS" ) );
      }
    } );
  }


  protected void showErrorDialog( String title, String message ) {
    errorDialog = (XulDialog) container.getDocumentRoot().getElementById( "errorDialog" );
    XulLabel errorLabel = (XulLabel) container.getDocumentRoot().getElementById( "errorLabel" );
    errorDialog.setTitle( title );
    errorLabel.setValue( message );
    errorDialog.show();
  }

  @Bindable
  public void onLoad() {
  }

  public void xulLoaded( GwtXulRunner gwtXulRunner ) {
    container = gwtXulRunner.getXulDomContainers().get( 0 );
    container.addEventHandler( this );

    BogoPojo bogo = new BogoPojo();
    service.gwtWorkaround( bogo, new XulServiceCallback<BogoPojo>() {
      public void success( BogoPojo retVal ) {

      }

      public void error( String message, Throwable error ) {

      }
    } );

    datasourceService = new DSWDatasourceServiceGwtImpl();
    //    connectionService = new ConnectionServiceGwtImpl();
    csvService = (ICsvDatasourceServiceAsync) GWT.create( ICsvDatasourceService.class );

    if ( wizard == null ) {
      wizard = new EmbeddedWizard( false );

      wizard.setDatasourceService( datasourceService );
      //      wizard.setConnectionService(connectionService);
      wizard.setCsvDatasourceService( csvService );
      wizard.init( null );
    }


    messages = new GwtModelerMessages( (ResourceBundle) container.getResourceBundles().get( 0 ) );
    try {
      ModelerMessagesHolder.setMessages( messages );
    } catch ( Exception ignored ) {
      // Messages may have been set earlier, ignore.
    }

    IModelerWorkspaceHelper workspacehelper = model.getWorkspaceHelper();

    controller = new ModelerController( model );
    controller.setWorkspaceHelper( workspacehelper );
    //    controller.setMessages(messages);
    final BindingFactory bf = new GwtBindingFactory( container.getDocumentRoot() );
    controller.setBindingFactory( bf );
    container.addEventHandler( controller );
    try {
      controller.init();
    } catch ( ModelerException e ) {
      e.printStackTrace();
    }

    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( model, "valid", "modeler_dialog_accept", "disabled", new BindingConvertor<Boolean, Boolean>() {
      @Override
      public Boolean sourceToTarget( Boolean value ) {
        return !value;
      }

      @Override
      public Boolean targetToSource( Boolean value ) {
        return !value;
      }
    } );

    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );

    // go get the geocontext from the server. Prop forms are initialized after this call returns as they
    // may need them to create the UI
    datasourceService.getGeoContext( new XulServiceCallback<GeoContext>() {
      public void success( GeoContext geoContext ) {
        model.setGeoContext( geoContext );
        ModelerUiHelper.configureControllers( container, model, bf, controller, new ColResolverController() );
        ModelerDialog.this.constructorListener.asyncConstructorDone( ModelerDialog.this );
      }

      public void error( String s, Throwable throwable ) {
        throwable.printStackTrace();
        // put in a stub to ensure the rest of the dialog works
        model.setGeoContext( new GeoContext() );
        ModelerUiHelper.configureControllers( container, model, bf, controller, new ColResolverController() );
        ModelerDialog.this.constructorListener.asyncConstructorDone( ModelerDialog.this );
      }
    } );

  }

  public void overlayLoaded() {
  }

  public void overlayRemoved() {
  }

  public void showDialog( Domain domain ) {
    enableWaitCursor( true );
    model.setDomain( domain );
    if ( model.getLogicalModel( ModelerPerspective.ANALYSIS ) != null ) {
      controller.setModelerPerspective( ModelerPerspective.ANALYSIS );
    } else {
      controller.setModelerPerspective( ModelerPerspective.REPORTING );
    }
    controller.resetPropertyForm();
    showDialog();
    enableWaitCursor( false );
  }

  public void showDialog( String domainId, String modelId ) {
    showDialog( domainId, modelId, ModelerPerspective.ANALYSIS );
  }

  public void showDialog( String domainId, String modelId, final ModelerPerspective modelerPerspective ) {
    enableWaitCursor( true );
    service.loadDomain( domainId, new XulServiceCallback<Domain>() {
      public void success( Domain retVal ) {
        model.setDomain( retVal );

        if ( model.getLogicalModel( modelerPerspective ) != null ) {
          controller.setModelerPerspective( modelerPerspective );
        } else {
          controller.setModelerPerspective( ModelerPerspective.REPORTING );
        }
        controller.resetPropertyForm();

        // disable edit button if required model not present (imported dsw)
        XulComponent editIcon = container.getDocumentRoot().getElementById( EDIT_TOOLBAR_BUTTON_ID );
        boolean disabled = model.getDomain().getLogicalModels().get( 0 ).getProperty( "datasourceModel" ) == null;
        editIcon.setDisabled( disabled );

        enableWaitCursor( false );
        showDialog();
      }

      public void error( String message, Throwable error ) {
        enableWaitCursor( false );
        showErrorDialog( messages.getString( "ModelEditor.ERROR" ),
          messages.getString( "ModelEditor.ERROR_0002_LOADING_DOMAIN" ) );
      }
    } );
  }

  private void enableWaitCursor( final boolean enable ) {
    if ( enable ) {
      MessageHandler.getInstance().showWaitingDialog( MessageHandler.getString( "waiting.generalWaiting" ) );
    } else {
      MessageHandler.getInstance().closeWaitingDialog();
    }
  }

  @Override
  public String getName() {
    return "modelerDialogController";
  }

  @Bindable
  public void closeErrorDialog() {
    errorDialog.hide();
  }

  @Bindable
  public void onCancel() {
    hideDialog();
    ModelerDialog.this.listener.onDialogCancel();
  }

  @Bindable
  public void onEditSource() {
    showEditSourceDialog();
  }

  public void showEditSourceDialog() {
    wizard.showEditDialog( model.getDomain(), new DialogListener<Domain>() {
      @Override
      public void onDialogAccept( Domain domain ) {
        try {
          model.refresh( domain );
        } catch ( ModelerException e ) {
          showErrorDialog( "Error", e.getMessage() );
        }
      }

      @Override
      public void onDialogCancel() {
        //To change body of implemented methods use File | Settings | File Templates.
      }

      @Override
      public void onDialogReady() {
      }

      @Override
      public void onDialogError( String errorMessage ) {
        // TODO Auto-generated method stub

      }
    } );
  }

  /**
   * Specified by <code>DialogController</code>.
   */
  public void addDialogListener( org.pentaho.ui.xul.util.DialogController.DialogListener<Domain> listener ) {
    //    checkInitialized();
    super.addDialogListener( listener );
    this.listener = listener;
    listener.onDialogReady();
  }

}
