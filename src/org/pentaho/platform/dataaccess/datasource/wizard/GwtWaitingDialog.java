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
 * Created June 24, 2009
 * @author rmansoor
 */
package org.pentaho.platform.dataaccess.datasource.wizard;

import org.pentaho.gwt.widgets.client.dialogs.GlassPane;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class GwtWaitingDialog extends SimplePanel implements WaitingDialog{
  private String waitMessage;
  private String waitTitle;
  private boolean shown = false;
  private Label messageLabel = null;
  private Label messageTitle = null; 
  private static FocusPanel pageBackground = null;
  private static int clickCount = 0;  
  
  public GwtWaitingDialog() {
    
  }
  public GwtWaitingDialog(String waitTitle, String waitMessage) {
    setStyleName("waitPopupg"); //$NON-NLS-1$
    VerticalPanel vp = new VerticalPanel();
    messageLabel = new Label(waitMessage); //$NON-NLS-1$
    messageLabel.setStyleName("waitPopup_title"); //$NON-NLS-1$
    vp.add(messageLabel);
    messageTitle = new Label(waitTitle); //$NON-NLS-1$
    messageTitle.setStyleName("waitPopup_msg"); //$NON-NLS-1$
    vp.add(messageTitle);
    vp.setStyleName("waitPopup_table"); //$NON-NLS-1$
    this.add(vp);
    
    if (pageBackground == null) {
      pageBackground = new FocusPanel();
      pageBackground.setHeight("100%"); //$NON-NLS-1$
      pageBackground.setWidth("100%"); //$NON-NLS-1$
      pageBackground.setStyleName("modalDialogPageBackground"); //$NON-NLS-1$
      pageBackground.addClickListener(new ClickListener() {

        public void onClick(Widget sender) {
          clickCount++;
          if (clickCount > 2) {
            clickCount = 0;
            pageBackground.setVisible(false);
          }
        }
      });
      RootPanel.get().add(pageBackground, 0, 0);
      RootPanel.get().add(this);
      this.setVisible(false);
    }
  }


  public String getMessage() {
    return waitMessage;
  }

  public void hide() {
    if(shown) {
      this.setVisible(false);
    }
  }

  public boolean isShown() {
    return shown;
  }

  public void setMessage(String message) {
    this.waitMessage = message;
    messageLabel.setText(message);
  }

  public void show() {
   if(!shown) {
     this.setVisible(true);
   }
  }
  
  public void setTitle(String title) {
    this.waitTitle = title;
    messageTitle.setText(title);
  }
  
  public String getTitle() {
   return this.waitTitle;
  }

  @Override
  public void setVisible(boolean visible) {
    try {
      super.setVisible(visible);
      pageBackground.setVisible(visible);
    
      // Notify listeners that this wait dialog is shown (hide pdfs, flash, etc.)
      if(visible){
        GlassPane.getInstance().show();
      } else {
        GlassPane.getInstance().hide();
      }
    } catch (Throwable t) {
    }
  }
}
