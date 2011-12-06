package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulRadioGroup;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class AnalysisImportDialogController extends AbstractXulEventHandler {

	private BindingFactory bf;
	private ImportDialogController importDialogController;
	private XulRadioGroup basicOrAdvancedRadioGroup;
	private XulMenuList dataSourceListDropDown;
	private XulTree parametersTree;

	public AnalysisImportDialogController(ImportDialogController importDialogController) {
		this.importDialogController = importDialogController;
	}

	public void init() {

		basicOrAdvancedRadioGroup = (XulRadioGroup) document.getElementById("basicOrAdvancedRadioGroup"); //$NON-NLS-1$
		dataSourceListDropDown = (XulMenuList) document.getElementById("dataSourceListDropDown"); //$NON-NLS-1$
		parametersTree = (XulTree) document.getElementById("parametersTree"); //$NON-NLS-1$
	}

	public void setBindingFactory(final BindingFactory bf) {
		this.bf = bf;
	}

	@Override
	public String getName() {
		return "analysisImportDialogController";
	}

	@Bindable
	public void showDialog() {
		importDialogController.reset();
		importDialogController.show(1);
	}
}
