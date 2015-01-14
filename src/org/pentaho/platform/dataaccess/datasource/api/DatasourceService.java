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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.utils.DataAccessPermissionUtil;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.services.importexport.legacy.MondrianCatalogRepositoryHelper;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

public class DatasourceService {
  protected IMetadataDomainRepository metadataDomainRepository;
  protected IMondrianCatalogService mondrianCatalogService;
  protected RepositoryFileAclAdapter repositoryFileAclAdapter;

  public DatasourceService() {
    metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );
    mondrianCatalogService = PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() );
    repositoryFileAclAdapter = new RepositoryFileAclAdapter();
  }

  protected IUnifiedRepository getRepository() {
    return PentahoSystem.get( IUnifiedRepository.class );
  }

  public static boolean canAdminister() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
      && ( policy.isAllowed( AdministerSecurityAction.NAME ) );
  }

  public static void validateAccess() throws PentahoAccessControlException {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    boolean isAdmin =
        policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
        && ( policy.isAllowed( AdministerSecurityAction.NAME ) || policy.isAllowed( PublishAction.NAME ) );
    if ( !isAdmin ) {
      throw new PentahoAccessControlException( "Access Denied" );
    }
  }

  protected boolean canManageACL() {
    IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );
    return policy.isAllowed( RepositoryReadAction.NAME ) && policy.isAllowed( RepositoryCreateAction.NAME )
      && DataAccessPermissionUtil.hasManageAccess();
  }

  /**
   * Fix for "%5C" and "%2F" in datasource name ("/" and "\" are omitted and %5C, %2F are decoded in
   * PentahoPathDecodingFilter.EncodingAwareHttpServletRequestWrapper)
   *
   * @param param pathParam
   * @return correct param
   */
  protected String fixEncodedSlashParam( String param ) {
    return param.replaceAll( "\\\\", "%5C" ).replaceAll( "/", "%2F" );
  }

  protected boolean isMetadataDatasource( String id ) {
    Domain domain;
    try {
      domain = metadataDomainRepository.getDomain( id );
      if ( domain == null ) {
        return false;
      }
    } catch ( Exception e ) { // If we can't load the domain then we MUST return false
      return false;
    }

    List<LogicalModel> logicalModelList = domain.getLogicalModels();
    if ( logicalModelList != null && logicalModelList.size() >= 1 ) {
      for ( LogicalModel logicalModel : logicalModelList ) {
        // keep this check for backwards compatibility for now
        Object property = logicalModel.getProperty( "AGILE_BI_GENERATED_SCHEMA" ); //$NON-NLS-1$
        if ( property != null ) {
          return false;
        }

        // moving forward any non metadata generated datasource should have this property
        property = logicalModel.getProperty( "WIZARD_GENERATED_SCHEMA" ); //$NON-NLS-1$
        if ( property != null ) {
          return false;
        }
      }
      return true;
    } else {
      return true;
    }
  }

  public static void parseMondrianSchemaName( String dswId, Map<String, InputStream> fileData ) {
    final String keySchema = "schema.xml"; //$NON-NLS-1$
    if ( fileData.containsKey( keySchema ) ) {
      final int xmiIndex = dswId.lastIndexOf( ".xmi" ); //$NON-NLS-1$
      fileData.put( ( xmiIndex > 0 ? dswId.substring( 0, xmiIndex ) : dswId ) + ".mondrian.xml",
          fileData.get( keySchema ) ); //$NON-NLS-1$
      fileData.remove( keySchema );
    }
  }

  protected void flushDataSources() {
    metadataDomainRepository.flushDomains();
    mondrianCatalogService.reInit( PentahoSessionHolder.getSession() );
  }
}
