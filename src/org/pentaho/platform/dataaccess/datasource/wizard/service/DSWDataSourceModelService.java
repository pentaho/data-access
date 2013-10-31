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
