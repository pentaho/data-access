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
 * Copyright (c) 2002-2015 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.agilebi.modeler.util.ModelerWorkspaceHelper;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository.datasource.IDatasourceMgmtService;
import org.pentaho.platform.api.repository2.unified.IPlatformImportBundle;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.api.repository2.unified.RepositoryFileAcl;
import org.pentaho.platform.dataaccess.datasource.api.DataSourceWizardService.DswPublishValidationException.Type;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.utils.DataAccessPermissionUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ModelerService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.metadata.MetadataPublisher;
import org.pentaho.platform.plugin.action.mondrian.MondrianCachePublisher;
import org.pentaho.platform.plugin.action.mondrian.catalog.IAclAwareMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogServiceException;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.metadata.IAclAwarePentahoMetadataDomainRepositoryImporter;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;

public class DataSourceWizardService extends DatasourceService {

  protected IDSWDatasourceService dswService;
  protected IModelerService modelerService;
  protected IDatasourceMgmtService datasourceMgmtSvc;

  protected IAclAwarePentahoMetadataDomainRepositoryImporter aclAwarePentahoMetadataDomainRepositoryImporter;
  protected IAclAwareMondrianCatalogService aclAwareMondrianCatalogService;

  private static final Log logger = LogFactory.getLog( DataSourceWizardService.class );

  private static final String MONDRIAN_CATALOG_REF = "MondrianCatalogRef"; //$NON-NLS-1$
  private static final String METADATA_PUBLISHER = MetadataPublisher.class.getName();
  private static final String MONDRIAN_PUBLISHER = MondrianCachePublisher.class.getName();
  private static final String ENCODING = "UTF-8";
  private static final String MONDRIAN_CONNECTION_PARAM = "parameters";
  private static final String MONDRIAN_SCHEMA_NAME = "schema.xml";
  private static final String MONDRIAN_MIME = "application/vnd.pentaho.mondrian+xml";
  private static final String METADATA_MIME = "text/xmi+xml";
  private static final String METADATA_EXT = ".xmi";
  private static final String IMPORT_DOMAIN_ID = "domain-id";

  public DataSourceWizardService() {
    dswService = getDswDatasourceService();
    modelerService = new ModelerService();
    datasourceMgmtSvc = PentahoSystem.get( IDatasourceMgmtService.class, PentahoSessionHolder.getSession() );
    if ( metadataDomainRepository instanceof IAclAwarePentahoMetadataDomainRepositoryImporter ) {
      aclAwarePentahoMetadataDomainRepositoryImporter = (IAclAwarePentahoMetadataDomainRepositoryImporter) metadataDomainRepository;
    }
    if ( mondrianCatalogService instanceof IAclAwareMondrianCatalogService ) {
      aclAwareMondrianCatalogService = (IAclAwareMondrianCatalogService) mondrianCatalogService;
    }
  }

  protected IDSWDatasourceService getDswDatasourceService() {
    return new DSWDatasourceServiceImpl();
  }

  public Map<String, InputStream> doGetDSWFilesAsDownload( String dswId ) throws PentahoAccessControlException {
    if ( !canManageACL() ) {
      throw new PentahoAccessControlException();
    }
    // First get the metadata files;
    Map<String, InputStream> fileData = getMetadataFiles( dswId );


    // Then get the corresponding mondrian files
    Domain domain = metadataDomainRepository.getDomain( dswId );
    ModelerWorkspace model = createModelerWorkspace();
    model.setDomain( domain );
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( logicalModel == null ) {
      logicalModel = model.getLogicalModel( ModelerPerspective.REPORTING );
    }
    if ( logicalModel.getProperty( MONDRIAN_CATALOG_REF ) != null ) {
      MondrianCatalogRepositoryHelper helper = createMondrianCatalogRepositoryHelper();
      String catalogRef = (String) logicalModel.getProperty( MONDRIAN_CATALOG_REF );
      fileData.putAll( helper.getModrianSchemaFiles( catalogRef ) );
      parseMondrianSchemaNameWrapper( dswId, fileData );
    }

    return fileData;
  }

