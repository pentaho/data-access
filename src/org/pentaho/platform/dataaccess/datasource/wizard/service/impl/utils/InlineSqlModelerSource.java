package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.util.ISpoonModelerSource;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.SqlPhysicalTable;
import org.pentaho.metadata.util.ThinModelConverter;
import org.pentaho.platform.dataaccess.datasource.beans.BusinessData;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DatasourceServiceImpl;
import org.pentaho.reporting.libraries.base.util.StringUtils;

/**
 * User: nbaker
 * Date: Jul 16, 2010
 */
public class InlineSqlModelerSource implements ISpoonModelerSource {

  private DatabaseMeta databaseMeta;
  private String query, datasourceName;
  private static Log logger = LogFactory.getLog(InlineSqlModelerSource.class);
  private IDatasourceService datasourceImpl;
  private String connectionName;
  private String dbType;

	public static final String SOURCE_TYPE = InlineSqlModelerSource.class.getSimpleName();

  public InlineSqlModelerSource( String connectionName, String dbType, String query, String datasourceName){
    this(new DatasourceServiceImpl(), connectionName, dbType, query, datasourceName);
  }

  public InlineSqlModelerSource( IDatasourceService datasourceService, String connectionName, String dbType, String query, String datasourceName){
    this.query = query;
    this.dbType = dbType;
    this.connectionName = connectionName;
    this.datasourceName = datasourceName;
    this.datasourceImpl = datasourceService;
  }


  public String getDatabaseName() {
    return databaseMeta.getName();
  }

  public Domain generateDomain() throws ModelerException {
    try{
      BusinessData bd =  datasourceImpl.generateLogicalModel(datasourceName, connectionName, dbType, query, "10");
      return bd.getDomain();
    } catch(DatasourceServiceException dce){
      throw new ModelerException(dce);
    }
  }

  public void initialize(Domain domain) throws ModelerException {
    SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get(0);
    SqlPhysicalTable table = model.getPhysicalTables().get(0);

    String targetTable = (String) table.getProperty("target_table"); //$NON-NLS-1$
    if(!StringUtils.isEmpty(targetTable)) {
      domain.setId(targetTable);
    }

    this.databaseMeta = ThinModelConverter.convertToLegacy(model.getId(), model.getDatasource());

  }

  public void serializeIntoDomain(Domain d) {
    LogicalModel lm = d.getLogicalModels().get(0);
    lm.setProperty("source_type", SOURCE_TYPE); //$NON-NLS-1$
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabaseMeta(DatabaseMeta databaseMeta) {
    this.databaseMeta = databaseMeta;
  }

  public String getSchemaName() {
    return "";
  }

  public String getTableName() {
    return "INLINE_SQL_1";
  }
}
