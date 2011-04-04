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

import java.util.List;

import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ICsvDatasourceServiceAsync {

  public void stageFile(String name, String delimiter, String enclosure, boolean isFirstRowHeader, String encoding,
      AsyncCallback<ModelInfo> aCallback);

  public void getStagedFiles(AsyncCallback<FileInfo[]> aCallback);

  public void generateDomain(DatasourceDTO datasourceDto, AsyncCallback<IDatasourceSummary> callback);

  public void getPreviewRows(String filename, boolean isFirstRowHeader, int rows, String encoding, AsyncCallback<List<String>> callback) throws Exception;
  
  public void getEncoding(String fileName, AsyncCallback<String> callback);

  public void gwtWorkaround(BogoPojo pojo, AsyncCallback<BogoPojo> callback);

}
