package org.pentaho.platform.dataaccess.datasource.utils;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.PartitionDatabaseMeta;
import org.pentaho.di.core.variables.VariableSpace;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for applying variable substitution to database connection fields.
 * Applies environmentSubstitute to connection fields that may contain variable references
 * (in the form ${VARIABLE_NAME}).
 */
public class DatabaseConnectionUtils {

  private DatabaseConnectionUtils() {
    // Utility class - prevent instantiation
  }

  /**
   * Apply environment variable substitution to an IDatabaseConnection using the provided VariableSpace.
   * Substitutes variables in the following fields:
   * - name (connection identifier, may be used in pooling keys)
   * - hostname
   * - databaseName
   * - databasePort
   * - username
   * - password
   * - connectSql
   * - sqlServerInstance
   * - dataTablespace (Oracle and other database-specific tablespaces)
   * - indexTablespace (Oracle and other database-specific tablespaces)
   * - connectionPoolingProperties (all values)
   * - partitioning information (if present)
   *
   * @param connection The database connection to update (modified in-place)
   * @param variableSpace The variable space to use for substitution
   */
  public static void applyEnvironmentSubstitution( final IDatabaseConnection connection,
      final VariableSpace variableSpace ) {
    if ( connection == null || variableSpace == null ) {
      return;
    }

    applyBasicFieldSubstitution( connection, variableSpace );
    applyPoolingPropertySubstitution( connection, variableSpace );
    applyPartitioningSubstitution( connection, variableSpace );
  }

  private static void applyBasicFieldSubstitution( final IDatabaseConnection connection,
      final VariableSpace variableSpace ) {
    // Substitute connection name (used for pooling and identification)
    connection.setName( substituteValue( connection.getName(), variableSpace ) );

    // Substitute basic connection fields
    connection.setHostname( substituteValue( connection.getHostname(), variableSpace ) );
    connection.setDatabaseName( substituteValue( connection.getDatabaseName(), variableSpace ) );
    connection.setDatabasePort( substituteValue( connection.getDatabasePort(), variableSpace ) );
    connection.setUsername( substituteValue( connection.getUsername(), variableSpace ) );
    connection.setPassword( substituteValue( connection.getPassword(), variableSpace ) );

    // Substitute connect SQL
    connection.setConnectSql( substituteValue( connection.getConnectSql(), variableSpace ) );

    // Substitute SQL Server instance
    connection.setSQLServerInstance( substituteValue( connection.getSQLServerInstance(), variableSpace ) );

    // Substitute tablespace fields (Oracle and similar databases)
    connection.setDataTablespace( substituteValue( connection.getDataTablespace(), variableSpace ) );
    connection.setIndexTablespace( substituteValue( connection.getIndexTablespace(), variableSpace ) );
  }

  private static void applyPoolingPropertySubstitution( final IDatabaseConnection connection,
      final VariableSpace variableSpace ) {
    if ( connection.getConnectionPoolingProperties() == null
        || connection.getConnectionPoolingProperties().isEmpty() ) {
      return;
    }

    Map<String, String> substitutedPoolingProps = new HashMap<>();
    for ( Map.Entry<String, String> entry : connection.getConnectionPoolingProperties().entrySet() ) {
      substitutedPoolingProps.put( entry.getKey(), substituteValue( entry.getValue(), variableSpace ) );
    }
    connection.setConnectionPoolingProperties( substitutedPoolingProps );
  }

  private static void applyPartitioningSubstitution( final IDatabaseConnection connection,
      final VariableSpace variableSpace ) {
    if ( !connection.isPartitioned() || connection.getPartitioningInformation() == null ) {
      return;
    }

    for ( PartitionDatabaseMeta partition : connection.getPartitioningInformation() ) {
      applyEnvironmentSubstitutionToPartition( partition, variableSpace );
    }
  }

  private static String substituteValue( final String value, final VariableSpace variableSpace ) {
    if ( value == null ) {
      return null;
    }
    return variableSpace.environmentSubstitute( value );
  }

  /**
   * Apply environment variable substitution to a PartitionDatabaseMeta.
   * Substitutes variables in hostname, port, and database name fields.
   *
   * @param partition The partition metadata to update (modified in-place)
   * @param variableSpace The variable space to use for substitution
   */
  private static void applyEnvironmentSubstitutionToPartition( final PartitionDatabaseMeta partition,
      final VariableSpace variableSpace ) {
    if ( partition == null || variableSpace == null ) {
      return;
    }

    if ( partition.getHostname() != null ) {
      partition.setHostname( variableSpace.environmentSubstitute( partition.getHostname() ) );
    }

    if ( partition.getPort() != null ) {
      partition.setPort( variableSpace.environmentSubstitute( partition.getPort() ) );
    }

    if ( partition.getDatabaseName() != null ) {
      partition.setDatabaseName( variableSpace.environmentSubstitute( partition.getDatabaseName() ) );
    }
  }
}
