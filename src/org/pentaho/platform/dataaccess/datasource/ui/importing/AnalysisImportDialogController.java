/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created December 08, 2011
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.ui.importing;

import java.util.List;

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringUtils;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractXulDialogController;

import com.google.gwt.user.client.Window;

@SuppressWarnings("all")
public class AnalysisImportDialogController extends AbstractXulDialogController<AnalysisImportDialogModel> implements IImportPerspective {

	private BindingFactory bf;
	private XulMenuList connectionList;
	private XulTree analysisParametersTree;
	private XulDialog importDialog;
	private XulDialog analysisParametersDialog;
	private ResourceBundle resBundle;
	private AnalysisImportDialogModel importDialogModel;
	private IXulAsyncConnectionService connectionService;
	private XulTextbox paramNameTextBox;
	private XulTextbox paramValueTextBox;
	private XulDeck analysisPreferencesDeck;
	private XulRadio availableRadio;	
	private XulRadio manualRadio;
	private XulButton acceptButton;
	
	private static final Integer PARAMETER_MODE = 1;
	private static final Integer DATASOURCE_MODE = 0;

	public void init() {
		try {
			resBundle = (ResourceBundle) super.getXulDomContainer().getResourceBundles().get(0);
			connectionService = new ConnectionServiceGwtImpl();
			importDialogModel = new AnalysisImportDialogModel();
			connectionList = (XulMenuList) document.getElementById("connectionList");
			analysisParametersTree = (XulTree) document.getElementById("analysisParametersTree");
			importDialog = (XulDialog) document.getElementById("importDialog");
			analysisParametersDialog = (XulDialog) document.getElementById("analysisParametersDialog");
			paramNameTextBox = (XulTextbox) document.getElementById("paramNameTextBox");
			paramValueTextBox = (XulTextbox) document.getElementById("paramValueTextBox");
			analysisPreferencesDeck = (XulDeck) document.getElementById("analysisPreferencesDeck");
			availableRadio = (XulRadio) document.getElementById("availableRadio");			
			manualRadio = (XulRadio) document.getElementById("manualRadio");
			acceptButton = (XulButton) document.getElementById("importDialog_accept");
			acceptButton.setDisabled(true);
			
			bf.setBindingType(Binding.Type.ONE_WAY);
			bf.createBinding(connectionList, "selectedItem", importDialogModel, "connection");
			bf.createBinding(manualRadio, "checked", this, "preference", new PreferencesBindingConvertor());

			Binding connectionListBinding = bf.createBinding(importDialogModel, "connectionList", connectionList, "elements");
			Binding analysisParametersBinding = bf.createBinding(importDialogModel, "analysisParameters", analysisParametersTree, "elements");

			connectionListBinding.fireSourceChanged();
			analysisParametersBinding.fireSourceChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public XulDialog getDialog() {
		return importDialog;
	}

	public AnalysisImportDialogModel getDialogResult() {
		return importDialogModel;
	}

	private void reset() {
		reloadConnections();
		importDialogModel.removeAllParameters();
		importDialogModel.setUploadedFile(null);
		availableRadio.setSelected(true);
		acceptButton.setDisabled(true);
		setPreference(DATASOURCE_MODE);
	}

	private void reloadConnections() {
		if (connectionService != null) {
			connectionService.getConnections(new XulServiceCallback<List<Connection>>() {
				public void error(String message, Throwable error) {
					error.printStackTrace();
					Window.alert(message);
				}

				public void success(List<Connection> connections) {
					importDialogModel.setConnectionList(connections);
				}
			});
		}
	}

	public boolean isValid() {
		return importDialogModel.isValid();
	}

	public void concreteUploadCallback(String fileName, String uploadedFile) {
		acceptButton.setDisabled(!importDialogModel.isValid());
	}

	public void genericUploadCallback(String uploadedFile) {
		importDialogModel.setUploadedFile(uploadedFile);
		acceptButton.setDisabled(!importDialogModel.isValid());
	}

	@Bindable
	public void setPreference(Integer preference) {
		analysisPreferencesDeck.setSelectedIndex(preference);
		importDialogModel.setParameterMode(preference == PARAMETER_MODE);
		acceptButton.setDisabled(!importDialogModel.isValid());
	}

	@Bindable
	public void removeParameter() {
		int[] selectedRows = analysisParametersTree.getSelectedRows();
		if (selectedRows.length == 1) {
			importDialogModel.removeParameter(selectedRows[0]);
			acceptButton.setDisabled(!importDialogModel.isValid());
		}
	}

	@Bindable
	public void addParameter() {
		String paramName = paramNameTextBox.getValue();
		String paramValue = paramValueTextBox.getValue();
		if (!StringUtils.isEmpty(paramName) && !StringUtils.isEmpty(paramValue)) {
			importDialogModel.addParameter(paramName, paramValue);
			closeParametersDialog();
			acceptButton.setDisabled(!importDialogModel.isValid());
		}
	}

	@Bindable
	public void closeParametersDialog() {
		paramNameTextBox.setValue("");
		paramValueTextBox.setValue("");
		importDialogModel.setSelectedAnalysisParameter(-1);
		analysisParametersTree.clearSelection();
		analysisParametersDialog.hide();
	}

	@Bindable
	public void editParameter() {
		int[] selectedRows = analysisParametersTree.getSelectedRows();
		if (selectedRows.length == 1) {
			importDialogModel.setSelectedAnalysisParameter(selectedRows[0]);
			ParameterDialogModel parameter = importDialogModel.getSelectedAnalysisParameter();
			paramNameTextBox.setValue(parameter.getName());
			paramValueTextBox.setValue(parameter.getValue());
			analysisParametersDialog.show();
		}
	}

	@Bindable
	public void openParametersDialog() {
		analysisParametersDialog.show();
	}

	public void showDialog() {
		reset();
		importDialog.setTitle(resBundle.getString("importDialog.IMPORT_MONDRIAN", "Import Mondrian"));
		super.showDialog();
	}

	public void setBindingFactory(final BindingFactory bf) {
		this.bf = bf;
	}

	public String getName() {
		return "analysisImportDialogController";
	}

	class PreferencesBindingConvertor extends BindingConvertor<Boolean, Integer> {

		public Integer sourceToTarget(Boolean value) {
			int result = 0;
			if (value) {
				result = 1;
			}
			return result;
		}

		public Boolean targetToSource(Integer value) {
			return true;
		}
	}
}
