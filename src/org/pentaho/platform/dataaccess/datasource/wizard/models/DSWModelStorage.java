package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;
import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWExistingFileException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * This class contains the logic of what to do with a Serialized <code>IDSWTemplateModel</code> to
 * persist it to the repository.  Conversely, it also provides the logic to load the serialized
 * model from the repository, and use the <code>IDSWTemplate</code> to deserialize back into the
 * <code>IDSWTemplateModel</code> object.
 *  
 * @author tkafalas
 *
 */
public class DSWModelStorage implements IDSWModelStorage {
  public static final String TEMPLATE_ID_PROPERTY = "DSWTemplate";
  public static final String TEMPLATE_MODEL_PROPERTY = "DSWTemplateModel";
  private IMetadataDomainRepository metadataDomainRepository = PentahoSystem.get(IMetadataDomainRepository.class, PentahoSessionHolder.getSession());
 
  @Override
  public void storeModel(String serializedModel, IDSWDataSource iDSWDataSource)  throws DSWException {
    Domain domain = metadataDomainRepository.getDomain(iDSWDataSource.getName());
    LogicalModel logicalModel = domain.getLogicalModels().get(0);
    logicalModel.setProperty(TEMPLATE_ID_PROPERTY, iDSWDataSource.getTemplate().getID());
    logicalModel.setProperty(TEMPLATE_MODEL_PROPERTY, serializedModel);

    try {
      metadataDomainRepository.storeDomain(domain, true);
    } catch (DomainIdNullException e) {
      throw new DSWException("Domain was null");
    } catch (DomainAlreadyExistsException e) {
      throw new DSWExistingFileException();
    } catch (DomainStorageException e) {
      throw new DSWException("Failure in metadata layer");
    }
  }

  @Override
  public IDSWTemplateModel loadModel(String dataSourceID) throws DSWException {
    //Load Domain from repo
    Domain domain = metadataDomainRepository.getDomain(dataSourceID);
    //Get the propeties pertaining to the model
    LogicalModel logicalModel = domain.getLogicalModels().get(0);
    String templateID = (String) logicalModel.getProperty(TEMPLATE_ID_PROPERTY);
    String serializedModel = (String) logicalModel.getProperty(TEMPLATE_MODEL_PROPERTY);
    //Get the template and deserialize the model string
    
    DSWDataSourceWizard dswDataSourceWizard = PentahoSystem.get(DSWDataSourceWizard.class);
    IDSWTemplate iDSWTemplate = dswDataSourceWizard.getTemplateByID(templateID);
    IDSWTemplateModel iDSWTemplateModel = iDSWTemplate.deserialize(serializedModel);
    return iDSWTemplateModel;
  }

}
