package org.pentaho.platform.dataaccess.datasource;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.platform.dataaccess.datasource.ui.admindialog.DatasourceAdminDialogModel;

public class DatasourcAdminDialogModelTest {

  @Test
  public void test() {
    DatasourceAdminDialogModel model = new DatasourceAdminDialogModel();
    model.addDatasource(new DatasourceInfo("datasource1","datasource1", "JDBC", true, true, false, false));
    model.addDatasource(new DatasourceInfo("datasource2","datasource2", "Metadata", false, true, true, true));
    model.addDatasource(new DatasourceInfo("datasource3","datasource3", "Analysis", false, true, true, true));
    model.addDatasource(new DatasourceInfo("datasource4","datasource4", "Data Source Wizard", true, true, false, true));
    
    
    Assert.assertEquals(4, model.getDatasourcesList().size());
    List<String> datasourceTypes = new ArrayList<String>() ;
    datasourceTypes.add("JDBC");
    datasourceTypes.add("Metadata");
    datasourceTypes.add("Analysis");
    datasourceTypes.add("Data Source Wizard");

    model.setDatasourceTypeList(datasourceTypes);
    
    Assert.assertEquals(4, model.getDatasourceTypeList().size());
    
    model.setSelectedDatasource(new DatasourceInfo("datasource1","datasource1", "JDBC", true, true, false, false));
    IDatasourceInfo datasourceInfo = model.getSelectedDatasource();
    
    Assert.assertEquals("JDBC", datasourceInfo.getType());
  }
}
