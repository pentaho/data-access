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

package org.pentaho.platform.dataaccess.datasource.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.util.DatabaseTypeHelper;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.Text;
import com.google.gwt.xml.client.XMLParser;


public class DatabaseConnectionConverter {
  private static final String USERNAME = "username";
  private static final String ATTRIBUTES = "attributes";
  private static final String DATABASE_TYPE = "databaseType";
  private static final String ACCESS_TYPE = "accessType";
  private static final String PASSWORD = "password";
  private static final String NAME = "name";
  private static final String SERVER_NAME = "serverName";
  private static final String DATA_TABLESPACE = "dataTablespace";
  private static final String INDEX_TABLESPACE = "indexTablespace";
  private static final String HOSTNAME = "hostname";
  private static final String DATABASE_PORT = "databasePort";
  private static final String DATABASE_NAME = "databaseName";
  private static final String DATABASE_CONNECTION = "databaseConnection";
  private DatabaseTypeHelper databaseTypeHelper;
  public DatabaseConnectionConverter(DatabaseTypeHelper databaseTypeHelper) {
    this.databaseTypeHelper = databaseTypeHelper;
  }
  public String convertToXml(IDatabaseConnection dbConn) {
    
    Document document = XMLParser.createDocument();
    try {
      Element databaseConnection = document.createElement(DATABASE_CONNECTION);
      document.appendChild(databaseConnection);
      
      Element databaseName = document.createElement(DATABASE_NAME);
      Text databaseNameText = document.createTextNode(dbConn.getDatabaseName());
      databaseName.appendChild(databaseNameText);
      databaseConnection.appendChild(databaseName);
      
      Element databasePort = document.createElement(DATABASE_PORT);
      Text databasePortText = document.createTextNode(dbConn.getDatabasePort());
      databasePort.appendChild(databasePortText);
      databaseConnection.appendChild(databasePort);
      
      Element hostname = document.createElement(HOSTNAME);
      Text hostnameText = document.createTextNode(dbConn.getHostname());
      hostname.appendChild(hostnameText);
      databaseConnection.appendChild(hostname);
  
      Element indexTablespace = document.createElement(INDEX_TABLESPACE);
      Text indexTablespaceText = document.createTextNode(dbConn.getIndexTablespace());
      indexTablespace.appendChild(indexTablespaceText);
      databaseConnection.appendChild(indexTablespace);

      Element dataTablespace = document.createElement(DATA_TABLESPACE);
      Text dataTablespaceText = document.createTextNode(dbConn.getIndexTablespace());
      dataTablespace.appendChild(dataTablespaceText);
      databaseConnection.appendChild(dataTablespace);
      
      Element informixServername = document.createElement(SERVER_NAME);
      Text informixServernameText = document.createTextNode(dbConn.getInformixServername());
      informixServername.appendChild(informixServernameText);
      databaseConnection.appendChild(informixServername);
  
      Element name = document.createElement(NAME);
      Text nameText = document.createTextNode(dbConn.getName());
      name.appendChild(nameText);
      databaseConnection.appendChild(name);
  
      Element username = document.createElement(USERNAME);
      Text usernameTxt = document.createTextNode(dbConn.getUsername());
      username.appendChild(usernameTxt);
      databaseConnection.appendChild(username);
      
      Element password = document.createElement(PASSWORD);
      Text passwordText = document.createTextNode(dbConn.getPassword());
      password.appendChild(passwordText);
      databaseConnection.appendChild(password);
  
      Element accessType = document.createElement(ACCESS_TYPE);
      Text accessTypeText = document.createTextNode(dbConn.getAccessType().getName());
      accessType.appendChild(accessTypeText);
      databaseConnection.appendChild(accessType);
  
      Element databaseType = document.createElement(DATABASE_TYPE);   
      Text databaseTypeText = document.createTextNode(dbConn.getDatabaseType().getShortName());
      databaseType.appendChild(databaseTypeText);
      databaseConnection.appendChild(databaseType);
  
      Element attrributes = document.createElement(ATTRIBUTES);
      databaseConnection.appendChild(attrributes);
      Map<String, String> attributeMap = dbConn.getAttributes();
      for(String key:attributeMap.keySet()) {
        Element attribute = document.createElement(key);
        Text attributeText = document.createTextNode(attributeMap.get(key));
        attribute.appendChild(attributeText);
        attrributes.appendChild(attribute);
      }
      
      return document.toString();
    } catch(Exception e) {
      return null;
    }
  }

  public IDatabaseConnection convertToObject(String xml) {
    Document document = XMLParser.parse(xml);
    Element element = document.getDocumentElement();
    IDatabaseConnection databaseConnection = new DatabaseConnection();
    databaseConnection.setDatabaseName(getNodeValueByTagName(element, DATABASE_NAME));
    databaseConnection.setHostname(getNodeValueByTagName(element, HOSTNAME));
    databaseConnection.setIndexTablespace(getNodeValueByTagName(element, INDEX_TABLESPACE));
    databaseConnection.setDataTablespace(getNodeValueByTagName(element, DATA_TABLESPACE));
    databaseConnection.setName(getNodeValueByTagName(element, NAME));
    databaseConnection.setUsername(getNodeValueByTagName(element, USERNAME));
    databaseConnection.setPassword(getNodeValueByTagName(element, PASSWORD));    
    databaseConnection.setDatabasePort(getNodeValueByTagName(element, DATABASE_PORT));
    databaseConnection.setAccessType(DatabaseAccessType.getAccessTypeByName(getNodeValueByTagName(element, ACCESS_TYPE)));
    databaseConnection.setDatabaseType((DatabaseType) databaseTypeHelper.getDatabaseTypeByShortName(getNodeValueByTagName(element, DATABASE_TYPE)));
    databaseConnection.setPassword(getNodeValueByTagName(element, PASSWORD));
    databaseConnection.setInformixServername(getNodeValueByTagName(element, SERVER_NAME));
    for(Node node :getNodesByTagName(element, ATTRIBUTES)) {
      databaseConnection.getAttributes().put(node.getNodeName(), node.getNodeValue());
    }
    return databaseConnection;
  }
  
  
  /*
   * Return the first name that matched the tagName. Starting from the 
   * current element location
   */
  private Node getNodeByTagName(Element element, String tagName) {
    NodeList list = element.getChildNodes();
    for(int i=0;i<list.getLength();i++) {
      Node node = list.item(i);
      if( node != null && node.getNodeName().equals(tagName)) {
        return node;
      }
    }
    return null;
  }
  
  /*
   * Return all node matching the tagName, starting from the current element
   * location
   */
  private List<Node> getNodesByTagName(Element element, String tagName) {
    List<Node> nodes = new ArrayList<Node>();
    NodeList list = element.getChildNodes();
    for(int i=0;i<list.getLength();i++) {
      Node node = list.item(i);
      if( node != null && node.getNodeName().equals(tagName)) {
        nodes.add(node);
      }
    }
    return nodes;
  }
  
 
  /*
   * Get Node Value of the element matching the tag name
   */
  private String getNodeValueByTagName(Element element, String tagName) {
    Node node = getNodeByTagName(element, tagName);
    if(node != null && node.getFirstChild() != null) {
      return node.getFirstChild().getNodeValue();
    } else {
      return null;
    }
  }
}
