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

import java.util.Arrays;
import java.util.List;

import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryConnectionServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.CsvDatasourceServiceHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class JoinSelectionDebugGwtServlet extends RemoteServiceServlet implements IGwtJoinSelectionService {
	private static final long serialVersionUID = -6800729673421568704L;
	public static InMemoryConnectionServiceImpl SERVICE;

	static {
		if (!PentahoSystem.getInitializedOK()) {
			CsvDatasourceServiceHelper csvHelper = new CsvDatasourceServiceHelper();
			csvHelper.setUp();
		}
		try {
			KettleEnvironment.init();
			Props.init(Props.TYPE_PROPERTIES_EMPTY);
		} catch (KettleException e) {
			e.printStackTrace();
		}
	}

	public JoinSelectionDebugGwtServlet() {
	}

	private DatabaseMeta getDatabaseMeta(IConnection connection) {
		DatabaseMeta databaseMeta = new DatabaseMeta();
		databaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
		databaseMeta.setDatabaseType(AgileHelper.getDialect(databaseMeta));
		databaseMeta.setDBName(connection.getName());
		databaseMeta.setName(connection.getName());
		return databaseMeta;
	}

	public List<String> getDatabaseTables(IConnection connection) throws Exception {

		DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
		Database database = new Database(null, databaseMeta);
		database.connect();

		String[] tableNames = database.getTablenames();
		List<String> tables = Arrays.asList(tableNames);
		database.disconnect();
		return tables;
	}

	public List<String> getTableFields(String table, IConnection connection) throws Exception {

		DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
		Database database = new Database(null, databaseMeta);
		database.connect();

		String query = databaseMeta.getSQLQueryFields(table);
		database.getRows(query, 1);
		String[] tableFields = database.getReturnRowMeta().getFieldNames();

		List<String> fields = Arrays.asList(tableFields);
		database.disconnect();
		return fields;
	}
}