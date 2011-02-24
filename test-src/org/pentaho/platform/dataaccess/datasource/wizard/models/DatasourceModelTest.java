package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.metadata.messages.LocaleHelper;
import org.pentaho.metadata.model.*;
import org.pentaho.metadata.model.concept.types.*;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.Delimiter;
import org.pentaho.platform.dataaccess.datasource.Enclosure;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceMessages;
import org.pentaho.platform.dataaccess.datasource.wizard.models.GuiStateModel.ConnectionEditType;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.CsvTransformGeneratorTest;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryDatasourceServiceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("nls")
public class DatasourceModelTest {
  
  @Test
  public void test() {
    DatasourceModel datasourceModel = new DatasourceModel();
//    Assert.assertNull(datasourceModel.getCsvModel().getSelectedFile());
    Assert.assertNull(datasourceModel.getQuery());
    assertNull(datasourceModel.getModelInfo().getFileInfo().getTmpFilename());
    Assert.assertEquals(DatasourceType.NONE, datasourceModel.getDatasourceType());
    datasourceModel.setDatasourceType(DatasourceType.SQL);
    Assert.assertEquals(false, datasourceModel.isValidated());
    datasourceModel.setGuiStateModel(contructRelationalModel(datasourceModel.getGuiStateModel()));
    Assert.assertEquals(true, datasourceModel.isValidated());
    datasourceModel.setDatasourceType(DatasourceType.CSV);    
    Assert.assertEquals(false, datasourceModel.isValidated());
//    datasourceModel.setCsvModel(constructCsvModel(datasourceModel.getCsvModel()));    
    Assert.assertEquals(false, datasourceModel.isValidated());    
    datasourceModel.setModelInfo(CsvTransformGeneratorTest.createModel());
    assertEquals(false, datasourceModel.isValidated());
  }
  
  private GuiStateModel contructRelationalModel(GuiStateModel guiStateModel) {
    IConnection connection = new Connection();
    connection.setDriverClass("org.hsqldb.jdbcDriver");
    connection.setName("SampleData");
    connection.setPassword("password");
    connection.setUrl("jdbc:hsqldb:file:test-src/solution/system/data/sampledata");
    connection.setUsername("pentaho_user");
    List<IConnection> connectionList = new ArrayList<IConnection>();
    connectionList.add(connection);
    guiStateModel.setConnections(connectionList);
    guiStateModel.setEditType(ConnectionEditType.EDIT);
    guiStateModel.setPreviewLimit("10");
    guiStateModel.validateRelational();
    LogicalColumn logColumn = new LogicalColumn();
    logColumn.setDataType(DataType.NUMERIC);
    List<AggregationType> aggTypeList = new ArrayList<AggregationType>();
    aggTypeList.add(AggregationType.AVERAGE);
    logColumn.setAggregationList(aggTypeList);
    logColumn.setName(new LocalizedString("En", "Column1"));
    List<ModelDataRow> dataRows = new ArrayList<ModelDataRow>();
    List<String> data = new ArrayList<String>();
    data.add("Sample1");
    data.add("Sample2");
    data.add("Sample3");
    data.add("Sample4");
    data.add("Sample5");
    ModelDataRow row = new ModelDataRow(logColumn, data, "En");
    dataRows.add(row);
    guiStateModel.setDataRows(dataRows);
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
    guiStateModel.setLocaleCode("en");
    guiStateModel.setLogicalModels(domain.getLogicalModels());
    guiStateModel.setRelationalData(businessData.getData());
    guiStateModel.validateRelational();
    return guiStateModel;
  }
  
