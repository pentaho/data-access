/*
 * This program is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this 
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
 * or from the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created June, 2010
 * @author Ezequiel Cuellar
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.CsvDatasourceServiceImpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import javax.servlet.http.HttpServletRequest;

public class CsvDatasourceServiceServlet extends RemoteServiceServlet implements ICsvDatasourceService {

  private static final long serialVersionUID = 2498165533158485182L;

  public ModelInfo stageFile(String fileName, String delimiter, String enclosure, boolean isFirstRowHeader, String encoding)
      throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.stageFile(fileName, delimiter, enclosure, isFirstRowHeader, encoding);
  }

  public FileInfo[] getStagedFiles() throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.getStagedFiles();
  }

  public List<String> getPreviewRows(String filename, boolean isFirstRowHeader, int rows, String encoding) throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.getPreviewRows(filename, isFirstRowHeader, rows, encoding);
  }
  
  public String getEncoding(String fileName) {
	  CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
	  return serviceImpl.getEncoding(fileName);
  }

  @Override
  public FileTransformStats generateDomain(ModelInfo modelInfo) throws Exception {
	  CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
	  return serviceImpl.generateDomain(modelInfo);
  }

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

}

