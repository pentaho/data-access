/*
 * Copyright 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
 * BI Platform.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 *
 * @created Dec 7, 2012 
 * @author wseyler
 */


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author wseyler
 *
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

  public void setArray(String[] items) {
    this.items = items;
  }
  
}
