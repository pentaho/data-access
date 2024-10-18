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
* Copyright (c) 2002-2024 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.metadata.service;

import flexjson.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.pentaho.commons.connection.IPentahoResultSet;
import org.pentaho.commons.connection.marshal.MarshallableResultSet;
import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.query.model.util.QueryXmlHelper;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.api.engine.ILogger;
import org.pentaho.platform.dataaccess.datasource.utils.DataAccessPermissionUtil;
import org.pentaho.platform.dataaccess.metadata.messages.Messages;
import org.pentaho.platform.dataaccess.metadata.model.impl.Model;
import org.pentaho.platform.dataaccess.metadata.model.impl.ModelInfo;
import org.pentaho.platform.dataaccess.metadata.model.impl.ModelInfoComparator;
import org.pentaho.platform.dataaccess.metadata.model.impl.Query;
import org.pentaho.platform.engine.core.system.PentahoBase;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.plugin.action.pentahometadata.MetadataQueryComponent;
import org.pentaho.platform.util.messages.LocaleHelper;
import org.pentaho.pms.core.exception.PentahoMetadataException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_XML;

/**
 * An object that makes lightweight, serializable metadata models available to callers, and allow queries to be
 * executed. All objects are simple POJOs. This object can be used as a Axis web service.
 *
 * @author jamesdixon
 */
@Path( "/data-access/api/metadataDA" )
public class MetadataService extends PentahoBase {

  private static final long serialVersionUID = 8481450224870463494L;

  private Log logger = LogFactory.getLog( MetadataService.class );

  public MetadataService() {
    setLoggingLevel( ILogger.ERROR );
  }

  /**
   * Returns a string that indicates whether the current user has access to edit or view metadata models
   *
   * @return
   */

  @GET
  @Path( "/getDatasourcePermissions" )
  @Produces( { APPLICATION_JSON } )
  public String getDatasourcePermissions() {
    boolean canEdit = hasManageAccess();
    boolean canView = hasViewAccess();
    String response = "NONE"; //$NON-NLS-1$

    if ( canEdit ) {
      response = "EDIT"; //$NON-NLS-1$
    } else if ( canView ) {
      response = "VIEW"; //$NON-NLS-1$
    }
    return response;
  }

  /**
   * Returns a list of the available business models
   *
   * @param domainName
   *          optional domain to limit the results
   * @param context
   *          Area to check for model visibility
   * @return list of ModelInfo objects representing the available models
   * @throws IOException
   */
  @GET
  @Path( "/listBusinessModels" )
  @Produces( { APPLICATION_XML } )
  public ModelInfo[] listBusinessModels( @QueryParam( "domainName" ) String domainName,
                                         @QueryParam( "context" ) String context ) throws IOException {

    List<ModelInfo> models = new ArrayList<ModelInfo>();

    // get hold of the metadata repository
    IMetadataDomainRepository repo = getMetadataRepository();
    if ( repo == null ) {
      error( Messages.getErrorString( "MetadataService.ERROR_0001_BAD_REPO" ) ); //$NON-NLS-1$
      return null;
    }

    try {
      if ( StringUtils.isEmpty( domainName ) ) {
        // if no domain has been specified, loop over all of them
        for ( String domain : repo.getDomainIds() ) {
          getModelInfos( domain, context, models );
        }
      } else {
        // get the models for the specified domain
        getModelInfos( domainName, context, models );
      }
    } catch ( Throwable t ) {
      error( Messages.getErrorString( "MetadataService.ERROR_0002_BAD_MODEL_LIST" ), t ); //$NON-NLS-1$
    }

    Collections.sort( models, new ModelInfoComparator() );
    return models.toArray( new ModelInfo[ models.size() ] );
  }

  /**
   * Returns a JSON list of the available business models
   *
   * @param domainName
   *          optional domain to limit the results
   * @param context
   *          Area to check for model visibility
   * @return JSON string of list of ModelInfo objects representing the available models
   * @throws IOException
   */
  public String listBusinessModelsJson( String domainName, String context ) throws IOException {

    ModelInfo[] models = listBusinessModels( domainName, context );
    JSONSerializer serializer = new JSONSerializer();
    String json = serializer.deepSerialize( models );
    return json;
  }

