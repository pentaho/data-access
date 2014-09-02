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

import org.pentaho.platform.api.engine.IPentahoSession;

/**
 * Implement this interface to override the permissions behavior of data access.
 * <p/>
 * This interface may be implemented and then the implementation class specified in the data-access settings.xml file,
 * within the settings/dataaccess-permission-handler
 *
 * @author Will Gorman (wgorman@pentaho.com)
 */
public interface IDataAccessPermissionHandler {

  /**
   * This method returns true if the session has permission to execute arbitrary sql from the client.
   *
   * @param session pentaho session
   * @return true if allowed
   */
  boolean hasDataAccessPermission( IPentahoSession session );
}
