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
 * Created April 21, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.beans;

import java.util.List;
import java.util.Map;

import org.pentaho.database.model.DatabaseAccessType;
import org.pentaho.database.model.IDatabaseConnection;
import org.pentaho.database.model.IDatabaseType;
import org.pentaho.database.model.PartitionDatabaseMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.stereotype.Bindable;

@XmlRootElement
public class Connection extends XulEventSourceAdapter implements IDatabaseConnection {

	private static final long serialVersionUID = 5825649640767205332L;
	private String id;
	private String name;
	private String driverClass;
	private String username;
	private String password;
	private String url;

	public Connection() {

	}
	 @Bindable
	public void setId(String id) {
	    this.id = id;
	}

	  @Bindable
	public String getId() {
	    return id;
	}
	  

	@Bindable
	public void setName(String name) {
		this.name = name;
	}

	@Bindable
	public String getName() {
		return name;
	}

	@Bindable
	public void setDriverClass(String driverClass) {
		this.driverClass = driverClass;
	}

	@Bindable
	public String getDriverClass() {
		return driverClass;
	}

	@Bindable
	public void setUsername(String username) {
		this.username = username;
	}

	@Bindable
	public String getUsername() {
		return username;
	}

	@Bindable
	public void setPassword(String password) {
		this.password = password;
	}

	@Bindable
	public String getPassword() {
		return password;
	}

	@Bindable
	public void setUrl(String url) {
		this.url = url;
	}

	@Bindable
	public String getUrl() {
		return url;
	}

	public void setAccessType(DatabaseAccessType accessType) {
	}

	public DatabaseAccessType getAccessType() {
		return null;
	}

	public void setDatabaseType(IDatabaseType driver) {
	}

	public IDatabaseType getDatabaseType() {
		return null;
	}

	public Map<String, String> getExtraOptions() {
		return null;
	}

	public void setHostname(String hostname) {
	}

	public String getHostname() {
		return null;
	}

	public void setDatabaseName(String databaseName) {
	}

	public String getDatabaseName() {
		return null;
	}

	public void setDatabasePort(String databasePort) {
	}

	public String getDatabasePort() {
		return null;
	}

	public void setStreamingResults(boolean streamingResults) {
	}

	public boolean isStreamingResults() {
		return false;
	}

	public void setDataTablespace(String dataTablespace) {
	}

	public String getDataTablespace() {
		return null;
	}

	public void setIndexTablespace(String indexTablespace) {
	}

	public String getIndexTablespace() {
		return null;
	}

	public void setSQLServerInstance(String sqlServerInstance) {
	}

	public String getSQLServerInstance() {
		return null;
	}

	public void setUsingDoubleDecimalAsSchemaTableSeparator(boolean usingDoubleDecimalAsSchemaTableSeparator) {
	}

	public boolean isUsingDoubleDecimalAsSchemaTableSeparator() {
		return false;
	}

	public void setInformixServername(String informixServername) {
	}

	public String getInformixServername() {
		return null;
	}

	public void addExtraOption(String databaseTypeCode, String option, String value) {
	}

	public Map<String, String> getAttributes() {
		return null;
	}

	public void setChanged(boolean changed) {
	}

	public boolean getChanged() {
		return false;
	}

	public void setQuoteAllFields(boolean quoteAllFields) {
	}

	public boolean isQuoteAllFields() {
		return false;
	}

	public void setForcingIdentifiersToLowerCase(boolean forcingIdentifiersToLowerCase) {
	}

	public boolean isForcingIdentifiersToLowerCase() {
		return false;
	}

	public void setForcingIdentifiersToUpperCase(boolean forcingIdentifiersToUpperCase) {
	}

	public boolean isForcingIdentifiersToUpperCase() {
		return false;
	}

	public void setConnectSql(String sql) {
	}

	public String getConnectSql() {
		return null;
	}

	public void setUsingConnectionPool(boolean usingConnectionPool) {
	}

	public boolean isUsingConnectionPool() {
		return false;
	}

	public void setInitialPoolSize(int initialPoolSize) {
	}

	public int getInitialPoolSize() {
		return -1;
	}

	public void setMaximumPoolSize(int maxPoolSize) {
	}

	public int getMaximumPoolSize() {
		return -1;
	}

	public void setPartitioned(boolean partitioned) {
	}

	public boolean isPartitioned() {
		return false;
	}

	public Map<String, String> getConnectionPoolingProperties() {
		return null;
	}

	public void setConnectionPoolingProperties(Map<String, String> connectionPoolingProperties) {
	}

	public void setPartitioningInformation(List<PartitionDatabaseMeta> partitioningInformation) {
	}

	public List<PartitionDatabaseMeta> getPartitioningInformation() {
		return null;
	}

}
