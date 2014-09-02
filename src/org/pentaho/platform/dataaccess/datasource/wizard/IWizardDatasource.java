/*!
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
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.metadata.model.Domain;
import org.pentaho.platform.dataaccess.datasource.wizard.models.IWizardModel;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulEventSource;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.XulServiceCallback;

import java.util.List;

/**
 * User: nbaker Date: 3/22/11
 */
public interface IWizardDatasource extends XulEventSource {

  /**
   * Localized name of the Datasource.
   *
   * @return
   */
  String getName();

  /**
   * Return a list of steps to be added to the wizard when the datasource is activated.
   *
   * @return
   */
  List<IWizardStep> getSteps();


  /**
   * @return a boolean that determines if the "Finish" button should be enabled.
   */
  public boolean isFinishable();


  /**
   * Flags a datasource as finishable. This needs to be fired as a propertyChangeEvent from the implementing Datasource
   * So the Main Controller will pickup the change.
   */
  public void setFinishable( boolean isFinishable );

  /**
   * Called when the Wizard is finished.
   *
   * @param callback gets called with a summary of the results.
   */
  void onFinish( XulServiceCallback<IDatasourceSummary> callback );

  /**
   * Called when the datasource is becoming active (selected in the UI). At this time datasource steps will be added to
   * the IWizardController. Steps should be "cleared" when this method is called
   */
  void activating() throws XulException;

  /**
   * Step controllers should be initialized with bindings created at this time.
   */
  void init( XulDomContainer container, IWizardModel wizardModel ) throws XulException;


  /**
   * Called when the datasource is deactivating (de-selected in the UI). All steps will be removed from the
   * IWizardController
   */
  void deactivating();

  /**
   * Returns the unique ID for this type of datasource. This ID will be stored in the final Domain for retrieval on
   * edit.
   *
   * @return
   */
  String getId();

  void restoreSavedDatasource( Domain previousDomain, XulServiceCallback<Void> callback );

  void reset();
}
