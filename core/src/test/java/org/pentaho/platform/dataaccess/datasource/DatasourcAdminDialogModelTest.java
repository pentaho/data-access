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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

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
