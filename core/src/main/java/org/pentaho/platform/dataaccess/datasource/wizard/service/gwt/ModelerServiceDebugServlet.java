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

package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerService;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DebugModelerService;

import java.io.File;
import java.io.FileInputStream;

/**
 * User: nbaker Date: Jul 16, 2010
 */
public class ModelerServiceDebugServlet extends RemoteServiceServlet implements IGwtModelerService {


  private DebugModelerService delegate = new DebugModelerService();

  public String serializeModels( Domain domain, String name ) throws Exception {
    try {
      return delegate.serializeModels( domain, name );
    } catch ( Exception e ) {
      e.printStackTrace();
      throw e;
    }
  }

  @Override
  public String serializeModels( Domain domain, String name, boolean doOlap ) throws Exception {
    try {
      return delegate.serializeModels( domain, name, doOlap );
    } catch ( Exception e ) {
      e.printStackTrace();
      throw e;
    }
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return delegate.gwtWorkaround( pojo );
  }

  @Deprecated
  public Domain generateDomain( String connectionName, String tableName, String dbType, String query,
                                String datasourceName ) throws Exception {
    throw new UnsupportedOperationException(
      "Old generateDomain is no longer supported in the data access testing environment." );
  }

  @Override
  protected void doUnexpectedFailure( Throwable e ) {
    e.printStackTrace();
    super.doUnexpectedFailure( e );
  }

  public Domain loadDomain( String id ) throws Exception {
    XmiParser parser = new XmiParser();
    try {
      return parser.parseXmi( new FileInputStream( new File( "target/test-classes/" + id + ".xmi" ) ) );
    } catch ( Exception e ) {
      e.printStackTrace();
      throw e;
    }
  }

}
