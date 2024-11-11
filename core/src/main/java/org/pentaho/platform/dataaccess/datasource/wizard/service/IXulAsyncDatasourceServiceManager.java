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

import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

public interface IXulAsyncDatasourceServiceManager {

  void isAdmin( XulServiceCallback<Boolean> callback );

  void getAnalysisDatasourceIds( XulServiceCallback<List<String>> callback );

  void getMetadataDatasourceIds( XulServiceCallback<List<String>> callback );

  void getDSWDatasourceIds( XulServiceCallback<List<String>> callback );

  void export( IDatasourceInfo dsInfo );

  void remove( IDatasourceInfo dsInfo, Object callback );
}
