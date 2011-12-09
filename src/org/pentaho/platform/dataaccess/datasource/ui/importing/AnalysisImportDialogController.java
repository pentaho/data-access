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
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

import com.google.gwt.user.client.Window;

public class AnalysisImportDialogController extends AbstractXulEventHandler implements IImportPerspective {

	private BindingFactory bf;
	private XulMenuList connectionList;
	private XulTree analysisParametersTree;
	private XulButton addButton;
	private XulButton removeButton;
	private XulDialog importDialog;
	private XulDialog analysisParametersDialog;
	private ResourceBundle resBundle;
	private ImportDialogModel importDialogModel;
	private IXulAsyncConnectionService connectionService;
	private XulTextbox paramNameTextBox;
	private XulTextbox paramValueTextBox;

	public void init() {
		try {
			resBundle = (ResourceBundle) super.getXulDomContainer().getResourceBundles().get(0);
			connectionService = new ConnectionServiceGwtImpl();
			importDialogModel = new ImportDialogModel();
			addButton = (XulButton) document.getElementById("addButton");
			removeButton = (XulButton) document.getElementById("removeButton");
			connectionList = (XulMenuList) document.getElementById("connectionList");
			analysisParametersTree = (XulTree) document.getElementById("analysisParametersTree");
			importDialog = (XulDialog) document.getElementById("importDialog");
			analysisParametersDialog = (XulDialog) document.getElementById("analysisParametersDialog");
			paramNameTextBox = (XulTextbox) document.getElementById("paramNameTextBox");
			paramValueTextBox = (XulTextbox) document.getElementById("paramValueTextBox");

			bf.setBindingType(Binding.Type.ONE_WAY);
			Binding connectionListBinding = bf.createBinding(importDialogModel, "connectionList", connectionList, "elements");
			Binding analysisParametersBinding = bf.createBinding(importDialogModel, "analysisParameters", analysisParametersTree, "elements");

			connectionListBinding.fireSourceChanged();
			analysisParametersBinding.fireSourceChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void processImport() {
	}

	private void reset() {
		reloadConnections();
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

	@Bindable
	public void saveParameter() {
		// TODO PENDING VALIDATE PARAMETER
		String paramName = paramNameTextBox.getValue();
		String paramValue = paramValueTextBox.getValue();
		importDialogModel.addParameter(paramName, paramValue);
		closeParametersDialog();
	}

	@Bindable
	public void closeParametersDialog() {
		analysisParametersDialog.hide();
	}

	@Bindable
	public void openParametersDialog() {
		analysisParametersDialog.show();
	}

	public void show() {
		reset();
		importDialog.setTitle(resBundle.getString("importDialog.IMPORT_MONDRIAN", "Import Mondrian"));
		importDialog.show();
	}

	public void setBindingFactory(final BindingFactory bf) {
		this.bf = bf;
	}

	public String getName() {
		return "analysisImportDialogController";
	}
}
