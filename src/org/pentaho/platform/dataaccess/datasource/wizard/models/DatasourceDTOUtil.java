package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;

/**
 * User: nbaker
 * Date: Aug 13, 2010
 */
public class DatasourceDTOUtil {

  public static DatasourceDTO generateDTO(DatasourceModel model){
    DatasourceDTO dto = new DatasourceDTO();
    dto.setDatasourceName(model.getDatasourceName());
    dto.setCsvModelInfo(model.getModelInfo());
    dto.setDatasourceType(model.getDatasourceType());
    dto.setQuery(model.getQuery());
    dto.setConnectionName(model.getSelectedRelationalConnection().getName());
    return dto;
  }

  public static void populateModel(DatasourceDTO dto, DatasourceModel model){
    model.setDatasourceName(dto.getDatasourceName());
    model.setModelInfo(dto.getCsvModelInfo());
    model.setDatasourceType(dto.getDatasourceType());
    model.setQuery(dto.getQuery());
    model.setSelectedRelationalConnection(model.getGuiStateModel().getConnectionByName(dto.getConnectionName()));
  }

}
