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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.database.IDatabaseDialect;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.service.DatabaseDialectService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will convert legacy Connection class references to
 * 5.0 IDatabaseConnection instances for MultiTableDatasourceDTO objects'
 * deserialization
 */
public class LegacyDatasourceConverter implements Converter {
  /**
   * Convert an object to textual data.
   *
   * @param source  The object to be marshalled.
   * @param writer  A stream to write to.
   * @param context A context that allows nested objects to be processed by XStream.
   */
  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
  }

  /**
   * Convert textual data back into an object.
   *
   * @param reader  The stream to read the text from.
   * @param context
   * @return The resulting object.
   */
  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    MultiTableDatasourceDTO resultDTO = new MultiTableDatasourceDTO();

    while(reader.hasMoreChildren()){
      reader.moveDown();

      String nodeName = reader.getNodeName();

      if(nodeName.equalsIgnoreCase("datasourceName")){
        String value = reader.getValue();
        resultDTO.setDatasourceName(value);
      }
      else if(nodeName.equalsIgnoreCase("selectedConnection")){

        String connectionClass = reader.getAttribute("class");

        if(connectionClass != null){

          DatabaseConnection databaseConnection = new DatabaseConnection();

          if(connectionClass.equals("org.pentaho.platform.dataaccess.datasource.beans.Connection")){
            // instantiate IDatabaseConnection impl

            while(reader.hasMoreChildren()){
              reader.moveDown();

              nodeName = reader.getNodeName();

              if(reader.getNodeName().equalsIgnoreCase("name")){
                String databaseName = reader.getValue();
                databaseConnection.setName(databaseName);
                databaseConnection.setId(databaseName);
              }
              else if(reader.getNodeName().equalsIgnoreCase("username")){
                databaseConnection.setUsername(reader.getValue());
              }
              else if(reader.getNodeName().equalsIgnoreCase("password")){
                databaseConnection.setPassword(reader.getValue());
              }
              else if(reader.getNodeName().equalsIgnoreCase("url")){
                ParsedJdbcUrl parsedJdbcUrl = new ParsedJdbcUrl(reader.getValue());

                databaseConnection.setHostname(parsedJdbcUrl.getHostname());
                databaseConnection.setDatabasePort(parsedJdbcUrl.getPort());
                databaseConnection.setDatabaseName(parsedJdbcUrl.getDatabaseName());

                databaseConnection.setDatabaseType(resolveDatabaseType(parsedJdbcUrl.getJdbcPrefix()));

              }
              reader.moveUp();
            }

            resultDTO.setSelectedConnection(databaseConnection);
          }
          else{
            // instantiate the class specified
            try{
              Class databaseConnectionClass = Class.forName(connectionClass);
              IDatabaseConnection databaseConnectionInstance = (IDatabaseConnection) context.convertAnother(resultDTO, databaseConnectionClass);

              resultDTO.setSelectedConnection(databaseConnectionInstance);
            }
            catch (ClassNotFoundException e){
              // not going to work anyway, set empty connection for now
              resultDTO.setSelectedConnection(new DatabaseConnection());
            }
          }
        }
      }
      else if(nodeName.equalsIgnoreCase("schemaModel")){
        SchemaModel schemaModel = (SchemaModel) context.convertAnother(resultDTO, SchemaModel.class);
        if(schemaModel != null){
          resultDTO.setSchemaModel(schemaModel);
        }
      }
      else if(nodeName.equalsIgnoreCase("selectedTables")){
        List<String> selectedTables = (List<String>) context.convertAnother(resultDTO, ArrayList.class);
        if(selectedTables != null){
          resultDTO.setSelectedTables(selectedTables);
        }
      }

      reader.moveUp();
    }

    return resultDTO;
  }

  /**
   * Determines whether the converter can marshall a particular type.
   *
   * @param type the Class representing the object type to be converted
   */
  @Override
  public boolean canConvert(Class type) {
    if(type.isAssignableFrom(MultiTableDatasourceDTO.class)){
      return true;
    }
    return false;
  }

  /**
   * Will resolve a database type based on the prefix of the jdbc URL,
   * which we assume originated from one of these classes
   *
   * @param urlPrefix
   * @return
   */
  private IDatabaseType resolveDatabaseType(String urlPrefix){
    DatabaseDialectService databaseDialectService = new DatabaseDialectService(false);
    List<IDatabaseDialect> databaseDialects = databaseDialectService.getDatabaseDialects();
    for(IDatabaseDialect databaseDialect : databaseDialects){
      if(databaseDialect.getNativeJdbcPre().startsWith(urlPrefix)){
        return databaseDialect.getDatabaseType();
      }
    }

    return null;
  }

  /**
   * Helper to parse a jdbc url and provide hostname,
   * port, and database name as property values
   */
  private class ParsedJdbcUrl{
    private String hostname;
    private String port;
    private String databaseName;
    private String jdbcPrefix;

    ParsedJdbcUrl(String jdbcUrl){
      parseUrl(jdbcUrl);
    }

    /**
     * Parse jdbc url to get hostname, port, databasename
     *
     * @param jdbcUrl
     */
    void parseUrl(String jdbcUrl){

      String[] urlParts = jdbcUrl.split(":");

      String port = "";
      String hostname = "";
      String databaseName = urlParts[urlParts.length-1];
      databaseName = databaseName.replaceAll(".*[/|:]","");
      databaseName = databaseName.replaceAll("[^a-zA-Z0-9\\-_]+.*","");

      for(int x = 0; x <= urlParts.length-1; x++){
        String part = urlParts[x];
        if(part.matches("^[\\d]+.*")){

          port = part.replaceAll("[/|:].*","");
          port = port.replaceAll("[^\\d]+","");

          // hostname should be in previous part
          hostname = urlParts[x-1];
          if(hostname.contains("@")){
            hostname = hostname.replaceAll(".*@\\W*","");
          }
          else{
            // valid hostname chars per https://tools.ietf.org/html/rfc952
            hostname = hostname.replaceAll("[^a-zA-Z0-9\\-.]","");
          }
        }
      }

      // if hostname is still empty, try a different way
      if(hostname == ""){
        if(jdbcUrl.contains("//")){
          urlParts = jdbcUrl.split("//");
          hostname = urlParts[urlParts.length-1];
          hostname = hostname.replaceAll("[\\W].*","");
        }
        else if(jdbcUrl.contains("@")){
          urlParts = jdbcUrl.split("@");
          hostname = urlParts[urlParts.length-1];
        }
      }

      this.hostname = hostname;
      this.port = port;
      this.databaseName = databaseName;
      this.jdbcPrefix = urlParts[0] + ":" + urlParts[1];

    }

    private String getHostname() {
      return hostname;
    }

    private String getPort() {
      return port;
    }

    private String getDatabaseName() {
      return databaseName;
    }

    private String getJdbcPrefix() {
      return jdbcPrefix;
    }
  }

}
