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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created May 7, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.agilebi.modeler.geo.GeoContextPropertiesProvider;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.InlineEtlPhysicalModel;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.repository.InMemoryMetadataDomainRepository;
import org.pentaho.metadata.util.SQLModelGenerator;
import org.pentaho.metadata.util.SQLModelGeneratorException;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.QueryValidationException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceInMemoryServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.util.messages.LocaleHelper;

import com.thoughtworks.xstream.XStream;

/*
 * TODO mlowery This class professes to be a datasource service yet it takes as inputs both IDatasource instances and 
 * lower-level BusinessData instances. (BusinessData instances are stored in IDatasources.) They are not currently being
 * kept in sync. I propose that the service only deals with IDatasources from a caller perspective.
 */
public class InMemoryDatasourceServiceImpl implements IDatasourceService {

  public static final IMetadataDomainRepository METADATA_DOMAIN_REPO = new InMemoryMetadataDomainRepository();

  private static final Log logger = LogFactory.getLog(InMemoryDatasourceServiceImpl.class);

  public static final String DEFAULT_UPLOAD_FILEPATH_FILE_NAME = "debug_upload_filepath.properties"; //$NON-NLS-1$

  public static final String UPLOAD_FILE_PATH = "upload.file.path"; //$NON-NLS-1$
  
  private static final String BEFORE_QUERY = " SELECT * FROM (";

  private static final String AFTER_QUERY = ")";


  private IMetadataDomainRepository metadataDomainRepository;
  private IConnectionService connectionService;


  public InMemoryDatasourceServiceImpl() {
    this(new InMemoryConnectionServiceImpl());
  }
  public InMemoryDatasourceServiceImpl(IConnectionService connectionService) {
    this.connectionService = connectionService;
    // this needs to share the same one as MQL editor...
    metadataDomainRepository = METADATA_DOMAIN_REPO;
  }

