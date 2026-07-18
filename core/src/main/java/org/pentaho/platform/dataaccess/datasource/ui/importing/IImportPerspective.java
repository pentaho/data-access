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

public interface IImportPerspective {

  public void onDialogAccept();

  public void onDialogCancel();

  public void showDialog();

  public void genericUploadCallback( String uploadedFile );

  public void concreteUploadCallback( String fileName, String uploadedFile );

  public boolean isValid();
}
