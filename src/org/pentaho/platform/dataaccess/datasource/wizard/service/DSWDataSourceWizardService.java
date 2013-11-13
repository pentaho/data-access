/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWDataSourceWizard;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWTemplate;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * This class provides web services associated with the DataSource Wizard Dialog.
 * 
 * @author tkafalas
 * 
 */

@Path( "/data-access-v2/api/dsw-wizard/" )
public class DSWDataSourceWizardService {

  /**
   * Queries the system for a list of registered datasource templates.
   * 
   * @return A "templateList" that wraps a list of 'DSWTemplateSummaryDto'.
   */
  @GET
  @Path( "/list" )
  @Produces( { APPLICATION_JSON, APPLICATION_XML } )
  public DSWTemplateSummaryListDto getDSWModelList() {
    IDSWDataSourceWizard dsw = PentahoSystem.get( IDSWDataSourceWizard.class );
    List<IDSWTemplate> templates = dsw.getTemplates();
    ArrayList<DSWTemplateSummaryDto> templateNames = new ArrayList<DSWTemplateSummaryDto>();
    for ( IDSWTemplate template : templates ) {
      templateNames.add( new DSWTemplateSummaryDto( template.getID(), template.getDisplayName( Locale.getDefault() ) ) );
    }
    return new DSWTemplateSummaryListDto( templateNames );
  }
}
