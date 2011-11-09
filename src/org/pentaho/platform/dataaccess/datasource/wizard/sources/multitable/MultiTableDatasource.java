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
 * Copyright (c) 2011 Pentaho Corporation..  All rights reserved.
 * 
 * @author Ezequiel Cuellar
 */

package org.pentaho.platform.dataaccess.datasource.wizard.sources.multitable;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.agilebi.modeler.gwt.BogoPojo;
import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.beans.Connection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryPhysicalStep;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulLabel;
import org.pentaho.ui.xul.components.XulRadio;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulListbox;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MultiTableDatasource extends AbstractXulEventHandler implements IWizardDatasource {

	private boolean finishable;
	private MultitableGuiModel joinGuiModel;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private QueryPhysicalStep connectionSelectionStep;
	private TablesSelectionStep tablesSelectionStep;
	private JoinDefinitionsStep joinDefinitionsStep;
	private Connection connection;
	private BindingFactory bf;
	private IWizardModel wizardModel;
	private JoinValidator validator;
	private XulDialog errorDialog;
	private XulLabel errorLabel;

	public MultiTableDatasource(DatasourceModel datasourceModel) {
		this.joinGuiModel = new MultitableGuiModel();
		this.joinSelectionServiceGwtImpl = new JoinSelectionServiceGwtImpl();
		this.validator = new JoinValidator(this.joinGuiModel, wizardModel);

		this.joinSelectionServiceGwtImpl.gwtWorkaround(new BogoPojo(), new XulServiceCallback<BogoPojo>() {

			public void error(String message, Throwable error) {
				error.printStackTrace();
			}

			public void success(BogoPojo bogoPojo) {
				bogoPojo.getJoinDTO();
			}
		});

		connectionSelectionStep = new QueryPhysicalStep(datasourceModel, MultiTableDatasource.this, false);
		tablesSelectionStep = new TablesSelectionStep(joinGuiModel, joinSelectionServiceGwtImpl, MultiTableDatasource.this);
		joinDefinitionsStep = new JoinDefinitionsStep(joinGuiModel, joinSelectionServiceGwtImpl, MultiTableDatasource.this);
	}

	@Override
	public void activating() throws XulException {
		this.connectionSelectionStep.activating();
		this.tablesSelectionStep.activating();
		this.joinDefinitionsStep.activating();

		XulVbox queryVbox = (XulVbox) document.getElementById("queryBox");
		queryVbox.setVisible(false);
		
		XulVbox metadataVbox = (XulVbox) document.getElementById("metadata");
		metadataVbox.setVisible(true);

		XulVbox connectionsVbox = (XulVbox) document.getElementById("connectionsLbl");
		connectionsVbox.setVisible(true);


		XulListbox connections = (XulListbox) document.getElementById("connectionList");
	    connections.setWidth(568);
	    connections.setHeight(275);

        // Non-star is the default, change that if relational only is not an option
        joinGuiModel.setDoOlap(!wizardModel.isReportingOnlyValid());

	    try {
		    // conditionally hiding the selection of reporting vs reporting+olap in the case where reporting only makes no sense.
	        bf.createBinding(wizardModel, "reportingOnlyValid", "metadata", "visible").fireSourceChanged();
	
		    // Use a binding to keep the radio buttons in sync
		    bf.setBindingType(Type.BI_DIRECTIONAL);
		    XulRadio olapRadio  = (XulRadio) document.getElementById("reporting_analysis");
		    bf.createBinding(olapRadio, "checked", joinGuiModel, "doOlap");
		
		    bf.setBindingType(Type.ONE_WAY);
		    XulRadio reportingRadio  = (XulRadio) document.getElementById("reporting");
		    bf.createBinding(wizardModel, "reportingOnlyValid", reportingRadio, "checked").fireSourceChanged();
    
	    } catch (Exception e) {
	    	e.printStackTrace(); 
        }

		this.errorDialog = (XulDialog) document.getElementById("errorDialog");
		this.errorLabel = (XulLabel) document.getElementById("errorLabel");

		this.connectionSelectionStep.setValid(true);
		this.setConnection(connectionSelectionStep.getConnection());

	}

	@Override
	public void deactivating() {
		this.connectionSelectionStep.deactivate();
		this.tablesSelectionStep.deactivate();
		this.joinDefinitionsStep.deactivate();
	}

	@Override
	public void init(final XulDomContainer container, final IWizardModel wizardModel) throws XulException {
		this.wizardModel = wizardModel;
		document = container.getDocumentRoot();
		this.bf = new GwtBindingFactory(document);
		bf.setBindingType(Binding.Type.ONE_WAY);

		container.addEventHandler(connectionSelectionStep);
		container.addEventHandler(tablesSelectionStep);
		container.addEventHandler(joinDefinitionsStep);
		connectionSelectionStep.init(wizardModel);
		tablesSelectionStep.init(wizardModel);
		joinDefinitionsStep.init(wizardModel);
    
		bf.createBinding(connectionSelectionStep, "connection", this, "connection");
	}

	@Override
	@Bindable
	public String getName() {
		return MessageHandler.getString("multitable.DATABASE_TABLES");
	}

	@Override
	public List<IWizardStep> getSteps() {
		List<IWizardStep> steps = new ArrayList<IWizardStep>();
		steps.add(this.connectionSelectionStep);
		steps.add(this.tablesSelectionStep);
		steps.add(this.joinDefinitionsStep);
		return steps;
	}

	protected void displayErrors(JoinError error) {
		this.errorDialog.setTitle(error.getTitle());
		this.errorLabel.setValue(error.getError());
		this.errorDialog.show();
	}

	@Override
	public void onFinish(final XulServiceCallback<IDatasourceSummary> callback) {

		if (this.validator.allTablesJoined()) {
			String dsName = this.wizardModel.getDatasourceName();
			MultiTableDatasourceDTO dto = this.joinGuiModel.createMultiTableDatasourceDTO(dsName);
			dto.setSelectedConnection(this.connection);
			joinSelectionServiceGwtImpl.serializeJoins(dto, this.connection, new XulServiceCallback<IDatasourceSummary>() {
				public void error(String message, Throwable error) {
					error.printStackTrace();
				}

				public void success(IDatasourceSummary value) {
					callback.success(value);
				}
			});
		} else {
			MessageHandler.getInstance().closeWaitingDialog();
			XulDialog wizardDialog = (XulDialog) document.getElementById("main_wizard_window");
			wizardDialog.show();
			this.displayErrors(this.validator.getError());
		}
	}

	@Override
	public String getId() {
		return "MULTI-TABLE-DS";
	}

	@Override
	public boolean isFinishable() {
		return finishable;
	}

	@Bindable
	public void setFinishable(boolean finishable) {
		this.finishable = finishable;
		firePropertyChange("finishable", !finishable, finishable);
	}

	@Override
	public void restoreSavedDatasource(final Domain previousDomain, final XulServiceCallback<Void> callback) {

		String serializedDatasource = (String) previousDomain.getLogicalModels().get(0).getProperty("datasourceModel");
		joinSelectionServiceGwtImpl.deSerializeModelState(serializedDatasource, new XulServiceCallback<MultiTableDatasourceDTO>() {

			public void success(final MultiTableDatasourceDTO datasourceDTO) {
				wizardModel.setDatasourceName(datasourceDTO.getDatasourceName());
				// This sets up a race. populateJoinGuiModel relies on the
				// available tables being populated, with is async
				MultiTableDatasource.this.connectionSelectionStep.selectConnectionByName(datasourceDTO.getSelectedConnection().getName());

				// We'll get out of the race by making the same async call now
				// and tailing off it's success
				// TODO: investigate a better way around this race condition.
				// This service is being called twice on edit as is
				joinSelectionServiceGwtImpl.getDatabaseTables(connection, new XulServiceCallback<List>() {
					public void error(String message, Throwable error) {
						error.printStackTrace();
					}

					public void success(List tables) {
						joinGuiModel.processAvailableTables(tables);
						joinGuiModel.populateJoinGuiModel(previousDomain, datasourceDTO);
						tablesSelectionStep.setValid(true);
						if(joinGuiModel.isDoOlap()) {
							tablesSelectionStep.setFactTable(joinGuiModel.getFactTable());
						}
						callback.success(null);
					}
				});

			}

			public void error(String s, Throwable throwable) {
				MessageHandler.getInstance().showErrorDialog(MessageHandler.getString("ERROR"), MessageHandler.getString("DatasourceEditor.ERROR_0002_UNABLE_TO_SHOW_DIALOG", throwable.getLocalizedMessage()));
				callback.error(s, throwable);
			}
		});
	}

	class NotDisabledBindingConvertor extends BindingConvertor<Boolean, Boolean> {
		public Boolean sourceToTarget(Boolean value) {
			return Boolean.valueOf(!value.booleanValue());
		}

		public Boolean targetToSource(Boolean value) {
			return Boolean.valueOf(!value.booleanValue());
		}
	}

	@Override
	public void reset() {
		this.joinGuiModel.reset();
	}

	@Bindable
	public Connection getConnection() {
		return connection;
	}

	@Bindable
	public void setConnection(Connection connection) {
		this.connection = connection;
		this.joinGuiModel.reset();
		this.joinDefinitionsStep.resetComponents();
		this.tablesSelectionStep.processAvailableTables(connection);
	}
}
