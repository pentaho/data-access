package org.pentaho.platform.dataaccess.datasource.provider;

import org.pentaho.platform.dataaccess.catalog.impl.DatasourceType;

public class AnalysisDatasourceType extends DatasourceType{
  public static final String ID = "ANALYSIS"; 

  public AnalysisDatasourceType( String id, String displayName ) {
    super( id, displayName );
  }

}