  public boolean deleteLogicalModel(String domainId, String modelName) throws DatasourceServiceException {
    try {
      metadataDomainRepository.removeModel(domainId, modelName);
    } catch (DomainStorageException dse) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0017_UNABLE_TO_STORE_DOMAIN", domainId),
          dse);
      throw new DatasourceServiceException(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainId), dse); //$NON-NLS-1$      
    } catch (DomainIdNullException dne) {
      logger.error(Messages.getErrorString("DatasourceServiceDelegate.ERROR_0019_DOMAIN_IS_NULL"), dne);
      throw new DatasourceServiceException(Messages
          .getErrorString("InMemoryDatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
    return true;
  }

  protected List<String> getPermittedRoleList() {
    DebugDataAccessViewPermissionHandler dataAccessViewPermHandler = new DebugDataAccessViewPermissionHandler();
    return dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession());
  }

  protected List<String> getPermittedUserList() {
    DebugDataAccessViewPermissionHandler dataAccessViewPermHandler = new DebugDataAccessViewPermissionHandler();
    return dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession());
  }

  protected int getDefaultAcls() {
    DebugDataAccessViewPermissionHandler dataAccessViewPermHandler = new DebugDataAccessViewPermissionHandler();
    return dataAccessViewPermHandler.getDefaultAcls(PentahoSessionHolder.getSession());
  }

  private IPentahoResultSet executeQuery(String connectionName, String query, String previewLimit)
      throws QueryValidationException {
    SQLConnection sqlConnection = null;
    int limit = (previewLimit != null && previewLimit.length() > 0) ? Integer.parseInt(previewLimit) : -1;
    try {
      sqlConnection = DatasourceInMemoryServiceHelper.getConnection(connectionName);
      sqlConnection.setMaxRows(limit);
      sqlConnection.setReadOnly(true);
      return sqlConnection.executeQuery(BEFORE_QUERY + query + AFTER_QUERY);
    } catch (Exception e) {
      logger.error(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new QueryValidationException(e.getLocalizedMessage(), e); //$NON-NLS-1$      
    } finally {
      if (sqlConnection != null) {
        sqlConnection.close();
      }
    }

  }

  public SerializedResultSet doPreview(String connectionName, String query, String previewLimit)
      throws DatasourceServiceException {
    SerializedResultSet returnResultSet;
    try {
      executeQuery(connectionName, query, previewLimit);
      returnResultSet = DatasourceInMemoryServiceHelper.getSerializeableResultSet(connectionName, query,
          Integer.parseInt(previewLimit), null);
    } catch (QueryValidationException e) {
      logger.error(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e);//$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()), e); //$NON-NLS-1$      
    }
    return returnResultSet;

  }

  public boolean testDataSourceConnection(String connectionName) throws DatasourceServiceException {
    java.sql.Connection conn = null;
    try {
      conn = DatasourceInMemoryServiceHelper.getDataSourceConnection(connectionName);
    } catch (DatasourceServiceException dme) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0026_UNABLE_TO_TEST_CONNECTION",
          connectionName), dme);
      throw new DatasourceServiceException(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connectionName), dme); //$NON-NLS-1$
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
        logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0026_UNABLE_TO_TEST_CONNECTION",
            connectionName), e);
        throw new DatasourceServiceException(Messages.getErrorString(
            "InMemoryDatasourceServiceImpl.ERROR_0026_UNABLE_TO_TEST_CONNECTION", connectionName), e); //$NON-NLS-1$
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
    try {
      executeQuery(connectionName, query, previewLimit);

      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
          || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SerializedResultSet resultSet = DatasourceInMemoryServiceHelper.getSerializeableResultSet(connectionName, query,
          Integer.parseInt(previewLimit), null);
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(modelName, connectionName, dbType,
           resultSet.getColumnTypes(), resultSet.getColumns(),query, securityEnabled,
          getPermittedRoleList(), getPermittedUserList(), getDefaultAcls(), "joe");
      Domain domain = sqlModelGenerator.generate();
      return new BusinessData(domain, resultSet.getData());
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
    }
  }

  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository(IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public BusinessData loadBusinessData(String domainId, String modelId) throws DatasourceServiceException {
    Domain domain = getMetadataDomainRepository().getDomain(domainId);
    List<List<String>> data = null;
    if (domain.getPhysicalModels().get(0) instanceof InlineEtlPhysicalModel) {
      InlineEtlPhysicalModel model = (InlineEtlPhysicalModel) domain.getPhysicalModels().get(0);
      data = DatasourceInMemoryServiceHelper.getCsvDataSample(model.getFileLocation(), model.getHeaderPresent(), model
          .getDelimiter(), model.getEnclosure(), 5);
    } else {
      SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
      String query = model.getPhysicalTables().get(0).getTargetTable();
      SerializedResultSet resultSet = DatasourceInMemoryServiceHelper.getSerializeableResultSet(model.getDatasource().getDatabaseName(), query, 5,null);
      data = resultSet.getData();
    }
    return new BusinessData(domain, data);
  }

  public boolean saveLogicalModel(Domain domain, boolean overwrite) throws DatasourceServiceException {
    String domainName = domain.getId();
    try {
      getMetadataDomainRepository().storeDomain(domain, overwrite);
      return true;
    } catch (DomainStorageException dse) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0017_UNABLE_TO_STORE_DOMAIN",
          domainName), dse);
      throw new DatasourceServiceException(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainName), dse); //$NON-NLS-1$      
    } catch (DomainAlreadyExistsException dae) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0018_DOMAIN_ALREADY_EXIST",
          domainName), dae);
      throw new DatasourceServiceException(Messages.getErrorString(
          "InMemoryDatasourceServiceImpl.ERROR_0018_DOMAIN_ALREADY_EXIST", domainName), dae); //$NON-NLS-1$      
    } catch (DomainIdNullException dne) {
      logger.error(Messages.getErrorString("InMemoryDatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL"), dne);
      throw new DatasourceServiceException(Messages
          .getErrorString("InMemoryDatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL"), dne); //$NON-NLS-1$      
    }
  }

  private String getUploadFilePath() throws DatasourceServiceException {
    try {
      URL url = ClassLoader.getSystemResource(DEFAULT_UPLOAD_FILEPATH_FILE_NAME);
      URI uri = url.toURI();
      File file = new File(uri);
      FileInputStream fis = new FileInputStream(file);
      try {
      Properties properties = new Properties();
      properties.load(fis);
      return (String) properties.get(UPLOAD_FILE_PATH);
      } finally {
        fis.close();
      }
    } catch (Exception e) {
      throw new DatasourceServiceException(e);
    }
  }

  public boolean hasPermission() {
    return true;
  }

  public List<LogicalModelSummary> getLogicalModels(String context) throws DatasourceServiceException {
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
        String vis = (String) model.getProperty("visible");
        if(vis != null){
          String[] visibleContexts = vis.split(",");
          boolean visibleToContext = false;
          for(String c : visibleContexts){
            if(context.equals(c.trim())){
              visibleToContext = true;
              break;
            }
          }
          if(!visibleToContext){
            continue;
          }
        }
        logicalModelSummaries.add(new LogicalModelSummary(domainId, model.getId(), model.getName().getString(locale)));
      }
    }
    return logicalModelSummaries;
  }

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

  public String serializeModelState(DatasourceDTO dto) throws DatasourceServiceException {
    XStream xs = new XStream();
    return xs.toXML(dto);
  }

  public DatasourceDTO deSerializeModelState(String dtoStr) throws DatasourceServiceException {
    try{
      XStream xs = new XStream();
      return (DatasourceDTO) xs.fromXML(dtoStr);
    } catch(Exception e){
      e.printStackTrace();
      throw new DatasourceServiceException(e);
    }
  }

  @Override
  public List<String> listDatasourceNames() throws IOException {
    return new ArrayList<String>();
  }

  @Override
  public QueryDatasourceSummary generateQueryDomain(String name, String query, Connection connection, DatasourceDTO datasourceDTO) throws DatasourceServiceException {

    ModelerWorkspace modelerWorkspace = new ModelerWorkspace(new GwtModelerWorkspaceHelper(), getGeoContext());
    ModelerService modelerService = new ModelerService();
    modelerWorkspace.setModelName(name);

    try {

      Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
          || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
      SerializedResultSet resultSet = DatasourceInMemoryServiceHelper.getSerializeableResultSet(connection.getName(), query,
          Integer.parseInt("10"), null);
      SQLModelGenerator sqlModelGenerator = new SQLModelGenerator(name, connection.getName(), connectionService.convertFromConnection(connection).getDatabaseType().getShortName(),
           resultSet.getColumnTypes(), resultSet.getColumns(),query, securityEnabled,
          getPermittedRoleList(), getPermittedUserList(), getDefaultAcls(), "joe");
      Domain domain = sqlModelGenerator.generate();
      modelerWorkspace.setDomain(domain);


      modelerWorkspace.getWorkspaceHelper().autoModelFlat(modelerWorkspace);
      modelerWorkspace.setModelName(datasourceDTO.getDatasourceName());
      modelerWorkspace.getWorkspaceHelper().populateDomain(modelerWorkspace);
      domain.getLogicalModels().get(0).setProperty("datasourceModel", serializeModelState(datasourceDTO));
      domain.getLogicalModels().get(0).setProperty("DatasourceType", "SQL-DS");

      QueryDatasourceSummary summary = new QueryDatasourceSummary();
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

  public String getDatasourceIllegalCharacters() throws DatasourceServiceException {
    return "$<>?&#%^*()!~:;[]{}|" ;
  }

  public GeoContext getGeoContext() throws DatasourceServiceException {
    try {
      Properties props = new Properties();
      props.load(new FileInputStream(new File("test-res/geoContextSample.properties")));
      GeoContext geo = GeoContextFactory.create(new GeoContextPropertiesProvider(props));
      return geo;
    } catch (ModelerException e) {
      throw new DatasourceServiceException(e);
    } catch (FileNotFoundException e) {
      throw new DatasourceServiceException(e);
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return null;
  }

}
