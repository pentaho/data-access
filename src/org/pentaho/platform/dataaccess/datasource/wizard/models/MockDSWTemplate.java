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
import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWException;
import org.pentaho.platform.dataaccess.datasource.wizard.exception.DSWExistingFileException;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

/**
 * This class serves as a place holder for real DSWTemplates until they can be developed.
 * 
 * @author tkafalas
 * 
 */
public class MockDSWTemplate extends AbstractDSWTemplate {
  protected Domain domain;
  private IMetadataDomainRepository metadataDomainRepository = PentahoSystem.get( IMetadataDomainRepository.class,
      PentahoSessionHolder.getSession() );

  private MockDSWTemplateModel mockDSWTemplateModel;

  public MockDSWTemplate( String id, String displayName ) {
    super( id, displayName );
    this.mockDSWTemplateModel = new MockDSWTemplateModel( id );
  }

  @Override
  public IDSWTemplateModel deserialize( String serializedModel ) throws DSWException {
    return mockDSWTemplateModel;
  }

  @Override
  public String serialize( IDSWTemplateModel dswTemplateModel ) {
    return "serialized " + mockDSWTemplateModel.getMockData();
  }

  @Override
  public void createDatasource( IDSWDataSource iDSWDataSource, boolean overwrite ) throws DSWException {
    domain = new Domain();
    domain.setId( iDSWDataSource.getName() );
    LogicalModel logicalModel = MockLogicalModel.buildDefaultModel();
    domain.addLogicalModel( logicalModel );

    try {
      metadataDomainRepository.storeDomain( domain, overwrite );
    } catch ( DomainIdNullException e ) {
      throw new DSWException( "Domain was null" );
    } catch ( DomainAlreadyExistsException e ) {
      throw new DSWExistingFileException();
    } catch ( DomainStorageException e ) {
      throw new DSWException( "Failure in metadata layer" );
    }
  }
}
