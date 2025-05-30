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


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.agilebi.modeler.geo.GeoContext;
import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.agilebi.modeler.util.MultiTableModelerSource;
import org.pentaho.database.dialect.PDIDialect;
import org.pentaho.database.model.DatabaseType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.util.DatabaseUtil;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.model.concept.Concept;
import org.pentaho.metadata.model.concept.security.Security;
import org.pentaho.metadata.model.concept.security.SecurityOwner;
import org.pentaho.metadata.util.SerializationService;
import org.pentaho.platform.dataaccess.datasource.utils.DataAccessPermissionUtil;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException;
import org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtJoinSelectionService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.ConnectionServiceHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.LegacyDatasourceConverter;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryDatasourceSummary;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;

import com.thoughtworks.xstream.XStream;

public class MultitableDatasourceService extends PentahoBase implements IGwtJoinSelectionService {

  private DatabaseMeta databaseMeta;
  private ConnectionServiceImpl connectionServiceImpl;
  private Log logger = LogFactory.getLog( MultitableDatasourceService.class );

  public MultitableDatasourceService() {
    this.connectionServiceImpl = new ConnectionServiceImpl();
    init();
  }

  public MultitableDatasourceService( DatabaseMeta databaseMeta ) {
    this.databaseMeta = databaseMeta;
    init();
  }

  protected void init() {
  }

  private boolean isDataServicesConnection( IDatabaseConnection connection ) {
    return connection.getDatabaseType().getName().equals( new PDIDialect().getDatabaseType().getName() );
  }

  private DatabaseMeta getDatabaseMeta( IDatabaseConnection connection ) throws ConnectionServiceException {
    if ( this.connectionServiceImpl == null ) {
      return this.databaseMeta;
    }

    // DatabaseConnection objects may be de-serialized from the client and missing extra parameters and attributes.
    // Resolve the connection by name through ConnectionService before use.
    // All public methods should use getDatabaseMeta to guarantee accurate connection info.
    // NOTE: We want to retrieve the connection again later, so we don't want an unsanitized name here
    connection = connectionServiceImpl.getConnectionByName( connection.getName(), false );
    connection
      .setPassword( ConnectionServiceHelper.getConnectionPassword( connection.getName(), connection.getPassword() ) );
    DatabaseMeta dbmeta = DatabaseUtil.convertToDatabaseMeta( connection );
    dbmeta.getDatabaseInterface().setQuoteAllFields(
      true ); //This line probably shouldn't be here.  It overrides the "Quote all in Database" checkbox
    return dbmeta;
  }

  public List<String> retrieveSchemas( IDatabaseConnection connection ) throws DatasourceServiceException {
    List<String> schemas = new ArrayList<String>();
    try {
      DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
      Database database = new Database( null, databaseMeta );
      database.connect();

      Map<String, Collection<String>> tableMap = database.getTableMap( null,
          this.isDataServicesConnection( connection )
              ? new HashMap<String, String>() {{ put( "STREAMING", "N" ); }} : null );

      //database.getSchemas()

      Set<String> schemaNames = tableMap.keySet();
      schemas.addAll( schemaNames );
      database.disconnect();
    } catch ( KettleDatabaseException e ) {
      logger.error( "Error creating database object", e );
      throw new DatasourceServiceException( e );
    } catch ( ConnectionServiceException e ) {
      logger.error( "Error getting database meta", e );
      throw new DatasourceServiceException( e );
    }
    return schemas;
  }

  public List<String> getDatabaseTables( IDatabaseConnection connection, String schema )
      throws DatasourceServiceException {
    try {
      DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
      Database database = new Database( null, databaseMeta );
      database.connect();
      String[] tableNames = database.getTablenames( schema, true,
          this.isDataServicesConnection( connection )
              ? new HashMap<String, String>() {{ put( "STREAMING", "N" ); }} : null );
      List<String> tables = new ArrayList<String>();
      tables.addAll( Arrays.asList( tableNames ) );
      tables.addAll( Arrays.asList( database.getViews( schema, true ) ) );
      database.disconnect();
      return tables;
    } catch ( KettleDatabaseException e ) {
      logger.error( "Error creating database object", e );
      throw new DatasourceServiceException( e );
    } catch ( ConnectionServiceException e ) {
      logger.error( "Error getting database meta", e );
      throw new DatasourceServiceException( e );
    }
  }

  public IDatasourceSummary serializeJoins( MultiTableDatasourceDTO dto, IDatabaseConnection connection )
    throws DatasourceServiceException {
    try {
      ModelerService modelerService = new ModelerService();
      modelerService.initKettle();

      DSWDatasourceServiceImpl datasourceService = new DSWDatasourceServiceImpl();
      GeoContext geoContext = datasourceService.getGeoContext();

      DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
      MultiTableModelerSource multiTable =
        new MultiTableModelerSource( databaseMeta, dto.getSchemaModel(), dto.getDatasourceName(),
          dto.getSelectedTables(), geoContext );
      Domain domain = multiTable.generateDomain( dto.isDoOlap() );

      String modelState = serializeModelState( dto );
      for ( LogicalModel lm : domain.getLogicalModels() ) {
        lm.setProperty( "datasourceModel", modelState );
        lm.setProperty( "DatasourceType", "MULTI-TABLE-DS" );

        // BISERVER-6450 - add security settings to the logical model
        applySecurity( lm );
      }
      modelerService.serializeModels( domain, dto.getDatasourceName(), dto.isDoOlap() );

      QueryDatasourceSummary summary = new QueryDatasourceSummary();
      summary.setDomain( domain );
      return summary;
    } catch ( Exception e ) {
      logger.error( "Error serializing joins", e );
      throw new DatasourceServiceException( e );
    }
  }

