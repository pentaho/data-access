package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ModelerService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.CsvDatasourceServiceHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class SerializeServiceTest {

  static {
    if (!PentahoSystem.getInitializedOK()) {
      CsvDatasourceServiceHelper csvHelper = new CsvDatasourceServiceHelper();
      csvHelper.setUp();
      System.setProperty("org.osjava.sj.root", "test-src/solution/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  @Test
  public void testSerialize() throws Exception {
    KettleEnvironment.init();
    Props.init(Props.TYPE_PROPERTIES_EMPTY);

    String solutionStorage = AgileHelper.getDatasourceSolutionStorage();
    String path = solutionStorage + ISolutionRepository.SEPARATOR
        + "resources" + ISolutionRepository.SEPARATOR + "metadata" + ISolutionRepository.SEPARATOR; //$NON-NLS-1$  //$NON-NLS-2$

    String olapPath = null;

    IApplicationContext appContext = PentahoSystem.getApplicationContext();
    if (appContext != null) {
      path = PentahoSystem.getApplicationContext().getSolutionPath(path);
      olapPath = PentahoSystem.getApplicationContext().getSolutionPath(
          "system" + ISolutionRepository.SEPARATOR + "olap" + ISolutionRepository.SEPARATOR); //$NON-NLS-1$  //$NON-NLS-2$
    }

    File olap1 = new File(olapPath + "datasources.xml"); //$NON-NLS-1$
    File olap2 = new File(olapPath + "tmp_datasources.xml"); //$NON-NLS-1$

    FileUtils.copyFile(olap1, olap2);

    Domain domain = generateModel();
    ModelerService service = new ModelerService();
    service.serializeModels(domain, "test_file");//$NON-NLS-1$

    Assert.assertEquals(domain.getLogicalModels().get(0).getProperty("MondrianCatalogRef"), "test_file");
    
    File xmiFile = new File(path + "test_file.xmi");//$NON-NLS-1$
    File mondrianFile = new File(path + "test_file.mondrian.xml");//$NON-NLS-1$

    assertTrue(xmiFile.exists());
    assertTrue(mondrianFile.exists());

    if (xmiFile.exists()) {
      xmiFile.delete();
    }

    if (mondrianFile.exists()) {
      mondrianFile.delete();
    }

    //Restores datasources.xml to its original content.
    FileUtils.copyFile(olap2, olap1);
    olap2.delete();
  }

  private Domain generateModel() {
    Domain domain = null;
    try {

      DatabaseMeta database = new DatabaseMeta();
      //database.setDatabaseInterface(new HypersonicDatabaseMeta());
      database.setDatabaseType("Hypersonic");//$NON-NLS-1$
      //database.setUsername("sa");//$NON-NLS-1$
      //database.setPassword("");//$NON-NLS-1$
      database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
      //database.setHostname(".");
      database.setDBName("SampleData");//$NON-NLS-1$
      //database.setDBPort("9001");//$NON-NLS-1$
      database.setName("SampleData");//$NON-NLS-1$

      System.out.println(database.testConnection());

      TableModelerSource source = new TableModelerSource(database, "ORDERS", null);//$NON-NLS-1$
      domain = source.generateDomain();

      List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
      OlapDimension dimension = new OlapDimension();
      dimension.setName("test");//$NON-NLS-1$
      dimension.setTimeDimension(false);
      olapDimensions.add(dimension);
      domain.getLogicalModels().get(0).setProperty("olap_dimensions", olapDimensions);//$NON-NLS-1$

    } catch (Exception e) {
      e.printStackTrace();
    }
    return domain;
  }
}
