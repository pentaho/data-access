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
