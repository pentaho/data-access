package org.pentaho.platform.dataaccess.datasource.wizard;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Test;
import static org.junit.Assert.*;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
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
