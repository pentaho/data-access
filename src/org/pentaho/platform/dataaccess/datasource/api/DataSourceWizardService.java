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

package org.pentaho.platform.dataaccess.datasource.api;

import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceServiceImpl;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;

public class DataSourceWizardService extends DatasourceService {

  private IMetadataDomainRepository metadataDomainRepository;
  private IMondrianCatalogService mondrianCatalogService;
  private IDSWDatasourceService dswService;

  private static final String MONDRIAN_CATALOG_REF = "MondrianCatalogRef"; //$NON-NLS-1$

  public DataSourceWizardService() {
    metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );
    mondrianCatalogService = PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() );
    dswService = new DSWDatasourceServiceImpl();
  }

  public void removeDSW( String dswId ) {

    dswId = fixEncodedSlashParam( dswId );
    Domain domain = metadataDomainRepository.getDomain( dswId );
    ModelerWorkspace model = new ModelerWorkspace( new GwtModelerWorkspaceHelper() );
    model.setDomain( domain );
    LogicalModel logicalModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
    if ( logicalModel == null ) {
      logicalModel = model.getLogicalModel( ModelerPerspective.REPORTING );
    }
    if ( logicalModel.getProperty( MONDRIAN_CATALOG_REF ) != null ) {
      String catalogRef = (String) logicalModel.getProperty( MONDRIAN_CATALOG_REF );
      mondrianCatalogService.removeCatalog( catalogRef, PentahoSessionHolder.getSession() );
    }
    try {
      dswService.deleteLogicalModel( domain.getId(), logicalModel.getId() );
    } catch ( DatasourceServiceException ex ) {
    }
    metadataDomainRepository.removeDomain( dswId );
  }
}
