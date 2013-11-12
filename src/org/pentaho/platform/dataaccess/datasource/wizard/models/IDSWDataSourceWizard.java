package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;

/**
 * Intended as a singleton, this class provides various convenience services for DSWDataSource
 * 
 * @author tkafalas
 *
 */
public interface IDSWDataSourceWizard {
  /**
   * 
   * @return a list of all <code>ISDWTemplate</code> implementations registered with this class.
   */
  List<IDSWTemplate> getTemplates ();
  
  /**
   *
   * @param templateID The unique ID name of the template.
   * @return the template with an ID matching templateID.
   */
  IDSWTemplate getTemplateByID ( String templateID );
  
  /**
   * 
   * @param dswDataSource
   * @return the <code>IDSWTemplate</code> associated with the given the <code>IDSWDataSource</code>
   */
  IDSWTemplate getTemplateByDatasource ( IDSWDataSource dswDataSource);
  
  /**
   * Persists the datasource to the repository.
   * 
   * @param dswDataSource The <code>IDSWDataSource</code> to persist.
   * @param overwrite true allows overwrite of an existing data.  If false,
   * then an attempt to overwrite an existing datasource will generate a <code>DSWExistingFileException</code>. 
   * @throws DSWException
   */
  void storeDataSource(IDSWDataSource dswDataSource, boolean overwrite) throws DSWException;
  
  /**
   * 
   * @param dataSourceId The name assigned to the datasource
   * @return The IDSWDataSource that is persisted in the repository.
   * @throws DSWException If the load fails
   */
  IDSWDataSource loadDataSource ( String dataSourceId) throws DSWException;
}
