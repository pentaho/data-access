/**
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
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 *
 */

package org.pentaho.platform.dataaccess.catalog.impl;

import org.pentaho.platform.dataaccess.catalog.api.IDatasourceType;
import org.pentaho.platform.dataaccess.datasource.provider.messages.Messages;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * An implementation of 'IDatasourceType' that defines a datasource type and provides for retrieving a localized name for
 * that type.
 * 
 * @author wseyler
 */
@XmlRootElement
public class DatasourceType implements IDatasourceType {

  String id;
  String displayName;
  ResourceBundle resourceBundle;

  public DatasourceType() {
    super();
  }

  /**
   * 
   * @param id
   */
  public DatasourceType( String id ) {
    this();
    this.id = id;
  }

  
  /**
   * 
   * @param id
   * @param displayName
   */
  public DatasourceType( String id, String displayName ) {
    this();
    this.id = id;
    this.displayName = displayName;
  }

  /**
   * 
   * @return
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * 
   * @param locale
   * @return
   */
  @Override
  public String getDisplayName( Locale locale ) {
    resourceBundle = Messages.getInstance().getBundle(locale);
    return resourceBundle.getString( id );
  }

  /**
   * 
   * @param id
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * 
   * @param displayName
   * @return
   */
  public void setDisplayName( String displayName ) {
    this.displayName = displayName;
  }

}
