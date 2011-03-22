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
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinSelectionStepModel;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class JoinDefinitionStepController extends AbstractXulEventHandler {

	protected static final String JOIN_DEFINITION_PANEL_ID = "joinDefinitionWindow";

	private XulDialog joinDefinitionDialog;
	private JoinSelectionStepModel joinModel;
	private XulListbox leftTables;
	private XulListbox rightTables;

	public JoinDefinitionStepController(JoinSelectionStepModel joinModel) {
		this.joinModel = joinModel;
	}

	public void init() {

		XulDomContainer mainContainer = getXulDomContainer();
		Document rootDocument = mainContainer.getDocumentRoot();
		mainContainer.addEventHandler(this);
		this.joinDefinitionDialog = (XulDialog) rootDocument.getElementById(JOIN_DEFINITION_PANEL_ID);
		this.leftTables = (XulListbox) rootDocument.getElementById("leftTables");
		this.rightTables = (XulListbox) rootDocument.getElementById("rightTables");

		BindingFactory bf = new GwtBindingFactory(rootDocument);

		Binding leftTablesBinding = bf.createBinding(this.joinModel.getSelectedTables(), "children", this.leftTables, "elements");
		Binding rightTablesBinding = bf.createBinding(this.joinModel.getSelectedTables(), "children", this.rightTables, "elements");

		try {
			leftTablesBinding.fireSourceChanged();
			rightTablesBinding.fireSourceChanged();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getName() {
		return "joinDefinitionStepController";
	}

	@Bindable
	public void next() {
		this.joinDefinitionDialog.show();
	}
}
