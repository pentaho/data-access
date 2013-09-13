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

import java.util.List;

import org.pentaho.metadata.model.Domain;

public class BusinessData implements java.io.Serializable{
  private static final long serialVersionUID = 8275330793662889379L;
  private Domain domain;// contains column names
  private List<List<String>> data; // contains sample data
  public BusinessData(Domain domain, List<List<String>> data) {
    super();
    this.domain = domain;
    this.data = data;
  }
  public BusinessData()
  {
    
  }
  public Domain getDomain() {
    return domain;
  }
  public void setDomain(Domain domain) {
    this.domain = domain;
  }

  public List<List<String>> getData() {
    return data;
  }
  public void setData(List<List<String>> data) {
    this.data = data;
  }
}
