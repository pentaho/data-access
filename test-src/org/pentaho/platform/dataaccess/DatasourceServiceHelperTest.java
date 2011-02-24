package org.pentaho.platform.dataaccess;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;

public class DatasourceServiceHelperTest {

  @SuppressWarnings("nls")
  @Test
  public void testCsvSampleData() {

    List<List<String>> data = DatasourceServiceHelper.getCsvDataSample("test-res/example.csv", true, ",", "\"", 10);
    Assert.assertEquals(5, data.size());
    Assert.assertEquals(4, data.get(0).size());

    // row 1
    Assert.assertEquals("1", data.get(0).get(0));
    Assert.assertEquals("4.5", data.get(0).get(1));
    Assert.assertEquals("02/12/77", data.get(0).get(2));
    Assert.assertEquals("String - , Value", data.get(0).get(3));

  }
  
  @SuppressWarnings("nls")
  @Test
  public void testCsvDoesNotExist() {
    // all this does is return 0
    List<List<String>> data = DatasourceServiceHelper.getCsvDataSample("test-res/doesnotexist.csv", true, ",", "\"", 10);
    Assert.assertEquals(0, data.size());
  }
}
