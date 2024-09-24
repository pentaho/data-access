/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IAuthorizationPolicy;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.core.Authentication;

public class SimpleDataAccessViewPermissionHandler implements IDataAccessViewPermissionHandler {

  private Log logger = LogFactory.getLog( SimpleDataAccessViewPermissionHandler.class );

  private IAuthorizationPolicy policy = PentahoSystem.get( IAuthorizationPolicy.class );

  public List<String> getPermittedRoleList( IPentahoSession session ) {
    List<String> roleList = new ArrayList<String>();
    Authentication auth = SecurityHelper.getInstance().getAuthentication( session, true );
    IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    String roles = null;

    try {
      roles = resLoader.getPluginSetting( getClass(), "settings/data-access-view-roles" ); //$NON-NLS-1$
    } catch ( Exception e ) {
      logger.debug( "Error getting plugin setting", e );
    }

    if ( roles != null && roles.length() > 0 ) {
      String[] roleArr = roles.split( "," ); //$NON-NLS-1$

      for ( String role : roleArr ) {
        if ( role != null && role.trim().length() > 0 ) {
          roleList.add( role );
        }
      }
    }
    return roleList;
  }

  @Override
  public List<String> getPermittedUserList( IPentahoSession session ) {
    List<String> userList = new ArrayList<String>();
    IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    String users = null;

    try {
      users = resLoader.getPluginSetting( getClass(), "settings/data-access-view-users" ); //$NON-NLS-1$
    } catch ( Exception e ) {
      logger.debug( "Error getting plugin setting", e );
    }

    if ( users != null && users.length() > 0 ) {
      String[] userArr = users.split( "," ); //$NON-NLS-1$
      for ( String user : userArr ) {
        if ( user != null && user.trim().length() > 0 ) {
          userList.add( user );
        }
      }
    }

    return userList;
  }

  public int getDefaultAcls( IPentahoSession session ) {
    IPluginResourceLoader resLoader = PentahoSystem.get( IPluginResourceLoader.class, null );
    String defaultAclsAsString = null;
    int defaultAcls = -1;
    try {
      defaultAclsAsString =
        resLoader.getPluginSetting( getClass(), "settings/data-access-default-view-acls" ); //$NON-NLS-1$
    } catch ( Exception e ) {
      logger.debug( "Error getting plugin setting", e );
    }
    if ( defaultAclsAsString != null && defaultAclsAsString.length() > 0 ) {
      defaultAcls = Integer.parseInt( defaultAclsAsString );
    }
    return defaultAcls;
  }

  public boolean hasDataAccessViewPermission( IPentahoSession session ) {
    return getPermittedUserList( session ).size() > 0 || getPermittedRoleList( session ).size() > 0;
  }
}
