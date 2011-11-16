package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.api.datasource.IGenericDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.GenericDatasourceInfo;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class XMLToGenericDatasourceInfoConverter {

    private String xml;
    
    public XMLToGenericDatasourceInfoConverter(String xml) {
      this.xml = xml;
    }
    
    public List<IGenericDatasourceInfo> convert() {
      Document document = getXMLDocumentFromString(xml);
      return getGenericDatasourceInfoList(document.getDocumentElement());
    }
    
    
    private List<IGenericDatasourceInfo> getGenericDatasourceInfoList(Element element) {
      List<IGenericDatasourceInfo> datasourceInfoList = new ArrayList<IGenericDatasourceInfo>();
      NodeList nodeList = element.getChildNodes();
      for(int i=0;i<nodeList.getLength();i++) {
        Element ele = (Element) nodeList.item(i);
        IGenericDatasourceInfo info = new GenericDatasourceInfo(getId(ele), getType(ele));
        datasourceInfoList.add(info);
      }
      return datasourceInfoList;
    }
    
    private Element getGenericDatasourceInfoElement(Node node) {
      Element element = (Element) node;
      return (Element) getNodeByTagName(element, "genericDatasourceInfo");
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
    
    private final String getType(Element element) {
      return getNodeValueByTagName(element, "type");
    }
    
    private final String getId(Element element) {
      return getNodeValueByTagName(element, "id");
    }
    
    private Document getXMLDocumentFromString(String xmlText) {
      return (Document) XMLParser.parse(xmlText);
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

