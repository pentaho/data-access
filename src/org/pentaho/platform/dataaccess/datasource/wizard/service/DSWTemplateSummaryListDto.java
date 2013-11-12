package org.pentaho.platform.dataaccess.datasource.wizard.service;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class provides the list structure of templates to the template listing service.
 * @author tkafalas
 *
 */
@XmlRootElement(name="templateList")
public class DSWTemplateSummaryListDto {

  public List<DSWTemplateSummaryDto> template;
  DSWTemplateSummaryListDto() {
    }
  DSWTemplateSummaryListDto(List<DSWTemplateSummaryDto> templateList) {
      this.template = templateList;
    }
}
