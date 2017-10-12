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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.controllers;

import com.google.gwt.http.client.URL;
import org.pentaho.platform.dataaccess.datasource.wizard.DatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

//TODO: move to the CSV datasource package
public class FileImportController extends AbstractXulEventHandler {

  private XulDialog datasourceDialog;

  private XulFileUpload fileUpload;

  private XulDialog errorDialog;

  private XulLabel errorLabel;

  private BindingFactory bf;

  private DatasourceMessages messages;

  private DatasourceModel datasourceModel;

  public FileImportController( DatasourceModel datasourceModel, DatasourceMessages messages ) {
    this.datasourceModel = datasourceModel;
    setDatasourceMessages( messages );

  }

  @Bindable
  public void init() {
    bf = new GwtBindingFactory( document );

    fileUpload = (XulFileUpload) document.getElementById( "fileUpload" ); //$NON-NLS-1$
    datasourceDialog = (XulDialog) document.getElementById( "fileImportEditorWindow" ); //$NON-NLS-1$
    errorLabel = (XulLabel) document.getElementById( "errorLabel" ); //$NON-NLS-1$
    errorDialog = (XulDialog) document.getElementById( "errorDialog" ); //$NON-NLS-1$

    BindingConvertor<String, Boolean> isDisabledConvertor = new BindingConvertor<String, Boolean>() {

      @Override
      public Boolean sourceToTarget( String aValue ) {
        return ( aValue == null || "".equals( aValue ) );
      }

      @Override
      public String targetToSource( Boolean aValue ) {
        return null;
      }
    };

    bf.createBinding( "fileUpload", "selectedFile", "okButton", "disabled",
      isDisabledConvertor ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

  }

  @Override
  public String getName() {
    return "fileImportController"; //$NON-NLS-1$
  }

  @Bindable
  public void submitCsv() {
    String fileName = fileUpload.getSeletedFile();

    if ( fileName != null ) {
      fileName = extractFilename( fileName );
    }

    fileUpload.addParameter( "file_name", fileName == null ? "" : URL.encodeQueryString( fileName ) ); //$NON-NLS-1$ //$NON-NLS-2$
    fileUpload.addParameter( "mark_temporary", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
    // specify that we want any compressed files to be unpacked
    fileUpload.addParameter( "unzip", "true" ); //$NON-NLS-1$ //$NON-NLS-2$
    showWaitingDialog();
    fileUpload.submit();
  }

  public void closeWaitingDialog() {
    MessageHandler.getInstance().closeWaitingDialog();
  }

  public void showWaitingDialog() {
    MessageHandler.getInstance().showWaitingDialog(
      messages.getString( "fileImportDialog.UPLOADING", fileUpload.getSeletedFile() ) ); //$NON-NLS-1$
  }

  @Bindable
  public void showFileImportDialog() {
    fileUpload.setSelectedFile( "" );
    datasourceDialog.show();
  }

  @Bindable
  public void close() {
    datasourceDialog.hide();
  }

  @Bindable
  public void uploadSuccess( String uploadedFile ) {
    closeWaitingDialog();
    close();
    String selectedFile = this.fileUpload.getSeletedFile();
    String selectedFileLc = selectedFile.toLowerCase();
    String uploadedFileLc = uploadedFile.toLowerCase();

    if ( uploadedFile.indexOf( "\n" ) != -1 ) {
      // uploadedFile is newline-separated list of file names
      // for now we only support a single file
      showErroDialog( messages.getString( "fileImportDialog.COMPRESSED_TOO_MANY_FILES" ) );
      return;
    }
    if ( selectedFileLc.endsWith( ".zip" ) || selectedFileLc.endsWith( ".tgz" ) || selectedFileLc.endsWith( ".tar" ) ) {
      // check to see what kind of file was extracted from the compressed upload
      if ( !uploadedFileLc.endsWith( ".csv.tmp" ) && !uploadedFileLc.endsWith( ".txt.tmp" ) ) {
        showErroDialog( messages.getString( "fileImportDialog.COMPRESSED_NO_CSV" ) );
        return;
      }
    }

    if ( selectedFileLc.endsWith( ".csv" ) || selectedFileLc.endsWith( ".txt" )
      || selectedFileLc.endsWith( ".zip" ) || selectedFileLc.endsWith( ".tgz" ) || selectedFileLc.endsWith( ".tar" ) ) {
      datasourceModel.getModelInfo().getFileInfo().setTmpFilename( uploadedFile );
      datasourceModel.getModelInfo().getFileInfo().setFriendlyFilename( extractFilename( selectedFile ) );
    } else {
      showErroDialog( messages.getString( "fileImportDialog.INVALID_FILE" ) );
    }
  }

  private String extractFilename( String path ) {
    int idx = path.lastIndexOf( '\\' );
    if ( idx >= 0 ) { // Windows-based path
      return path.substring( idx + 1 );
    }
    idx = path.lastIndexOf( '/' );
    if ( idx >= 0 ) { // Unix-based path
      return path.substring( idx + 1 );
    }
    return path; // just the filename
  }

  @Bindable
  public void uploadFailure( Throwable t ) {
    closeWaitingDialog();
    close();
    datasourceModel.getModelInfo().getFileInfo().setTmpFilename( "" );
    datasourceModel.getModelInfo().getFileInfo().setFriendlyFilename( "" );
    showErroDialog( t.getMessage() );
  }

  public void showErroDialog( String error ) {
    errorLabel.setValue( error );
    errorDialog.show();
  }

  @Bindable
  public void closeErrorDialog() {
    errorDialog.hide();
  }


  public void setDatasourceMessages( DatasourceMessages datasourceMessages ) {
    this.messages = datasourceMessages;
  }


}
