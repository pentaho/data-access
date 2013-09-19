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

package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

public class LegacyDatasourceConverterTest {

  File sampleDTOFile;

  final String SAMPLE_FILE_PATH = "test-res/testDTOSchema.xml";
  final String DEFAULT_ENCODING = "utf-8";

  FileInputStream inputStream;

  String dtoStr;

  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testMarshal() throws Exception {

  }

  @Test
  public void testUnmarshal() throws Exception {

  }

  @Test
  public void testCanConvert() throws Exception {

  }

  @Test
  public void testStreamConverter() throws Exception {
    sampleDTOFile = new File(SAMPLE_FILE_PATH);

    inputStream = new FileInputStream(sampleDTOFile);

    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStream, writer, DEFAULT_ENCODING);
    dtoStr = writer.toString();

    try {
      if (inputStream != null)
        inputStream.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

    XStream xs = new XStream();
    xs.registerConverter(new LegacyDatasourceConverter());
    MultiTableDatasourceDTO resultDTO = (MultiTableDatasourceDTO) xs.fromXML(dtoStr);

    System.out.println(resultDTO.toString());
  }
}
