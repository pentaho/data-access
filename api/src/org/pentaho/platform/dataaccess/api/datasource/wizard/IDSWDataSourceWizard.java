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

package org.pentaho.platform.dataaccess.api.datasource.wizard;

import java.util.List;

/**
 * Intended as a singleton, this class provides various convenience services for DSWDataSource
 * 
 * @author tkafalas
 * 
 */
public interface IDSWDataSourceWizard {
  /**
   * 
   * @return a list of all <code>ISDWTemplate</code> implementations registered with this class.
   */
  List<IDSWTemplate> getTemplates();

  /**
   * 
   * @param templateID
   *          The unique ID name of the template.
   * @return the template with an ID matching templateID.
   */
  IDSWTemplate getTemplateByID( String templateID );

  /**
   * 
   * @param dswDataSource
   * @return the <code>IDSWTemplate</code> associated with the given the <code>IDSWDataSource</code>
   */
  IDSWTemplate getTemplateByDatasource( IDSWDataSource dswDataSource );

  /**
   * Persists the datasource to the repository.
   * 
   * @param dswDataSource
   *          The <code>IDSWDataSource</code> to persist.
   * @param overwrite
   *          true allows overwrite of an existing data. If false, then an attempt to overwrite an existing datasource
   *          will generate a <code>DSWExistingFileException</code>.
   * @throws DSWException
   */
  void storeDataSource( IDSWDataSource dswDataSource, boolean overwrite ) throws DSWException;

  /**
   * 
   * @param dataSourceId
   *          The name assigned to the datasource
   * @return The IDSWDataSource that is persisted in the repository.
   * @throws DSWException
   *           If the load fails
   */
  IDSWDataSource loadDataSource( String dataSourceId ) throws DSWException;
}
