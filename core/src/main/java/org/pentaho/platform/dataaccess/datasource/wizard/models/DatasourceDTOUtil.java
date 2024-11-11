/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.wizard.models;

/**
 * User: nbaker Date: Aug 13, 2010
 */
public class DatasourceDTOUtil {

  public static DatasourceDTO generateDTO( DatasourceModel model ) {
    DatasourceDTO dto = new DatasourceDTO();
    dto.setDatasourceName( model.getDatasourceName() );
    dto.setCsvModelInfo( model.getModelInfo() );
    dto.setDatasourceType( model.getDatasourceType() );
    dto.setQuery( model.getQuery() );
    if ( model.getSelectedRelationalConnection() != null ) {
      dto.setConnectionName( model.getSelectedRelationalConnection().getName() );
    }
    return dto;
  }

  public static void populateModel( DatasourceDTO dto, DatasourceModel model ) {
    model.setDatasourceName( dto.getDatasourceName() );
    model.setModelInfo( dto.getCsvModelInfo() );
    model.setDatasourceType( dto.getDatasourceType() );
    model.setQuery( dto.getQuery() );
    model.setSelectedRelationalConnection( model.getGuiStateModel().getConnectionByName( dto.getConnectionName() ) );
  }

}
