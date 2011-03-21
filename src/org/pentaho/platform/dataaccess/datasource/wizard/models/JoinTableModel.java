package org.pentaho.platform.dataaccess.datasource.wizard.models;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class JoinTableModel extends XulEventSourceAdapter {

	private String name;

	@Bindable
	public String getName() {
		return this.name;
	}

	@Bindable
	public void setName(String name) {
		this.name = name;
	}
}
