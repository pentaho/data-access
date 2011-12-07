package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncConstructorListener;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;

import com.google.gwt.core.client.GWT;

public class GwtImportDialog implements IXulLoaderCallback {

	protected MetadataImportDialogController metadataImportDialogController;

	protected AnalysisImportDialogController analysisImportDialogController;

	protected AsyncConstructorListener<GwtImportDialog> constructorListener;

	public GwtImportDialog(final AsyncConstructorListener<GwtImportDialog> constructorListener) {
		this.constructorListener = constructorListener;
		try {
			AsyncXulLoader.loadXulFromUrl(GWT.getModuleBaseURL() + "importDialog.xul", GWT.getModuleBaseURL() + "importDialog", this); //$NON-NLS-1$//$NON-NLS-2$
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void overlayLoaded() {
	}

	public void overlayRemoved() {
	}

	public void showMetadataImportDialog() {
		metadataImportDialogController.showDialog();
	}
	
	public void showAnalysisImportDialog() {
		analysisImportDialogController.showDialog();
	}

	@Override
	public void xulLoaded(GwtXulRunner runner) {
		try {
			GwtXulDomContainer container = (GwtXulDomContainer) runner.getXulDomContainers().get(0);

			BindingFactory bf = new GwtBindingFactory(container.getDocumentRoot());

			ImportDialogController importDialogController = new ImportDialogController();
			container.addEventHandler(importDialogController);

			metadataImportDialogController = new MetadataImportDialogController(importDialogController);
			metadataImportDialogController.setBindingFactory(bf);
			container.addEventHandler(metadataImportDialogController);

			analysisImportDialogController = new AnalysisImportDialogController(importDialogController);
			analysisImportDialogController.setBindingFactory(bf);
			container.addEventHandler(analysisImportDialogController);

			runner.initialize();

			runner.start();

			importDialogController.init();
			metadataImportDialogController.init();
			analysisImportDialogController.init();

			if (constructorListener != null) {
				constructorListener.asyncConstructorDone(this);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
