/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.database.model.IDatabaseConnection;

public interface ConnectionDialogListener {

  public void onDialogAccept( IDatabaseConnection connection );

  public void onDialogCancel();
}
