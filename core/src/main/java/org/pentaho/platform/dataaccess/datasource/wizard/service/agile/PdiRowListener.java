/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

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
