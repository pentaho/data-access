package org.pentaho.platform.dataaccess.datasource.wizard.service.agile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.platform.api.data.DatasourceServiceException;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.services.connection.datasource.dbcp.JndiDatasourceService;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class AgileHelper {

  public static final String PLUGIN_NAME = "data-access"; //$NON-NLS-1$
  private static final String STAGING_SCHEMA = "data-access-staging-schema"; //$NON-NLS-1$
  private static final String STAGING_JNDI = "data-access-staging-jndi"; //$NON-NLS-1$
  private static final String FILES_PATH = "data-access-files-path"; //$NON-NLS-1$
  private static final String TEMP_FILES_PATH = "data-access-tmp-files-path"; //$NON-NLS-1$
  private static final String SETTINGS_FILE = PLUGIN_NAME + "/settings.xml"; //$NON-NLS-1$  
  private static final String DATASOURCE_SOLUTION_STORAGE = "data-access-datasource-solution-storage" ; //$NON-NLS-1$
  private static final String CSV_SAMPLE_SIZE = "data-access-csv-sample-rows";
  private static final Log logger = LogFactory.getLog(AgileHelper.class);

  private static long contentLength = 0;
  
  public static String getSchemaName() {
    return PentahoSystem.getSystemSetting(SETTINGS_FILE, STAGING_SCHEMA, null);
  }
  
  public static String getDatasourceSolutionStorage() {
    return PentahoSystem.getSystemSetting(SETTINGS_FILE, DATASOURCE_SOLUTION_STORAGE, "admin");
  }

  public static String getDialect(DatabaseMeta meta) {
    String dialect = null;
    try {
      Connection conn = getConnection(getJndiName());
      dialect = conn.getMetaData().getDatabaseProductName();
      if (dialect.indexOf("HSQL") >= 0) {
        dialect = "Hypersonic";
      }
      conn.close();
    } catch (SQLException e) {
      logger.debug("Error determining database type from connection", e);
    } catch (DatasourceServiceException e) {
      logger.debug("Error determining database type from connection - getting JNDI connection", e);
    }
    return dialect;
  }

  public static String generateTableName( String filename ) {
    // TODO add other replacements to guarantee a good table name
    return filename.replace('.', '_');
  }
  
  public static String getJndiName() {
    return PentahoSystem.getSystemSetting(SETTINGS_FILE, STAGING_JNDI, null);
  }

  public static int getCsvSampleRowSize() {
    String sampleSize = PentahoSystem.getSystemSetting(SETTINGS_FILE, CSV_SAMPLE_SIZE, null);
    if (sampleSize != null) {
      return Integer.valueOf(sampleSize);
    } else {
      return 100;
    }    
  }

  public static DatabaseMeta getDatabaseMeta() {
    // get the database settings from configuration
    String jndi = getJndiName();
    DatabaseMeta databaseMeta = new DatabaseMeta();
    databaseMeta.setAccessType( DatabaseMeta.TYPE_ACCESS_JNDI );
    databaseMeta.setDBName( jndi );
    databaseMeta.setName( jndi );    
    String dialect = getDialect(databaseMeta);
    databaseMeta.setDatabaseType( dialect );


    databaseMeta.setQuoteAllFields(true);

    return databaseMeta;
  }


  public static Connection getConnection(String jndiName) throws DatasourceServiceException, SQLException {
    JndiDatasourceService jndiService = new JndiDatasourceService();
    DataSource ds = jndiService.getDataSource(jndiName);
    return ds.getConnection();
  }

  public static String getFolderPath( String project ) {
    
    String folderPath = PentahoSystem.getSystemSetting(SETTINGS_FILE, FILES_PATH, null);
    if( folderPath != null ) {
      folderPath = PentahoSystem.getApplicationContext().getSolutionPath(folderPath+project);
    }
    return folderPath;

  }
  
  public static String getTmpFolderPath( String project ) {
    
    String folderPath = PentahoSystem.getSystemSetting(SETTINGS_FILE, TEMP_FILES_PATH, null);
    if( folderPath != null ) {
      folderPath = PentahoSystem.getApplicationContext().getSolutionPath(folderPath+project);
    }
    return folderPath;

  }

  public static String getProjectMetadataFolder( String project ) {
    return project+"/resources/metadata";
  }


}
