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

import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ICsvDatasourceServiceAsync {

  public void stageFile( String name, String delimiter, String enclosure, boolean isFirstRowHeader, String encoding,
                         AsyncCallback<ModelInfo> aCallback );

  public void getStagedFiles( AsyncCallback<FileInfo[]> aCallback );

  public void generateDomain( DatasourceDTO datasourceDto, AsyncCallback<IDatasourceSummary> callback );

  public void getPreviewRows( String filename, boolean isFirstRowHeader, int rows, String encoding,
                              AsyncCallback<List<String>> callback ) throws Exception;

  public void getEncoding( String fileName, AsyncCallback<String> callback );

  public void gwtWorkaround( BogoPojo pojo, AsyncCallback<BogoPojo> callback );

}
