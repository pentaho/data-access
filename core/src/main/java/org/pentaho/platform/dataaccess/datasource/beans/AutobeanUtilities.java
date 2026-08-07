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



package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    connectionImpl.setPartitioningInformation( listToImpl( connectionBean.getPartitioningInformation() ) );
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
    if ( databaseTypeBean == null ) {
      return null;
    }

    DatabaseType databaseTypeImpl = new DatabaseType();

    databaseTypeImpl.setDefaultDatabasePort( databaseTypeBean.getDefaultDatabasePort() );
    databaseTypeImpl.setExtraOptionsHelpUrl( databaseTypeBean.getExtraOptionsHelpUrl() );
    databaseTypeImpl.setName( databaseTypeBean.getName() );
    databaseTypeImpl.setShortName( databaseTypeBean.getShortName() );
    databaseTypeImpl.setSupportedAccessTypes( listToImpl( databaseTypeBean.getSupportedAccessTypes() ) );

    return databaseTypeImpl;
  }

  /**
   * Copies a list into a plain {@link ArrayList}, preserving {@code null}.
   * <p>
   * GWT AutoBean proxies return their collection properties wrapped in emulated types such as
   * {@code emul.java.util.ListAutoBean$1}, which are absent from every GWT-RPC serialization policy. Passing such a
   * list into a GWT-RPC call fails on the client with "could not get type signature for class
   * emul.java.util.ListAutoBean$1" and produces no server-side log entry, so every list copied off an AutoBean must
   * be converted here first.</p>
   * <p>
   * {@code null} is deliberately preserved rather than replaced with an empty list, because callers such as
   * {@code DatabaseConnectionUtils} and {@code ConnectionService} branch on {@code null} to decide whether the
   * property is set at all.</p>
   *
   * @param list the list to copy, may be {@code null}
   * @return a plain copy of the list, or {@code null} if the input was {@code null}
   */
  public static <T> List<T> listToImpl( List<T> list ) {
    return list == null ? null : new ArrayList<>( list );
  }

  /**
   * Copies a map into a plain {@link HashMap}, preserving {@code null}.
   * <p>
   * See {@link #listToImpl(List)} for why AutoBean-backed collections must not be shared by reference.
   * </p>
   *
   * @param map the map to copy, may be {@code null}
   * @return a plain copy of the map, or {@code null} if the input was {@code null}
   */
  public static Map<String, String> mapBeanToImpl( Map<String, String> map ) {
    return map == null ? null : new HashMap<>( map );
  }

  /**
   * Copies the editable properties of one connection onto another, converting any AutoBean-backed collections into
   * plain implementations along the way.
   * <p>
   * Shared by the connection controllers so that the conversion rules stay in one place; see
   * {@link #listToImpl(List)} for why the collections cannot simply be assigned by reference.
   * </p>
   *
   * @param source the connection to read from
   * @param target the connection to write to
   */
  public static void copyDatabaseConnectionProperties( IDatabaseConnection source, IDatabaseConnection target ) {
    target.setId( source.getId() );
    target.setAccessType( source.getAccessType() );
    target.setDatabaseType( dbTypeBeanToImpl( source.getDatabaseType() ) );
    target.setExtraOptions( mapBeanToImpl( source.getExtraOptions() ) );
    target.setExtraOptionsOrder( mapBeanToImpl( source.getExtraOptionsOrder() ) );
    target.setName( source.getName() );
    target.setHostname( source.getHostname() );
    target.setDatabaseName( source.getDatabaseName() );
    target.setDatabasePort( source.getDatabasePort() );
    target.setUsername( source.getUsername() );
    target.setPassword( source.getPassword() );
    target.setStreamingResults( source.isStreamingResults() );
    target.setDataTablespace( source.getDataTablespace() );
    target.setIndexTablespace( source.getIndexTablespace() );
    target.setUsingDoubleDecimalAsSchemaTableSeparator( source.isUsingDoubleDecimalAsSchemaTableSeparator() );
    target.setInformixServername( source.getInformixServername() );
    target.setAttributes( mapBeanToImpl( source.getAttributes() ) );
    target.setChanged( source.getChanged() );
    target.setQuoteAllFields( source.isQuoteAllFields() );
    // advanced option (convert to enum with upper, lower, none?)
    target.setForcingIdentifiersToLowerCase( source.isForcingIdentifiersToLowerCase() );
    target.setForcingIdentifiersToUpperCase( source.isForcingIdentifiersToUpperCase() );
    target.setConnectSql( source.getConnectSql() );
    target.setUsingConnectionPool( source.isUsingConnectionPool() );
    target.setInitialPoolSize( source.getInitialPoolSize() );
    target.setMaximumPoolSize( source.getMaximumPoolSize() );
    target.setPartitioned( source.isPartitioned() );
    target.setConnectionPoolingProperties( mapBeanToImpl( source.getConnectionPoolingProperties() ) );
    target.setPartitioningInformation( listToImpl( source.getPartitioningInformation() ) );
  }

}
