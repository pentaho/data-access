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

import java.util.*;

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
public class JoinDefinitionsStep extends AbstractWizardStep { 

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
		this.parentDatasource.setFinishable(this.validator.allTablesJoined());
	}

	@Override
	public void init(IWizardModel wizardModel) throws XulException {
		this.validator = new JoinValidator(this.joinGuiModel, wizardModel);
		this.joinDefinitionDialog = (XulVbox) document.getElementById(JOIN_DEFINITION_PANEL_ID);
		this.joins = (XulListbox) document.getElementById("joins");
		this.leftKeyFieldList = (XulListbox) document.getElementById("leftKeyField");
		this.rightKeyFieldList = (XulListbox) document.getElementById("rightKeyField");
		this.leftTables = (XulMenuList<JoinTableModel>) document.getElementById("leftTables");
		this.rightTables = (XulMenuList<JoinTableModel>) document.getElementById("rightTables");
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
		bf.createBinding(this.leftTables, "selectedItem", this.leftKeyFieldList, "elements", new TableSelectionConvertor(this.leftTables));
		bf.createBinding(this.rightTables, "selectedItem", this.rightKeyFieldList, "elements", new TableSelectionConvertor(this.rightTables));
		
		this.leftKeyFieldBinding = bf.createBinding(this.leftKeyFieldList, "selectedIndex", this.joinGuiModel, "leftKeyField", new BindingConvertor<Integer, JoinFieldModel>() {
			@Override
			public JoinFieldModel sourceToTarget(final Integer index) {
				JoinTableModel joinTable = joinGuiModel.getLeftJoinTable();
				if(joinTable != null) {
					List<JoinFieldModel> fields = joinTable.getFields();
					if (index == -1 || fields.isEmpty()) {
						return null;
					}
					return fields.get(index);
				}
				return null;
			}

			@Override
			public Integer targetToSource(final JoinFieldModel value) {
				return null;
			}
		});

		this.rightKeyFieldBinding = bf.createBinding(this.rightKeyFieldList, "selectedIndex", this.joinGuiModel, "rightKeyField", new BindingConvertor<Integer, JoinFieldModel>() {
			@Override
			public JoinFieldModel sourceToTarget(final Integer index) {
				JoinTableModel joinTable = joinGuiModel.getRightJoinTable();
				if(joinTable != null) {
					List<JoinFieldModel> fields = joinTable.getFields();
					if (index == -1 || fields.isEmpty()) {
						return null;
					}
					return fields.get(index);
				}
				return null;
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
		parentDatasource.setFinishable(this.validator.allTablesJoined());
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
	
	class TableSelectionConvertor extends BindingConvertor<JoinTableModel, List> {
		private XulMenuList<JoinTableModel> source;
		
		public TableSelectionConvertor(XulMenuList<JoinTableModel> source) {
			this.source = source;
		}
		
		@Override
		public List sourceToTarget(final JoinTableModel table) {
			if (table != null) {
        if(table.getFields() == null || table.getFields().isEmpty()){
          MessageHandler.getInstance().showWaitingDialog(MessageHandler.getString("multitable.FETCHING_TABLE_INFO"));
          joinSelectionServiceGwtImpl.getTableFields(table.getName(), selectedConnection, new XulServiceCallback<List>() {
            public void error(String message, Throwable error) {
              MessageHandler.getInstance().closeWaitingDialog();
            }

            public void success(List fields) {
              try {
                List<JoinFieldModel> fieldModels = table.processTableFields(fields);
                table.setFields(new AbstractModelList<JoinFieldModel>(fieldModels));
                if (source.equals(leftTables)) {
                  leftKeyFieldList.setElements(table.getFields());
                  leftKeyFieldBinding.fireSourceChanged();
                } else if (source.equals(rightTables)) {
                  rightKeyFieldList.setElements(table.getFields());
                  rightKeyFieldBinding.fireSourceChanged();
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
              MessageHandler.getInstance().closeWaitingDialog();
            }
          });
        }
        return table.getFields();

			}
      return Collections.emptyList();
		}

		@Override
		public JoinTableModel targetToSource(List value) {
			return null;
		}
	}	
}
