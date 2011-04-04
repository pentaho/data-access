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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtJoinSelectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;
import org.pentaho.platform.engine.core.system.PentahoBase;

import com.thoughtworks.xstream.XStream;

public class MultitableDatasourceService extends PentahoBase implements IGwtJoinSelectionService {

	private DatabaseMeta getDatabaseMeta(IConnection connection) {
		DatabaseMeta databaseMeta = new DatabaseMeta();
		databaseMeta.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
		databaseMeta.setDBName(connection.getName());
		databaseMeta.setName(connection.getName());
		databaseMeta.setDatabaseType(AgileHelper.getDialect(databaseMeta, connection.getName()));
		databaseMeta.setQuoteAllFields(true);
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

	public IDatasourceSummary serializeJoins(MultiTableDatasourceDTO dto, IConnection connection) throws Exception {

		ModelerService modelerService = new ModelerService();
		modelerService.initKettle();
		
		DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
		MultiTableModelerSource multiTable = new MultiTableModelerSource(databaseMeta, dto.getLogicalRelationships(), dto.getDatasourceName());
		Domain domain = multiTable.generateDomain();
		domain.getLogicalModels().get(0).setProperty("datasourceModel", serializeModelState(dto));
		domain.getLogicalModels().get(0).setProperty("DatasourceType", "MULTI-TABLE-DS");
		modelerService.serializeModels(domain, dto.getDatasourceName(), false);

		QueryDatasourceSummary summary = new QueryDatasourceSummary();
		summary.setDomain(domain);
		return summary;
	}

	private String serializeModelState(MultiTableDatasourceDTO dto) throws DatasourceServiceException {
		XStream xs = new XStream();
		return xs.toXML(dto);
	}

	public MultiTableDatasourceDTO deSerializeModelState(String dtoStr) throws DatasourceServiceException {
		try {
			XStream xs = new XStream();
			return (MultiTableDatasourceDTO) xs.fromXML(dtoStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DatasourceServiceException(e);
		}
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

	public BogoPojo gwtWorkaround(BogoPojo pojo) {
		return pojo;
	}

	@Override
	public Log getLogger() {
		// TODO Auto-generated method stub
		return null;
	}
}
