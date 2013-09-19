/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.metadata.messages.LocaleHelper;
import org.pentaho.metadata.model.Category;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.LogicalTable;
import org.pentaho.metadata.model.SqlDataSource;
import org.pentaho.metadata.model.SqlPhysicalColumn;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.metadata.model.concept.types.LocaleType;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.model.concept.types.TargetTableType;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;


@SuppressWarnings("nls")
public class GuiStateModelTest {
  
  @Test
  public void test() {
    GuiStateModel guiStateModel = new GuiStateModel();
    Assert.assertEquals(0, guiStateModel.getConnections().size());
    Assert.assertEquals(false, guiStateModel.isRelationalValidated());
    IDatabaseConnection connection = new DatabaseConnection();
    connection.setAccessType(DatabaseAccessType.NATIVE);
//    connection.setDriverClass("org.hsqldb.jdbcDriver");
    connection.setName("SampleData");
    connection.setPassword("password");
//    connection.setUrl("jdbc:hsqldb:file:test-res/solution1/system/data/sampledata");
    connection.setUsername("pentaho_user");
    List<IDatabaseConnection> connectionList = new ArrayList<IDatabaseConnection>();
    connectionList.add(connection);
    guiStateModel.setConnections(connectionList);
    guiStateModel.setPreviewLimit("10");
    guiStateModel.validateRelational();
    Assert.assertEquals(true, guiStateModel.isRelationalPreviewValidated());
//    Assert.assertEquals(false, relationalModel.isValidated());
    LogicalColumn logColumn = new LogicalColumn();
    logColumn.setDataType(DataType.NUMERIC);
    List<AggregationType> aggTypeList = new ArrayList<AggregationType>();
    aggTypeList.add(AggregationType.AVERAGE);
    logColumn.setAggregationList(aggTypeList);
    logColumn.setName(new LocalizedString("En", "Column1"));
    BusinessData businessData = new BusinessData();
    List<List<String>> dataSample = new ArrayList<List<String>>();
    List<String> rowData = new ArrayList<String>();
    rowData.add("Data1");
    rowData.add("Data2");
    rowData.add("Data3");
    rowData.add("Data4");
    dataSample.add(rowData);

    String locale = LocaleHelper.getLocale().toString();
    
    SqlPhysicalModel model = new SqlPhysicalModel();
    SqlDataSource dataSource = new SqlDataSource();
    dataSource.setDatabaseName("SampleData");
    model.setDatasource(dataSource);
    SqlPhysicalTable table = new SqlPhysicalTable(model);
    model.getPhysicalTables().add(table);
    table.setTargetTableType(TargetTableType.INLINE_SQL);
    table.setTargetTable("select * from customers");
    
    SqlPhysicalColumn column = new SqlPhysicalColumn(table);
    column.setTargetColumn("customername");
    column.setName(new LocalizedString(locale, "Customer Name"));
    column.setDescription(new LocalizedString(locale, "Customer Name Desc"));
    column.setDataType(DataType.STRING);
    
    table.getPhysicalColumns().add(column);
    
    LogicalModel logicalModel = new LogicalModel();
    model.setId("MODEL");
    model.setName(new LocalizedString(locale, "My Model"));
    model.setDescription(new LocalizedString(locale, "A Description of the Model"));
    
    LogicalTable logicalTable = new LogicalTable();
    logicalTable.setPhysicalTable(table);
    
    logicalModel.getLogicalTables().add(logicalTable);
    
    LogicalColumn logicalColumn = new LogicalColumn();
    logicalColumn.setId("LC_CUSTOMERNAME");
    logicalColumn.setPhysicalColumn(column);
    
    logicalTable.addLogicalColumn(logicalColumn);
    
    Category mainCategory = new Category();
    mainCategory.setId("CATEGORY");
    mainCategory.setName(new LocalizedString(locale, "Category"));
    mainCategory.addLogicalColumn(logicalColumn);
    
    logicalModel.getCategories().add(mainCategory);
    
    Domain domain = new Domain();
    domain.addPhysicalModel(model);
    domain.addLogicalModel(logicalModel);
    List<LocaleType> localeTypeList = new ArrayList<LocaleType>();
    localeTypeList.add(new LocaleType("Code", "Locale Description"));
    domain.setLocales(localeTypeList);
    businessData.setData(dataSample);
    businessData.setDomain(domain);
    guiStateModel.setLogicalModels(domain.getLogicalModels());
    guiStateModel.setLocaleCode("en");
    Assert.assertEquals(true, guiStateModel.isRelationalValidated()); 
  }
}
