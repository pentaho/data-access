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

package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class ParameterDialogModel extends XulEventSourceAdapter {

  private String name;
  private String value;

  public ParameterDialogModel() {
  }

  public ParameterDialogModel( String name, String value ) {
    this.name = name;
    this.value = value;
  }

  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public void setName( String name ) {
    this.name = name;
  }

  @Bindable
  public String getValue() {
    return value;
  }

  @Bindable
  public void setValue( String value ) {
    this.value = value;
  }
}
