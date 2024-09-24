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

public class GwtDatasourceMessages implements DatasourceMessages {
  ResourceBundle messageBundle;

  public GwtDatasourceMessages() {

  }

  public GwtDatasourceMessages( ResourceBundle messageBundle ) {
    this.messageBundle = messageBundle;
  }

  public String getString( String key ) {
    if ( this.messageBundle == null ) {
      return key;
    }
    return this.messageBundle.getString( key );
  }

  public String getString( String key, String... parameters ) {
    if ( this.messageBundle == null ) {
      return key;
    }
    return this.messageBundle.getString( key, key, parameters );
  }

  public ResourceBundle getMessageBundle() {
    return this.messageBundle;
  }

  public void setMessageBundle( ResourceBundle messageBundle ) {
    this.messageBundle = messageBundle;
  }
}
