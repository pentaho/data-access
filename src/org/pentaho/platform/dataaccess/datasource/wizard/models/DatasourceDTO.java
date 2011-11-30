package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;

import org.pentaho.platform.dataaccess.datasource.DatasourceType;

/**
 * This class is used to serialize out the current state of a DataSource. It contains enough information
 * to reconstitute the Datasource Dialog, associated models and no more.
 *
 * User: nbaker
 * Date: Aug 12, 2010
 */
@Deprecated
public class DatasourceDTO implements Serializable {

  private static final long serialVersionUID = 2498165523678485182L;

  private String datasourceName;
  private DatasourceType datasourceType;
  private ModelInfo csvModelInfo;
  private String query;
  private String connectionName;
  private double version = 2.0;

  public DatasourceDTO() {

  }

  public static DatasourceDTO generateDTO(DatasourceModel model){
    DatasourceDTO dto = new DatasourceDTO();
    dto.setDatasourceName(model.getDatasourceName());
    dto.setCsvModelInfo(model.getModelInfo());
    dto.setDatasourceType(model.getDatasourceType());
    dto.setQuery(model.getQuery());
    if(model.getSelectedRelationalConnection() != null){
      dto.setConnectionName(model.getSelectedRelationalConnection().getName());
    }
    return dto;
  }

  public static void populateModel(DatasourceDTO dto, DatasourceModel model){
    model.setDatasourceName(dto.getDatasourceName());
    model.setModelInfo(dto.getCsvModelInfo());
    model.setDatasourceType(dto.getDatasourceType());
    model.setQuery(dto.getQuery());
    model.setSelectedRelationalConnection(model.getGuiStateModel().getConnectionByName(dto.getConnectionName()));
  }

  public String getDatasourceName() {
    return datasourceName;
  }

  public void setDatasourceName(String datasourceName) {
    this.datasourceName = datasourceName;
  }

  public DatasourceType getDatasourceType() {
    return datasourceType;
  }

  public void setDatasourceType(DatasourceType datasourceType) {
    this.datasourceType = datasourceType;
  }

  public ModelInfo getCsvModelInfo() {
    return csvModelInfo;
  }

  public void setCsvModelInfo(ModelInfo csvModelInfo) {
    this.csvModelInfo = csvModelInfo;
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public String getConnectionName() {
    return connectionName;
  }

  public void setConnectionName(String connectionName) {
    this.connectionName = connectionName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    DatasourceDTO that = (DatasourceDTO) o;

    if (Double.compare(that.version, version) != 0) return false;
    if (connectionName != null ? !connectionName.equals(that.connectionName) : that.connectionName != null)
      return false;
    if (csvModelInfo != null ? !csvModelInfo.equals(that.csvModelInfo) : that.csvModelInfo != null) return false;
    if (datasourceName != null ? !datasourceName.equals(that.datasourceName) : that.datasourceName != null)
      return false;
    if (datasourceType != that.datasourceType) return false;
    if (query != null ? !query.equals(that.query) : that.query != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    result = datasourceName != null ? datasourceName.hashCode() : 0;
    result = 31 * result + (datasourceType != null ? datasourceType.hashCode() : 0);
    result = 31 * result + (csvModelInfo != null ? csvModelInfo.hashCode() : 0);
    result = 31 * result + (query != null ? query.hashCode() : 0);
    result = 31 * result + (connectionName != null ? connectionName.hashCode() : 0);
    return result;
  }
}
