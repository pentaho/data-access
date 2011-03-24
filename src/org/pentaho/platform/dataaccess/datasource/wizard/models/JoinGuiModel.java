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

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

public class JoinGuiModel extends XulEventSourceAdapter {

	private AbstractModelList<JoinTableModel> availableTables;
	private AbstractModelList<JoinTableModel> selectedTables;
	private JoinTableModel leftJoinTable;
	private JoinTableModel rightJoinTable;
	private JoinFieldModel leftJoinField;
	private JoinFieldModel rightJoinField;

	public JoinGuiModel() {
		this.availableTables = new AbstractModelList<JoinTableModel>();
		this.selectedTables = new AbstractModelList<JoinTableModel>();
		this.leftJoinTable = new JoinTableModel();
		this.rightJoinTable = new JoinTableModel();
	}

	@Bindable
	public AbstractModelList<JoinTableModel> getAvailableTables() {
		return this.availableTables;
	}

	@Bindable
	public void setAvailableTables(AbstractModelList<JoinTableModel> availableTables) {
		this.availableTables = availableTables;
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
	public JoinFieldModel getLeftJoinField() {
		return this.leftJoinField;
	}

	@Bindable
	public void setLeftJoinField(JoinFieldModel leftJoinField) {
		this.leftJoinField = leftJoinField;
	}

	@Bindable
	public JoinFieldModel getRightJoinField() {
		return this.rightJoinField;
	}

	@Bindable
	public void setRightJoinField(JoinFieldModel rightJoinField) {
		this.rightJoinField = rightJoinField;
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
}
