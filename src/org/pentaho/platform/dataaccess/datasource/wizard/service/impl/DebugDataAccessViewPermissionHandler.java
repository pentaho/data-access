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

public class DebugDataAccessViewPermissionHandler implements IDataAccessViewPermissionHandler {

  public List<String> getPermittedRoleList(IPentahoSession session) {
    List<String> roleList = new ArrayList<String>();
    roleList.add("Admin");
    return roleList;
  }

  public List<String> getPermittedUserList(IPentahoSession session) {
    List<String> userList = new ArrayList<String>();
    userList.add("joe");
    return userList;
  }

  public int getDefaultAcls(IPentahoSession session) {
    return 31;
  }

  public boolean hasDataAccessViewPermission(IPentahoSession session) {
    return true;
  }
}
