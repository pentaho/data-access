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
 * Copyright 2009 Pentaho Corporation.  All rights reserved.
 *
 * Created Nov 4, 2009
 * @author jdixon
 */
package org.pentaho.platform.dataaccess.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.engine.ObjectSupplier;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Provides a simple API for working with the connections web service.
 * 
 * This class implements IConnectionService
 * {@link org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IConnectionService }
 * 
 * Example code for using this class:    
 *
 *  ConnectionServiceClient serviceClient = new ConnectionServiceClient(); 
 *  serviceClient.setHost("http://localhost:8080/pentaho");
 *  serviceClient.setUserId("joe");
 *  serviceClient.setPassword("password");  
 *  List<IConnection> connections = serviceClient.getConnections();
 * 
 * @author jamesdixon
 *
 */
public class ConnectionServiceClient implements IConnectionService, ObjectSupplier {

  protected static final Log logger = LogFactory.getLog(ConnectionServiceClient.class);
  
  private String serviceUrl; // e.g. http://someserver:port/pentaho
  
  private String userId;
  
  private String password;

  private HttpClient httpClientInstance = null;

  /**
   * Returns an instance of an HttpClient. Only one is created per 
   * ConnectionServiceClient so all calls should be made synchronously.
   * @return The HTTP client to be used for web service calls
   */
  private HttpClient getClient() {
    
    if( httpClientInstance == null ) {
      httpClientInstance = new HttpClient();
      if ((userId != null) && (userId.length() > 0) && (password != null)
        && (password.length() > 0)) {
        Credentials creds = new UsernamePasswordCredentials(userId, password);
        httpClientInstance.getState().setCredentials(AuthScope.ANY, creds);
        httpClientInstance.getParams().setAuthenticationPreemptive(true);
      }
    }
    return httpClientInstance;
  }
  
  /**
   * Adds a connection to the server's configuration. Returns true if the attempt was successful.
   * setHost(), setUserId() and setPassword() must be called before this
   * method is called.
   * @param connection The connection to be added
   * @return True if the addition was successful
   */
  public boolean addConnection(Connection connection) throws ConnectionServiceException {
    String xml = getConnectionXml( connection );
    PostMethod callMethod = new PostMethod( serviceUrl+"/addConnection" ); //$NON-NLS-1$

    // add the xml to the request entity
    RequestEntity requestEntity = new StringRequestEntity( xml ); 
    callMethod.setRequestEntity( requestEntity );
    // get the result and parse de-serialize it
    Node node = getResultNode( callMethod );
    return node != null && Boolean.parseBoolean( this.getNodeText(node) );
  }

  public DatabaseConnection convertFromConnection(Connection connection) throws ConnectionServiceException {
    String xml = getConnectionXml( connection );
    PostMethod callMethod = new PostMethod( serviceUrl+"/convertFromConnection" ); //$NON-NLS-1$

    // add the xml to the request entity
    RequestEntity requestEntity = new StringRequestEntity( xml ); 
    callMethod.setRequestEntity( requestEntity );
    // get the result and parse de-serialize it
    Document doc = getResultDocument( callMethod );
    
    try {
      return getResponseDatabaseConnection( doc.getRootElement() );
    } catch ( XMLStreamException e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    } catch ( AxisFault e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    } catch ( Exception e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    }
  }

  public Document serializeDatabaseConnection( DatabaseConnection databaseConnection ) {
    XStream xstream = new XStream(new DomDriver());
    xstream.alias("databaseConnection", DatabaseConnection.class); //$NON-NLS-1$
    xstream.alias("accessType", DatabaseAccessType.class); //$NON-NLS-1$
    String xml = xstream.toXML(databaseConnection);
    System.out.println(xml);
    return null;
  }
  
  protected Connection getResponseConnection( Element rootNode ) throws XMLStreamException, AxisFault {
    return (Connection) getResponseObject( Connection.class, rootNode );
  }
  
  protected DatabaseConnection getResponseDatabaseConnection( Element rootNode ) throws XMLStreamException, AxisFault {
    DatabaseConnection result = (DatabaseConnection) getResponseObject( DatabaseConnection.class, rootNode );
    String accessTypeValue = result.getAccessTypeValue();
    DatabaseAccessType accessType = result.getAccessType();
    if( accessType == null || !accessType.getValue().equals(accessTypeValue)) {
      result.setAccessType(DatabaseAccessType.valueOf(accessTypeValue));
    }
    return result;
  }
  
  protected Object getResponseObject( Class clazz, Element rootNode ) throws XMLStreamException, AxisFault{
    Object results[] = getResponseObjects( new Object[] { clazz }, rootNode );
    if( results == null || results.length == 0 ) {
      return null;
    }
    return results[0];
  }
  
