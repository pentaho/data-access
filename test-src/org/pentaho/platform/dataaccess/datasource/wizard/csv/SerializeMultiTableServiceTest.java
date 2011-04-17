package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.olap.OlapDimension;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ModelerService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class SerializeMultiTableServiceTest {
	
	  static {
	    if (!PentahoSystem.getInitializedOK()) {
	      PentahoSystemHelper.init();
	      System.setProperty("org.osjava.sj.root", "test-res/solution1/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
	    }
	    
	    if(ModelerMessagesHolder.getMessages() == null){
	 	   ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
	 	}
	  }

	  @Test
	  public void testSerialize() throws Exception {
		  try{
	      KettleEnvironment.init();
	      Props.init(Props.TYPE_PROPERTIES_EMPTY);
      } catch(Exception e){
        // may already be initialized by another test
      }

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

	    DatabaseMeta database = getDatabaseMeta();
		MultiTableModelerSource multiTable = new MultiTableModelerSource(database, getSchema(), database.getName(), Arrays.asList("CUSTOMERS","PRODUCTS","CUSTOMERNAME","PRODUCTCODE"));
		Domain domain = multiTable.generateDomain();
	    
	    List<OlapDimension> olapDimensions = new ArrayList<OlapDimension>();
		OlapDimension dimension = new OlapDimension();
		dimension.setName("test");//$NON-NLS-1$
		dimension.setTimeDimension(false);
		olapDimensions.add(dimension);
		domain.getLogicalModels().get(0).setProperty("olap_dimensions", olapDimensions);//$NON-NLS-1$
	    
	    
	    ModelerService service = new ModelerService();
	    service.serializeModels(domain, "test_file");//$NON-NLS-1$

	    Assert.assertEquals(domain.getLogicalModels().get(0).getProperty("MondrianCatalogRef"), "SampleData");
	    
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
	  
	  private SchemaModel getSchema() {
			List<JoinRelationshipModel> joins = new ArrayList<JoinRelationshipModel>();

			JoinTableModel joinTable1 = new JoinTableModel();
			joinTable1.setName("CUSTOMERS");

			JoinTableModel joinTable2 = new JoinTableModel();
			joinTable2.setName("PRODUCTS");

			JoinRelationshipModel join1 = new JoinRelationshipModel();
			JoinFieldModel lField1 = new JoinFieldModel();
			lField1.setName("CUSTOMERNAME");
			lField1.setParentTable(joinTable1);
			join1.setLeftKeyFieldModel(lField1);

			JoinFieldModel rField1 = new JoinFieldModel();
			rField1.setName("PRODUCTCODE");
			rField1.setParentTable(joinTable2);
			join1.setRightKeyFieldModel(rField1);

			joins.add(join1);
      SchemaModel model = new SchemaModel();
      model.setJoins(joins);
			return model;
		}

	  private DatabaseMeta getDatabaseMeta() {
	     DatabaseMeta database = new DatabaseMeta();
	     try {
	      //database.setDatabaseInterface(new HypersonicDatabaseMeta());
	      database.setDatabaseType("Hypersonic");//$NON-NLS-1$
	      //database.setUsername("sa");//$NON-NLS-1$
	      //database.setPassword("");//$NON-NLS-1$
	      database.setAccessType(DatabaseMeta.TYPE_ACCESS_JNDI);
	      //database.setHostname(".");
	      database.setDBName("SampleData");//$NON-NLS-1$
	      //database.setDBPort("9001");//$NON-NLS-1$
	      database.setName("SampleData");//$NON-NLS-1$
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	    return database;
	  }
	}
