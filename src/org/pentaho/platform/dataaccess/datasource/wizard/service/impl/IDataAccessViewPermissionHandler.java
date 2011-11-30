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

import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Implement this interface to override the view permissions behavior of
 * data access.
 * 
 * This interface may be implemented and then the implementation class specified 
 * in the data-access settings.xml file, within the
 * settings/dataaccess-permission-handler. The specific setting for this class
 * is data-access-view-roles and data-access-view-users
 * 
 * @author Ramaiz Mansoor (rmansoor@pentaho.com)
 */
public interface IDataAccessViewPermissionHandler {
  /**
   * This method returns list of permitted roles who are allowed to view and use datasource
   * 
   * @param session pentaho session
   * @return List of permitted roles
   */

  List<String> getPermittedRoleList(IPentahoSession session);
  /**
   * This method returns list of permitted user who are allowed to view and use datasource
   * 
   * @param session pentaho session
   * @return List of permitted users
   */
  
  List<String> getPermittedUserList(IPentahoSession session);
  /**
   * This method returns the default acls for permitted role and user
   * 
   * @param session pentaho session
   * @return int default acls
   */
  
  int getDefaultAcls(IPentahoSession session);
  
  /**
   * This method returns true if user represented by session has view permission
   * 
   * @param session pentaho session
   * @return true if user represented by session has view permission
   */
  boolean hasDataAccessViewPermission(IPentahoSession session);
  
}
