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
import java.util.List;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

public class JoinGuiModel extends XulEventSourceAdapter {

	private AbstractModelList<JoinModel> joins;
	private AbstractModelList<JoinTableModel> selectedTables;

	private AbstractModelList<JoinTableModel> availableTables;
	private JoinTableModel leftJoinTable;
	private JoinTableModel rightJoinTable;
	private JoinFieldModel leftKeyField;
	private JoinFieldModel rightKeyField;
	private JoinModel selectedJoin;

	public JoinGuiModel() {
		this.availableTables = new AbstractModelList<JoinTableModel>();
		this.selectedTables = new AbstractModelList<JoinTableModel>();
		this.joins = new AbstractModelList<JoinModel>();
		this.leftJoinTable = new JoinTableModel();
		this.rightJoinTable = new JoinTableModel();
		this.selectedJoin = new JoinModel();
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
	public AbstractModelList<JoinModel> getJoins() {
		return this.joins;
	}

	@Bindable
	public void setJoins(AbstractModelList<JoinModel> joins) {
		this.joins = joins;
	}

	@Bindable
	public JoinModel getSelectedJoin() {
		return this.selectedJoin;
	}

	@Bindable
	public void setSelectedJoin(JoinModel selectedJoin) {
		this.selectedJoin = selectedJoin;
	}

	public void addJoin(JoinModel join) {
		this.joins.add(join);
	}

	public void removeSelectedJoin() {
		this.joins.remove(this.selectedJoin);
	}

	public void addSelectedTable(JoinTableModel table) {
		this.availableTables.remove(table);
		this.selectedTables.add(table);
	}

	public void removeSelectedTable(JoinTableModel table) {
		this.selectedTables.remove(table);

		this.availableTables.add(table);
	}

	public void processAvailableTables(List<String> tables) {

		List<JoinTableModel> joinTables = new ArrayList<JoinTableModel>();
		for (String table : tables) {
			JoinTableModel joinTable = new JoinTableModel();
			joinTable.setName(table);
			joinTables.add(joinTable);
		}
		setAvailableTables(new AbstractModelList<JoinTableModel>(joinTables));
	}

	public List<LogicalRelationship> generateLogicalRelationships(List<JoinModel> joins) {
		String locale = LocalizedString.DEFAULT_LOCALE;
		List<LogicalRelationship> logicalRelationships = new ArrayList<LogicalRelationship>();
		for (JoinModel join : joins) {
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
		dto.setDatasourceName(dsName);
    List<String> selectedTables = new ArrayList<String>();
    for(JoinTableModel tbl : this.selectedTables){
      selectedTables.add(tbl.getName());
    }
    dto.setSelectedTables(selectedTables);
		dto.setLogicalRelationships(this.generateLogicalRelationships(this.getJoins()));
		return dto;
	}

	public void populateJoinGuiModel(Domain domain, MultiTableDatasourceDTO dto) {

    //existing joinTableModels will not have fields. We can add these from the domain.
    addFieldsToTables(domain, this.availableTables);

		// Populate "selectedTables" from availableTables using
		// logicalRelationships.
		AbstractModelList<JoinTableModel> selectedTablesList = new AbstractModelList<JoinTableModel>();
		List<LogicalRelationship> logicalRelationships = dto.getLogicalRelationships();
//		for (LogicalRelationship logicalRelationship : logicalRelationships) {
//			this.selectTable(logicalRelationship.getFromTable(), selectedTablesList);
//			this.selectTable(logicalRelationship.getToTable(), selectedTablesList);
//		}
    for(String selectedTable : dto.getSelectedTables()){
      this.selectTable(selectedTable, selectedTablesList);
    }
		this.selectedTables.addAll(selectedTablesList);


		// Populate "joins" from availableTables using logicalRelationships.
		AbstractModelList<JoinModel> joinsList = new AbstractModelList<JoinModel>();
		for (LogicalRelationship logicalRelationship : logicalRelationships) {
			this.populateJoin(logicalRelationship, joinsList);
		}
		this.joins.addAll(joinsList);

	}

  private void addFieldsToTables(Domain domain, AbstractModelList<JoinTableModel> availableTables) {

		String locale = LocalizedString.DEFAULT_LOCALE;
    for(JoinTableModel table : availableTables){
      for(LogicalTable tbl : domain.getLogicalModels().get(0).getLogicalTables()){
        if(tbl.getName(locale).equals(table.getName())){
          for(LogicalColumn col : tbl.getLogicalColumns()){
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

  private LogicalTable findTable(Domain domain, String id) {
    return domain.getLogicalModels().get(0).findLogicalTable(id);
  }

  private void populateJoin(LogicalRelationship logicalRelationship, AbstractModelList<JoinModel> joinsList) {

		JoinModel join = new JoinModel();
		String locale = LocalizedString.DEFAULT_LOCALE;

		for (JoinTableModel table : this.selectedTables) {
			if (table.getName().equals(logicalRelationship.getFromTable().getName(locale))) {
				for (JoinFieldModel field : table.getFields()) {
					if (field.getName().equals(logicalRelationship.getFromColumn().getName(locale))) {
						join.setLeftKeyFieldModel(field);
					}
				}
			}

			if (table.getName().equals(logicalRelationship.getToTable().getName(locale))) {
				for (JoinFieldModel field : table.getFields()) {
					if (field.getName().equals(logicalRelationship.getToColumn().getName(locale))) {
						join.setRightKeyFieldModel(field);
					}
				}
			}
		}
		joinsList.add(join);
	}

	private void selectTable(LogicalTable logicalTable, AbstractModelList<JoinTableModel> selectedTablesList) {
		String locale = LocalizedString.DEFAULT_LOCALE;
		for (JoinTableModel table : this.availableTables) {
			if (table.getName().equals(logicalTable.getName(locale))) {
				if (!selectedTablesList.contains(table)) {
					selectedTablesList.add(table);
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
