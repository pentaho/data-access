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
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.controllers.MessageHandler;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MultiTableDatasource extends AbstractXulEventHandler implements IWizardDatasource {

	private boolean finishable;
	private JoinGuiModel joinGuiModel;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private TablesSelectionStep tablesSelectionStep;
	private JoinDefinitionsStep joinDefinitionsStep;
	private IConnection selectedConnection;
	private BindingFactory bf;
	private IWizardModel wizardModel;

	public MultiTableDatasource() {
		this.joinGuiModel = new JoinGuiModel();
		this.joinSelectionServiceGwtImpl = new JoinSelectionServiceGwtImpl();

		this.joinSelectionServiceGwtImpl.gwtWorkaround(new BogoPojo(), new XulServiceCallback<BogoPojo>() {

			public void error(String message, Throwable error) {
				error.printStackTrace();
			}

			public void success(BogoPojo bogoPojo) {
				bogoPojo.getlRelationship();
			}
		});

		// GOOD DO NOT REMOVE
		//this.joinSelectionStepController = new JoinSelectionStepController(this.joinGuiModel, joinSelectionServiceGwtImpl, this.datasourceModel.getSelectedRelationalConnection(), this);
		//this.joinDefinitionStepController = new JoinDefinitionStepController(this.joinGuiModel, joinSelectionServiceGwtImpl, this.datasourceModel.getSelectedRelationalConnection(), this);
	}

	@Override
	public void activating() throws XulException {
		this.tablesSelectionStep.activating();
		this.joinDefinitionsStep.activating();
	}

	@Override
	public void deactivating() {
		this.tablesSelectionStep.deactivate();
		this.joinDefinitionsStep.deactivate();
	}

	@Override
	public void init(final XulDomContainer container, final IWizardModel wizardModel) throws XulException {
		this.wizardModel = wizardModel;
		bf = new GwtBindingFactory(document);

		// HARCODING SAMPLE DATA FOR NOW
		ConnectionServiceGwtImpl connectionService = new ConnectionServiceGwtImpl();
		connectionService.getConnectionByName("SampleData", new XulServiceCallback<IConnection>() {
			public void error(String message, Throwable error) {
			}

			public void success(IConnection iConnection) {

				try {
					selectedConnection = iConnection;
					tablesSelectionStep = new TablesSelectionStep(joinGuiModel, joinSelectionServiceGwtImpl, selectedConnection, MultiTableDatasource.this);
					joinDefinitionsStep = new JoinDefinitionsStep(joinGuiModel, joinSelectionServiceGwtImpl, selectedConnection, MultiTableDatasource.this);

					// THIS BELONGS HERE IN THE INIT METHOD.
					container.addEventHandler(tablesSelectionStep);
					container.addEventHandler(joinDefinitionsStep);
					tablesSelectionStep.init(wizardModel);
					joinDefinitionsStep.init(wizardModel);
				} catch (XulException e) {
					e.printStackTrace();
				}
			}
		});

		this.bf = new GwtBindingFactory(container.getDocumentRoot());
		bf.setBindingType(Binding.Type.ONE_WAY);
		Binding finishedButtonBinding = this.bf.createBinding(this, "finishable", "finish_btn", "disabled", new NotDisabledBindingConvertor());
		try {
			finishedButtonBinding.fireSourceChanged();
		} catch (Exception e) {
			// TODO add some exception handling here.
		}

	}

	@Override
	@Bindable
	public String getName() {
		return "Database Table(s)"; // TODO: i18n
	}

	@Override
	public List<IWizardStep> getSteps() {
		List<IWizardStep> steps = new ArrayList<IWizardStep>();
		steps.add(this.tablesSelectionStep);
		steps.add(this.joinDefinitionsStep);
		return steps;
	}

	@Override
	public void onFinish(final XulServiceCallback<IDatasourceSummary> callback) {

		String dsName = this.wizardModel.getDatasourceName().replace(".", "_").replace(" ", "_");
		MultiTableDatasourceDTO dto = this.joinGuiModel.createMultiTableDatasourceDTO(dsName);

		joinSelectionServiceGwtImpl.serializeJoins(dto, this.selectedConnection, new XulServiceCallback<IDatasourceSummary>() {
      public void error(String message, Throwable error) {
        error.printStackTrace();
      }

      public void success(IDatasourceSummary value) {
        callback.success(value);
      }
    });
	}

	@Override
	public String getId() {
		return "databaseTables";
	}

	@Override
	public boolean isFinishable() {
		return finishable;
	}

	@Bindable
	public void setFinishable(boolean finishable) {
		boolean prevFinishable = this.finishable;
		this.finishable = finishable;
		firePropertyChange("finishable", prevFinishable, finishable);
	}

	@Override
	public void restoreSavedDatasource(final Domain previousDomain, final XulServiceCallback<Void> callback) {

		String serializedDatasource = (String) previousDomain.getLogicalModels().get(0).getProperty("datasourceModel");
		joinSelectionServiceGwtImpl.deSerializeModelState(serializedDatasource, new XulServiceCallback<MultiTableDatasourceDTO>() {

      public void success(MultiTableDatasourceDTO datasourceDTO) {
        wizardModel.setDatasourceName(datasourceDTO.getDatasourceName());
        joinGuiModel.populateJoinGuiModel(datasourceDTO);
        callback.success(null);
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
}
