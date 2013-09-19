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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class FileInfo extends XulEventSourceAdapter implements Serializable {//, XulEventSource {
  
  private static final long serialVersionUID = 3416165533158485182L;

  private static final long KB = 1024;
  
  private static final long MB = 1024*1024;
  
  private static final long GB = 1024*1024*1024;
  
  private String name;
  
  private long size;
  
  private String modified;

  private String sizeStr;
  
  @Bindable
  public String getName() {
    return name;
  }

  @Bindable
  public void setName(String name) {
    this.name = name;
  }

  public long getSize() {
    return size;
  }

  public static String getSizeStr( long size ) {
    String str;
    if( size > GB ) {
      NumberFormat fmt = new DecimalFormat("#.##GB"); //$NON-NLS-1$
      str = fmt.format((double)size/GB);
    } 
    else if( size > MB ) {
      NumberFormat fmt = new DecimalFormat("#.#MB"); //$NON-NLS-1$
      str = fmt.format((double)size/MB);
    } else {
      NumberFormat fmt = new DecimalFormat("#KB"); //$NON-NLS-1$
      str = fmt.format((double)size/KB);
    }
    return str;
  }
  
  public void setSize(long size) {
    this.size = size;
    this.sizeStr = getSizeStr(size);
    
  }

  public String getModified() {
    return modified;
  }

  public void setModified(String modified) {
    this.modified = modified;
  }

  public String getSizeStr() {
    return sizeStr;
  }
  
}
