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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.AbstractModelList;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


public class ColumnInfoCollection extends AbstractModelList<ColumnInfo> {

  private int selectedCount = 0;

  private PropertyChangeListener selectedListener = new PropertyChangeListener() {
    public void propertyChange( PropertyChangeEvent evt ) {
      if ( evt.getPropertyName().equals( "include" ) || evt.getPropertyName().equals( "children" ) ) {
        int count = 0;
        for ( ColumnInfo ci : getChildren() ) {
          if ( ci.isInclude() ) {
            count++;
          }
        }
        setSelectedCount( count );
      }
    }
  };

  private void setSelectedCount( int count ) {
    int prev = selectedCount;
    selectedCount = count;
    firePropertyChange( "selectedCount", prev, count );
  }

  @Bindable
  public int getSelectedCount() {
    return selectedCount;
  }

  public ColumnInfoCollection() {

  }

  public void onAdd( ColumnInfo child ) {
    child.addPropertyChangeListener( selectedListener );
  }

  public void onRemove( ColumnInfo child ) {
    child.removePropertyChangeListener( selectedListener );
  }
}
