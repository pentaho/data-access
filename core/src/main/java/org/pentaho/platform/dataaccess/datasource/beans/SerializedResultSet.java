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

package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.List;

public class SerializedResultSet implements java.io.Serializable {
  private static final long serialVersionUID = 8275330793662889379L;
  private String[] columns; // contains column names
  private int[] columnTypes; // contains column types
  private List<List<String>> data; // 2 dimensional array

  public SerializedResultSet( int[] columnTypes, String[] columns, List<List<String>> data ) {
    super();
    this.columnTypes = columnTypes;
    this.columns = columns;
    this.data = data;
  }

  public SerializedResultSet() {

  }

  public String[] getColumns() {
    return columns;
  }

  public void setColumns( String[] columns ) {
    this.columns = columns;
  }

  public int[] getColumnTypes() {
    return columnTypes;
  }

  public void setColumnTypes( int[] columnTypes ) {
    this.columnTypes = columnTypes;
  }

  public List<List<String>> getData() {
    return data;
  }

  public void setData( List<List<String>> data ) {
    this.data = data;
  }
}
