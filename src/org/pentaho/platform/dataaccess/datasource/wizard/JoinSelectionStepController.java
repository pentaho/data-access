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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 * 
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinTableModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.GwtXulRunner;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.util.AsyncXulLoader;
import org.pentaho.ui.xul.gwt.util.IXulLoaderCallback;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

@SuppressWarnings("unchecked")
public class JoinSelectionStepController extends AbstractXulEventHandler implements IXulLoaderCallback {

	protected static final String JOIN_STEP_PANEL = "joinSelection.xul";
	protected static final String JOIN_STEP_PANEL_PACKAGE = "joinSelection";
	protected static final String JOIN_STEP_PANEL_ID = "joinSelectionWindow";

	private IConnection selectedConnection;
	private XulDialog tablesSelectionDialog;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private XulListbox availableTables;
	private XulListbox selectedTables;
	private JoinGuiModel joinGuiModel;
	private JoinDefinitionStepController definitionStepController;

	public JoinSelectionStepController(IConnection selectedConnection) {
		this.selectedConnection = selectedConnection;
		this.joinSelectionServiceGwtImpl = new JoinSelectionServiceGwtImpl();
		this.joinGuiModel = new JoinGuiModel();
		this.definitionStepController = new JoinDefinitionStepController(this.joinGuiModel, this.joinSelectionServiceGwtImpl, this.selectedConnection);
		this.getAvailableTables();
	}

	public String getName() {
		return "joinSelectionStepController";
	}

	public void show() {
		this.tablesSelectionDialog.show();
	}

	public void xulLoaded(GwtXulRunner runner) {

		XulDomContainer mainContainer = runner.getXulDomContainers().get(0);
		Document rootDocument = mainContainer.getDocumentRoot();
		mainContainer.addEventHandler(this);
		mainContainer.addEventHandler(this.definitionStepController);
		this.definitionStepController.init();

		this.tablesSelectionDialog = (XulDialog) rootDocument.getElementById(JOIN_STEP_PANEL_ID);
		this.availableTables = (XulListbox) rootDocument.getElementById("availableTables");
		this.selectedTables = (XulListbox) rootDocument.getElementById("selectedTables");
		BindingFactory bf = new GwtBindingFactory(rootDocument);

		Binding availableTablesBinding = bf.createBinding(this.joinGuiModel.getAvailableTables(), "children", this.availableTables, "elements");
		Binding selectedTablesBinding = bf.createBinding(this.joinGuiModel.getSelectedTables(), "children", this.selectedTables, "elements");

		try {
			availableTablesBinding.fireSourceChanged();
			selectedTablesBinding.fireSourceChanged();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getAvailableTables() {
		joinSelectionServiceGwtImpl.getDatabaseTables(this.selectedConnection, new XulServiceCallback<List>() {
			public void error(String message, Throwable error) {
				error.printStackTrace();
			}

			public void success(List tables) {
				joinGuiModel.processAvailableTables(tables);
				AsyncXulLoader.loadXulFromUrl(JOIN_STEP_PANEL, JOIN_STEP_PANEL_PACKAGE, JoinSelectionStepController.this);
			}
		});
	}

	@Bindable
	public void addSelectedTable() {
		if (this.availableTables.getSelectedItem() != null) {
			this.joinGuiModel.addSelectedTable((JoinTableModel) this.availableTables.getSelectedItem());
		}
	}

	@Bindable
	public void removeSelectedTable() {
		if (this.selectedTables.getSelectedItem() != null) {
			this.joinGuiModel.removeSelectedTable((JoinTableModel) this.selectedTables.getSelectedItem());
		}
	}

	public void overlayLoaded() {
	}

	public void overlayRemoved() {
	}
}
