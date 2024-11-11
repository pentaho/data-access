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


package org.pentaho.platform.dataaccess.datasource.wizard.models;

public interface IModelInfoValidationListener {

  public void onCsvValid();

  public void onCsvInValid();

  public void onModelInfoValid();

  public void onModelInfoInvalid();

}
