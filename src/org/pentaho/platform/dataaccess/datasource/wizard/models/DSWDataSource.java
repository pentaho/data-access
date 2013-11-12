package org.pentaho.platform.dataaccess.datasource.wizard.models;

/**
 * This class represents the server support needed to drive the UI for editing the datasource.  The <code>IDSWTemplate</code>
 * is responsible for serializing/de-serializing the state of the UI for the Datasource.  This state is stored in the
 * <code>IDSWTemplateModel</code>.
 * 
 * @author tkafalas
 *
 */
public class DSWDataSource implements IDSWDataSource {
  private String name;
  private IDSWTemplate iDSWTemplate;
  private IDSWTemplateModel iDSWTemplateModel;
  
  public DSWDataSource(String name, IDSWTemplate iDSWTemplate, IDSWTemplateModel iDSWTemplateModel) {
    this.name = name ;
    this.iDSWTemplate = iDSWTemplate;
    this.iDSWTemplateModel = iDSWTemplateModel;
  }
  
  @Override
  public String getName() {
    return name;
  }

  @Override
  public IDSWTemplate getTemplate() {
    return iDSWTemplate;
  }

  @Override
  public IDSWTemplateModel getModel() {
    return iDSWTemplateModel;
  }

}
