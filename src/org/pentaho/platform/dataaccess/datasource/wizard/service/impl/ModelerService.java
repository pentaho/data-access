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
import java.util.ArrayList;
import java.util.Locale;

import mondrian.xmla.DataSourcesConfig.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.agilebi.modeler.*;
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
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
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
      throw new IllegalStateException("Failed to initialize Kettle system"); //$NON-NLS-1$
    }
  }

  //TODO: remove this method in favor so specific calls
  @Deprecated
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


  public Domain generateCSVDomain(String tableName, String datasourceName) throws Exception {
    initKettle();
    try{
      DatabaseMeta database = AgileHelper.getDatabaseMeta();
      IModelerSource source = new TableModelerSource(database, tableName, null, datasourceName);
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
    return serializeModels(domain, name, true);
  }

  public String serializeModels(Domain domain, String name, boolean doOlap) throws Exception {
    String domainId = null;
    initKettle();

    try {
      DatasourceServiceImpl datasourceService = new DatasourceServiceImpl();      
      ModelerWorkspace model = new ModelerWorkspace(new GwtModelerWorkspaceHelper(), datasourceService.getGeoContext());
      model.setModelName(name);
      model.setDomain(domain);
      String solutionStorage = AgileHelper.getDatasourceSolutionStorage();

      String metadataLocation = "resources" + ISolutionRepository.SEPARATOR + "metadata"; //$NON-NLS-1$  //$NON-NLS-2$

      String path = solutionStorage + ISolutionRepository.SEPARATOR + metadataLocation + ISolutionRepository.SEPARATOR;
      domainId = path + name + ".xmi"; //$NON-NLS-1$ 
      domain.setId(domainId);

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
      
      LogicalModel lModel = model.getLogicalModel(ModelerPerspective.ANALYSIS);
      String catName = lModel.getName(Locale.getDefault().toString());

      // strip off the _olap suffix for the catalog ref
      catName = catName.replace(BaseModelerWorkspaceHelper.OLAP_SUFFIX, "");

      cleanseExistingCatalog(catName, session);
      if(doOlap){
        lModel.setProperty("MondrianCatalogRef", catName); //$NON-NLS-1$
      }
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
      if(doOlap){
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
        // Need to find a better way to get the connection name instead of using the Id.
        String catConnectStr = "Provider=mondrian;DataSource=" + ((SqlPhysicalModel) domain.getPhysicalModels().get(0)).getId(); //$NON-NLS-1$        
        String catDef = "solution:" + solutionStorage + ISolutionRepository.SEPARATOR //$NON-NLS-1$
            + "resources" + ISolutionRepository.SEPARATOR + "metadata" + ISolutionRepository.SEPARATOR + file.getName(); //$NON-NLS-1$//$NON-NLS-2$
        addCatalog(catName, catConnectStr, catDef, session);
      }

    } catch (Exception e) {
      logger.error(e);
      throw e;
    }
    return domainId;
  }
  
  private void cleanseExistingCatalog(String catName, IPentahoSession session) {
	  
	  // If mondrian catalog exists delete it to avoid duplicates and orphan entries in the datasources.xml registry.
	  //IPentahoSession session = PentahoSessionHolder.getSession();
	  if(session != null) {
		  IMondrianCatalogService service = PentahoSystem.get(IMondrianCatalogService.class, null);
	      MondrianCatalog catalog = service.getCatalog(catName, session);
	      if(catalog != null) {
	      	  service.removeCatalog(catName, session);
	      }
	  }
  }
  
  private void addCatalog(String catName, String catConnectStr, String catDef, IPentahoSession session) {
    
    IMondrianCatalogService mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, "IMondrianCatalogService", session); //$NON-NLS-1$
    
    String dsUrl = PentahoSystem.getApplicationContext().getBaseUrl();
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
