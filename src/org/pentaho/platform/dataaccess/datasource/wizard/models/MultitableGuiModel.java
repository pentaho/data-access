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

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import mondrian.rolap.aggmatcher.AggStar.Table.JoinCondition;

import org.pentaho.agilebi.modeler.multitable.JoinDTO;
import org.pentaho.agilebi.modeler.multitable.JoinFieldDTO;
import org.pentaho.agilebi.modeler.multitable.JoinTableDTO;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

import com.google.gwt.core.client.UnsafeNativeLong;

public class MultitableGuiModel extends XulEventSourceAdapter {

	private AbstractModelList<JoinRelationshipGuiModel> joins;
	private AbstractModelList<JoinedTableGuiModel> selectedTables;
	private AbstractModelList<JoinedTableGuiModel> availableTables;
	private AbstractModelList<JoinedTableGuiModel> leftTables;
	private AbstractModelList<JoinedTableGuiModel> rightTables;
	private JoinedTableGuiModel leftJoinTable;
	private JoinedTableGuiModel rightJoinTable;
	private JoinedFieldGuiModel leftKeyField;
	private JoinedFieldGuiModel rightKeyField;
	private JoinRelationshipGuiModel selectedJoin;
	private JoinedTableGuiModel factTable;
	private boolean doOlap;

	public MultitableGuiModel() {
		this.availableTables = new AbstractModelList<JoinedTableGuiModel>();
		this.selectedTables = new AbstractModelList<JoinedTableGuiModel>();
		this.leftTables = new AbstractModelList<JoinedTableGuiModel>();
		this.rightTables = new AbstractModelList<JoinedTableGuiModel>();
		this.joins = new AbstractModelList<JoinRelationshipGuiModel>();
		this.leftJoinTable = new JoinedTableGuiModel();
		this.rightJoinTable = new JoinedTableGuiModel();
		this.selectedJoin = new JoinRelationshipGuiModel();
	}

	@Bindable
	public AbstractModelList<JoinedTableGuiModel> getAvailableTables() {
		return this.availableTables;
	}

	@Bindable
	public void setAvailableTables(AbstractModelList<JoinedTableGuiModel> availableTables) {
		this.availableTables.setChildren(availableTables);
	}

	@Bindable
	public AbstractModelList<JoinedTableGuiModel> getSelectedTables() {
		return this.selectedTables;
	}

	@Bindable
	public void setSelectedTables(AbstractModelList<JoinedTableGuiModel> selectedTables) {
		this.selectedTables = selectedTables;
	}

	@Bindable
	public JoinedTableGuiModel getLeftJoinTable() {
		return this.leftJoinTable;
	}

	@Bindable
	public void setLeftJoinTable(JoinedTableGuiModel leftJoinTable) {
		this.leftJoinTable = leftJoinTable;
	}

	@Bindable
	public JoinedTableGuiModel getRightJoinTable() {
		return this.rightJoinTable;
	}

	@Bindable
	public void setRightJoinTable(JoinedTableGuiModel rightJoinTable) {
		this.rightJoinTable = rightJoinTable;
	}

	@Bindable
	public JoinedFieldGuiModel getLeftKeyField() {
		return this.leftKeyField;
	}

	@Bindable
	public void setLeftKeyField(JoinedFieldGuiModel leftKeyField) {
		this.leftKeyField = leftKeyField;
	}
	
	@Bindable
	public JoinedFieldGuiModel getRightKeyField() {
		return this.rightKeyField;
	}

	@Bindable
	public void setRightKeyField(JoinedFieldGuiModel rightKeyField) {
		this.rightKeyField = rightKeyField;
	}

	@Bindable
	public AbstractModelList<JoinRelationshipGuiModel> getJoins() {
		return this.joins;
	}

	@Bindable
	public void setJoins(AbstractModelList<JoinRelationshipGuiModel> joins) {
		this.joins = joins;
	}

	@Bindable
	public JoinRelationshipGuiModel getSelectedJoin() {
		return this.selectedJoin;
	}

	@Bindable
	public void setSelectedJoin(JoinRelationshipGuiModel selectedJoin) {
		this.selectedJoin = selectedJoin;
	}
	
	@Bindable
	public JoinedTableGuiModel getFactTable() {
		return factTable;
	}
	
	@Bindable
	public void setFactTable(JoinedTableGuiModel factTable) {
		this.factTable = factTable;
	}

	public void addJoin(JoinRelationshipGuiModel join) {
		this.joins.add(join);
	}

	public void removeSelectedJoin() {
		this.joins.remove(this.selectedJoin);
	}

	public void addSelectedTable(JoinedTableGuiModel table) {
		this.availableTables.remove(table);
		this.selectedTables.add(table);
	}

	public void removeSelectedTable(JoinedTableGuiModel table) {
		this.selectedTables.remove(table);
		this.availableTables.add(table);
	}
	
	@Bindable
	public AbstractModelList<JoinedTableGuiModel> getLeftTables() {
		return this.leftTables;
	}
	
	@Bindable
	public AbstractModelList<JoinedTableGuiModel> getRightTables() {
		return this.rightTables;
	}
	
	public void doOlap(boolean isStar) {
		this.doOlap = isStar;
	}
	
