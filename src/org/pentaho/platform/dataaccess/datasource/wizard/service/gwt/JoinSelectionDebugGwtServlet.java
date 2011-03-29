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
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultitableDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class JoinSelectionDebugGwtServlet extends RemoteServiceServlet implements IGwtJoinSelectionService {
	private static final long serialVersionUID = -6800729673421568704L;

	static {
		PentahoSystemHelper.init();
		try {
			KettleEnvironment.init();
			Props.init(Props.TYPE_PROPERTIES_EMPTY);
		} catch (KettleException e) {
			e.printStackTrace();
		}
	}

	public List<String> getDatabaseTables(IConnection connection) throws Exception {
		MultitableDatasourceService service = new MultitableDatasourceService();
		return service.getDatabaseTables(connection);
	}

	public void serializeJoins(List<LogicalRelationship> joins, IConnection connection) throws Exception {
		MultitableDatasourceService service = new MultitableDatasourceService();
		service.serializeJoins(joins, connection);
	}

	public List<String> getTableFields(String table, IConnection connection) throws Exception {
		MultitableDatasourceService service = new MultitableDatasourceService();
		return service.getTableFields(table, connection);
	}

	public BogoPojo gwtWorkaround(BogoPojo pojo) {
		return pojo;
	}
}