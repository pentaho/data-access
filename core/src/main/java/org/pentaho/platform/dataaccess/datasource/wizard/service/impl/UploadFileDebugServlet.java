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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.platform.dataaccess.datasource.wizard.service.messages.Messages;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.UUIDUtil;
import org.pentaho.reporting.libraries.base.util.StringUtils;

public class UploadFileDebugServlet extends HttpServlet implements Servlet {

  private static final long serialVersionUID = 8305367618713715640L;

  private static final long MAX_FILE_SIZE = 300000;

  private static final long MAX_FOLDER_SIZE = 900000;

  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH =
    File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles"
      + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

  public UploadFileDebugServlet() {
    PentahoSystemHelper.init();
  }

  protected void doPost( HttpServletRequest request, HttpServletResponse response )
    throws ServletException, IOException {
    try {

      String relativePath = PentahoSystem.getSystemSetting( "file-upload-defaults/relative-path",
        String.valueOf( DEFAULT_RELATIVE_UPLOAD_FILE_PATH ) );  //$NON-NLS-1$
      String maxFileLimit = PentahoSystem
        .getSystemSetting( "file-upload-defaults/max-file-limit", String.valueOf( MAX_FILE_SIZE ) );  //$NON-NLS-1$
      String maxFolderLimit = PentahoSystem
        .getSystemSetting( "file-upload-defaults/max-folder-limit", String.valueOf( MAX_FOLDER_SIZE ) );  //$NON-NLS-1$
      IPentahoSession session = PentahoSessionHolder.getSession();

      response.setContentType( "text/plain" ); //$NON-NLS-1$

      FileItem uploadItem = getFileItem( request );
      if ( uploadItem == null ) {
        String error = Messages.getErrorString( "UploadFileDebugServlet.ERROR_0001_NO_FILE_TO_UPLOAD" ); //$NON-NLS-1$
        response.getWriter().write( error );
        return;
      }
      if ( Long.parseLong( maxFileLimit ) < uploadItem.getSize() ) {
        String error = Messages.getErrorString( "UploadFileDebugServlet.ERROR_0003_FILE_TOO_BIG" ); //$NON-NLS-1$
        response.getWriter().write( error );
        return;
      }

      String path = PentahoSystem.getApplicationContext().getSolutionPath( relativePath );
      File pathDir = new File( path );
      // create the path if it doesn't exist yet
      if ( !pathDir.exists() ) {
        pathDir.mkdirs();
      }

      if ( uploadItem.getSize() + getFolderSize( new File( path ) ) > Long.parseLong( maxFolderLimit ) ) {
        String error =
          Messages.getErrorString( "UploadFileDebugServlet.ERROR_0004_FOLDER_SIZE_LIMIT_REACHED" ); //$NON-NLS-1$
        response.getWriter().write( error );
        return;
      }

      String filename = request.getParameter( "file_name" ); //$NON-NLS-1$
      if ( StringUtils.isEmpty( filename ) ) {
        filename = UUIDUtil.getUUID().toString();
      }

      String temporary = request.getParameter( "mark_temporary" ); //$NON-NLS-1$
      boolean isTemporary = false;
      if ( temporary != null ) {
        isTemporary = Boolean.valueOf( temporary );
      }

      File file;
      if ( isTemporary ) {
        File tempDir = new File( PentahoSystem.getApplicationContext().getSolutionPath( "system/tmp" ) );
        if ( tempDir.exists() == false ) {
          tempDir.mkdir();
        }
        file = PentahoSystem.getApplicationContext().createTempFile( session, filename, ".tmp", true ); //$NON-NLS-1$
      } else {
        file = new File( path + File.separatorChar + filename );
      }

      FileOutputStream outputStream = new FileOutputStream( file );
      byte[] fileContents = uploadItem.get();
      outputStream.write( fileContents );
      outputStream.flush();
      outputStream.close();

      response.getWriter().write( file.getName() );
    } catch ( Exception e ) {
      String error = Messages
        .getErrorString( "UploadFileDebugServlet.ERROR_0005_UNKNOWN_ERROR", e.getLocalizedMessage() );  //$NON-NLS-1$
      response.getWriter().write( error );
    }
  }

  private FileItem getFileItem( HttpServletRequest request ) {
    FileItemFactory factory = new DiskFileItemFactory();
    ServletFileUpload upload = new ServletFileUpload( factory );
    try {
      List items = upload.parseRequest( request );
      Iterator it = items.iterator();
      while ( it.hasNext() ) {
        FileItem item = (FileItem) it.next();
        if ( !item.isFormField() && "uploadFormElement".equals( item.getFieldName() ) ) { //$NON-NLS-1$
          return item;
        }
      }
    } catch ( FileUploadException e ) {
      return null;
    }
    return null;
  }

  private long getFolderSize( File folder ) {
    long foldersize = 0;
    File[] filelist = folder.listFiles();
    for ( int i = 0; i < filelist.length; i++ ) {
      if ( filelist[ i ].isDirectory() ) {
        foldersize += getFolderSize( filelist[ i ] );
      } else {
        foldersize += filelist[ i ].length();
      }
    }
    return foldersize;
  }
}
