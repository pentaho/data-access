/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.dataaccess.datasource.wizard.models.IDSWDataSourceModel;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@Path( "/data-access-v2/api/dswmodel/" )
public class DSWDataSourceModelService {
  
  @GET
  @Path("/list")
  @Produces({APPLICATION_JSON, APPLICATION_XML})  
  public DSWDataSourceModelSummaryListDto getDSWModelList() {
    List<IDSWDataSourceModel> models = PentahoSystem.getAll(IDSWDataSourceModel.class);
    ArrayList<DSWDataSourceModelSummaryDto> modelNames = new ArrayList<DSWDataSourceModelSummaryDto>();
    for (IDSWDataSourceModel model : models) {
      modelNames.add(new DSWDataSourceModelSummaryDto(model.getModelName(), model.getDisplayName()));
    }
    return new DSWDataSourceModelSummaryListDto(modelNames);
  }
}
