/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.dataaccess.api.datasource.wizard;

import java.util.Locale;


public interface IDSWTemplate {
  /**
   * Get the ID of the data source model
   * 
   * @param modelName
   * @return
   */
  String getID();

  /**
   * Get the display name of the model.
   * 
   * @return
   */
  String getDisplayName( Locale locale );

  /**
   * Creates the underlaying domain for the template
   * 
   * @param iDSWDataSource
   */
  void createDatasource( IDSWDataSource iDSWDataSource, boolean overwrite ) throws DSWException;

  /**
   * Serialize the IDSWTemplateModel into a string
   * 
   * @param dswTemplateModel
   * @return
   */
  String serialize( IDSWTemplateModel dswTemplateModel ) throws DSWException;

  /**
   * Deserializes the IDSWTemplateModel object.
   * 
   * @param IDSWTemplateModel
   * @return
   */
  IDSWTemplateModel deserialize( String serializedModel ) throws DSWException;
}
