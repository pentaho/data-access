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
* Copyright (c) 2002-2017 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.BaseModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.IModelerSource;
import org.pentaho.agilebi.modeler.ModelerMessagesHolder;
import org.pentaho.agilebi.modeler.ModelerPerspective;
import org.pentaho.agilebi.modeler.ModelerWorkspace;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.gwt.GwtModelerWorkspaceHelper;
import org.pentaho.agilebi.modeler.services.IModelerService;
import org.pentaho.agilebi.modeler.util.SpoonModelerMessages;
import org.pentaho.agilebi.modeler.util.TableModelerSource;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metadata.automodel.PhysicalTableImporter;
import org.pentaho.metadata.automodel.importing.strategy.CsvDatasourceImportStrategy;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.SqlPhysicalModel;
import org.pentaho.metadata.model.thin.Column;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.metadata.util.MondrianModelExporter;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.agile.AgileHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.InlineSqlModelerSource;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.security.SecurityHelper;
import org.pentaho.platform.plugin.action.kettle.KettleSystemListener;
import org.pentaho.platform.plugin.action.mondrian.catalog.IMondrianCatalogService;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCatalog;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianCube;
import org.pentaho.platform.plugin.action.mondrian.catalog.MondrianSchema;

/**
 * User: nbaker Date: Jul 16, 2010
 */
public class ModelerService extends PentahoBase implements IModelerService {

  private static final long serialVersionUID = 1L;
  private static final Log logger = LogFactory.getLog( ModelerService.class );
  public static final String TMP_FILE_PATH =
    File.separatorChar + "system" + File.separatorChar + File.separatorChar + "tmp" + File.separatorChar;
  private SimpleDataAccessPermissionHandler dataAccessPermHandler;
  private DSWDatasourceServiceImpl datasourceService;

  public ModelerService() {
    super();
    dataAccessPermHandler = new SimpleDataAccessPermissionHandler();
  }

  public Log getLogger() {
    return logger;
  }

  static {
    try {
      // try to set the modelermessages.  at this point, just give it the spoon messages.  no need to give it
      // GWT messages, this is a server-side function.
      ModelerMessagesHolder.setMessages( new SpoonModelerMessages() );
    } catch ( IllegalStateException e ) {
      logger.debug( e.getMessage(), e );
    }
  }

  protected void initKettle() {

    try {
      KettleSystemListener.environmentInit( PentahoSessionHolder.getSession() );
      if ( Props.isInitialized() == false ) {
        Props.init( Props.TYPE_PROPERTIES_EMPTY );
      }
    } catch ( KettleException e ) {
      logger.error( e );
      throw new IllegalStateException( "Failed to initialize Kettle system" ); //$NON-NLS-1$
    }
  }

  //TODO: remove this method in favor so specific calls
  @Deprecated
  public Domain generateDomain( String connectionName, String tableName, String dbType, String query,
                                String datasourceName ) throws Exception {
    initKettle();
    try {
      DatabaseMeta database = AgileHelper.getDatabaseMeta();
      IModelerSource source;
      if ( tableName != null ) {
        source = new TableModelerSource( database, tableName, null, datasourceName );
      } else {
        source = new InlineSqlModelerSource( connectionName, dbType, query, datasourceName );
      }
      return source.generateDomain();
    } catch ( Exception e ) {
      logger.error( e );
      throw new Exception( e.getLocalizedMessage() );
    }
  }

  /**
   * Use {@link ModelerService#generateCSVDomain(ModelInfo)} instead,
   * as ModelInfo object contains information about csv column names,
   * provided by user, that are not always the same as the names of columns,
   * stored in database. (see BISERVER-13026 for more info)
   *
   */
  @Deprecated
  public Domain generateCSVDomain( String tableName, String datasourceName ) throws Exception {
    initKettle();
    try {
      DatabaseMeta database = AgileHelper.getDatabaseMeta();
      IModelerSource source = new TableModelerSource( database, tableName, null, datasourceName );
      return source.generateDomain();
    } catch ( Exception e ) {
      logger.error( e );
      throw new Exception( e.getLocalizedMessage() );
    }
  }

  public Domain generateCSVDomain( ModelInfo modelInfo ) throws Exception {
    initKettle();
    try {
      DatabaseMeta database = getDatabaseMeta();
      final String tableName = modelInfo.getStageTableName();
      final String datasourceName = modelInfo.getDatasourceName();

      Column[] columns = toColumns( modelInfo.getColumns() );
      PhysicalTableImporter.ImportStrategy importStrategy = new CsvDatasourceImportStrategy( columns );
      TableModelerSource source = createTableModelerSource( database, tableName, null, datasourceName );

      return source.generateDomain( importStrategy );
    } catch ( Exception e ) {
      logger.error( e );
      throw new Exception( e.getLocalizedMessage() );
    }
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return new BogoPojo();
  }

  public String serializeModels( final Domain domain, final String name ) throws Exception {
    return serializeModels( domain, name, true );
  }

