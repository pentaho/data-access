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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.service.IDatabaseDialectService;
import org.pentaho.database.util.DatabaseTypeHelper;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;

public class AgileMartDatasourceHelper {

  private IPluginResourceLoader resLoader;
  private static final String AGILEMART_STAGING_PROPERTIES_FILE = "agile_mart_datasource.properties"; //$NON-NLS-1$

  public static final String NAME = "name";
  public static final String DEFAULT_NAME_VALUE = "AgileBI";
  public static final String ACCESS_TYPE = "accessType";
  public static final String DEFAULT_ACCESS_TYPE_VALUE = "NATIVE";
  public static final String CHANGED = "changed";
  public static final String DEFAULT_CHANGED_VALUE = "NATIVE";
  public static final String DATABASENAME = "databaseName";
  public static final String DEFAULT_DATABASENAME_VALUE = "pentaho-instaview";
  public static final String DATABASEPORT = "databasePort";
  public static final String DEFAULT_DATABASEPORT_VALUE = "50000";
  public static final String DEFAULTDATABASEPORT = "databaseType.defaultDatabasePort";
  public static final String DEFAULT_DEFAULTDATABASEPORT_VALUE = "50000";
  public static final String DATABASETYPENAME = "databaseType.name";
  public static final String DEFAULT_DATABASETYPENAME_VALUE = "monetdb";
  public static final String DATABASETYPESHORTNAME = "databaseType.shortName";
  public static final String DEFAULT_DATABASETYPESHORTNAME_VALUE = "MONETDB";
  public static final String FORCINGIDENTIFIERSTOLOWERCASE = "forcingIdentifiersToLowerCase";
  public static final String DEFAULT_FORCINGIDENTIFIERSTOLOWERCASE_VALUE = "false";
  public static final String FORCINGIDENTIFIERSTOUPPERCASE = "forcingIdentifiersToUpperCase";
  public static final String DEFAULT_FORCINGIDENTIFIERSTOUPPERCASE_VALUE = "false";
  public static final String HOSTNAME = "hostname";
  public static final String DEFAULT_HOSTNAME_VALUE = "localhost";
  public static final String USERNAME = "username";
  public static final String DEFAULT_USERNAME_VALUE = "monetdb";
  public static final String PASSWORD = "password";
  public static final String DEFAULT_PASSWORD_VALUE = "monetdb";
  public static final String INITIALPOOLSIZE = "initialPoolSize";
  public static final String DEFAULT_INITIALPOOLSIZE_VALUE = "0";
  public static final String MAXIMUMPOOLSIZE = "maximumPoolSize";
  public static final String DEFAULT_MAXIMUMPOOLSIZE_VALUE = "0";
  public static final String PARTITIONED = "partitioned";
  public static final String DEFAULT_PARTITIONED_VALUE = "false";
  public static final String QUOTEALLFIELDS = "quoteAllFields";
  public static final String DEFAULT_QUOTEALLFIELDS_VALUE = "false";
  public static final String STREAMINGRESULTS = "streamingResults";
  public static final String DEFAULT_STREAMINGRESULTS_VALUE = "false";
  public static final String USECONNECTIONPOOL = "usingConnectionPool";
  public static final String DEFAULT_USECONNECTIONPOOL_VALUE = "false";
  public static final String USINGDOUBLEDECIMALASSCHEMATABLESEPERATOR = "usingDoubleDecimalAsSchemaTableSeparator";
  public static final String DEFAULT_USINGDOUBLEDECIMALASSCHEMATABLESEPERATOR_VALUE = "false";
  private static final String ATTRIBUTE_PORT_NUMBER = "PORT_NUMBER";
  public static final String ATTRIBUTE_STANDARD_CONNECTION = "STANDARD_CONNECTION";
  public static final String ATTRIBUTE_AGILE_MART_CONNECTION = "AGILE_MART_CONNECTION";

  public AgileMartDatasourceHelper( final IPluginResourceLoader resLoader ) {
    this.resLoader = resLoader;
  }

  public IDatabaseConnection getAgileMartDatasource() {
    InputStream inputStream =
      resLoader.getResourceAsStream( AgileMartDatasourceLifecycleManager.class, AGILEMART_STAGING_PROPERTIES_FILE );
    Properties agileMartDatasourceProperties = new Properties();
    try {
      agileMartDatasourceProperties.load( inputStream );
      return getAgileMartDatasource( false, agileMartDatasourceProperties );
    } catch ( IOException e ) {
      return getAgileMartDatasource( true, agileMartDatasourceProperties );
    }

  }

