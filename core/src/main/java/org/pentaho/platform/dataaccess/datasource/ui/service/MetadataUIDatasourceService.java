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
