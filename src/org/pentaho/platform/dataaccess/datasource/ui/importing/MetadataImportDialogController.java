package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.platform.dataaccess.datasource.ui.admindialog.IGenericImportSource;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

public class MetadataImportDialogController extends AbstractXulDialogController<IGenericImportSource> {

	private BindingFactory bf;
	private XulDialog dialog;

	public void init() {
		dialog = (XulDialog) document.getElementById("metadataImportDialog"); //$NON-NLS-1$
	}

	@Override
	protected XulDialog getDialog() {
		return dialog;
	}

	@Override
	protected IGenericImportSource getDialogResult() {
		return null;
	}

	public void setBindingFactory(final BindingFactory bf) {
		this.bf = bf;
	}

	@Bindable
	public void showDialog() {
		super.showDialog();
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
