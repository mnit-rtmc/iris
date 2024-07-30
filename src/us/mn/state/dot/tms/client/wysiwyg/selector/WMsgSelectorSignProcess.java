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
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.wysiwyg.selector.WMsgSelectorForm;

/** Worker thread that handles background tasks
 * for the WYSIWYG message selector, checking
 * that sign exists.
 *
 * @author Gordon Parikh - SRF Consulting
 */
public class WMsgSelectorSignProcess extends
	SwingWorker<ArrayList<MsgPattern>, Boolean>
{
	private final Session session;
	private final String signName;
	private final WMsgSelectorForm selectorForm;

	public WMsgSelectorSignProcess(Session s, String sName,
		WMsgSelectorForm sForm)
	{
		session = s;
		signName = sName;
		selectorForm = sForm;
	}

	/** Check that the sign exists and return a list of messages */
	@Override
	protected ArrayList<MsgPattern> doInBackground() throws Exception {
		DMS dms = DMSHelper.lookup(signName);
		if (dms == null)
			return null;

		selectorForm.setSelectedDMS(dms);
		SignConfig dmsConfig = dms.getSignConfig();
		ArrayList<MsgPattern> plist = new ArrayList<MsgPattern>();

		Hashtags tags = new Hashtags(dms.getNotes());
		Iterator<MsgPattern> pit = MsgPatternHelper.iterator();
		while (pit.hasNext()) {
			MsgPattern pat = pit.next();
			if (tags.contains(pat.getComposeHashtag()))
				plist.add(pat);
		}
		return plist;
	}

	@Override protected void done() {
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
