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

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

public class JoinGuiModel extends XulEventSourceAdapter {

	private AbstractModelList<JoinTableModel> availableTables;
	private AbstractModelList<JoinTableModel> selectedTables;
	private AbstractModelList<JoinModel> joins;
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
}
