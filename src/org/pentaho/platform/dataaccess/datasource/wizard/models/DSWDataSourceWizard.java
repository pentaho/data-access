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

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;

/**
 * This is a singleton class that provides various convenience services for DSWDataSource
 * 
 * @author tkafalas
 * 
 */
public class DSWDataSourceWizard implements IDSWDataSourceWizard {
  private Map<String, IDSWTemplate> templateList = Collections.synchronizedMap( new HashMap<String, IDSWTemplate>() );

  /**
   * @param templateList
   *          the templateList to set
   */
  public void setTemplates( List<IDSWTemplate> templates ) {
    templateList.clear();
    for ( IDSWTemplate template : templates ) {
      templateList.put( template.getID(), template );
    }
  }

  @Override
  public List<IDSWTemplate> getTemplates() {
    ArrayList<IDSWTemplate> tempList = new ArrayList<IDSWTemplate>();
    tempList.addAll( templateList.values() );
    return tempList;
  }

  @Override
  public IDSWTemplate getTemplateByID( String templateID ) {
    // This doesn't work yet with unit test -- PentahoSystem.get(IDSWTemplate.class, PentahoSessionHolder.getSession(),
    // Collections.singletonMap("DSWTemplateType", templateId));
    return templateList.get( templateID );
  }

  @Override
  public IDSWTemplate getTemplateByDatasource( IDSWDataSource dswDataSource ) {
    return dswDataSource.getTemplate();
  }

  @Override
  public void storeDataSource( IDSWDataSource iDSWDataSource, boolean overwrite ) throws DSWException {
    iDSWDataSource.getTemplate().createDatasource( iDSWDataSource, overwrite );
    IDSWModelStorage iDSWModelStorage = new DSWModelStorage();
    String serializedModel = iDSWDataSource.getTemplate().serialize( iDSWDataSource.getModel() );
    iDSWModelStorage.storeModel( serializedModel, iDSWDataSource );
  }

  @Override
  public IDSWDataSource loadDataSource( String dataSourceID ) throws DSWException {
    IDSWModelStorage dswModelStorage = new DSWModelStorage();
    IDSWTemplateModel templateModel = dswModelStorage.loadModel( dataSourceID );
    IDSWTemplate template = getTemplateByID( templateModel.getTemplateID() );
    return new DSWDataSource( dataSourceID, template, templateModel );
  }

}
