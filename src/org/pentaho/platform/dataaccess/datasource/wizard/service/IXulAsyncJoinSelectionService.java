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

package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncJoinSelectionService {

	void getDatabaseTables(IConnection connection, XulServiceCallback<List> callback);

	void getTableFields(String table, IConnection connection, XulServiceCallback<List> callback);

	void serializeJoins(List<LogicalRelationship> joins, IConnection connection, XulServiceCallback<Void> callback);

	void gwtWorkaround(BogoPojo pojo, XulServiceCallback<BogoPojo> callback);
}
