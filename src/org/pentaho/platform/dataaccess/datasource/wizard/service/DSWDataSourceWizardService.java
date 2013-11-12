package org.pentaho.platform.dataaccess.datasource.wizard.service;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.dataaccess.datasource.wizard.models.IDSWDataSourceWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IDSWTemplate;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * This class provides web services associated with the DataSource Wizard Dialog.
 * @author tkafalas
 *
 */

@Path( "/data-access-v2/api/dsw-wizard/" )
public class DSWDataSourceWizardService {
  
  @GET
  @Path("/list")
  @Produces({APPLICATION_JSON, APPLICATION_XML})
  public DSWTemplateSummaryListDto getDSWModelList() {
    IDSWDataSourceWizard dsw = PentahoSystem.get(IDSWDataSourceWizard.class);
    List<IDSWTemplate> templates = dsw.getTemplates();
    ArrayList<DSWTemplateSummaryDto> templateNames = new ArrayList<DSWTemplateSummaryDto>();
    for (IDSWTemplate template : templates) {
      templateNames.add(new DSWTemplateSummaryDto(template.getID(), template.getDisplayName(Locale.getDefault())));
    }
    return new DSWTemplateSummaryListDto(templateNames);
  }
}
