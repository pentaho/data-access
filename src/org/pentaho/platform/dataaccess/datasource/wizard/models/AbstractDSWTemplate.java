package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.Locale;
import java.util.MissingResourceException;

import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;
import org.pentaho.platform.dataaccess.datasource.wizard.messages.Messages;

/**
 * Abstract Implementation of the IDSWTemplate.  Implements getters/setters for basic fields.  Does not
 * implement creation of the datasource or serialization/deserialization of the templateModel.
 * 
 * @author tkafalas
 *
 */
public abstract class AbstractDSWTemplate implements IDSWTemplate {

  private String id;
  private String displayName;  //default display name, used in testing
  
  public AbstractDSWTemplate(String id, String displayName) { 
    this.id = id;
    this.displayName = displayName;
  }
  
  @Override
  public String getID() {
    return id;
  }

  @Override
  public String getDisplayName(Locale locale) {
    //TODO: Other Messages class was not loading the resource so switched to old version
    String localizedString = Messages.getInstance().getString( "TEMPLATE_NAME_" + id);
    if (displayName != null  && ("!TEMPLATE_NAME_" + id + "!").equals(localizedString)) {
      return displayName;
    } else {
      return localizedString;
    }
  }
  
  //=====================  Should be overridden  ==========
  
  @Override
  public abstract void createDatasource( IDSWDataSource iDSWDataSource, boolean overwrite ) throws DSWException;

  @Override
  public abstract IDSWTemplateModel deserialize(String serializedModel) throws DSWException;

  @Override
  public abstract String serialize(IDSWTemplateModel dswTemplateModel) throws DSWException;
  
}
