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
 */
package org.pentaho.platform.dataaccess.datasource;


import org.pentaho.platform.api.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DatasourceInfo extends XulEventSourceAdapter implements IDatasourceInfo {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  String name;
  
  String id;

  String type;
  
  boolean editable;
  
  boolean removable;
  
  boolean importable;
  
  boolean exportable;
  

  public DatasourceInfo() {
    super();
    // TODO Auto-generated constructor stub
  }
  
  public DatasourceInfo(String name, String id, String type) {
    super();
    this.name = name;
    this.id = id;
    this.type = type;
  }

  public DatasourceInfo(String name, String id, String type, boolean editable, boolean removable, boolean importable, boolean exportable) {
    super();
    this.name = name;
    this.id = id;
    this.type = type;
    this.editable = editable;
    this.removable = removable;
    this.importable = importable;
    this.exportable = exportable;
  }
  
  @Bindable
  @Override
  public String getName() {
    return name;
  }

  @Bindable
  @Override
  public String getId() {
    return id;
  }

  
  @Bindable
  @Override
  public String getType() {
    return type;
  }

  @SuppressWarnings("nls")
  @Override
  public String toString() {
    return "DatasourceInfo [id=" + id + ", type=" + type + "]";
  }

  @Override
  public boolean isEditable() {
    return this.editable;
  }

  @Override
  public boolean isRemovable() {
    return this.removable ;
  }

  @Override
  public boolean isImportable() {
    return this.importable;
  }

  @Override
  public boolean isExportable() {
    return this.exportable;
  }
}
