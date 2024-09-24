/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.util.DatabaseUtil;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultitableDatasourceService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class JoinSelectionDebugGwtServlet extends RemoteServiceServlet implements IGwtJoinSelectionService {
  private static final long serialVersionUID = -6800729673421568704L;

  static {
    PentahoSystemHelper.init();
    try {
      KettleEnvironment.init();
      Props.init( Props.TYPE_PROPERTIES_EMPTY );
    } catch ( KettleException e ) {
      e.printStackTrace();
    }
  }

  private DatabaseMeta getDatabaseMeta( IDatabaseConnection connection ) throws Exception {
    DatabaseMeta databaseMeta = DatabaseUtil.convertToDatabaseMeta( connection );

    if ( connection.getName().equals( "SampleData" ) ) {
      databaseMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
      databaseMeta.setDBName( "SampleData" );
    }
    return databaseMeta;
  }

  public List<String> getDatabaseTables( IDatabaseConnection connection, String schema ) throws Exception {
    DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
    MultitableDatasourceService service = new MultitableDatasourceService( databaseMeta );
    return service.getDatabaseTables( connection, schema );
  }

  public List<String> retrieveSchemas( IDatabaseConnection connection ) throws Exception {
    DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
    MultitableDatasourceService service = new MultitableDatasourceService( databaseMeta );
    return service.retrieveSchemas( connection );
  }

  public IDatasourceSummary serializeJoins( MultiTableDatasourceDTO dto, IDatabaseConnection connection )
    throws Exception {
    DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
    MultitableDatasourceService service = new MultitableDatasourceService( databaseMeta );
    return service.serializeJoins( dto, connection );
  }

  public List<String> getTableFields( String table, IDatabaseConnection connection ) throws Exception {
    DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
    MultitableDatasourceService service = new MultitableDatasourceService( databaseMeta );
    return service.getTableFields( table, connection );
  }

  public MultiTableDatasourceDTO deSerializeModelState( String source ) throws Exception {
    MultitableDatasourceService service = new MultitableDatasourceService();
    return service.deSerializeModelState( source );
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return pojo;
  }
}
