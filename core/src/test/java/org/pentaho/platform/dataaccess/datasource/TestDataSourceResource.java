/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.platform.dataaccess.datasource;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.json.JSONObject;
import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.database.model.IDatabaseConnection;

import javax.ws.rs.client.Client;


public class TestDataSourceResource {
  private static final String CONNECTION_NAME = "TylerSales";

  private static final String add_url =   "http://localhost:8080/pentaho/plugin/data-access/api/connection/add";


  private static final String update_url =   "http://localhost:8080/pentaho/plugin/data-access/api/connection/update";

  private static final String getURL =    "http://localhost:8080/pentaho/plugin/data-access/api/connection/get";

  private static final String delete_url ="http://localhost:8080/pentaho/plugin/data-access/api/connection/delete";

  private static Client client = null;

  public static void main(String[] args) {
    IDatabaseConnection conn = getConnectionByName(CONNECTION_NAME);
    
    if(conn == null)
      add();
    else
      update();
    conn = getConnectionByName(CONNECTION_NAME);
    if(conn != null){
      System.out.println("GetConn "+ conn.getName());
      delete();
    }
  }

  private static void update() {
    init();
    IDatabaseConnection connection = createConnectionObject();
    String conn = (new JSONObject(connection)).toString();
    System.out.println(conn);
    try {

      WebTarget resource = client.target(update_url);
      Response result = resource.request(MediaType.APPLICATION_JSON).post(Entity.entity(conn, MediaType.APPLICATION_JSON),Response.class);
      System.out.println(result);
    } catch (Exception ex) {
      System.out.println("Error in update");
    }
  }

  private static void delete() {
    init();
    WebTarget resource = client.target(delete_url);
    IDatabaseConnection connection = createConnectionObject();
    String conn = (new JSONObject(connection)).toString();
    System.out.println(conn);
    try {
      Response result = resource
          //.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
          //.accept(MediaType.APPLICATION_JSON_TYPE)
          .request(MediaType.APPLICATION_JSON)
          .post( Entity.entity( conn,MediaType.APPLICATION_JSON ), Response.class );
      System.out.println(result);
    } catch (Exception ex) {
      System.out.println("Error in Delete");
    }
  }

  private static void init() {
    Client client = ClientBuilder.newClient();
    client.register( HttpAuthenticationFeature.basic( "joe", "password" ) );
  }

  private static void add() {
    init();
    IDatabaseConnection connection = createConnectionObject();
    String conn = (new JSONObject(connection)).toString();
    System.out.println(conn);
    try {

      WebTarget resource = client.target(add_url);
      Response result = resource.request(MediaType.APPLICATION_JSON).post( Entity.entity( conn, MediaType.APPLICATION_JSON ),Response.class);
      System.out.println(result);
    } catch (Exception ex) {
      System.out.println("Error in Add");
    }
  }

  private static IDatabaseConnection createConnectionObject() {  
    IDatabaseConnection connection = new DatabaseConnection();
    connection.setAccessType(DatabaseAccessType.NATIVE);
//    connection.setDriverClass("org.hsqldb.jdbcDriver");
//    connection.setUrl("jdbc:hsqldb:hsql://localhost:9001/sampledata");
    connection.setUsername("pentaho_user");
    connection.setPassword("password");
    connection.setName(CONNECTION_NAME);
    return connection;
  }

  private static IDatabaseConnection getConnectionByName(String aConnecitonName) {
    Client client= ClientBuilder.newClient( new ClientConfig() );
    IDatabaseConnection connection = null;
    WebTarget resource = client.target(getURL);
    try {
      connection = resource.request(MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML)
              .post( Entity.entity( aConnecitonName, MediaType.APPLICATION_XML ), DatabaseConnection.class );
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return connection;
  }
}
