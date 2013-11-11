package org.pentaho.platform.dataaccess.datasource.provider;

import org.pentaho.platform.dataaccess.catalog.impl.DatasourceType;

public class JDBCDatasourceType extends DatasourceType{

  public JDBCDatasourceType( String id, String displayName ) {
    super( id, displayName );
  }

  public static final String ID = "JDBC"; 
}
