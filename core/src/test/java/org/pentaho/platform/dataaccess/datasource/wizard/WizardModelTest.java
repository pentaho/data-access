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

package org.pentaho.platform.dataaccess.datasource.wizard;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import org.pentaho.platform.dataaccess.datasource.wizard.models.WizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasource;
import org.pentaho.ui.xul.XulException;

/**
 * User: nbaker
 * Date: 3/31/11
 */
public class WizardModelTest {

    Mockery context = new JUnit4Mockery();
  @Test
  public void testDatasourceAdditionAndRemoval(){
    WizardModel model = new WizardModel();
    QueryDatasource queryDatasource = new QueryDatasource( context.mock(IXulAsyncDSWDatasourceService.class), null);
    model.addDatasource(queryDatasource);
    assertTrue(model.getDatasources().contains(queryDatasource));
    model.removeDatasourceByType(QueryDatasource.class);
    assertFalse(model.getDatasources().contains(queryDatasource));
  }

  @Test
  /**
   * Due to the initialization path of the wizard, datasources may be added after a call to remove them has come in from
   * the client. In this case the addition should be ignored
   */
  public void testIgnoredAddition() throws XulException {
    WizardModel model = new WizardModel();
    model.removeDatasourceByType(QueryDatasource.class);
    IXulAsyncDSWDatasourceService datasource = context.mock(IXulAsyncDSWDatasourceService.class);
    QueryDatasource queryDatasource = new QueryDatasource(datasource , null);
    model.addDatasource(queryDatasource);
    assertFalse(model.getDatasources().contains(queryDatasource));

    // this mechninism shouldn't prevent subclasses from being added though
    QueryDatasource subClass = new QueryDatasource( datasource, null){
      
    };
    model.addDatasource(subClass);
    assertTrue(model.getDatasources().contains(subClass));

  }
}
