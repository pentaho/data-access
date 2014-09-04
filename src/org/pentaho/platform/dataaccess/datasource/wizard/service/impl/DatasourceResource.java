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
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.Facet;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseConnectionPoolParameter;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseConnectionPoolParameter;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.database.util.DatabaseUtil;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.api.resources.AnalysisResource;
import org.pentaho.platform.dataaccess.datasource.api.resources.DataSourceWizardResource;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataResource;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.CsvUtils;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;
import org.pentaho.platform.web.http.api.resources.JaxbList;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionList;
import org.pentaho.ui.database.event.DefaultDatabaseConnectionPoolParameterList;
import org.pentaho.ui.database.event.IDatabaseConnectionList;
import org.pentaho.ui.database.event.IDatabaseConnectionPoolParameterList;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path( "/data-access/api" )
public class DatasourceResource extends DataSourceWizardResource {

  private static final String XMLA_ENABLED_FLAG = "xmlaEnabledFlag";
  private static final String CATALOG_NAME = "catalogName";
  private static final String ORIG_CATALOG_NAME = "origCatalogName";
  private static final String DATASOURCE_NAME = "datasourceName";
  private static final String UPLOAD_ANALYSIS = "uploadAnalysis";
  private static final String PARAMETERS = "parameters";
  private static final String OVERWRITE_IN_REPOS = "overwrite";
  private static final int SUCCESS = 3;

  private static final Log logger = LogFactory.getLog( DatasourceResource.class );

  private ConnectionServiceImpl connectionService;
  private DatabaseDialectService dialectService;
  GenericDatabaseDialect genericDialect = new GenericDatabaseDialect();

  private static final String LANG = "[a-z]{2}";
  private static final String LANG_CC = LANG + "_[A-Z]{2}";
  private static final String LANG_CC_EXT = LANG_CC + "_[^/]+";
  private static final List<String> ENCODINGS = Arrays.asList( "", "UTF-8", "UTF-16BE", "UTF-16LE", "UTF-32BE",
      "UTF-32LE", "Shift_JIS", "ISO-2022-JP", "ISO-2022-CN", "ISO-2022-KR", "GB18030", "Big5", "EUC-JP", "EUC-KR",
      "ISO-8859-1", "ISO-8859-2", "ISO-8859-5", "ISO-8859-6", "ISO-8859-7", "ISO-8859-8", "windows-1251",
      "windows-1256", "KOI8-R", "ISO-8859-9" );

  private static final Pattern[] patterns = new Pattern[] { Pattern.compile( "(" + LANG + ").properties$" ),
    Pattern.compile( "(" + LANG_CC + ").properties$" ), Pattern.compile( "(" + LANG_CC_EXT + ").properties$" ),
    Pattern.compile( "([^/]+)_(" + LANG + ")\\.properties$" ),
    Pattern.compile( "([^/]+)_(" + LANG_CC + ")\\.properties$" ),
    Pattern.compile( "([^/]+)_(" + LANG_CC_EXT + ")\\.properties$" ), };

  public DatasourceResource() {
    super();
    connectionService = new ConnectionServiceImpl();
    this.dialectService = new DatabaseDialectService( true );
  }

  /**
   * Get list of IDs of analysis datasource
   * 
   * @return JaxbList<String> of analysis IDs
   */
  public JaxbList<String> getAnalysisDatasourceIds() {
    return new AnalysisResource().getAnalysisDatasourceIds();
  }

  /**
   * Get the Metadata datasource IDs
   * 
   * @return JaxbList<String> of metadata IDs
   */
  public JaxbList<String> getMetadataDatasourceIds() {
    return new MetadataResource().getMetadataDatasourceIds();
  }

  /**
   * Returns a list of datasource IDs from datasource wizard
   * 
   * @return JaxbList<String> list of datasource IDs
   */
  public JaxbList<String> getDSWDatasourceIds() {
    return new DataSourceWizardResource().getDSWDatasourceIds();
  }

