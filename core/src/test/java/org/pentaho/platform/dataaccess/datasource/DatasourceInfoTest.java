/*
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU General Public License, version 2 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/gpl-2.0.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
*
* Copyright 2006 - 2017 Hitachi Vantara.  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource;

import org.junit.Assert;
import org.junit.Test;

public class DatasourceInfoTest {

    @Test
    public void testGetName() {
        DatasourceInfo d1 = new DatasourceInfo( "<iMg SrC=x OnErRoR=alert(11111)>", null, null );
        Assert.assertEquals( "&lt;iMg SrC=x OnErRoR=alert(11111)&gt;", d1.getName() );

        DatasourceInfo d2 = new DatasourceInfo( "<&\"=>;", null, null );
        Assert.assertEquals( "&lt;&amp;&quot;=&gt;;", d2.getName() );
    }
}
