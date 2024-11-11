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
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;

import com.google.gwt.user.client.rpc.RemoteService;

public interface IGwtJoinSelectionService extends RemoteService {

  List<String> getDatabaseTables( IDatabaseConnection connection, String schema ) throws Exception;

  List<String> retrieveSchemas( IDatabaseConnection connection ) throws Exception;

  List<String> getTableFields( String table, IDatabaseConnection connection ) throws Exception;

  IDatasourceSummary serializeJoins( MultiTableDatasourceDTO dto, IDatabaseConnection connection ) throws Exception;

  MultiTableDatasourceDTO deSerializeModelState( String source ) throws Exception;

  BogoPojo gwtWorkaround( BogoPojo pojo );
}
