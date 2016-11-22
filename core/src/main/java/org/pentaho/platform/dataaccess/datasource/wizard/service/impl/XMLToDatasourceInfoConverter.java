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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class XMLToDatasourceInfoConverter {

  private String xml;

  public XMLToDatasourceInfoConverter( String xml ) {
    this.xml = xml;
  }

  public List<IDatasourceInfo> convert() {
    Document document = getXMLDocumentFromString( xml );
    return getDatasourceInfoList( document.getDocumentElement() );
  }


  private List<IDatasourceInfo> getDatasourceInfoList( Element element ) {
    List<IDatasourceInfo> datasourceInfoList = new ArrayList<IDatasourceInfo>();
    NodeList nodeList = element.getChildNodes();
    for ( int i = 0; i < nodeList.getLength(); i++ ) {
      Element ele = (Element) nodeList.item( i );
      boolean editable = Boolean.parseBoolean( getNodeValueByTagName( ele, "editable" ) );
      boolean removable = Boolean.parseBoolean( getNodeValueByTagName( ele, "removable" ) );
      boolean importable = Boolean.parseBoolean( getNodeValueByTagName( ele, "importable" ) );
      boolean exportable = Boolean.parseBoolean( getNodeValueByTagName( ele, "exportable" ) );

      IDatasourceInfo info =
        new DatasourceInfo( getName( ele ), getId( ele ), getType( ele ), editable, removable, importable, exportable );
      datasourceInfoList.add( info );
    }
    return datasourceInfoList;
  }

  /*
   * Return the first name that matched the tagName. Starting from the
   * current element location
   */
  private Node getNodeByTagName( Element element, String tagName ) {
    NodeList list = element.getChildNodes();
    for ( int i = 0; i < list.getLength(); i++ ) {
      Node node = list.item( i );
      if ( node != null && node.getNodeName().equals( tagName ) ) {
        return node;
      }
    }
    return null;
  }

  private final String getType( Element element ) {
    return getNodeValueByTagName( element, "type" );
  }

  private final String getId( Element element ) {
    return getNodeValueByTagName( element, "id" );
  }

  private final String getName( Element element ) {
    return getNodeValueByTagName( element, "name" );
  }

  private Document getXMLDocumentFromString( String xmlText ) {
    return (Document) XMLParser.parse( xmlText );
  }

  /*
   * Get Node Value of the element matching the tag name
   */
  private String getNodeValueByTagName( Element element, String tagName ) {
    Node node = getNodeByTagName( element, tagName );
    if ( node != null && node.getFirstChild() != null ) {
      return node.getFirstChild().getNodeValue();
    } else {
      return null;
    }
  }
}

