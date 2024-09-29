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