  public String serializeModels( final Domain domain, final String name, final boolean doOlap ) throws Exception {
    String domainId = null;
    initKettle();

    if ( dataAccessPermHandler.hasDataAccessPermission( PentahoSessionHolder.getSession() ) ) {
      SecurityHelper.getInstance().runAsSystem( new Callable<Void>() {

        @Override
        public Void call() throws Exception {

          try {
            if ( datasourceService == null ) {
              datasourceService = new DSWDatasourceServiceImpl();
            }

            ModelerWorkspace model = new ModelerWorkspace( new GwtModelerWorkspaceHelper(), datasourceService
              .getGeoContext() );
            model.setModelName( name );
            model.setDomain( domain );

            if ( name.endsWith( ".xmi" ) ) {
              domain.setId( name );
            } else {
              domain.setId( name + ".xmi" );
            }

            LogicalModel lModel = model.getLogicalModel( ModelerPerspective.ANALYSIS );
            if ( lModel == null ) {
              lModel = model.getLogicalModel( ModelerPerspective.REPORTING );
            }
            lModel.setProperty( "AGILE_BI_GENERATED_SCHEMA", "TRUE" );
            lModel.setProperty( "WIZARD_GENERATED_SCHEMA", "TRUE" );

            String catName = lModel.getName( Locale.getDefault().toString() );

            // strip off the _olap suffix for the catalog ref
            catName = catName.replace( BaseModelerWorkspaceHelper.OLAP_SUFFIX, "" );

            if ( doOlap ) {
              lModel.setProperty( "MondrianCatalogRef", catName ); //$NON-NLS-1$
            }

            // Stores metadata into JCR.
            IMetadataDomainRepository metadataDomainRep = PentahoSystem.get( IMetadataDomainRepository.class );
            if ( metadataDomainRep != null ) {
              metadataDomainRep.storeDomain( model.getDomain(), true );
            }
            // Serialize domain to olap schema.
            if ( doOlap ) {
              MondrianModelExporter exporter = new MondrianModelExporter( lModel, Locale.getDefault().toString() );
              String mondrianSchema = exporter.createMondrianModelXML();
              IPentahoSession session = PentahoSessionHolder.getSession();
              if ( session != null ) {
                // first remove the existing schema, including any
                // model annotations which may have been previously applied
                IMondrianCatalogService mondrianCatalogService =
                    PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", session ); //$NON-NLS-1$

                // try to get the current catalog
                MondrianCatalog currentCatalog = mondrianCatalogService.getCatalog( catName, session );

                // if current catalog exists, remove it
                if ( currentCatalog != null ) {
                  mondrianCatalogService.removeCatalog( catName, session );
                }

                session.setAttribute( "MONDRIAN_SCHEMA_XML_CONTENT", mondrianSchema );
                String catConnectStr = "Provider=mondrian;DataSource=\"" + getMondrianDatasource( domain ) + "\""; //$NON-NLS-1$
                addCatalog( catName, catConnectStr, session );
              }
            }

          } catch ( Exception e ) {
            logger.error( e );
            throw e;
          }
          return null;
        }
      } );
    }
    return domainId;
  }

  public static String getMondrianDatasource( Domain domain ) {
    return ( (SqlPhysicalModel) domain.getPhysicalModels().get( 0 ) ).getId();
  }

  private void addCatalog( String catName, String catConnectStr, IPentahoSession session ) {

    IMondrianCatalogService mondrianCatalogService =
      PentahoSystem.get( IMondrianCatalogService.class, "IMondrianCatalogService", session ); //$NON-NLS-1$

    String dsUrl = PentahoSystem.getApplicationContext().getBaseUrl();
    if ( !dsUrl.endsWith( "/" ) ) { //$NON-NLS-1$
      dsUrl += "/"; //$NON-NLS-1$
    }
    dsUrl += "Xmla"; //$NON-NLS-1$

    MondrianCatalog cat = new MondrianCatalog(
      catName,
      catConnectStr,
      "",
      new MondrianSchema( catName, new ArrayList<MondrianCube>() )
    );

    mondrianCatalogService.addCatalog( cat, true, session );
  }

  public Domain loadDomain( String id ) throws Exception {
    IMetadataDomainRepository repo = PentahoSystem.get( IMetadataDomainRepository.class );
    return repo.getDomain( id );
  }

  public DSWDatasourceServiceImpl getDatasourceService() {
    return datasourceService;
  }

  public void setDatasourceService( DSWDatasourceServiceImpl datasourceService ) {
    this.datasourceService = datasourceService;
  }

  Column[] toColumns( ColumnInfo[] columnInfos ) {
    Column[] columns = new Column[ columnInfos.length ];
    for ( int i = 0; i < columnInfos.length; i++ ) {
      ColumnInfo columnInfo = columnInfos[ i ];
      final String id = columnInfo.getId();
      final String title = columnInfo.getTitle();
      if ( id == null || title == null ) {
        continue;
      }
      Column column = new Column();
      column.setId( id );
      column.setName( title );

      columns[ i ] = column;
    }

    return columns;
  }

  /**
   * For testing
   */
  DatabaseMeta getDatabaseMeta() {
    return AgileHelper.getDatabaseMeta();
  }

  /**
   * For testing
   */
  TableModelerSource createTableModelerSource( DatabaseMeta database, String tableName, String schemaName,
                                               String datasourceName ) {
    return new TableModelerSource( database, tableName, schemaName, datasourceName );
  }


}
