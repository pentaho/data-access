package org.pentaho.platform.dataaccess.datasource.wizard.models;


public class DSWDataSourceModel implements IDSWDataSourceModel {
  
  String modelName;
  String displayName;
  String implementerClassName;
  IDSWModelImplementer modelImplementer;
  
  public DSWDataSourceModel(String modelName, String displayName) { //, String implementerClassName, IDSWModelImplementer modelImplementer) {
    this.modelName = modelName;
    this.displayName = displayName;
    //this.implementerClassName = implementerClassName;
    //this.modelImplementer = modelImplementer;
  }

  @Override
  public String getModelName() {
    return modelName;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<IDSWModelImplementer> getImplementingClass() {
    try {
      return (Class<IDSWModelImplementer>) Class.forName(implementerClassName);
    } catch (ClassNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public IDSWModelImplementer getImplementer() {
    return modelImplementer;
  }

  @Override
  public IDSWDataSourceModel deserialize(String IDSWDataSourceCoreService) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String serialize(IDSWDataSourceModel iDSWDataSourceCoreService) throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isValid() {
    return hasValue(displayName) && hasValue(modelName);
  }

  private boolean hasValue(String value) {
    return value != null && value.length() > 0;
  }

}
