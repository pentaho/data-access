package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.util.StringEvaluator;
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
import org.pentaho.platform.dataaccess.datasource.Delimiter;
import org.pentaho.platform.dataaccess.datasource.Enclosure;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.wizard.GwtDatasourceMessages;

public class CsvModelTest {
  
  @SuppressWarnings("nls")
  @Test
  public void test() {
  
    CsvModel csvModel = new CsvModel();
    Assert.assertNull(csvModel.getBusinessData());
    Assert.assertNull(csvModel.getDelimiter());
    Assert.assertNull(csvModel.getEnclosure());
    Assert.assertNull(csvModel.getDelimiterList());
    Assert.assertNull(csvModel.getEnclosureList());
    Assert.assertNull(csvModel.getSelectedFile());
    Assert.assertNull(csvModel.getDatasourceName());
    Assert.assertEquals(0, csvModel.getDataRows().size());
    Assert.assertEquals(false, csvModel.isValidated());
    csvModel.setDatasourceName("newdatasource");
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
    Assert.assertEquals("user/local/myfile.csv", csvModel.getSelectedFile());
    Assert.assertEquals(Delimiter.COMMA, csvModel.getDelimiter());
    Assert.assertEquals(Enclosure.DOUBLEQUOTE, csvModel.getEnclosure());
    Assert.assertEquals(5, csvModel.getDelimiterList().size());
    Assert.assertEquals(3, csvModel.getEnclosureList().size());
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
    Assert.assertEquals(true, csvModel.isValidated());    
  }


  @Ignore
  @Test
  public void testStringEvaluator_format() {
    StringEvaluator eval = new StringEvaluator(false);    
    eval.evaluateString("$400000.345");

    ValueMetaInterface meta = eval.getAdvicedResult().getConversionMeta();
    String format = meta.getConversionMask();
    
    System.out.println(format);

    eval = new StringEvaluator(false);
    eval.evaluateString("12/25/2010");
    meta = eval.getAdvicedResult().getConversionMeta();
    format = meta.getConversionMask();

    System.out.println(format);

    eval = new StringEvaluator(false);
    eval.evaluateString("12.4");
    meta = eval.getAdvicedResult().getConversionMeta();
    format = meta.getConversionMask();
    System.out.println(format);

  }

}
