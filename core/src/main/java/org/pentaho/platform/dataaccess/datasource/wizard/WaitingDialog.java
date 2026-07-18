/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/



package org.pentaho.platform.dataaccess.datasource.wizard;

public interface WaitingDialog {
  public String getMessage();

  public String getTitle();

  public void setTitle( String title );

  public void setMessage( String message );

  public void show();

  public void hide();
}
