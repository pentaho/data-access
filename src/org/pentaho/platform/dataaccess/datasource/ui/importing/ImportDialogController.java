package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.components.XulFileUpload;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class ImportDialogController extends AbstractXulEventHandler {

	private XulDialog dialog;
	private XulDialog uploadDialog;
	private XulTextbox uploadedFile;
	private XulFileUpload fileUpload;

	public void init() {
		uploadDialog = (XulDialog) document.getElementById("fileImportEditorWindow"); //$NON-NLS-1$

		dialog = (XulDialog) document.getElementById("importDialog"); //$NON-NLS-1$
		fileUpload = (XulFileUpload) document.getElementById("fileUpload"); //$NON-NLS-1$
	}

	@Bindable
	public void showFileImportDialog() {
		uploadDialog.show();
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
			String fileTextBox = null;
			if (selectedFile.endsWith(".xmi")) {
				fileTextBox = "metadataFile";
			} else if (selectedFile.endsWith(".xml")) {
				fileTextBox = "analysisFile";
			}
			uploadedFile = (XulTextbox) document.getElementById(fileTextBox);
			uploadedFile.setValue(selectedFile);
		} catch (Exception e) {

		}
		closeUpload();
	}

	@Bindable
	public void closeUpload() {
		uploadDialog.hide();
	}

	@Override
	public String getName() {
		return "importDialogController";
	}

	@Bindable
	public void closeDialog() {
		dialog.hide();
	}
}
