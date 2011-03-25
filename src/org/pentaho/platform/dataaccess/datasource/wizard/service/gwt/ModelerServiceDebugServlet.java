package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.ModelerException;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.services.IGwtModelerService;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.util.XmiParser;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.DebugModelerService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.InMemoryDatasourceServiceImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.CsvDatasourceServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.InlineSqlModelerSource;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.platform.engine.core.system.PentahoSystem;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * User: nbaker
 * Date: Jul 16, 2010
 */
public class ModelerServiceDebugServlet extends RemoteServiceServlet implements IGwtModelerService {

  static{
    if (!PentahoSystem.getInitializedOK()) {
      CsvDatasourceServiceHelper csvHelper = new CsvDatasourceServiceHelper();
      csvHelper.setUp();
    }
    try {
      KettleEnvironment.init();
      Props.init(Props.TYPE_PROPERTIES_EMPTY);      
    } catch (KettleException e) {
      e.printStackTrace();
    }
  }

  private DebugModelerService delegate = new DebugModelerService();
  private IDatasourceService datasourceService = new InMemoryDatasourceServiceImpl();
  
  public String serializeModels( Domain domain, String name ) throws Exception {
    return delegate.serializeModels(domain, name);
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return delegate.gwtWorkaround(pojo);
  }

  public Domain generateDomain( String connectionName, String tableName, String dbType, String query, String datasourceName ) throws Exception {
    try{
      IModelerSource source;
      if(tableName != null){
        DatabaseMeta database = AgileHelper.getDatabaseMeta();
        source = new TableModelerSource(database, tableName, null, datasourceName);
      } else {
        source = new InlineSqlModelerSource(datasourceService, connectionName, dbType, query, datasourceName);
      }
      Domain d = null;
      try {
        d = source.generateDomain();
      } catch (ModelerException e) {
        throw new Exception(e.getMessage(), e.getCause());
      }

      return d;
    } catch(Exception e){
      throw e;
    }
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