  public void removeDSW( String dswId ) throws PentahoAccessControlException {
    if ( !canAdministerCheck() ) {
      throw new PentahoAccessControlException();
    }
    dswId = parseMondrianSchemaNameWrapper( dswId );
    Domain domain = metadataDomainRepository.getDomain( dswId );
    ModelerWorkspace model = createModelerWorkspace();
    model.setDomain( domain );
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( logicalModel == null ) {
      logicalModel = model.getLogicalModel( ModelerPerspective.REPORTING );
    }
    if ( logicalModel.getProperty( MONDRIAN_CATALOG_REF ) != null ) {
      String catalogRef = (String) logicalModel.getProperty( MONDRIAN_CATALOG_REF );
      try {
        mondrianCatalogService.removeCatalog( catalogRef, getSession() );
      } catch ( MondrianCatalogServiceException e ) {
        logger.warn( "Failed to remove mondrian catalog", e );
      }
    }
    try {
      dswService.deleteLogicalModel( domain.getId(), logicalModel.getId() );
    } catch ( DatasourceServiceException ex ) {
      logger.warn( "Failed to remove logical model", ex );
    }
    metadataDomainRepository.removeDomain( dswId );
  }

  public List<String> getDSWDatasourceIds() {
    List<String> datasourceList = new ArrayList<String>();
    try {
    nextModel:
      for ( LogicalModelSummary summary : dswService.getLogicalModels( null ) ) {
        Domain domain = modelerService.loadDomain( summary.getDomainId() );
        List<LogicalModel> logicalModelList = domain.getLogicalModels();
        if ( logicalModelList != null && logicalModelList.size() >= 1 ) {
          for ( LogicalModel logicalModel : logicalModelList ) {
            Object property = logicalModel.getProperty( "AGILE_BI_GENERATED_SCHEMA" ); //$NON-NLS-1$
            if ( property != null ) {
              datasourceList.add( summary.getDomainId() );
              continue nextModel;
            }
          }
        }
      }
    } catch ( Throwable e ) {
      return null;
    }
    return datasourceList;
  }

  public String publishDsw( String domainId, InputStream metadataFile, boolean overwrite, boolean checkConnection,
      RepositoryFileAclDto acl )
    throws PentahoAccessControlException, IllegalArgumentException, DswPublishValidationException, Exception {
    if ( !hasManageAccessCheck() ) {
      throw new PentahoAccessControlException();
    }
    if ( !endsWith( domainId, METADATA_EXT ) ) {
      // if doesn't end in case-sensitive '.xmi' there will be trouble later on
      final String errorMsg = "domainId must end in " + METADATA_EXT;
      throw new IllegalArgumentException( errorMsg );
    }
    if ( metadataFile == null ) {
      throw new IllegalArgumentException( "metadataFile is null" );
    }
    if ( !overwrite ) {
      final List<String> overwritten = getOverwrittenDomains( domainId );
      if ( !overwritten.isEmpty() ) {
        final String domainIds = StringUtils.join( overwritten, "," );
        throw new DswPublishValidationException( DswPublishValidationException.Type.OVERWRITE_CONFLICT, domainIds );
      }
    }

    XmiParser xmiParser = createXmiParser();
    Domain domain = null;
    try {
      domain = xmiParser.parseXmi( metadataFile );
    } catch ( Exception e ) {
      throw new DswPublishValidationException( DswPublishValidationException.Type.INVALID_XMI, e.getMessage() );
    }
    domain.setId( domainId );
    if ( checkConnection ) {
      final String connectionId = getMondrianDatasourceWrapper( domain );
      if ( datasourceMgmtSvc.getDatasourceByName( connectionId ) == null ) {
        final String msg = "connection not found: '" + connectionId + "'";
        throw new DswPublishValidationException( Type.MISSING_CONNECTION, msg );
      }
    }
    // build bundles
    InputStream metadataIn = toInputStreamWrapper( domain, xmiParser );
    IPlatformImportBundle metadataBundle = createMetadataDswBundle( domain, metadataIn, overwrite, acl );
    IPlatformImportBundle mondrianBundle = createMondrianDswBundle( domain, acl );
    // do import
    IPlatformImporter importer = getIPlatformImporter();
    importer.importFile( metadataBundle );
    logger.debug( "imported metadata xmi" );
    importer.importFile( mondrianBundle );
    logger.debug( "imported mondrian schema" );
    // trigger refreshes
    IPentahoSession session = getSession();
    PentahoSystem.publish( session, METADATA_PUBLISHER );
    PentahoSystem.publish( session, MONDRIAN_PUBLISHER );
    logger.info( "publishDsw: Published DSW with domainId='" + domainId + "'." );
    return domainId;
  }

