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
  private DatabaseTypeHelper databaseTypeHelper;
  public DatabaseConnectionConverter(DatabaseTypeHelper databaseTypeHelper) {
    this.databaseTypeHelper = databaseTypeHelper;
  }
  public String convertToXml(IDatabaseConnection dbConn) {
    
    Document document = XMLParser.createDocument();
    try {
      Element databaseConnection = document.createElement("databaseConnection");
      document.appendChild(databaseConnection);
      
      Element databaseName = document.createElement("databaseName");
      Text databaseNameText = document.createTextNode(dbConn.getDatabaseName());
      databaseName.appendChild(databaseNameText);
      databaseConnection.appendChild(databaseName);
      
      Element databasePort = document.createElement("databasePort");
      Text databasePortText = document.createTextNode(dbConn.getDatabasePort());
      databasePort.appendChild(databasePortText);
      databaseConnection.appendChild(databasePort);
      
      Element hostname = document.createElement("hostname");
      Text hostnameText = document.createTextNode(dbConn.getHostname());
      hostname.appendChild(hostnameText);
      databaseConnection.appendChild(hostname);
  
      Element indexTablespace = document.createElement("indexTablespace");
      Text indexTablespaceText = document.createTextNode(dbConn.getIndexTablespace());
      indexTablespace.appendChild(indexTablespaceText);
      databaseConnection.appendChild(indexTablespace);

      Element dataTablespace = document.createElement("dataTablespace");
      Text dataTablespaceText = document.createTextNode(dbConn.getIndexTablespace());
      dataTablespace.appendChild(dataTablespaceText);
      databaseConnection.appendChild(dataTablespace);
      
      Element informixServername = document.createElement("serverName");
      Text informixServernameText = document.createTextNode(dbConn.getInformixServername());
      informixServername.appendChild(informixServernameText);
      databaseConnection.appendChild(informixServername);
  
      Element name = document.createElement("name");
      Text nameText = document.createTextNode(dbConn.getName());
      name.appendChild(nameText);
      databaseConnection.appendChild(name);
  
      Element password = document.createElement("password");
      Text passwordText = document.createTextNode(dbConn.getPassword());
      password.appendChild(passwordText);
      databaseConnection.appendChild(password);
  
      Element accessType = document.createElement("accessType");
      Text accessTypeText = document.createTextNode(dbConn.getAccessType().getName());
      accessType.appendChild(accessTypeText);
      databaseConnection.appendChild(accessType);
  
      Element databaseType = document.createElement("databaseType");   
      Text databaseTypeText = document.createTextNode(dbConn.getDatabaseType().getShortName());
      databaseType.appendChild(databaseTypeText);
      databaseConnection.appendChild(databaseType);
  
      Element attrributes = document.createElement("attrributes");
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
    databaseConnection.setDatabaseName(getNodeValueByTagName(element, "databaseName"));
    databaseConnection.setHostname(getNodeValueByTagName(element, "hostname"));
    databaseConnection.setIndexTablespace(getNodeValueByTagName(element, "indexTablespace"));
    databaseConnection.setDataTablespace(getNodeValueByTagName(element, "dataTablespace"));
    databaseConnection.setName(getNodeValueByTagName(element, "name"));
    databaseConnection.setPassword(getNodeValueByTagName(element, "password"));
    databaseConnection.setUsername(getNodeValueByTagName(element, "username"));
    databaseConnection.setDatabasePort(getNodeValueByTagName(element, "port"));
    databaseConnection.setAccessType(DatabaseAccessType.getAccessTypeByName(getNodeValueByTagName(element, "accessType")));
    databaseConnection.setDatabaseType((DatabaseType) databaseTypeHelper.getDatabaseTypeByShortName(getNodeValueByTagName(element, "databaseType")));
    databaseConnection.setPassword(getNodeValueByTagName(element, "password"));
    databaseConnection.setInformixServername(getNodeValueByTagName(element, "serverName"));
    for(Node node :getNodesByTagName(element, "attributes")) {
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
