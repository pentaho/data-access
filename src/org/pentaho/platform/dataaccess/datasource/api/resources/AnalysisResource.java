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

package org.pentaho.platform.dataaccess.datasource.api.resources;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

import org.codehaus.enunciate.Facet;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclDto;
import org.pentaho.platform.web.http.api.resources.JaxbList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.*;
import static javax.ws.rs.core.Response.Status.*;

/**
 * This service allows for listing, download, upload, and removal of Analysis files or Mondrian schemas in the BA
 * Platform.
 */
@Path( "/data-access/api/datasource/analysis" )
public class AnalysisResource {

  protected static final String UPLOAD_ANALYSIS = "uploadInput";
  protected static final String CATALOG_ID = "catalogId";
  protected static final String ORIG_CATALOG_NAME = "origCatalogName";
  protected static final String DATASOURCE_NAME = "datasourceName";
  protected static final String OVERWRITE_IN_REPOS = "overwrite";
  protected static final String XMLA_ENABLED_FLAG = "xmlaEnabledFlag";
  protected static final String PARAMETERS = "parameters";
  private static final String DATASOURCE_ACL = "acl";
  private static final int SUCCESS = 3;
  protected static final Logger logger = LoggerFactory.getLogger( AnalysisResource.class );

  protected AnalysisService service;
  protected ResourceUtil resourceUtil;

  public AnalysisResource() {
    service = createAnalysisService();
    resourceUtil = new ResourceUtil();
  }

  protected AnalysisService createAnalysisService() {
    return new AnalysisService();
  }

