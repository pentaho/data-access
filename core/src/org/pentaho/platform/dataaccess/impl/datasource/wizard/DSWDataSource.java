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

package org.pentaho.platform.dataaccess.impl.datasource.wizard;

import org.pentaho.platform.dataaccess.api.datasource.wizard.IDSWDataSource;
import org.pentaho.platform.dataaccess.api.datasource.wizard.IDSWTemplate;
import org.pentaho.platform.dataaccess.api.datasource.wizard.IDSWTemplateModel;

/**
 * This class represents the server support needed to drive the UI for editing the datasource. The
 * <code>IDSWTemplate</code> is responsible for serializing/de-serializing the state of the UI for the Datasource. This
 * state is stored in the <code>IDSWTemplateModel</code>.
 * 
 * @author tkafalas
 * 
 */
public class DSWDataSource implements IDSWDataSource {
  private String name;
  private IDSWTemplate iDSWTemplate;
  private IDSWTemplateModel iDSWTemplateModel;

  public DSWDataSource( String name, IDSWTemplate iDSWTemplate, IDSWTemplateModel iDSWTemplateModel ) {
    this.name = name;
    this.iDSWTemplate = iDSWTemplate;
    this.iDSWTemplateModel = iDSWTemplateModel;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public IDSWTemplate getTemplate() {
    return iDSWTemplate;
  }

  @Override
  public IDSWTemplateModel getModel() {
    return iDSWTemplateModel;
  }

}
