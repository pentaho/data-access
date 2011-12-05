package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MetadataImportDialogController extends AbstractXulEventHandler {

	private XulDialog dialog;
	private BindingFactory bf;
	private ImportDialogController importDialogController;

	public MetadataImportDialogController(ImportDialogController importDialogController) {
		this.importDialogController = importDialogController;
	}

	public void init() {

		dialog = (XulDialog) document.getElementById("metadataImportDialog"); //$NON-NLS-1$
	}

	public void setBindingFactory(final BindingFactory bf) {
		this.bf = bf;
	}

	@Bindable
	public void showDialog() {
		XulTextbox fileTextBox = (XulTextbox) document.getElementById("metadataFile");
		fileTextBox.setValue("");
		dialog.show();
	}

	@Bindable
	public void closeDialog() {
		dialog.hide();
	}

	@Override
	public String getName() {
		return "metadataImportDialogController";
	}
}