  /**
   * Download the analysis files for a given analysis id.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/plugin/data-access/api/datasource/analysis/catalog/SampleSchema
   * </p>
   *
   * @param catalog String Id of the analysis data to retrieve.
   *
   * @return Response containing the analysis file data XML.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      &lt;?xml version=&quot;1.0&quot;?&gt;
   *      &lt;Schema name=&quot;SampleData2&quot;&gt;
   *        &lt;Dimension name=&quot;Region&quot;&gt;
   *          &lt;Hierarchy hasAll=&quot;true&quot; allMemberName=&quot;All Regions&quot;&gt;
   *            &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *            &lt;Level name=&quot;Region&quot; column=&quot;REGION&quot; uniqueMembers=&quot;true&quot;/&gt;
   *          &lt;/Hierarchy&gt;
   *        &lt;/Dimension&gt;
   *        &lt;Dimension name=&quot;Department&quot;&gt;
   *          &lt;Hierarchy hasAll=&quot;true&quot; allMemberName=&quot;All Departments&quot;&gt;
   *            &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *            &lt;Level name=&quot;Department&quot; column=&quot;DEPARTMENT&quot; uniqueMembers=&quot;true&quot;/&gt;
   *          &lt;/Hierarchy&gt;
   *        &lt;/Dimension&gt;
   *        &lt;Dimension name=&quot;Positions&quot;&gt;
   *          &lt;Hierarchy hasAll=&quot;true&quot; allMemberName=&quot;All Positions&quot;&gt;
   *            &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *            &lt;Level name=&quot;Positions&quot; column=&quot;POSITIONTITLE&quot; uniqueMembers=&quot;true&quot;/&gt;
   *          &lt;/Hierarchy&gt;
   *        &lt;/Dimension&gt;
   *        &lt;Cube name=&quot;Quadrant Analysis&quot;&gt;
   *          &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *          &lt;DimensionUsage name=&quot;Region&quot; source=&quot;Region&quot;/&gt;
   *          &lt;DimensionUsage name=&quot;Department&quot; source=&quot;Department&quot; /&gt;
   *          &lt;DimensionUsage name=&quot;Positions&quot; source=&quot;Positions&quot; /&gt;
   *          &lt;Measure name=&quot;Actual&quot; column=&quot;ACTUAL&quot; aggregator=&quot;sum&quot; formatString=&quot;#,###.00&quot;/&gt;
   *          &lt;Measure name=&quot;Budget&quot; column=&quot;BUDGET&quot; aggregator=&quot;sum&quot; formatString=&quot;#,###.00&quot;/&gt;
   *          &lt;Measure name=&quot;Variance&quot; column=&quot;VARIANCE&quot; aggregator=&quot;sum&quot; formatString=&quot;#,###.00&quot;/&gt;
   *        &lt;/Cube&gt;
   *      &lt;/Schema&gt;
   *    </pre>
   */
  @GET
  @Path( "/catalog/{catalogId : .+}" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully downloaded the analysis file" ),
      @ResponseCode( code = 401, condition = "Unauthorized" ),
      @ResponseCode( code = 500, condition = "Unabled to download analysis file" )
  } )
  public Response downloadSchema( @PathParam( CATALOG_ID ) String catalog ) {
    try {
      Map<String, InputStream> fileData = service.doGetAnalysisFilesAsDownload( catalog );
      return createAttachment( fileData, catalog );
    } catch ( PentahoAccessControlException e ) {
      throw new WebApplicationException( Response.Status.UNAUTHORIZED );
    }
  }

  /**
   * Remove the analysis data for a given analysis ID.
   *
   * <p><b>Example Request:</b><br />
   *    DELETE pentaho/plugin/data-access/api/datasource/analysis/catalog/{catalog}
   *
   * @param catalog ID of the analysis data to remove.
   *
   * @return A 200 response code representing the successful removal of the analysis datasource.
   *
   * <p><b>Example Response:</b></p>
   *    <pre function="syntax.xml">
   *      This response does not contain data.
   *    </pre>
   */
  @DELETE
  @Path( "/catalog/{catalogId : .+}" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully removed the analysis data" ),
      @ResponseCode( code = 401, condition = "User is not authorized to delete the analysis datasource" ),
      @ResponseCode( code = 500, condition = "Unable to remove the analysis data." )
  } )
  public Response deleteSchema( @PathParam( CATALOG_ID ) String catalog ) {
    try {
      service.removeAnalysis( catalog );
      return buildOkResponse();
    } catch ( PentahoAccessControlException e ) {
      throw new WebApplicationException( Response.Status.UNAUTHORIZED );
    }
  }

  /**
   * Get a list of analysis data source ids.
   *
   * <p><b>Example Request:</b><br />
   *    GET pentaho/plugin/data-access/api/datasource/analysis/catalog
   * </p>
   *
   * @return A list of catalog IDs.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *  {
   *   "Item":[
   *     {
   *       "@type":"xs:string",
   *       "$":"SampleData"
   *     },
   *     {
   *       "@type":"xs:string",
   *       "$":"SteelWheels"
   *     },
   *     {
   *       "@type":"xs:string",
   *       "$":"pentaho_operations_mart"
   *     }
   *   ]
   *  }
   * </pre>
   */
  @GET
  @Path( "/catalog" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully retrieved the list of analysis IDs" )
  } )
  public JaxbList<String> getSchemaIds() {
    return createNewJaxbList( service.getAnalysisDatasourceIds() );
  }

  /**
   * Import Analysis Schema.
   *
   * <p><b>Example Request:</b><br />
   *    PUT pentaho/plugin/data-access/api/datasource/analysis/catalog/SampleSchema
   * <br /><b>PUT data:</b>
   *  <pre function="syntax.xml">
   *      ------WebKitFormBoundaryNLNb246RTFIn1elY
   *      Content-Disposition: form-data; name=&quot;uploadAnalysis&quot;; filename=&quot;SampleData2.mondrian.xml&quot;
   *      Content-Type: text/xml
   *
   *      &lt;?xml version=&quot;1.0&quot;?&gt;
   *      &lt;Schema name=&quot;SampleData2&quot;&gt;
   *      &lt;!-- Shared dimensions --&gt;
   *
   *      &lt;Dimension name=&quot;Region&quot;&gt;
   *      &lt;Hierarchy hasAll=&quot;true&quot; allMemberName=&quot;All Regions&quot;&gt;
   *      &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *      &lt;Level name=&quot;Region&quot; column=&quot;REGION&quot; uniqueMembers=&quot;true&quot;/&gt;
   *      &lt;/Hierarchy&gt;
   *      &lt;/Dimension&gt;
   *      &lt;Dimension name=&quot;Department&quot;&gt;
   *      &lt;Hierarchy hasAll=&quot;true&quot; allMemberName=&quot;All Departments&quot;&gt;
   *      &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *      &lt;Level name=&quot;Department&quot; column=&quot;DEPARTMENT&quot; uniqueMembers=&quot;true&quot;/&gt;
   *      &lt;/Hierarchy&gt;
   *      &lt;/Dimension&gt;
   *
   *      &lt;Dimension name=&quot;Positions&quot;&gt;
   *      &lt;Hierarchy hasAll=&quot;true&quot; allMemberName=&quot;All Positions&quot;&gt;
   *      &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *      &lt;Level name=&quot;Positions&quot; column=&quot;POSITIONTITLE&quot; uniqueMembers=&quot;true&quot;/&gt;
   *      &lt;/Hierarchy&gt;
   *      &lt;/Dimension&gt;
   *
   *      &lt;Cube name=&quot;Quadrant Analysis&quot;&gt;
   *      &lt;Table name=&quot;QUADRANT_ACTUALS&quot;/&gt;
   *      &lt;DimensionUsage name=&quot;Region&quot; source=&quot;Region&quot;/&gt;
   *      &lt;DimensionUsage name=&quot;Department&quot; source=&quot;Department&quot; /&gt;
   *      &lt;DimensionUsage name=&quot;Positions&quot; source=&quot;Positions&quot; /&gt;
   *      &lt;Measure name=&quot;Actual&quot; column=&quot;ACTUAL&quot; aggregator=&quot;sum&quot; formatString=&quot;#,###.00&quot;/&gt;
   *      &lt;Measure name=&quot;Budget&quot; column=&quot;BUDGET&quot; aggregator=&quot;sum&quot; formatString=&quot;#,###.00&quot;/&gt;
   *      &lt;Measure name=&quot;Variance&quot; column=&quot;VARIANCE&quot; aggregator=&quot;sum&quot; formatString=&quot;#,###.00&quot;/&gt;
   *      &lt;!--    &lt;CalculatedMember name=&quot;Variance Percent&quot; dimension=&quot;Measures&quot; formula=&quot;([Measures].[Variance]/[Measures].[Budget])*100&quot; /&gt; --&gt;
   *      &lt;/Cube&gt;
   *
   *      &lt;/Schema&gt;
   *
   *      ------WebKitFormBoundaryNLNb246RTFIn1elY
   *      Content-Disposition: form-data; name=&quot;parameters&quot;
   *
   *      DataSource=SampleData2;overwrite=true
   *      ------WebKitFormBoundaryNLNb246RTFIn1elY
   *      Content-Disposition: form-data; name=&quot;schemaFileInfo&quot;
   *
   *      test.xml
   *      ------WebKitFormBoundaryNLNb246RTFIn1elY
   *      Content-Disposition: form-data; name=&quot;catalogName&quot;
   *
   *      Catalog Name
   *      ------WebKitFormBoundaryNLNb246RTFIn1elY
   *      Content-Disposition: form-data; name=&quot;xmlaEnabledFlag&quot;
   *
   *      true
   *      ------WebKitFormBoundaryNLNb246RTFIn1elY--
   *  </pre>
   * </p>
   *
   * @param catalog         (optional) The catalog name.
   * @param uploadAnalysis  A Mondrian schema XML file.
   * @param schemaFileInfo  User selected name for the file.
   * @param origCatalogName (optional) The original catalog name.
   * @param datasourceName  (optional) The datasource  name.
   * @param overwrite       Flag for overwriting existing version of the file.
   * @param xmlaEnabledFlag Is XMLA enabled or not.
   * @param parameters      Import parameters.
   * @param acl             acl information for the data source. This parameter is optional.
   *
   * @return Response containing the success of the method.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *   200
   * </pre>
   */
  @PUT
  @Path( "/catalog/{catalogId : .+}" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @StatusCodes( {
      @ResponseCode( code = 409, condition = "Content already exists (use overwrite flag to force)" ),
      @ResponseCode( code = 401, condition = "Import failed because publish is prohibited" ),
      @ResponseCode( code = 500, condition = "Unspecified general error has occurred" ),
      @ResponseCode( code = 412,
          condition = "Analysis datasource import failed.  Error code or message included in response entity" ),
      @ResponseCode( code = 403, condition = "Access Control Forbidden" ),
      @ResponseCode( code = 201, condition = "Indicates successful import" ) } )
  public Response putSchema(
      @PathParam( CATALOG_ID ) String catalog, // Optional
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream uploadAnalysis,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) Boolean overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) Boolean xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl )
      throws PentahoAccessControlException {
    try {
      service.putMondrianSchema( uploadAnalysis, schemaFileInfo, catalog, origCatalogName, datasourceName, overwrite,
          xmlaEnabledFlag, parameters, acl );
      Response response = Response.status( CREATED ).build();
      logger.debug( "putMondrianSchema Response " + response );
      return response;
    } catch ( PentahoAccessControlException pac ) {
      int statusCode = PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL;
      logger.error( "Error putMondrianSchema " + pac.getMessage() + " status = " + statusCode );
      throw new ResourceUtil.AccessControlException( pac.getMessage() );
    } catch ( PlatformImportException pe ) {


      if ( pe.getErrorStatus() == PlatformImportException.PUBLISH_PROHIBITED_SYMBOLS_ERROR ) {
        throw new ResourceUtil.PublishProhibitedException( pe.getMessage() );
      } else {
        String msg = pe.getMessage();
        logger.error( "Error import analysis: " + msg + " status = " + pe.getErrorStatus() );
        Throwable throwable = pe.getCause();
        if ( throwable != null ) {
          msg = throwable.getMessage();
          logger.error( "Root cause: " + msg );
        }
        int status = pe.getErrorStatus();
        if ( status == 8 ) {
          throw new ResourceUtil.ContentAlreadyExistsException( msg );
        } else {
          throw new ResourceUtil.ImportFailedException( msg );
        }
      }
    } catch ( Exception e ) {
      int statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
      logger.error( "Error putMondrianSchema " + e.getMessage() + " status = " + statusCode );
      throw new ResourceUtil.UnspecifiedErrorException( e.getMessage() );
    }
  }

  public Response importMondrianSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream uploadAnalysis,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_ID ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl )
      throws PentahoAccessControlException {
    Response response = null;
    int statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    try {
      boolean overWriteInRepository = "True".equalsIgnoreCase( overwrite ) ? true : false;
      boolean xmlaEnabled = "True".equalsIgnoreCase( xmlaEnabledFlag ) ? true : false;
      service.putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
          overWriteInRepository, xmlaEnabled, parameters, acl );
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

    response = buildOkResponse( String.valueOf( statusCode ) );
    logger.debug( "putMondrianSchema Response " + response );
    return response;
  }

  protected JaxbList<String> createNewJaxbList( List<String> DSWDatasources ) {
    return new JaxbList<String>( DSWDatasources );
  }

  protected Response buildOkResponse( String statusCode ) {
    return Response.ok( statusCode ).type( MediaType.TEXT_PLAIN ).build();
  }

  protected Response createAttachment( Map<String, InputStream> fileData, String catalog ) {
    return resourceUtil.createAttachment( fileData, catalog );
  }

  protected Response buildOkResponse() {
    return Response.ok().build();
  }

  protected Response buildUnauthorizedResponse() {
    return Response.status( UNAUTHORIZED ).build();
  }

  protected Response buildServerErrorResponse() {
    return Response.serverError().build();
  }

  /**
   * Get list of IDs of analysis datasource
   *
   * @return JaxbList<String> of analysis IDs
   */
  @GET
  @Path( "/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @Facet( name = "Unsupported" )
  public JaxbList<String> getAnalysisDatasourceIds() {
    return getSchemaIds();
  }

  @GET
  @Path( "/{catalog : .+}/download" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully downloaded the analysis file" ),
      @ResponseCode( code = 401, condition = "Unauthorized" ),
      @ResponseCode( code = 500, condition = "Unabled to download analysis file" )
  } )

  public Response doGetAnalysisFilesAsDownload( @PathParam( "catalog" ) String catalog ) {
    return downloadSchema( catalog );
  }

  @PUT
  @Path( "/import" )
  @Consumes( MediaType.MULTIPART_FORM_DATA )
  @Produces( "text/plain" )
  @StatusCodes( {
      @ResponseCode( code = 200,
          condition = "Status code indicating a success or failure while importing Mondrian schema XML. A response of:\n"
              + "   *  2: Unspecified general error has occurred\n"
              + "   *  3: Success\n"
              + "   *  5: Authorization error" )
  } )

  public Response putMondrianSchema(
      @FormDataParam( UPLOAD_ANALYSIS ) InputStream uploadAnalysis,
      @FormDataParam( UPLOAD_ANALYSIS ) FormDataContentDisposition schemaFileInfo,
      @FormDataParam( CATALOG_ID ) String catalogName, // Optional
      @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
      @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
      @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
      @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters,
      @FormDataParam( DATASOURCE_ACL ) RepositoryFileAclDto acl )
      throws PentahoAccessControlException {
    return importMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
        overwrite, xmlaEnabledFlag, parameters, acl );
  }

  @POST
  @Path( "/{catalog : .+}/remove" )
  @Produces( WILDCARD )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully removed the analysis data" ),
      @ResponseCode( code = 401, condition = "User is not authorized to delete the analysis datasource" ),
      @ResponseCode( code = 500, condition = "Unable to remove the analysis data." )
  } )
  public Response doRemoveAnalysis( @PathParam( "catalog" ) String catalog ) {
    try {
      service.removeAnalysis( catalog );
      return buildOkResponse();
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    }
  }

  /**
   * Get ACL for the analysis data source by name
   *
   * @param catalog analysis data source name
   * @return        ACL or null if the data source doesn't have it
   * @throws        PentahoAccessControlException if the user doesn't have access
   */
  @GET
  @Path( "/{catalog : .+}/acl" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully got the ACL" ),
      @ResponseCode( code = 401, condition = "Unauthorized" ),
      @ResponseCode( code = 404, condition = "ACL doesn't exist" ),
      @ResponseCode( code = 409, condition = "Analysis DS doesn't exist" ),
      @ResponseCode(
          code = 500,
          condition = "ACL failed to be retrieved. This could be caused by an invalid path, or the file does not exist."
      )
      } )
      public RepositoryFileAclDto doGetAnalysisDatasourceAcl( @PathParam( "catalog" ) String catalog ) {
    try {
      final RepositoryFileAclDto acl = service.getAnalysisDatasourceAcl( catalog );
      if ( acl == null ) {
        throw new WebApplicationException( NOT_FOUND );
      }
      return acl;
    } catch ( FileNotFoundException e ) {
      throw new WebApplicationException( CONFLICT );
    } catch ( PentahoAccessControlException e ) {
      throw new WebApplicationException( UNAUTHORIZED );
    }
  }

  /**
   * Set ACL for the analysis data source
   *
   * @param catalog analysis data source name
   * @param acl     ACL to set
   * @return        response
   * @throws        PentahoAccessControlException if the user doesn't have access
   */
  @PUT
  @Path( "/{catalog : .+}/acl" )
  @Produces ( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( {
      @ResponseCode( code = 200, condition = "Successfully updated the ACL" ),
      @ResponseCode( code = 401, condition = "Unauthorized" ),
      @ResponseCode( code = 409, condition = "Analysis DS doesn't exist" ),
      @ResponseCode( code = 500, condition = "Failed to save acls due to another error." )
      } )
      public Response doSetAnalysisDatasourceAcl( @PathParam( "catalog" ) String catalog, RepositoryFileAclDto acl )
      throws PentahoAccessControlException {
    try {
      service.setAnalysisDatasourceAcl( catalog, acl );
      return buildOkResponse();
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    } catch ( FileNotFoundException e ) {
      return Response.status( CONFLICT ).build();
    } catch ( Exception e ) {
      return buildServerErrorResponse();
    }
  }
}
