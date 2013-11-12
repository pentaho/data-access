package org.pentaho.platform.dataaccess.datasource.wizard.models;

/**
 * Serves as a placeholder for true implementations of the IDSWTemplate until they are developed.
 * 
 * @author tkafalas
 *
 */
public class MockDSWTemplateModel implements IDSWTemplateModel {
  private String templateID;
  private String mockModelData =  "The quick brown fox jumped over the lazy dogs back";
  
  public MockDSWTemplateModel(String templateID) {
    this.templateID = templateID;
  }

  @Override
  public String getTemplateID() {
    return templateID;
  }
  
  public String getMockData(){
    return mockModelData;
  }

}
