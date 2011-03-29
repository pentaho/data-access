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
 * User: nbaker
 * Date: Jul 16, 2010
 */
public class ModelerServiceDebugServlet extends RemoteServiceServlet implements IGwtModelerService {


  private DebugModelerService delegate = new DebugModelerService();
  
  public String serializeModels( Domain domain, String name ) throws Exception {
    try{
      return delegate.serializeModels(domain, name);
    } catch(Exception e){
      e.printStackTrace();
      throw e;
    }
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return delegate.gwtWorkaround(pojo);
  }

  @Deprecated
  public Domain generateDomain( String connectionName, String tableName, String dbType, String query, String datasourceName ) throws Exception {
    throw new UnsupportedOperationException("Old generateDomain is no longer supported in the data access testing environment.");
  }

  @Override
  protected void doUnexpectedFailure(Throwable e) {
    e.printStackTrace();
    super.doUnexpectedFailure(e);
  }

  public Domain loadDomain(String id) throws Exception{
    XmiParser parser = new XmiParser();
    try {
      return parser.parseXmi(new FileInputStream(new File("test-res/test.xmi")));
    } catch (Exception e) {
      e.printStackTrace();
      throw e;
    }
  }

}
