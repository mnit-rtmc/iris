/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

package us.mn.state.dot.tms.client.dms;

import java.awt.event.ItemEvent;
import java.rmi.RemoteException;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;

import us.mn.state.dot.tms.TrafficDeviceAttributeHelper;
import us.mn.state.dot.tms.client.dms.DMSDispatcher;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * Checkbox to indicate if a particular DMS is part of an automated warning
 * system or not. This functionality is agency specific.
 * @see DMSImpl, DMS, DMSDispatcher
 * @author Michael Darter
 */
public class AwsCheckBox extends JCheckBox implements ItemListener
{
	/* parent container */
	protected DMSDispatcher m_dmsDispatcher;

	/* constructor */
	public AwsCheckBox(DMSDispatcher dmsDispatcher) {
		super(I18NMessages.get("DMSDispatcher.AwsCheckBox"));
		assert dmsDispatcher != null;
		m_dmsDispatcher = dmsDispatcher;
		addItemListener(this);
	}

	/** Set the current overwrite state in DMSImpl */
	/*
	public void setOverwrite(boolean state) {
		//System.err.println("AwsCheckBox.setOverwrite() called. state="+state);
		DMSProxy dms = m_dmsDispatcher.getSelectedDms();
		if(dms == null)
			return;
		if(dms.dms != null) {
			try {
				dms.dms.setOverwrite(state);
			} catch(RemoteException ex) {}
		}
	}
	*/

	/** Get overwrite state from DMSImpl */
	/*
	public boolean getOverwrite() {
		//System.err.println("AwsCheckBox.getOverwrite() called.");
		DMSProxy dms = m_dmsDispatcher.getSelectedDms();
		if(dms == null)
			return false;
		boolean state = false;
		if(dms.dms != null) {
			try {
				state = dms.dms.getOverwrite();
			} catch(RemoteException ex) {}
		}
		//System.err.println("AwsCheckBox.getOverwrite() state="+state);
		return state;
	}
	*/

	/** listener for state change */
	public void itemStateChanged(ItemEvent e) {
		//if (this == e.getItemSelectable() )
		//updateDmsState();
	}

	/** Set the initial state */
	public void setDefaultSelection() {
		if(m_dmsDispatcher == null || m_dmsDispatcher.selectedSign==null)
			return;
		String id = m_dmsDispatcher.selectedSign.getId();
		if(id == null)
			return;
		this.setSelected(TrafficDeviceAttributeHelper.awsControlled(id));
	}
}

