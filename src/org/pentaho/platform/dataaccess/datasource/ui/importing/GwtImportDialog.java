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
