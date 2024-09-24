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

package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncJoinSelectionService {

  void getDatabaseTables( IDatabaseConnection connection, String schema, XulServiceCallback<List> callback );

  void retrieveSchemas( IDatabaseConnection connection, XulServiceCallback<List> callback );

  void getTableFields( String table, IDatabaseConnection connection, XulServiceCallback<List> callback );

  void serializeJoins( MultiTableDatasourceDTO dto, IDatabaseConnection connection,
                       XulServiceCallback<IDatasourceSummary> callback );

  void deSerializeModelState( String source, XulServiceCallback<MultiTableDatasourceDTO> callback );

  void gwtWorkaround( BogoPojo pojo, XulServiceCallback<BogoPojo> callback );
}
