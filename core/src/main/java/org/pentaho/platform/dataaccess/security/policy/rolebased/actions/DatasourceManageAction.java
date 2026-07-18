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


package org.pentaho.platform.dataaccess.security.policy.rolebased.actions;

import org.pentaho.platform.dataaccess.security.policy.rolebased.actions.messages.Messages;
import org.pentaho.platform.engine.security.authorization.core.AbstractAuthorizationAction;

public class DatasourceManageAction extends AbstractAuthorizationAction {
  public static final String NAME = "org.pentaho.platform.dataaccess.datasource.security.manage";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String getLocalizedDisplayName( String localeString ) {
    return Messages.getInstance().getString( NAME );
  }

  @Override
  public String getLocalizedDescription( String localeString ) {
    return Messages.getInstance().getString( NAME + ".description" );
  }
}