  protected Object[] getResponseArray( Class clazz, Element rootNode ) throws XMLStreamException, AxisFault {
    
    // find out how many responses there are
    List nodes = rootNode.selectNodes( "*/*/*" ); //$NON-NLS-1$
    int count = nodes.size();
    Object types[] = new Object[count];
    for( int idx=0; idx<count; idx++ ) {
      types[idx] = clazz;
    }
    return getResponseObjects( types, rootNode );
  }

  protected Object[] getResponseObjects( Object[] types, Element rootNode ) throws XMLStreamException, AxisFault {
    ByteArrayInputStream in = new ByteArrayInputStream( rootNode.asXML().getBytes() );
    XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(in);

    StAXOMBuilder builder = new StAXOMBuilder(parser);
    //get the root element (in this case the envelope)
    OMElement omElement =  builder.getDocumentElement();
    OMElement bodyElement = omElement.getFirstElement(); 
    OMElement responseElement = bodyElement.getFirstElement();
    return BeanUtil.deserialize( responseElement, types, this );
  }
  
  public DatabaseConnection deserializeDatabaseConnection(Element rootNode) throws XMLStreamException, AxisFault {

    System.out.println(rootNode.asXML());
    ByteArrayInputStream in = new ByteArrayInputStream( rootNode.asXML().getBytes() );
    XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(in);

    StAXOMBuilder builder = new StAXOMBuilder(parser);
    //get the root element (in this case the envelope)
    OMElement omElement =  builder.getDocumentElement();
    OMElement bodyElement = omElement.getFirstElement(); 
    OMElement responseElement = bodyElement.getFirstElement(); 
    Object types[] = new Object[] { DatabaseConnection.class };
    Object results[] = BeanUtil.deserialize( responseElement, types, this );
    if( results == null || results.length == 0 ) {
      return null;
    }
    return (DatabaseConnection) results[0];
  }
  
  public Connection convertToConnection(IDatabaseConnection arg0) throws ConnectionServiceException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Deletes a connection from the server's configuration.
   * Returns true if the attempt was successful.
   * setHost(), setUserId() and setPassword() must be called before this
   * method is called.
   * @param connection The connection to be deleted
   * @return True if the deletion was successful
   */
  public boolean deleteConnection(Connection connection) throws ConnectionServiceException {
    return deleteConnection( connection.getName() );
  }

  /**
   * Deletes a connection from the server's configuration.
   * Returns true if the attempt was successful.
   * setHost(), setUserId() and setPassword() must be called before this
   * method is called.
   * @param connectionName The name of the connection to be deleted
   * @return True if the deletion was successful
   */
  public boolean deleteConnection(String connectionName) throws ConnectionServiceException {
    GetMethod callMethod = new GetMethod( serviceUrl+"/deleteConnectionByName" ); //$NON-NLS-1$
    callMethod.setQueryString("name="+connectionName); //$NON-NLS-1$
    
    // get the result and parse de-serialize it
    Node node = getResultNode( callMethod );
    return node != null && Boolean.parseBoolean( this.getNodeText(node) );
  }

  /**
   * Returns a specified connection. Returns null is the connection is not
   * defined in the server's configuration.
   * setHost(), setUserId() and setPassword() must be called before this
   * method is called.
   * @param connectionName The name of the connection to be returned
   * @return The connection requested
   */
  public Connection getConnectionByName(String connectionName) throws ConnectionServiceException {
    PostMethod callMethod = new PostMethod( serviceUrl+"/getConnectionByName" ); //$NON-NLS-1$

    // add the xml to the request entity
    String xml = getRequestXml( new Parameter( "name", connectionName )  ); //$NON-NLS-1$
    RequestEntity requestEntity = new StringRequestEntity( xml ); 
    callMethod.setRequestEntity( requestEntity );

    // get the result and parse de-serialize it
    Document resultDoc = getResultDocument( callMethod );
    
    try {
      return getResponseConnection( resultDoc.getRootElement() );
    } catch ( XMLStreamException e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    } catch ( AxisFault e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    } catch ( Exception e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    }
 }

