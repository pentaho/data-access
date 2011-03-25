/*
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
 * Copyright 2008 - 2009 Pentaho Corporation.  All rights reserved.
 *
 *
 * Created June, 2010
 * @author Ezequiel Cuellar
 */
package org.pentaho.platform.dataaccess.datasource.wizard.service.gwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import org.pentaho.platform.dataaccess.datasource.beans.BogoPojo;
import org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.csv.FileTransformStats;
import org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.CsvDatasourceServiceImpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import javax.servlet.http.HttpServletRequest;

public class CsvDatasourceServiceServlet extends RemoteServiceServlet implements ICsvDatasourceService {

  private static final long serialVersionUID = 2498165533158485182L;

  public ModelInfo stageFile(String fileName, String delimiter, String enclosure, boolean isFirstRowHeader, String encoding)
      throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.stageFile(fileName, delimiter, enclosure, isFirstRowHeader, encoding);
  }

  public FileInfo[] getStagedFiles() throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.getStagedFiles();
  }

  public List<String> getPreviewRows(String filename, boolean isFirstRowHeader, int rows, String encoding) throws Exception {
    CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
    return serviceImpl.getPreviewRows(filename, isFirstRowHeader, rows, encoding);
  }
  
  public String getEncoding(String fileName) {
	  CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
	  return serviceImpl.getEncoding(fileName);
  }

  @Override
  public FileTransformStats generateDomain(ModelInfo modelInfo) throws Exception {
	  CsvDatasourceServiceImpl serviceImpl = new CsvDatasourceServiceImpl();
	  return serviceImpl.generateDomain(modelInfo);
  }

  public BogoPojo gwtWorkaround(BogoPojo pojo) {
    return pojo;
  }

    @Override
  protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL, String strongName) {
    SerializationPolicy policy = new SerializationPolicy(){

        List<Class<?>> classes = new ArrayList<Class<?>>();
        {
          // Standard Classesclasses.add(classes.add(com.allen_sauer.gwt.dnd.client.VetoDragException.class);
classes.add(com.google.gwt.http.client.RequestException.class);
classes.add(com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException.class);
classes.add(com.google.gwt.user.client.rpc.InvocationException.class);
classes.add(com.google.gwt.user.client.rpc.SerializableException.class);
classes.add(com.google.gwt.user.client.rpc.SerializationException.class);
classes.add(ServiceDefTarget.NoServiceEntryPointSpecifiedException.class);
classes.add(com.google.gwt.xml.client.DOMException.class);
classes.add(com.google.gwt.xml.client.impl.DOMParseException.class);
classes.add(java.io.IOException.class);
classes.add(java.lang.ArithmeticException.class);
classes.add(java.lang.ArrayIndexOutOfBoundsException.class);
classes.add(java.lang.ArrayStoreException.class);
classes.add(java.lang.Boolean.class);
classes.add(java.lang.ClassCastException.class);
classes.add(java.lang.Exception.class);
classes.add(java.lang.IllegalArgumentException.class);
classes.add(java.lang.IllegalStateException.class);
classes.add(java.lang.IndexOutOfBoundsException.class);
classes.add(java.lang.Integer.class);
classes.add(java.lang.NegativeArraySizeException.class);
classes.add(java.lang.NullPointerException.class);
classes.add(java.lang.Number.class);
classes.add(java.lang.NumberFormatException.class);
classes.add(java.lang.RuntimeException.class);
classes.add(java.lang.String.class);
classes.add(java.lang.StringIndexOutOfBoundsException.class);
classes.add(java.lang.Throwable.class);
classes.add(java.lang.UnsupportedOperationException.class);
classes.add(java.lang.annotation.AnnotationTypeMismatchException.class);
classes.add(java.lang.reflect.InvocationTargetException.class);
classes.add(java.util.ArrayList.class);
classes.add(java.util.ConcurrentModificationException.class);
classes.add(java.util.EmptyStackException.class);
classes.add(java.util.HashMap.class);
classes.add(java.util.IdentityHashMap.class);
classes.add(java.util.LinkedHashMap.class);
classes.add(java.util.LinkedList.class);
classes.add(java.util.NoSuchElementException.class);
classes.add(java.util.Stack.class);
classes.add(java.util.TooManyListenersException.class);
classes.add(java.util.TreeMap.class);
classes.add(java.util.Vector.class);
classes.add(org.pentaho.agilebi.modeler.IncompatibleModelerException.class);
classes.add(org.pentaho.agilebi.modeler.ModelerException.class);
classes.add(org.pentaho.agilebi.modeler.gwt.BogoPojo.class);
classes.add(org.pentaho.agilebi.modeler.gwt.services.IGwtModelerService.class);
classes.add(org.pentaho.gwt.widgets.login.client.AuthenticationCanceledException.class);
classes.add(org.pentaho.gwt.widgets.login.client.AuthenticationFailedException.class);
classes.add(org.pentaho.metadata.model.Category.class);
classes.add(org.pentaho.metadata.model.Domain.class);
classes.add(org.pentaho.metadata.model.InlineEtlPhysicalColumn.class);
classes.add(org.pentaho.metadata.model.InlineEtlPhysicalModel.class);
classes.add(org.pentaho.metadata.model.InlineEtlPhysicalTable.class);
classes.add(org.pentaho.metadata.model.LogicalColumn.class);
classes.add(org.pentaho.metadata.model.LogicalModel.class);
classes.add(org.pentaho.metadata.model.LogicalRelationship.class);
classes.add(org.pentaho.metadata.model.LogicalTable.class);
classes.add(org.pentaho.metadata.model.SqlDataSource.class);
classes.add(org.pentaho.metadata.model.SqlDataSource.DataSourceType.class);
classes.add(org.pentaho.metadata.model.SqlPhysicalColumn.class);
classes.add(org.pentaho.metadata.model.SqlPhysicalModel.class);
classes.add(org.pentaho.metadata.model.SqlPhysicalTable.class);
classes.add(org.pentaho.metadata.model.concept.Concept.class);
classes.add(org.pentaho.metadata.model.concept.security.RowLevelSecurity.class);
classes.add(org.pentaho.metadata.model.concept.security.RowLevelSecurity.Type.class);
classes.add(org.pentaho.metadata.model.concept.security.Security.class);
classes.add(org.pentaho.metadata.model.concept.security.SecurityOwner.class);
classes.add(org.pentaho.metadata.model.concept.security.SecurityOwner.OwnerType.class);
classes.add(org.pentaho.metadata.model.concept.types.AggregationType.class);
classes.add(org.pentaho.metadata.model.concept.types.Alignment.class);
classes.add(org.pentaho.metadata.model.concept.types.Color.class);
classes.add(org.pentaho.metadata.model.concept.types.ColumnWidth.class);
classes.add(org.pentaho.metadata.model.concept.types.ColumnWidth.WidthType.class);
classes.add(org.pentaho.metadata.model.concept.types.DataType.class);
classes.add(org.pentaho.metadata.model.concept.types.FieldType.class);
classes.add(org.pentaho.metadata.model.concept.types.Font.class);
classes.add(org.pentaho.metadata.model.concept.types.JoinType.class);
classes.add(org.pentaho.metadata.model.concept.types.LocaleType.class);
classes.add(org.pentaho.metadata.model.concept.types.LocalizedString.class);
classes.add(org.pentaho.metadata.model.concept.types.RelationshipType.class);
classes.add(org.pentaho.metadata.model.concept.types.TableType.class);
classes.add(org.pentaho.metadata.model.concept.types.TargetColumnType.class);
classes.add(org.pentaho.metadata.model.concept.types.TargetTableType.class);
classes.add(org.pentaho.metadata.model.olap.OlapCube.class);
classes.add(org.pentaho.metadata.model.olap.OlapDimension.class);
classes.add(org.pentaho.metadata.model.olap.OlapDimensionUsage.class);
classes.add(org.pentaho.metadata.model.olap.OlapHierarchy.class);
classes.add(org.pentaho.metadata.model.olap.OlapHierarchyLevel.class);
classes.add(org.pentaho.metadata.model.olap.OlapHierarchy.class);
classes.add(org.pentaho.metadata.model.olap.OlapMeasure.class);
classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.CsvParseException.class);
classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException.class);
classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException.class);
classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException.class);
classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.QueryValidationException.class);
classes.add(org.pentaho.ui.xul.XulDomException.class);
classes.add(org.pentaho.ui.xul.XulException.class);
          classes.add(com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException.class);
