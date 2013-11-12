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

import java.util.Locale;
import java.util.MissingResourceException;

import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;
import org.pentaho.platform.dataaccess.datasource.wizard.messages.Messages;

/**
 * Abstract Implementation of the IDSWTemplate. Implements getters/setters for basic fields. Does not implement creation
 * of the datasource or serialization/deserialization of the templateModel.
 * 
 * @author tkafalas
 * 
 */
public abstract class AbstractDSWTemplate implements IDSWTemplate {

  private String id;
  private String displayName; // default display name, used in testing

  public AbstractDSWTemplate( String id, String displayName ) {
    this.id = id;
    this.displayName = displayName;
  }

  @Override
  public String getID() {
    return id;
  }

  @Override
  public String getDisplayName( Locale locale ) {
    // TODO: Other Messages class was not loading the resource so switched to old version
    String localizedString = Messages.getInstance().getString( "TEMPLATE_NAME_" + id );
    if ( displayName != null && ( "!TEMPLATE_NAME_" + id + "!" ).equals( localizedString ) ) {
      return displayName;
    } else {
      return localizedString;
    }
  }

  // ===================== Should be overridden ==========

  @Override
  public abstract void createDatasource( IDSWDataSource iDSWDataSource, boolean overwrite ) throws DSWException;

  @Override
  public abstract IDSWTemplateModel deserialize( String serializedModel ) throws DSWException;

  @Override
  public abstract String serialize( IDSWTemplateModel dswTemplateModel ) throws DSWException;

}
