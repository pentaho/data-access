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

import java.util.List;

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Implement this interface to override the view permissions behavior of data access.
 * <p/>
 * This interface may be implemented and then the implementation class specified in the data-access settings.xml file,
 * within the settings/dataaccess-permission-handler. The specific setting for this class is data-access-view-roles and
 * data-access-view-users
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

  List<String> getPermittedRoleList( IPentahoSession session );

  /**
   * This method returns list of permitted user who are allowed to view and use datasource
   *
   * @param session pentaho session
   * @return List of permitted users
   */

  List<String> getPermittedUserList( IPentahoSession session );

  /**
   * This method returns the default acls for permitted role and user
   *
   * @param session pentaho session
   * @return int default acls
   */

  int getDefaultAcls( IPentahoSession session );

  /**
   * This method returns true if user represented by session has view permission
   *
   * @param session pentaho session
   * @return true if user represented by session has view permission
   */
  boolean hasDataAccessViewPermission( IPentahoSession session );

}