  /**
   * Generates a SOAP request for an Axis service.
   * @param params
   * @return
   */
  protected String getRequestXml( Parameter...  params ) {
    
    Document doc = DocumentHelper.createDocument();
    
    Element envelopeNode = DocumentHelper.createElement( "soapenv:Envelope" ); //$NON-NLS-1$
    envelopeNode.addAttribute("xmlns:soapenv", "http://www.w3.org/2003/05/soap-envelope"); //$NON-NLS-1$ //$NON-NLS-2$
    envelopeNode.addAttribute("xmlns:wsa", "http://www.w3.org/2005/08/addressing"); //$NON-NLS-1$ //$NON-NLS-2$
    envelopeNode.addAttribute("xmlns:pho", "http://impl.service.wizard.datasource.dataaccess.platform.pentaho.org"); //$NON-NLS-1$ //$NON-NLS-2$
    doc.add( envelopeNode );
    // create a Body node
    Element bodyNode = DocumentHelper.createElement( "soapenv:Body" ); //$NON-NLS-1$
    envelopeNode.add( bodyNode );

    if( params == null || params.length == 0 ) {
      return doc.asXML();
    }
    
    // create a parameter called 'parameters'
    Element parametersNode = DocumentHelper.createElement( "pho:parameters" ); //$NON-NLS-1$
    bodyNode.add(parametersNode);

    for( Parameter param : params ) {
      // create a parameter called 'name'
      Element nameNode = DocumentHelper.createElement( "pho:"+param.getName() ); //$NON-NLS-1$
      parametersNode.add(nameNode);
      nameNode.setText(param.getValue().toString());
      nameNode.addAttribute("type", param.getValue().getClass().getCanonicalName());  //$NON-NLS-1$
    }
    
    return doc.asXML();
  }
  
  /**
   * Returns a list of connections known to the server. Each one is an IConnection object.
   * Returns an empty list if no connections are defined.
   * setHost(), setUserId() and setPassword() must be called before this
   * method is called.
   * @return List of the connections
   */
  public List<Connection> getConnections() throws ConnectionServiceException {
    
    PostMethod callMethod = new PostMethod( serviceUrl+"/getConnections" ); //$NON-NLS-1$

    String xml = getRequestXml( );
    RequestEntity requestEntity = new StringRequestEntity( xml ); 
    callMethod.setRequestEntity( requestEntity );

    List<Connection> connections = new ArrayList<Connection>();

    // get the result and parse de-serialize it
    Document resultDoc = getResultDocument( callMethod );
    
    try {
      Object connectionArray[] = getResponseArray(Connection.class, resultDoc.getRootElement() );
      for( Object connection : connectionArray ) {
        connections.add( (Connection) connection );
      }
    } catch ( XMLStreamException e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    } catch ( AxisFault e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    } catch ( Exception e ) {
      e.printStackTrace();
      throw new ConnectionServiceException(e);
    }
    
    return connections;
  }

  /**
   * Returns the text with the provided node. If the node is null, a null String is returned.
   * @param node Dom4J node
   * @return Text within the node
   */
  protected String getNodeText( Node node ) {
    if( node == null ) {
      return null;
    } else {
      return node.getText();
    }
  }
  
  /**
   * Returns XML for a connection object suitable for submitting to the connection web service
   * @param connection Connection object to be encoded as XML
   * @return XML serialization of the connection object
   */
  protected String getConnectionXml( Connection connection ) {
    Document doc = DocumentHelper.createDocument();
    
    // create a SOAP envelope and specify namespaces
    Element envelopeNode = DocumentHelper.createElement( "soapenv:Envelope" ); //$NON-NLS-1$
    envelopeNode.addAttribute("xmlns:soapenv", "http://www.w3.org/2003/05/soap-envelope"); //$NON-NLS-1$ //$NON-NLS-2$
    envelopeNode.addAttribute("xmlns:wsa", "http://www.w3.org/2005/08/addressing"); //$NON-NLS-1$ //$NON-NLS-2$
    envelopeNode.addAttribute("xmlns:pho", "http://impl.service.wizard.datasource.dataaccess.platform.pentaho.org"); //$NON-NLS-1$ //$NON-NLS-2$
    doc.add( envelopeNode );
    // create a Body node
    Element bodyNode = DocumentHelper.createElement( "soapenv:Body" ); //$NON-NLS-1$
    envelopeNode.add( bodyNode );
    // create a parameter called 'connection'
    Element parameterNode = DocumentHelper.createElement( "pho:connection" ); //$NON-NLS-1$
    bodyNode.add(parameterNode);
    // create a Connection node with a type of org.pentaho.platform.dataaccess.datasource.beans.Connection
    Element connectionNode = DocumentHelper.createElement( "pho:Connection" ); //$NON-NLS-1$
    connectionNode.addAttribute("type", "org.pentaho.platform.dataaccess.datasource.beans.Connection"); //$NON-NLS-1$ //$NON-NLS-2$
    parameterNode.add(connectionNode);
    // add the driver class of the connection
    Element node = DocumentHelper.createElement( "driverClass" ); //$NON-NLS-1$
    node.setText( connection.getDriverClass() );
    connectionNode.add(node);
    // add the name of the connection
    node = DocumentHelper.createElement( "name" ); //$NON-NLS-1$
    node.setText( connection.getName() );
    connectionNode.add(node);
    // add the password for the connection
    node = DocumentHelper.createElement( "password" ); //$NON-NLS-1$
    node.setText( connection.getPassword() );
    connectionNode.add(node);
    // add the url of the connection
    node = DocumentHelper.createElement( "url" ); //$NON-NLS-1$
    node.setText( connection.getUrl() );
    connectionNode.add(node);
    // add the user name for the connection
    node = DocumentHelper.createElement( "username" ); //$NON-NLS-1$
    node.setText( connection.getUsername() );
    connectionNode.add(node);
    
    // return the XML
    return doc.asXML();
  }
  
