package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * Created by IntelliJ IDEA.
 * User: rfellows
 * Date: 9/27/11
 * Time: 10:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class GeoContextSettingsProvider implements GeoContextConfigProvider {
  private String settingsFile;

  public GeoContextSettingsProvider(String settingsFile) {
    this.settingsFile = settingsFile;
  }

  @Override
  public String getDimensionName() throws ModelerException {
    return PentahoSystem.getSystemSetting(settingsFile, "geo/dimension-name", null);
  }

  @Override
  public String getRoles() throws ModelerException {
    return PentahoSystem.getSystemSetting(settingsFile, "geo/roles", null);
  }

  @Override
  public String getRoleAliases(String roleName) throws ModelerException {
    String aliasKey = "geo/" + roleName + "/aliases";
    String aliases = PentahoSystem.getSystemSetting(settingsFile, aliasKey, null);
    if (aliases == null || aliases.trim().length() == 0) {
      throw new ModelerException("Error while building GeoContext: No Aliases found for role  " + roleName + ". Make sure there is a " + aliasKey + " element defined");
    }
    return aliases;
  }

  @Override
  public String getRoleRequirements(String roleName) throws ModelerException {
    String key = "geo/" + roleName + "/required-parents";
    return PentahoSystem.getSystemSetting(settingsFile, key, null);
  }
}
