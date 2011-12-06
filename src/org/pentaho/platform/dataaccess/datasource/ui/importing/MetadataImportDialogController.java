package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MetadataImportDialogController extends AbstractXulEventHandler {

	private BindingFactory bf;
	private ImportDialogController importDialogController;
	private XulButton plusButton;
	private XulButton minusButton;
	private XulTree localizedBundlesTree;
	private XulTextbox domainIdText;

	public MetadataImportDialogController(ImportDialogController importDialogController) {
		this.importDialogController = importDialogController;
	}

	public void init() {

		plusButton = (XulButton) document.getElementById("plusButton"); //$NON-NLS-1$
		minusButton = (XulButton) document.getElementById("minusButton"); //$NON-NLS-1$
		localizedBundlesTree = (XulTree) document.getElementById("localizedBundlesTree"); //$NON-NLS-1$
		domainIdText = (XulTextbox) document.getElementById("domainIdText"); //$NON-NLS-1$
	}

	public void setBindingFactory(final BindingFactory bf) {
		this.bf = bf;
	}

	@Override
	public String getName() {
		return "metadataImportDialogController";
	}

	@Bindable
	public void showDialog() {
		importDialogController.reset();
		importDialogController.show(0);
	}
}