  /**
   * Returns a list of ModelInfo objects for the specified domain. These objects are small and this list is intended to
   * allow a client to provide a list of models to a user so the user can pick which one they want to work with.
   *
   * @param domain
   * @param context
   *          Area to check for model visibility
   * @param models
   */
  private void getModelInfos( final String domain, final String context, List<ModelInfo> models ) {

    IMetadataDomainRepository repo = getMetadataRepository();

    Domain domainObject = repo.getDomain( domain );
    if ( domainObject == null ) {
      // the domain does not exist
      return;
    }

    // find the best locale
    String locale = LocaleHelper.getClosestLocale( LocaleHelper.getLocale().toString(), domainObject.getLocaleCodes() );

    // iterate over all of the models in this domain
    for ( LogicalModel model : domainObject.getLogicalModels() ) {
      String vis = (String) model.getProperty( "visible" );
      if ( vis != null ) {
        String[] visibleContexts = vis.split( "," );
        boolean visibleToContext = false;
        for ( String c : visibleContexts ) {
          if ( StringUtils.isEmpty( context ) || c.equals( context ) ) {
            visibleToContext = true;
            break;
          }
        }
        if ( !visibleToContext ) {
          continue;
        }
      }
      // create a new ModelInfo object and give it the envelope information about the model
      ModelInfo modelInfo = new ModelInfo();
      modelInfo.setDomainId( domain );
      modelInfo.setModelId( model.getId() );
      modelInfo.setModelName( model.getName( locale ).replaceAll( "<", "&lt;" ).replaceAll( ">", "&gt;" )  );
      if ( model.getDescription() != null ) {
        String modelDescription = model.getDescription( locale );
        modelInfo.setModelDescription( modelDescription );
      }
      models.add( modelInfo );
    }
    return;
  }

  /**
   * Returns a Model object for the requested model. The model will include the basic metadata - categories and
   * columns.
   *
   * @param domainId
   * @param modelId
   * @return
   */
  @GET
  @Path( "/loadModel" )
  @Produces( { APPLICATION_XML } )
  public Model loadModel( @QueryParam( "domainId" ) String domainId,
                          @QueryParam( "modelId" ) String modelId ) {

    if ( domainId == null ) {
      // we can't do this without a model
      error( Messages.getErrorString( "MetadataService.ERROR_0003_NULL_DOMAIN" ) ); //$NON-NLS-1$
      return null;
    }

    if ( modelId == null ) {
      // we can't do this without a model
      error( Messages.getErrorString( "MetadataService.ERROR_0004_NULL_Model" ) ); //$NON-NLS-1$
      return null;
    }

    // because it's lighter weight, check the thin model
    Domain domain = getMetadataRepository().getDomain( domainId );
    if ( domain == null ) {
      error( Messages.getErrorString( "MetadataService.ERROR_0005_DOMAIN_NOT_FOUND", domainId ) ); //$NON-NLS-1$
      return null;
    }

    LogicalModel model = domain.findLogicalModel( modelId );

    if ( model == null ) {
      // the model cannot be found or cannot be loaded
      error( Messages.getErrorString( "MetadataService.ERROR_0006_MODEL_NOT_FOUND", modelId ) ); //$NON-NLS-1$
      return null;
    }

    // create the thin metadata model and return it
    MetadataServiceUtil util = getMetadataServiceUtil();
    util.setDomain( domain );
    Model thinModel = util.createThinModel( model, domainId );
    return thinModel;

  }

  /**
   * Returns a JSON Model object for the requested model. The model will include the basic metadata - categories and
   * columns.
   *
   * @param domainId
   * @param modelId
   * @return JSON string of the model
   */
  public String loadModelJson( String domainId, String modelId ) {

    Model model = loadModel( domainId, modelId );
    JSONSerializer serializer = new JSONSerializer();
    String json = serializer.deepSerialize( model );
    return json;
  }

  /**
   * Executes a query model and returns a serializable result set
   *
   * @param query
   * @param rowLimit An optional row limit, -1 or null means all rows
   * @return
   */
  public MarshallableResultSet doQuery( Query query, Integer rowLimit ) {

    MetadataServiceUtil util = getMetadataServiceUtil();
    org.pentaho.metadata.query.model.Query fullQuery = util.convertQuery( query );
    QueryXmlHelper helper = new QueryXmlHelper();
    String xml = helper.toXML( fullQuery );
    return doXmlQuery( xml, rowLimit );
  }

  /**
   * Executes a XML query and returns a serializable result set
   *
   * @param rowLimit An optional row limit, -1 or null means all rows
   * @return
   */
  public MarshallableResultSet doXmlQuery( String xml, Integer rowLimit ) {
    IPentahoResultSet resultSet = executeQuery( xml, rowLimit );
    if ( resultSet == null ) {
      return null;
    }
    MarshallableResultSet result = getMarshallableResultSet();
    result.setResultSet( resultSet );
    return result;
  }

  /**
   * Executes a XML query and returns a JSON serialization of the result set
   *
   * @param rowLimit
   * @return
   */
  public String doXmlQueryToJson( String xml, int rowLimit ) {
    MarshallableResultSet resultSet = doXmlQuery( xml, rowLimit );
    if ( resultSet == null ) {
      return null;
    }
    JSONSerializer serializer = new JSONSerializer();
    String json = serializer.deepSerialize( resultSet );
    return json;
  }

