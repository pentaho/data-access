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

package org.pentaho.platform.dataaccess.datasource.wizard.models;


import org.junit.Assert;
import org.junit.Test;

public class CsvTransformGeneratorExceptionTest {

  @Test
  public void test() {
    final Exception causeException = new Exception( "cause msg" );

    Assert.assertEquals( null, ( new CsvTransformGeneratorException() ).getLocalizedMessage() );

    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg" ) ).getLocalizedMessage() );

    Assert.assertEquals( causeException.toString(), ( new CsvTransformGeneratorException( causeException ) ).getLocalizedMessage() );

    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", null ) ).getLocalizedMessage() );
    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", causeException ) ).getLocalizedMessage() );

    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", causeException, "explicit cause message" ) ).getLocalizedMessage() );
    Assert.assertEquals( "msg", ( new CsvTransformGeneratorException( "msg", causeException, "explicit cause message", "cause stack trace" ) ).getLocalizedMessage() );

    Assert.assertEquals( "localized", ( new CsvTransformGeneratorException( "a message", null, null, null, "localized" ) ).getLocalizedMessage() );
    Assert.assertEquals( "localized", ( new CsvTransformGeneratorException( "a message", new Exception(), "a", "b", "localized" ) ).getLocalizedMessage() );
  }
}
