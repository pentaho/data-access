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
