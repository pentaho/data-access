package org.pentaho.platform.dataaccess.datasource.wizard.models;

public interface IDSWTemplateModel {
  /**
   * Return the unique Id name of the template.  Each template Id should be implemented by only
   * one object and that object should be registered in plugin.spring.xml file for it's plugin.
   * 
   * @return The Id with identifies the template responsible for serializing and deserializing this
   * model.
   */
  String getTemplateID();
}
