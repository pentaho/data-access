package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.CsvDatasourceServiceHelper;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.util.messages.LocaleHelper;

public class FileUtils {

  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  
  static {
    if (!PentahoSystem.getInitializedOK()) {
      CsvDatasourceServiceHelper csvHelper = new CsvDatasourceServiceHelper();
      csvHelper.setUp();
    }
  }
  
  public FileInfo[] listFiles() {
    List<FileInfo> fileList = new ArrayList<FileInfo>();
    String relativePath = PentahoSystem.getSystemSetting("file-upload-defaults/relative-path", String.valueOf(DEFAULT_RELATIVE_UPLOAD_FILE_PATH));  //$NON-NLS-1$  
    String path = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
    File folder = new File(path);
    if (folder.exists()) {
      File files[] = folder.listFiles();
      for (File file : files) {        
        String name = file.getName();
        if(file.isFile()) {
          long lastModified = file.lastModified();
          DateFormat fmt = LocaleHelper.getShortDateFormat(true, true);
          Date modified = new Date();
          modified.setTime(lastModified);
          String modifiedStr = fmt.format(modified);
          long size = file.length();
          FileInfo info = new FileInfo();
          info.setModified(modifiedStr);
          info.setName(name);
          info.setSize(size);
          fileList.add(info);
        }
      }
    }

    return fileList.toArray(new FileInfo[fileList.size()]);
  }
  
  public Boolean deleteFile(String aFileName) {
    boolean result = false;
    String relativePath = PentahoSystem.getSystemSetting(
        "file-upload-defaults/relative-path", String.valueOf(FileUtils.DEFAULT_RELATIVE_UPLOAD_FILE_PATH)); //$NON-NLS-1$
    String path = PentahoSystem.getApplicationContext().getSolutionPath(relativePath);
    File file = new File(path + File.separatorChar + aFileName);

    if (file.exists()) {
      result = file.delete();
    }
    return result;
  }

}
