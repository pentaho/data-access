package org.pentaho.platform.dataaccess.datasource.utils;

import java.util.Collections;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.IDataAccessPermissionHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.IDataAccessViewPermissionHandler;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class DataAccessPermissionUtil {

  public static IDataAccessPermissionHandler getDataAccessPermissionHandler() {
    return PentahoSystem.get( IDataAccessPermissionHandler.class, /* session */null, Collections.singletonMap( "id",
      "dataAccessPermissionHandler" ) );
  }

  public static IDataAccessViewPermissionHandler getDataAccessViewPermissionHandler() {
    return PentahoSystem.get( IDataAccessViewPermissionHandler.class, /* session */null, Collections.singletonMap(
      "id", "dataAccessViewPermissionHandler" ) );
  }

  public static boolean hasViewAccess() {
    return getDataAccessViewPermissionHandler().hasDataAccessViewPermission( PentahoSessionHolder.getSession() );
  }

  public static boolean hasManageAccess() {
    return getDataAccessPermissionHandler().hasDataAccessPermission( PentahoSessionHolder.getSession() );
  }

  public static List<String> getPermittedViewUserList() {
    return getDataAccessViewPermissionHandler().getPermittedUserList( PentahoSessionHolder.getSession() );
  }

  public static List<String> getPermittedViewRoleList() {
    return getDataAccessViewPermissionHandler().getPermittedRoleList( PentahoSessionHolder.getSession() );
  }
}