  /**
   * Download the metadata files for a given metadataId
   * 
   * @param metadataId
   *          String Id of the metadata to retrieve
   * 
   * @return Response containing the file data
   */
  @Facet( name = "Unsupported" )
  public Response doGetMetadataFilesAsDownload( @PathParam( "metadataId" ) String metadataId ) {
    return new MetadataResource().doGetMetadataFilesAsDownload( metadataId );
  }

  /**
   * Download the analysis files for a given analysis id
   * 
   * @param analysisId
   *          String Id of the analysis data to retrieve
   * 
   * @return Response containing the file data
   */
  @Facet( name = "Unsupported" )
  public Response doGetAnalysisFilesAsDownload( @PathParam( "analysisId" ) String analysisId ) {
    return new AnalysisResource().doGetAnalysisFilesAsDownload( analysisId );
  }

  /**
   * Download the data source wizard data for a given data source wizard ID
   * 
   * @param dswId
   *          String Id of the data source wizard data to retrieve
   * 
   * @return Response containing the file data
   */
  public Response doGetDSWFilesAsDownload( @PathParam( "dswId" ) String dswId ) {
    return new DataSourceWizardResource().download( dswId );
  }

  /**
   * Remove the metadata for a given metadata ID
   * 
   * @param metadataId
   *          String ID of the metadata to remove
   * 
   * @return Response ok if successful
   */
  @Facet( name = "Unsupported" )
  public Response doRemoveMetadata( @PathParam( "metadataId" ) String metadataId ) {
    return new MetadataResource().doRemoveMetadata( metadataId );
  }

  /**
   * Remove the analysis data for a given analysis ID
   * 
   * @param analysisId
   *          String ID of the analysis data to remove
   * 
   * @return Response ok if successful
   */
  @Facet( name = "Unsupported" )
  public Response doRemoveAnalysis( @PathParam( "analysisId" ) String analysisId ) {
    return new AnalysisResource().doRemoveAnalysis( analysisId );
  }

  /**
   * Remove the datasource wizard data for a given datasource wizard ID
   * 
   * @param dswId
   *          String ID of the datasource wizard data to remove
   * 
   * @return Response ok if successful
   */
  @Facet( name = "Unsupported" )
  public Response doRemoveDSW( @PathParam( "dswId" ) String dswId ) {
    return new DataSourceWizardResource().remove( dswId );
  }

