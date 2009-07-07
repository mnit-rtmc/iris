/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.client.toolbar;

import javax.swing.ComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * Combobox model for action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanComboModel extends ProxyListModel<ActionPlan>
	implements ComboBoxModel 
{
	/** Check box for deploying selected action plan */
	protected final JCheckBox check_box = new JCheckBox();

	/** Get the check box */
	public JCheckBox getCheckBox() {
		return check_box;
	}

	/** Listener for check box change events */
	protected final ChangeListener listener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			ActionPlan p = selected;
			if(p != null)
				p.setDeployed(check_box.isSelected());
		}
	};

	/** Currently selected action plan */
	protected ActionPlan selected;

	/** Removed action plan.  This is needed because proxyChangedSlow
	 * first removes and then adds a proxy when it changes. */
	protected ActionPlan removed;

	/** Create a new action plan combo box model */
	public ActionPlanComboModel(TypeCache<ActionPlan> c) {
		super(c);
		initialize();
	}

	/** Add a new proxy to the model */
	protected int doProxyAdded(ActionPlan proxy) {
		if(proxy.getActive()) {
			// This method is also called when an attribute is
			// changed.  If the attribute is "deployed", then we
			// need to update the checkbox.  Calling
			// setSelectedItem will do that.
			if(proxy == removed)
				setSelectedItem(proxy);
			removed = null;
			return super.doProxyAdded(proxy);
		} else {
			removed = null;
			return -1;
		}
	}

	/** Remove a proxy from the model */
	protected int doProxyRemoved(ActionPlan proxy) {
		if(selected == proxy) {
			removed = proxy;
			setSelectedItem(null);
		}
		return super.doProxyRemoved(proxy);
	}

	/** Get the selected item */
	public Object getSelectedItem() {
		return selected;
	}

	/** Set the selected item */
	public void setSelectedItem(Object p) {
		if(p instanceof ActionPlan)
			selected = (ActionPlan)p;
		else
			selected = null;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateCheckBox();
			}
		});
	}

	/** Update the check box */
	protected void updateCheckBox() {
		ActionPlan p = selected;
		check_box.removeChangeListener(listener);
		check_box.setEnabled(p != null);
		if(p != null) {
			check_box.setSelected(p.getDeployed());
			check_box.addChangeListener(listener);
		} else
			check_box.setSelected(false);
	}
}
