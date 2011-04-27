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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.AbstractWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

@SuppressWarnings("unchecked")
public class JoinDefinitionsStep extends AbstractWizardStep implements PropertyChangeListener {

	protected static final String JOIN_DEFINITION_PANEL_ID = "joinDefinitionWindow";

	private XulVbox joinDefinitionDialog;
	private MultitableGuiModel joinGuiModel;
	private XulMenuList<JoinTableModel> leftTables;
	private XulMenuList<JoinTableModel> rightTables;
	private XulListbox joins;
	private XulListbox leftKeyFieldList;
	private XulListbox rightKeyFieldList;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private IConnection selectedConnection;
	private JoinValidator validator;
	private Binding rightKeyFieldBinding;
	private Binding leftKeyFieldBinding;

	public JoinDefinitionsStep(MultitableGuiModel joinGuiModel, JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl, MultiTableDatasource parentDatasource) {
		super(parentDatasource);
		this.joinGuiModel = joinGuiModel;
		this.joinSelectionServiceGwtImpl = joinSelectionServiceGwtImpl;
	}

	public void propertyChange(PropertyChangeEvent evt) {
	
		final XulMenuList<JoinTableModel> tablesList = (XulMenuList<JoinTableModel>) evt.getSource();
		List<JoinTableModel> tables = null;
		if (tablesList.equals(leftTables)) {
			tables = this.joinGuiModel.getLeftTables();
		} else if (tablesList.equals(rightTables)) {
			tables = this.joinGuiModel.getRightTables();
		}
		
		final JoinTableModel table = tablesList.getSelectedIndex() >= 0 ? (JoinTableModel) tables.get(tablesList.getSelectedIndex()) : null;
		if (table != null) {
			joinSelectionServiceGwtImpl.getTableFields(table.getName(), this.selectedConnection, new XulServiceCallback<List>() {
				public void error(String message, Throwable error) {
				}

				public void success(List fields) {
					try {
						List<JoinFieldModel> fieldModels = table.processTableFields(fields);
						table.setFields(new AbstractModelList<JoinFieldModel>(fieldModels));
						if (tablesList.equals(leftTables)) {
							leftKeyFieldList.setElements(fields);	
							leftKeyFieldBinding.fireSourceChanged();
						} else if (tablesList.equals(rightTables)) {
							rightKeyFieldList.setElements(fields);
							rightKeyFieldBinding.fireSourceChanged();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
	}

	public String getName() {
		return "joinDefinitionStepController";
	}

	@Bindable
	public void createJoin() {
		JoinRelationshipModel join = new JoinRelationshipModel();
		join.setLeftKeyFieldModel(this.joinGuiModel.getLeftKeyField());
		join.setRightKeyFieldModel(this.joinGuiModel.getRightKeyField());

		if (this.validator.isValid(join)) {
			this.joinGuiModel.addJoin(join);
      this.parentDatasource.setFinishable(this.validator.allTablesJoined());
		} else {
			((MultiTableDatasource) this.parentDatasource).displayErrors(this.validator.getError());
		}
	}

	@Bindable
	public void deleteJoin() {
		this.joinGuiModel.removeSelectedJoin();
	}

	@Override
	public void init(IWizardModel wizardModel) throws XulException {
		this.validator = new JoinValidator(this.joinGuiModel, wizardModel);
		this.joinDefinitionDialog = (XulVbox) document.getElementById(JOIN_DEFINITION_PANEL_ID);
		this.joins = (XulListbox) document.getElementById("joins");
		this.leftKeyFieldList = (XulListbox) document.getElementById("leftKeyField");
		this.rightKeyFieldList = (XulListbox) document.getElementById("rightKeyField");

		this.leftTables = (XulMenuList<JoinTableModel>) document.getElementById("leftTables");
		this.leftTables.addPropertyChangeListener(this);

		this.rightTables = (XulMenuList<JoinTableModel>) document.getElementById("rightTables");
		this.rightTables.addPropertyChangeListener(this);

		super.init(wizardModel);
	}

	public void setBindings() {

		BindingFactory bf = new GwtBindingFactory(document);
		bf.createBinding(this.joinGuiModel.getLeftTables(), "children", this.leftTables, "elements");
		bf.createBinding(this.joinGuiModel.getRightTables(), "children", this.rightTables, "elements");
		bf.createBinding(this.leftTables, "selectedItem", this.joinGuiModel, "leftJoinTable");
		bf.createBinding(this.rightTables, "selectedItem", this.joinGuiModel, "rightJoinTable");
		bf.createBinding(this.joinGuiModel.getJoins(), "children", this.joins, "elements");
		bf.createBinding(this.joins, "selectedItem", this.joinGuiModel, "selectedJoin");
		
		this.leftKeyFieldBinding = bf.createBinding(this.leftKeyFieldList, "selectedIndex", this.joinGuiModel, "leftKeyField", new BindingConvertor<Integer, JoinFieldModel>() {

			@Override
			public JoinFieldModel sourceToTarget(final Integer index) {
				List<JoinFieldModel> fields = joinGuiModel.getLeftJoinTable().getFields();
				if (index == -1 || fields.isEmpty()) {
					return null;
				}
				return fields.get(index);
			}

			@Override
			public Integer targetToSource(final JoinFieldModel value) {
				return null;
			}
		});

		this.rightKeyFieldBinding = bf.createBinding(this.rightKeyFieldList, "selectedIndex", this.joinGuiModel, "rightKeyField", new BindingConvertor<Integer, JoinFieldModel>() {

			@Override
			public JoinFieldModel sourceToTarget(final Integer index) {
				List<JoinFieldModel> fields = joinGuiModel.getRightJoinTable().getFields();
				if (index == -1 || fields.isEmpty()) {
					return null;
				}
				return fields.get(index);
			}

			@Override
			public Integer targetToSource(final JoinFieldModel value) {
				return null;
			}
		});
	}

	@Override
	public void stepActivatingForward() {
		
		super.stepActivatingForward();
		this.selectedConnection = ((MultiTableDatasource) this.parentDatasource).getConnection();
		this.joinGuiModel.computeJoinDefinitionStepTables();
		this.leftTables.setSelectedIndex(0);
		this.rightTables.setSelectedIndex(0);
		checkExistingJoinsStillValid();
		parentDatasource.setFinishable(this.validator.allTablesJoined());
	}

	private void checkExistingJoinsStillValid() {
		Set<String> allTables = new HashSet<String>();
		for (JoinTableModel tbl : joinGuiModel.getAvailableTables()) {
			allTables.add(tbl.getName());
		}

		List<JoinRelationshipModel> toRemove = new ArrayList<JoinRelationshipModel>();

		for (JoinRelationshipModel join : joinGuiModel.getJoins()) {
			if (!allTables.contains(join.getLeftKeyFieldModel().getParentTable().getName()) || !allTables.contains(join.getRightKeyFieldModel().getParentTable().getName())) {
				toRemove.add(join);
			}
		}
		for (JoinRelationshipModel join : toRemove) {
			joinGuiModel.getJoins().remove(join);
		}
	}

	public String getStepName() {
		return MessageHandler.getString("multitable.DEFINE_JOINS");
	}

	public XulComponent getUIComponent() {
		return this.joinDefinitionDialog;
	}

	public void resetComponents() {
		this.leftKeyFieldList.setElements(new AbstractModelList<JoinFieldModel>());
		this.rightKeyFieldList.setElements(new AbstractModelList<JoinFieldModel>());
	}
}
