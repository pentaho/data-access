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

import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;

import com.google.gwt.user.client.rpc.RemoteService;

public interface ICsvDatasourceService extends RemoteService {

  public ModelInfo stageFile( String name, String delimiter, String enclosure, boolean isFirstRowHeader,
                              String encoding ) throws Exception;

  public FileInfo[] getStagedFiles() throws Exception;

  public FileTransformStats generateDomain( DatasourceDTO datasourceDto ) throws Exception;

  public List<String> getPreviewRows( String filename, boolean isFirstRowHeader, int rows, String encoding )
    throws Exception;

  public String getEncoding( String fileName ) throws Exception;

  public BogoPojo gwtWorkaround( BogoPojo pojo );
}
