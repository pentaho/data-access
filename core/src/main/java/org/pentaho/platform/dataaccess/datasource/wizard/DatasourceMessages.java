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


package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;

public interface DatasourceMessages {
  public String getString( String key );

  public String getString( String key, String... parameters );

  public ResourceBundle getMessageBundle();

  public void setMessageBundle( ResourceBundle messageBundle );
}
