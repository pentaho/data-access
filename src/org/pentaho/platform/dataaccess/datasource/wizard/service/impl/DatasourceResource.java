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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.web.http.api.resources.JaxbList;


@Path("/data-access/api/datasource")
public class DatasourceResource {

  protected IMetadataDomainRepository metadataDomainRepository;
  protected IMondrianCatalogService mondrianCatalogService;
  IDSWDatasourceService dswService;
  IModelerService modelerService;
  public static final String METADATA_EXT = ".xmi";
  
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
    for(MondrianCatalog mondrianCatalog: mondrianCatalogService.listCatalogs(PentahoSessionHolder.getSession(), true)) {
      String domainId = mondrianCatalog.getName() + METADATA_EXT;
      Domain domain = metadataDomainRepository.getDomain(domainId);
      if(domain == null) {
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
    Domain domain = metadataDomainRepository.getDomain(id);
    List<LogicalModel> logicalModelList = domain.getLogicalModels();
    if(logicalModelList != null && logicalModelList.size() >= 1) {
      LogicalModel logicalModel = logicalModelList.get(0);
      Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
      if(property == null) {
        return true;    
      } 
    } else {
      return true;
    }
    return false;
  }
  
  @GET
  @Path("/dsw/ids")
  @Produces( { APPLICATION_XML, APPLICATION_JSON })
  public JaxbList<String> getDSWDatasourceIds() {
    List<String> datasourceList = new ArrayList<String>();
    try {
      for(LogicalModelSummary summary:dswService.getLogicalModels(null)) {
        Domain domain = modelerService.loadDomain(summary.getDomainId());
        List<LogicalModel> logicalModelList = domain.getLogicalModels();
        if(logicalModelList != null && logicalModelList.size() >= 1) {
          LogicalModel logicalModel = logicalModelList.get(0);
          Object property = logicalModel.getProperty("AGILE_BI_GENERATED_SCHEMA"); //$NON-NLS-1$
          if(property != null) {
            datasourceList.add(summary.getDomainId());
          }
        }
      }
    } catch (Throwable e) {
      return null;
    }
    return new JaxbList<String>(datasourceList);
  }
}
