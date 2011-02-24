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
 * Created May 26, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.concept.types.AggregationType;
import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;


public class CsvModelDataRow extends XulEventSourceAdapter{
  public static final int MAX_COL_SIZE = 15;
  private String columnName, sampleData;
  private List<String> sampleDataList;
  private List<DataType> dataTypes = new ArrayList<DataType>();
  private DataType selectedDataType;
  private String locale;
  private Aggregation aggregation;
  //private List<DataFormatType> dataFormatTypes = new ArrayList<DataFormatType>();
  //private DataFormatType selectedDataFormatType;
  
  // Commenting out data format for now
 public CsvModelDataRow(LogicalColumn col, List<String> columnData,String locale) {
    setSelectedDataType(col.getDataType());
    //setSelectedDataFormatType(DataFormatType.CURRENCY);
    setAggregation(new Aggregation(col.getAggregationList(), AggregationType.NONE));
    setColumnName(col.getName().getString(locale));
    if(columnData.size() > 0) {
      setSampleData(columnData.get(0));
      setSampleDataList(columnData);
    }
  }

  @Bindable
  public String getColumnName() {
    return columnName;
  }


  @Bindable
  public void setColumnName(String columnName) {
    this.columnName = columnName;
  }

  @Bindable
  public String getSampleData() {
    return sampleData;
  }


  @Bindable
  public void setSampleData(String sampleData) {
    this.sampleData = sampleData;
  }

  @Bindable
  public List<DataType> getDataTypes() {
    return dataTypes;
  }


  @Bindable
  public void setDataTypes(List<DataType> dataTypes) {
    this.dataTypes = dataTypes;
  }

  @Bindable
  public List<String> getSampleDataList() {
    return sampleDataList;
  }


  @Bindable
  public void setSampleDataList(List<String> sampleDataList) {
    this.sampleDataList = sampleDataList;
  }

  @Bindable
  public DataType getSelectedDataType() {
    return selectedDataType;
  }


  @Bindable
  public void setSelectedDataType(DataType selectedDataType) {
    this.selectedDataType = selectedDataType;
  }


  @Bindable
  public void setSelectedDataType(Object o){
    setSelectedDataType((DataType)  o);
  }
  
  @Bindable
  public Vector getBindingDataTypes(){
    Vector v = new Vector();
    //for(DataType t : this.dataTypes){
    for(DataType t : DataType.values()){
      v.add(t);
    }
    return v;
  }

  @Bindable
  public void setAggregation(Aggregation aggregation) {
    this.aggregation = aggregation;
  }

  @Bindable
  public Aggregation getAggregation() {
    return aggregation;
  }
  
  //public List<DataFormatType> getDataFormatTypes() {
  //  return dataFormatTypes;
  //}


  //public void setDataFormatTypes(List<DataFormatType> dataFormatTypes) {
  //  this.dataFormatTypes = dataFormatTypes;
  //}


  //public DataFormatType getSelectedDataFormatType() {
   // return selectedDataFormatType;
  //}


  //public void setSelectedDataFormatType(DataFormatType selectedDataFormatType) {
  //  this.selectedDataFormatType = selectedDataFormatType;
 // }
  
  //public void setSelectedDataFormatType(Object o){
  //  setSelectedDataFormatType((DataFormatType)  o);
  //}
  
  //public Vector getBindingDataFormatTypes(){
  //  Vector v = new Vector();
    //for(DataFormatType t : this.dataFormatTypes){
   // for(DataFormatType t : DataFormatType.values()){
   //   v.add(t);
   // }
   // return v;
 // }

}
