package org.pentaho.platform.dataaccess.datasource.wizard.models;

/**
 * This interface provides the api for the server support needed to drive the UI for editing the datasource.  The
 * <code>IDSWTemplate</code> is responsible for serializing/de-serializing the state of the UI for the Datasource.
 * This state is stored in the <code>IDSWTemplateModel</code>.
 * 
 * @author tkafalas
 *
 */
public interface IDSWDataSource {
  /**
   *
   * @return The formal name of the DataSource
   */
  String getName ();
  
  /**
   * 
   * @return The template which allows serialization/de-serailization of the UI state as well as providing a base model
   * suitable adding a new datasource. 
   */
  IDSWTemplate getTemplate();
  
  /**
   * 
   * @return The state of the UI, if it exists, or null if it is not defined.
   */
  IDSWTemplateModel getModel ();
}
