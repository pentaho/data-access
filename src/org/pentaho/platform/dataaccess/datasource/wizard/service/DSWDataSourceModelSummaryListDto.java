package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="modelList")
public class DSWDataSourceModelSummaryListDto {

  public List<DSWDataSourceModelSummaryDto> model;
  DSWDataSourceModelSummaryListDto() {
    }
  DSWDataSourceModelSummaryListDto(List<DSWDataSourceModelSummaryDto> modelList) {
      this.model = modelList;
    }
}
