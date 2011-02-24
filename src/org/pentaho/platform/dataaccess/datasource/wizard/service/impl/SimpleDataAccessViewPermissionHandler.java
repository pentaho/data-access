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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created June 16, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

public class SimpleDataAccessViewPermissionHandler implements IDataAccessViewPermissionHandler {

  public List<String> getPermittedRoleList(IPentahoSession session) {
    List<String> roleList = new ArrayList<String>();
    Authentication auth = SecurityHelper.getAuthentication(session, true);
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    String roles = null;

    try {
      roles = resLoader.getPluginSetting(getClass(), "settings/data-access-view-roles"); //$NON-NLS-1$
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (roles != null) {
      String roleArr[] = roles.split(","); //$NON-NLS-1$

      for (String role : roleArr) {
        for (GrantedAuthority userRole : auth.getAuthorities()) {
          if (role != null && role.trim().equals(userRole.getAuthority())) {
            roleList.add(role);
          }
        }
      }
    }
    return roleList;
  }

  public List<String> getPermittedUserList(IPentahoSession session) {
    List<String> userList = new ArrayList<String>();
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    String users = null;

    try {
      users = resLoader.getPluginSetting(getClass(), "settings/data-access-view-users"); //$NON-NLS-1$
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (users != null) {
      String userArr[] = users.split(","); //$NON-NLS-1$
      for (String user : userArr) {
        if (user != null && user.trim().length() > 0) {
          userList.add(user);
        }
      }
    }

    return userList;
  }

  public int getDefaultAcls(IPentahoSession session) {
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    String defaultAclsAsString = null;
    int defaultAcls = -1;
    try {
      defaultAclsAsString = resLoader.getPluginSetting(getClass(), "settings/data-access-default-view-acls"); //$NON-NLS-1$
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (defaultAclsAsString != null && defaultAclsAsString.length() > 0) {
      defaultAcls = Integer.parseInt(defaultAclsAsString);
    }
    return defaultAcls;
  }

  public boolean hasDataAccessViewPermission(IPentahoSession session) {
    return getPermittedUserList(session).size() > 0 || getPermittedRoleList(session).size() > 0;
      
  }
}
