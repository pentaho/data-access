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
 * Copyright (c) 2002-2020 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.engine.PentahoAccessControlException;
import org.pentaho.platform.api.repository2.unified.IUnifiedRepository;
import org.pentaho.platform.dataaccess.datasource.utils.DataAccessPermissionUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceImpl;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.repository2.unified.webservices.RepositoryFileAclAdapter;
import org.pentaho.platform.security.policy.rolebased.actions.AdministerSecurityAction;
import org.pentaho.platform.security.policy.rolebased.actions.PublishAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryCreateAction;
import org.pentaho.platform.security.policy.rolebased.actions.RepositoryReadAction;

public class DatasourceService {
  protected IMetadataDomainRepository metadataDomainRepository;
  protected IMondrianCatalogService mondrianCatalogService;
  protected RepositoryFileAclAdapter repositoryFileAclAdapter;
  protected IPluginResourceLoader pluginResourceLoader;
  private static final Log LOGGER = LogFactory.getLog( DatasourceService.class );

  public DatasourceService() {
    this( PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() ),
        PentahoSystem.get( IMondrianCatalogService.class, PentahoSessionHolder.getSession() ),
        new RepositoryFileAclAdapter(),
        PentahoSystem.get( IPluginResourceLoader.class, PentahoSessionHolder.getSession() ) );
  }

  public DatasourceService( IMetadataDomainRepository metadataDomainRepository,
      IMondrianCatalogService mondrianCatalogService, RepositoryFileAclAdapter repositoryFileAclAdapter,
      IPluginResourceLoader pluginResourceLoader ) {
    this.metadataDomainRepository = metadataDomainRepository;
    this.mondrianCatalogService = mondrianCatalogService;
    this.repositoryFileAclAdapter = repositoryFileAclAdapter;
    this.pluginResourceLoader = pluginResourceLoader;
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
        && policy.isAllowed( PublishAction.NAME );
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

  public boolean isMetadataDatasource( String id ) {
    Domain domain;
    try {
      domain = metadataDomainRepository.getDomain( id );
      if ( domain == null ) {
        return false;
      }
    } catch ( Exception e ) { // If we can't load the domain then we MUST return false
      return false;
    }

    return isMetadataDatasource( domain );
  }

  public static boolean isMetadataDatasource( Domain domain ) {

    if ( domain == null ) {
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

  public void ensureDataAccessPermission() throws ConnectionServiceException {
    ConnectionServiceImpl connectionService = new ConnectionServiceImpl();
    connectionService.ensureDataAccessPermission();
  }

  public static boolean isDSWDatasource( Domain domain ) {
    if ( domain == null ) {
      return false; //If we can't find it, then it can't be a DSW
    }
    return !isMetadataDatasource( domain );
  }

  public boolean isDSWDatasource( String domainId ) {
    return !isMetadataDatasource( domainId );
  }

  protected int getDatasourceLoadThreadCount() throws IllegalArgumentException {
    int threadCount = Runtime.getRuntime().availableProcessors();
    String threadCountAsString =
        pluginResourceLoader.getPluginSetting( getClass(), "settings/data-access-datasource-load-threads" );
    if ( StringUtils.isNotBlank( threadCountAsString ) ) {
      threadCount = Integer.parseInt( threadCountAsString );
      if ( threadCount <= 0 ) {
        throw new NumberFormatException( "Data access datasource load threads are negative or zero" );
      }
    }
    LOGGER.debug( "Data access datasource load threads: " + threadCount );
    return threadCount;
  }

  protected List<String> getDatasourceIds( Predicate<String> isDatasourceType ) {
    List<String> datasourceList = new ArrayList<>();
    Set<String> domainIds = metadataDomainRepository.getDomainIds();
    Set<Callable<String>> callables = new HashSet<>();
    int threadCount;
    try {
      threadCount = getDatasourceLoadThreadCount();
    } catch ( IllegalArgumentException e ) {
      LOGGER.error( e.getMessage(), e );
      return datasourceList;
    }
    ExecutorService executor = Executors.newFixedThreadPool( threadCount );
    for ( String domainId : domainIds ) {
      callables.add( () -> isDatasourceType.test( domainId ) ? domainId : null );
    }
    try {
      List<Future<String>> futures = executor.invokeAll( callables );
      for ( Future<String> future : futures ) {
        if ( future.get() != null ) {
          datasourceList.add( future.get() );
        }
      }
    } catch ( InterruptedException ie ) {
      LOGGER.error( ie.getMessage(), ie );
      Thread.currentThread().interrupt();
    } catch ( ExecutionException ee ) {
      LOGGER.error( ee.getMessage(), ee );
    }
    executor.shutdown();
    return datasourceList;
  }
}
