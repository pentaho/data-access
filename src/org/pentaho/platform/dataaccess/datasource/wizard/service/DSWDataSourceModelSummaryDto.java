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

package org.pentaho.platform.dataaccess.datasource.wizard.service;

public class DSWDataSourceModelSummaryDto {
  String modelName;
  String displayName;
  
  public DSWDataSourceModelSummaryDto() {
    
  }
  
  public DSWDataSourceModelSummaryDto(String modelName, String displayName) {
    this.modelName = modelName;
    this.displayName = displayName;
  }

  /**
   * @return the modelName
   */
  public String getModelName() {
    return modelName;
  }

  /**
   * @param modelName the modelName to set
   */
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName the displayName to set
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
