package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.Locale;

import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;

public interface IDSWTemplate {
  /**
   * Get the ID of the data source model
   * 
   * @param modelName
   * @return
   */
  String getID();
  
  /**
   * Get the display name of the model.
   * @return
   */
  String getDisplayName(Locale locale);
  
  /**
   * Creates the underlaying domain for the template
   * 
   * @param iDSWDataSource
   */
  void createDatasource(IDSWDataSource iDSWDataSource, boolean overwrite) throws DSWException;
  
  /**
   * Serialize the IDSWTemplateModel into a string
   *  
   * @param dswTemplateModel
   * @return
   */
  String serialize(IDSWTemplateModel dswTemplateModel) throws DSWException;
  
  /**
   * Deserializes the IDSWTemplateModel object.
   * @param IDSWTemplateModel
   * @return
   */
  IDSWTemplateModel deserialize(String serializedModel) throws DSWException;
}
