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


package org.pentaho.platform.dataaccess.datasource.wizard.service.impl.utils;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.agilebi.modeler.models.JoinFieldModel;
import org.pentaho.agilebi.modeler.models.JoinRelationshipModel;
import org.pentaho.agilebi.modeler.models.JoinTableModel;
import org.pentaho.agilebi.modeler.models.SchemaModel;
import org.pentaho.metadata.util.SerializationService;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

public class LegacyDatasourceConverterTest {

  File sampleDTOFile;

  final String SAMPLE_FILE_PATH = "target/test-classes/testDTOSchema.xml";
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

    XStream xs = SerializationService.createXStreamWithAllowedTypes(null, MultiTableDatasourceDTO.class, SchemaModel.class, JoinFieldModel.class, JoinTableModel.class, JoinRelationshipModel.class);
    xs.registerConverter(new LegacyDatasourceConverter());
    MultiTableDatasourceDTO resultDTO = (MultiTableDatasourceDTO) xs.fromXML(dtoStr);

    System.out.println(resultDTO.toString());
  }
}
