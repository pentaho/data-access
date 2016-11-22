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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.codehaus.enunciate.Facet;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.api.DatasourceService;
import org.pentaho.platform.dataaccess.datasource.api.resources.MetadataResource;
import org.pentaho.platform.dataaccess.datasource.wizard.csv.CsvUtils;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.metadata.PentahoMetadataDomainRepository;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;

@Path( "/data-access/api/metadata" )
public class MetadataDatasourceService {
  
  private static final String OVERWRITE_IN_REPOS = "overwrite";
  private static final String DATASOURCE_ACL = "acl";
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
  @Path( "/addLocalizationFile" )
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
  @Path( "/storeDomain" )
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
  @Path( "/uploadServletImport" )
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
   * @param acl
   *          acl information for the data source. This parameter is optional.
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
  @Path( "/postimport" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/html" )
  @Facet( name = "Unsupported" )
  public Response importMetadataDatasourceWithPost( @FormDataParam( "domainId" ) String domainId,
      @FormDataParam( "metadataFile" ) InputStream metadataFile,
      @FormDataParam( "metadataFile" ) FormDataContentDisposition metadataFileInfo,
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( "localeFiles" ) List<FormDataBodyPart> localeFiles,
      @FormDataParam( "localeFiles" ) List<FormDataContentDisposition> localeFilesInfo,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl )
    throws PentahoAccessControlException {
    Response response =
        importMetadataDatasource( domainId, metadataFile, metadataFileInfo, overwrite, localeFiles, localeFilesInfo, acl );
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
   * @param acl
   *          acl information for the data source. This parameter is optional.
   * 
   * @return Response containing the success of the method
   * 
   */
  @PUT
  @Path( "/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @Deprecated
  @Facet( name = "Unsupported" )
  public Response importMetadataDatasource( @FormDataParam( "domainId" ) String domainId,
      @FormDataParam( "metadataFile" ) InputStream metadataFile,
      @FormDataParam( "metadataFile" ) FormDataContentDisposition metadataFileInfo,
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( "localeFiles" ) List<FormDataBodyPart> localeFiles,
      @FormDataParam( "localeFiles" ) List<FormDataContentDisposition> localeFilesInfo,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl ) {
    return new MetadataResource().importMetadataDatasourceLegacy( domainId, metadataFile, metadataFileInfo, overwrite,
        localeFiles, localeFilesInfo, acl );
  }
}
