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

    // Substitute connection name (used for pooling and identification)
    if ( connection.getName() != null ) {
      connection.setName( variableSpace.environmentSubstitute( connection.getName() ) );
    }

    // Substitute basic connection fields
    if ( connection.getHostname() != null ) {
      connection.setHostname( variableSpace.environmentSubstitute( connection.getHostname() ) );
    }

    if ( connection.getDatabaseName() != null ) {
      connection.setDatabaseName( variableSpace.environmentSubstitute( connection.getDatabaseName() ) );
    }

    if ( connection.getDatabasePort() != null ) {
      connection.setDatabasePort( variableSpace.environmentSubstitute( connection.getDatabasePort() ) );
    }

    if ( connection.getUsername() != null ) {
      connection.setUsername( variableSpace.environmentSubstitute( connection.getUsername() ) );
    }

    if ( connection.getPassword() != null ) {
      connection.setPassword( variableSpace.environmentSubstitute( connection.getPassword() ) );
    }

    // Substitute connect SQL
    if ( connection.getConnectSql() != null ) {
      connection.setConnectSql( variableSpace.environmentSubstitute( connection.getConnectSql() ) );
    }

    // Substitute SQL Server instance
    if ( connection.getSQLServerInstance() != null ) {
      connection.setSQLServerInstance( variableSpace.environmentSubstitute( connection.getSQLServerInstance() ) );
    }

    // Substitute tablespace fields (Oracle and similar databases)
    if ( connection.getDataTablespace() != null ) {
      connection.setDataTablespace( variableSpace.environmentSubstitute( connection.getDataTablespace() ) );
    }

    if ( connection.getIndexTablespace() != null ) {
      connection.setIndexTablespace( variableSpace.environmentSubstitute( connection.getIndexTablespace() ) );
    }

    // Substitute connection pooling properties
    if ( connection.getConnectionPoolingProperties() != null
        && !connection.getConnectionPoolingProperties().isEmpty() ) {
      Map<String, String> substitutedPoolingProps = new HashMap<>();
      for ( Map.Entry<String, String> entry : connection.getConnectionPoolingProperties().entrySet() ) {
        String substitutedValue = entry.getValue() != null ? variableSpace.environmentSubstitute( entry.getValue() )
            : null;
        substitutedPoolingProps.put( entry.getKey(), substitutedValue );
      }
      connection.setConnectionPoolingProperties( substitutedPoolingProps );
    }

    // Substitute partitioning information if present
    if ( connection.isPartitioned() && connection.getPartitioningInformation() != null ) {
      for ( PartitionDatabaseMeta partition : connection.getPartitioningInformation() ) {
        applyEnvironmentSubstitutionToPartition( partition, variableSpace );
      }
    }
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
