package org.pentaho.platform.dataaccess.datasource.provider;

import java.util.Locale;
import java.util.ResourceBundle;

import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;
import org.pentaho.platform.dataaccess.datasource.provider.messages.Messages;;

public class JDBCDatasourceType implements IDatasourceType{
  private static final String ID = "JDBC"; 
  ResourceBundle resourceBundle;
  @Override
  public String getId() {
    return ID;
  }

  @Override
  public String getDisplayName( Locale locale ) {
    resourceBundle = Messages.getInstance().getBundle(locale);
    return resourceBundle.getString( ID );
  }

}