  /**
   * Retrieve ACL of the DSW. Actually it is ACL of it's Metadata.
   *
   * @param dswId dsw id
   * @return ACL
   * @throws PentahoAccessControlException
   */
  public RepositoryFileAclDto getDSWAcl( String dswId ) throws PentahoAccessControlException, FileNotFoundException {
    checkDSWExists( dswId );

    if ( aclAwarePentahoMetadataDomainRepositoryImporter != null ) {
      final RepositoryFileAcl acl = aclAwarePentahoMetadataDomainRepositoryImporter.getAclFor( dswId );
      return acl == null ? null : repositoryFileAclAdapter.marshal( acl );
    }
    return null;
  }

  /**
   * Set ACL to both Mondrian Catalog and Metadata Schema
   *
   * @param dswId dsw id
   * @param aclDto ACL
   * @throws PentahoAccessControlException
   * @throws FileNotFoundException
   */
  public void setDSWAcl( String dswId, RepositoryFileAclDto aclDto )
      throws PentahoAccessControlException, FileNotFoundException {
    checkDSWExists( dswId );

    if ( !endsWith( dswId, METADATA_EXT ) ) {
      // if doesn't end in case-sensitive '.xmi' there will be trouble later on
      final String errorMsg = "domainId must end in " + METADATA_EXT;
      throw new IllegalArgumentException( errorMsg );
    }

    final RepositoryFileAcl acl = aclDto == null ? null : repositoryFileAclAdapter.unmarshal( aclDto );
    if ( aclAwareMondrianCatalogService != null ) {
      aclAwareMondrianCatalogService.setAclFor( dswId.substring( 0, dswId.lastIndexOf( METADATA_EXT ) ), acl );
    }
    if ( aclAwarePentahoMetadataDomainRepositoryImporter != null ) {
      aclAwarePentahoMetadataDomainRepositoryImporter.setAclFor( dswId, acl );
    }
    flushDataSources();
  }

  private void checkDSWExists( String dswId ) throws PentahoAccessControlException, FileNotFoundException {
    try {
      doGetDSWFilesAsDownload( dswId );
    } catch ( NullPointerException e ) {
      throw new FileNotFoundException( dswId + " doesn't exist" );
    }
  }

  public static class DswPublishValidationException extends Exception {
    public enum Type {
      OVERWRITE_CONFLICT,
      MISSING_CONNECTION,
      INVALID_XMI
    }
    private static final long serialVersionUID = 1L;
    private Type type;

    public DswPublishValidationException( Type type, String msg ) {
      super( msg );
    }
    public Type getType() {
      return type;
    }
  }

  protected List<String> getOverwrittenDomains( String dswId ) {
    List<String> domainIds = new ArrayList<String>( 2 );
    if ( metadataDomainRepository.getDomainIds().contains( dswId ) ) {
      domainIds.add( "dsw/" + dswId );
    }
    final String catalogName = toAnalysisDomainId( dswId );
    if ( mondrianCatalogService.getCatalog( catalogName, PentahoSessionHolder.getSession() ) != null ) {
      domainIds.add( "mondrian/" + catalogName );
    }
    return domainIds;
  }

  private String toAnalysisDomainId( String dswId ) {
    return dswId.substring( 0, dswId.lastIndexOf( '.' ) );
  }

  protected IPlatformImportBundle createMetadataDswBundle( Domain domain, InputStream metadataIn, boolean overwrite, RepositoryFileAclDto acl ) {
    final RepositoryFileImportBundle.Builder builder = new RepositoryFileImportBundle.Builder()
        .input( metadataIn )
        .charSet( ENCODING )
        .hidden( false )
        .overwriteFile( overwrite )
        .mime( METADATA_MIME )
        .withParam( IMPORT_DOMAIN_ID, domain.getId() )
        .preserveDsw( true );
    if ( acl != null ) {
      builder.acl( repositoryFileAclAdapter.unmarshal( acl ) ).applyAclSettings( true );
    }
    return builder
        .build();
  }

