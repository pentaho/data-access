package org.pentaho.platform.dataaccess.datasource.provider;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.catalog.api.IDatasource;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceChild;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceProvider;
import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;
import org.pentaho.platform.dataaccess.catalog.impl.Datasource;
import org.pentaho.platform.dataaccess.catalog.impl.DatasourceChild;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalogHelper;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;

public class AnalysisDatasourceProvider implements IDatasourceProvider {

  private MondrianCatalogHelper mondrianCatalogHelper;
  private IDatasourceType datasourceType = new AnalysisDatasourceType();

  public AnalysisDatasourceProvider( final MondrianCatalogHelper mondrianCatalogHelper ) {
    this.mondrianCatalogHelper = mondrianCatalogHelper;
  }

  @Override
  public List<IDatasource> getDatasources() {
    List<IDatasource> datasources = new ArrayList<IDatasource>();

    for ( MondrianCatalog mondrianCatalog : mondrianCatalogHelper.listCatalogs( PentahoSessionHolder.getSession(),
        false ) ) {
      List<IDatasourceChild> datasourceChildren = new ArrayList<IDatasourceChild>();

      for ( MondrianCube cube : mondrianCatalog.getSchema().getCubes() ) {
        datasourceChildren.add( new DatasourceChild( cube.getId(), cube.getName(), null ) );
      }

      IDatasourceChild datasourceChild =
          new DatasourceChild( mondrianCatalog.getSchema().getName(), mondrianCatalog.getSchema().getName(),
              datasourceChildren );

      List<IDatasourceChild> children = new ArrayList<IDatasourceChild>();
      children.add( datasourceChild );
      datasources.add( new Datasource( mondrianCatalog.getName(), getType(), children ) );
    }
    return null;
  }

  @Override
  public IDatasourceType getType() {
    return datasourceType;
  }

}
