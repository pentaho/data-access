/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 * 
 * @author Ezequiel Cuellar
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.util.DatabaseUtil;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtJoinSelectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.ConnectionServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.thoughtworks.xstream.XStream;

public class MultitableDatasourceService extends PentahoBase implements IGwtJoinSelectionService {
	
	private DatabaseMeta databaseMeta;
	private ConnectionServiceImpl connectionServiceImpl;
  private Log logger = LogFactory.getLog(MultitableDatasourceService.class);
  private IDataAccessViewPermissionHandler dataAccessViewPermHandler;

	public MultitableDatasourceService() {
		this.connectionServiceImpl = new ConnectionServiceImpl();
    init();
	}

	public MultitableDatasourceService(DatabaseMeta databaseMeta) {
		this.databaseMeta = databaseMeta;
    init();
	}

  protected void init() {
    String dataAccessViewClassName = null;
    try {
      IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
      dataAccessViewClassName = resLoader
          .getPluginSetting(
              getClass(),
              "settings/data-access-view-permission-handler", "org.pentaho.platform.dataaccess.datasource.wizard.service.impl.SimpleDataAccessViewPermissionHandler"); //$NON-NLS-1$ //$NON-NLS-2$
      Class<?> clazz = Class.forName(dataAccessViewClassName);
      Constructor<?> defaultConstructor = clazz.getConstructor(new Class[] {});
      dataAccessViewPermHandler = (IDataAccessViewPermissionHandler) defaultConstructor.newInstance();
    } catch (Exception e) {
      logger.error(
          Messages.getErrorString("DatasourceServiceImpl.ERROR_0030_DATAACCESS_VIEW_PERMISSIONS_INIT_ERROR"), e); //$NON-NLS-1$
      // TODO: Unhardcode once this is an actual plugin
      dataAccessViewPermHandler = new SimpleDataAccessViewPermissionHandler();
    }

  }

	private DatabaseMeta getDatabaseMeta(Connection connection) throws ConnectionServiceException {
		if(this.connectionServiceImpl == null) {
			return this.databaseMeta;
		}
		
		IDatabaseConnection iDatabaseConnection = this.connectionServiceImpl.convertFromConnection(connection);
		iDatabaseConnection.setPassword(ConnectionServiceHelper.getConnectionPassword(connection.getName(), connection.getPassword()));
		return DatabaseUtil.convertToDatabaseMeta(iDatabaseConnection);
  }

	public List<String> getDatabaseTables(Connection connection) throws DatasourceServiceException {
    try{
      DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
      Database database = new Database(null, databaseMeta);
      database.connect();

      String[] tableNames = database.getTablenames();
      List<String> tables = Arrays.asList(tableNames);
      database.disconnect();
      return tables;
    } catch (KettleDatabaseException e) {
      logger.error("Error creating database object", e);
      throw new DatasourceServiceException(e);
    } catch (ConnectionServiceException e) {
      logger.error("Error getting database meta", e);
      throw new DatasourceServiceException(e);
    }
  }

	public IDatasourceSummary serializeJoins(MultiTableDatasourceDTO dto, Connection connection) throws DatasourceServiceException {
    try{
      ModelerService modelerService = new ModelerService();
      modelerService.initKettle();

      DSWDatasourceServiceImpl datasourceService = new DSWDatasourceServiceImpl();
      GeoContext geoContext = datasourceService.getGeoContext();

      DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
      MultiTableModelerSource multiTable = new MultiTableModelerSource(databaseMeta, dto.getSchemaModel(), dto.getDatasourceName(), dto.getSelectedTables(), geoContext);
      Domain domain = multiTable.generateDomain(dto.isDoOlap());
      domain.getLogicalModels().get(0).setProperty("datasourceModel", serializeModelState(dto));
      domain.getLogicalModels().get(0).setProperty("DatasourceType", "MULTI-TABLE-DS");

      // BISERVER-6450 - add security settings to the logical model
      applySecurity(domain.getLogicalModels().get(0));

      modelerService.serializeModels(domain, dto.getDatasourceName(), dto.isDoOlap());

      QueryDatasourceSummary summary = new QueryDatasourceSummary();
      summary.setDomain(domain);
      return summary;
    } catch (Exception e) {
      logger.error("Error serializing joins", e);
      throw new DatasourceServiceException(e);
    }
  }

	private String serializeModelState(MultiTableDatasourceDTO dto) throws DatasourceServiceException {
		XStream xs = new XStream();
		return xs.toXML(dto);
	}

	public MultiTableDatasourceDTO deSerializeModelState(String dtoStr) throws DatasourceServiceException {
		try {
			XStream xs = new XStream();
			return (MultiTableDatasourceDTO) xs.fromXML(dtoStr);
		} catch (Exception e) {
      logger.error(e);
			throw new DatasourceServiceException(e);
		}
	}

	public List<String> getTableFields(String table, Connection connection) throws DatasourceServiceException {
    try{
      DatabaseMeta databaseMeta = this.getDatabaseMeta(connection);
      Database database = new Database(null, databaseMeta);
      database.connect();

      String query = databaseMeta.getSQLQueryFields(table);
      database.getRows(query, 1);
      String[] tableFields = database.getReturnRowMeta().getFieldNames();

      List<String> fields = Arrays.asList(tableFields);
      database.disconnect();
      return fields;
    } catch (KettleDatabaseException e) {
      logger.error(e);
      throw new DatasourceServiceException(e);
    } catch (ConnectionServiceException e) {
      logger.error(e);
      throw new DatasourceServiceException(e);
    }
  }

	public BogoPojo gwtWorkaround(BogoPojo pojo) {
		return pojo;
	}

	@Override
	public Log getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

  protected boolean hasDataAccessViewPermission() {
    return dataAccessViewPermHandler != null
        && dataAccessViewPermHandler.hasDataAccessViewPermission(PentahoSessionHolder.getSession());
  }

  protected List<String> getPermittedRoleList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedRoleList(PentahoSessionHolder.getSession());
  }

  protected List<String> getPermittedUserList() {
    if (dataAccessViewPermHandler == null) {
      return null;
    }
    return dataAccessViewPermHandler.getPermittedUserList(PentahoSessionHolder.getSession());
  }

  protected int getDefaultAcls() {
    if (dataAccessViewPermHandler == null) {
      return -1;
    }
    return dataAccessViewPermHandler.getDefaultAcls(PentahoSessionHolder.getSession());
  }
    
  protected boolean isSecurityEnabled() {
    Boolean securityEnabled = (getPermittedRoleList() != null && getPermittedRoleList().size() > 0)
        || (getPermittedUserList() != null && getPermittedUserList().size() > 0);
    return securityEnabled;
  }

  protected void applySecurity(LogicalModel logicalModel) {
    if (isSecurityEnabled()) {
      Security security = new Security();
      for (String user : getPermittedUserList()) {
        SecurityOwner owner = new SecurityOwner(SecurityOwner.OwnerType.USER, user);
        security.putOwnerRights(owner, getDefaultAcls());
      }
      for (String role : getPermittedRoleList()) {
        SecurityOwner owner = new SecurityOwner(SecurityOwner.OwnerType.ROLE, role);
        security.putOwnerRights(owner, getDefaultAcls());
      }
      logicalModel.setProperty(Concept.SECURITY_PROPERTY, security);
    }
  }

}
