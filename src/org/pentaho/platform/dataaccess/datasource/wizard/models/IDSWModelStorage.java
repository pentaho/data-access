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

import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;

/**
 * This interface provides the api for the methods save a seialized model, or, load the serialized model from the
 * repository and using the <code>IDSWTemplateModel</code>, de-serialized it back into an <code>IDSWTemplateModel</code>
 * .
 * 
 * @author tkafalas
 * 
 */
public interface IDSWModelStorage {
  /**
   * Persist the serilizedModel provided for the given <code>IDSWDataSource</code>
   * 
   * @param serializedModel
   *          The model to be written to the repository.
   * @param iDSWDataSource
   *          The DSWDatasource associated with the model.
   * @throws DSWException
   *           thrown if the method fails.
   */
  void storeModel( String serializedModel, IDSWDataSource iDSWDataSource ) throws DSWException;

  /**
   * Read the serializedModel in from the repository and call the models IDSWTemplate to de-serialize it into an
   * <code>IDSWTemplate</code>.
   * 
   * @param dataSourceID
   * @return The <code>IDSWTemplateModel</code> object representing the state of the UI.
   * @throws DSWException
   */
  IDSWTemplateModel loadModel( String dataSourceID ) throws DSWException;
}
