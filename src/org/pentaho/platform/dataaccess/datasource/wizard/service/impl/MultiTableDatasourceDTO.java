/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.Serializable;
import java.util.List;

import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.database.model.IDatabaseConnection;

public class MultiTableDatasourceDTO implements Serializable {

	private static final long serialVersionUID = 1368165523678535182L;

	private String datasourceName;
	private IDatabaseConnection selectedConnection;
	private SchemaModel schemaModel;
	private List<String> selectedTables;
	private boolean doOlap;

	public MultiTableDatasourceDTO() {
	}

	public String getDatasourceName() {
		return datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	public SchemaModel getSchemaModel() {
		return schemaModel;
	}

	public void setSchemaModel(SchemaModel schemaModel) {
		this.schemaModel = schemaModel;
	}

	public IDatabaseConnection getSelectedConnection() {
		return selectedConnection;
	}

	public void setSelectedConnection(IDatabaseConnection selectedConnection) {
		this.selectedConnection = selectedConnection;
	}

	public List<String> getSelectedTables() {
		return selectedTables;
	}

	public void setSelectedTables(List<String> selectedTables) {
		this.selectedTables = selectedTables;
	}

	public boolean isDoOlap() {
		return (getSchemaModel() != null && getSchemaModel().getFactTable() != null);
	}

	public void setDoOlap(boolean doOlap) {
		this.doOlap = doOlap;
	}
}
