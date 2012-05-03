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
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;

import com.google.gwt.user.client.rpc.RemoteService;

public interface IGwtJoinSelectionService extends RemoteService {

	List<String> getDatabaseTables(Connection connection, String schema) throws Exception;
	
	List<String> retrieveSchemas(Connection connection) throws Exception;

	List<String> getTableFields(String table, Connection connection) throws Exception;

	IDatasourceSummary serializeJoins(MultiTableDatasourceDTO dto, Connection connection) throws Exception;

	MultiTableDatasourceDTO deSerializeModelState(String source) throws Exception;

	BogoPojo gwtWorkaround(BogoPojo pojo);
}
