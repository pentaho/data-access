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

package org.pentaho.platform.dataaccess.datasource.wizard.csv;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.junit.Test;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.UploadFileDebugServlet;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils.PentahoSystemHelper;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.util.UUIDUtil;
import org.safehaus.uuid.UUID;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.util.Assert;

public class FileUploadServiceTest {

  public static final String DEFAULT_RELATIVE_UPLOAD_FILE_PATH = File.separatorChar
      + "system" + File.separatorChar + "metadata" + File.separatorChar + "csvfiles" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  
  public static final String TMP_FILE_PATH = File.separatorChar + "system" + File.separatorChar + File.separatorChar + "tmp" + File.separatorChar; //$NON-NLS-1$ //$NON-NLS-2$

  @Test
  public void testUpload() throws Exception {

    PentahoSystemHelper.init();
    StandaloneSession pSession = new StandaloneSession("12345678901234567890");
    PentahoSessionHolder.setSession(pSession);

    UUID uuid = UUIDUtil.getUUID();
    String fileName = uuid.toString();
    MockHttpSession session = new MockHttpSession(null, "12345678901234567890"); //$NON-NLS-1$
    MockHttpServletRequest request = new MockHttpServletRequest("POST", ""); //$NON-NLS-1$ //$NON-NLS-2$
    request.setSession(session);
    request.addParameter("file_name", fileName); //$NON-NLS-1$
    request.addParameter("mark_temporary", "true"); //$NON-NLS-1$ //$NON-NLS-2$
    request.setContentType("multipart/form-data; boundary=boundary"); //$NON-NLS-1$
    StringBuffer content = new StringBuffer();
    content.append("--boundary\r\n"); //$NON-NLS-1$
    content
        .append("Content-Disposition: form-data; name=uploadFormElement; filename=test_file.csv\r\nContent-Type: multipart/form-data\r\n\r\n"); //$NON-NLS-1$ 

    content.append("REGIONC,NWEIGHT,HD65,xdate,Location,charlen,xfactor,Flag\r\n"); //$NON-NLS-1$
    content.append("3,25677.96525,1231,1/1/10,Afghanistan,11,111.9090909,0\r\n"); //$NON-NLS-1$
    content.append("4,24261.81026,1663,1/2/10,Albania,7,237.5714286,0\r\n"); //$NON-NLS-1$
    content.append("2,31806.29502,5221,1/3/10,Algeria,7,745.8571429,1\r\n");//$NON-NLS-1$
    content.append("4,22345.39749,5261,1/4/10,American Samoa,14,375.7857143,1\r\n");//$NON-NLS-1$
    content.append("4,22345.39749,5261,1/4/10,American Samoa,14,375.7857143,1\r\n");//$NON-NLS-1$
    content.append("3,25677.96525,1231,1/1/10,Afghanistan,11,111.9090909,0\r\n");//$NON-NLS-1$
    content.append("4,24261.81026,1663,1/2/10,Albania,7,237.5714286,0\r\n");//$NON-NLS-1$
    content.append("2,31806.29502,5221,1/3/10,Algeria,7,745.8571429,1\r\n");//$NON-NLS-1$
    content.append("4,22345.39749,5261,1/4/10,American Samoa,14,375.7857143,1\r\n");//$NON-NLS-1$

    content.append("--boundary--\r\n"); //$NON-NLS-1$
    request.setContent(content.toString().getBytes());
    UploadFileDebugServlet uploadServlet = new UploadFileDebugServlet();
    MockHttpServletResponse response = new MockHttpServletResponse();
    uploadServlet.service(request, response);
    
    response.getWriter().flush();
    response.getWriter().close();
    fileName = response.getContentAsString();
    String path = PentahoSystem.getApplicationContext().getSolutionPath(TMP_FILE_PATH);
    
    String filenameWithPath = path + File.separatorChar + fileName;
    File file = new File(filenameWithPath);
    assertTrue(file.exists());
    if (file.exists()) {
      file.delete();
    }
  }
  
  /*
   * Newer version of spring's MockHttpSession includes a MockHttpSession(ServletContext servletContext, String id) constructor.
   * Meanwhile just adding its source code here to be able to set the session id in the MockHttpSession. 
   * This class can completely go away when a new version of spring is gotten through ivy. 
   * */
  
  class MockHttpSession implements HttpSession {

    public static final String SESSION_COOKIE_NAME = "JSESSION";

    private int nextId = 1;

    private final String id;

    private final long creationTime = System.currentTimeMillis();

    private int maxInactiveInterval;

