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

package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.RowListener;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DataRow;

public class PdiRowListener implements RowListener {

  private List<Object[]> read = new ArrayList<Object[]>();

  private List<Object[]> written = new ArrayList<Object[]>();

  private List<Object[]> error = new ArrayList<Object[]>();

  public void errorRowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
    error.add( row );
  }

  public void rowReadEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
    read.add( row );
  }

  public void rowWrittenEvent( RowMetaInterface rowMeta, Object[] row ) throws KettleStepException {
    for ( int i = 0; i < row.length; i++ ) {
      if ( row[ i ] instanceof byte[] ) {
        try {
          row[ i ] = rowMeta.getValueMeta( i ).convertBinaryStringToNativeType( (byte[]) row[ i ] );
        } catch ( KettleValueException e ) {
          // couldn't convert it back to the native type, leave it as is
        }
      } else {
        continue;
      }
    }
    written.add( row );
  }

  public DataRow[] getReadRows() {
    return getDataRows( read );
  }

  public DataRow[] getWrittenRows() {
    return getDataRows( written );
  }

  public DataRow[] getErrorRows() {
    return getDataRows( error );
  }

  private DataRow[] getDataRows( List<Object[]> list ) {
    DataRow[] rows = new DataRow[ list.size() ];
    int idx = 0;
    for ( Object[] cells : list ) {
      rows[ idx ] = new DataRow();
      rows[ idx ].setCells( cells );
      idx++;
    }
    return rows;
  }

}
