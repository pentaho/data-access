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
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

import com.google.gwt.user.client.Window;

public class AnalysisImportDialogController extends AbstractXulEventHandler implements IImportPerspective {

	private BindingFactory bf;
	private XulMenuList connectionList;
	private XulTree parametersTree;
	private XulButton addButton;
	private XulButton removeButton;
	private XulDialog importDialog;
	private ResourceBundle resBundle;
	private ImportGuiStateModel guiStateModel;
	private IXulAsyncConnectionService connectionService;

	public void init() {
		try {
			resBundle = (ResourceBundle) super.getXulDomContainer().getResourceBundles().get(0);
			connectionService = new ConnectionServiceGwtImpl();
			guiStateModel = new ImportGuiStateModel();
			addButton = (XulButton) document.getElementById("addButton");
			removeButton = (XulButton) document.getElementById("removeButton");
			connectionList = (XulMenuList) document.getElementById("connectionList");
			parametersTree = (XulTree) document.getElementById("parametersTree");
			importDialog = (XulDialog) document.getElementById("importDialog");

			bf.setBindingType(Binding.Type.ONE_WAY);
			Binding connectionListBinding = bf.createBinding(guiStateModel, "connectionList", connectionList, "elements");

			connectionListBinding.fireSourceChanged();
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
					guiStateModel.setConnectionList(connections);
				}

			});
		}
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
