/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.beans;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

@XmlRootElement
public class Connection extends XulEventSourceAdapter implements IConnection, Serializable {

  private static final long serialVersionUID = 5825649640767205332L;
  private String id;
  private String name;
  private String driverClass;
  private String username;
  private String password;
  private String url;

  public Connection() {}
  
   @Bindable
  public void setId(String id) {
      this.id = id;
  }

    @Bindable
  public String getId() {
      return id;
  }
    

  @Bindable
  public void setName(String name) {
    this.name = name;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public void setDriverClass(String driverClass) {
    this.driverClass = driverClass;
  }

  @Bindable
  public String getDriverClass() {
    return driverClass;
  }

  @Bindable
  public void setUsername(String username) {
    this.username = username;
  }

  @Bindable
  public String getUsername() {
    return username;
  }

  @Bindable
  public void setPassword(String password) {
    this.password = password;
  }

  @Bindable
  public String getPassword() {
    return password;
  }

  @Bindable
  public void setUrl(String url) {
    this.url = url;
  }

  @Bindable
  public String getUrl() {
    return url;
  }

}
