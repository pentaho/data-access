package org.pentaho.platform.dataaccess.datasource.ui.importing;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class ImportGuiStateModel extends XulEventSourceAdapter {

	private List<Connection> connectionList;

	public ImportGuiStateModel() {
		connectionList = new ArrayList<Connection>();
	}

	@Bindable
	public List<Connection> getConnectionList() {
		return connectionList;
	}

	@Bindable
	public void setConnectionList(List<Connection> value) {
		List<Connection> previousValue = connectionList;
		this.connectionList = value;
		this.firePropertyChange("connectionList", previousValue, value); //$NON-NLS-1$
	}
}
