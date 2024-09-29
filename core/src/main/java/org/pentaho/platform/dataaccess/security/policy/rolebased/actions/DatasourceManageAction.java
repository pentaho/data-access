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

package org.pentaho.platform.dataaccess.security.policy.rolebased.actions;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.dataaccess.security.policy.rolebased.actions.messages.Messages;

public class DatasourceManageAction implements IAuthorizationAction {
  public static final String NAME = "org.pentaho.platform.dataaccess.datasource.security.manage";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getLocalizedDisplayName( String localeString ) {
    return Messages.getInstance().getString( NAME );
  }
}
