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
