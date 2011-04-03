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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.Serializable;
import java.util.List;

import org.pentaho.metadata.model.LogicalRelationship;

public class MultiTableDatasourceDTO implements Serializable {

	private static final long serialVersionUID = 1368165523678535182L;

	private String datasourceName;
	private String datasourceId;
	private List<LogicalRelationship> logicalRelationships;

	public MultiTableDatasourceDTO() {
	}

	public String getDatasourceName() {
		return datasourceName;
	}

	public void setDatasourceName(String datasourceName) {
		this.datasourceName = datasourceName;
	}

	public List<LogicalRelationship> getLogicalRelationships() {
		return logicalRelationships;
	}

	public void setLogicalRelationships(List<LogicalRelationship> logicalRelationships) {
		this.logicalRelationships = logicalRelationships;
	}

	public String getDatasourceId() {
		return datasourceId;
	}

	public void setDatasourceId(String datasourceId) {
		this.datasourceId = datasourceId;
	}
}
