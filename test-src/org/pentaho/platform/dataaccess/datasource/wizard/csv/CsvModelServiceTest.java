package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import java.io.File;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.CsvTransformGeneratorTest;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.test.platform.engine.core.BaseTest;


public class CsvModelServiceTest extends BaseTest {
  private static final String SOLUTION_PATH = "test-res/solution1/"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-res/solution11"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "/system/pentaho.xml"; //$NON-NLS-1$

  private static final String SYSTEM_FOLDER = "/system"; //$NON-NLS-1$

  private static final String SOLUTION = "testsolution"; //$NON-NLS-1$

  private CsvModelService service = null;

  private void init() {
    if (!PentahoSystem.getInitializedOK()) {
      IApplicationContext context = new StandaloneApplicationContext(SOLUTION_PATH, "."); //$NON-NLS-1$
      PentahoSystem.init(context);
    }
  }
  
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
  
  @Test
  public void testSaveModelInfo() throws Exception {
    ModelInfo modelInfo = CsvTransformGeneratorTest.createModel();
    service = new CsvModelService();
    service.saveModelInfo(modelInfo);

    String project = modelInfo.getFileInfo().getProject() == null ? "" : modelInfo.getFileInfo().getProject();
    
    String filepath = AgileHelper.getFolderPath(project) + "/" + modelInfo.getFileInfo().getTmpFilename() + ".xml";
    File f = new File(filepath);
    assertTrue(f.exists());
  }
  
  @Test
  public void testGetModelInfo() throws Exception {
    testSaveModelInfo();
    ModelInfo expected = CsvTransformGeneratorTest.createModel();
    service = new CsvModelService();
    ModelInfo modelInfo = service.getModelInfo(SOLUTION, "unit_test.csv"); //$NON-NLS-1$
    assertNotNull(modelInfo);
    assertEquals(expected, modelInfo);
  }

  @SuppressWarnings("nls")
  @Test
  public void testGetLines() throws Exception {
    service = new CsvModelService();
    ModelInfo modelInfo = CsvTransformGeneratorTest.createModel();
    String project = modelInfo.getFileInfo().getProject() == null ? "" : modelInfo.getFileInfo().getProject();
    String filepath = AgileHelper.getFolderPath(project) + "/" + modelInfo.getFileInfo().getTmpFilename();

    String lines = service.getLines(filepath, 100, "UTF-8");
    System.out.println(lines);
    StringTokenizer tokenizer = new StringTokenizer(lines, "\r\n");
    assertNotNull(lines);
    assertEquals(100, tokenizer.countTokens());
  }
  
  @Test
  public void testAssumeColumnDetails_Numeric() {
    service = new CsvModelService();
    String[] samples = new String[] {"100.00", "100.00", "100.08", "100.00", "100.12", "100.02"};
    ColumnInfo ci = new ColumnInfo();

    service.assumeColumnDetails(ci, Arrays.asList(samples));
    assertEquals(DataType.NUMERIC, ci.getDataType());
    assertEquals(2, ci.getPrecision());

    samples = new String[] {"12.009", "988,000.3", "9877.9991", "999", "888.02"};
    ci = new ColumnInfo();

    service.assumeColumnDetails(ci, Arrays.asList(samples));
    assertEquals(DataType.NUMERIC, ci.getDataType());
    assertEquals("#,##0.###", ci.getFormat());
    assertEquals(4, ci.getPrecision());
  }
  @Test
  public void testAssumeColumnDetails_Currency() {
    service = new CsvModelService();
    String[] samples = new String[] {"$101.04", "$100.3", "$100.3000", "$100.1", "$11100.32", "$7,100.433", "($500.00)"};
    ColumnInfo ci = new ColumnInfo();

    service.assumeColumnDetails(ci, Arrays.asList(samples));
    assertEquals(DataType.NUMERIC, ci.getDataType());
    assertEquals(2, ci.getPrecision());

    String format = ci.getFormat();
    assertNotNull(format);
    assertEquals("$#,##0.00;($#,##0.00)", format);

    ci = new ColumnInfo();
    samples = new String[] {"$101.04", "$100.3", "$100.3000", "$100.1", "not currency"};
    service.assumeColumnDetails(ci, Arrays.asList(samples));
    format = ci.getFormat();
    assertNull(format);


  }

  @Test
  public void testAssumeColumnDetails_Dates() {
    service = new CsvModelService();
    String[] samples = new String[] {"01/20/2000", "02/29/2000", "10/31/2000", "12/31/2000", "01/01/2000"};
    ColumnInfo ci = new ColumnInfo();

    service.assumeColumnDetails(ci, Arrays.asList(samples));
    assertEquals(DataType.DATE, ci.getDataType());
    assertEquals("MM/dd/yyyy", ci.getFormat());

  }
}
