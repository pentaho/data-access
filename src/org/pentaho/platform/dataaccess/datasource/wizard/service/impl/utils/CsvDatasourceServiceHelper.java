package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoObjectFactory;
import org.pentaho.platform.engine.core.system.PathBasedSystemSettings;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import org.pentaho.platform.engine.core.system.StandaloneApplicationContext;
import org.pentaho.platform.engine.core.system.objfac.StandaloneSpringPentahoObjectFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.FileSystemResource;

/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created June, 2010
 * @author Ezequiel Cuellar
 */

public class CsvDatasourceServiceHelper {

  final String SYSTEM_FOLDER = "system"; //$NON-NLS-1$

  private static final String SOLUTION_PATH = "test-res/solution1/"; //$NON-NLS-1$

  private static final String ALT_SOLUTION_PATH = "test-res/solution11"; //$NON-NLS-1$

  private static final String PENTAHO_XML_PATH = "system/pentaho.xml"; //$NON-NLS-1$

  private static final Log logger = LogFactory.getLog(CsvDatasourceServiceHelper.class);

  public void setUp() {

    try {
      PentahoSystem.setSystemSettingsService(new PathBasedSystemSettings());

      if (PentahoSystem.getApplicationContext() == null) {
        StandaloneApplicationContext applicationContext = new StandaloneApplicationContext(getSolutionPath(), ""); //$NON-NLS-1$
        // set the base url assuming there is a running server on port 8080
        applicationContext.setFullyQualifiedServerURL(getBaseUrl());
        String inContainer = System.getProperty("incontainer", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        if (inContainer.equalsIgnoreCase("false")) { //$NON-NLS-1$
          // Setup simple-jndi for datasources
          System.setProperty("java.naming.factory.initial", "org.osjava.sj.SimpleContextFactory"); //$NON-NLS-1$ //$NON-NLS-2$
          System.setProperty("org.osjava.sj.root", getSolutionPath() + "/system/simple-jndi"); //$NON-NLS-1$ //$NON-NLS-2$
          System.setProperty("org.osjava.sj.delimiter", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        ApplicationContext springApplicationContext = getSpringApplicationContext();

        IPentahoObjectFactory pentahoObjectFactory = new StandaloneSpringPentahoObjectFactory();
        pentahoObjectFactory.init(null, springApplicationContext);
        PentahoSystem.setObjectFactory(pentahoObjectFactory);

        //force Spring to inject PentahoSystem, there has got to be a better way than this, perhaps an alternate way of initting spring's app context
        springApplicationContext.getBean("pentahoSystemProxy"); //$NON-NLS-1$
        PentahoSystem.init(applicationContext);
      }
    } catch (Exception e) {
      logger.error(e);
    }
  }

  private ApplicationContext getSpringApplicationContext() {

    String[] fns = {
        "pentahoObjects.spring.xml", "adminPlugins.xml", "sessionStartupActions.xml", "systemListeners.xml", "pentahoSystemConfig.xml" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

    GenericApplicationContext appCtx = new GenericApplicationContext();
    XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(appCtx);

    for (String fn : fns) {
      File f = new File(getSolutionPath() + SYSTEM_FOLDER + "/" + fn); //$NON-NLS-1$
      if (f.exists()) {
        FileSystemResource fsr = new FileSystemResource(f);
        xmlReader.loadBeanDefinitions(fsr);
      }
    }

    String[] beanNames = appCtx.getBeanDefinitionNames();
    System.out.println("Loaded Beans: "); //$NON-NLS-1$
    for (String n : beanNames) {
      System.out.println("bean: " + n); //$NON-NLS-1$
    }
    return appCtx;
  }

  public String getBaseUrl() {
    return "http://localhost:8080/pentaho/"; //$NON-NLS-1$
  }

  public String getSolutionPath() {
    File file = new File(SOLUTION_PATH + PENTAHO_XML_PATH);
    if (file.exists()) {
      System.out.println("File exist returning " + SOLUTION_PATH); //$NON-NLS-1$
      return SOLUTION_PATH;
    } else {
      System.out.println("File does not exist returning " + ALT_SOLUTION_PATH); //$NON-NLS-1$
      return ALT_SOLUTION_PATH;
    }
  }

}
