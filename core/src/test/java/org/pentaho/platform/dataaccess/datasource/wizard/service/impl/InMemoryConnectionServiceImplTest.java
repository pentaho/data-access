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
 * Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.DatabaseConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;

public class InMemoryConnectionServiceImplTest {

  @Test
  public void testGetConnections() {
    try {
      InMemoryConnectionServiceImpl serv = new InMemoryConnectionServiceImpl();
      List<IDatabaseConnection> conns = serv.getConnections();
      assertTrue( conns != null && conns.size() == 0 );
      IDatabaseConnection connection = new DatabaseConnection();
      connection.setName( "Connection 1" );
      serv.addConnection( connection );
      conns = serv.getConnections();
      assertTrue( conns != null && conns.size() > 0 );
    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testGetConnectionByName() {
    try {
      InMemoryConnectionServiceImpl serv = new InMemoryConnectionServiceImpl();
      List<IDatabaseConnection> conns = serv.getConnections();
      assertTrue( conns != null && conns.size() == 0 );
      IDatabaseConnection connection = new DatabaseConnection();
      connection.setName( "Connection 1" );
      serv.addConnection( connection );
      connection = new DatabaseConnection();
      connection.setName( "Connection 2" );
      serv.addConnection( connection );
      connection = serv.getConnectionByName( "Connection 2" );
      assertTrue( connection != null && connection.getName().equals( "Connection 2" ) );
      try {
        connection = serv.getConnectionByName( "Connection 5" );
        fail();
      } catch ( Exception ex ) { }
    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testAddConnections() {
    try {
      InMemoryConnectionServiceImpl serv = new InMemoryConnectionServiceImpl();
      List<IDatabaseConnection> conns = serv.getConnections();
      assertTrue( conns != null && conns.size() == 0 );
      IDatabaseConnection connection = new DatabaseConnection();
      connection.setName( "Connection 1" );
      serv.addConnection( connection );
      connection = new DatabaseConnection();
      connection.setName( "Connection 1" );
      try {
        //validate adding connection with existing name
        serv.addConnection( connection );
        fail();
      } catch ( ConnectionServiceException e ) { }
      connection = new DatabaseConnection();
      connection.setName( "Connection 2" );
      serv.addConnection( connection );
      conns = serv.getConnections();
      assertTrue( conns != null && conns.size() > 0 );
    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testUpdateConnection() {
    try {
      InMemoryConnectionServiceImpl serv = new InMemoryConnectionServiceImpl();
      List<IDatabaseConnection> conns = serv.getConnections();
      assertTrue( conns != null && conns.size() == 0 );
      IDatabaseConnection connection = new DatabaseConnection();
      connection.setName( "Connection 1" );
      connection.setUsername( "admin" );
      connection.setPassword( "password" );
      serv.addConnection( connection );
      conns = serv.getConnections();
      assertTrue( conns.get( 0 ).getUsername().equals( "admin" ) );
      assertTrue( conns.get( 0 ).getPassword().equals( "password" ) );
      connection = new DatabaseConnection();
      connection.setName( "Connection 1" );
      connection.setUsername( "root" );
      connection.setPassword( "pass" );
      serv.updateConnection( connection );
      List<IDatabaseConnection> conns1 = serv.getConnections();
      assertTrue( conns1 != null && conns1.size() > 0 );
      assertTrue( conns1.size() == conns.size() );
      assertTrue( conns1.get( 0 ).getUsername().equals( "root" ) );
      assertTrue( conns1.get( 0 ).getPassword().equals( "pass" ) );
    } catch ( Exception ex ) {
      fail();
    }
  }

  @Test
  public void testDeleteConnections() {
    try {
      InMemoryConnectionServiceImpl serv = new InMemoryConnectionServiceImpl();
      List<IDatabaseConnection> conns = serv.getConnections();
      assertTrue( conns != null && conns.size() == 0 );
      IDatabaseConnection connection = new DatabaseConnection();
      connection.setName( "Connection 1" );
      serv.addConnection( connection );
      connection = new DatabaseConnection();
      connection.setName( "Connection 2" );
      serv.addConnection( connection );
      conns = serv.getConnections();
      assertTrue( conns != null && conns.size() == 2 );
      try {
        serv.deleteConnection( connection );
        connection = serv.getConnectionByName( "Connection 2" );
        fail();
      } catch ( Exception ex ) { }
      try {
        serv.deleteConnection( "Connection 1" );
        connection = serv.getConnectionByName( "Connection 1" );
        fail();
      } catch ( Exception ex ) { }
    } catch ( Exception ex ) {
      fail();
    }
  }

}
