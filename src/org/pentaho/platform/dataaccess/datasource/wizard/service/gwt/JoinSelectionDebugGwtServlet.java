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
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.util.DatabaseUtil;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
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
	
	private DatabaseMeta getDatabaseMeta(IConnection connection) throws Exception {
		ConnectionDebugGwtServlet connectionServiceImpl = new ConnectionDebugGwtServlet();
		IDatabaseConnection iDatabaseConnection = connectionServiceImpl.convertFromConnection(connection);
		DatabaseMeta databaseMeta = DatabaseUtil.convertToDatabaseMeta(iDatabaseConnection);
		
		if(connection.getName().equals("SampleData")) {
			databaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
			databaseMeta.setDBName("SampleData");
		}
		return databaseMeta;
	}

	public List<String> getDatabaseTables(IConnection connection, String schema) throws Exception {
		DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
		MultitableDatasourceService service = new MultitableDatasourceService(databaseMeta);
		return service.getDatabaseTables(connection, schema);
	}
	
	public List<String> retrieveSchemas(IConnection connection) throws Exception {
		DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
		MultitableDatasourceService service = new MultitableDatasourceService(databaseMeta);
		return service.retrieveSchemas(connection);
	}	

	public IDatasourceSummary serializeJoins(MultiTableDatasourceDTO dto, IConnection connection) throws Exception {
		DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
		MultitableDatasourceService service = new MultitableDatasourceService(databaseMeta);
		return service.serializeJoins(dto, connection);
	}

	public List<String> getTableFields(String table, IConnection connection) throws Exception {
		DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
		MultitableDatasourceService service = new MultitableDatasourceService(databaseMeta);
		return service.getTableFields(table, connection);
	}
	
	public MultiTableDatasourceDTO deSerializeModelState(String source) throws Exception {
		MultitableDatasourceService service = new MultitableDatasourceService();
		return service.deSerializeModelState(source);
	}

	public BogoPojo gwtWorkaround(BogoPojo pojo) {
		return pojo;
	}
}