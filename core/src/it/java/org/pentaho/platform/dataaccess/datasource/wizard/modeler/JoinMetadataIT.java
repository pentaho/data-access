/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.modeler;

import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinMetadataIT extends BaseTest {

	static {
		PentahoSystemHelper.init();
		try {
			KettleEnvironment.init();
			Props.init(Props.TYPE_PROPERTIES_EMPTY);
		} catch (Exception e) {
			//May be already initialized by another test
		}
		
	    if(ModelerMessagesHolder.getMessages() == null){
	  	   ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
	  	}
	}

	public void testGenerateDomain() {
		
		Domain domain = null;
		try {
			MultiTableModelerSource multiTable = new MultiTableModelerSource(this.getDatabaseMeta(), getSchemaModel(), this.getDatabaseMeta().getName(), Arrays.asList("CUSTOMERS", "PRODUCTS", "CUSTOMERNAME", "PRODUCTCODE"));
			domain = multiTable.generateDomain();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(domain);

	}
  private SchemaModel getSchemaModel() {
    List<JoinRelationshipModel> joins = new ArrayList<JoinRelationshipModel>();

    JoinTableModel joinTable1 = new JoinTableModel();
    joinTable1.setName("CUSTOMERS");

    JoinTableModel joinTable2 = new JoinTableModel();
    joinTable2.setName("PRODUCTS");

    JoinRelationshipModel join1 = new JoinRelationshipModel();
    JoinFieldModel lField1 = new JoinFieldModel();
    lField1.setName("CUSTOMERNAME");
    lField1.setParentTable(joinTable1);
    join1.setLeftKeyFieldModel(lField1);

    JoinFieldModel rField1 = new JoinFieldModel();
    rField1.setName("PRODUCTCODE");
    rField1.setParentTable(joinTable2);
    join1.setRightKeyFieldModel(rField1);

    joins.add(join1);
    SchemaModel model = new SchemaModel();
    model.setJoins(joins);
    return model;
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
