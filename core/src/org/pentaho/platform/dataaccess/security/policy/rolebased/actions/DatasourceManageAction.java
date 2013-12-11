package org.pentaho.platform.dataaccess.security.policy.rolebased.actions;

import org.pentaho.platform.api.engine.IAuthorizationAction;
import org.pentaho.platform.dataaccess.datasource.wizard.messages.Messages;

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
