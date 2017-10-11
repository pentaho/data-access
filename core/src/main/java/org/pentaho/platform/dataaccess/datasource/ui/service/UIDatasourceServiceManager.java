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
* Copyright (c) 2002-2017 Hitachi Vantara..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.ui.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.gwt.widgets.client.ui.ICallback;
import org.pentaho.platform.dataaccess.datasource.IDatasourceInfo;
import org.pentaho.ui.xul.XulServiceCallback;

public class UIDatasourceServiceManager {

  Map<String, IUIDatasourceAdminService> serviceMap = new HashMap<String, IUIDatasourceAdminService>();
  private static UIDatasourceServiceManager instance;

  private UIDatasourceServiceManager() {
  }

  public static UIDatasourceServiceManager getInstance() {
    if ( instance == null ) {
      instance = new UIDatasourceServiceManager();
    }
    return instance;
  }

  public UIDatasourceServiceManager( List<IUIDatasourceAdminService> services ) {
    for ( IUIDatasourceAdminService service : services ) {
      registerService( service );
    }
  }

  public void registerService( IUIDatasourceAdminService service ) {
    serviceMap.put( service.getType(), service );
  }

  public IUIDatasourceAdminService getService( String serviceType ) {
    return serviceMap.get( serviceType );
  }

  public void getIds( final XulServiceCallback<List<IDatasourceInfo>> mainCallback ) {
    final List<IDatasourceInfo> datasourceList = new ArrayList<IDatasourceInfo>();

    final int asyncCallCount = serviceMap.size();

    final ICallback<Void> counterCallback = new ICallback<Void>() {
      int counter = 0;

      @Override
      public void onHandle( Void o ) {
        counter++;
        if ( counter >= asyncCallCount ) {
          if ( mainCallback != null ) {
            mainCallback.success( datasourceList );
          }
        }
      }
    };
    for ( IUIDatasourceAdminService service : serviceMap.values() ) {
      service.getIds( new XulServiceCallback<List<IDatasourceInfo>>() {

        @Override
        public void success( List<IDatasourceInfo> list ) {
          datasourceList.addAll( list );
          counterCallback.onHandle( null );
        }

        @Override
        public void error( String message, Throwable error ) {
          if ( mainCallback != null ) {
            mainCallback.error( message, error );
          }
        }
      } );
    }
  }

  public void exportDatasource( IDatasourceInfo dsInfo ) {
    for ( IUIDatasourceAdminService service : serviceMap.values() ) {
      if ( service.getType().equals( dsInfo.getType() ) && dsInfo.isExportable() ) {
        service.export( dsInfo );
        break;
      }
    }
  }

  /**
   * @param dsInfo
   */
  public void remove( IDatasourceInfo dsInfo, XulServiceCallback<Boolean> callback ) {
    for ( IUIDatasourceAdminService service : serviceMap.values() ) {
      if ( service.getType().equals( dsInfo.getType() ) && dsInfo.isRemovable() ) {
        service.remove( dsInfo, callback );
        break;
      }
    }
  }

  public List<String> getTypes() {
    return new ArrayList<String>( serviceMap.keySet() );
  }

}
