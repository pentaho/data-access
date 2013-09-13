/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.List;

public class SerializedResultSet implements java.io.Serializable{
  private static final long serialVersionUID = 8275330793662889379L;
  private String[] columns;// contains column names
  private int[] columnTypes;// contains column types
  private List<List<String>> data;// 2 dimensional array

  public SerializedResultSet(int[] columnTypes, String[] columns, List<List<String>> data) {
    super();
    this.columnTypes = columnTypes;
    this.columns = columns;
    this.data = data;
  }
  
  public SerializedResultSet()
  {
    
  }
  public String[] getColumns() {
    return columns;
  }
  public void setColumns(String[] columns) {
    this.columns = columns;
  }
  public int[] getColumnTypes() {
    return columnTypes;
  }
  public void setColumnTypes(int[] columnTypes) {
    this.columnTypes = columnTypes;
  }
  public List<List<String>> getData() {
    return data;
  }
  public void setData(List<List<String>> data) {
    this.data = data;
  }
}