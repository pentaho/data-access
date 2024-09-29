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


package org.pentaho.platform.dataaccess.datasource.api;

import com.ctc.wstx.stax.WstxInputFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class AnalysisServiceXxeAttackTest {
  private static AnalysisService analysisService;

  @Before
  public void setUp() throws Exception {
    analysisService = mock( AnalysisService.class );
    doCallRealMethod().when( analysisService ).getSchemaName( anyString(), any( InputStream.class ) );
    doReturn( new WstxInputFactory() ).when( analysisService ).getXMLInputFactory();
  }

  @Test( timeout = 1500/*, expected = XMLStreamException.class*/ )
  public void whenReceivingNameFromMaliciousXmlParsingEndsWithNoErrorAndNullValueIsReturned() throws Exception {
    /**
     * @see  <a href="https://en.wikipedia.org/wiki/Billion_laughs" />
     */
    final String maliciousXml =
      "<?xml version=\"1.0\"?>\n"
        + "<!DOCTYPE lolz [\n"
        + " <!ENTITY lol \"lol\">\n"
        + " <!ELEMENT lolz (#PCDATA)>\n"
        + " <!ENTITY lol1 \"&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;\">\n"
        + " <!ENTITY lol2 \"&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;&lol1;\">\n"
        + " <!ENTITY lol3 \"&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;\">\n"
        + " <!ENTITY lol4 \"&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;&lol3;\">\n"
        + " <!ENTITY lol5 \"&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;&lol4;\">\n"
        + " <!ENTITY lol6 \"&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;&lol5;\">\n"
        + " <!ENTITY lol7 \"&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;&lol6;\">\n"
        + " <!ENTITY lol8 \"&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;&lol7;\">\n"
        + " <!ENTITY lol9 \"&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;&lol8;\">\n"
        + "]>\n"
        + "<lolz>&lol9;</lolz>";

    InputStream inputStream = toInputStream( maliciousXml );
    assertEquals( null, analysisService.getSchemaName( "UTF-8", inputStream ) );
  }


  @Test
  public void whenParsingValidAnalysisSchemaCorrectSchemaNameIsReceived() throws Exception {
    final String schemaName = "SteelWheels";

    final String validSchemeXml = "<?xml version=\"1.0\"?>\n"
      + "<Schema name=\"" + schemaName + "\">\n"
      + "<Cube name=\"SteelWheelsSales2\" cache=\"true\" enabled=\"true\">"
      + "<Table name=\"ORDERFACT\">"
      + "</Table>"
      + "</Cube>"
      + "</Schema>\n";

    InputStream inputStream = toInputStream( validSchemeXml );
    assertEquals( schemaName, analysisService.getSchemaName( "UTF-8", inputStream ) );
  }


  private InputStream toInputStream( String data ) {
    return new ByteArrayInputStream( data.getBytes( StandardCharsets.UTF_8 ) );
  }
}
