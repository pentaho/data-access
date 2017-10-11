/*
* Copyright 2002 - 2017 Hitachi Vantara.  All rights reserved.
* 
* This software was developed by Hitachi Vantara and is provided under the terms
* of the Mozilla Public License, Version 1.1, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;

/**
 * @author wseyler
 */
public class AutobeanUtilities {
  /**
   * @param connectionBean - IDatabaseConnection backed by a Autobean implementation
   * @return an IDatabaseConnection that is backed by a concrete DatabaseConnection
   * <p/>
   * This method will take an autobean implementation of IDatabaseConnection and return a DatabaseConnection
   */
  public static IDatabaseConnection connectionBeanToImpl( IDatabaseConnection connectionBean ) {
    DatabaseConnection connectionImpl = new DatabaseConnection();
    connectionImpl.setAccessType( connectionBean.getAccessType() );
    if ( connectionImpl.getAccessType() != null ) {
      connectionImpl.setAccessTypeValue( connectionImpl.getAccessType().toString() );
    }
    connectionImpl.setAttributes( mapBeanToImpl( connectionBean.getAttributes() ) );
    connectionImpl.setConnectionPoolingProperties( mapBeanToImpl( connectionBean.getConnectionPoolingProperties() ) );
    connectionImpl.setConnectSql( connectionBean.getConnectSql() );
    connectionImpl.setDatabaseName( connectionBean.getDatabaseName() );
    connectionImpl.setDatabasePort( connectionBean.getDatabasePort() );
    connectionImpl.setDatabaseType( dbTypeBeanToImpl( connectionBean.getDatabaseType() ) );
    connectionImpl.setDataTablespace( connectionBean.getDataTablespace() );
    connectionImpl.setForcingIdentifiersToLowerCase( connectionBean.isForcingIdentifiersToLowerCase() );
    connectionImpl.setForcingIdentifiersToUpperCase( connectionBean.isForcingIdentifiersToUpperCase() );
    connectionImpl.setHostname( connectionBean.getHostname() );
    connectionImpl.setId( connectionBean.getId() );
    connectionImpl.setIndexTablespace( connectionBean.getIndexTablespace() );
    connectionImpl.setInformixServername( connectionBean.getInformixServername() );
    connectionImpl.setInitialPoolSize( connectionBean.getInitialPoolSize() );
    connectionImpl.setMaximumPoolSize( connectionBean.getMaximumPoolSize() );
    connectionImpl.setName( connectionBean.getName() );
    connectionImpl.setPartitioned( connectionBean.isPartitioned() );
    connectionImpl.setPartitioningInformation( connectionBean.getPartitioningInformation() );
    connectionImpl.setPassword( connectionBean.getPassword() );
    connectionImpl.setDatabasePort( connectionBean.getDatabasePort() );
    connectionImpl.setQuoteAllFields( connectionBean.isQuoteAllFields() );
    connectionImpl.setSQLServerInstance( connectionBean.getSQLServerInstance() );
    connectionImpl.setStreamingResults( connectionBean.isStreamingResults() );
    connectionImpl.setUsername( connectionBean.getUsername() );
    connectionImpl.setUsingConnectionPool( connectionBean.isUsingConnectionPool() );
    connectionImpl
      .setUsingDoubleDecimalAsSchemaTableSeparator( connectionBean.isUsingDoubleDecimalAsSchemaTableSeparator() );
    connectionImpl.setExtraOptions( mapBeanToImpl( connectionBean.getExtraOptions() ) );
    connectionImpl.setExtraOptionsOrder( mapBeanToImpl( connectionBean.getExtraOptionsOrder() ) );
    return connectionImpl;
  }

  /**
   * @param databaseType - A DatabaseType
   * @return IDatabaseType backed by an DatabaseType
   * <p/>
   * Conversion method for creating a Database Type from an Autobean implementation of IDatabaseType
   */
  public static IDatabaseType dbTypeBeanToImpl( IDatabaseType databaseTypeBean ) {
    DatabaseType databaseTypeImpl = new DatabaseType();

    databaseTypeImpl.setDefaultDatabasePort( databaseTypeBean.getDefaultDatabasePort() );
    databaseTypeImpl.setExtraOptionsHelpUrl( databaseTypeBean.getExtraOptionsHelpUrl() );
    databaseTypeImpl.setName( databaseTypeBean.getName() );
    databaseTypeImpl.setShortName( databaseTypeBean.getShortName() );
    databaseTypeImpl.setSupportedAccessTypes( listBeanToImpl( databaseTypeBean.getSupportedAccessTypes() ) );

    return databaseTypeImpl;
  }

  /**
   * @param supportedAccessTypes
   * @return
   */
  private static List<DatabaseAccessType> listBeanToImpl( List<DatabaseAccessType> supportedAccessTypes ) {
    return new ArrayList<DatabaseAccessType>( supportedAccessTypes );
  }

  /**
   * @param map
   * @return
   */
  public static Map<String, String> mapBeanToImpl( Map<String, String> map ) {
    return new HashMap<String, String>( map );
  }

}
