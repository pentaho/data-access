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

package org.pentaho.platform.dataaccess.datasource.api.resources;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by dstepanov on 12/06/17.
 */

@XmlRootElement
public class MetadataTempFilesListBundleDto implements Serializable {
  private static final long serialVersionUID = 4526978475946122862L;
  String originalFileName;
  String tempFileName;
  String id;

  public MetadataTempFilesListBundleDto() {
  }

  public MetadataTempFilesListBundleDto( String localFileName, String fileName ) {
    this.originalFileName = fileName;
    this.tempFileName = localFileName;
  }

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return "MetadataTempFilesListBundleDto [id=" + id + ", tempFileName=" + tempFileName + ", originalFileName="
      + originalFileName + "]";
  }

  public String getId() {
    return id;
  }

  public void setId( String id ) {
    this.id = id;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName( String originalFileName ) {
    this.originalFileName = originalFileName;
  }

  public String getTempFileName() {
    return tempFileName;
  }

  public void setTempFileName( String tempFileName ) {
    this.tempFileName = tempFileName;
  }
}
