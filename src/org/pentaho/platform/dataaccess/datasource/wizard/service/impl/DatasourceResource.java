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
 * @author Ramaiz Mansoor
 * 
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;


import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.MediaType.WILDCARD;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.plugin.services.metadata.IPentahoMetadataDomainRepositoryExporter;
import org.pentaho.platform.repository2.unified.fileio.RepositoryFileInputStream;
import org.pentaho.platform.web.http.api.resources.JaxbList;


@Path("/data-access/api/datasource")
public class DatasourceResource {

  private static final String MONDRIAN_CATALOG_REF = "MondrianCatalogRef"; //$NON-NLS-1$
  public static final String APPLICATION_ZIP = "application/zip"; //$NON-NLS-1$
  
  protected IMetadataDomainRepository metadataDomainRepository;
  protected IMondrianCatalogService mondrianCatalogService;
  IDSWDatasourceService dswService;
  IModelerService modelerService;
  public static final String METADATA_EXT = ".xmi"; //$NON-NLS-1$
  
  public DatasourceResource() {
    super();
    metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, PentahoSessionHolder.getSession());
    mondrianCatalogService = PentahoSystem.get(IMondrianCatalogService.class, PentahoSessionHolder.getSession());
    dswService = new DSWDatasourceServiceImpl();
    modelerService = new ModelerService();
    
  }

  @GET
  @Path("/analysis/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getAnalysisDatasourceIds() {
    List<String> analysisIds = new ArrayList<String>();
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), false)) {
      String domainId = mondrianCatalog.getName() + METADATA_EXT;
      Set<String> ids = metadataDomainRepository.getDomainIds();
      if(ids.contains(domainId) == false){
        analysisIds.add(mondrianCatalog.getName());
      }
    }
    return new JaxbList<String>(analysisIds);
  }

  @GET
  @Path("/metadata/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getMetadataDatasourceIds() {
    List<String> metadataIds = new ArrayList<String>();
    try {
		Thread.sleep(100);
		for(String id:metadataDomainRepository.getDomainIds()) {
		    if(isMetadataDatasource(id)) {
		      metadataIds.add(id);
		    }
		}
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
    return new JaxbList<String>(metadataIds);
  }
  
  private boolean isMetadataDatasource(String id) {
    Domain domain;
    try { 
      domain = metadataDomainRepository.getDomain(id);
      if(domain == null) return false;
    } catch (Exception e) { // If we can't load the domain then we MUST return false
      return false;
    }
    
    List<LogicalModel> logicalModelList = domain.getLogicalModels();
    if(logicalModelList != null && logicalModelList.size() >= 1) {
      for(LogicalModel logicalModel : logicalModelList) {	
    	  Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
    	  if(property != null) {
    		  return false;    
    	  } 
      }
      return true;
    } else {
      return true;
    }
  }
  
  @GET
  @Path("/dsw/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getDSWDatasourceIds() {
    List<String> datasourceList = new ArrayList<String>();
    try {
      nextModel: for(LogicalModelSummary summary:dswService.getLogicalModels(null)) {
        Domain domain = modelerService.loadDomain(summary.getDomainId());
        List<LogicalModel> logicalModelList = domain.getLogicalModels();
        if(logicalModelList != null && logicalModelList.size() >= 1) {
          for(LogicalModel logicalModel : logicalModelList) {	
        	  Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
        	  if(property != null) {
        		  datasourceList.add(summary.getDomainId());
        		  continue nextModel;
        	  }
          }
        }
      }
    } catch (Throwable e) {
      return null;
    }
    return new JaxbList<String>(datasourceList);
  }
  
  @GET
  @Path("/metadata/{metadataId : .+}/download")
  @Produces(WILDCARD)
  public Response doGetMetadataFilesAsDownload(@PathParam("metadataId") String metadataId) {
    if (! (metadataDomainRepository instanceof IPentahoMetadataDomainRepositoryExporter)) {
      return Response.serverError().build();
    }
    Map<String, InputStream> fileData = ((IPentahoMetadataDomainRepositoryExporter)metadataDomainRepository).getDomainFilesData(metadataId);
    return createAttachment(fileData, metadataId);
  }

  @GET
  @Path("/analysis/{analysisId : .+}/download")
  @Produces(WILDCARD)
  public Response doGetAnalysisFilesAsDownload(@PathParam("analysisId") String analysisId) {
    MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper(PentahoSystem.get(IUnifiedRepository.class));
    Map<String, InputStream> fileData = helper.getModrianSchemaFiles(analysisId);
    
    return createAttachment(fileData, analysisId);
  }
  
  @GET
  @Path("/dsw/{dswId : .+}/download")
  @Produces(WILDCARD)
  public Response doGetDSWFilesAsDownload(@PathParam("dswId") String dswId) {
    // First get the metadata files;
    Map<String, InputStream> fileData = ((IPentahoMetadataDomainRepositoryExporter)metadataDomainRepository).getDomainFilesData(dswId); 
  
    // Then get the corresponding mondrian files
    Domain domain = metadataDomainRepository.getDomain(dswId);
    ModelerWorkspace model = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
    model.setDomain(domain);
    LogicalModel logicalModel = model.getLogicalModel(ModelerPerspective.ANALYSIS);
    if (logicalModel.getProperty(MONDRIAN_CATALOG_REF) != null) {
      MondrianCatalogRepositoryHelper helper = new MondrianCatalogRepositoryHelper(PentahoSystem.get(IUnifiedRepository.class));
      String catalogRef = (String)logicalModel.getProperty(MONDRIAN_CATALOG_REF);
      fileData.putAll(helper.getModrianSchemaFiles(catalogRef));
    }

    return createAttachment(fileData, dswId);
  }
  
  @POST
  @Path("/metadata/{metadataId : .+}/remove")
  @Produces(WILDCARD)
  public Response doRemoveMetadata(@PathParam("metadataId") String metadataId) {
    metadataDomainRepository.removeDomain(metadataId);
    return Response.ok().build();
  }
  
  @POST
  @Path("/analysis/{analysisId : .+}/remove")
  @Produces(WILDCARD)
  public Response doRemoveAnalysis(@PathParam("analysisId") String analysisId) {
    mondrianCatalogService.removeCatalog(analysisId, PentahoSessionHolder.getSession());
    return Response.ok().build();
  }
  
  @POST
  @Path("/dsw/{dswId : .+}/remove")
  @Produces(WILDCARD)
  public Response doRemoveDSW(@PathParam("dswId") String dswId) {
    Domain domain = metadataDomainRepository.getDomain(dswId);
    ModelerWorkspace model = new ModelerWorkspace(new GwtModelerWorkspaceHelper());
    model.setDomain(domain);
    LogicalModel logicalModel = model.getLogicalModel(ModelerPerspective.ANALYSIS);
    if (logicalModel.getProperty(MONDRIAN_CATALOG_REF) != null) {
      String catalogRef = (String)logicalModel.getProperty(MONDRIAN_CATALOG_REF);
      mondrianCatalogService.removeCatalog(catalogRef, PentahoSessionHolder.getSession());
    }
    metadataDomainRepository.removeDomain(dswId);

    return Response.ok().build();
  }
  
  @GET
  @Path("/{dswId : .+}/getAnalysisDatasourceInfo")
  @Produces(WILDCARD)
  public Response getAnalysisDatasourceInfo(@PathParam("dswId") String dswId) {
	MondrianCatalog catalog = mondrianCatalogService.getCatalog(dswId, PentahoSessionHolder.getSession());
	String parameters = catalog.getDataSourceInfo();
	return Response.ok().entity(parameters).build();
  }  

  private Response createAttachment(Map<String, InputStream> fileData, String domainId) {
    String quotedFileName = null;
    final InputStream is;
    if (fileData.size() > 1) { // we've got more than one file so we want to zip them up and send them
      File zipFile = null;
      try {
        zipFile = File.createTempFile("datasourceExport", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
        zipFile.deleteOnExit();
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
        for (String fileName : fileData.keySet()) {
          InputStream zipEntryIs = null;
          try {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zipEntryIs = fileData.get(fileName);
            IOUtils.copy(zipEntryIs, zos);
          } catch (Exception e) {
            continue;
          } finally {
            zos.closeEntry();
            if (zipEntryIs != null) {
              zipEntryIs.close();
            }
          }
        }
        zos.close();
        is = new FileInputStream(zipFile);
      } catch (IOException ioe) {
        return Response.serverError().entity(ioe.toString()).build();
      }
      StreamingOutput streamingOutput = new StreamingOutput() {
        public void write(OutputStream output) throws IOException {
          IOUtils.copy(is, output);
        }
      };
      quotedFileName = "\"" + domainId + ".zip\""; //$NON-NLS-1$//$NON-NLS-2$
      return Response.ok(streamingOutput, APPLICATION_ZIP).header("Content-Disposition", "attachment; filename=" + quotedFileName).build(); //$NON-NLS-1$ //$NON-NLS-2$
    } else if (fileData.size() == 1) {  // we've got a single metadata file so we just return that.
      String fileName = (String) fileData.keySet().toArray()[0];
      quotedFileName = "\"" + domainId + "\""; //$NON-NLS-1$ //$NON-NLS-2$
      is = fileData.get(fileName);
      String mimeType = MediaType.TEXT_PLAIN;
      if (is instanceof RepositoryFileInputStream) {
        mimeType = ((RepositoryFileInputStream)is).getMimeType();
      }
      StreamingOutput streamingOutput = new StreamingOutput() {
        public void write(OutputStream output) throws IOException {
          IOUtils.copy(is, output);
        }
      };
      return Response.ok(streamingOutput, mimeType).header("Content-Disposition", "attachment; filename=" + quotedFileName).build(); //$NON-NLS-1$ //$NON-NLS-2$
    }
    return Response.serverError().build();
  }
}
