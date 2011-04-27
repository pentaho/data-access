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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

public class MultitableGuiModel extends XulEventSourceAdapter {

	private AbstractModelList<JoinRelationshipModel> joins;
	private AbstractModelList<JoinTableModel> selectedTables;
	private AbstractModelList<JoinTableModel> availableTables;
	private AbstractModelList<JoinTableModel> leftTables;
	private AbstractModelList<JoinTableModel> rightTables;
	private JoinTableModel leftJoinTable;
	private JoinTableModel rightJoinTable;
	private JoinFieldModel leftKeyField;
	private JoinFieldModel rightKeyField;
	private JoinRelationshipModel selectedJoin;
	private JoinTableModel factTable;
	private boolean doOlap;

	public MultitableGuiModel() {
		this.availableTables = new AbstractModelList<JoinTableModel>();
		this.selectedTables = new AbstractModelList<JoinTableModel>();
		this.leftTables = new AbstractModelList<JoinTableModel>();
		this.rightTables = new AbstractModelList<JoinTableModel>();
		this.joins = new AbstractModelList<JoinRelationshipModel>();
		this.leftJoinTable = new JoinTableModel();
		this.rightJoinTable = new JoinTableModel();
		this.selectedJoin = new JoinRelationshipModel();
	}

	@Bindable
	public AbstractModelList<JoinTableModel> getAvailableTables() {
		return this.availableTables;
	}

	@Bindable
	public void setAvailableTables(AbstractModelList<JoinTableModel> availableTables) {
		this.availableTables.setChildren(availableTables);
	}

	@Bindable
	public AbstractModelList<JoinTableModel> getSelectedTables() {
		return this.selectedTables;
	}

	@Bindable
	public void setSelectedTables(AbstractModelList<JoinTableModel> selectedTables) {
		this.selectedTables = selectedTables;
	}

	@Bindable
	public JoinTableModel getLeftJoinTable() {
		return this.leftJoinTable;
	}

	@Bindable
	public void setLeftJoinTable(JoinTableModel leftJoinTable) {
		this.leftJoinTable = leftJoinTable;
	}

	@Bindable
	public JoinTableModel getRightJoinTable() {
		return this.rightJoinTable;
	}

	@Bindable
	public void setRightJoinTable(JoinTableModel rightJoinTable) {
		this.rightJoinTable = rightJoinTable;
	}

	@Bindable
	public JoinFieldModel getLeftKeyField() {
		return this.leftKeyField;
	}

	@Bindable
	public void setLeftKeyField(JoinFieldModel leftKeyField) {
		this.leftKeyField = leftKeyField;
	}
	
	@Bindable
	public JoinFieldModel getRightKeyField() {
		return this.rightKeyField;
	}

	@Bindable
	public void setRightKeyField(JoinFieldModel rightKeyField) {
		this.rightKeyField = rightKeyField;
	}

	@Bindable
	public AbstractModelList<JoinRelationshipModel> getJoins() {
		return this.joins;
	}

	@Bindable
	public void setJoins(AbstractModelList<JoinRelationshipModel> joins) {
		this.joins = joins;
	}

	@Bindable
	public JoinRelationshipModel getSelectedJoin() {
		return this.selectedJoin;
	}

	@Bindable
	public void setSelectedJoin(JoinRelationshipModel selectedJoin) {
		this.selectedJoin = selectedJoin;
	}
	
	@Bindable
	public JoinTableModel getFactTable() {
		return factTable;
	}
	
	@Bindable
	public void setFactTable(JoinTableModel factTable) {
		this.factTable = factTable;
	}

	public void addJoin(JoinRelationshipModel join) {
		this.joins.add(join);
	}

	public void removeSelectedJoin() {
		this.joins.remove(this.selectedJoin);
	}

	public void addSelectedTable(JoinTableModel table) {
		this.availableTables.remove(table);
		this.selectedTables.add(table);
	}

  public void addSelectedTables(List<JoinTableModel> selected) {
    this.availableTables.removeAll(selected);
		this.selectedTables.addAll(selected);
  }

  public void removeSelectedTables(List<JoinTableModel> selected) {
		this.selectedTables.removeAll(selected);
		this.availableTables.addAll(selected);
  }

	public void removeSelectedTable(JoinTableModel table) {
		this.selectedTables.remove(table);
		this.availableTables.add(table);
	}
	
	@Bindable
	public AbstractModelList<JoinTableModel> getLeftTables() {
		return this.leftTables;
	}
	
	@Bindable
	public AbstractModelList<JoinTableModel> getRightTables() {
		return this.rightTables;
	}
	
	public boolean isDoOlap() {
		return this.doOlap;
	}
	
	public void doOlap(boolean isStar) {
		this.doOlap = isStar;
	}
	