	public void computeJoinDefinitionStepTables() {
		this.leftTables.clear();
		this.rightTables.clear();
		if(this.doOlap) {
			this.leftTables.add(this.factTable);
			for(JoinedTableGuiModel table : this.selectedTables) {
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

		List<JoinedTableGuiModel> joinTables = new ArrayList<JoinedTableGuiModel>();
		for (String table : tables) {
			JoinedTableGuiModel joinTable = new JoinedTableGuiModel();
			joinTable.setName(table);
			joinTables.add(joinTable);
		}

		Collections.sort(joinTables, new Comparator<JoinedTableGuiModel>() {
			@Override
			public int compare(JoinedTableGuiModel joinTableModel, JoinedTableGuiModel joinTableModel1) {
				return joinTableModel.getName().compareTo(joinTableModel1.getName());
			}
		});
		setAvailableTables(new AbstractModelList<JoinedTableGuiModel>(joinTables));
	}

	@Deprecated
	public List<LogicalRelationship> generateLogicalRelationships(List<JoinRelationshipGuiModel> joins) {
		String locale = LocalizedString.DEFAULT_LOCALE;
		List<LogicalRelationship> logicalRelationships = new ArrayList<LogicalRelationship>();
		for (JoinRelationshipGuiModel join : joins) {
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
		for (JoinedTableGuiModel tbl : this.selectedTables) {
			selectedTables.add(tbl.getName());
		}
		dto.setSelectedTables(selectedTables);
		dto.setJoins(this.generateJoinDTOs(this.getJoins()));
		return dto;
	}
	
	
	public List<JoinDTO> generateJoinDTOs(List<JoinRelationshipGuiModel> joins) {
		List<JoinDTO> logicalRelationships = new ArrayList<JoinDTO>();
		for (JoinRelationshipGuiModel join : joins) {
			JoinTableDTO fromTable = new JoinTableDTO();
			fromTable.setName(join.getLeftKeyFieldModel().getParentTable().getName());

			JoinTableDTO toTable = new JoinTableDTO();
			toTable.setName(join.getRightKeyFieldModel().getParentTable().getName());

			JoinFieldDTO fromColumn = new JoinFieldDTO();
			fromColumn.setName(join.getLeftKeyFieldModel().getName());
			fromColumn.setParentTable(fromTable);

			JoinFieldDTO toColumn = new JoinFieldDTO();
			toColumn.setName(join.getRightKeyFieldModel().getName());
			toColumn.setParentTable(toTable);

			JoinDTO logicalRelationship = new JoinDTO();
			logicalRelationship.setLeftKeyFieldModel(fromColumn);
			logicalRelationship.setRightKeyFieldModel(toColumn);
			logicalRelationships.add(logicalRelationship);
		}
		return logicalRelationships;
	}
	

	public void populateJoinGuiModel(Domain domain, MultiTableDatasourceDTO dto) {
 
		// existing joinTableModels will not have fields. We can add these from
		// the domain.
		addFieldsToTables(domain, this.availableTables);

		// Populate "selectedTables" from availableTables using
		// logicalRelationships.
		AbstractModelList<JoinedTableGuiModel> selectedTablesList = new AbstractModelList<JoinedTableGuiModel>();
		for (String selectedTable : dto.getSelectedTables()) {
			this.selectTable(selectedTable, selectedTablesList);
		}
		this.selectedTables.addAll(selectedTablesList);
		
		// Populate "joins" from availableTables using logicalRelationships.
		AbstractModelList<JoinRelationshipGuiModel> joinsList = new AbstractModelList<JoinRelationshipGuiModel>();
		for (JoinDTO logicalRelationship : dto.getJoins()) {
			this.populateJoin(logicalRelationship, joinsList);
		}
		this.joins.addAll(joinsList);
	}

	private void addFieldsToTables(Domain domain, AbstractModelList<JoinedTableGuiModel> availableTables) {

		String locale = LocalizedString.DEFAULT_LOCALE;
		for (JoinedTableGuiModel table : availableTables) {
			for (LogicalTable tbl : domain.getLogicalModels().get(0).getLogicalTables()) {
				if(tbl.getPhysicalTable().getProperty("target_table").equals(table.getName())){
					for (LogicalColumn col : tbl.getLogicalColumns()) {
						JoinedFieldGuiModel field = new JoinedFieldGuiModel();
						field.setName(col.getName(locale));
						field.setParentTable(table);
						table.getFields().add(field);
					}
					continue;
				}
			}
		}
	}

	
	private void populateJoin(JoinDTO logicalRelationship, AbstractModelList<JoinRelationshipGuiModel> joinsList) {

		JoinRelationshipGuiModel join = new JoinRelationshipGuiModel();

		for (JoinedTableGuiModel table : this.selectedTables) {
			if (table.getName().equals(logicalRelationship.getLeftKeyFieldModel().getName())) {
				for (JoinedFieldGuiModel field : table.getFields()) {
					if (field.getName().equals(logicalRelationship.getRightKeyFieldModel().getName())) {
						join.setLeftKeyFieldModel(field);
					}
				}
			}

			if (table.getName().equals(logicalRelationship.getLeftKeyFieldModel().getParentTable().getName())) {
				for (JoinedFieldGuiModel field : table.getFields()) {
					if (field.getName().equals(logicalRelationship.getRightKeyFieldModel().getParentTable().getName())) {
						join.setRightKeyFieldModel(field);
					}
				}
			}
		}
		joinsList.add(join);
	}

	private void selectTable(String selectedTable, AbstractModelList<JoinedTableGuiModel> selectedTablesList) {
		for (JoinedTableGuiModel table : this.availableTables) {
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
