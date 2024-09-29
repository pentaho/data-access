/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.List;

import org.pentaho.metadata.model.Domain;

public class BusinessData implements java.io.Serializable {
  private static final long serialVersionUID = 8275330793662889379L;
  private Domain domain; // contains column names
  private List<List<String>> data; // contains sample data

  public BusinessData( Domain domain, List<List<String>> data ) {
    super();
    this.domain = domain;
    this.data = data;
  }

  public BusinessData() {

  }

  public Domain getDomain() {
    return domain;
  }

  public void setDomain( Domain domain ) {
    this.domain = domain;
  }

  public List<List<String>> getData() {
    return data;
  }

  public void setData( List<List<String>> data ) {
    this.data = data;
  }
}
