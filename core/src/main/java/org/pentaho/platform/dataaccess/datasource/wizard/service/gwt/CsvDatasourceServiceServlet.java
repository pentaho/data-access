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
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.CsvDatasourceServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class CsvDatasourceServiceServlet extends RemoteServiceServlet implements ICsvDatasourceService {

  private static final long serialVersionUID = 2498165533158485182L;

  public ModelInfo stageFile( String fileName, String delimiter, String enclosure, boolean isFirstRowHeader,
                              String encoding )
    throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.stageFile( fileName, delimiter, enclosure, isFirstRowHeader, encoding );
  }

  public FileInfo[] getStagedFiles() throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.getStagedFiles();
  }

  public List<String> getPreviewRows( String filename, boolean isFirstRowHeader, int rows, String encoding )
    throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.getPreviewRows( filename, isFirstRowHeader, rows, encoding );
  }

  public String getEncoding( String fileName ) {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.getEncoding( fileName );
  }

  @Override
  public FileTransformStats generateDomain( DatasourceDTO datasourceDto ) throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.generateDomain( datasourceDto );
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return pojo;
  }

}

