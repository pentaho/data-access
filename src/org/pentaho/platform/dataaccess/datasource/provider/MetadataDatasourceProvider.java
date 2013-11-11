package org.pentaho.platform.dataaccess.datasource.provider;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;
import org.pentaho.platform.dataaccess.catalog.impl.Datasource;

public class MetadataDatasourceProvider implements IDatasourceProvider{

  private IMetadataDomainRepository metadataDomainRepository;
  private IDatasourceType datasourceType = new MetadataDatasourceType();
  
  public MetadataDatasourceProvider(final IMetadataDomainRepository metadataDomainRepository) {
    this.metadataDomainRepository = metadataDomainRepository;
  }
  
  @Override
  public List<IDatasource> getDatasources() {
    List<IDatasource> datasources = new ArrayList<IDatasource>();
    for(String id :metadataDomainRepository.getDomainIds()) {
      datasources.add( new Datasource(id, getType(), null) );
    }
    return datasources;
  }

  @Override
  public IDatasourceType getType() {
    return datasourceType;
  }

}