  /**
   * Get the data source wizard info (parameters) for a specific data source wizard id
   * 
   * @param dswId
   *          String id for a data source wizard
   * 
   * @return Response containing the parameter list
   */
  @GET
  @Path( "/{dswId : .+}/getAnalysisDatasourceInfo" )
  @Produces( WILDCARD )
  @Facet( name = "Unsupported" )
  public Response getAnalysisDatasourceInfo( @PathParam( "dswId" ) String dswId ) {
    IMondrianCatalogService mondrianCatalogService =
        PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() );
    MondrianCatalog catalog = mondrianCatalogService.getCatalog( dswId, PentahoSessionHolder.getSession() );
    String parameters = catalog.getDataSourceInfo();
    return Response.ok().entity( parameters ).build();
  }

  /**
   * This is used by PUC to use a Jersey put to import a Mondrian Schema XML into PUR
   * 
   * @param dataInputStream
   * @param schemaFileInfo
   * @param catalogName
   * @param datasourceName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @param parameters
   * @return this method returns a response of "3" for success, 8 if exists, etc.
   * @throws PentahoAccessControlException
   */
  @PUT
  @Path( "/mondrian/putSchema" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @Facet( name = "Unsupported" )
  public Response putMondrianSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream dataInputStream,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_NAME ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters )
    throws PentahoAccessControlException {
    Response response = null;
    int statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    try {
      AnalysisService service = new AnalysisService();
      service.putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, datasourceName,
          overwrite, xmlaEnabledFlag, parameters );
      statusCode = SUCCESS;
    } catch ( PentahoAccessControlException pac ) {
      logger.error( pac.getMessage() );
      statusCode = PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL;
    } catch ( PlatformImportException pe ) {
      statusCode = pe.getErrorStatus();
      logger.error( "Error putMondrianSchema " + pe.getMessage() + " status = " + statusCode );
    } catch ( Exception e ) {
      logger.error( "Error putMondrianSchema " + e.getMessage() );
      statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    }

    response = Response.ok().status( statusCode ).type( MediaType.TEXT_PLAIN ).build();
    logger.debug( "putMondrianSchema Response " + response );
    return response;
  }

  /**
   * This is used by PUC to use a form post to import a Mondrian Schema XML into PUR
   * 
   * @param dataInputStream
   * @param schemaFileInfo
   * @param catalogName
   * @param datasourceName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @param parameters
   * @return this method returns a response of "SUCCESS" for success, 8 if exists, 2 for general error,etc.
   * @throws PentahoAccessControlException
   */
  @POST
  @Path( "/mondrian/postAnalysis" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( { "text/plain", "text/html" } )
  @Facet( name = "Unsupported" )
  public Response postMondrainSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream dataInputStream,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_NAME ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters )
    throws PentahoAccessControlException {
    // use existing Jersey post method - but translate into text/html for PUC Client
    ResponseBuilder responseBuilder;
    Response response =
        this.putMondrianSchema( dataInputStream, schemaFileInfo, catalogName, origCatalogName, datasourceName,
            overwrite, xmlaEnabledFlag, parameters );
    responseBuilder = Response.ok();
    responseBuilder.entity( String.valueOf( response.getStatus() ) );
    responseBuilder.status( 200 );
    return responseBuilder.build();
  }

  /**
   * @param domainId
   *          Unique identifier for the metadata datasource
   * @param metadataFile
   *          Input stream for the metadata.xmi
   * @param metadataFileInfo
   *          User selected name for the file
   * @param localeFiles
   *          List of local files
   * @param localeFilesInfo
   *          List of information for each local file
   * 
   * @return Response containing the success of the method
   * 
   * @throws PentahoAccessControlException
   *           Thrown when validation of access fails
   * 
   *           A convenience method stubs out to the importMetadataDatasource method so that imports can be called from
   *           a http form which requires a post.
   */
  @POST
  @Path( "/metadata/postimport" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/html" )
  @Facet( name = "Unsupported" )
  public Response importMetadataDatasourceWithPost( @FormDataParam( "domainId" ) String domainId,
      @FormDataParam( "metadataFile" ) InputStream metadataFile,
      @FormDataParam( "metadataFile" ) FormDataContentDisposition metadataFileInfo,
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( "localeFiles" ) List<FormDataBodyPart> localeFiles,
      @FormDataParam( "localeFiles" ) List<FormDataContentDisposition> localeFilesInfo )
    throws PentahoAccessControlException {
    Response response =
        importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo );
    ResponseBuilder responseBuilder;
    responseBuilder = Response.ok();
    responseBuilder.entity( String.valueOf( response.getStatus() ) );
    responseBuilder.status( 200 );
    return responseBuilder.build();
  }

  /**
   * @param domainId
   *          Unique identifier for the metadata datasource
   * @param metadataFile
   *          Input stream for the metadata.xmi
   * @param metadataFileInfo
   *          User selected name for the file
   * @param localeFiles
   *          List of local files
   * @param localeFilesInfo
   *          List of information for each local file
   * @param overwrite
   *          Flag for overwriting existing version of the file
   * 
   * @return Response containing the success of the method
   * 
   */
  @PUT
  @Path( "/metadata/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @Deprecated
  @Facet( name = "Unsupported" )
  public Response importMetadataDatasource( @FormDataParam( "domainId" ) String domainId,
      @FormDataParam( "metadataFile" ) InputStream metadataFile,
      @FormDataParam( "metadataFile" ) FormDataContentDisposition metadataFileInfo,
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( "localeFiles" ) List<FormDataBodyPart> localeFiles,
      @FormDataParam( "localeFiles" ) List<FormDataContentDisposition> localeFilesInfo ) {
    return new MetadataResource().importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite,
        localeFiles, localeFilesInfo );
  }

  /**
   * @param localizeBundleEntries
   * @param domainId
   *          Unique identifier for the metadata datasource
   * @param metadataFile
   *          Input stream for the metadata.xmi
   * 
   * @return Response containing the success of the method
   * 
   * @throws PentahoAccessControlException
   *           Thrown when validation of access fails
   */
  @PUT
  @Path( "/metadata/uploadServletImport" )
  @Consumes( { TEXT_PLAIN } )
  @Produces( "text/plain" )
  @Deprecated
  @Facet( name = "Unsupported" )
  public Response uploadServletImportMetadataDatasource( String localizeBundleEntries,
      @QueryParam( "domainId" ) String domainId, @QueryParam( "metadataFile" ) String metadataFile )
    throws PentahoAccessControlException {
    try {
      DatasourceService.validateAccess();
    } catch ( PentahoAccessControlException e ) {
      return Response.serverError().entity( e.toString() ).build();
    }

    IMetadataDomainRepository metadataDomainRepository =
        PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );
    PentahoMetadataDomainRepository metadataImporter =
        new PentahoMetadataDomainRepository( PentahoSystem.get( IUnifiedRepository.class ) );
    CsvUtils csvUtils = new CsvUtils();
    boolean validPropertyFiles = true;
    StringBuffer invalidFiles = new StringBuffer();
    try {
      String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "tmp" + File.separatorChar;
      String sysTmpDir = PentahoSystem.getApplicationContext().getSolutionPath( TMP_FILE_PATH );
      FileInputStream metadataInputStream = new FileInputStream( sysTmpDir + File.separatorChar + metadataFile );
      metadataImporter.storeDomain( metadataInputStream, domainId, true );
      metadataDomainRepository.getDomain( domainId );

      StringTokenizer bundleEntriesParam = new StringTokenizer( localizeBundleEntries, ";" );
      while ( bundleEntriesParam.hasMoreTokens() ) {
        String localizationBundleElement = bundleEntriesParam.nextToken();
        StringTokenizer localizationBundle = new StringTokenizer( localizationBundleElement, "=" );
        String localizationFileName = localizationBundle.nextToken();
        String localizationFile = localizationBundle.nextToken();

        if ( localizationFileName.endsWith( ".properties" ) ) {
          String encoding = csvUtils.getEncoding( localizationFile );
          if ( ENCODINGS.contains( encoding ) ) {
            for ( final Pattern propertyBundlePattern : patterns ) {
              final Matcher propertyBundleMatcher = propertyBundlePattern.matcher( localizationFileName );
              if ( propertyBundleMatcher.matches() ) {
                FileInputStream bundleFileInputStream =
                    new FileInputStream( sysTmpDir + File.separatorChar + localizationFile );
                metadataImporter.addLocalizationFile( domainId, propertyBundleMatcher.group( 2 ),
                    bundleFileInputStream, true );
                break;
              }
            }
          } else {
            validPropertyFiles = false;
            invalidFiles.append( localizationFileName );
          }
        } else {
          validPropertyFiles = false;
          invalidFiles.append( localizationFileName );
        }
      }

      if ( !validPropertyFiles ) {
        return Response.serverError().entity(
            Messages.getString( "MetadataDatasourceService.ERROR_002_PROPERTY_FILES_ERROR" ) + invalidFiles.toString() )
            .build();
      }
      return Response.ok( "SUCCESS" ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( Exception e ) {
      metadataImporter.removeDomain( domainId );
      return Response.serverError().entity(
          Messages.getString( "MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR" ) ).build();
    }
  }

  /**
   * @param metadataFile
   *          Input stream for the metadata.xmi
   * @param domainId
   *          Unique identifier for the metadata datasource
   * 
   * @return Response containing the success of the method
   * 
   * @throws PentahoAccessControlException
   *           Thrown when validation of access fails
   */
  @PUT
  @Path( "/metadata/storeDomain" )
  @Consumes( { MediaType.APPLICATION_OCTET_STREAM, TEXT_PLAIN } )
  @Produces( "text/plain" )
  @Facet( name = "Unsupported" )
  public Response storeDomain( InputStream metadataFile, @QueryParam( "domainId" ) String domainId )
    throws PentahoAccessControlException {
    try {
      DatasourceService.validateAccess();
      PentahoMetadataDomainRepository metadataImporter =
          new PentahoMetadataDomainRepository( PentahoSystem.get( IUnifiedRepository.class ) );
      metadataImporter.storeDomain( metadataFile, domainId, true );
      return Response.ok( "SUCCESS" ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( PentahoAccessControlException e ) {
      return Response.serverError().entity( e.toString() ).build();
    } catch ( Exception e ) {
      return Response.serverError().entity(
          Messages.getString( "MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR" ) ).build();
    }
  }

  /**
   * @param domainId
   *          Unique identifier for the metadata datasource
   * @param locale
   *          String value of the locale
   * @param propertiesFile
   *          Input stream of the properties file
   * 
   * @return Response containing the success of the method
   * 
   * @throws PentahoAccessControlException
   *           Thrown when validation of access fails
   */
  @PUT
  @Path( "/metadata/addLocalizationFile" )
  @Consumes( { MediaType.APPLICATION_OCTET_STREAM, TEXT_PLAIN } )
  @Produces( "text/plain" )
  @Facet( name = "Unsupported" )
  public Response addLocalizationFile( @QueryParam( "domainId" ) String domainId,
      @QueryParam( "locale" ) String locale, InputStream propertiesFile ) throws PentahoAccessControlException {
    try {
      DatasourceService.validateAccess();
      PentahoMetadataDomainRepository metadataImporter =
          new PentahoMetadataDomainRepository( PentahoSystem.get( IUnifiedRepository.class ) );
      metadataImporter.addLocalizationFile( domainId, locale, propertiesFile, true );
      return Response.ok( "SUCCESS" ).type( MediaType.TEXT_PLAIN ).build();
    } catch ( PentahoAccessControlException e ) {
      return Response.serverError().entity( e.toString() ).build();
    } catch ( Exception e ) {
      return Response.serverError().entity(
          Messages.getString( "MetadataDatasourceService.ERROR_001_METADATA_DATASOURCE_ERROR" ) ).build();
    }
  }

  /**
   * Returns the list of database connections
   * 
   * @return List of database connections
   * 
   * @throws ConnectionServiceException
   */
  @GET
  @Path( "/connection/list" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public IDatabaseConnectionList getConnections() throws ConnectionServiceException {
    IDatabaseConnectionList databaseConnections = new DefaultDatabaseConnectionList();
    List<IDatabaseConnection> conns = connectionService.getConnections();
    for ( IDatabaseConnection conn : conns ) {
      hidePassword( conn );
    }
    databaseConnections.setDatabaseConnections( conns );
    return databaseConnections;
  }

  /**
   * Returns the list of database connections
   * 
   * @param name
   *          String representing the name of the database to return
   * @return Database connection by name
   * 
   * @throws ConnectionServiceException
   */
  @GET
  @Path( "/connection/get" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public IDatabaseConnection getConnectionByName( @QueryParam( "name" ) String name ) throws ConnectionServiceException {
    IDatabaseConnection conn = connectionService.getConnectionByName( name );
    hidePassword( conn );
    return conn;
  }

  /**
   * Returns a response based on the existence of a database connection
   * 
   * @param name
   *          String representing the name of the database to check
   * @return Response based on the boolean value of the connection existing
   * 
   * @throws ConnectionServiceException
   */
  @GET
  @Path( "/connection/checkexists" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public Response isConnectionExist( @QueryParam( "name" ) String name ) throws ConnectionServiceException {
    boolean exists = connectionService.isConnectionExist( name );
    try {
      if ( exists ) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  /**
   * this is a method to return a response object with an error message use getEntity(Connection.class) and getStatus()
   * to determine success
   */
  @GET
  @Path( "/connection/getresponse" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public Response getConnectionByNameWithResponse( @QueryParam( "name" ) String name )
    throws ConnectionServiceException {
    IDatabaseConnection conn = null;
    Response response;
    try {
      conn = connectionService.getConnectionByName( name );
      hidePassword( conn );
      response = Response.ok().entity( conn ).build();
    } catch ( Exception ex ) {
      response = Response.serverError().entity( ex.getMessage() ).build();
    }
    return response;
  }

  /**
   * Add a database connection
   * 
   * @param connection
   *          A database connection object to add
   * @return Response indicating the success of this operation
   * 
   * @throws ConnectionServiceException
   */
  @POST
  @Path( "/connection/add" )
  @Consumes( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public Response addConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    try {
      validateAccess();
      boolean success = connectionService.addConnection( connection );
      if ( success ) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  /**
   * Update an existing database connection
   * 
   * @param connection
   *          Database connection object to update
   * @return Response indicating the success of this operation
   * 
   * @throws ConnectionServiceException
   */
  @POST
  @Path( "/connection/update" )
  @Consumes( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public Response updateConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    try {
      applySavedPassword( connection );
      boolean success = connectionService.updateConnection( connection );
      if ( success ) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  /**
   * Delete an existing database connection
   * 
   * @param connection
   *          Database connection object to delete
   * @return Response indicating the success of this operation
   * 
   * @throws ConnectionServiceException
   */
  @DELETE
  @Path( "/connection/delete" )
  @Consumes( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public Response deleteConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    try {
      boolean success = connectionService.deleteConnection( connection );
      if ( success ) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch ( Throwable t ) {
      t.printStackTrace();
      return Response.serverError().build();
    }
  }

  /**
   * Delete an existing database connection by name
   * 
   * @param name
   *          String representing the name of the database connection to delete
   * @return Response indicating the success of this operation
   * 
   * @throws ConnectionServiceException
   */
  @DELETE
  @Path( "/connection/deletebyname" )
  public Response deleteConnectionByName( @QueryParam( "name" ) String name ) throws ConnectionServiceException {
    try {
      boolean success = connectionService.deleteConnection( name );
      if ( success ) {
        return Response.ok().build();
      } else {
        return Response.notModified().build();
      }
    } catch ( Throwable t ) {
      return Response.serverError().build();
    }
  }

  /**
   * Tests the database connection
   * 
   * @param connection
   *          Database connection object to test
   * @return Response based on the boolean value of the connection test
   * @throws ConnectionServiceException
   */
  @PUT
  @Path( "/connection/test" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { TEXT_PLAIN } )
  @Facet( name = "Unsupported" )
  public Response testConnection( DatabaseConnection connection ) throws ConnectionServiceException {
    boolean success = false;
    applySavedPassword( connection );
    success = connectionService.testConnection( connection );
    if ( success ) {
      return Response.ok(
          Messages.getString( "ConnectionServiceImpl.INFO_0001_CONNECTION_SUCCEED", connection.getDatabaseName() ) )
          .build();
    } else {
      return Response.serverError()
          .entity(
              Messages.getErrorString( "ConnectionServiceImpl.ERROR_0009_CONNECTION_FAILED", connection
                  .getDatabaseName() ) ).build();
    }
  }

  private static final DatabaseConnectionPoolParameter[] poolingParameters =
      new DatabaseConnectionPoolParameter[] {
        new DatabaseConnectionPoolParameter( "defaultAutoCommit", "true",
            "The default auto-commit state of connections created by this pool." ),
        new DatabaseConnectionPoolParameter(
            "defaultReadOnly",
            null,
            "The default read-only state of connections created by this pool.\nIf not set then the setReadOnly method will not be called.\n (Some drivers don't support read only mode, ex: Informix)" ),
        new DatabaseConnectionPoolParameter(
            "defaultTransactionIsolation",
            null,
            "the default TransactionIsolation state of connections created by this pool. One of the following: (see javadoc)\n\n  * NONE\n  * READ_COMMITTED\n  * READ_UNCOMMITTED\n  * REPEATABLE_READ  * SERIALIZABLE\n" ),
        new DatabaseConnectionPoolParameter( "defaultCatalog", null,
            "The default catalog of connections created by this pool." ),
        new DatabaseConnectionPoolParameter( "initialSize", "0",
            "The initial number of connections that are created when the pool is started." ),
        new DatabaseConnectionPoolParameter(
            "maxActive",
            "8",
            "The maximum number of active connections that can be allocated from this pool at the same time, or non-positive for no limit." ),
        new DatabaseConnectionPoolParameter(
            "maxIdle",
            "8",
            "The maximum number of connections that can remain idle in the pool, without extra ones being released, or negative for no limit." ),
        new DatabaseConnectionPoolParameter(
            "minIdle",
            "0",
            "The minimum number of connections that can remain idle in the pool, without extra ones being created, or zero to create none." ),
        new DatabaseConnectionPoolParameter(
            "maxWait",
            "-1",
            "The maximum number of milliseconds that the pool will wait (when there are no available connections) for a connection to be returned before throwing an exception, or -1 to wait indefinitely." ),
        new DatabaseConnectionPoolParameter(
            "validationQuery",
            null,
            "The SQL query that will be used to validate connections from this pool before returning them to the caller.\nIf specified, this query MUST be an SQL SELECT statement that returns at least one row." ),
        new DatabaseConnectionPoolParameter(
            "testOnBorrow",
            "true",
            "The indication of whether objects will be validated before being borrowed from the pool.\nIf the object fails to validate, it will be dropped from the pool, and we will attempt to borrow another.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string." ),
        new DatabaseConnectionPoolParameter(
            "testOnReturn",
            "false",
            "The indication of whether objects will be validated before being returned to the pool.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string." ),
        new DatabaseConnectionPoolParameter(
            "testWhileIdle",
            "false",
            "The indication of whether objects will be validated by the idle object evictor (if any). If an object fails to validate, it will be dropped from the pool.\nNOTE - for a true value to have any effect, the validationQuery parameter must be set to a non-null string." ),
        new DatabaseConnectionPoolParameter(
            "timeBetweenEvictionRunsMillis",
            null,
            "The number of milliseconds to sleep between runs of the idle object evictor thread. When non-positive, no idle object evictor thread will be run." ),
        new DatabaseConnectionPoolParameter( "poolPreparedStatements", "false",
            "Enable prepared statement pooling for this pool." ),
        new DatabaseConnectionPoolParameter(
            "maxOpenPreparedStatements",
            "-1",
            "The maximum number of open statements that can be allocated from the statement pool at the same time, or zero for no limit." ),
        new DatabaseConnectionPoolParameter( "accessToUnderlyingConnectionAllowed", "false",
            "Controls if the PoolGuard allows access to the underlying connection." ),
        new DatabaseConnectionPoolParameter(
            "removeAbandoned",
            "false",
            "Flag to remove abandoned connections if they exceed the removeAbandonedTimout.\nIf set to true a connection is considered abandoned and eligible for removal if it has been idle longer than the removeAbandonedTimeout. Setting this to true can recover db connections from poorly written applications which fail to close a connection." ),
        new DatabaseConnectionPoolParameter( "removeAbandonedTimeout", "300",
            "Timeout in seconds before an abandoned connection can be removed." ),
        new DatabaseConnectionPoolParameter(
            "logAbandoned",
            "false",
            "Flag to log stack traces for application code which abandoned a Statement or Connection.\nLogging of abandoned Statements and Connections adds overhead for every Connection open or new Statement because a stack trace has to be generated." ), };

  /**
   * Returns a list of the database connection pool parameters
   * 
   * @return IDatabaseConnectionPoolParameterList a list of the pooling parameters
   */
  @GET
  @Path( "/connection/poolingParameters" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public IDatabaseConnectionPoolParameterList getPoolingParameters() {
    IDatabaseConnectionPoolParameterList value = new DefaultDatabaseConnectionPoolParameterList();
    List<IDatabaseConnectionPoolParameter> paramList = new ArrayList<IDatabaseConnectionPoolParameter>();
    for ( DatabaseConnectionPoolParameter param : poolingParameters ) {
      paramList.add( param );
    }
    value.setDatabaseConnectionPoolParameters( paramList );
    return value;
  }

  /**
   * Create a database connection
   * 
   * @param driver
   *          String name of the driver to use
   * @param url
   *          String name of the url used to create the connection.
   * 
   * @return IDatabaseConnection for the given parameters
   */
  @GET
  @Path( "/connection/createDatabaseConnection" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public IDatabaseConnection createDatabaseConnection( @QueryParam( "driver" ) String driver,
      @QueryParam( "url" ) String url ) {
    for ( IDatabaseDialect dialect : dialectService.getDatabaseDialects() ) {
      if ( dialect.getNativeDriver() != null && dialect.getNativeDriver().equals( driver ) ) {
        if ( dialect.getNativeJdbcPre() != null && url.startsWith( dialect.getNativeJdbcPre() ) ) {
          return dialect.createNativeConnection( url );
        }
      }
    }

    // if no native driver was found, create a custom dialect object.

    IDatabaseConnection conn = genericDialect.createNativeConnection( url );
    conn.getAttributes().put( GenericDatabaseDialect.ATTRIBUTE_CUSTOM_DRIVER_CLASS, driver );

    return conn;
  }

  /**
   * Returns the database meta for the given connection.
   * 
   * @param connection
   *          DatabaseConnection to retrieve meta from
   * 
   * @return array containing the database connection metadata
   */
  @POST
  @Path( "/connection/checkParams" )
  @Consumes( { APPLICATION_JSON } )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public StringArrayWrapper checkParameters( DatabaseConnection connection ) {
    StringArrayWrapper array = null;
    String[] rawValues = DatabaseUtil.convertToDatabaseMeta( connection ).checkParameters();
    if ( rawValues.length > 0 ) {
      array = new StringArrayWrapper();
      array.setArray( rawValues );
    }
    return array;
  }

  /**
   * internal validation of authorization
   * 
   * @throws PentahoAccessControlException
   */
  private void validateAccess() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    boolean isAdmin =
        policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
            && ( policy.isAllowed( AdministerSecurityAction.NAME ) || policy.isAllowed( PublishAction.NAME ) );
    if ( !isAdmin ) {
      throw new PentahoAccessControlException( "Access Denied" );
    }
  }

  /**
   * Hides password for connections for return to user.
   */
  private void hidePassword( IDatabaseConnection conn ) {
    conn.setPassword( null );
  }

  /**
   * If password is empty, that means connection sent from UI and user didn't change password. Since we cleaned password
   * during sending to UI, we need to use stored password.
   */
  private void applySavedPassword( IDatabaseConnection conn ) throws ConnectionServiceException {
    if ( StringUtils.isBlank( conn.getPassword() ) ) {
      IDatabaseConnection savedConn;
      if ( conn.getId() != null ) {
        savedConn = connectionService.getConnectionById( conn.getId() );
      } else {
        savedConn = connectionService.getConnectionByName( conn.getName() );
      }
      conn.setPassword( savedConn.getPassword() );
    }
  }
  /**
   * Returns a response with id of a database connection
   * 
   * @param name
   *          String representing the name of the database to search
   * @return Response based on the string value of the connection id
   * 
   * @throws ConnectionServiceException
   */
  @GET
  @Path( "/connection/getid" )
  @Produces( { APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public Response getConnectionIdByNameWithResponse( @QueryParam( "name" ) String name )
    throws ConnectionServiceException {
    IDatabaseConnection conn = null;
    Response response;
    try {
      conn = connectionService.getConnectionByName( name );
      if ( conn != null ) {
        response = Response.ok().entity( conn.getId() ).build();
      } else {
        response = Response.notModified().build();
      }
    } catch ( Exception ex ) {
      response = Response.serverError().entity( ex.getMessage() ).build();
    }
    return response;
  }
}
