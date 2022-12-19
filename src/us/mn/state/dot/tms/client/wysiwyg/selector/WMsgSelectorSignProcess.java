/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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
package us.mn.state.dot.tms.client.wysiwyg.selector;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingWorker;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.wysiwyg.selector.WMsgSelectorForm;

/** Worker thread that handles background tasks
 * for the WYSIWYG message selector, checking 
 * that signs/sign groups exist.
 * 
 * @author Gordon Parikh - SRF Consulting
 */
public class WMsgSelectorSignProcess extends
	SwingWorker<ArrayList<MsgPattern>, Boolean>
{
	protected final Session session;
	protected final String signOrGroupName;
	protected WMsgSelectorForm selectorForm;
	
	public WMsgSelectorSignProcess(Session s, String sName,
			WMsgSelectorForm sForm) {
		this.session = s;
		this.signOrGroupName = sName;
		this.selectorForm = sForm;
	}

	/** Check that the sign exists and return a list of messages */
	protected ArrayList<MsgPattern> doInBackground() throws Exception {
		DMS dms = null;
		SignGroup sg = null;
		SignConfig dmsConfig = null;
		Boolean signOrGroupExists = false;
		ArrayList<MsgPattern> plist = new ArrayList<MsgPattern>();

		// the "signName" we get is either the name of a sign
		// OR a sign group first try to look up the sign
		dms = DMSHelper.lookup(signOrGroupName);

		if (dms != null) {
			// if the sign exists, get the sign's config
			dmsConfig = dms.getSignConfig();
			signOrGroupExists = true;
			
			// set the selector form's selectedDMS (this does a bunch of
			// stuff)
			selectorForm.setSelectedDMS(dms);
		} else {
			// if the sign doesn't exist, try looking for a sign group
			sg = SignGroupHelper.lookup(signOrGroupName);

			if (sg != null) {
				// sign groups don't have a config, so just set that this
				// exists
				signOrGroupExists = true;

				// set the selector form's selectedSignGroup (and other stuff)
				selectorForm.setSelectedSignGroup(sg);
			}
		}

		if (signOrGroupExists) {
			// sign or group exists - get a list of messages

			// iterate through all MsgPatterns to find matching ones
			Iterator<MsgPattern> pit = MsgPatternHelper.iterator();
			while (pit.hasNext()) {
				MsgPattern pat = pit.next();

				// first check if the SignGroup matches
				SignGroup psg = pat.getSignGroup();
				// if it does, add the message to the list
				// and go to the next one
				if (psg != null && signOrGroupName.equals(psg.getName()))
					plist.add(pat);
				else if (dmsConfig != null) {
					// if the groups don't match but we have a sign config,
					// check if that matches
					SignConfig psc = pat.getSignConfig();
					if (psc != null && psc.getName().equals(
						dmsConfig.getName()))
					{
						plist.add(pat);
					}
				}
			}

			// return whatever messages we have
			return plist;
		} else {
			// sign doesn't exist anymore - show a warning
			return null;
		}
	}
	
	protected void done() {
		try {
			ArrayList<MsgPattern> plist = get();
			if (plist != null)
				selectorForm.updateMessageList(plist);
			else {
				// show a warning
				// get the object type to show in the warning
				String selectedType = "<unknown type>";
				if (selectorForm.getSelectedDMS() != null)
					selectedType = "DMS";
				else if (selectorForm.getSelectedSignGroup() != null)
					selectedType = "SignGroup";
				
				session.getDesktop().show(new WMsgWarningForm(
						session, selectorForm, selectedType));
			}
		} catch (Exception e) {
			// if something happens, clear the list (showing an error message)
			// otherwise something really bad could happen
			selectorForm.updateMessageList(true);
			e.printStackTrace();
		}
	}	
}