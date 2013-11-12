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

/**
 * This interface provides the api for the server support needed to drive the UI for editing the datasource. The
 * <code>IDSWTemplate</code> is responsible for serializing/de-serializing the state of the UI for the Datasource. This
 * state is stored in the <code>IDSWTemplateModel</code>.
 * 
 * @author tkafalas
 * 
 */
public interface IDSWDataSource {
  /**
   * 
   * @return The formal name of the DataSource
   */
  String getName();

  /**
   * 
   * @return The template which allows serialization/de-serailization of the UI state as well as providing a base model
   *         suitable adding a new datasource.
   */
  IDSWTemplate getTemplate();

  /**
   * 
   * @return The state of the UI, if it exists, or null if it is not defined.
   */
  IDSWTemplateModel getModel();
}
