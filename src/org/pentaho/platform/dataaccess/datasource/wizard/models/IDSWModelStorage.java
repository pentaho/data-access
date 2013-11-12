package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;

/**
 * This interface provides the api for the methods save a seialized model, or, load the serialized
 * model from the repository and using the <code>IDSWTemplateModel</code>, de-serialized it back into
 * an <code>IDSWTemplateModel</code>.
 *  
 * @author tkafalas
 *
 */
public interface IDSWModelStorage {
  /**
   * Persist the serilizedModel provided for the given <code>IDSWDataSource</code>
   * @param serializedModel The model to be written to the repository.
   * @param iDSWDataSource The DSWDatasource associated with the model.
   * @throws DSWException  thrown if the method fails.
   */
  void storeModel(String serializedModel, IDSWDataSource iDSWDataSource) throws DSWException;
  
  /**
   * Read the serializedModel in from the repository and call the models IDSWTemplate to de-serialize
   * it into an <code>IDSWTemplate</code>.
   * @param dataSourceID
   * @return The <code>IDSWTemplateModel</code> object representing the state of the UI.
   * @throws DSWException
   */
  IDSWTemplateModel loadModel( String dataSourceID ) throws DSWException;
}
