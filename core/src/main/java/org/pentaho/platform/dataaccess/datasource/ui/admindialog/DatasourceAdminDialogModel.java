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

package org.pentaho.platform.dataaccess.datasource.ui.admindialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class DatasourceAdminDialogModel extends XulEventSourceAdapter {

  private List<IDatasourceInfo> datasources = new ArrayList<IDatasourceInfo>();
  private List<String> datasourceTypes;
  private IDatasourceInfo selectedDatasource;
  private int selectedIndex = -1;


  public DatasourceAdminDialogModel() {
    datasources = new ArrayList<IDatasourceInfo>();
  }

  public void addDatasource( IDatasourceInfo datasource ) {
    datasources.add( datasource );
    this.firePropertyChange( "datasources", null, datasources ); //$NON-NLS-1$
  }

  public void removeDatasource( IDatasourceInfo datasource ) {
    datasources.remove( datasource );
    this.firePropertyChange( "datasources", null, datasources ); //$NON-NLS-1$
  }


  @Bindable
  public List<IDatasourceInfo> getDatasourcesList() {
    return datasources;
  }

  @Bindable
  public void setDatasourcesList( List<IDatasourceInfo> datasourceList ) {
    List<IDatasourceInfo> previousList = getPreviousDatasourceList();
    this.datasources.clear();
    this.firePropertyChange( "datasources", previousList, datasourceList ); //$NON-NLS-1$
  }

  private List<IDatasourceInfo> getPreviousDatasourceList() {
    List<IDatasourceInfo> previous = new ArrayList<IDatasourceInfo>();
    previous.addAll( this.datasources );
    return previous;
  }

  @Bindable
  public void setSelectedIndex( final int selectedIndex ) {
    this.selectedIndex = selectedIndex;
    // we want this to fire every time. setting prevval to always be different.
    this.firePropertyChange( "selectedIndex", selectedIndex + 1, selectedIndex ); //$NON-NLS-1$
  }

  @Bindable
  public int getSelectedIndex() {
    return selectedIndex;
  }

  @Bindable
  public void setDatasourceTypeList( List<String> datasourceTypes ) {
    this.datasourceTypes = new ArrayList<String>( datasourceTypes );
    this.firePropertyChange( "datasourceTypes", null, datasourceTypes ); //$NON-NLS-1$
  }

  @Bindable
  public List<String> getDatasourceTypeList() {
    return datasourceTypes;
  }

  @Bindable
  public void setSelectedDatasource( IDatasourceInfo selectedDatasource ) {
    this.selectedDatasource = selectedDatasource;
    this.firePropertyChange( "selectedDatasource", null, selectedDatasource ); //$NON-NLS-1$
  }

  @Bindable
  public IDatasourceInfo getSelectedDatasource() {
    return selectedDatasource;
  }
}
