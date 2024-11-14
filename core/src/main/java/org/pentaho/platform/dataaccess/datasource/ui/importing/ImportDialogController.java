/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.ui.importing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.user.client.Window;

public class ImportDialogController extends AbstractXulEventHandler {

  private XulFileUpload genericFileUpload;
  //	private XulFileUpload concreteFileUpload;
  private XulDeck importDeck;
  private Map<Integer, IImportPerspective> importPerspectives;
  private IImportPerspective activeImportPerspective;

  public ImportDialogController() {
    importPerspectives = new HashMap<Integer, IImportPerspective>();
  }

  public void init() {
    importDeck = (XulDeck) document.getElementById( "importDeck" );

    try {
      genericFileUpload = (XulFileUpload) document.getElementById( "genericFileUpload" );
      genericFileUpload.addPropertyChangeListener( new FileUploadPropertyChangeListener() );
    } catch ( Exception e ) {
      // Gobble this up if there isn't one in the document.
    }

    //		concreteFileUpload = (XulFileUpload) document.getElementById("concreteFileUpload");
    //		concreteFileUpload.addPropertyChangeListener(new FileUploadPropertyChangeListener());
  }

  public void addImportPerspective( int index, IImportPerspective importPerspective ) {
    importPerspectives.put( index, importPerspective );
  }

  public String getName() {
    return "importDialogController";
  }

  public void show( int index ) {
    reset();
    importDeck.setSelectedIndex( index );
    activeImportPerspective = importPerspectives.get( index );
    activeImportPerspective.showDialog();
  }

  private void reset() {
    if ( genericFileUpload != null ) {
      genericFileUpload.setSelectedFile( "" );
    }
    //		concreteFileUpload.setSelectedFile("");
  }

  @Bindable
  public void genericUploadSuccess( String uploadedFile ) {
    try {
      activeImportPerspective.genericUploadCallback( uploadedFile );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  @Bindable
  public void concreteUploadSuccess( String uploadedFile ) {
    //		try {
    //			String selectedFile = concreteFileUpload.getSeletedFile();
    //			activeImportPerspective.concreteUploadCallback(selectedFile, uploadedFile);
    //		} catch (Exception e) {
    //			e.printStackTrace();
    //		}
  }

  @Bindable
  public void uploadFailure( Throwable error ) {
    error.printStackTrace();
    Window.alert( error.getMessage() );
  }

  @Bindable
  public void closeDialog() {
    activeImportPerspective.onDialogCancel();
  }

  @Bindable
  public void acceptDialog() {
    if ( activeImportPerspective.isValid() ) {
      activeImportPerspective.onDialogAccept();
      closeDialog();
    }
  }

  class FileUploadPropertyChangeListener implements PropertyChangeListener {

    public void propertyChange( PropertyChangeEvent propertyChangeEvent ) {
      XulFileUpload uploadControl = (XulFileUpload) propertyChangeEvent.getSource();
      String value = uploadControl.getSeletedFile();
      if ( !StringUtils.isEmpty( value ) ) {
        uploadControl.addParameter( "file_name", uploadControl.getSeletedFile() );
        uploadControl.addParameter( "mark_temporary", "true" );
        uploadControl.addParameter( "unzip", "true" );
        uploadControl.submit();
      }
    }
  }
}
