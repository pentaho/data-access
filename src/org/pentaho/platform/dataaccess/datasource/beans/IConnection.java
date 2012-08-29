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
 * @created Aug 17, 2012 
 * @author wseyler
 */


package org.pentaho.platform.dataaccess.datasource.beans;


/**
 * @author wseyler
 *
 */
public interface IConnection {
  public void setId(String id);
  public String getId();
  public void setName(String name);
  public String getName();
  public void setDriverClass(String driverClass);
  public String getDriverClass();
  public void setUsername(String username);
  public String getUsername();
  public void setPassword(String password);
  public String getPassword();
  public void setUrl(String url);
  public String getUrl();
}
