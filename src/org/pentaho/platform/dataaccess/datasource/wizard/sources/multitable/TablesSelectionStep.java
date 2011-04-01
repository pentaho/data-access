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

package org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.AbstractWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinTableModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryPhysicalStep;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.stereotype.Bindable;

@SuppressWarnings("unchecked")
public class TablesSelectionStep extends AbstractWizardStep {

	protected static final String JOIN_STEP_PANEL_ID = "joinSelectionWindow";

	private IConnection selectedConnection;
	private XulVbox tablesSelectionDialog;
	private XulListbox availableTables;
	private XulListbox selectedTables;
	private JoinGuiModel joinGuiModel;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;

	public TablesSelectionStep(JoinGuiModel joinGuiModel, JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl, MultiTableDatasource parentDatasource) {
		super(parentDatasource);
		this.joinGuiModel = joinGuiModel;
		this.joinSelectionServiceGwtImpl = joinSelectionServiceGwtImpl;
	}

	public String getName() {
		return "joinSelectionStepController";
	}

	private void getAvailableTables() {
		joinSelectionServiceGwtImpl.getDatabaseTables(this.selectedConnection, new XulServiceCallback<List>() {
			public void error(String message, Throwable error) {
				error.printStackTrace();
			}

			public void success(List tables) {
				joinGuiModel.processAvailableTables(tables);
			}
		});
	}

	@Bindable
	public void addSelectedTable() {
		if (this.availableTables.getSelectedItem() != null) {
			this.joinGuiModel.addSelectedTable((JoinTableModel) this.availableTables.getSelectedItem());
		}
		super.setValid(!this.selectedTables.getElements().isEmpty());
	}

	@Bindable
	public void removeSelectedTable() {
		if (this.selectedTables.getSelectedItem() != null) {
			this.joinGuiModel.removeSelectedTable((JoinTableModel) this.selectedTables.getSelectedItem());
		}
		super.setValid(!this.selectedTables.getElements().isEmpty());
	}

	@Override
	public void init(IWizardModel wizardModel) throws XulException {
		this.tablesSelectionDialog = (XulVbox) document.getElementById(JOIN_STEP_PANEL_ID);
		this.availableTables = (XulListbox) document.getElementById("availableTables");
		this.selectedTables = (XulListbox) document.getElementById("selectedTables");

		super.init(wizardModel);
	}

	public void setBindings() {
		BindingFactory bf = new GwtBindingFactory(document);
		bf.createBinding(this.joinGuiModel.getAvailableTables(), "children", this.availableTables, "elements");
		bf.createBinding(this.joinGuiModel.getSelectedTables(), "children", this.selectedTables, "elements");

	}

	public String getStepName() {
		return "Select Tables";
	}

	public XulComponent getUIComponent() {
		return this.tablesSelectionDialog;
	}

	@Override
	public void stepActivatingReverse() {
		parentDatasource.setFinishable(false);
	}

	@Override
	public void activating() throws XulException {

		//TODO get rid of the DatasourceModel dependency.
		QueryPhysicalStep step = (QueryPhysicalStep) parentDatasource.getSteps().get(0);
		this.selectedConnection = step.getConnectionController().getDatasourceModel().getSelectedRelationalConnection();
		this.getAvailableTables();
	}

}
