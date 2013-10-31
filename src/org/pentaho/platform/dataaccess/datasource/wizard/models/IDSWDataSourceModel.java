package org.pentaho.platform.dataaccess.datasource.wizard.models;


public interface IDSWDataSourceModel {
  /**
   * Get the name of the data source model
   * 
   * @param modelName
   * @return
   */
  public String getModelName();
  
  /**
   * Get the display name of the model.
   * @return
   */
  public String getDisplayName();
  
  /**
   * Returns the class concrete class that will handle the functions specific to this
   * model.
   * @return
   */
  public Class<IDSWModelImplementer> getImplementingClass();
  
  /**
   * Gets the Implementing object for this model.  The object will be of the same class as the
   * <code>getImplementingClass</code>
   * @return
   */
  public IDSWModelImplementer getImplementer();
  
  /**
   * Deserializes the IDSWDataSourceCoreService object.
   * @param IDSWDataSourceCoreService
   * @return
   */
  public IDSWDataSourceModel deserialize(String IDSWDataSourceCoreService) throws Exception;
  
  /**
   * Create a serialized version of this object suitable for storage.
   * @param iDSWDataSourceCoreService
   * @return
   */
  public String serialize(IDSWDataSourceModel iDSWDataSourceCoreService) throws Exception;
  
  /**
   * Check if the object is suitable for storage and/or object creation doing any checks for required data or sanity
   * @return true if valid
   */
  public boolean isValid();
  
}
