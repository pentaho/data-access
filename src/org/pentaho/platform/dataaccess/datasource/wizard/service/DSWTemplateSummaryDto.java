package org.pentaho.platform.dataaccess.datasource.wizard.service;

/**
 * This class provides the structure for individual templates providing in the template listing service.
 * @author tkafalas
 *
 */
public class DSWTemplateSummaryDto {
  String templateID;
  String displayName;
  
  public DSWTemplateSummaryDto() {
    
  }
  
  public DSWTemplateSummaryDto(String templateID, String displayName) {
    this.templateID = templateID;
    this.displayName = displayName;
  }

  /**
* @return the templateName
*/
  public String getTemplateID() {
    return templateID;
  }

  /**
* @param templateName the templateName to set
*/
  public void setTemplateID(String templateID) {
    this.templateID = templateID;
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
