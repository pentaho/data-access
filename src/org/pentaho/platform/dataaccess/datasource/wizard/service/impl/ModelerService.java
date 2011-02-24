/*
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
 * Copyright 2009-2010 Pentaho Corporation.  All rights reserved.
 *
 * Created Sep, 2010
 * @author jdixon
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import mondrian.xmla.DataSourcesConfig.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.dataaccess.datasource.DatasourceType;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.FileService;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.CsvTransformGenerator;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.InlineSqlModelerSource;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianDataSource;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;

import com.thoughtworks.xstream.XStream;

/**
 * User: nbaker
 * Date: Jul 16, 2010
 */
public class ModelerService extends PentahoBase implements IModelerService {

  private static final Log logger = LogFactory.getLog(ModelerService.class);
  public static final String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + File.separatorChar + "tmp" + File.separatorChar;

  public Log getLogger() {
    return logger;
  }
  
  static {
    try {
      // try to set the modelermessages.  at this point, just give it the spoon messages.  no need to give it
      // GWT messages, this is a server-side function.
      ModelerMessagesHolder.setMessages(new SpoonModelerMessages());
    } catch (IllegalStateException e) {
      logger.debug(e.getMessage(), e);
    }
  }
  
  protected void initKettle(){

    try {
      KettleSystemListener.environmentInit(PentahoSessionHolder.getSession());
      if(Props.isInitialized() == false){
        Props.init(Props.TYPE_PROPERTIES_EMPTY);
      }
    } catch (KettleException e) {
      logger.error(e);
      e.printStackTrace();
      throw new IllegalStateException("Failed to initialize Kettle system"); //$NON-NLS-1$
    }
  }

  public Domain generateDomain(String connectionName, String tableName, String dbType, String query, String datasourceName) throws Exception {
    initKettle();
    try{
      DatabaseMeta database = AgileHelper.getDatabaseMeta();
      IModelerSource source;
      if(tableName != null){
        source = new TableModelerSource(database, tableName, null, datasourceName);
      } else {
        source = new InlineSqlModelerSource(connectionName, dbType, query, datasourceName);
      }
      return source.generateDomain();
    } catch(Exception e){
      logger.error(e);
      throw new Exception(e.getLocalizedMessage());
    }
  }


  public BogoPojo gwtWorkaround ( BogoPojo pojo){
    return new BogoPojo();
  }

  public String serializeModels(Domain domain, String name) throws Exception {
    String domainId = null;
    initKettle();

    try {
      ModelerWorkspace model = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
      model.setModelName(name);
      model.setDomain(domain);
      String solutionStorage = AgileHelper.getDatasourceSolutionStorage();

      String metadataLocation = "resources" + ISolutionRepository.SEPARATOR + "metadata"; //$NON-NLS-1$  //$NON-NLS-2$

      String path = solutionStorage + ISolutionRepository.SEPARATOR + metadataLocation + ISolutionRepository.SEPARATOR;
      domainId = path + name + ".xmi"; //$NON-NLS-1$ 

      IApplicationContext appContext = PentahoSystem.getApplicationContext();
      if (appContext != null) {
        path = PentahoSystem.getApplicationContext().getSolutionPath(path);
      }

      File pathDir = new File(path);
      if (!pathDir.exists()) {
        pathDir.mkdirs();
      }

      IPentahoObjectFactory pentahoObjectFactory = PentahoSystem.getObjectFactory();
      IPentahoSession session = pentahoObjectFactory.get(IPentahoSession.class, "systemStartupSession", null); //$NON-NLS-1$

      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);

      // Keep a reference to the mondrian catalog
      model.getWorkspaceHelper().populateDomain(model);
      
      prepareForSerialization(domain);
      
      LogicalModel lModel = domain.getLogicalModels().get(0);
      String catName = lModel.getName(Locale.getDefault().toString());
      lModel.setProperty("MondrianCatalogRef", catName); //$NON-NLS-1$
      XmiParser parser = new XmiParser();
      String reportXML =  parser.generateXmi(model.getDomain());

      // Serialize domain to xmi.
      String base = PentahoSystem.getApplicationContext().getSolutionRootPath();
      String parentPath = ActionInfo.buildSolutionPath(solutionStorage, metadataLocation, ""); //$NON-NLS-1$
      int status = repository.publish(base, '/' + parentPath, name + ".xmi", reportXML.getBytes("UTF-8"), true); //$NON-NLS-1$  //$NON-NLS-2$
      if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
        throw new RuntimeException("Unable to save to repository. Status: " + status); //$NON-NLS-1$
      }

      // Serialize domain to olap schema.
      MondrianModelExporter exporter = new MondrianModelExporter(lModel, Locale.getDefault().toString());
      String mondrianSchema = exporter.createMondrianModelXML();
      Document schemaDoc = DocumentHelper.parseText(mondrianSchema);
      byte[] schemaBytes = schemaDoc.asXML().getBytes("UTF-8"); //$NON-NLS-1$