  private IDatabaseConnection getAgileMartDatasource( Boolean useDefault, Properties agileMartDatasourceProperties ) {
    IDatabaseConnection databaseConnection = new DatabaseConnection();

    IDatabaseDialectService databaseDialectService = PentahoSystem.get( IDatabaseDialectService.class );
    DatabaseTypeHelper databaseTypeHelper = new DatabaseTypeHelper( databaseDialectService.getDatabaseTypes() );

    databaseConnection.setDatabaseType( databaseTypeHelper.getDatabaseTypeByShortName( useDefault
      ? agileMartDatasourceProperties.getProperty( DEFAULT_DATABASETYPESHORTNAME_VALUE )
      : agileMartDatasourceProperties.getProperty( DATABASETYPESHORTNAME ) ) );

    String accessType =
      useDefault ? agileMartDatasourceProperties.getProperty( DEFAULT_ACCESS_TYPE_VALUE )
        : agileMartDatasourceProperties.getProperty( ACCESS_TYPE );

    // This is a special case with some PDI connections
    if ( accessType != null && accessType.contains( "Native" ) || accessType.equals( "NATIVE" ) ) {
      accessType = DatabaseAccessType.NATIVE.getName();
    } else if ( accessType != null && accessType.equals( ", " ) ) {
      accessType = DatabaseAccessType.JNDI.getName();
    }

    databaseConnection.setAccessType( accessType != null
      ? DatabaseAccessType.getAccessTypeByName( accessType ) : null );

    databaseConnection.setName( useDefault ? agileMartDatasourceProperties.getProperty( DEFAULT_NAME_VALUE )
      : agileMartDatasourceProperties.getProperty( NAME ) );
    databaseConnection.setChanged( Boolean.getBoolean( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_CHANGED_VALUE ) : agileMartDatasourceProperties.getProperty( CHANGED ) ) );
    databaseConnection.setDatabaseName( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_DATABASENAME_VALUE ) : agileMartDatasourceProperties.getProperty( DATABASENAME ) );
    databaseConnection.setDatabasePort( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_DATABASEPORT_VALUE ) : agileMartDatasourceProperties.getProperty( DATABASEPORT ) );
    databaseConnection.setForcingIdentifiersToLowerCase( Boolean.getBoolean( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_FORCINGIDENTIFIERSTOLOWERCASE_VALUE ) : agileMartDatasourceProperties
      .getProperty( FORCINGIDENTIFIERSTOLOWERCASE ) ) );
    databaseConnection.setForcingIdentifiersToUpperCase( Boolean.getBoolean( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_FORCINGIDENTIFIERSTOUPPERCASE_VALUE ) : agileMartDatasourceProperties
      .getProperty( FORCINGIDENTIFIERSTOUPPERCASE ) ) );
    databaseConnection.setHostname( useDefault ? agileMartDatasourceProperties.getProperty( DEFAULT_HOSTNAME_VALUE )
      : agileMartDatasourceProperties.getProperty( HOSTNAME ) );
    databaseConnection.setInitialPoolSize( Integer.valueOf( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_INITIALPOOLSIZE_VALUE )
      : agileMartDatasourceProperties.getProperty( INITIALPOOLSIZE ) ) );
    databaseConnection.setMaximumPoolSize( Integer.valueOf( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_MAXIMUMPOOLSIZE_VALUE )
      : agileMartDatasourceProperties.getProperty( MAXIMUMPOOLSIZE ) ) );
    databaseConnection.setPartitioned( Boolean.getBoolean( useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_PARTITIONED_VALUE ) : agileMartDatasourceProperties.getProperty( PARTITIONED ) ) );
    databaseConnection.setPassword( useDefault ? agileMartDatasourceProperties.getProperty( DEFAULT_PASSWORD_VALUE )
      : agileMartDatasourceProperties.getProperty( PASSWORD ) );
    databaseConnection.setUsername( useDefault ? agileMartDatasourceProperties.getProperty( DEFAULT_USERNAME_VALUE )
      : agileMartDatasourceProperties.getProperty( USERNAME ) );
    databaseConnection.setUsingConnectionPool( Boolean
      .getBoolean( useDefault ? agileMartDatasourceProperties.getProperty( DEFAULT_USECONNECTIONPOOL_VALUE )
        : agileMartDatasourceProperties.getProperty( USECONNECTIONPOOL ) ) );
    databaseConnection.setUsingDoubleDecimalAsSchemaTableSeparator( Boolean.getBoolean( useDefault
      ? agileMartDatasourceProperties.getProperty( DEFAULT_USINGDOUBLEDECIMALASSCHEMATABLESEPERATOR_VALUE )
      : agileMartDatasourceProperties.getProperty( USINGDOUBLEDECIMALASSCHEMATABLESEPERATOR ) ) );

    Map<String, String> attributes = new HashMap<String, String>();
    attributes.put( ATTRIBUTE_PORT_NUMBER, useDefault ? agileMartDatasourceProperties
      .getProperty( DEFAULT_DATABASEPORT_VALUE ) : agileMartDatasourceProperties.getProperty( DATABASEPORT ) );

    attributes.put( ATTRIBUTE_STANDARD_CONNECTION, Boolean.FALSE.toString() );
    attributes.put( ATTRIBUTE_AGILE_MART_CONNECTION, Boolean.TRUE.toString() );

    databaseConnection.setAttributes( attributes );

    return databaseConnection;
  }
}
