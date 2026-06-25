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



package org.pentaho.platform.dataaccess.datasource.ui.importing;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class LocalizedBundleDialogModel extends XulEventSourceAdapter {

  private String fileName;
  private String uploadedFile;

  public LocalizedBundleDialogModel() {
  }

  public LocalizedBundleDialogModel( String fileName, String uploadedFile ) {
    this.fileName = fileName;
    this.uploadedFile = uploadedFile;
  }

  @Bindable
  public String getFileName() {
    return fileName;
  }

  @Bindable
  public void setFileName( String fileName ) {
    this.fileName = fileName;
  }

  public String getUploadedFile() {
    return uploadedFile;
  }

  public void setUploadedFile( String uploadedFile ) {
    this.uploadedFile = uploadedFile;
  }
}