classes.add(java.lang.Exception.class);
classes.add(java.lang.RuntimeException.class);
classes.add(java.lang.String.class);
classes.add(java.lang.String.class);
classes.add(java.lang.Throwable.class);
classes.add(java.util.ArrayList.class);
classes.add(java.util.HashMap.class);
classes.add(java.util.IdentityHashMap.class);
classes.add(java.util.LinkedHashMap.class);
classes.add(java.util.LinkedList.class);
classes.add(java.util.Stack.class);
classes.add(java.util.TreeMap.class);
classes.add(java.util.Vector.class);
classes.add(org.pentaho.database.model.DatabaseAccessType.class);
classes.add(org.pentaho.database.model.DatabaseAccessType.class);
classes.add(org.pentaho.database.model.DatabaseConnection.class);
classes.add(org.pentaho.database.model.DatabaseConnectionPoolParameter.class);
classes.add(org.pentaho.database.model.DatabaseConnectionPoolParameter.class);
classes.add(org.pentaho.database.model.DatabaseType.class);
classes.add(org.pentaho.database.model.DatabaseType.class);
classes.add(org.pentaho.database.model.IDatabaseType.class);
classes.add(org.pentaho.database.model.PartitionDatabaseMeta.class);
classes.add(org.pentaho.database.model.PartitionDatabaseMeta.class);
classes.add(org.pentaho.ui.database.gwt.IGwtDatabaseConnectionService.class);
          classes.add(com.allen_sauer.gwt.dnd.client.VetoDragException.class);
          classes.add(com.google.gwt.http.client.RequestException.class);
          classes.add(com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException.class);
          classes.add(com.google.gwt.user.client.rpc.InvocationException.class);
          classes.add(com.google.gwt.user.client.rpc.SerializableException.class);
          classes.add(com.google.gwt.user.client.rpc.SerializationException.class);
          classes.add(com.google.gwt.user.client.rpc.ServiceDefTarget.NoServiceEntryPointSpecifiedException.class);
          classes.add(com.google.gwt.xml.client.DOMException.class);
          classes.add(com.google.gwt.xml.client.impl.DOMParseException.class);
          classes.add(java.io.IOException.class);
          classes.add(java.lang.ArithmeticException.class);
          classes.add(java.lang.ArrayIndexOutOfBoundsException.class);
          classes.add(java.lang.ArrayStoreException.class);
          classes.add(java.lang.ClassCastException.class);
          classes.add(java.lang.Exception.class);
          classes.add(java.lang.IllegalArgumentException.class);
          classes.add(java.lang.IllegalStateException.class);
          classes.add(java.lang.IndexOutOfBoundsException.class);
          classes.add(java.lang.NegativeArraySizeException.class);
          classes.add(java.lang.NullPointerException.class);
          classes.add(java.lang.NumberFormatException.class);
          classes.add(java.lang.RuntimeException.class);
          classes.add(java.lang.String.class);
          classes.add(java.lang.StringIndexOutOfBoundsException.class);
          classes.add(java.lang.Throwable.class);
          classes.add(java.lang.UnsupportedOperationException.class);
          classes.add(java.lang.annotation.AnnotationTypeMismatchException.class);
          classes.add(java.lang.reflect.InvocationTargetException.class);
          classes.add(java.util.ArrayList.class);
          classes.add(java.util.ConcurrentModificationException.class);
          classes.add(java.util.EmptyStackException.class);
          classes.add(java.util.LinkedList.class);
          classes.add(java.util.NoSuchElementException.class);
          classes.add(java.util.Stack.class);
          classes.add(java.util.TooManyListenersException.class);
          classes.add(java.util.Vector.class);
          classes.add(org.pentaho.agilebi.modeler.IncompatibleModelerException.class);
          classes.add(org.pentaho.agilebi.modeler.ModelerException.class);
          classes.add(org.pentaho.gwt.widgets.login.client.AuthenticationCanceledException.class);
          classes.add(org.pentaho.gwt.widgets.login.client.AuthenticationFailedException.class);
          classes.add(org.pentaho.metadata.model.concept.types.DataType.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.CsvParseException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.CsvTransformGeneratorException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.FileInfo.class);
          classes.add(FileTransformStats.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.QueryValidationException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.ICsvDatasourceService.class);
          classes.add(org.pentaho.ui.xul.XulDomException.class);
          classes.add(org.pentaho.ui.xul.XulException.class);
          classes.add(com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException.class);
          classes.add(java.lang.Exception.class);
          classes.add(java.lang.RuntimeException.class);
          classes.add(java.lang.String.class);
          classes.add(java.lang.Throwable.class);
          classes.add(java.util.ArrayList.class);
          classes.add(java.util.HashMap.class);
          classes.add(java.util.IdentityHashMap.class);
          classes.add(java.util.LinkedHashMap.class);
          classes.add(java.util.LinkedList.class);
          classes.add(java.util.Stack.class);
          classes.add(java.util.TreeMap.class);
          classes.add(java.util.Vector.class);
          classes.add(org.pentaho.database.model.DatabaseAccessType.class);
          classes.add(org.pentaho.database.model.DatabaseConnection.class);
          classes.add(org.pentaho.database.model.DatabaseType.class);
          classes.add(org.pentaho.database.model.PartitionDatabaseMeta.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.beans.Connection.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.ConnectionServiceException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtConnectionService.class);
          classes.add(com.allen_sauer.gwt.dnd.client.DragHandlerCollection.class);
          classes.add(com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException.class);
          classes.add(java.lang.Boolean.class);
          classes.add(java.lang.Exception.class);
          classes.add(java.lang.Integer.class);
          classes.add(java.lang.Number.class);
          classes.add(java.lang.RuntimeException.class);
          classes.add(java.lang.String.class);
          classes.add(java.lang.Throwable.class);
          classes.add(java.util.ArrayList.class);
          classes.add(java.util.HashMap.class);
          classes.add(java.util.IdentityHashMap.class);
          classes.add(java.util.LinkedHashMap.class);
          classes.add(java.util.LinkedList.class);
          classes.add(java.util.Stack.class);
          classes.add(java.util.TreeMap.class);
          classes.add(java.util.Vector.class);
          classes.add(org.pentaho.agilebi.modeler.IncompatibleModelerException.class);
          classes.add(org.pentaho.agilebi.modeler.ModelerException.class);
          classes.add(org.pentaho.agilebi.modeler.nodes.AbstractMetaDataModelNode.class);
          classes.add(org.pentaho.agilebi.modeler.nodes.AvailableFieldCollection.class);
          classes.add(org.pentaho.agilebi.modeler.nodes.DimensionMetaData.class);
          classes.add(org.pentaho.agilebi.modeler.nodes.DimensionMetaDataCollection.class);
          classes.add(org.pentaho.agilebi.modeler.nodes.HierarchyMetaData.class);
          classes.add(org.pentaho.agilebi.modeler.nodes.MainModelNode.class);
          classes.add(org.pentaho.agilebi.modeler.nodes.MeasuresCollection.class);
          classes.add(org.pentaho.gwt.widgets.client.wizards.WizardPanelListenerCollection.class);
          classes.add(org.pentaho.metadata.model.AbstractPhysicalColumn.class);
          classes.add(org.pentaho.metadata.model.Category.class);
          classes.add(org.pentaho.metadata.model.Domain.class);
          classes.add(org.pentaho.metadata.model.InlineEtlPhysicalColumn.class);
          classes.add(org.pentaho.metadata.model.InlineEtlPhysicalModel.class);
          classes.add(org.pentaho.metadata.model.InlineEtlPhysicalTable.class);
          classes.add(org.pentaho.metadata.model.LogicalColumn.class);
          classes.add(org.pentaho.metadata.model.LogicalModel.class);
          classes.add(org.pentaho.metadata.model.LogicalRelationship.class);
          classes.add(org.pentaho.metadata.model.LogicalTable.class);
          classes.add(org.pentaho.metadata.model.SqlDataSource.class);
          classes.add(org.pentaho.metadata.model.SqlDataSource.DataSourceType.class);
          classes.add(org.pentaho.metadata.model.SqlPhysicalColumn.class);
          classes.add(org.pentaho.metadata.model.SqlPhysicalModel.class);
          classes.add(org.pentaho.metadata.model.SqlPhysicalTable.class);
          classes.add(org.pentaho.metadata.model.concept.Concept.class);
          classes.add(org.pentaho.metadata.model.concept.security.RowLevelSecurity.class);
          classes.add(org.pentaho.metadata.model.concept.security.RowLevelSecurity.Type.class);
          classes.add(org.pentaho.metadata.model.concept.security.Security.class);
          classes.add(org.pentaho.metadata.model.concept.security.SecurityOwner.class);
          classes.add(org.pentaho.metadata.model.concept.security.SecurityOwner.OwnerType.class);
          classes.add(org.pentaho.metadata.model.concept.types.AggregationType.class);
          classes.add(org.pentaho.metadata.model.concept.types.Alignment.class);
          classes.add(org.pentaho.metadata.model.concept.types.Color.class);
          classes.add(org.pentaho.metadata.model.concept.types.ColumnWidth.class);
          classes.add(org.pentaho.metadata.model.concept.types.DataType.class);
          classes.add(org.pentaho.metadata.model.concept.types.FieldType.class);
          classes.add(org.pentaho.metadata.model.concept.types.Font.class);
          classes.add(org.pentaho.metadata.model.concept.types.JoinType.class);
          classes.add(org.pentaho.metadata.model.concept.types.LocaleType.class);
          classes.add(org.pentaho.metadata.model.concept.types.LocalizedString.class);
          classes.add(org.pentaho.metadata.model.concept.types.RelationshipType.class);
          classes.add(org.pentaho.metadata.model.concept.types.TableType.class);
          classes.add(org.pentaho.metadata.model.concept.types.TargetColumnType.class);
          classes.add(org.pentaho.metadata.model.concept.types.TargetTableType.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.DatasourceType.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.beans.BogoPojo.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.beans.BusinessData.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.beans.LogicalModelSummary.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.beans.SerializedResultSet.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.ColumnInfo.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.CsvFileInfo.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.CsvParseException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceDTO.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfo.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.ModelInfoValidationListenerCollection.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.models.RelationalModelValidationListenerCollection.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.DatasourceServiceException.class);
          classes.add(org.pentaho.platform.dataaccess.datasource.wizard.service.gwt.IGwtDatasourceService.class);
          classes.add(org.pentaho.ui.xul.util.AbstractModelNode.class);

        }

        @Override
        public boolean shouldDeserializeFields(Class<?> clazz) {
          if(classes.contains(clazz) == false){
            System.out.println(clazz.getName()+" wasn't in the list!");
          }
          return classes.contains(clazz);
        }

        @Override
        public boolean shouldSerializeFields(Class<?> clazz) {
          return classes.contains(clazz);
        }

        @Override
        public void validateDeserialize(Class<?> arg0) throws SerializationException {}

        @Override
        public void validateSerialize(Class<?> arg0) throws SerializationException {}


    };
    return policy;
  }
}

