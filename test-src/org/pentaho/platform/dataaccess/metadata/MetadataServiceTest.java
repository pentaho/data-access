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
 * Copyright (c) 2011 Pentaho Corporation.  All rights reserved.
 * 
 * Created Jan, 2011
 * @author jdixon
 */
package org.pentaho.platform.dataaccess.metadata;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.query.model.CombinationType;
import org.pentaho.metadata.query.model.util.QueryXmlHelper;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.data.IDatasourceService;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.ICacheManager;
import org.pentaho.platform.api.engine.IPentahoDefinableObjectFactory.Scope;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.util.IPasswordService;
import org.pentaho.platform.dataaccess.metadata.messages.Messages;
import org.pentaho.platform.dataaccess.metadata.model.IColumn;
import org.pentaho.platform.dataaccess.metadata.model.Operator;
import org.pentaho.platform.dataaccess.metadata.model.impl.Category;
import org.pentaho.platform.dataaccess.metadata.model.impl.Column;
import org.pentaho.platform.dataaccess.metadata.model.impl.Condition;
import org.pentaho.platform.dataaccess.metadata.model.impl.Model;
import org.pentaho.platform.dataaccess.metadata.model.impl.ModelInfo;
import org.pentaho.platform.dataaccess.metadata.model.impl.Order;
import org.pentaho.platform.dataaccess.metadata.model.impl.Parameter;
import org.pentaho.platform.dataaccess.metadata.model.impl.Query;
import org.pentaho.platform.dataaccess.metadata.service.MetadataService;
import org.pentaho.platform.dataaccess.metadata.service.MetadataServiceUtil;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.SystemSettings;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.pluginmgr.PluginResourceLoader;
import org.pentaho.platform.repository2.unified.JackrabbitRepositoryTestBase;
import org.pentaho.platform.repository2.unified.fs.FileSystemBackedUnifiedRepository;
import org.pentaho.platform.repository2.unified.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.util.Base64PasswordService;
import org.pentaho.pms.core.exception.PentahoMetadataException;
import org.pentaho.test.platform.engine.core.MicroPlatform;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/repository.spring-ext.xml", "classpath:/repository-test-override.spring-ext.xml" })
@SuppressWarnings({ "all" })
public class MetadataServiceTest extends JackrabbitRepositoryTestBase implements ApplicationContextAware {

	private MicroPlatform booter;

	private IUnifiedRepository repo;

	private boolean startupCalled;

	public MetadataServiceTest() {
		super();
	}

	@Test
	public void testPermissions() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		String perms = svc.getDatasourcePermissions();
		Assert.assertEquals("NONE", perms);

