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
* Copyright (c) 2002-2016 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.DatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.IXulAsyncDatasourceServiceManager;
import org.pentaho.ui.xul.XulServiceCallback;

public class MetadataUIDatasourceService implements IUIDatasourceAdminService {

  public static final String TYPE = "Metadata";
  private boolean editable = false;
  private boolean removable = true;
  private boolean importable = true;
  private boolean exportable = true;
  private boolean creatable = true;
  private String newUI = "builtin:";
  private String editUI = "builtin:";
  private IXulAsyncDatasourceServiceManager datasourceService;
  private static final String EXT = ".xmi";

  public MetadataUIDatasourceService( IXulAsyncDatasourceServiceManager datasourceService ) {
    this.datasourceService = datasourceService;
  }

  @Override
  public String getType() {
    return TYPE;
  }

  private String createName( String id ) {
    if ( id.endsWith( EXT ) ) {
      return id.substring( 0, id.lastIndexOf( EXT ) );
    } else {
      return id;
    }
  }

  @Override
  public void getIds( final XulServiceCallback<List<IDatasourceInfo>> callback ) {
    datasourceService.getMetadataDatasourceIds( new XulServiceCallback<List<String>>() {

      @Override
      public void success( List<String> ids ) {
        List<IDatasourceInfo> datasourceInfos = new ArrayList<IDatasourceInfo>();
        for ( String id : ids ) {
          if ( id != null && id.length() > 0 ) {
            datasourceInfos.add( new DatasourceInfo( createName( id ), id, TYPE, editable, removable, importable, exportable ) );
          }
        }
        callback.success( datasourceInfos );
      }

      @Override
      public void error( String message, Throwable error ) {
        callback.error( message, error );
      }
    } );
  }

  @Override
  public String getNewUI() {
    return newUI;
  }

  @Override
  public String getEditUI( IDatasourceInfo dsInfo ) {
    return editUI;
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#export(org.pentaho.platform
   * .dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void export( IDatasourceInfo dsInfo ) {
    datasourceService.export( dsInfo );
  }

  /* (non-Javadoc)
   * @see org.pentaho.platform.dataaccess.datasource.ui.service.IUIDatasourceAdminService#remove(org.pentaho.platform
   * .dataaccess.datasource.IDatasourceInfo)
   */
  @Override
  public void remove( IDatasourceInfo dsInfo, Object callback ) {
    datasourceService.remove( dsInfo, callback );
  }

  /**
   * Return editable flag
   *
   * @return
   */
  @Override public boolean isEditable() {
    return editable;
  }

  /**
   * Return removable flag
   *
   * @return
   */
  @Override public boolean isRemovable() {
    return removable;
  }

  /**
   * Return importable flag
   *
   * @return
   */
  @Override public boolean isImportable() {
    return importable;
  }

  /**
   * Return exportable flag
   *
   * @return
   */
  @Override public boolean isExportable() {
    return exportable;
  }

  /**
   * Return creatable flag
   *
   * @return
   */
  @Override public boolean isCreatable() {
    return creatable;
  }

}
