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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created June 18, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.geo.GeoContextConfigProvider;
import org.pentaho.agilebi.modeler.geo.GeoContextFactory;
import org.pentaho.commons.connection.IPentahoConnection;
import org.pentaho.commons.connection.IPentahoMetaData;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.commons.connection.marshal.MarshallableRow;
import org.pentaho.commons.connection.memory.MemoryMetaData;
import org.pentaho.commons.connection.memory.MemoryResultSet;
import org.pentaho.metadata.query.model.util.CsvDataReader;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.services.connection.PentahoConnectionFactory;
import org.pentaho.platform.plugin.services.connections.sql.SQLConnection;
import org.pentaho.platform.plugin.services.connections.sql.SQLMetaData;
import org.pentaho.platform.util.logging.SimpleLogger;

public class DatasourceServiceHelper {
  private static final Log logger = LogFactory.getLog(DatasourceServiceHelper.class);
  
  private static final String PLUGIN_NAME = "data-access"; //$NON-NLS-1$
  private static final String SETTINGS_FILE = PLUGIN_NAME + "/settings.xml"; //$NON-NLS-1$

  private static final String LATITUDE = "latitude";
  private static final String LONGITUDE = "longitude";

  private static GeoContextConfigProvider configProvider = new GeoContextSettingsProvider(SETTINGS_FILE);

  public static Connection getDataSourceConnection(String connectionName, IPentahoSession session) {
    SQLConnection sqlConnection= (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connectionName, session, new SimpleLogger(DatasourceServiceHelper.class.getName()));
    return sqlConnection.getNativeConnection(); 
  }

  public static SerializedResultSet getSerializeableResultSet(String connectionName, String query, int rowLimit, IPentahoSession session) throws DatasourceServiceException{
    SerializedResultSet serializedResultSet = null;
    SQLConnection sqlConnection = null; 
    try {
      sqlConnection = (SQLConnection) PentahoConnectionFactory.getConnection(IPentahoConnection.SQL_DATASOURCE, connectionName, PentahoSessionHolder.getSession(), null);
      sqlConnection.setMaxRows(rowLimit);
      sqlConnection.setReadOnly(true);
      IPentahoResultSet resultSet =  sqlConnection.executeQuery(query);
      logger.debug("ResultSet is not scrollable. Copying into memory");//$NON-NLS-1$
      if ( !resultSet.isScrollable()) {
        resultSet = convertToMemoryResultSet(resultSet);
      } 
      MarshallableResultSet marshallableResultSet = new MarshallableResultSet();
      marshallableResultSet.setResultSet(resultSet);
      IPentahoMetaData ipmd = resultSet.getMetaData();
      int[] columnTypes = null;
      if (ipmd instanceof SQLMetaData) {
        SQLMetaData smd = (SQLMetaData)ipmd;
        columnTypes = smd.getJDBCColumnTypes();
      } else if(ipmd instanceof MemoryMetaData) {
        MemoryMetaData mmd = (MemoryMetaData)ipmd;
        String[] columnTypesAsString = mmd.getColumnTypes();
        columnTypes = new int[columnTypesAsString.length];
        for(int i=0;i<columnTypesAsString.length;i++) {
          columnTypes[i] = Integer.parseInt(columnTypesAsString[i]);
        }
      }
      
      if ( columnTypes != null ) {
        // Hack warning - get JDBC column types
        // TODO: Need to generalize this amongst all IPentahoResultSets
        List<List<String>> data = new ArrayList<List<String>>();
        for (MarshallableRow row : marshallableResultSet.getRows()) {
          String[] rowData = row.getCell();
          List<String> rowDataList = new ArrayList<String>(rowData.length);
          for(int j=0;j<rowData.length;j++) {
            rowDataList.add(rowData[j]);
          }
          data.add(rowDataList);
        }
        serializedResultSet = new SerializedResultSet(columnTypes, marshallableResultSet.getColumnNames().getColumnName(), data);
      }
    } catch (Exception e) {
      logger.error(Messages.getErrorString("DatasourceServiceHelper.ERROR_0001_QUERY_VALIDATION_FAILED", e.getLocalizedMessage()),e); //$NON-NLS-1$
      throw new DatasourceServiceException(Messages.getErrorString("DatasourceServiceHelper.ERROR_0001_QUERY_VALIDATION_FAILED",e.getLocalizedMessage()), e); //$NON-NLS-1$      
    } finally {
        if (sqlConnection != null) {
          sqlConnection.close();
        }
    }
    return serializedResultSet;

  }
  public static List<List<String>> getCsvDataSample(String fileLocation, boolean headerPresent, String delimiter, String enclosure, int rowLimit) {
    CsvDataReader reader = new CsvDataReader(fileLocation, headerPresent, delimiter, enclosure, rowLimit);
    return reader.loadData();
  }
  /**
   * Convert the live result set to memory result set.
   * @param resultSet
   * @return
   */
  private static IPentahoResultSet convertToMemoryResultSet(IPentahoResultSet resultSet) throws SQLException{
    MemoryResultSet cachedResultSet =  null;
    try {
      IPentahoMetaData meta = resultSet.getMetaData();
      Object columnHeaders[][] = meta.getColumnHeaders();
      MemoryMetaData cachedMetaData = new MemoryMetaData(columnHeaders, null);
      String[] colTypesAsString;
      // If the IPentahoMetaData is an instanceof SQLMetaData then get the column types from the metadata
      if(meta instanceof SQLMetaData) {
        SQLMetaData sqlMeta = (SQLMetaData) meta;
        // Column Types in SQLMetaData are int. MemoryMetaData stores column types as string. So we will store them as string in MemoryMetaData
        int[] colTypes = sqlMeta.getJDBCColumnTypes();
        colTypesAsString = new String[colTypes.length];
        for(int i=0;i<colTypes.length;i++) {
          colTypesAsString[i] = Integer.toString(colTypes[i]);
        }
        cachedMetaData.setColumnTypes(colTypesAsString);
      }
      cachedResultSet = new MemoryResultSet(cachedMetaData);
      Object[] rowObjects = resultSet.next();
      while (rowObjects != null) {
        cachedResultSet.addRow(rowObjects);
        rowObjects = resultSet.next();
      }
    } finally {
      resultSet.close();
    }
    return cachedResultSet;        
        
  }

  public static GeoContext getGeoContext() throws DatasourceServiceException {
    try {
      GeoContext geo = GeoContextFactory.create(configProvider);
      return geo;
    } catch (ModelerException e) {
      throw new DatasourceServiceException(e);
    }
  }

}
