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



package org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable;

public class JoinError {

  private String error;
  private String title;

  public JoinError( String title, String error ) {
    this.title = title;
    this.error = error;
  }

  public String getError() {
    return error;
  }

  public void setError( String error ) {
    this.error = error;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle( String title ) {
    this.title = title;
  }
}
