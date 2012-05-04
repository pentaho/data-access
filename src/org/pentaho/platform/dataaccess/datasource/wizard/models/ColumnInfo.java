package org.pentaho.platform.dataaccess.datasource.wizard.models;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.pentaho.metadata.model.concept.types.DataType;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

public class ColumnInfo extends XulEventSourceAdapter implements Serializable {
  
  public static final List<String> DATE_FORMATS = Arrays.asList("MM-dd-yyyy",
      "MM/dd/yyyy HH:mm:ss",
      "MM/dd/yyyy",
      "dd-MM-yyyy",
      "dd/MM/yyyy",
      "yyyy-MM-dd HH:mm:ss",
      "yyyy-MM-dd",
      "yyyy/MM/dd",
      "MM-dd-yy",
      "MM/dd/yy",
      "dd-MM-yy",
      "dd/MM/yy"
  );

  private static final long serialVersionUID = 2498345633158485182L;

  public static final String FIELD_TYPE_BOTH = "both"; //$NON-NLS-1$
  
  public static final String FIELD_TYPE_MEASURE = "measure"; //$NON-NLS-1$
  
  public static final String FIELD_TYPE_ATTRIBUTE = "attribute"; //$NON-NLS-1$
  
  public static final String FIELD_TYPE_DIMENSION = "dimension"; //$NON-NLS-1$
  
  public static final int DATE_LEVEL_YEAR = 0x01;
  
  public static final int DATE_LEVEL_MONTH = 0x02;
  
  public static final int DATE_LEVEL_QUARTER = 0x04;
  
  public static final int DATE_LEVEL_WEEK = 0x08;
  
  public static final int DATE_LEVEL_DAY = 0x10;
  
  public static final int DATE_LEVEL_DAYOFWEEK = 0x20;
  
  private String id;
  
  private String title;
  
  private DataType dataType;

  private static List<DataType> availableDataTypes;
  
  private String aggregateType;
  
  private String[] samples;

  private boolean index;
  
  private boolean ignore;
  
  private String fieldType;
  
  private String format;
  
  private int length;
  
  private int precision;
  
  private int dateFieldBreakout = DATE_LEVEL_YEAR | DATE_LEVEL_MONTH | DATE_LEVEL_QUARTER | DATE_LEVEL_WEEK | DATE_LEVEL_DAY | DATE_LEVEL_DAYOFWEEK;

  @Bindable
  public int getLength() {
    return length;
  }

  @Bindable
  public void setLength(int length) {
    this.length = length;
  }

  @Bindable
  public int getPrecision() {
    return precision;
  }

  @Bindable
  public void setPrecision(int precision) {
    this.precision = precision;
  }

  @Bindable
  public String getFormat() {
    return format;
  }

  @Bindable
  public void setFormat(String format) {
    this.format = format;
  }

  @Bindable
  public boolean isIndex() {
    return index;
  }

  @Bindable
  public void setIndex(boolean index) {
    this.index = index;
  }

  @Bindable
  public boolean isIgnore() {
    return ignore;
  }

  @Bindable
  public void setIgnore(boolean ignore) {
    this.ignore = ignore;
  }

  @Bindable
  public boolean isInclude() {
    return !ignore;
  }
  
  @Bindable
  public void setInclude(boolean include) {
    ignore = !include;
    firePropertyChange("include", null, include);    
  }
  
  @Bindable
  public String getFieldType() {
    return fieldType;
  }

  @Bindable
  public void setFieldType(String fieldType) {
    this.fieldType = fieldType;
  }

  @Bindable
  public String getId() {
    return id;
  }

  @Bindable
  public void setId(String id) {
    this.id = id;
  }

  @Bindable
  public String getTitle() {
    return title;
  }

  @Bindable
  public void setTitle(String title) {
    this.title = title.trim();
  }

  @Bindable
  public DataType getDataType() {
    return dataType;
  }

