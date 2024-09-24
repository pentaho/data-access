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