      status = repository.publish(base, '/' + parentPath, name + ".mondrian.xml", schemaBytes, true); //$NON-NLS-1$  
      if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
        throw new RuntimeException("Unable to save to repository. Status: " + status); //$NON-NLS-1$  
      }

      // Refresh Metadata
      PentahoSystem.publish(session, MetadataPublisher.class.getName());

      // Write this catalog to the default Pentaho DataSource and refresh the cache.
      File file = new File(path + name + ".mondrian.xml"); //$NON-NLS-1$  
      String catConnectStr = "Provider=mondrian;DataSource=" + ((SqlPhysicalModel) domain.getPhysicalModels().get(0)).getDatasource().getDatabaseName(); //$NON-NLS-1$
      String catDef = "solution:" + solutionStorage + ISolutionRepository.SEPARATOR //$NON-NLS-1$
          + "resources" + ISolutionRepository.SEPARATOR + "metadata" + ISolutionRepository.SEPARATOR + file.getName(); //$NON-NLS-1$//$NON-NLS-2$
      addCatalog(catName, catConnectStr, catDef, session);

    } catch (Exception e) {
      logger.error(e);
      throw e;
    }
    return domainId;
  }
  
  protected void prepareForSerialization(Domain domain) throws IOException {
	  
		/*
		 * This method is responsible for cleaning up legacy information when changing datasource types  
		 * and also manages CSV files for CSV based datasources. 
		 **/  
	    
	    String relativePath = PentahoSystem.getSystemSetting(
	        "file-upload-defaults/relative-path", String.valueOf(FileService.DEFAULT_RELATIVE_UPLOAD_FILE_PATH)); //$NON-NLS-1$  
	    String path = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
	    String sysTmpDir = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
	    LogicalModel logicalModel = domain.getLogicalModels().get(0);
	    String modelState = (String) logicalModel.getProperty("datasourceModel"); //$NON-NLS-1$

	    if(modelState != null) {
	    	
	      XStream xs = new XStream();	
	      DatasourceDTO datasource = (DatasourceDTO) xs.fromXML(modelState);
	      CsvFileInfo csvFileInfo = datasource.getCsvModelInfo().getFileInfo();
	      String tmpFileName = csvFileInfo.getTmpFilename();
	      String csvFileName = csvFileInfo.getFileName();
	      File tmpFile = new File(sysTmpDir + File.separatorChar + tmpFileName);
	    
	      if(datasource.getDatasourceType().equals(DatasourceType.CSV)) {
		      
	    	  //  Move CSV temporary file to final destination.
	    	  if(tmpFile.exists()) {
		        File csvFile = new File(path + File.separatorChar + csvFileName);
		        FileUtils.copyFile(tmpFile, csvFile);
		      }
	    	  
	    	  // Cleanup logic when updating from SQL datasource to CSV datasource.
		      datasource.setQuery(null);
		      
	      } else if(datasource.getDatasourceType().equals(DatasourceType.SQL)) {
	    	  
	    	  if(csvFileName != null) { 
	    		  
	    		  // Cleanup logic when updating from CSV datasource to SQL datasource.
	    		  csvFileInfo.setFileName(null);
	    		  csvFileInfo.setTmpFilename(null);
	    		  csvFileInfo.setFriendlyFilename(null);
	    		  csvFileInfo.setContents(null);
	    		  csvFileInfo.setEncoding(null);
	    		  
	    		  // Delete CSV file.
	    		  File csvFile = new File(path + File.separatorChar + csvFileName);
	    		  if(csvFile.exists()) {
	    			  csvFile.delete();
	    		  }
	    		  
	  			  // Delete STAGING database table.
				  CsvTransformGenerator csvTransformGenerator = new CsvTransformGenerator(datasource.getCsvModelInfo(), AgileHelper.getDatabaseMeta());
				  try {
					  csvTransformGenerator.dropTable(datasource.getCsvModelInfo().getStageTableName());
				  } catch (CsvTransformGeneratorException e) {
					getLogger().error(e);
				  }
	    	  }
	      }
	      
	      // If mondrian catalog exists delete it to avoid duplicates and orphan entries in the datasources.xml registry.
	      IMondrianCatalogService service = PentahoSystem.get(IMondrianCatalogService.class, null);
          String catName = logicalModel.getName(Locale.getDefault().toString());
          MondrianCatalog catalog = service.getCatalog(catName, PentahoSessionHolder.getSession());
          if(catalog != null) {
        	  service.removeCatalog(catName, PentahoSessionHolder.getSession());
          }
	     	      
	      modelState = xs.toXML(datasource);
	      logicalModel.setProperty("datasourceModel", modelState);
	    }
	  }
  
  private void addCatalog(String catName, String catConnectStr, String catDef, IPentahoSession session) {
    
    IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", session); //$NON-NLS-1$
    
    String dsUrl = PentahoSystem.getApplicationContext().getBaseUrl();;
    if (!dsUrl.endsWith("/")) { //$NON-NLS-1$
      dsUrl += "/"; //$NON-NLS-1$
    }
    dsUrl += "Xmla"; //$NON-NLS-1$
    
    MondrianDataSource ds = new MondrianDataSource(
        "Provider=Mondrian;DataSource=Pentaho", //$NON-NLS-1$
        "Pentaho BI Platform Datasources", //$NON-NLS-1$
        dsUrl, 
        "Provider=Mondrian", // no default jndi datasource should be specified //$NON-NLS-1$
        "PentahoXMLA",  //$NON-NLS-1$
        DataSource.PROVIDER_TYPE_MDP, 
        DataSource.AUTH_MODE_UNAUTHENTICATED, 
        null
      );

    MondrianCatalog cat = new MondrianCatalog(
        catName, 
        catConnectStr, 
        catDef, 
        ds, 
        new MondrianSchema(catName, new ArrayList<MondrianCube>())
      );

      mondrianCatalogService.addCatalog(cat, true, session);
  }

  public Domain loadDomain(String id) throws Exception {
    IMetadataDomainRepository repo = PentahoSystem.get(IMetadataDomainRepository.class);
    return repo.getDomain(id);
  }
}
