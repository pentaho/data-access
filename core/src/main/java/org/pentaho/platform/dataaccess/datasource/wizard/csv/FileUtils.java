/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

public class FileUtils {

  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH =
    File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles"
      + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  static {
    PentahoSystemHelper.init();
  }

  public FileInfo[] listFiles() {
    List<FileInfo> fileList = new ArrayList<FileInfo>();
    String path = getRelativeSolutionPath();
    File folder = new File( path );
    if ( folder.exists() ) {
      File[] files = folder.listFiles();
      for ( File file : files ) {
        String name = file.getName();
        if ( file.isFile() ) {
          long lastModified = file.lastModified();
          DateFormat fmt = LocaleHelper.getShortDateFormat( true, true );
          Date modified = new Date();
          modified.setTime( lastModified );
          String modifiedStr = fmt.format( modified );
          long size = file.length();
          FileInfo info = new FileInfo();
          info.setModified( modifiedStr );
          info.setName( name );
          info.setSize( size );
          fileList.add( info );
        }
      }
    }

    return fileList.toArray( new FileInfo[ fileList.size() ] );
  }

  protected String getRelativeSolutionPath() {
    String relativePath = PentahoSystem.getSystemSetting(
        "file-upload-defaults/relative-path",
        String.valueOf( FileUtils.DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) ); //$NON-NLS-1$
    return PentahoSystem.getApplicationContext().getSolutionPath( relativePath );

  }

  public Boolean deleteFile( String aFileName ) {
    boolean result = false;
    String path = getRelativeSolutionPath();
    File file = new File( path + File.separatorChar + aFileName );

    System.err.println( "File not null " + file != null );
    System.err.println( "File path " + file.getAbsolutePath() );
    System.err.println( "File exist " + file.exists() );
    if ( file.exists() ) {
      result = file.delete();
    }
    return result;
  }

}