  private String serializeModelState( MultiTableDatasourceDTO dto ) throws DatasourceServiceException {
    XStream xs = SerializationService.createXStreamWithAllowedTypes( null, null );
    return xs.toXML( dto );
  }

  public MultiTableDatasourceDTO deSerializeModelState( String dtoStr ) throws DatasourceServiceException {
    try {
      XStream xs = SerializationService.createXStreamWithAllowedTypes( null, MultiTableDatasourceDTO.class, SchemaModel.class,
        DatabaseType.class, JoinFieldModel.class, JoinRelationshipModel.class, JoinTableModel.class );
      xs.registerConverter( new LegacyDatasourceConverter() );
      return (MultiTableDatasourceDTO) xs.fromXML( dtoStr );
    } catch ( Exception e ) {
      logger.error( e );
      throw new DatasourceServiceException( e );
    }
  }

  private static String[] getSchemaTablePair( DatabaseMeta db, String table ) {
    // This is clunky, but it's better than rewriting all
    // of the pojos and interfaces. Unfortunately, the schema
    // name is scotched to the table pretty quick in the process.
    // Fortunately, since we built this serialized form, we know
    // how to undo it.
    if ( table.indexOf( "." ) < 0 ) {
      return new String[] { "", table };
    }
    String[] pair = new String[ 2 ];
    String[] parts = table.split( "\\." );
    pair[ 0 ] = parts[ 0 ];

    String tableName = "";
    for ( int i = 1; i < parts.length; i++ ) {
      tableName = tableName + "." + parts[ i ];
    }
    pair[ 1 ] = tableName.substring( 1 );

    pair[ 0 ] = pair[ 0 ].replaceAll( db.getStartQuote(), "" );
    pair[ 0 ] = pair[ 0 ].replaceAll( db.getEndQuote(), "" );

    pair[ 1 ] = pair[ 1 ].replaceAll( db.getStartQuote(), "" );
    pair[ 1 ] = pair[ 1 ].replaceAll( db.getEndQuote(), "" );

    return pair;
  }

  public List<String> getTableFields( String table, IDatabaseConnection connection ) throws DatasourceServiceException {
    try {
      DatabaseMeta databaseMeta = this.getDatabaseMeta( connection );
      Database database = new Database( null, databaseMeta );

      try {
        database.connect();

        String[] schemaTablePair = getSchemaTablePair( databaseMeta, table );
        RowMetaInterface fieldsMeta = database.getTableFieldsMeta( schemaTablePair[0], schemaTablePair[1] );

        List<String> fields = new ArrayList<>();
        for ( int i = fieldsMeta.size() - 1; i >= 0; i-- ) {
          ValueMetaInterface field = fieldsMeta.getValueMeta( i );
          fields.add( field.getName() );
        }

        return fields;
      } finally {
        database.disconnect();
      }
    } catch ( KettleDatabaseException e ) {
      logger.error( e );
      throw new DatasourceServiceException( e );
    } catch ( ConnectionServiceException e ) {
      logger.error( e );
      throw new DatasourceServiceException( e );
    }
  }

  public BogoPojo gwtWorkaround( BogoPojo pojo ) {
    return pojo;
  }

  @Override
  public Log getLogger() {
    // TODO Auto-generated method stub
    return null;
  }

  protected boolean hasDataAccessViewPermission() {
    return DataAccessPermissionUtil.hasViewAccess();
  }

  protected List<String> getPermittedRoleList() {
    return DataAccessPermissionUtil.getPermittedViewRoleList();
  }

  protected List<String> getPermittedUserList() {
    return DataAccessPermissionUtil.getPermittedViewUserList();
  }

  protected int getDefaultAcls() {
    return DataAccessPermissionUtil.getDataAccessViewPermissionHandler().getDefaultAcls(
      PentahoSessionHolder.getSession() );
  }

  protected boolean isSecurityEnabled() {
    Boolean securityEnabled = ( getPermittedRoleList() != null && getPermittedRoleList().size() > 0 )
      || ( ( getPermittedUserList() != null && getPermittedUserList().size() > 0 ) );
    return securityEnabled;
  }

  protected void applySecurity( LogicalModel logicalModel ) {
    if ( isSecurityEnabled() ) {
      Security security = new Security();
      for ( String user : getEffectivePermittedUserList( isSecurityEnabled() ) ) {
        SecurityOwner owner = new SecurityOwner( SecurityOwner.OwnerType.USER, user );
        security.putOwnerRights( owner, getDefaultAcls() );
      }
      for ( String role : getPermittedRoleList() ) {
        SecurityOwner owner = new SecurityOwner( SecurityOwner.OwnerType.ROLE, role );
        security.putOwnerRights( owner, getDefaultAcls() );
      }
      logicalModel.setProperty( Concept.SECURITY_PROPERTY, security );
    }
  }

  // Add user to list if not already present
  private List<String> getEffectivePermittedUserList( boolean securityEnabled ) {
    ArrayList<String> permittedUserList =
      getPermittedUserList() == null ? new ArrayList<String>() : new ArrayList<String>( getPermittedUserList() );
    if ( securityEnabled ) {
      if ( !permittedUserList.contains( PentahoSessionHolder.getSession().getName() ) ) {
        permittedUserList.add( PentahoSessionHolder.getSession().getName() );
      }
    }
    return permittedUserList;
  }

}
