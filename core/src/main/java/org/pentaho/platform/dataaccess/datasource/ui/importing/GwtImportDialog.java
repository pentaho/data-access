/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceMessages;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.util.DialogController.DialogListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

public class GwtImportDialog implements IXulLoaderCallback {

  private MetadataImportDialogController metadataImportDialogController;

  private AnalysisImportDialogController analysisImportDialogController;

  private AsyncConstructorListener<GwtImportDialog> constructorListener;

  private ImportDialogController importDialogController;

  private GwtDatasourceMessages datasourceMessages;

  private int initial_delay = 150;

  public GwtImportDialog( AsyncConstructorListener<GwtImportDialog> constructorListener ) {
    this.constructorListener = constructorListener;
    try {
      AsyncXulLoader.loadXulFromUrl( GWT.getModuleBaseURL() + "importDialog.xul", GWT.getModuleBaseURL()
        + "importDialog", this );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public void xulLoaded( GwtXulRunner runner ) {
    try {
      XulDomContainer container = runner.getXulDomContainers().get( 0 );

      BindingFactory bf = new GwtBindingFactory( container.getDocumentRoot() );
      ResourceBundle resBundle = (ResourceBundle) container.getResourceBundles().get( 0 );
      datasourceMessages = new GwtDatasourceMessages();
      datasourceMessages.setMessageBundle( resBundle );

      metadataImportDialogController = new MetadataImportDialogController();
      metadataImportDialogController.setBindingFactory( bf );
      container.addEventHandler( metadataImportDialogController );
      metadataImportDialogController.setDatasourceMessages( datasourceMessages );

      analysisImportDialogController = new AnalysisImportDialogController();
      analysisImportDialogController.setBindingFactory( bf );
      container.addEventHandler( analysisImportDialogController );
      analysisImportDialogController.setDatasourceMessages( datasourceMessages );

      importDialogController = new ImportDialogController();
      importDialogController.addImportPerspective( 0, metadataImportDialogController );
      importDialogController.addImportPerspective( 1, analysisImportDialogController );
      container.addEventHandler( importDialogController );

      runner.initialize();
      runner.start();

      importDialogController.init();
      metadataImportDialogController.init();
      analysisImportDialogController.init();
      //analysisImportDialogController.editDatasource(datasourceInfo);

      if ( constructorListener != null ) {
        constructorListener.asyncConstructorDone( this );
      }

    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  public void showMetadataImportDialog( DialogListener<MetadataImportDialogModel> listener ) {
    metadataImportDialogController.addDialogListener( listener );
    importDialogController.show( 0 );
  }

  public void showAnalysisImportDialog( DialogListener<AnalysisImportDialogModel> listener ) {
    showAnalysisImportDialog( listener, null );
  }

  public void showAnalysisImportDialog( DialogListener<AnalysisImportDialogModel> listener,
                                        IDatasourceInfo datasourceInfo ) {
    analysisImportDialogController.addDialogListener( listener );
    importDialogController.show( 1 );
    if ( initial_delay == 150 ) {
      initializeEditDatasource( datasourceInfo );
    } else {
      analysisImportDialogController.editDatasource( datasourceInfo );
    }
  }

  private void initializeEditDatasource( final IDatasourceInfo datasourceInfo ) {
    Timer timer = new Timer() {
      public void run() {
        analysisImportDialogController.editDatasource( datasourceInfo );
        initial_delay = 0;
      }
    };
    timer.schedule( initial_delay );
  }

  public MetadataImportDialogController getMetadataImportDialogController() {
    return metadataImportDialogController;
  }

  public AnalysisImportDialogController getAnalysisImportDialogController() {
    return analysisImportDialogController;
  }

  public void overlayLoaded() {
  }

  public void overlayRemoved() {
  }
}