  /**
   * Tests a provided connection on the server. This does not store the 
   * connection on the server, just validates that the connection works
   * in the server's environment.
   * @param connection The connection to be tested
   */
  public boolean testConnection(Connection connection) throws ConnectionServiceException {
    
    String xml = getConnectionXml( connection );
    PostMethod callMethod = new PostMethod( serviceUrl+"/testConnection" ); //$NON-NLS-1$

    // add the xml to the request entity
    RequestEntity requestEntity = new StringRequestEntity( xml ); 
    callMethod.setRequestEntity( requestEntity );
    // get the result and parse de-serialize it
    Node node = getResultNode( callMethod );
    return node != null && Boolean.parseBoolean( this.getNodeText(node) );
  }

  public boolean updateConnection(Connection connection) throws ConnectionServiceException {
    String xml = getConnectionXml( connection );
    PostMethod callMethod = new PostMethod( serviceUrl+"/updateConnection" ); //$NON-NLS-1$

    // add the xml to the request entity
    RequestEntity requestEntity = new StringRequestEntity( xml ); 
    callMethod.setRequestEntity( requestEntity );
    // get the result and parse de-serialize it
    Node node = getResultNode( callMethod );
    return node != null && Boolean.parseBoolean( this.getNodeText(node) );
  }

  /**
   * Sets the host server, port, and context. For example
   * http://server:port/pentaho
   * @param host The host address and context
   */
  public void setHost(String host) {
    // create the service url
    serviceUrl = host+"/content/ws-run/soapConnectionService"; //$NON-NLS-1$
  }

  /**
   * Sets the user id to use to connect to the server
   * @param userId
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Sets the password to use to connect to the server
   * @param userId
   */
  public void setPassword(String password) {
    this.password = password;
  }
  
  protected List getResultNodes( HttpMethod callMethod ) throws ConnectionServiceException {
    Document doc = getResultDocument( callMethod );
    return doc.selectNodes( "//return" ); //$NON-NLS-1$
  }
  
  protected Node getResultNode( HttpMethod callMethod ) throws ConnectionServiceException {
    Document doc = getResultDocument( callMethod );
    return doc.selectSingleNode( "//return" ); //$NON-NLS-1$
  }
  
  /**
   * Submits an HTTP result with the provided HTTPMethod and returns a dom4j document of the
   * response
   * @param callMethod
   * @return
   * @throws ConnectionServiceException
   */
  protected Document getResultDocument( HttpMethod callMethod ) throws ConnectionServiceException {
    
    try {
      HttpClient client = getClient();
      // execute the HTTP call
      int status = client.executeMethod(callMethod);
      if( status != HttpStatus.SC_OK) {
        throw new ConnectionServiceException("Web service call failed with code "+status); //$NON-NLS-1$
      }
      // get the result as a string
      InputStream in = callMethod.getResponseBodyAsStream();
      byte buffer[] = new byte[2048];
      int n = in.read(buffer);
      StringBuilder sb = new StringBuilder();
      while( n != -1 ) {
        sb.append( new String( buffer, 0, n ) );
        n = in.read(buffer);
      }
      String result = sb.toString();
      // convert to XML
      return DocumentHelper.parseText( result );
    } catch (IOException e) {
      throw new ConnectionServiceException(e);
    } catch (DocumentException e) {
      throw new ConnectionServiceException(e);
    }

  }

  /**
   * Returns an object for the specified class. 
   * This is in the ObjectSupplier interface used by
   * the BeanUtil deserialize methods.
   */
  public Object getObject(Class clazz) throws AxisFault {
    
    try {
      System.out.println( clazz.getCanonicalName() );
      Constructor[] cons = clazz.getConstructors();
      if( cons.length > 0 ) {
        return clazz.newInstance();
      }
      return null;
    } catch (IllegalAccessException e) {
      throw new AxisFault( e.getLocalizedMessage(), e );
    } catch (InstantiationException e) {
      throw new AxisFault( e.getLocalizedMessage(), e );
    }
  }
  
  private class Parameter {
    public String name;
    public Object value;
    
    public Parameter( String name, Object value ) {
      this.name = name;
      this.value = value;
    }

    public Object getValue() {
      return value;
    }

    public void setValue(Object value) {
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }
  
}
