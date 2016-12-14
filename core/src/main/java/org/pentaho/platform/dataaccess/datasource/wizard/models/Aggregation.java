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

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class Aggregation extends XulEventSourceAdapter {

  public Aggregation( List<AggregationType> aggregationList, AggregationType defaultAggregationType ) {
    super();
    setAggregationList( aggregationList );
    setDefaultAggregationType( defaultAggregationType );
  }

  public Aggregation() {
    super();
    // TODO Auto-generated constructor stub
  }

  public static final int MAX_COL_SIZE = 15;
  private AggregationType defaultAggregationType = AggregationType.NONE;
  private List<AggregationType> aggregationList = new ArrayList<AggregationType>() {
    @Override
    public String toString() {
      StringBuffer buffer = new StringBuffer();
      for ( int i = 0; i < this.size(); i++ ) {
        buffer.append( this.get( i ) );
        if ( i < this.size() - 1 && ( buffer.length()
          + this.get( i + 1 ).name().length() < MAX_COL_SIZE ) ) {
          buffer.append( ',' );
        } else {
          break;
        }
      }
      return buffer.toString();
    }


  };


  /**
   * @param aggregationList the aggregationList to set
   */
  @Bindable
  public void setAggregationList( List<AggregationType> aggregationList ) {
    this.aggregationList.clear();
    if ( aggregationList != null && aggregationList.size() > 0 ) {
      this.aggregationList.addAll( aggregationList );
    } else {
      this.aggregationList.add( AggregationType.NONE );
    }
    firePropertyChange( "aggregationList", null, aggregationList ); //$NON-NLS-1$
  }

  /**
   * @return the aggregationList
   */
  @Bindable
  public List<AggregationType> getAggregationList() {
    return aggregationList;
  }

  @Bindable
  public void setDefaultAggregationType( AggregationType defaultAggregationType ) {
    this.defaultAggregationType = defaultAggregationType;
    firePropertyChange( "defaultAggregationType", null, defaultAggregationType ); //$NON-NLS-1$
  }

  @Bindable
  public AggregationType getDefaultAggregationType() {
    return defaultAggregationType;
  }

  public String toString() {
    return aggregationList.toString();
  }
}
