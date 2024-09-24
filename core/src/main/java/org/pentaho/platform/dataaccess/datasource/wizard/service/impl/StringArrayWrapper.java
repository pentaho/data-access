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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author wseyler
 */
@XmlRootElement
public class StringArrayWrapper {
  public StringArrayWrapper() {
    super();
    // TODO Auto-generated constructor stub
  }

  public String[] items;

  public String[] getItems() {
    return items;
  }

  public void setArray( String[] items ) {
    this.items = items;
  }

}
