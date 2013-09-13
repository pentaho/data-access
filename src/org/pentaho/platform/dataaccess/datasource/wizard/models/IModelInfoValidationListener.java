package org.pentaho.platform.dataaccess.datasource.wizard.models;

public interface IModelInfoValidationListener {

  public void onCsvValid();
  public void onCsvInValid();

  public void onModelInfoValid();
  public void onModelInfoInvalid();
  
}
