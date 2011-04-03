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
 * Copyright 2008 - 2010 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created June 4, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.tree.DefaultElement;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.InlineEtlPhysicalModel;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.SQLModelGenerator;
import org.pentaho.metadata.util.SQLModelGeneratorException;
import org.pentaho.platform.api.engine.*;
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.FileUtils;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.QueryValidationException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.CsvTransformGenerator;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.uifoundation.component.xml.PMDUIComponent;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.messages.LocaleHelper;

import com.thoughtworks.xstream.XStream;
import org.pentaho.platform.util.web.SimpleUrlFactory;

public class DatasourceServiceImpl implements IDatasourceService {

  private static final Log logger = LogFactory.getLog(DatasourceServiceImpl.class);

  private IDataAccessPermissionHandler dataAccessPermHandler;

  private IDataAccessViewPermissionHandler dataAccessViewPermHandler;

  private IMetadataDomainRepository metadataDomainRepository;

  private static final String BEFORE_QUERY = " SELECT * FROM ("; //$NON-NLS-1$

  private static final String AFTER_QUERY = ") tbl"; //$NON-NLS-1$

  private IConnectionService connectionService;

  public DatasourceServiceImpl() {
    this(new ConnectionServiceImpl());
  }
  public DatasourceServiceImpl(IConnectionService connectionService) {
    this.connectionService = connectionService;
    metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, null);
    String dataAccessClassName = null;
    try {
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
      dataAccessClassName = resLoader
          .getPluginSetting(
              getClass(),
              "settings/data-access-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessPermissionHandler"); //$NON-NLS-1$ //$NON-NLS-2$
      Class<?> clazz = Class.forName(dataAccessClassName);
      Constructor<?> defaultConstructor = clazz.getConstructor(new Class[] {});
      dataAccessPermHandler = (IDataAccessPermissionHandler) defaultConstructor.newInstance();
    } catch (Exception e) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0007_DATAACCESS_PERMISSIONS_INIT_ERROR"), e); //$NON-NLS-1$
      // TODO: Unhardcode once this is an actual plugin
      dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
    }
    String dataAccessViewClassName = null;
    try {
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
      dataAccessViewClassName = resLoader
          .getPluginSetting(
              getClass(),
              "settings/data-access-view-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler"); //$NON-NLS-1$ //$NON-NLS-2$
      Class<?> clazz = Class.forName(dataAccessViewClassName);
      Constructor<?> defaultConstructor = clazz.getConstructor(new Class[] {});
      dataAccessViewPermHandler = (IDataAccessViewPermissionHandler) defaultConstructor.newInstance();
    } catch (Exception e) {
      logger.error(
          Messages.getErrorString("DatasourceServiceImpl.ERROR_0030_DATAACCESS_VIEW_PERMISSIONS_INIT_ERROR"), e); //$NON-NLS-1$
      // TODO: Unhardcode once this is an actual plugin
      dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
    }

  }

  protected boolean hasDataAccessPermission() {
    return dataAccessPermHandler != null
        && dataAccessPermHandler.hasDataAccessPermission(PentahoSessionHolder.getSession());
  }

  protected boolean hasDataAccessViewPermission() {
    return dataAccessViewPermHandler != null
        && dataAccessViewPermHandler.hasDataAccessViewPermission(PentahoSessionHolder.getSession());
  }

  protected List<String> getPermittedRoleList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession());
  }

  protected List<String> getPermittedUserList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession());
  }

  protected int getDefaultAcls() {
    if (dataAccessViewPermHandler == null) {
      return -1;
    }
    return dataAccessViewPermHandler.getDefaultAcls(PentahoSessionHolder.getSession());
  }

  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      return false;
    }
    String catalogRef = null;
    String targetTable = null;
    try {
      // first load the model
      Domain domain = metadataDomainRepository.getDomain(domainId);
      LogicalModel logicalModel = domain.getLogicalModels().get(0);
      String modelState = (String) logicalModel.getProperty("datasourceModel");
      DatasourceDTO datasource = null;

      if(modelState != null){
        datasource = deSerializeModelState(modelState);
      }
    	
      // if CSV, drop the staged table
      // TODO: use the edit story's stored info to do this
      if ("CSV".equals(logicalModel.getProperty("DatasourceType")) ||
          "true".equalsIgnoreCase((String)logicalModel.getProperty( LogicalModel.PROPERTY_TARGET_TABLE_STAGED ))) {
        targetTable = ((SqlPhysicalTable)domain.getPhysicalModels().get(0).getPhysicalTables().get(0)).getTargetTable();
        CsvTransformGenerator csvTransformGenerator = new CsvTransformGenerator(datasource.getCsvModelInfo(), AgileHelper.getDatabaseMeta());
        try {
          csvTransformGenerator.dropTable(targetTable);
        } catch (CsvTransformGeneratorException e) {
            // table might not be there, it's OK that is what we were trying to do anyway
            logger.warn(Messages.getErrorString(
              "DatasourceServiceImpl.ERROR_0019_UNABLE_TO_DROP_TABLE", targetTable, domainId, e.getLocalizedMessage()), e);//$NON-NLS-1$
        }
      }

      // if associated mondrian file, delete
      if (logicalModel.getProperty("MondrianCatalogRef") != null) {
        // remove Mondrian schema
        IMondrianCatalogService service = PentahoSystem.get(IMondrianCatalogService.class, null);
        catalogRef = (String)logicalModel.getProperty("MondrianCatalogRef");
        service.removeCatalog(catalogRef, PentahoSessionHolder.getSession());
      }

      metadataDomainRepository.removeModel(domainId, modelName);
      if(datasource != null){
        String fileName = datasource.getCsvModelInfo().getFileInfo().getFilename();
        FileUtils fileService = new FileUtils();
        if(fileName != null) {
          fileService.deleteFile(fileName);
        }
      }
    } catch (MondrianCatalogServiceException me) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0020_UNABLE_TO_DELETE_CATALOG", catalogRef, domainId, me.getLocalizedMessage()), me);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0020_UNABLE_TO_DELETE_CATALOG", catalogRef, domainId, me.getLocalizedMessage()), me); //$NON-NLS-1$      

    } catch (DomainStorageException dse) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0017_UNABLE_TO_STORE_DOMAIN", domainId, dse.getLocalizedMessage()), dse);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainId, dse.getLocalizedMessage()), dse); //$NON-NLS-1$      
    } catch (DomainIdNullException dne) {
      logger.error(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne); //$NON-NLS-1$      
    }
    return true;
  }

  private IPentahoResultSet executeQuery(String connectionName, String query, String previewLimit) throws QueryValidationException {
    SQLConnection sqlConnection = null;
    try {
      int limit = (previewLimit != null && previewLimit.length() > 0) ? Integer.parseInt(previewLimit) : -1;
      sqlConnection = (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE,
          connectionName, PentahoSessionHolder.getSession(), new SimpleLogger(DatasourceServiceHelper.class.getName()));
      sqlConnection.setMaxRows(limit);
      sqlConnection.setReadOnly(true);
      return sqlConnection.executeQuery(BEFORE_QUERY + query + AFTER_QUERY);
    } catch (Exception e) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new QueryValidationException(e.getLocalizedMessage(), e);
    } finally {
      if (sqlConnection != null) {
        sqlConnection.close();
      }
    }
  }

  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit)
      throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }
    SerializedResultSet returnResultSet;
    try {
      executeQuery(connectionName, query, previewLimit);
      returnResultSet = DatasourceServiceHelper.getSerializeableResultSet(connectionName, query,
          Integer.parseInt(previewLimit), PentahoSessionHolder.getSession());
    } catch (QueryValidationException e) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e); //$NON-NLS-1$      
    }
    return returnResultSet;

  }

  public boolean testDataSourceConnection(String connectionName) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }
    Connection conn = null;
    try {
      conn = DatasourceServiceHelper.getDataSourceConnection(connectionName, PentahoSessionHolder.getSession());
      if (conn == null) {
        logger.error(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName));//$NON-NLS-1$
        throw new DatasourceServiceException(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName)); //$NON-NLS-1$
      }
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName, e.getLocalizedMessage()), e);//$NON-NLS-1$
        throw new DatasourceServiceException(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName, e.getLocalizedMessage()), e); //$NON-NLS-1$
      }
    }
    return true;
  }

  /**
   * This method gets the business data which are the business columns, columns types and sample preview data
   * 
   * @param modelName, connection, query, previewLimit
   * @return BusinessData
   * @throws DatasourceServiceException
   */

  public BusinessData generateLogicalModel(String modelName, String connectionName, String dbType, String query, String previewLimit)
      throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }
    try {
      // Testing whether the query is correct or not
      executeQuery(connectionName, query, previewLimit);
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
          || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SerializedResultSet resultSet = DatasourceServiceHelper.getSerializeableResultSet(connectionName, query,
          Integer.parseInt(previewLimit), PentahoSessionHolder.getSession());
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connectionName, dbType, resultSet.getColumnTypes(), resultSet.getColumns(), query,
          securityEnabled, getPermittedRoleList(), getPermittedUserList(), getDefaultAcls(), (PentahoSessionHolder
              .getSession() != null) ? PentahoSessionHolder.getSession().getName() : null);
      Domain domain = sqlModelGenerator.generate();
      return new BusinessData(domain, resultSet.getData());
    } catch (SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", smge.getLocalizedMessage()), smge);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", smge.getLocalizedMessage()), smge); //$NON-NLS-1$
    } catch (QueryValidationException e) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e); //$NON-NLS-1$
    }
   }

  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository(IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException {
    if (!hasDataAccessPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED"));//$NON-NLS-1$
    }

    String domainName = domain.getId();
    try {
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return true;
    } catch (DomainStorageException dse) {
      logger.error(Messages.getErrorString(
            "DatasourceServiceImpl.ERROR_0012_UNABLE_TO_STORE_DOMAIN", domainName, dse.getLocalizedMessage()));//$NON-NLS-1$
      logger.error("error", dse); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0012_UNABLE_TO_STORE_DOMAIN", domainName, dse.getLocalizedMessage()), dse); //$NON-NLS-1$      
    } catch (DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0013_DOMAIN_ALREADY_EXIST", domainName, dae.getLocalizedMessage()));//$NON-NLS-1$
      logger.error("error", dae); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0013_DOMAIN_ALREADY_EXIST", domainName, dae.getLocalizedMessage()), dae); //$NON-NLS-1$      
    } catch (DomainIdNullException dne) {
      logger.error(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0014_DOMAIN_IS_NULL", dne.getLocalizedMessage()));//$NON-NLS-1$
      logger.error("error", dne); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0014_DOMAIN_IS_NULL", dne.getLocalizedMessage()), dne); //$NON-NLS-1$      
    }
  }

  public boolean hasPermission() {
    if (PentahoSessionHolder.getSession() != null) {
      return (SecurityHelper.isPentahoAdministrator(PentahoSessionHolder.getSession()) || hasDataAccessPermission());
    } else {
      return false;
    }
  }

  public List<LogicalModelSummary> getLogicalModels() throws DatasourceServiceException {
    if (!hasDataAccessViewPermission()) {
      logger.error(Messages.getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages
          .getErrorString("DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED")); //$NON-NLS-1$
    }
    List<LogicalModelSummary> logicalModelSummaries = new ArrayList<LogicalModelSummary>();
    for (String domainId : getMetadataDomainRepository().getDomainIds()) {
      Domain domain = getMetadataDomainRepository().getDomain(domainId);

      String locale = LocaleHelper.getLocale().toString();
      String locales[] = new String[domain.getLocales().size()];
      for (int i = 0; i < domain.getLocales().size(); i++) {
        locales[i] = domain.getLocales().get(i).getCode();
      }
      locale = LocaleHelper.getClosestLocale(locale, locales);

      for (LogicalModel model : domain.getLogicalModels()) {
        logicalModelSummaries.add(new LogicalModelSummary(domainId, model.getId(), model.getName(locale)));
      }
    }
    return logicalModelSummaries;
  }

  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException {
    Domain domain = getMetadataDomainRepository().getDomain(domainId);
    List<List<String>> data = null;
    if (domain.getPhysicalModels().get(0) instanceof InlineEtlPhysicalModel) {
      InlineEtlPhysicalModel model = (InlineEtlPhysicalModel) domain.getPhysicalModels().get(0);

      String relativePath = PentahoSystem.getSystemSetting(
          "file-upload-defaults/relative-path", String.valueOf(CsvTransformGenerator.DEFAULT_RELATIVE_UPLOAD_FILE_PATH)); //$NON-NLS-1$
      String csvFileLoc = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);

      data = DatasourceServiceHelper.getCsvDataSample(csvFileLoc + model.getFileLocation(), model.getHeaderPresent(),
          model.getDelimiter(), model.getEnclosure(), 5);
    } else {
      SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
      String query = model.getPhysicalTables().get(0).getTargetTable();
      SerializedResultSet resultSet = DatasourceServiceHelper.getSerializeableResultSet(model.getDatasource().getDatabaseName(), query, 5,
          PentahoSessionHolder.getSession());
      data = resultSet.getData();
    }
    return new BusinessData(domain, data);
  }

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

  public String serializeModelState(DatasourceDTO dto) throws DatasourceServiceException {
    XStream xstream = new XStream();
    return xstream.toXML(dto);
  }

  public DatasourceDTO deSerializeModelState(String dtoStr) throws DatasourceServiceException {
    XStream xs = new XStream();
    return (DatasourceDTO) xs.fromXML(dtoStr);
  }


  private IPentahoSession getSession() {
    IPentahoSession session = null;
    IPentahoObjectFactory pentahoObjectFactory = PentahoSystem.getObjectFactory();
    if (pentahoObjectFactory != null) {
      try {
        session = pentahoObjectFactory.get(IPentahoSession.class, "systemStartupSession", null); //$NON-NLS-1$
      } catch (ObjectFactoryException e) {
        logger.error(e);
      }
    }
    return session;
  }
  
  public List<String> listDatasourceNames() throws IOException {
	  IPentahoUrlFactory urlFactory = new SimpleUrlFactory(""); //$NON-NLS-1$
	  PMDUIComponent component = new PMDUIComponent(urlFactory, new ArrayList());
	  component.validate(getSession(), null);
	  component.setAction(PMDUIComponent.ACTION_LIST_MODELS);
	  Document document = component.getXmlContent();
	  List<DefaultElement> modelElements = document.selectNodes("//model_name"); //$NON-NLS-1$

	  ArrayList<String> datasourceNames = new ArrayList<String>();
	  for(DefaultElement element : modelElements) {
		  datasourceNames.add(element.getText());
	  }
	  return datasourceNames;
  }

  @Override
  public QueryDatasourceSummary generateQueryDomain(String name, String query, String datasourceId, IConnection connection, DatasourceDTO datasourceDTO) throws DatasourceServiceException {

    ModelerWorkspace modelerWorkspace = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
    ModelerService modelerService = new ModelerService();
    modelerWorkspace.setModelName(name);

    try {
      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
          || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SerializedResultSet resultSet = DatasourceServiceHelper.getSerializeableResultSet(connection.getName(), query,
          10, PentahoSessionHolder.getSession());
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(name, connection.getName(), connectionService.convertFromConnection(connection).getDatabaseType().getShortName(), resultSet.getColumnTypes(), resultSet.getColumns(), query,
          securityEnabled, getPermittedRoleList(), getPermittedUserList(), getDefaultAcls(), (PentahoSessionHolder
              .getSession() != null) ? PentahoSessionHolder.getSession().getName() : null);
      Domain domain = sqlModelGenerator.generate();
      modelerWorkspace.setDomain(domain);


      modelerWorkspace.getWorkspaceHelper().autoModelFlat(modelerWorkspace);
      modelerWorkspace.setModelName(datasourceDTO.getDatasourceName());
      modelerWorkspace.getWorkspaceHelper().populateDomain(modelerWorkspace);
      domain.getLogicalModels().get(0).setProperty("datasourceModel", serializeModelState(datasourceDTO));
      domain.getLogicalModels().get(0).setProperty("DatasourceType", "SQL-DS");

      QueryDatasourceSummary summary = new QueryDatasourceSummary();
      prepareForSerializaton(domain);
      modelerService.serializeModels(domain, modelerWorkspace.getModelName());
      summary.setDomain(domain);

      return summary;
    } catch (SQLModelGeneratorException smge) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0016_UNABLE_TO_GENERATE_MODEL",
          smge.getLocalizedMessage()), smge);
      throw new DatasourceServiceException(Messages
          .getErrorString("InMemoryDatasourceServiceImpl.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), smge); //$NON-NLS-1$
    } catch (QueryValidationException e) {
      logger.error(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e); //$NON-NLS-1$
    } catch (ModelerException e) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0016_UNABLE_TO_GENERATE_MODEL",
          e.getLocalizedMessage()), e);
      throw new DatasourceServiceException(Messages
          .getErrorString("InMemoryDatasourceServiceImpl.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), e); //$NON-NLS-1$
    } catch (Exception e) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0016_UNABLE_TO_GENERATE_MODEL",
          e.getLocalizedMessage()), e);
      throw new DatasourceServiceException(Messages
          .getErrorString("InMemoryDatasourceServiceImpl.ERROR_0015_UNABLE_TO_GENERATE_MODEL"), e); //$NON-NLS-1$
    }

  }
  
  public void prepareForSerializaton(Domain domain) {
		/*
		 * This method is responsible for cleaning up legacy information when
		 * changing datasource types and also manages CSV files for CSV based
		 * datasources.
		 */

		String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(FileUtils.DEFAULT_RELATIVE_UPLOAD_FILE_PATH)); //$NON-NLS-1$
		String path = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
		LogicalModel logicalModel = domain.getLogicalModels().get(0);
		String modelState = (String) logicalModel.getProperty("datasourceModel"); 

		if (modelState != null) {
			XStream xs = new XStream();
			DatasourceDTO datasource = (DatasourceDTO) xs.fromXML(modelState);
			CsvFileInfo csvFileInfo = datasource.getCsvModelInfo().getFileInfo();
			String csvFileName = csvFileInfo.getFilename();

			if (csvFileName != null) {

				// Cleanup logic when updating from CSV datasource to SQL
				// datasource.
				csvFileInfo.setFilename(null);
				csvFileInfo.setTmpFilename(null);
				csvFileInfo.setFriendlyFilename(null);
				csvFileInfo.setContents(null);
				csvFileInfo.setEncoding(null);

				// Delete CSV file.
				File csvFile = new File(path + File.separatorChar + csvFileName);
				if (csvFile.exists()) {
					csvFile.delete();
				}

				// Delete STAGING database table.
				CsvTransformGenerator csvTransformGenerator = new CsvTransformGenerator(datasource.getCsvModelInfo(), AgileHelper.getDatabaseMeta());
				try {
					csvTransformGenerator.dropTable(datasource.getCsvModelInfo().getStageTableName());
				} catch (CsvTransformGeneratorException e) {
					logger.error(e);
				}
			}
			// Update datasourceModel with the new modelState
			modelState = xs.toXML(datasource);
		    logicalModel.setProperty("datasourceModel", modelState);
		}
	}
}
