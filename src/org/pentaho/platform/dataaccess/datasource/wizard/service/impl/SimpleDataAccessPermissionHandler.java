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
 * Created May 19, 2009
 * @author wgorman
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.api.mt.ITenantedPrincipleNameResolver;
import org.pentaho.platform.core.mt.Tenant;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;

public class SimpleDataAccessPermissionHandler implements IDataAccessPermissionHandler {

  private Log logger = LogFactory.getLog(SimpleDataAccessPermissionHandler.class);
  @Override
  public boolean hasDataAccessPermission(IPentahoSession session) {

    Authentication auth = SecurityHelper.getInstance().getAuthentication(session, true);
    
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    ITenantedPrincipleNameResolver tenantedUserNameUtils = PentahoSystem.get(ITenantedPrincipleNameResolver.class, "tenantedUserNameUtils", session); 
    ITenantedPrincipleNameResolver tenantedRoleNameUtils = PentahoSystem.get(ITenantedPrincipleNameResolver.class, "tenantedRoleNameUtils", session);
    String tenantId = (String) session.getAttribute(IPentahoSession.TENANT_ID_KEY);
    String roles = null;
    String users = null;
    
    // TODO: delete this try catch once data access becomes a fully fledged plugin
    
    try {
      roles = resLoader.getPluginSetting(getClass(), "settings/data-access-roles" ); //$NON-NLS-1$
      users = resLoader.getPluginSetting(getClass(), "settings/data-access-users" ); //$NON-NLS-1$
    } catch (Exception e) {
      logger.debug("Error getting data access plugin settings", e);
    }
    
    if(roles != null && roles.length() > 0) {
      String roleArr[] = roles.split(","); //$NON-NLS-1$
      
      for (String role : roleArr) {
        String tenantedRole = tenantedRoleNameUtils.getPrincipleId(new Tenant(tenantId, true), role);
        for (GrantedAuthority userRole : auth.getAuthorities()) {
          if (tenantedRole != null && tenantedRole.trim().equals(userRole.getAuthority())) {
            return true;
          }
        }
      }
    }
    if (users != null && users.length() > 0) {
      String userArr[] = users.split(","); //$NON-NLS-1$
      for (String user : userArr) {
        String tenantedUser = tenantedUserNameUtils.getPrincipleId(new Tenant(tenantId, true), user);
        if (tenantedUser != null && tenantedUser.trim().equals(auth.getName())) {
          return true;
        }
      }
    }
    
    return false;
  }

}
