package org.pentaho.platform.dataaccess.datasource.provider;

import org.pentaho.platform.dataaccess.catalog.impl.DatasourceType;

public class MetadataDatasourceType extends DatasourceType {
  public MetadataDatasourceType( String id, String displayName ) {
    super( id, displayName );
  }

  public static final String ID = "METADATA"; 
}