  /**
   * Generate a mondrian schema from the model and create the appropriate import bundle
   * @param domain domain with olap model
   * @return import bundle
   * @throws DatasourceServiceException 
   * @throws Exception If schema generation fails
   */
  protected IPlatformImportBundle createMondrianDswBundle( Domain domain, RepositoryFileAclDto acl ) throws DatasourceServiceException,
    DswPublishValidationException, IOException {
    final String analysisDomainId = toAnalysisDomainId( domain.getId() );
    final String dataSource = ModelerService.getMondrianDatasource( domain );
    // get olap logical model
    final String locale = Locale.getDefault().toString();
    ModelerWorkspace workspace =
        new ModelerWorkspace( new ModelerWorkspaceHelper( locale ), dswService.getGeoContext() );
    workspace.setModelName( analysisDomainId );
    workspace.setDomain( domain );
    LogicalModel olapModel = workspace.getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( olapModel == null ) {
      throw new IllegalArgumentException( "No analysis model in xmi." );
    }
    // reference schema in xmi
    olapModel.setProperty( MONDRIAN_CATALOG_REF, analysisDomainId );
    // generate schema
    MondrianModelExporter exporter = new MondrianModelExporter( olapModel, locale );
    String mondrianSchema = null;
    try {
      mondrianSchema = exporter.createMondrianModelXML();
    } catch ( Exception e ) {
      throw new DswPublishValidationException( Type.INVALID_XMI, e.getMessage() );
    }
    // create bundle
    final RepositoryFileImportBundle.Builder builder = new RepositoryFileImportBundle.Builder()
        .input( IOUtils.toInputStream( mondrianSchema, ENCODING ) )
        .name( MONDRIAN_SCHEMA_NAME )
        .charSet( ENCODING )
        .overwriteFile( true )
        .mime( MONDRIAN_MIME )
        .withParam( IMPORT_DOMAIN_ID, analysisDomainId )
        .withParam( MONDRIAN_CONNECTION_PARAM, "DataSource=" + dataSource );
    if ( acl != null ) {
      builder.acl( repositoryFileAclAdapter.unmarshal( acl ) ).applyAclSettings( true );
    }
    return builder.build();
  }

  protected boolean canAdministerCheck() {
    return super.canAdminister();
  }

  protected boolean hasManageAccessCheck() {
    return DataAccessPermissionUtil.hasManageAccess();
  }

  protected boolean endsWith( String str, String suffix ) {
    return StringUtils.endsWith( str, suffix );
  }

  protected XmiParser createXmiParser() {
    return new XmiParser();
  }

  protected void parseMondrianSchemaNameWrapper( String dswId, Map<String, InputStream> fileData ) {
    super.parseMondrianSchemaName( dswId, fileData );
  }

  protected String getMondrianDatasourceWrapper( Domain domain ) {
    return ModelerService.getMondrianDatasource( domain );
  }

  protected InputStream toInputStreamWrapper( Domain domain, XmiParser xmiParser ) throws IOException {
    return IOUtils.toInputStream( xmiParser.generateXmi( domain ), ENCODING );
  }

  protected String parseMondrianSchemaNameWrapper( String dswId ) {
    return super.fixEncodedSlashParam( dswId );
  }

  protected Map<String, InputStream> getMetadataFiles( String dswId ) {
    return ( (IPentahoMetadataDomainRepositoryExporter) metadataDomainRepository ).getDomainFilesData( dswId );
  }

  protected ModelerWorkspace createModelerWorkspace() {
    return new ModelerWorkspace( new GwtModelerWorkspaceHelper() );
  }

  protected MondrianCatalogRepositoryHelper createMondrianCatalogRepositoryHelper() {
    return new MondrianCatalogRepositoryHelper( PentahoSystem.get( IUnifiedRepository.class ) );
  }

  protected IPentahoSession getSession() {
    return PentahoSessionHolder.getSession();
  }

  protected IPlatformImporter getIPlatformImporter() {
    return PentahoSystem.get( IPlatformImporter.class );
  }

}
