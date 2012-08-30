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
 * Copyright 2011 Pentaho Corporation.  All rights reserved.
 *
 *
 * @created Nov 12, 2011
 * @author  Ezequiel Cuellar, 
 *          Tyler Band
 * @modified: July 11, 2012
 * 
 * change notes July 12, 2012
 * The use of the IPlatformImporter can b e found in the SpringObjects.xml file - the new MondrianImportHanlder is registered
 * and can be replaced by a new handler in the future without changes to this code. Note: the original code is left in for backwards
 * compatibility with other existing services (should be replaced in future)
 * 
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import mondrian.olap.Util.PropertyList;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.services.importer.IPlatformImportBundle;
import org.pentaho.platform.plugin.services.importer.IPlatformImporter;
import org.pentaho.platform.plugin.services.importer.PlatformImportException;
import org.pentaho.platform.plugin.services.importer.RepositoryFileImportBundle;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;

@Path("/data-access/api/mondrian")
public class AnalysisDatasourceService {

  private static final String XMLA_ENABLED_FLAG = "xmlaEnabledFlag";

  private static final String CATALOG_NAME = "catalogName";

  private static final String DATASOURCE_NAME = "datasourceName";
  
  private static final String UPLOAD_ANALYSIS = "uploadAnalysis";

  private static final String PARAMETERS = "parameters";

  private static final String OVERWRITE_IN_REPOS = "overwrite";

  private static final String ENABLE_XMLA = "EnableXmla";

  private static final String ACCESS_DENIED = "Access Denied";

  private static final String SUCCESS = "3";
  
  private static final String SUCCESS_MSG = "SUCCESS";

  private final String ACTION_READ = "org.pentaho.repository.read";

  private final String ACTION_CREATE = "org.pentaho.repository.create";

  private final String ACTION_ADMINISTER_SECURITY = "org.pentaho.security.administerSecurity";

  private static final String DOMAIN_ID = "domain-id";

  private static final String UTF_8 = "UTF-8";

  private static final String MONDRIAN_MIME_TYPE = "application/vnd.pentaho.mondrian+xml";

  /*
   * register the handler in the PentahoSpringObjects.xml for MondrianImportHandler
   */
  private static IPlatformImporter importer;

  private static final Log logger = LogFactory.getLog(AnalysisDatasourceService.class);


  
  public AnalysisDatasourceService() {
    super();
    importer = PentahoSystem.get(IPlatformImporter.class);

  }

   
  /**
   * This is used by PUC to use a Jersey put to import a Mondrian Schema XML into PUR
   * @author: tband 
   * date: 7/10/12
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
  @Path("/putSchema")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces("text/plain")
  public Response putMondrianSchema(     
      @FormDataParam(UPLOAD_ANALYSIS) InputStream dataInputStream, 
      @FormDataParam(UPLOAD_ANALYSIS)FormDataContentDisposition schemaFileInfo, 
      @FormDataParam(CATALOG_NAME) String catalogName, //Optional
      @FormDataParam(DATASOURCE_NAME) String datasourceName, //Optional
      @FormDataParam(OVERWRITE_IN_REPOS) String overwrite, 
      @FormDataParam(XMLA_ENABLED_FLAG) String xmlaEnabledFlag,
      @FormDataParam(PARAMETERS) String parameters) throws PentahoAccessControlException {
    Response response = null;
    String statusCode = String.valueOf(PlatformImportException.PUBLISH_GENERAL_ERROR);
    try {
      validateAccess();
      String fileName = schemaFileInfo.getFileName();
      processMondrianImport(dataInputStream, catalogName, overwrite, xmlaEnabledFlag, parameters, fileName);
      statusCode = SUCCESS;
    } catch (PentahoAccessControlException pac) {
      logger.error(pac.getMessage());
      statusCode = String.valueOf(PlatformImportException.PUBLISH_USERNAME_PASSWORD_FAIL);
    } catch (PlatformImportException pe) {
      logger.error("Error putMondrianSchema " + pe.getMessage() + " status = " + pe.getErrorStatus());
      statusCode = String.valueOf(pe.getErrorStatus());
    } catch (Exception e) {
      logger.error("Error putMondrianSchema " + e.getMessage());
      statusCode = String.valueOf(PlatformImportException.PUBLISH_GENERAL_ERROR);
    }

    response = Response.ok().status(new Integer(statusCode)).type(MediaType.TEXT_PLAIN).build();
    logger.debug("putMondrianSchema Response " + response);
    return response;
  }



  /**
   * This is the main method that handles the actual Import Handler to persit to PUR
   * @param dataInputStream
   * @param catalogName
   * @param overwrite
   * @param xmlaEnabledFlag
   * @param parameters
   * @param fileName
   * @throws PlatformImportException
   */
  private void processMondrianImport(InputStream dataInputStream, String catalogName, String overwrite,
      String xmlaEnabledFlag, String parameters, String fileName) throws PlatformImportException {
    boolean overWriteInRepository = determineOverwriteFlag(parameters, overwrite);
 
    IPlatformImportBundle bundle = createPlatformBundle(parameters, dataInputStream, catalogName,
        overWriteInRepository, fileName, xmlaEnabledFlag);
    
    importer.importFile(bundle);
  }

  