	public void computeJoinDefinitionStepTables() {
		this.leftTables.clear();
		this.rightTables.clear();
		if(this.doOlap) {
			this.leftTables.add(this.factTable);
			for(JoinTableModel table : this.selectedTables) {
				if(table.equals(this.factTable)) {
					continue;
				} else {
					this.rightTables.add(table);
				}
			}
		} else {
			this.leftTables.addAll(this.selectedTables);
			this.rightTables.addAll(this.selectedTables);
		}
	}

	public void processAvailableTables(List<String> tables) {

		List<JoinTableModel> joinTables = new ArrayList<JoinTableModel>();
		for (String table : tables) {
			JoinTableModel joinTable = new JoinTableModel();
			joinTable.setName(table);
			joinTables.add(joinTable);
		}

		Collections.sort(joinTables, new Comparator<JoinTableModel>() {
			@Override
			public int compare(JoinTableModel joinTableModel, JoinTableModel joinTableModel1) {
				return joinTableModel.getName().compareTo(joinTableModel1.getName());
			}
		});
		setAvailableTables(new AbstractModelList<JoinTableModel>(joinTables));
	}

	@Deprecated
	public List<LogicalRelationship> generateLogicalRelationships(List<JoinRelationshipModel> joins) {
		String locale = LocalizedString.DEFAULT_LOCALE;
		List<LogicalRelationship> logicalRelationships = new ArrayList<LogicalRelationship>();
		for (JoinRelationshipModel join : joins) {
			LogicalTable fromTable = new LogicalTable();
			fromTable.setName(new LocalizedString(locale, join.getLeftKeyFieldModel().getParentTable().getName()));

			LogicalTable toTable = new LogicalTable();
			toTable.setName(new LocalizedString(locale, join.getRightKeyFieldModel().getParentTable().getName()));

			LogicalColumn fromColumn = new LogicalColumn();
			fromColumn.setName(new LocalizedString(locale, join.getLeftKeyFieldModel().getName()));

			LogicalColumn toColumn = new LogicalColumn();
			toColumn.setName(new LocalizedString(locale, join.getRightKeyFieldModel().getName()));

			LogicalRelationship logicalRelationship = new LogicalRelationship();
			logicalRelationship.setFromTable(fromTable);
			logicalRelationship.setToTable(toTable);
			logicalRelationship.setFromColumn(fromColumn);
			logicalRelationship.setToColumn(toColumn);
			logicalRelationships.add(logicalRelationship);
		}
		return logicalRelationships;
	}

	public MultiTableDatasourceDTO createMultiTableDatasourceDTO(String dsName) {
		MultiTableDatasourceDTO dto = new MultiTableDatasourceDTO();
		dto.setDoOlap(this.doOlap);
		dto.setDatasourceName(dsName);
		List<String> selectedTables = new ArrayList<String>();
		for (JoinTableModel tbl : this.selectedTables) {
			selectedTables.add(tbl.getName());
		}
		dto.setSelectedTables(selectedTables);
        SchemaModel schema = new SchemaModel();
        schema.setJoins(this.getJoins());
        schema.setFactTable(this.factTable);        
		dto.setSchemaModel(schema);
		return dto;
	}
	

	public void populateJoinGuiModel(Domain domain, MultiTableDatasourceDTO dto) {
 
		// existing joinTableModels will not have fields. We can add these from
		// the domain.
		addFieldsToTables(domain, this.availableTables);

		// Populate "selectedTables" from availableTables using
		// logicalRelationships.
		AbstractModelList<JoinTableModel> selectedTablesList = new AbstractModelList<JoinTableModel>();
		for (String selectedTable : dto.getSelectedTables()) {
			this.selectTable(selectedTable, selectedTablesList);
		}
		this.selectedTables.addAll(selectedTablesList);
		this.joins.addAll(dto.getSchemaModel().getJoins());
		this.doOlap(dto.isDoOlap());
		if(dto.isDoOlap()) {
			this.setFactTable(dto.getSchemaModel().getFactTable());
		}
	}

	private void addFieldsToTables(Domain domain, AbstractModelList<JoinTableModel> availableTables) {

		String locale = LocalizedString.DEFAULT_LOCALE;
		for (JoinTableModel table : availableTables) {
			for (LogicalTable tbl : domain.getLogicalModels().get(0).getLogicalTables()) {
				if(tbl.getPhysicalTable().getProperty("target_table").equals(table.getName())){
					for (LogicalColumn col : tbl.getLogicalColumns()) {
						JoinFieldModel field = new JoinFieldModel();
						field.setName(col.getName(locale));
						field.setParentTable(table);
						table.getFields().add(field);
					}
					continue;
				}
			}
		}
	}

	private void selectTable(String selectedTable, AbstractModelList<JoinTableModel> selectedTablesList) {
		for (JoinTableModel table : this.availableTables) {
			if (table.getName().equals(selectedTable)) {
				if (!selectedTablesList.contains(table)) {
					selectedTablesList.add(table);
				}
			}
		}
	}

	public void reset() {
		this.availableTables.clear();
		this.selectedTables.clear();
		this.joins.clear();
		this.leftJoinTable.reset();
		this.rightJoinTable.reset();
	}

}
