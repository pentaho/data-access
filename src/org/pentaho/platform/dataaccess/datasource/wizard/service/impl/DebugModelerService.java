package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.concept.types.LocalizedString;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IApplicationContext;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.ObjectFactoryException;
import org.pentaho.platform.api.repository.ISolutionRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.platform.engine.core.solution.ActionInfo;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.springframework.context.ApplicationContext;

/**
 * User: nbaker
 * Date: Jul 16, 2010
 */
public class DebugModelerService extends ModelerService {

  private IPentahoSession getSession() {
    IPentahoSession session = null;
    IPentahoObjectFactory pentahoObjectFactory = PentahoSystem.getObjectFactory();
    if (pentahoObjectFactory != null) {
      try {
        session = pentahoObjectFactory.get(IPentahoSession.class, "systemStartupSession", null); //$NON-NLS-1$
      } catch (ObjectFactoryException e) {
        e.printStackTrace();
      }
    }
    return session;
  }

  public String serializeModels(Domain domain, String name) throws Exception {
    String domainId;
    PentahoSystemHelper.init();
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

      IApplicationContext appContext = PentahoSystem.getApplicationContext();
      if (appContext != null) {
        path = PentahoSystem.getApplicationContext().getSolutionPath(path);
      }

      File pathDir = new File(path);
      if (!pathDir.exists()) {
        pathDir.mkdirs();
      }

      IPentahoSession session = getSession();

      ISolutionRepository repository = PentahoSystem.get(ISolutionRepository.class, session);

      // Keep a reference to the mondrian catalog
      model.getWorkspaceHelper().populateDomain(model);

      LogicalModel lModel = domain.getLogicalModels().get(0);
      String catName = lModel.getName(LocalizedString.DEFAULT_LOCALE);
      lModel.setProperty("MondrianCatalogRef", catName); //$NON-NLS-1$
      XmiParser parser = new XmiParser();
      String reportXML =  parser.generateXmi(model.getDomain());

      // Serialize domain to xmi.
      String base = PentahoSystem.getApplicationContext().getSolutionRootPath();
      String parentPath = ActionInfo.buildSolutionPath(solutionStorage, metadataLocation, ""); //$NON-NLS-1$
      int status = repository.publish(base, '/' + parentPath, name + ".xmi", reportXML.getBytes("UTF-8"), true); //$NON-NLS-1$ //$NON-NLS-2$
      if (status != ISolutionRepository.FILE_ADD_SUCCESSFUL) {
        throw new RuntimeException("Unable to save to repository. Status: " + status); //$NON-NLS-1$
      }

      // Serialize domain to olap schema.
      MondrianModelExporter exporter = new MondrianModelExporter(lModel, LocalizedString.DEFAULT_LOCALE);
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
      
     
    } catch (Exception e) {
      getLogger().error(e);
      throw e;
    }
    return domainId;
  }
  
  private void addCatalog(String catName, String catConnectStr, String catDef, IPentahoSession session) {
   // Do nothing.   
  }
}