		// session.setAuthenticated("suzy");
		perms = svc.getDatasourcePermissions();
		Assert.assertEquals("NONE", perms);

	}

	@Test
	public void testMessages() {
		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		Assert.assertEquals("!bogus!", Messages.getString("bogus"));
		Assert.assertEquals("test message", Messages.getString("TEST.MESSAGE1"));
		Assert.assertEquals("test message one", Messages.getString("TEST.MESSAGE2", "one"));
		Assert.assertEquals("test message one two", Messages.getString("TEST.MESSAGE3", "one", "two"));
		Assert.assertEquals("test message one two three", Messages.getString("TEST.MESSAGE4", "one", "two", "three"));
		Assert.assertEquals("test message one two three four", Messages.getString("TEST.MESSAGE5", "one", "two", "three", "four"));

		Assert.assertEquals("TEST.MESSAGE1 - test message", Messages.getErrorString("TEST.MESSAGE1"));
		Assert.assertEquals("TEST.MESSAGE2 - test message one", Messages.getErrorString("TEST.MESSAGE2", "one"));
		Assert.assertEquals("TEST.MESSAGE3 - test message one two", Messages.getErrorString("TEST.MESSAGE3", "one", "two"));
		Assert.assertEquals("TEST.MESSAGE4 - test message one two three", Messages.getErrorString("TEST.MESSAGE4", "one", "two", "three"));

		new Messages();
	}

	@Test
	public void testCondition2() throws KettleException {
		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		Query query = new Query();
		query.setDomainName("steel-wheels");
		query.setModelId("BV_ORDERS");
		query.setDisableDistinct(Boolean.FALSE);
		List<Column> cols = new ArrayList<Column>();
		Column col = new Column();
		col.setId("BC_CUSTOMER_W_TER_COUNTRY");
		col.setCategory("BC_CUSTOMER_W_TER_");
		col.setSelectedAggType("NONE");
		cols.add(col);
		query.setColumns(cols.toArray(new Column[cols.size()]));

		List<Condition> conditions = new ArrayList<Condition>();
		Condition condition = new Condition();
		condition.setColumn("BC_CUSTOMER_W_TER_COUNTRY");
		condition.setCategory("BC_CUSTOMER_W_TER_");
		condition.setOperator(Operator.EQUAL.name());
		condition.setValue(new String[] { "Australia" });
		conditions.add(condition);
		query.setConditions(conditions.toArray(new Condition[conditions.size()]));

		List<Order> orders = new ArrayList<Order>();
		Order order = new Order();
		order.setColumn("BC_CUSTOMER_W_TER_COUNTRY");
		order.setCategory("BC_CUSTOMER_W_TER_");
		order.setOrderType("ASC");
		orders.add(order);
		query.setOrders(orders.toArray(new Order[orders.size()]));

		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.deepSerialize(query);
		System.out.println(json);

		MarshallableResultSet results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 1, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);

		// TODO get BEGIN_WITH and other operators working

		condition.setValue(new String[] { "B" });
		condition.setOperator("<");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 2, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Austria", results.getRows()[1].getCell()[0]);

		condition.setValue(new String[] { "Belgium" });
		condition.setOperator("<=");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 3, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Austria", results.getRows()[1].getCell()[0]);
		Assert.assertEquals("wrong value", "Belgium", results.getRows()[2].getCell()[0]);

		condition.setValue(new String[] { "Switzerland" });
		condition.setOperator(">");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 2, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "UK", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "USA", results.getRows()[1].getCell()[0]);

		condition.setValue(new String[] { "Switzerland" });
		condition.setOperator(">=");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 3, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Switzerland", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "UK", results.getRows()[1].getCell()[0]);
		Assert.assertEquals("wrong value", "USA", results.getRows()[2].getCell()[0]);

		condition.setValue(new String[] { "Switzerland" });
		condition.setOperator("exactly matches");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 1, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Switzerland", results.getRows()[0].getCell()[0]);

		condition.setValue(new String[] { "Switzerland", "Austria" });
		condition.setOperator("exactly matches");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 2, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Austria", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Switzerland", results.getRows()[1].getCell()[0]);

		condition.setValue(new String[] { "Switzerland" });
		condition.setOperator("exactly matches");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 1, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Switzerland", results.getRows()[0].getCell()[0]);

		condition.setValue(new String[] { "land" });
		condition.setOperator("contains");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 6, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Finland", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Ireland", results.getRows()[1].getCell()[0]);
		Assert.assertEquals("wrong value", "Netherlands", results.getRows()[2].getCell()[0]);
		Assert.assertEquals("wrong value", "New Zealand", results.getRows()[3].getCell()[0]);
		Assert.assertEquals("wrong value", "Poland", results.getRows()[4].getCell()[0]);
		Assert.assertEquals("wrong value", "Switzerland", results.getRows()[5].getCell()[0]);

		condition.setValue(new String[] { "land" });
		condition.setOperator("CONTAINS");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 6, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Finland", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Ireland", results.getRows()[1].getCell()[0]);
		Assert.assertEquals("wrong value", "Netherlands", results.getRows()[2].getCell()[0]);
		Assert.assertEquals("wrong value", "New Zealand", results.getRows()[3].getCell()[0]);
		Assert.assertEquals("wrong value", "Poland", results.getRows()[4].getCell()[0]);
		Assert.assertEquals("wrong value", "Switzerland", results.getRows()[5].getCell()[0]);

		condition.setValue(new String[] { "a" });
		condition.setOperator("does not contain");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 6, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Belgium", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Hong Kong", results.getRows()[1].getCell()[0]);
		Assert.assertEquals("wrong value", "Philippines", results.getRows()[2].getCell()[0]);
		Assert.assertEquals("wrong value", "Sweden", results.getRows()[3].getCell()[0]);
		Assert.assertEquals("wrong value", "UK", results.getRows()[4].getCell()[0]);
		Assert.assertEquals("wrong value", "USA", results.getRows()[5].getCell()[0]);

		condition.setValue(new String[] { "A" });
		condition.setOperator("begins with");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 2, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Austria", results.getRows()[1].getCell()[0]);

		condition.setValue(new String[] { "A" });
		condition.setOperator("BEGINSWITH");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 2, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Austria", results.getRows()[1].getCell()[0]);

		condition.setValue(new String[] { "ia" });
		condition.setOperator("ends with");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 3, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Austria", results.getRows()[1].getCell()[0]);
		Assert.assertEquals("wrong value", "Russia", results.getRows()[2].getCell()[0]);

		condition.setValue(new String[] { "ia" });
		condition.setOperator("ENDSWITH");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 3, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 1, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong value", "Austria", results.getRows()[1].getCell()[0]);
		Assert.assertEquals("wrong value", "Russia", results.getRows()[2].getCell()[0]);

		condition.setValue(new String[] { "" });
		condition.setOperator("is null");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 0, results.getRows().length);

		condition.setValue(new String[] { "" });
		condition.setOperator("ISNA");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 0, results.getRows().length);

		condition.setValue(new String[] { "" });
		condition.setOperator("is not null");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 27, results.getRows().length);

		condition.setValue(new String[] { "Australia" });
		condition.setOperator(null);
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 1, results.getRows().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);

		condition.setValue(new String[] { "Australia" });
		condition.setOperator("=");
		results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 1, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_CUSTOMER_W_TER_COUNTRY", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 1, results.getRows().length);
		Assert.assertEquals("wrong value", "Australia", results.getRows()[0].getCell()[0]);

	}

	@Test
	public void testModelEquality() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		Model model = svc.loadModel("steel-wheels", "BV_HUMAN_RESOURCES");
		Assert.assertNotNull("model should not be null", model);

		Assert.assertTrue(model.equals(model));
		Assert.assertFalse(model.equals(null));
		Assert.assertFalse(model.equals(this));
		Assert.assertFalse(model.equals(svc.loadModel("steel-wheels", "BV_ORDERS")));

		Model model2 = new Model();
		Model model3 = new Model();

		Assert.assertTrue(model2.equals(model3));
		model2.setCategories(new Category[] { new Category() });
		Assert.assertFalse(model2.equals(model3));
		model3.setCategories(new Category[] { new Category() });
		Assert.assertFalse(model2.equals(model3));
		model2.setCategories(null);
		Assert.assertFalse(model2.equals(model3));
		model3.setCategories(null);
		Assert.assertTrue(model2.equals(model3));
		model3.setId("id");
		Assert.assertFalse(model2.equals(model3));
		model2.setId("not id");
		Assert.assertFalse(model2.equals(model3));
		model2.setId("id");
		Assert.assertTrue(model2.equals(model3));
		model3.setName("name");
		Assert.assertFalse(model2.equals(model3));
		model2.setName("not name");
		Assert.assertFalse(model2.equals(model3));
		model2.setName("name");
		Assert.assertTrue(model2.equals(model3));

	}

	@Test
	public void testDomain() throws KettleException, PentahoMetadataException {
		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>steel-wheels</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints><constraint><operator>AND</operator><condition><![CDATA[[BC_CUSTOMER_W_TER_.BC_CUSTOMER_W_TER_COUNTRY] = \"Australia\"]]></condition></constraint></constraints><orders><order><direction>ASC</direction><view_id>CAT_ORDERS</view_id><column_id>BC_ORDERDETAILS_QUANTITYORDERED</column_id></order></orders></mql>";

		MetadataServiceUtil util = new MetadataServiceUtil();
		Domain domain = util.getDomainObject(queryString);
		Assert.assertNotNull(domain);
		util.setDomain(domain);
		Assert.assertEquals(domain, util.getDomain());
	}

	@Test
	public void testQuery() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		Query query = getTestQuery();

		MarshallableResultSet results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 6, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_PRODUCTS_PRODUCTLINE", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 82, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 6, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Planes", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong number of column header sets", 0, results.getNumColumnHeaderSets());
		Assert.assertEquals("wrong number of row header sets", 0, results.getNumRowHeaderSets());

	}

	@Test
	public void testQuery2() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		Query query = getTestQuery();
		// remove the condition value so that the default value it used
		query.getConditions()[0].setValue(new String[] { "Canada" });

		MarshallableResultSet results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 6, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_PRODUCTS_PRODUCTLINE", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 48, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 6, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Ships", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong number of column header sets", 0, results.getNumColumnHeaderSets());
		Assert.assertEquals("wrong number of row header sets", 0, results.getNumRowHeaderSets());

	}

	@Test
	public void testQuery3() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		Query query = getTestQuery();
		// remove the condition value so that the default value it used
		Parameter param = new Parameter();
		param.setDefaultValue(new String[] { "Canada" });
		param.setColumn("BC_CUSTOMER_W_TER_COUNTRY");
		param.setValue(new String[] { "Germany" });
		query.setParameters(new Parameter[] { param });

		MetadataServiceUtil util = new MetadataServiceUtil();
		org.pentaho.metadata.query.model.Query fullQuery = util.convertQuery(query);
		QueryXmlHelper helper = new QueryXmlHelper();
		String xml = helper.toXML(fullQuery);
		// System.out.println(xml);
		MarshallableResultSet results = svc.doQuery(query, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 6, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_PRODUCTS_PRODUCTLINE", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 45, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 6, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Planes", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong number of column header sets", 0, results.getNumColumnHeaderSets());
		Assert.assertEquals("wrong number of row header sets", 0, results.getNumRowHeaderSets());

	}

	@Test
	public void testJsonQuery() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		Query query = getTestQuery();
		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.deepSerialize(query);

		System.out.println(json);
		MarshallableResultSet results = svc.doJsonQuery(json, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 6, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_PRODUCTS_PRODUCTLINE", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 82, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 6, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Planes", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong number of column header sets", 0, results.getNumColumnHeaderSets());
		Assert.assertEquals("wrong number of row header sets", 0, results.getNumRowHeaderSets());

	}

	private Query getTestQuery() {
		Query query = new Query();
		query.setDomainName("steel-wheels");
		query.setModelId("BV_ORDERS");
		query.setDisableDistinct(Boolean.FALSE);
		List<Column> cols = new ArrayList<Column>();
		Column col = new Column();
		col.setId("BC_PRODUCTS_PRODUCTLINE");
		col.setCategory("CAT_PRODUCTS");
		col.setSelectedAggType("NONE");
		cols.add(col);
		col = new Column();
		col.setId("BC_CUSTOMER_W_TER_COUNTRY");
		col.setCategory("BC_CUSTOMER_W_TER_");
		col.setSelectedAggType("NONE");
		cols.add(col);
		col = new Column();
		col.setId("BC_PRODUCTS_PRODUCTNAME");
		col.setCategory("CAT_PRODUCTS");
		col.setSelectedAggType("NONE");
		cols.add(col);
		col = new Column();
		col.setId("BC_PRODUCTS_PRODUCTCODE");
		col.setCategory("CAT_PRODUCTS");
		col.setSelectedAggType("NONE");
		cols.add(col);
		col = new Column();
		col.setId("BC_ORDERDETAILS_QUANTITYORDERED");
		col.setCategory("CAT_ORDERS");
		col.setSelectedAggType("SUM");
		cols.add(col);
		col = new Column();
		col.setId("BC_ORDERDETAILS_TOTAL");
		col.setCategory("CAT_ORDERS");
		col.setSelectedAggType("SUM");
		cols.add(col);
		query.setColumns(cols.toArray(new Column[cols.size()]));

		List<Condition> conditions = new ArrayList<Condition>();
		Condition condition = new Condition();
		condition.setColumn("BC_CUSTOMER_W_TER_COUNTRY");
		condition.setCategory("BC_CUSTOMER_W_TER_");
		condition.setOperator(Operator.EQUAL.name());
		condition.setValue(new String[] { "Australia" });
		conditions.add(condition);
		query.setConditions(conditions.toArray(new Condition[conditions.size()]));

		List<Order> orders = new ArrayList<Order>();
		Order order = new Order();
		order.setColumn("BC_ORDERDETAILS_QUANTITYORDERED");
		order.setCategory("CAT_ORDERS");
		order.setOrderType("ASC");
		orders.add(order);
		query.setOrders(orders.toArray(new Order[orders.size()]));

		return query;
	}

	@Test
	public void testXmlQuery1() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>steel-wheels</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints><constraint><operator>AND</operator><condition><![CDATA[[BC_CUSTOMER_W_TER_.BC_CUSTOMER_W_TER_COUNTRY] = \"Australia\"]]></condition></constraint></constraints><orders><order><direction>ASC</direction><view_id>CAT_ORDERS</view_id><column_id>BC_ORDERDETAILS_QUANTITYORDERED</column_id></order></orders></mql>";
		MarshallableResultSet results = svc.doXmlQuery(queryString, -1);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of column names", 5, results.getColumnNames().getColumnName().length);
		Assert.assertEquals("wrong column name", "BC_PRODUCTS_PRODUCTLINE", results.getColumnNames().getColumnName()[0]);
		Assert.assertEquals("wrong column type", "string", results.getColumnTypes().getColumnType()[0]);
		Assert.assertEquals("wrong number of rows", 82, results.getRows().length);
		Assert.assertEquals("wrong number of columns", 5, results.getRows()[0].getCell().length);
		Assert.assertEquals("wrong value", "Planes", results.getRows()[0].getCell()[0]);
		Assert.assertEquals("wrong number of column header sets", 0, results.getNumColumnHeaderSets());
		Assert.assertEquals("wrong number of row header sets", 0, results.getNumRowHeaderSets());

	}

	@Test
	public void testXmlQuery2() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>steel-wheels</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints><constraint><operator>AND</operator><condition><![CDATA[[BC_CUSTOMER_W_TER_.BC_CUSTOMER_W_TER_COUNTRY] = \"Australia\"]]></condition></constraint></constraints><orders><order><direction>ASC</direction><view_id>CAT_ORDERS</view_id><column_id>BC_ORDERDETAILS_QUANTITYORDERED</column_id></order></orders></mql>";
		MarshallableResultSet results = svc.doXmlQuery(queryString, 10);
		Assert.assertNotNull("results are null", results);
		Assert.assertEquals("wrong number of rows", 10, results.getRows().length);
	}

	@Test
	public void testXmlQuery3() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>bogud/metadata.xmi</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints><constraint><operator>AND</operator><condition><![CDATA[[BC_CUSTOMER_W_TER_.BC_CUSTOMER_W_TER_COUNTRY] = \"Australia\"]]></condition></constraint></constraints><orders><order><direction>ASC</direction><view_id>CAT_ORDERS</view_id><column_id>BC_ORDERDETAILS_QUANTITYORDERED</column_id></order></orders></mql>";
		MarshallableResultSet results = svc.doXmlQuery(queryString, 10);
		Assert.assertNull("results are not null", results);
	}

	@Test
	public void testXmlQueryToJson1() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>steel-wheels</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints/><orders/></mql>";
		String json = svc.doXmlQueryToJson(queryString, -1);
		Assert.assertNotNull("results are null", json);
		// System.out.println(json);
		Assert.assertTrue("wrong column name", json.indexOf("BC_PRODUCTS_PRODUCTLINE") != -1);
		Assert.assertTrue("wrong column type", json.indexOf("\"string\"") != -1);
		Assert.assertTrue("wrong value", json.indexOf("Classic Cars") != -1);
	}

	@Test
	public void testJsonQueryToJson1() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		Query query = getTestQuery();
		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.deepSerialize(query);

		json = svc.doJsonQueryToJson(json, -1);

		Assert.assertNotNull("results are null", json);
		// System.out.println(json);
		Assert.assertTrue("wrong column name", json.indexOf("BC_PRODUCTS_PRODUCTLINE") != -1);
		Assert.assertTrue("wrong column type", json.indexOf("\"string\"") != -1);
		Assert.assertTrue("wrong value", json.indexOf("Classic Cars") != -1);
	}

	@Test
	public void testJsonQueryToJson2() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String json = "{\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Query\",\"columns\":[{\"aggTypes\":[],\"category\":\"BC_CUSTOMER_W_TER_\",\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Column\",\"defaultAggType\":null,\"fieldType\":null,\"id\":\"BC_CUSTOMER_W_TER_COUNTRY\",\"name\":null,\"selectedAggType\":\"NONE\",\"type\":null}],\"conditions\":[{\"category\":\"BC_CUSTOMER_W_TER_\",\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Condition\",\"column\":\"BC_CUSTOMER_W_TER_COUNTRY\",\"combinationType\":\"AND\",\"operator\":\"EQUAL\",\"value\":[\"Australia\"]}],\"defaultParameterMap\":null,\"disableDistinct\":false,\"domainName\":\"steel-wheels\",\"modelId\":\"BV_ORDERS\",\"orders\":[{\"category\":\"BC_CUSTOMER_W_TER_\",\"class\":\"org.pentaho.platform.dataaccess.metadata.model.impl.Order\",\"column\":\"BC_CUSTOMER_W_TER_COUNTRY\",\"orderType\":\"ASC\"}],\"parameters\":[]}";
		json = svc.doJsonQueryToJson(json, -1);

		Assert.assertNotNull("results are null", json);
		// System.out.println(json);
		Assert.assertTrue("wrong column name", json.indexOf("BC_CUSTOMER_W_TER_COUNTRY") != -1);
		Assert.assertTrue("wrong column type", json.indexOf("\"string\"") != -1);
		Assert.assertTrue("wrong value", json.indexOf("Australia") != -1);
	}

	@Test
	public void testXmlQueryJson2() throws KettleException, JSONException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>bogus/metadata.xmi</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints/><orders/></mql>";
		String json = svc.doXmlQueryToJson(queryString, -1);
		Assert.assertNull("results are not null", json);

		MetadataServiceUtil util = new MetadataServiceUtil();
		Assert.assertNull("results are not null", util.createCdaJson(null, null));

	}

	@Test
	public void testXmlQueryToCdaJson1() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>steel-wheels</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints/><orders/></mql>";
		String json = svc.doXmlQueryToCdaJson(queryString, -1);
		Assert.assertNotNull("results are null", json);
		System.out.println(json);
		Assert.assertTrue("wrong column name", json.indexOf("BC_PRODUCTS_PRODUCTLINE") != -1);
		Assert.assertTrue("wrong column type", json.indexOf("\"STRING\"") != -1);
		Assert.assertTrue("wrong value", json.indexOf("Classic Cars") != -1);
	}

	@Test
	public void testXmlQueryToCdaJson2() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		String queryString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><mql><domain_id>bogus/metadata.xmi</domain_id><model_id>BV_ORDERS</model_id><options><disable_distinct>false</disable_distinct></options><selections><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTLINE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTNAME</column><aggregation>NONE</aggregation></selection><selection><view>CAT_PRODUCTS</view><column>BC_PRODUCTS_PRODUCTCODE</column><aggregation>NONE</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_QUANTITYORDERED</column><aggregation>SUM</aggregation></selection><selection><view>CAT_ORDERS</view><column>BC_ORDERDETAILS_TOTAL</column><aggregation>SUM</aggregation></selection></selections><constraints/><orders/></mql>";
		String json = svc.doXmlQueryToCdaJson(queryString, -1);
		Assert.assertNull("results are not null", json);
	}

	@Test
	public void testJsonQueryToCdaJson1() throws KettleException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
		MetadataService svc = new MetadataService();

		Query query = getTestQuery();
		JSONSerializer serializer = new JSONSerializer();
		String json = serializer.deepSerialize(query);

		json = svc.doJsonQueryToCdaJson(json, -1);

		Assert.assertNotNull("results are null", json);
		System.out.println(json);
		Assert.assertTrue("wrong column name", json.indexOf("BC_PRODUCTS_PRODUCTLINE") != -1);
		Assert.assertTrue("wrong column type", json.indexOf("\"STRING\"") != -1);
		Assert.assertTrue("wrong value", json.indexOf("Classic Cars") != -1);
	}

	@Test
	public void testGetModel1() {
		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		Model model = svc.loadModel("steel-wheels", "BV_ORDERS");
		Assert.assertNotNull("model should not be null", model);

		Assert.assertEquals("domain id is wrong", "steel-wheels", model.getDomainId());
		Assert.assertEquals("model id is wrong", "BV_ORDERS", model.getId());
		Assert.assertEquals("model name is wrong", "Orders", model.getName());
		Assert.assertEquals("model description is wrong", "This model contains information about customers and their orders.", model.getDescription());
		Assert.assertTrue("model hash is wrong", model.hashCode() != 0);

		Assert.assertEquals("wrong number of categories", 4, model.getCategories().length);

		Category category = model.getCategories()[0];
		Assert.assertEquals("wrong number of business columns", 13, category.getColumns().length);
		Assert.assertEquals("category id is wrong", "BC_CUSTOMER_W_TER_", category.getId());
		Assert.assertEquals("category name is wrong", "Customer", category.getName());

		IColumn column = category.getColumns()[0];
		Assert.assertEquals("column default agg type is wrong", "NONE", column.getDefaultAggType().toString());
		Assert.assertEquals("column id is wrong", "BC_CUSTOMER_W_TER_TERRITORY", column.getId());
		Assert.assertEquals("column name is wrong", "Territory", column.getName());
		Assert.assertEquals("column selected agg type is wrong", "NONE", column.getSelectedAggType().toString());
		Assert.assertEquals("column type is wrong", "STRING", column.getType().toString());
		Assert.assertEquals("field type is wrong", "DIMENSION", column.getFieldType().toString());
		Assert.assertEquals("mask is wrong", null, column.getFormatMask());
		Assert.assertEquals("alignment is wrong", "LEFT", column.getHorizontalAlignment().toString());
		Assert.assertEquals("column agg types list is wrong size", 1, column.getAggTypes().length);

		category = model.getCategories()[1];
		Assert.assertEquals("wrong number of business columns", 9, category.getColumns().length);
		Assert.assertEquals("category id is wrong", "CAT_ORDERS", category.getId());
		Assert.assertEquals("category name is wrong", "Orders", category.getName());

		column = category.getColumns()[6];
		Assert.assertEquals("column default agg type is wrong", "SUM", column.getDefaultAggType().toString());
		Assert.assertEquals("column id is wrong", "BC_ORDERDETAILS_QUANTITYORDERED", column.getId());
		Assert.assertEquals("column name is wrong", "Quantity Ordered", column.getName());
		Assert.assertEquals("column selected agg type is wrong", "SUM", column.getSelectedAggType().toString());
		Assert.assertEquals("column type is wrong", "NUMERIC", column.getType().toString());
		Assert.assertEquals("field type is wrong", "FACT", column.getFieldType().toString());
		Assert.assertEquals("mask is wrong", "#,###.##", column.getFormatMask());
		Assert.assertEquals("alignment is wrong", "RIGHT", column.getHorizontalAlignment().toString());
		Assert.assertEquals("column agg types list is wrong size", 5, column.getAggTypes().length);

		column = category.getColumns()[8];
		Assert.assertEquals("column default agg type is wrong", "SUM", column.getDefaultAggType().toString());
		Assert.assertEquals("column id is wrong", "BC_ORDERDETAILS_TOTAL", column.getId());
		Assert.assertEquals("column name is wrong", "Total", column.getName());
		Assert.assertEquals("column selected agg type is wrong", "SUM", column.getSelectedAggType().toString());
		Assert.assertEquals("column type is wrong", "NUMERIC", column.getType().toString());
		Assert.assertEquals("field type is wrong", "FACT", column.getFieldType().toString());
		Assert.assertEquals("mask is wrong", "$#,##0.00;($#,##0.00)", column.getFormatMask());
		Assert.assertEquals("alignment is wrong", "RIGHT", column.getHorizontalAlignment().toString());
		Assert.assertEquals("column agg types list is wrong size", 5, column.getAggTypes().length);

	}

	@Test
	public void testGetModel2() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		Model model = svc.loadModel(null, "BV_HUMAN_RESOURCES");
		Assert.assertNull("model should be null", model);
	}

	@Test
	public void testGetModel3() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);
		MetadataService svc = new MetadataService();

		Model model = svc.loadModel("steel-wheels", null);
		Assert.assertNull("model should be null", model);
	}

	@Test
	public void testGetModel4() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		Model model = svc.loadModel("bogud", "BV_HUMAN_RESOURCES");
		Assert.assertNull("model should be null", model);
	}

	@Test
	public void testGetModel5() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		Model model = svc.loadModel("steel-wheels", "bogus");
		Assert.assertNull("model should be null", model);
	}

	@Test
	public void testGetModelJson() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		String json = svc.loadModelJson("steel-wheels", "BV_HUMAN_RESOURCES");
		Assert.assertNotNull("json should not be null", json);
		// System.out.println(json);

		Model model = new JSONDeserializer<Model>().deserialize(json);
		Assert.assertNotNull("model should not be null", model);

		Assert.assertEquals("domain id is wrong", "steel-wheels", model.getDomainId());
		Assert.assertEquals("model id is wrong", "BV_HUMAN_RESOURCES", model.getId());
		Assert.assertEquals("model name is wrong", "Human Resources", model.getName());
		Assert.assertEquals("model description is wrong", "This model contains information about Employees.", model.getDescription());
		Assert.assertTrue("model hash is wrong", model.hashCode() != 0);

		Assert.assertEquals("wrong number of categories", 2, model.getCategories().length);

		Category category = model.getCategories()[0];
		Assert.assertEquals("wrong number of business columns", 9, category.getColumns().length);
		Assert.assertEquals("category id is wrong", "BC_OFFICES_", category.getId());
		Assert.assertEquals("category name is wrong", "Offices", category.getName());

		IColumn column = category.getColumns()[0];
		Assert.assertEquals("column default agg type is wrong", "NONE", column.getDefaultAggType().toString());
		Assert.assertEquals("column id is wrong", "BC_OFFICES_TERRITORY", column.getId());
		Assert.assertEquals("column name is wrong", "Territory", column.getName());
		Assert.assertEquals("column selected agg type is wrong", "NONE", column.getSelectedAggType().toString());
		Assert.assertEquals("column type is wrong", "STRING", column.getType().toString());
		Assert.assertEquals("field type is wrong", "DIMENSION", column.getFieldType().toString());
		Assert.assertEquals("column agg types list is wrong size", 1, column.getAggTypes().length);
	}

	@Test
	public void testListBusinessModels1() throws IOException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		ModelInfo[] models = svc.listBusinessModels(null);
		Assert.assertNotNull(models);
		Assert.assertTrue("Wrong nuber of models returned", models.length > 2);
		boolean found = false;
		for (int idx = 0; idx < models.length; idx++) {
			if (models[idx].getDomainId().equals("steel-wheels") && models[idx].getModelId().equals("BV_HUMAN_RESOURCES")) {
				Assert.assertEquals("Wrong domain id", "steel-wheels", models[idx].getDomainId());
				Assert.assertEquals("Wrong description", "This model contains information about Employees.", models[idx].getModelDescription());
				Assert.assertEquals("Wrong model id", "BV_HUMAN_RESOURCES", models[idx].getModelId());
				Assert.assertEquals("Wrong model name", "Human Resources", models[idx].getModelName());
				found = true;
			}
		}
		Assert.assertTrue("model was not found", found);
	}

	@Test
	public void testListBusinessModels2() throws IOException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		ModelInfo[] models = svc.listBusinessModels("steel-wheels");
		Assert.assertNotNull(models);
		Assert.assertEquals("Wrong nuber of models returned", 3, models.length);
	}

	@Test
	public void testListBusinessModels3() throws IOException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		ModelInfo[] models = svc.listBusinessModels("bogus");
		Assert.assertNotNull(models);
		Assert.assertEquals("Wrong nuber of models returned", 0, models.length);
	}

	@Test
	public void testListBusinessModelsJson() throws IOException {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		MetadataService svc = new MetadataService();

		String json = svc.listBusinessModelsJson(null);
		Assert.assertNotNull(json);
		// System.out.println(json);
		// convert to a list
		Object result = new JSONDeserializer().deserialize(json);
		List<ModelInfo> modelList = new JSONDeserializer<List<ModelInfo>>().deserialize(json);

		ModelInfo models[] = modelList.toArray(new ModelInfo[modelList.size()]);

		Assert.assertNotNull(models);
		Assert.assertTrue("Wrong nuber of models returned", models.length > 2);
		boolean found = false;
		for (int idx = 0; idx < models.length; idx++) {
			if (models[idx].getDomainId().equals("steel-wheels") && models[idx].getModelId().equals("BV_HUMAN_RESOURCES")) {
				Assert.assertEquals("Wrong domain id", "steel-wheels", models[idx].getDomainId());
				Assert.assertEquals("Wrong description", "This model contains information about Employees.", models[idx].getModelDescription());
				Assert.assertEquals("Wrong model id", "BV_HUMAN_RESOURCES", models[idx].getModelId());
				Assert.assertEquals("Wrong model name", "Human Resources", models[idx].getModelName());
				found = true;
			}
		}
		Assert.assertTrue("model was not found", found);
	}

	@Test
	public void testParameter() {

		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		Parameter param = new Parameter();

		param.setDefaultValue(new String[] { "default" });
		Assert.assertEquals("default", param.getValue()[0]);
		Assert.assertEquals("default", param.getDefaultValue()[0]);

		param.setValue(new String[] { "value" });
		Assert.assertEquals("value", param.getValue()[0]);
		Assert.assertEquals("default", param.getDefaultValue()[0]);

		param.setType("String");
		Assert.assertEquals("String", param.getType());

		param.setName("myparam");
		Assert.assertEquals("myparam", param.getName());
	}

	@Test
	public void testCondition1() {
		manager.startup();
		login(USERNAME_JOE, TENANT_ID_ACME, true);

		Condition condition = new Condition();
		condition.setCategory("cat");
		condition.setColumn("column");
		condition.setCombinationType(CombinationType.AND.name());
		condition.setOperator(Operator.EQUAL.name());
		condition.setValue(new String[] { "bingo" });

		String str = condition.getCondition(DataType.STRING.getName());
		Assert.assertEquals("[cat.column] = [param:column]", str);

		condition.setValue(new String[] { "bingo" });
		str = condition.getCondition(DataType.STRING.getName(), condition.getColumn());
		Assert.assertEquals("[cat.column] = [param:column]", str);

		condition.setValue(new String[] { "bingo" });
		str = condition.getCondition(DataType.DATE.getName(), condition.getColumn());
		Assert.assertEquals("[cat.column] =DATEVALUE([param:bingo])", str);

		condition.setValue(new String[] { "bingo" });
		str = condition.getCondition(DataType.STRING.getName(), null);
		Assert.assertEquals("[cat.column] = \"bingo\"", str);

		condition.setValue(new String[] { "bingo" });
		str = condition.getCondition(DataType.DATE.getName(), null);
		Assert.assertEquals("[cat.column] =DATEVALUE(\"bingo\")", str);

		str = condition.getCondition(DataType.STRING.getName(), "myparam");
		Assert.assertEquals("[cat.column] = [param:myparam]", str);

	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		super.setApplicationContext(applicationContext);
		repo = (IUnifiedRepository) applicationContext.getBean("unifiedRepository");
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		// unfortunate reference to superclass
		JackrabbitRepositoryTestBase.setUpClass();
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		JackrabbitRepositoryTestBase.tearDownClass();
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		startupCalled = true;

		booter = new MicroPlatform("test-res/solution1");
		booter.define(IPasswordService.class, Base64PasswordService.class, Scope.GLOBAL);
		booter.defineInstance(IAuthorizationPolicy.class, authorizationPolicy);
		booter.define(IDatabaseConnection.class, DatabaseConnection.class, Scope.GLOBAL);
		booter.define(IDatabaseDialectService.class, DatabaseDialectService.class, Scope.GLOBAL);
		booter.define(IMondrianCatalogService.class, MondrianCatalogHelper.class, Scope.GLOBAL);
		booter.define("connection-SQL", SQLConnection.class, Scope.GLOBAL);
		booter.define(IDatasourceService.class, JndiDatasourceService.class, Scope.GLOBAL);
		booter.define(IPluginResourceLoader.class, PluginResourceLoader.class, Scope.GLOBAL);

		FileSystemBackedUnifiedRepository fileSystemRepo = (FileSystemBackedUnifiedRepository) repo;
		fileSystemRepo.setRootDir(new File("test-res/solution1/steel-wheels"));
		booter.defineInstance(IUnifiedRepository.class, fileSystemRepo);

		booter.defineInstance(IMetadataDomainRepository.class, new PentahoMetadataDomainRepository(fileSystemRepo));
		booter.setSettingsProvider(new SystemSettings());

		System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
		System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$

		booter.start();

		// Clear up the cache
		final ICacheManager cacheMgr = PentahoSystem.getCacheManager(null);
		cacheMgr.clearRegionCache(MondrianCatalogHelper.MONDRIAN_CATALOG_CACHE_REGION);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		if (startupCalled) {
			manager.shutdown();
		}
		// null out fields to get back memory
		repo = null;
	}

}