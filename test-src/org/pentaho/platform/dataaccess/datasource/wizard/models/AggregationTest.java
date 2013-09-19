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

package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.util.ArrayList;
import java.util.Arrays;import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.pentaho.metadata.model.concept.types.AggregationType;

public class AggregationTest{
  @SuppressWarnings("nls")
  @Test
  public void test() {
    final String VALUES = "NONE,SUM";
    Aggregation aggregation = new Aggregation();
    List<AggregationType> aggTypeList = new ArrayList<AggregationType>();
    aggTypeList.addAll(Arrays.asList(AggregationType.values()));
    aggregation.setAggregationList(aggTypeList);
    aggregation.setDefaultAggregationType(AggregationType.AVERAGE);
    
    Assert.assertEquals(aggregation.getAggregationList().size(), aggTypeList.size());
    Assert.assertEquals(aggregation.getDefaultAggregationType(), AggregationType.AVERAGE);
    Assert.assertEquals(VALUES, aggregation.toString());
  }

}