  /**
   * This is used by PUC to use a form post to import a Mondrian Schema XML into PUR
   * @author: tband 
   * date: 7/10/12
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
  @Path("/postAnalysis")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces({"text/plain","text/html"})
 
  public Response postMondrainSchema(
      @FormDataParam(UPLOAD_ANALYSIS) InputStream dataInputStream, 
      @FormDataParam(UPLOAD_ANALYSIS)FormDataContentDisposition schemaFileInfo, 
      @FormDataParam(CATALOG_NAME) String catalogName, //Optional
      @FormDataParam(DATASOURCE_NAME) String datasourceName, //Optional
      @FormDataParam(OVERWRITE_IN_REPOS) String overwrite, 
      @FormDataParam(XMLA_ENABLED_FLAG) String xmlaEnabledFlag,
      @FormDataParam(PARAMETERS) String parameters) throws PentahoAccessControlException {
     //use existing Jersey post method - but translate into text/html for PUC Client
     ResponseBuilder responseBuilder;
     Response response = this.putMondrianSchema(dataInputStream, schemaFileInfo, catalogName, datasourceName, overwrite, xmlaEnabledFlag, parameters);
     responseBuilder=  Response.ok().status(200);
     responseBuilder.entity(String.valueOf(response.getStatus()));
     
     return responseBuilder.build();
  }

  /**
   * overloaded method to get the values from the parameter before creating the bundle
   * @param parameters
   * @param dataInputStream
   * @param catalogName
   * @param fileName
   * @return IPlatformImportBundle
   */
  private IPlatformImportBundle createPlatformBundle(String parameters, InputStream dataInputStream,
      String catalogName, String fileName) {
    
    boolean overWriteInRepository = "True".equalsIgnoreCase(getValue(parameters, OVERWRITE_IN_REPOS)) ? true : false;
    String xmlaEnabled = "True".equals(getValue(parameters, "xmlaEnabled"))?"true":"false";
    String domainId = determineDomainCatalogName(parameters, catalogName, fileName);
    
    return createPlatformBundle(parameters, dataInputStream, domainId, overWriteInRepository, fileName, xmlaEnabled);
  }

    /**
     * helper method to calculate the domain id from the parameters, file name, or pass catalog
     * @param parameters
     * @param catalogName
     * @param fileName
     * @return Look up name from parameters or file name or passed in catalog name
     */
  private String determineDomainCatalogName(String parameters, String catalogName, String fileName) {
    String domainId  =  (getValue(parameters, CATALOG_NAME) == null)?catalogName:getValue(parameters, CATALOG_NAME);
    if(domainId == null || "".equals(domainId)){
      if(fileName.contains(".")){
        domainId = fileName.substring(0, fileName.indexOf("."));
      } else {       
          domainId = fileName;       
      }
    } else{
      if(domainId.contains(".")){
        domainId =  domainId.substring(0, domainId.indexOf("."));
      } 
    }
    return domainId;
  }
  /**
   * helper method to create the platform bundle used by the Jcr repository
   * @param parameters
   * @param dataInputStream
   * @param catalogName
   * @param overWriteInRepository
   * @param fileName
   * @param xmlaEnabled
   * @return IPlatformImportBundle
   */
  private IPlatformImportBundle createPlatformBundle(String parameters, InputStream dataInputStream,
      String catalogName, boolean overWriteInRepository, String fileName, String xmlaEnabled) {
    String datasource = getValue(parameters,"Datasource");
    String domainId = this.determineDomainCatalogName(parameters, catalogName, fileName);
    String sep = ";";
    if(StringUtils.isEmpty(parameters)){       
      parameters="Provider=mondrian";
          parameters += sep + DATASOURCE_NAME + "="+datasource; 
          if(!StringUtils.isEmpty(xmlaEnabled))
            parameters += sep + ENABLE_XMLA+"="+ xmlaEnabled;
    }

    RepositoryFileImportBundle.Builder bundleBuilder = new RepositoryFileImportBundle.Builder()
        .input(dataInputStream)
        .charSet(UTF_8).hidden(false)
        .name(domainId)
        .overwrite(overWriteInRepository)
        .mime(MONDRIAN_MIME_TYPE)
        .withParam(PARAMETERS, parameters)        
        .withParam(DOMAIN_ID, domainId);
      //pass as param if not in parameters string
      if(!StringUtils.isEmpty(xmlaEnabled))
        bundleBuilder.withParam(ENABLE_XMLA, xmlaEnabled);
      
    IPlatformImportBundle bundle = bundleBuilder.build();
    return bundle;
  }
  
  /**
   * helper method to calculate the overwrite in repos flag from parameters or passed value
   * @param parameters
   * @param overwrite
   * @return boolean if overwrite is allowed
   */
  private boolean determineOverwriteFlag(String parameters, String overwrite) {
    String overwriteStr = getValue(parameters,OVERWRITE_IN_REPOS);
    boolean overWriteInRepository = "True".equalsIgnoreCase(overwrite) ? true : false;
    if(overwriteStr != null){
      overWriteInRepository = "True".equalsIgnoreCase(overwriteStr) ? true : false;
    }//if there is a conflict - parameters win?
    return overWriteInRepository;
  }
 
  /**
   * internal validation of authorization
   * @throws PentahoAccessControlException
   */
  private void validateAccess() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem.get(IAuthorizationPolicy.class);
    boolean isAdmin = policy.isAllowed(ACTION_READ) && policy.isAllowed(ACTION_CREATE)
        && policy.isAllowed(ACTION_ADMINISTER_SECURITY);
    if (!isAdmin) {
      throw new PentahoAccessControlException(ACCESS_DENIED);
    }
  }
  /**
   * convert string to property to do a lookup "Provider=Mondrian;DataSource=Pentaho"
   * @param parameters
   * @param key
   * @return
   */
  private String getValue(String parameters, String key) {
    mondrian.olap.Util.PropertyList propertyList = mondrian.olap.Util.parseConnectString(parameters);    
    return propertyList.get(key);   
  }
  
}
