/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License, version 2 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 *
 * Copyright 2006 - 2013 Pentaho Corporation.  All rights reserved.
 */

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import org.pentaho.metadata.model.Domain;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.metadata.repository.DomainAlreadyExistsException;
import org.pentaho.metadata.repository.DomainIdNullException;
import org.pentaho.metadata.repository.DomainStorageException;
import org.pentaho.metadata.repository.IMetadataDomainRepository;
import org.pentaho.platform.dataaccess.datasource.wizard.api.DSWException;
import org.pentaho.platform.dataaccess.datasource.wizard.api.DSWExistingFileException;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWDataSource;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWModelStorage;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWTemplate;
import org.pentaho.platform.dataaccess.datasource.wizard.api.IDSWTemplateModel;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * This class contains the logic of what to do with a Serialized <code>IDSWTemplateModel</code> to persist it to the
 * repository. Conversely, it also provides the logic to load the serialized model from the repository, and use the
 * <code>IDSWTemplate</code> to deserialize back into the <code>IDSWTemplateModel</code> object.
 * 
 * @author tkafalas
 * 
 */
public class DSWModelStorage implements IDSWModelStorage {
  public static final String TEMPLATE_ID_PROPERTY = "DSWTemplate";
  public static final String TEMPLATE_MODEL_PROPERTY = "DSWTemplateModel";
  private IMetadataDomainRepository metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class,
      PentahoSessionHolder.getSession() );

  @Override
  public void storeModel( String serializedModel, IDSWDataSource iDSWDataSource ) throws DSWException {
    try {
      Domain domain = metadataDomainRepository.getDomain( iDSWDataSource.getName() );
      LogicalModel logicalModel = domain.getLogicalModels().get( 0 );
      logicalModel.setProperty( TEMPLATE_ID_PROPERTY, iDSWDataSource.getTemplate().getID() );
      logicalModel.setProperty( TEMPLATE_MODEL_PROPERTY, serializedModel );

      try {
        metadataDomainRepository.storeDomain( domain, true );
      } catch ( DomainIdNullException e ) {
        throw new DSWException( "Domain was null" );
      } catch ( DomainAlreadyExistsException e ) {
        throw new DSWExistingFileException();
      } catch ( DomainStorageException e ) {
        throw new DSWException( "Failure in metadata layer" );
      }
    } catch ( DSWException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new DSWException( "Unexpected Model Storage Failure", e );
    }
  }

  @Override
  public IDSWTemplateModel loadModel( String dataSourceID ) throws DSWException {
    try {
      // Load Domain from repo
      Domain domain = metadataDomainRepository.getDomain( dataSourceID );
      // Get the propeties pertaining to the model
      LogicalModel logicalModel = domain.getLogicalModels().get( 0 );
      String templateID = (String) logicalModel.getProperty( TEMPLATE_ID_PROPERTY );
      String serializedModel = (String) logicalModel.getProperty( TEMPLATE_MODEL_PROPERTY );
      // Get the template and deserialize the model string

      DSWDataSourceWizard dswDataSourceWizard = PentahoSystem.get( DSWDataSourceWizard.class );
      IDSWTemplate iDSWTemplate = dswDataSourceWizard.getTemplateByID( templateID );
      IDSWTemplateModel iDSWTemplateModel = iDSWTemplate.deserialize( serializedModel );
      return iDSWTemplateModel;
    } catch ( DSWException e ) {
      throw e;
    } catch ( Exception e ) {
      throw new DSWException( "Unexpected Model Load Failure", e );
    }
  }
}