  @Bindable
  public void setDataType(DataType dataType) {
    List<String> prev = getFormatStrings(); 
    boolean prevDisabled = getFormatStringsDisabled();
    this.dataType = dataType;
    // trigger a change to format string, and format disabled binding
    this.firePropertyChange("formatStrings", prev, getFormatStrings());
    this.firePropertyChange("formatStringsDisabled", prevDisabled, getFormatStringsDisabled());
  }

  @Bindable
  public String getAggregateType() {
    return aggregateType;
  }

  @Bindable
  public void setAggregateType(String aggregateType) {
    this.aggregateType = aggregateType;
  }

  @Bindable
  public String[] getSamples() {
    return samples;
  }

  @Bindable
  public void setSamples(String[] samples) {
    this.samples = samples;
  }

  @Bindable
  public static List<DataType> getAvailableDataTypes() {
    if (availableDataTypes == null || availableDataTypes.size() == 0) {
      ArrayList<DataType> types = new ArrayList<DataType>();
      DataType[] dt = DataType.values();
      for (DataType dataType : dt) {
        // don't support url, binary, unknown, or image in csv 
        switch(dataType) {
          case URL:
          case BINARY:
          case IMAGE:
          case UNKNOWN:
            break;
          default: 
//            types.add(dataType.getDescription());
            types.add(dataType);
            break;
        }
      }
      availableDataTypes = types;
    }
    return availableDataTypes;    
  }

  @Bindable 
  public boolean getFormatStringsDisabled() {
    return (dataType != DataType.NUMERIC && dataType != DataType.DATE);
  }
  
  @Bindable 
  public List<String> getFormatStrings() {
    ArrayList<String> formatStrings = new ArrayList<String>();
    if (dataType == DataType.NUMERIC) {
      formatStrings.add("0.00");
      formatStrings.add("#.#");
      formatStrings.add("#");
      formatStrings.add("#,##0.###");
      formatStrings.add("###,###,###.#");
      formatStrings.add("$#,###");
      formatStrings.add("$#,###.00;($#,###.00)");

    } else if (dataType == DataType.DATE) {
      formatStrings.addAll(DATE_FORMATS);
    } else {
      // No Format Strings, field should be disabled
    }
    return formatStrings;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((aggregateType == null) ? 0 : aggregateType.hashCode());
    result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
    result = prime * result + ((fieldType == null) ? 0 : fieldType.hashCode());
    result = prime * result + ((format == null) ? 0 : format.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + (ignore ? 1231 : 1237);
    result = prime * result + (index ? 1231 : 1237);
    result = prime * result + length;
    result = prime * result + precision;
    result = prime * result + Arrays.hashCode(samples);
    result = prime * result + ((title == null) ? 0 : title.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ColumnInfo other = (ColumnInfo) obj;
    if (aggregateType == null) {
      if (other.aggregateType != null)
        return false;
    } else if (!aggregateType.equals(other.aggregateType))
      return false;
    if (dataType == null) {
      if (other.dataType != null)
        return false;
    } else if (!dataType.equals(other.dataType))
      return false;
    if (fieldType == null) {
      if (other.fieldType != null)
        return false;
    } else if (!fieldType.equals(other.fieldType))
      return false;
    if (format == null) {
      if (other.format != null)
        return false;
    } else if (!format.equals(other.format))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (ignore != other.ignore)
      return false;
    if (index != other.index)
      return false;
    if (length != other.length)
      return false;
    if (precision != other.precision)
      return false;
    if (!Arrays.equals(samples, other.samples))
      return false;
    if (title == null) {
      if (other.title != null)
        return false;
    } else if (!title.equals(other.title))
      return false;
    return true;
  }

  public int getDateFieldBreakout() {
    return dateFieldBreakout;
  }

  public void setDateFieldBreakout(int dateFieldBreakout) {
    this.dateFieldBreakout = dateFieldBreakout;
  }
  
  
  
}
