/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

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
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IDSWDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DSWDatasourceServiceImpl;
import org.pentaho.reporting.libraries.base.util.StringUtils;

/**
 * User: nbaker Date: Jul 16, 2010
 */
public class InlineSqlModelerSource implements ISpoonModelerSource {

  private DatabaseMeta databaseMeta;
  private String query, datasourceName;
  private IDSWDatasourceService datasourceImpl;
  private String connectionName;
  private String dbType;

  public static final String SOURCE_TYPE = InlineSqlModelerSource.class.getSimpleName();

  public InlineSqlModelerSource( String connectionName, String dbType, String query, String datasourceName ) {
    this( new DSWDatasourceServiceImpl(), connectionName, dbType, query, datasourceName );
  }

  public InlineSqlModelerSource( IDSWDatasourceService datasourceService, String connectionName, String dbType,
                                 String query, String datasourceName ) {
    this.query = query;
    this.dbType = dbType;
    this.connectionName = connectionName;
    this.datasourceName = datasourceName;
    this.datasourceImpl = datasourceService;
  }


  public String getDatabaseName() {
    return databaseMeta.getName();
  }

  @Override
  public Domain generateDomain( boolean dualModelingMode ) throws ModelerException {
    try {
      BusinessData bd = datasourceImpl.generateLogicalModel( datasourceName, connectionName, dbType, query, "10" );
      Domain domain = bd.getDomain();
      return domain;
    } catch ( DatasourceServiceException dce ) {
      throw new ModelerException( dce );
    }
  }

  public Domain generateDomain() throws ModelerException {
    return generateDomain( true );
  }

  public void initialize( Domain domain ) throws ModelerException {
    SqlPhysicalModel model = (SqlPhysicalModel) domain.getPhysicalModels().get( 0 );
    SqlPhysicalTable table = model.getPhysicalTables().get( 0 );

    String targetTable = (String) table.getProperty( "target_table" ); //$NON-NLS-1$
    if ( !StringUtils.isEmpty( targetTable ) ) {
      domain.setId( targetTable );
    }

    this.databaseMeta = ThinModelConverter.convertToLegacy( model.getId(), model.getDatasource() );

  }

  public void serializeIntoDomain( Domain d ) {
    LogicalModel lm = d.getLogicalModels().get( 0 );
    lm.setProperty( "source_type", SOURCE_TYPE ); //$NON-NLS-1$
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabaseMeta( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
  }

  public String getSchemaName() {
    return "";
  }

  public String getTableName() {
    return "INLINE_SQL_1";
  }
}
