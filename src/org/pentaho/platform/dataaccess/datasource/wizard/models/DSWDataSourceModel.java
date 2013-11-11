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


public class DSWDataSourceModel implements IDSWDataSourceModel {
  
  String modelName;
  String displayName;
  String implementerClassName;
  IDSWModelImplementer modelImplementer;
  
  public DSWDataSourceModel(String modelName, String displayName) { //, String implementerClassName, IDSWModelImplementer modelImplementer) {
    this.modelName = modelName;
    this.displayName = displayName;
    //this.implementerClassName = implementerClassName;
    //this.modelImplementer = modelImplementer;
  }

  @Override
  public String getModelName() {
    return modelName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<IDSWModelImplementer> getImplementingClass() {
    try {
      return (Class<IDSWModelImplementer>) Class.forName(implementerClassName);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public IDSWModelImplementer getImplementer() {
    return modelImplementer;
  }

  @Override
  public IDSWDataSourceModel deserialize(String IDSWDataSourceCoreService) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String serialize(IDSWDataSourceModel iDSWDataSourceCoreService) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isValid() {
    return hasValue(displayName) && hasValue(modelName);
  }

  private boolean hasValue(String value) {
    return value != null && value.length() > 0;
  }

}
