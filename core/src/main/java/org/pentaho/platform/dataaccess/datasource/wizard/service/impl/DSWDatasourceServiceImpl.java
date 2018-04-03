/*!
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
* Copyright (c) 2002-2018 Hitachi Vantara. All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.tree.DefaultElement;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
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
import org.pentaho.platform.api.engine.IPentahoUrlFactory;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.utils.DataAccessPermissionUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.FileUtils;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.QueryValidationException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.SqlQueriesNotSupportedException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.CsvTransformGenerator;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.DatasourceServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.UtilHtmlSanitizer;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

import org.pentaho.platform.uifoundation.component.xml.PMDUIComponent;
import org.pentaho.platform.util.logging.SimpleLogger;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.platform.util.web.SimpleUrlFactory;

import com.thoughtworks.xstream.XStream;

public class DSWDatasourceServiceImpl implements IDSWDatasourceService {

  private static final Log logger = LogFactory.getLog( DSWDatasourceServiceImpl.class );

  /**
   * property which can be saved in  {@link org.pentaho.metadata.model.LogicalModel} 
   */
  protected static final String LM_PROP_DATASOURCE_TYPE = "DatasourceType";

  /**
   * property which can be saved in  {@link org.pentaho.metadata.model.LogicalModel} 
   */
  protected static final String LM_PROP_MONDRIAN_CATALOG_REF = "MondrianCatalogRef";

  /**
   * property which can be saved in  {@link org.pentaho.metadata.model.LogicalModel} 
   */
  protected static final String LM_PROP_DATASOURCE_MODEL = "datasourceModel";

  /**
   * property which can be saved in  {@link org.pentaho.metadata.model.LogicalModel} 
   */
  protected static final String LM_PROP_VISIBLE = "visible";

  private static final String DB_TYPE_ID_PENTAHO_DATA_SERVICE = "Pentaho Data Services";

  private IMetadataDomainRepository metadataDomainRepository;

  private static final String BEFORE_QUERY = " SELECT * FROM ("; //$NON-NLS-1$

  private static final String AFTER_QUERY = ") tbl"; //$NON-NLS-1$

  private GeoContext geoContext;

  private ConnectionServiceImpl connService;

  public DSWDatasourceServiceImpl() {
    this( new ConnectionServiceImpl() );
  }

  public DSWDatasourceServiceImpl( ConnectionServiceImpl connService ) {
    metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, null );
    this.connService = connService;
  }

  protected boolean hasDataAccessPermission() {
    return DataAccessPermissionUtil.hasManageAccess();
  }

  protected boolean hasDataAccessViewPermission() {
    return DataAccessPermissionUtil.hasViewAccess();
  }

  protected List<String> getPermittedRoleList() {
    return DataAccessPermissionUtil.getPermittedViewRoleList();
  }

  protected List<String> getPermittedUserList() {
    return DataAccessPermissionUtil.getPermittedViewUserList();
  }

  protected int getDefaultAcls() {
    return DataAccessPermissionUtil.getDataAccessViewPermissionHandler().getDefaultAcls( PentahoSessionHolder.getSession() );
  }

  protected ModelerWorkspace createModelerWorkspace() {
    return new ModelerWorkspace( new GwtModelerWorkspaceHelper() );
  }

  protected ModelerService createModelerService() {
    return new ModelerService();
  }

  public boolean deleteLogicalModel( String domainId, String modelName ) throws DatasourceServiceException {
    if ( !hasDataAccessPermission() ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
      return false;
    }
    String catalogRef = null;
    String targetTable = null;
    try {
      // first load the model
      Domain domain = getMetadataDomainRepository().getDomain( domainId );
      ModelerWorkspace model = createModelerWorkspace();
      model.setDomain( domain );
      LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
      if ( logicalModel == null ) {
        logicalModel = model.getLogicalModel( ModelerPerspective.REPORTING );
      }
      LogicalModel logicalModelRep = model.getLogicalModel( ModelerPerspective.REPORTING );
      //CSV related data is bounded to reporting model so need to perform some additional clean up here
      if ( logicalModelRep != null ) {
        String modelState = (String) logicalModelRep.getProperty( LM_PROP_DATASOURCE_MODEL );

        // if CSV, drop the staged table
        // TODO: use the edit story's stored info to do this
        if ( "CSV".equals( logicalModelRep.getProperty( LM_PROP_DATASOURCE_TYPE ) )
          || "true".equalsIgnoreCase( (String) logicalModelRep.getProperty( LogicalModel.PROPERTY_TARGET_TABLE_STAGED ) ) ) {
          targetTable = ( (SqlPhysicalTable) domain.getPhysicalModels().get( 0 ).getPhysicalTables().get( 0 ) ).getTargetTable();
          DatasourceDTO datasource = null;

          if ( modelState != null ) {
            datasource = deSerializeModelState( modelState );
          }
          if ( datasource != null ) {
            CsvTransformGenerator csvTransformGenerator =
              new CsvTransformGenerator( datasource.getCsvModelInfo(), AgileHelper.getDatabaseMeta() );
            try {
              csvTransformGenerator.dropTable( targetTable );
            } catch ( CsvTransformGeneratorException e ) {
              // table might not be there, it's OK that is what we were trying to do anyway
              logger.warn( Messages.getErrorString(
                "DatasourceServiceImpl.ERROR_0019_UNABLE_TO_DROP_TABLE", targetTable, domainId,
                e.getLocalizedMessage() ), e ); //$NON-NLS-1$
            }
            String fileName = datasource.getCsvModelInfo().getFileInfo().getFilename();
            FileUtils fileService = new FileUtils();
            if ( fileName != null ) {
              fileService.deleteFile( fileName );
            }
          }
        }
      }

      // if associated mondrian file, delete
      if ( logicalModel.getProperty( LM_PROP_MONDRIAN_CATALOG_REF ) != null ) {
        // remove Mondrian schema
        IMondrianCatalogService service = PentahoSystem.get( IMondrianCatalogService.class, null );
        catalogRef = (String) logicalModel.getProperty( LM_PROP_MONDRIAN_CATALOG_REF );
        // check if the model is not already removed
        if ( service.getCatalog( catalogRef, PentahoSessionHolder.getSession() ) != null ) {
          service.removeCatalog( catalogRef, PentahoSessionHolder.getSession() );
        }
      }

      getMetadataDomainRepository().removeModel( domainId, logicalModel.getId() );

      if ( logicalModelRep != null && !logicalModelRep.getId().equals( logicalModel.getId() ) ) {
        getMetadataDomainRepository().removeModel( domainId, logicalModelRep.getId() );
      }

      // get updated domain
      domain = getMetadataDomainRepository().getDomain( domainId );

      if ( domain == null ) {
        // already deleted
        return true;
      }

      if ( domain.getLogicalModels() == null || domain.getLogicalModels().isEmpty() ) {
        getMetadataDomainRepository().removeDomain( domainId );
      }
    } catch ( MondrianCatalogServiceException me ) {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0020_UNABLE_TO_DELETE_CATALOG", catalogRef, domainId, me.getLocalizedMessage() ), me ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0020_UNABLE_TO_DELETE_CATALOG", catalogRef, domainId, me.getLocalizedMessage() ), me ); //$NON-NLS-1$
    } catch ( DomainStorageException dse ) {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0017_UNABLE_TO_STORE_DOMAIN", domainId, dse.getLocalizedMessage() ), dse ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0016_UNABLE_TO_STORE_DOMAIN", domainId, dse.getLocalizedMessage() ), dse ); //$NON-NLS-1$
    } catch ( DomainIdNullException dne ) {
      logger.error( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage() ), dne ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0019_DOMAIN_IS_NULL", dne.getLocalizedMessage() ), dne ); //$NON-NLS-1$
    }
    return true;
  }

  IPentahoResultSet executeQuery( String connectionName, String query, String previewLimit )
    throws QueryValidationException, SqlQueriesNotSupportedException {
    SQLConnection sqlConnection = null;
    try {
      checkSqlQueriesSupported( connectionName );

      int limit = ( previewLimit != null && previewLimit.length() > 0 ) ? Integer.parseInt( previewLimit ) : -1;
      sqlConnection = (SQLConnection) PentahoConnectionFactory.getConnection( IPentahoConnection.SQL_DATASOURCE,
        connectionName, PentahoSessionHolder.getSession(),
        new SimpleLogger( DatasourceServiceHelper.class.getName() ) );
      sqlConnection.setMaxRows( limit );
      sqlConnection.setReadOnly( true );
      return sqlConnection.executeQuery( BEFORE_QUERY + query + AFTER_QUERY );
    } catch ( SqlQueriesNotSupportedException e ) {
      logger.error( e.getLocalizedMessage() );
      throw e;
    } catch ( SQLException e ) {

      String error = "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED";

      if ( e.getSQLState().equals( "S0021" ) ) { // Column already exists
        error = "DatasourceServiceImpl.ERROR_0021_DUPLICATE_COLUMN_NAMES";
      }

      logger.error(
        Messages.getErrorString( error, e.getLocalizedMessage() ) );

      throw new QueryValidationException(
        Messages.getString( error, e.getLocalizedMessage() ) );

    } catch ( Exception e ) {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED",
        e.getLocalizedMessage() ), e ); //$NON-NLS-1$
      throw new QueryValidationException( e.getLocalizedMessage(), e );
    } finally {
      if ( sqlConnection != null ) {
        sqlConnection.close();
      }
    }
  }

  /**
   * Method is designed to check whether sql queries can be executed via connection with a {@core connName}.
   * For now we can't allow sql queries for connections, that are based on Pentaho Data Services.
   * See BISERVER-13225 for more info.
   *
   * @param connName
   *          name of connection, to be examined for sql queries support
   * @throws ConnectionServiceException
   *            if an error occurs while receiving connection with {@code connectionName}
   * @throws SqlQueriesNotSupportedException
   *            if query is not supported for a connection with a {@code connectionName}
   */
  void checkSqlQueriesSupported( String connName )
    throws ConnectionServiceException, SqlQueriesNotSupportedException {
    IDatabaseConnection conn = connService.getConnectionByName( connName );
    IDatabaseType dbType = conn.getDatabaseType();

    if ( dbType.getName().equals( DB_TYPE_ID_PENTAHO_DATA_SERVICE ) ) {
      throw new SqlQueriesNotSupportedException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0024_SQL_QUERIES_NOT_SUPPORTED_FOR_PENTAHO_DATA_SERVICE" ) );
    }
  }

  public SerializedResultSet doPreview( String connectionName, String query, String previewLimit )
    throws DatasourceServiceException {
    if ( !hasDataAccessPermission() ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
    }
    SerializedResultSet returnResultSet;
    try {
      connectionName = UtilHtmlSanitizer.getInstance().safeEscapeHtml( connectionName );
      executeQuery( connectionName, query, previewLimit );
      returnResultSet = DatasourceServiceHelper.getSerializeableResultSet( connectionName, query,
        Integer.parseInt( previewLimit ), PentahoSessionHolder.getSession() );
    } catch ( QueryValidationException e ) {
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage() ), e ); //$NON-NLS-1$
    } catch ( SqlQueriesNotSupportedException e ) {
      throw new DatasourceServiceException( e.getLocalizedMessage(), e ); //$NON-NLS-1$
    }
    return returnResultSet;

  }

  public boolean testDataSourceConnection( String connectionName ) throws DatasourceServiceException {
    if ( !hasDataAccessPermission() ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
    }
    java.sql.Connection conn = null;
    try {
      conn = DatasourceServiceHelper.getDataSourceConnection( connectionName, PentahoSessionHolder.getSession() );
      if ( conn == null ) {
        logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName ) ); //$NON-NLS-1$
        throw new DatasourceServiceException( Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName ) ); //$NON-NLS-1$
      }
    } finally {
      try {
        if ( conn != null ) {
          conn.close();
        }
      } catch ( SQLException e ) {
        logger.error( Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName, e.getLocalizedMessage() ), e ); //$NON-NLS-1$
        throw new DatasourceServiceException( Messages.getErrorString(
          "DatasourceServiceImpl.ERROR_0018_UNABLE_TO_TEST_CONNECTION", connectionName, e.getLocalizedMessage() ), e ); //$NON-NLS-1$
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

  public BusinessData generateLogicalModel( String modelName, String connectionName, String dbType, String query,
                                            String previewLimit )
    throws DatasourceServiceException {
    if ( !hasDataAccessPermission() ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
    }
    try {
      // Testing whether the query is correct or not
      connectionName = UtilHtmlSanitizer.getInstance().safeEscapeHtml( connectionName );
      executeQuery( connectionName, query, previewLimit );
      Boolean securityEnabled = ( getPermittedRoleList() != null && getPermittedRoleList().size() > 0 )
        || ( getPermittedUserList() != null && getPermittedUserList().size() > 0 );
      SerializedResultSet resultSet = DatasourceServiceHelper.getSerializeableResultSet( connectionName, query,
        Integer.parseInt( previewLimit ), PentahoSessionHolder.getSession() );

      SQLModelGenerator sqlModelGenerator =
        new SQLModelGenerator( modelName, connectionName, dbType, resultSet.getColumnTypes(), resultSet.getColumns(),
          query, securityEnabled, getEffectivePermittedUserList( securityEnabled ), getPermittedRoleList(),
          getDefaultAcls(), ( PentahoSessionHolder.getSession() != null ) ? PentahoSessionHolder.getSession().getName() : null );
      Domain domain = sqlModelGenerator.generate();
      return new BusinessData( domain, resultSet.getData() );
    } catch ( SQLModelGeneratorException smge ) {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", smge.getLocalizedMessage() ), smge ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", smge.getLocalizedMessage() ), smge ); //$NON-NLS-1$
    } catch ( QueryValidationException e ) {
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage() ), e ); //$NON-NLS-1$
    } catch ( SqlQueriesNotSupportedException e ) {
      throw new DatasourceServiceException( e.getLocalizedMessage(), e ); //$NON-NLS-1$
    }
  }

  // Add user to list if not already present
  private List<String> getEffectivePermittedUserList( boolean securityEnabled ) {
    ArrayList<String> permittedUserList =
      getPermittedUserList() == null ? new ArrayList<String>() : new ArrayList<String>( getPermittedUserList() );
    if ( securityEnabled ) {
      if ( !permittedUserList.contains( PentahoSessionHolder.getSession().getName() ) ) {
        permittedUserList.add( PentahoSessionHolder.getSession().getName() );
      }
    }
    return permittedUserList;
  }

  public IMetadataDomainRepository getMetadataDomainRepository() {
    return metadataDomainRepository;
  }

  public void setMetadataDomainRepository( IMetadataDomainRepository metadataDomainRepository ) {
    this.metadataDomainRepository = metadataDomainRepository;
  }

  public boolean saveLogicalModel( Domain domain, boolean overwrite ) throws DatasourceServiceException {
    if ( !hasDataAccessPermission() ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
    }

    String domainName = domain.getId();
    try {
      getMetadataDomainRepository().storeDomain( domain, overwrite );
      return true;
    } catch ( DomainStorageException dse ) {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0012_UNABLE_TO_STORE_DOMAIN", domainName, dse.getLocalizedMessage() ), dse ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0012_UNABLE_TO_STORE_DOMAIN", domainName, dse.getLocalizedMessage() ), dse ); //$NON-NLS-1$
    } catch ( DomainAlreadyExistsException dae ) {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0013_DOMAIN_ALREADY_EXIST", domainName, dae.getLocalizedMessage() ), dae ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0013_DOMAIN_ALREADY_EXIST", domainName, dae.getLocalizedMessage() ), dae ); //$NON-NLS-1$
    } catch ( DomainIdNullException dne ) {
      logger.error( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0014_DOMAIN_IS_NULL", dne.getLocalizedMessage() ), dne ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0014_DOMAIN_IS_NULL", dne.getLocalizedMessage() ), dne ); //$NON-NLS-1$
    }
  }

  public boolean hasPermission() {
    if ( PentahoSessionHolder.getSession() != null ) {
      IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class, PentahoSessionHolder.getSession() );
      return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
          && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
    } else {
      return false;
    }
  }

  public List<LogicalModelSummary> getLogicalModels( String context ) throws DatasourceServiceException {
    if ( !hasDataAccessViewPermission() ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0001_PERMISSION_DENIED" ) ); //$NON-NLS-1$
    }
    List<LogicalModelSummary> logicalModelSummaries = new ArrayList<LogicalModelSummary>();
    for ( String domainId : getMetadataDomainRepository().getDomainIds() ) {
      Domain domain;
      try {
        domain = getMetadataDomainRepository().getDomain( domainId );
      } catch ( Exception e ) {
        logger.error(
          Messages.getErrorString( "DatasourceServiceImpl.ERROR_0022_UNABLE_TO_PROCESS_LOGICAL_MODEL", domainId ), e );
        continue;
      }

      String locale = LocaleHelper.getLocale().toString();
      String[] locales = new String[ domain.getLocales().size() ];
      for ( int i = 0; i < domain.getLocales().size(); i++ ) {
        locales[ i ] = domain.getLocales().get( i ).getCode();
      }
      locale = LocaleHelper.getClosestLocale( locale, locales );

      for ( LogicalModel model : domain.getLogicalModels() ) {
        String vis = (String) model.getProperty( LM_PROP_VISIBLE );
        if ( vis != null ) {
          String[] visibleContexts = vis.split( "," );
          boolean visibleToContext = false;
          for ( String c : visibleContexts ) {
            if ( StringUtils.isNotEmpty( c.trim() ) && c.trim().equals( context ) ) {
              visibleToContext = true;
              break;
            }
          }
          if ( !visibleToContext ) {
            continue;
          }
        }
        logicalModelSummaries.add( new LogicalModelSummary( domainId, model.getId(), model.getName( locale ) ) );
      }
    }
    return logicalModelSummaries;
  }

  public BusinessData loadBusinessData( String domainId, String modelId ) throws DatasourceServiceException {
    Domain domain = getMetadataDomainRepository().getDomain( domainId );
    List<List<String>> data = null;
    if ( domain.getPhysicalModels().get( 0 ) instanceof InlineEtlPhysicalModel ) {
      InlineEtlPhysicalModel model = (InlineEtlPhysicalModel) domain.getPhysicalModels().get( 0 );

      String relativePath = PentahoSystem.getSystemSetting(
        "file-upload-defaults/relative-path",
        String.valueOf( CsvTransformGenerator.DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) ); //$NON-NLS-1$
      String csvFileLoc = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );

      data = DatasourceServiceHelper.getCsvDataSample( csvFileLoc + model.getFileLocation(), model.getHeaderPresent(),
        model.getDelimiter(), model.getEnclosure(), 5 );
    } else {
      SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get( 0 );
      String query = model.getPhysicalTables().get( 0 ).getTargetTable();
      SerializedResultSet resultSet =
        DatasourceServiceHelper.getSerializeableResultSet( model.getDatasource().getDatabaseName(), query, 5,
          PentahoSessionHolder.getSession() );
      data = resultSet.getData();
    }
    return new BusinessData( domain, data );
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return pojo;
  }

  public String serializeModelState( DatasourceDTO dto ) throws DatasourceServiceException {
    XStream xstream = new XStream();
    return xstream.toXML( dto );
  }

  public DatasourceDTO deSerializeModelState( String dtoStr ) throws DatasourceServiceException {
    XStream xs = new XStream();
    xs.setClassLoader( DatasourceDTO.class.getClassLoader() );
    if ( dtoStr.startsWith( "<org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO>" )
      && dtoStr.endsWith( "</org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO>" ) ) {
      return (DatasourceDTO) xs.fromXML( dtoStr );
    } else {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0025_STRING_FOR_DESERIALIZATION_IS_NOT_VALID" ) ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0025_STRING_FOR_DESERIALIZATION_IS_NOT_VALID" ) ); //$NON-NLS-1$
    }
  }

  public List<String> listDatasourceNames() throws IOException {
    synchronized ( CsvDatasourceServiceImpl.lock ) {
      IPentahoUrlFactory urlFactory = new SimpleUrlFactory( "" ); //$NON-NLS-1$
      PMDUIComponent component = new PMDUIComponent( urlFactory, new ArrayList() );
      component.validate( PentahoSessionHolder.getSession(), null );
      component.setAction( PMDUIComponent.ACTION_LIST_MODELS );
      Document document = component.getXmlContent();
      List<DefaultElement> modelElements = document.selectNodes( "//model_name" ); //$NON-NLS-1$

      ArrayList<String> datasourceNames = new ArrayList<String>();
      for ( DefaultElement element : modelElements ) {
        datasourceNames.add( element.getText() );
      }
      return datasourceNames;
    }
  }

  @Override
  public QueryDatasourceSummary generateQueryDomain( String name, String query, DatabaseConnection connection,
                                                     DatasourceDTO datasourceDTO ) throws DatasourceServiceException {

    ModelerWorkspace modelerWorkspace = new ModelerWorkspace( new GwtModelerWorkspaceHelper(), getGeoContext() );
    ModelerService modelerService = createModelerService();
    modelerWorkspace.setModelName( name );

    try {
      UtilHtmlSanitizer.getInstance().sanitizeConnectionParameters( connection );
      executeQuery( UtilHtmlSanitizer.getInstance().safeEscapeHtml( datasourceDTO.getConnectionName() ), query, "1" );
      Boolean securityEnabled = ( getPermittedRoleList() != null && getPermittedRoleList().size() > 0 )
        || ( getPermittedUserList() != null && getPermittedUserList().size() > 0 );
      SerializedResultSet resultSet = DatasourceServiceHelper.getSerializeableResultSet( connection.getName(), query,
        10, PentahoSessionHolder.getSession() );
      SQLModelGenerator sqlModelGenerator =
        new SQLModelGenerator( name, connection.getName(), connection.getDatabaseType().getShortName(),
          resultSet.getColumnTypes(), resultSet.getColumns(), query,
          securityEnabled, getEffectivePermittedUserList( securityEnabled ), getPermittedRoleList(), getDefaultAcls(),
          ( PentahoSessionHolder
            .getSession() != null ) ? PentahoSessionHolder.getSession().getName() : null );
      Domain domain = sqlModelGenerator.generate();
      domain.getPhysicalModels().get( 0 ).setId( connection.getName() );

      modelerWorkspace.setDomain( domain );


      modelerWorkspace.getWorkspaceHelper().autoModelFlat( modelerWorkspace );
      modelerWorkspace.getWorkspaceHelper().autoModelRelationalFlat( modelerWorkspace );
      modelerWorkspace.setModelName( datasourceDTO.getDatasourceName() );
      modelerWorkspace.getWorkspaceHelper().populateDomain( modelerWorkspace );
      domain.getLogicalModels().get( 0 ).setProperty( "datasourceModel", serializeModelState( datasourceDTO ) );
      domain.getLogicalModels().get( 0 ).setProperty( "DatasourceType", "SQL-DS" );

      QueryDatasourceSummary summary = new QueryDatasourceSummary();
      prepareForSerializaton( domain );
      modelerService.serializeModels( domain, modelerWorkspace.getModelName() );
      summary.setDomain( domain );

      return summary;
    } catch ( SQLModelGeneratorException smge ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", //$NON-NLS-1$
        smge.getLocalizedMessage() ), smge );
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", smge.getLocalizedMessage() ),
        smge ); //$NON-NLS-1$
    } catch ( QueryValidationException e ) {
      logger.error( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage() ), e ); //$NON-NLS-1$
      throw new DatasourceServiceException( Messages.getErrorString(
        "DatasourceServiceImpl.ERROR_0009_QUERY_VALIDATION_FAILED", e.getLocalizedMessage() ), e ); //$NON-NLS-1$
    } catch ( ModelerException e ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", //$NON-NLS-1$
        e.getLocalizedMessage() ), e );
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", e.getLocalizedMessage() ),
        e ); //$NON-NLS-1$
    } catch ( SqlQueriesNotSupportedException e ) {
      throw new DatasourceServiceException( e.getLocalizedMessage(), e ); //$NON-NLS-1$
    } catch ( Exception e ) {
      logger.error( Messages.getErrorString( "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", //$NON-NLS-1$
        e.getLocalizedMessage() ), e );
      throw new DatasourceServiceException( Messages
        .getErrorString( "DatasourceServiceImpl.ERROR_0011_UNABLE_TO_GENERATE_MODEL", e.getLocalizedMessage() ),
        e ); //$NON-NLS-1$
    }

  }

  public void prepareForSerializaton( Domain domain ) {
    /*
     * This method is responsible for cleaning up legacy information when
     * changing datasource types and also manages CSV files for CSV based
     * datasources.
     */

    String relativePath = PentahoSystem.getSystemSetting( "file-upload-defaults/relative-path",
      String.valueOf( FileUtils.DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) ); //$NON-NLS-1$
    String path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
    LogicalModel logicalModel = domain.getLogicalModels().get( 0 );
    String modelState = (String) logicalModel.getProperty( "datasourceModel" );

    if ( modelState != null ) {
      XStream xs = new XStream();
      DatasourceDTO datasource = (DatasourceDTO) xs.fromXML( modelState );
      CsvFileInfo csvFileInfo = datasource.getCsvModelInfo().getFileInfo();
      String csvFileName = csvFileInfo.getFilename();

      if ( csvFileName != null ) {

        // Cleanup logic when updating from CSV datasource to SQL
        // datasource.
        csvFileInfo.setFilename( null );
        csvFileInfo.setTmpFilename( null );
        csvFileInfo.setFriendlyFilename( null );
        csvFileInfo.setContents( null );
        csvFileInfo.setEncoding( null );

        // Delete CSV file.
        File csvFile = new File( path + File.separatorChar + csvFileName );
        if ( csvFile.exists() ) {
          csvFile.delete();
        }

        // Delete STAGING database table.
        CsvTransformGenerator csvTransformGenerator =
          new CsvTransformGenerator( datasource.getCsvModelInfo(), AgileHelper.getDatabaseMeta() );
        try {
          csvTransformGenerator.dropTable( datasource.getCsvModelInfo().getStageTableName() );
        } catch ( CsvTransformGeneratorException e ) {
          logger.error( e );
        }
      }
      // Update datasourceModel with the new modelState
      modelState = xs.toXML( datasource );
      logicalModel.setProperty( "datasourceModel", modelState );
    }
  }

  public String getDatasourceIllegalCharacters() throws DatasourceServiceException {
    IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    return resLoader.getPluginSetting( getClass(), "settings/data-access-datasource-illegal-characters" ); //$NON-NLS-1$
  }

  @Override
  public GeoContext getGeoContext() throws DatasourceServiceException {
    if ( this.geoContext == null ) {
      this.geoContext = DatasourceServiceHelper.getGeoContext();
    }
    return this.geoContext;
  }
}
