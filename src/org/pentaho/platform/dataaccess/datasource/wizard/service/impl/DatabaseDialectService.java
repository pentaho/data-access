/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.dialect.DB2DatabaseDialect;
import org.pentaho.database.dialect.GenericDatabaseDialect;
import org.pentaho.database.dialect.H2DatabaseDialect;
import org.pentaho.database.dialect.HiveDatabaseDialect;
import org.pentaho.database.dialect.HypersonicDatabaseDialect;
import org.pentaho.database.dialect.ImpalaDatabaseDialect;
import org.pentaho.database.dialect.MSSQLServerDatabaseDialect;
import org.pentaho.database.dialect.MSSQLServerNativeDatabaseDialect;
import org.pentaho.database.dialect.MonetDatabaseDialect;
import org.pentaho.database.dialect.MySQLDatabaseDialect;
import org.pentaho.database.dialect.OracleDatabaseDialect;
import org.pentaho.database.dialect.PostgreSQLDatabaseDialect;
import org.pentaho.database.dialect.VerticaDatabaseDialect;
import org.pentaho.database.dialect.Vertica5DatabaseDialect;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.ui.database.event.DefaultDatabaseDialectList;
import org.pentaho.ui.database.event.DefaultDatabaseTypesList;
import org.pentaho.ui.database.event.IDatabaseDialectList;
import org.pentaho.ui.database.event.IDatabaseTypesList;

@Path("/data-access/api/dialect")
public class DatabaseDialectService {
  
  private static final Log logger = LogFactory.getLog(DatabaseDialectService.class);
  
  List<IDatabaseDialect> databaseDialects = new ArrayList<IDatabaseDialect>();
  List<IDatabaseType> databaseTypes = new ArrayList<IDatabaseType>();
  Map<IDatabaseType, IDatabaseDialect> typeToDialectMap = new HashMap<IDatabaseType, IDatabaseDialect>();
  GenericDatabaseDialect genericDialect = new GenericDatabaseDialect();

  
  public DatabaseDialectService() {
    this(true);
  }

  public DatabaseDialectService(boolean validateClasses) {
    // temporary until we have a better approach
    registerDatabaseDialect(new OracleDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new MySQLDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new HiveDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new HypersonicDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new ImpalaDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new MSSQLServerDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new MSSQLServerNativeDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new DB2DatabaseDialect(), validateClasses);
    registerDatabaseDialect(new PostgreSQLDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new H2DatabaseDialect(), validateClasses);
    registerDatabaseDialect(new MonetDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new VerticaDatabaseDialect(), validateClasses);
    registerDatabaseDialect(new Vertica5DatabaseDialect(), validateClasses);
    // the generic service is special, because it plays a role
    // in generation from a URL and Driver
    registerDatabaseDialect(genericDialect, validateClasses);
  }

  @POST
  @Path("/registerDatabaseDialect")
  @Consumes({APPLICATION_JSON})
  public Response registerDatabaseDialect(IDatabaseDialect databaseDialect) {
    return registerDatabaseDialect(databaseDialect, true);
  }
  
  /**
   * 
   * @param databaseDialect
   * @param validateClassExists
   */
  @POST
  @Path("/registerDatabaseDialectWithValidation/{validateClassExists}")
  @Consumes({APPLICATION_JSON})
  public Response registerDatabaseDialect(IDatabaseDialect databaseDialect, @PathParam("validateClassExists") Boolean validateClassExists) {
    try {
      if (!validateClassExists || validateJdbcDriverClassExists(databaseDialect.getNativeDriver())) {
        databaseTypes.add(databaseDialect.getDatabaseType());
        typeToDialectMap.put(databaseDialect.getDatabaseType(), databaseDialect);
        databaseDialects.add(databaseDialect);
      }
    } catch (Throwable e) {
      Response.serverError().entity(e).build();
    }
    return Response.ok().build();
  }
  
  /**
   * Attempt to load the JDBC Driver class. If it's not available, return false.
   * 
   * @param classname validate that this classname exists in the classpath
   * 
   * @return true if the class exists
   */
  @POST
  @Path("/validateJdbcDriverClassExists")
  @Consumes({APPLICATION_JSON})
  @Produces({APPLICATION_JSON})
  public Boolean validateJdbcDriverClassExists(String classname) {
    // no need to test if the class exists if it is null
    if (classname == null) {
      return true;
    }
    
    try {
      Class.forName(classname);
      return true;
    } catch(NoClassDefFoundError e) { 
      if (logger.isDebugEnabled()) {
        logger.debug("classExists returning false", e);
      }
    } catch(ClassNotFoundException e) {
      if (logger.isDebugEnabled()) {
        logger.debug("classExists returning false", e);
      }
    } catch(Exception e) { 
      if (logger.isDebugEnabled()) {
        logger.debug("classExists returning false", e);
      }
    }
    // if we've made it here, an exception has occurred.
    return false;
  }
 
  @GET
  @Path("/getDatabaseTypes")
  @Produces({APPLICATION_JSON})
  public IDatabaseTypesList getDatabaseTypes() {
    DefaultDatabaseTypesList value = new DefaultDatabaseTypesList();
    value.setDbTypes(databaseTypes);
    return value;
  }
  
  @POST
  @Path("/getDialectByType")
  @Consumes({APPLICATION_JSON})
  @Produces({APPLICATION_JSON})
  public IDatabaseDialect getDialect(DatabaseType databaseType) {
    return typeToDialectMap.get(databaseType);
  }
 
  @POST
  @Path("/getDialectByConnection")
  @Consumes({APPLICATION_JSON})
  @Produces({APPLICATION_JSON})
  public IDatabaseDialect getDialect(IDatabaseConnection connection) {
    return typeToDialectMap.get(connection.getDatabaseType());
  }
  
  @GET
  @Path("/getDatabaseDialects")
  @Produces({APPLICATION_JSON})
  public IDatabaseDialectList getDatabaseDialects() {
    IDatabaseDialectList value = new DefaultDatabaseDialectList();
    value.setDialects(databaseDialects);
    return value;
  }
}
