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


package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtJoinSelectionServiceAsync {

  void getDatabaseTables( IDatabaseConnection connection, String schema, AsyncCallback<List> callback );

  void retrieveSchemas( IDatabaseConnection connection, AsyncCallback<List> callback );

  void getTableFields( String table, IDatabaseConnection connection, AsyncCallback<List> callback );

  void serializeJoins( MultiTableDatasourceDTO dto, IDatabaseConnection connection,
                       AsyncCallback<IDatasourceSummary> callback );

  void deSerializeModelState( String source, AsyncCallback<MultiTableDatasourceDTO> callback );

  void gwtWorkaround( BogoPojo pojo, AsyncCallback<BogoPojo> callback );
}