  /**
   * Executes a XML query and returns a CDA compatible JSON serialization of the result set
   *
   * @param rowLimit
   * @return
   */
  @GET
  @Path( "/doXmlQueryToCdaJson" )
  @Produces( { APPLICATION_JSON } )
  public String doXmlQueryToCdaJson( @QueryParam( "xml" ) String xml,
                                     @QueryParam( "rowLimit" ) int rowLimit ) {
    IPentahoResultSet resultSet = executeQuery( xml, rowLimit );
    if ( resultSet == null ) {
      return null;
    }
    String json = null;
    try {
      MetadataServiceUtil util = getMetadataServiceUtil();
      Domain domain = util.getDomainObject( xml );
      util.setDomain( domain );
      String locale = LocaleHelper.getClosestLocale( LocaleHelper.getLocale().toString(), domain.getLocaleCodes() );
      json = util.createCdaJson( resultSet, locale );
    } catch ( JSONException e ) {
      error( Messages.getErrorString( "MetadataService.ERROR_0007_JSON_ERROR" ), e ); //$NON-NLS-1$
    } catch ( PentahoMetadataException e ) {
      error( Messages.getErrorString( "MetadataService.ERROR_0007_BAD_QUERY_DOMAIN" ), e ); //$NON-NLS-1$
    }
    return json;
  }

  /**
   * Executes a XML query and returns a serializable result set
   *
   * @param rowLimit An optional row limit, -1 or null means all rows
   * @return
   */
  public MarshallableResultSet doJsonQuery( String json, Integer rowLimit ) {

    // return the results
    return doXmlQuery( getQueryXmlFromJson( json ), rowLimit );
  }

  /**
   * Executes a XML query and returns a JSON serialization of the result set
   *
   * @param rowLimit
   * @return
   */
  public String doJsonQueryToJson( String json, int rowLimit ) {
    // return the results
    return doXmlQueryToJson( getQueryXmlFromJson( json ), rowLimit );
  }

  /**
   * Executes a XML query and returns a CDA compatible JSON serialization of the result set
   *
   * @param rowLimit
   * @return
   */
  @GET
  @Path( "/doJsonQueryToCdaJson" )
  @Produces( { APPLICATION_JSON } )
  public String doJsonQueryToCdaJson( @QueryParam( "json" ) String json,
                                      @QueryParam( "rowLimit" ) int rowLimit ) {
    // return the results
    return doXmlQueryToCdaJson( getQueryXmlFromJson( json ), rowLimit );
  }


  /**
   * Executes a XML query and returns a native result set
   *
   * @param query
   * @param rowLimit An optional row limit, -1 or null means all rows
   * @return
   */
  protected IPentahoResultSet executeQuery( String query, Integer rowLimit ) {
    // create a component to execute the query
    MetadataQueryComponent dataComponent = new MetadataQueryComponent();
    dataComponent.setQuery( query );
    dataComponent.setLive( false );
    dataComponent.setUseForwardOnlyResultSet( true );
    if ( rowLimit != null && rowLimit > -1 ) {
      // set the row limit
      dataComponent.setMaxRows( rowLimit );
    }
    if ( dataComponent.execute() ) {
      return dataComponent.getResultSet();
    }
    return null;
  }

  /**
   * Converts a JSON query into a full Query object by going via a thin Query object
   *
   * @param json
   * @return
   */
  protected String getQueryXmlFromJson( String json ) {
    MetadataServiceUtil util = getMetadataServiceUtil();
    Query query = util.deserializeJsonQuery( json );
    try {
      // convert the thin query model into a full one
      org.pentaho.metadata.query.model.Query fullQuery = util.convertQuery( query );

      // get the XML for the query
      QueryXmlHelper helper = new QueryXmlHelper();
      String xml = helper.toXML( fullQuery );
      return xml;
    } catch ( Exception e ) {
      error( Messages.getErrorString( "MetadataService.ERROR_0008_BAD_QUERY" ), e ); //$NON-NLS-1$
    }
    return null;
  }

  /**
   * Returns a instance of the IMetadataDomainRepository for the current session
   *
   * @return
   */
  protected IMetadataDomainRepository getMetadataRepository() {
    IMetadataDomainRepository mdr =
      PentahoSystem.get( IMetadataDomainRepository.class, PentahoSessionHolder.getSession() );
    if ( mdr instanceof ILogger ) {
      ( (ILogger) mdr ).setLoggingLevel( getLoggingLevel() );
    }
    return mdr;
  }

  @Override
  public Log getLogger() {
    return logger;
  }

  protected boolean hasManageAccess() {
    return DataAccessPermissionUtil.hasManageAccess();
  }

  protected boolean hasViewAccess() {
    return DataAccessPermissionUtil.hasViewAccess();
  }

  protected MetadataServiceUtil getMetadataServiceUtil() {
    return new MetadataServiceUtil();
  }

  protected MarshallableResultSet getMarshallableResultSet() {
    return new MarshallableResultSet();
  }
}