  private CsvModel constructCsvModel(CsvModel csvModel) {
    csvModel.setSelectedFile("user/local/myfile.csv");
    csvModel.setDelimiter(Delimiter.COMMA);
    csvModel.setEnclosure(Enclosure.DOUBLEQUOTE);
    csvModel.setMessages(new GwtDatasourceMessages());
    csvModel.setEnclosureList();
    csvModel.setDelimiterList();
    csvModel.setHeadersPresent(true);
    List<CsvModelDataRow> dataRows = new ArrayList<CsvModelDataRow>();
    LogicalColumn logColumn = new LogicalColumn();
    logColumn.setDataType(DataType.NUMERIC);
    List<AggregationType> aggTypeList = new ArrayList<AggregationType>();
    aggTypeList.add(AggregationType.AVERAGE);
    logColumn.setAggregationList(aggTypeList);
    logColumn.setName(new LocalizedString("En", "Column1"));
    List<String> data = new ArrayList<String>();
    data.add("Sample1");
    data.add("Sample2");
    data.add("Sample3");
    data.add("Sample4");
    data.add("Sample5");
    CsvModelDataRow row = new CsvModelDataRow(logColumn, data, "En");
    dataRows.add(row);
    csvModel.setDataRows(dataRows);
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
    csvModel.setBusinessData(businessData);
    csvModel.validate();
    return csvModel;
  }
  
  
  @Test
  public void testGenerateTablename() throws Exception {
    DatasourceModel model = new DatasourceModel();
    
    model.setDatasourceName("ABcdef$GhijkLmNopqRSTuvwxy&z 1234567890");
    assertEquals("abcdefghijklmnopqrstuvwxyz_1234567890", model.generateTableName());
    
    model.setDatasourceName("!@#$%^&*()=+?><';:.|{}~`");
    assertEquals("", model.generateTableName());
  }
  
  @Test(expected=IllegalStateException.class)
  public void testGenerateTablename_nullDatasourceName() throws Exception {
     DatasourceModel model = new DatasourceModel();
     model.generateTableName();
  }


  @Test
  public void testRelationalDatasourceDTO() throws Exception {

    DatasourceModel datasourceModel = new DatasourceModel();
    datasourceModel.setDatasourceName("testDatasource");
    datasourceModel.setDatasourceType(DatasourceType.SQL);
    datasourceModel.setGuiStateModel(contructRelationalModel(datasourceModel.getGuiStateModel()));
    datasourceModel.setSelectedRelationalConnection(datasourceModel.getGuiStateModel().getConnections().get(0));

    DatasourceDTO dto = DatasourceDTOUtil.generateDTO(datasourceModel);
    assertNotNull(dto);
    assertEquals(datasourceModel.getDatasourceName(), dto.getDatasourceName());
    assertEquals(datasourceModel.getDatasourceType(), dto.getDatasourceType());
    assertEquals(datasourceModel.getQuery(), dto.getQuery());
    assertEquals(datasourceModel.getSelectedRelationalConnection().getName(), dto.getConnectionName());

    DatasourceModel datasourceModel2 = new DatasourceModel();

    IConnection connection = new Connection();
    connection.setDriverClass("org.hsqldb.jdbcDriver");
    connection.setName("SampleData");
    connection.setPassword("password");
    connection.setUrl("jdbc:hsqldb:file:test-src/solution/system/data/sampledata");
    connection.setUsername("pentaho_user");
    datasourceModel2.getGuiStateModel().setConnections(Collections.singletonList(connection));

    DatasourceDTOUtil.populateModel(dto, datasourceModel2);

    assertEquals(datasourceModel.getDatasourceName(), datasourceModel2.getDatasourceName());
    assertEquals(datasourceModel.getDatasourceType(), datasourceModel2.getDatasourceType());
    assertEquals(datasourceModel.getQuery(), datasourceModel2.getQuery());
    assertEquals(datasourceModel.getSelectedRelationalConnection().getName(), datasourceModel2.getSelectedRelationalConnection().getName());

  }


  @Test
  public void testDatasourceDTOSerialization() throws Exception {

    DatasourceModel datasourceModel = new DatasourceModel();
    datasourceModel.setDatasourceName("testDatasource");
    datasourceModel.setDatasourceType(DatasourceType.SQL);
    datasourceModel.setGuiStateModel(contructRelationalModel(datasourceModel.getGuiStateModel()));
    datasourceModel.setSelectedRelationalConnection(datasourceModel.getGuiStateModel().getConnections().get(0));

    DatasourceDTO dto = DatasourceDTOUtil.generateDTO(datasourceModel);
    assertNotNull(dto);
    InMemoryDatasourceServiceImpl service = new InMemoryDatasourceServiceImpl();
    String dtoString = service.serializeModelState(dto);
    assertNotNull(dtoString);
    assertTrue(dtoString.contains("testDatasource"));

    DatasourceDTO dto2 = service.deSerializeModelState(dtoString);

    assertEquals(dto, dto2);
  }
}
