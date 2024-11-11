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


package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IUIDatasourceAdminService {

  /**
   * Returns the type of the datasource
   *
   * @return type of the datasource
   */
  public String getType();

  /**
   * Return the datasource ids of the selected datasource type
   *
   * @param callback
   */
  public void getIds( XulServiceCallback<List<IDatasourceInfo>> callback );

  /**
   * Returns the information required to launch a editor to create new datasource of this type
   *
   * @return
   */
  public String getNewUI();

  /**
   * Returns the information required to launch a editor to edit datasource of this type
   *
   * @param dsInfo
   * @return
   */
  public String getEditUI( IDatasourceInfo dsInfo );

  /**
   * Export the selected datasource
   *
   * @param dsInfo
   */
  public void export( IDatasourceInfo dsInfo );

  public void remove( IDatasourceInfo dsInfo, Object callback );

  public boolean isEditable();

  public boolean isRemovable();

  public boolean isImportable();

  public boolean isExportable();

  public boolean isCreatable();

}
