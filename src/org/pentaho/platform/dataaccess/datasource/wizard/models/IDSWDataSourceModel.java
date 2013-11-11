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

package org.pentaho.platform.dataaccess.datasource.wizard.models;


public interface IDSWDataSourceModel {
  /**
   * Get the name of the data source model
   * 
   * @param modelName
   * @return
   */
  public String getModelName();
  
  /**
   * Get the display name of the model.
   * @return
   */
  public String getDisplayName();
  
  /**
   * Returns the class concrete class that will handle the functions specific to this
   * model.
   * @return
   */
  public Class<IDSWModelImplementer> getImplementingClass();
  
  /**
   * Gets the Implementing object for this model.  The object will be of the same class as the
   * <code>getImplementingClass</code>
   * @return
   */
  public IDSWModelImplementer getImplementer();
  
  /**
   * Deserializes the IDSWDataSourceCoreService object.
   * @param IDSWDataSourceCoreService
   * @return
   */
  public IDSWDataSourceModel deserialize(String IDSWDataSourceCoreService) throws Exception;
  
  /**
   * Create a serialized version of this object suitable for storage.
   * @param iDSWDataSourceCoreService
   * @return
   */
  public String serialize(IDSWDataSourceModel iDSWDataSourceCoreService) throws Exception;
  
  /**
   * Check if the object is suitable for storage and/or object creation doing any checks for required data or sanity
   * @return true if valid
   */
  public boolean isValid();
  
}
