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
import org.pentaho.platform.dataaccess.datasource.IConnection;
import org.pentaho.platform.dataaccess.datasource.wizard.IDatasourceSummary;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardDatasource;
import org.pentaho.platform.dataaccess.datasource.wizard.IWizardStep;
import org.pentaho.platform.dataaccess.datasource.wizard.JoinDefinitionStepController;
import org.pentaho.platform.dataaccess.datasource.wizard.JoinSelectionStepController;
import org.pentaho.platform.dataaccess.datasource.wizard.models.DatasourceModel;
import org.pentaho.platform.dataaccess.datasource.wizard.models.JoinGuiModel;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.ConnectionServiceGwtImpl;
import org.pentaho.platform.dataaccess.datasource.wizard.service.impl.JoinSelectionServiceGwtImpl;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;

public class MultiTableDatasource extends AbstractXulEventHandler implements IWizardDatasource {

	private DatasourceModel datasourceModel;
	private BindingFactory bindingFactory;
	private boolean finishable;
	private JoinGuiModel joinGuiModel;
	private JoinSelectionServiceGwtImpl joinSelectionServiceGwtImpl;
	private JoinSelectionStepController joinSelectionStepController;
	private JoinDefinitionStepController joinDefinitionStepController;

	public MultiTableDatasource(DatasourceModel datasourceModel) {
		this.datasourceModel = datasourceModel;
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

		
		//GOOD DO NOT REMOVE
		//this.joinSelectionStepController = new JoinSelectionStepController(this.joinGuiModel, joinSelectionServiceGwtImpl, this.datasourceModel.getSelectedRelationalConnection(), this);
		//this.joinDefinitionStepController = new JoinDefinitionStepController(this.joinGuiModel, joinSelectionServiceGwtImpl, this.datasourceModel.getSelectedRelationalConnection(), this);
	}

	@Override
	public void activating() throws XulException {
		this.joinSelectionStepController.activating();
		this.joinDefinitionStepController.activating();
	}

	@Override
	public void deactivating() {
		this.joinSelectionStepController.deactivate();
		this.joinDefinitionStepController.deactivate();
	}

	@Override
	  public void init(final XulDomContainer container) throws XulException {
	    bindingFactory = new GwtBindingFactory(document);

	    
	    
	    //HARCODING SAMPLE DATA FOR NOW
		ConnectionServiceGwtImpl connectionService = new ConnectionServiceGwtImpl();
        connectionService.getConnectionByName("SampleData", new XulServiceCallback<IConnection>() {
            public void error(String message, Throwable error) {
            }
            public void success(IConnection iConnection) {
            	
            	try {
	            	joinSelectionStepController = new JoinSelectionStepController(joinGuiModel, joinSelectionServiceGwtImpl, iConnection, MultiTableDatasource.this);
	        		joinDefinitionStepController = new JoinDefinitionStepController(joinGuiModel, joinSelectionServiceGwtImpl, iConnection, MultiTableDatasource.this);
	        		   
	        		//THIS BELONGS HERE IN THE INIT METHOD.
	        	    container.addEventHandler(joinSelectionStepController);
	        	    container.addEventHandler(joinDefinitionStepController);
	        	    joinSelectionStepController.init();
	        	    joinDefinitionStepController.init();
            	} catch(XulException e) {
            		e.printStackTrace();
            	}
            }
          });
        
        
        
	    
	  }

	@Override
	@Bindable
	public String getName() {
		return "Database Table(s)"; // TODO: i18n
	}

	 @Override
	  public List<IWizardStep> getSteps() {
	    List<IWizardStep> steps = new ArrayList<IWizardStep>();
	    steps.add(this.joinSelectionStepController);
	    steps.add(this.joinDefinitionStepController);
	    return steps;
	  }

	@Override
	public void onFinish(final XulServiceCallback<IDatasourceSummary> callback) {

	}

	@Override
	public String getId() {
		return "databaseTables";
	}

	@Override
	public boolean isFinishable() {
		return finishable;
	}

	public void setFinishable(boolean finishable) {
		boolean prevFinishable = this.finishable;
		this.finishable = finishable;
		firePropertyChange("finishable", prevFinishable, finishable);
	}
}