    private long lastAccessedTime = System.currentTimeMillis();

    private final ServletContext servletContext;

    private final Hashtable attributes = new Hashtable();

    private boolean invalid = false;

    private boolean isNew = true;


    /**
     * Create a new MockHttpSession with a default {@link MockServletContext}.
     * @see MockServletContext
     */
    public MockHttpSession() {
      this(null);
    }

    /**
     * Create a new MockHttpSession.
     * @param servletContext the ServletContext that the session runs in
     */
    public MockHttpSession(ServletContext servletContext) {
      this(servletContext, null);
    }

    /**
     * Create a new MockHttpSession.
     * @param servletContext the ServletContext that the session runs in
     * @param id a unique identifier for this session
     */
    public MockHttpSession(ServletContext servletContext, String id) {
      this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
      this.id = (id != null ? id : Integer.toString(nextId++));
    }


    public long getCreationTime() {
      return this.creationTime;
    }

    public String getId() {
      return this.id;
    }

    public void access() {
      this.lastAccessedTime = System.currentTimeMillis();
      this.isNew = false;
    }

    public long getLastAccessedTime() {
      return this.lastAccessedTime;
    }

    public ServletContext getServletContext() {
      return this.servletContext;
    }

    public void setMaxInactiveInterval(int interval) {
      this.maxInactiveInterval = interval;
    }

    public int getMaxInactiveInterval() {
      return this.maxInactiveInterval;
    }

    public HttpSessionContext getSessionContext() {
      throw new UnsupportedOperationException("getSessionContext");
    }

    public Object getAttribute(String name) {
      Assert.notNull(name, "Attribute name must not be null");
      return this.attributes.get(name);
    }

    public Object getValue(String name) {
      return getAttribute(name);
    }

    public Enumeration getAttributeNames() {
      return this.attributes.keys();
    }

    public String[] getValueNames() {
      return (String[]) this.attributes.keySet().toArray(new String[this.attributes.size()]);
    }

    public void setAttribute(String name, Object value) {
      Assert.notNull(name, "Attribute name must not be null");
      if (value != null) {
        this.attributes.put(name, value);
        if (value instanceof HttpSessionBindingListener) {
          ((HttpSessionBindingListener) value).valueBound(new HttpSessionBindingEvent(this, name, value));
        }
      }
      else {
        removeAttribute(name);
      }
    }

    public void putValue(String name, Object value) {
      setAttribute(name, value);
    }

    public void removeAttribute(String name) {
      Assert.notNull(name, "Attribute name must not be null");
      Object value = this.attributes.remove(name);
      if (value instanceof HttpSessionBindingListener) {
        ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
      }
    }

    public void removeValue(String name) {
      removeAttribute(name);
    }

    /**
     * Clear all of this session's attributes.
     */
    public void clearAttributes() {
      for (Iterator it = this.attributes.entrySet().iterator(); it.hasNext();) {
        Map.Entry entry = (Map.Entry) it.next();
        String name = (String) entry.getKey();
        Object value = entry.getValue();
        it.remove();
        if (value instanceof HttpSessionBindingListener) {
          ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
        }
      }
    }

    public void invalidate() {
      this.invalid = true;
      clearAttributes();
    }

    public boolean isInvalid() {
      return this.invalid;
    }

    public void setNew(boolean value) {
      this.isNew = value;
    }

    public boolean isNew() {
      return this.isNew;
    }


    /**
     * Serialize the attributes of this session into an object that can
     * be turned into a byte array with standard Java serialization.
     * @return a representation of this session's serialized state
     */
    public Serializable serializeState() {
      HashMap state = new HashMap();
      for (Iterator it = this.attributes.entrySet().iterator(); it.hasNext();) {
        Map.Entry entry = (Map.Entry) it.next();
        String name = (String) entry.getKey();
        Object value = entry.getValue();
        it.remove();
        if (value instanceof Serializable) {
          state.put(name, value);
        }
        else {
          // Not serializable... Servlet containers usually automatically
          // unbind the attribute in this case.
          if (value instanceof HttpSessionBindingListener) {
            ((HttpSessionBindingListener) value).valueUnbound(new HttpSessionBindingEvent(this, name, value));
          }
        }
      }
      return state;
    }

    /**
     * Deserialize the attributes of this session from a state object
     * created by {@link #serializeState()}.
     * @param state a representation of this session's serialized state
     */
    public void deserializeState(Serializable state) {
      Assert.isTrue(state instanceof Map, "Serialized state needs to be of type [java.util.Map]");
      this.attributes.putAll((Map) state);
    }
  }
}
