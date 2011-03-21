package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

public class JoinSelectionStepModel extends XulEventSourceAdapter {

	private AbstractModelList<JoinTableModel> availableTables;
	private AbstractModelList<JoinTableModel> selectedTables;

	public JoinSelectionStepModel() {
		this.availableTables = new AbstractModelList<JoinTableModel>();
		this.selectedTables = new AbstractModelList<JoinTableModel>();
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
