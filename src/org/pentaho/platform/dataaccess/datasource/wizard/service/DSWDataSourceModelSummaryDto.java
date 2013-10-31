package org.pentaho.platform.dataaccess.datasource.wizard.service;

public class DSWDataSourceModelSummaryDto {
  String modelName;
  String displayName;
  
  public DSWDataSourceModelSummaryDto() {
    
  }
  
  public DSWDataSourceModelSummaryDto(String modelName, String displayName) {
    this.modelName = modelName;
    this.displayName = displayName;
  }

  /**
   * @return the modelName
   */
  public String getModelName() {
    return modelName;
  }

  /**
   * @param modelName the modelName to set
   */
  public void setModelName(String modelName) {
    this.modelName = modelName;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName the displayName to set
   */
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

}
