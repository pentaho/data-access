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

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface IGwtDatasourceServiceManagerAsync {

  void getAnalysisDatasourceIds( AsyncCallback<List<String>> callback );

  void getMetadataDatasourceIds( AsyncCallback<List<String>> callback );

  void getDSWDatasourceIds( AsyncCallback<List<String>> callback );

  void isAdmin( AsyncCallback<Boolean> callback );
}
