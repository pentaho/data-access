/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

public interface IGwtDatasourceServiceManager extends RemoteService {

  public Boolean isAdmin();

  public List<String> getAnalysisDatasourceIds();

  public List<String> getMetadataDatasourceIds();

  public List<String> getDSWDatasourceIds();
}
