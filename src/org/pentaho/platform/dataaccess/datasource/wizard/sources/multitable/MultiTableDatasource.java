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
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.MultiTableDatasourceDTO;
import org.pentaho.platform.dataaccess.datasource.wizard.sources.query.QueryPhysicalStep;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.containers.XulVbox;
import org.pentaho.ui.xul.dom.Document;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MultiTableDatasource extends AbstractXulEventHandler implements IWizardDatasource {

	private boolean finishable;
	private JoinGuiModel joinGuiModel;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private QueryPhysicalStep connectionSelectionStep;
	private TablesSelectionStep tablesSelectionStep;
	private JoinDefinitionsStep joinDefinitionsStep;
	private IConnection connection;
	private BindingFactory bf;
	private IWizardModel wizardModel;

	public MultiTableDatasource(DatasourceModel datasourceModel) {
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

		connectionSelectionStep = new QueryPhysicalStep(datasourceModel, MultiTableDatasource.this, false);
		tablesSelectionStep = new TablesSelectionStep(joinGuiModel, joinSelectionServiceGwtImpl, MultiTableDatasource.this);
		joinDefinitionsStep = new JoinDefinitionsStep(joinGuiModel, joinSelectionServiceGwtImpl, MultiTableDatasource.this);
	}

	@Override
	public void activating() throws XulException {
		this.connectionSelectionStep.activating();
		this.tablesSelectionStep.activating();
		this.joinDefinitionsStep.activating();

		Document document = this.connectionSelectionStep.getXulDomContainer().getDocumentRoot();
		XulVbox vbox = (XulVbox) document.getElementById("queryBox");
		vbox.setVisible(false);
    this.connectionSelectionStep.setValid(true);

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
		bf = new GwtBindingFactory(document);
		this.bf = new GwtBindingFactory(container.getDocumentRoot());
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
		return "Database Table(s)"; // TODO: i18n
	}

	@Override
	public List<IWizardStep> getSteps() {
		List<IWizardStep> steps = new ArrayList<IWizardStep>();
		steps.add(this.connectionSelectionStep);
		steps.add(this.tablesSelectionStep);
		steps.add(this.joinDefinitionsStep);
		return steps;
	}

	@Override
	public void onFinish(final XulServiceCallback<IDatasourceSummary> callback) {

		String dsName = this.wizardModel.getDatasourceName().replace(".", "_").replace(" ", "_");
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

			public void success(MultiTableDatasourceDTO datasourceDTO) {
				wizardModel.setDatasourceName(datasourceDTO.getDatasourceName());
        MultiTableDatasource.this.connectionSelectionStep.selectConnectionByName(datasourceDTO.getSelectedConnection().getName());
				joinGuiModel.populateJoinGuiModel(previousDomain, datasourceDTO);
        joinDefinitionsStep.setValid(true);
        tablesSelectionStep.setValid(true);
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

  @Bindable
  public IConnection getConnection() {
    return connection;
  }

  @Bindable
  public void setConnection(IConnection connection) {
    this.connection = connection;
  }
}
