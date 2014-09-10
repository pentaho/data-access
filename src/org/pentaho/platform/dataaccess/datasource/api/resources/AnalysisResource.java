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

package org.pentaho.platform.dataaccess.datasource.api.resources;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.enunciate.jaxrs.ResponseCode;
import org.codehaus.enunciate.jaxrs.StatusCodes;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.dataaccess.datasource.api.AnalysisService;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.web.http.api.resources.JaxbList;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

public class AnalysisResource {

  private static final String UPLOAD_ANALYSIS = "uploadAnalysis";
  private static final String CATALOG_NAME = "catalogName";
  private static final String ORIG_CATALOG_NAME = "origCatalogName";
  private static final String DATASOURCE_NAME = "datasourceName";
  private static final String OVERWRITE_IN_REPOS = "overwrite";
  private static final String XMLA_ENABLED_FLAG = "xmlaEnabledFlag";
  private static final String PARAMETERS = "parameters";
  private static final int SUCCESS = 3;
  private static final Log logger = LogFactory.getLog( AnalysisResource.class );

  protected AnalysisService service;
  protected ResourceUtil resourceUtil;

  public AnalysisResource() {
    service = new AnalysisService();
    resourceUtil = new ResourceUtil();
  }

  /**
   * Download the analysis files for a given analysis id.
   *
   * <p><b>Example Request:</b><br />
   *    GET plugin/data-access/api/datasource/analysis/{analysisId}/download
   * </p>
   *
   * @param analysisId String Id of the analysis data to retrieve
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
  @Path( "/{analysisId : .+}/download" )
  @Produces( WILDCARD )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully downloaded the analysis file" ),
    @ResponseCode( code = 401, condition = "Unauthorized" ),
    @ResponseCode( code = 500, condition = "Unabled to download analysis file" )
  } )
  public Response doGetAnalysisFilesAsDownload( @PathParam( "analysisId" ) String analysisId ) {
    try {
      Map<String, InputStream> fileData = service.doGetAnalysisFilesAsDownload( analysisId );
      return createAttachment( fileData, analysisId );
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    }
  }

  /**
   * Remove the analysis data for a given analysis ID.
   *
   * <p><b>Example Request:</b><br />
   *    POST plugin/data-access/api/datasource/analysis/{analysisId}/remove
   * </p>
   *
   * @param analysisId ID of the analysis data to remove
   *
   * @return A 200 response code representing the successful removal of the analysis datasource
   */
  @POST
  @Path( "/{analysisId : .+}/remove" )
  @Produces( WILDCARD )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully removed the analysis data" ),
    @ResponseCode( code = 401, condition = "User is not authorized to delete the analysis datasource" ),
    @ResponseCode( code = 500, condition = "Unable to remove the analysis data." )
  } )
  public Response doRemoveAnalysis( @PathParam( "analysisId" ) String analysisId ) {
    try {
      service.removeAnalysis( analysisId );
      return buildOkResponse();
    } catch ( PentahoAccessControlException e ) {
      return buildUnauthorizedResponse();
    }
  }

  /**
   * Get a list of analysis data source ids.
   *
   * <p><b>Example Request:</b><br />
   *    GET /data-access/api/datasource/analysis/ids
   * </p>
   *
   * @return A list of analysis IDs.
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
  @Path( "/ids" )
  @Produces( { APPLICATION_XML, APPLICATION_JSON } )
  @StatusCodes( {
    @ResponseCode( code = 200, condition = "Successfully retrieved the list of analysis IDs" )
  } )
  public JaxbList<String> getAnalysisDatasourceIds() {
    return createNewJaxbList( service.getAnalysisDatasourceIds() );
  }

  /**
   * Import Mondrian Schema.
   *
   * <p><b>Example Request:</b><br />
   *    PUT /data-access/api/datasource/analysis/import
   * </p>
   *
   * @param uploadAnalysis A Mondrian schema XML file
   * @param schemaFileInfo User selected name for the file
   * @param catalogName (optional) The catalog name
   * @param overwrite Flag for overwriting existing version of the file
   * @param xmlaEnabledFlag Is XMLA enabled or not
   * @param parameters Import parameters
   *
   * @return Response containing the success of the method.
   *
   * <p><b>Example Response:</b></p>
   * <pre function="syntax.xml">
   *   3
   * </pre>
   */
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
    @FormDataParam( CATALOG_NAME ) String catalogName, // Optional
    @FormDataParam( ORIG_CATALOG_NAME ) String origCatalogName, // Optional
    @FormDataParam( DATASOURCE_NAME ) String datasourceName, // Optional
    @FormDataParam( OVERWRITE_IN_REPOS ) String overwrite,
    @FormDataParam( XMLA_ENABLED_FLAG ) String xmlaEnabledFlag, @FormDataParam( PARAMETERS ) String parameters )
    throws PentahoAccessControlException {
    Response response = null;
    int statusCode = PlatformImportException.PUBLISH_GENERAL_ERROR;
    try {
      service.putMondrianSchema( uploadAnalysis, schemaFileInfo, catalogName, origCatalogName, datasourceName,
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

  protected Response createAttachment( Map<String, InputStream> fileData, String domainId  ) {
    return resourceUtil.createAttachment( fileData, domainId );
  }

  protected Response buildOkResponse() {
    return Response.ok().build();
  }

  protected Response buildUnauthorizedResponse() {
    return Response.status( UNAUTHORIZED ).build();
  }
}
