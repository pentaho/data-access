package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class ImportDialogController extends AbstractXulEventHandler {

	private XulDialog importDialog;
	private XulDialog fileUploadDialog;
	private XulTextbox uploadedFile;
	private XulFileUpload fileUpload;
	private XulDeck importDeck;

	public void init() {
		fileUploadDialog = (XulDialog) document.getElementById("fileUploadDialog"); //$NON-NLS-1$
		importDeck = (XulDeck) document.getElementById("importDeck"); //$NON-NLS-1$
		importDialog = (XulDialog) document.getElementById("importDialog"); //$NON-NLS-1$
		fileUpload = (XulFileUpload) document.getElementById("fileUpload"); //$NON-NLS-1$
	}

	@Bindable
	public void showFileUploadDialog() {
		fileUploadDialog.show();
	}

	@Bindable
	public void uploadSuccess() {
	}

	@Bindable
	public void uploadFailure() {

	}

	@Bindable
	public void uploadFile() {
		try {
			String selectedFile = fileUpload.getSeletedFile();
			uploadedFile = (XulTextbox) document.getElementById("uploadedFile");
			uploadedFile.setValue(selectedFile);
		} catch (Exception e) {

		}
		closeUpload();
	}

	@Bindable
	public void closeUpload() {
		fileUploadDialog.hide();
	}

	@Override
	public String getName() {
		return "importDialogController";
	}

	public void reset() {
		XulTextbox fileTextBox = (XulTextbox) document.getElementById("uploadedFile");
		fileTextBox.setValue("");
	}

	@Bindable
	public void closeDialog() {
		importDialog.hide();
	}

	public void show(int index) {
		importDeck.setSelectedIndex(index);
		importDialog.show();
	}
}
