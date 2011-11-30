package org.pentaho.platform.dataaccess.datasource.wizard.modeler;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ConnectionDebugGwtServlet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryDatasourceServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.InlineSqlModelerSource;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.test.platform.engine.core.BaseTest;

import java.io.File;

/**
 * User: nbaker
 * Date: Jul 26, 2010
 */
public class ModelerServiceTest extends BaseTest {


  private static final String solution = "testsolution"; //$NON-NLS-1$

  private static final String SOLUTION_PATH = "test-res/solution1/"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-res/solution11"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

  private static final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH); //$NON-NLS-1$
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH); //$NON-NLS-1$
      return ALT_SOLUTION_PATH;
    }
  }


  private void init() throws Exception{
    if (!PentahoSystem.getInitializedOK()) {
      IApplicationContext context = new StandaloneApplicationContext(SOLUTION_PATH, "."); //$NON-NLS-1$
      PentahoSystem.init(context);
    }


  }
  
  @Test
  public void testGenerateDomainFromInlineSql() throws Exception{

    new ConnectionDebugGwtServlet().getConnections();

    IDatasourceService datasourceService = new InMemoryDatasourceServiceImpl();

    IModelerSource source = new InlineSqlModelerSource(datasourceService, "SampleData", "Hypersonic", "select * from customers", "testModel");

    Domain d = source.generateDomain();

    assertEquals(1, d.getLogicalModels().size());

    assertEquals("LOGICAL_TABLE_1", d.getLogicalModels().get(0).getLogicalTables().get(0).getId());
  }
}
