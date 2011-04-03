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

package org.pentaho.platform.dataaccess.datasource.wizard.modeler;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalRelationship;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinFieldModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinTableModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.test.platform.engine.core.BaseTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JoinMetadataTest extends BaseTest {

	private static Logger logger = LoggerFactory.getLogger(JoinMetadataTest.class);

	static {
		PentahoSystemHelper.init();
		try {
			KettleEnvironment.init();
			Props.init(Props.TYPE_PROPERTIES_EMPTY);
		} catch (KettleException e) {
			e.printStackTrace();
		}
	}

	public void testGenerateDomain() {

		try {
			JoinGuiModel joinGuiModel = new JoinGuiModel();
			List<LogicalRelationship> logicalRelationships = joinGuiModel.generateLogicalRelationships(getJoinModel());
			MultiTableModelerSource multiTable = new MultiTableModelerSource(this.getDatabaseMeta(), logicalRelationships, this.getDatabaseMeta().getName());
			Domain domain = multiTable.generateDomain();
			assertNotNull(domain);
		} catch (ModelerException e) {
			e.printStackTrace();
			logger.info(e.getLocalizedMessage());
		}
	}

	private List<JoinModel> getJoinModel() {
		List<JoinModel> joins = new ArrayList<JoinModel>();

		JoinTableModel joinTable1 = new JoinTableModel();
		joinTable1.setName("CUSTOMERS");

		JoinTableModel joinTable2 = new JoinTableModel();
		joinTable2.setName("PRODUCTS");

		JoinModel join1 = new JoinModel();
		JoinFieldModel lField1 = new JoinFieldModel();
		lField1.setName("CUSTOMERNAME");
		lField1.setParentTable(joinTable1);
		join1.setLeftKeyFieldModel(lField1);

		JoinFieldModel rField1 = new JoinFieldModel();
		rField1.setName("PRODUCTCODE");
		rField1.setParentTable(joinTable2);
		join1.setRightKeyFieldModel(rField1);

		joins.add(join1);
		return joins;
	}

	private DatabaseMeta getDatabaseMeta() {
		DatabaseMeta database = new DatabaseMeta();
		database.setDatabaseType("Hypersonic");
		database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
		database.setDBName("SampleData");
		database.setName("SampleData");
		return database;
	}
}
