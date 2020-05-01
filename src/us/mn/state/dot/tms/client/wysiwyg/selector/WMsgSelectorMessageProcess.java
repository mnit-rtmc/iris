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

import javax.swing.SwingWorker;

import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.wysiwyg.selector.WMsgSelectorForm;
import us.mn.state.dot.tms.utils.I18N;

/** Worker thread that handles background tasks
 * for the WYSIWYG message selector, checking 
 * that signs/sign groups exist.
 * 
 * @author Gordon Parikh - SRF Consulting
 */
public class WMsgSelectorMessageProcess extends SwingWorker<Boolean,Boolean> {

	protected final Session session;
	protected final String messageName;
	protected WMsgSelectorForm selectorForm;
	
	public WMsgSelectorMessageProcess(Session s, String mName, WMsgSelectorForm sForm) {
		this.session = s;
		this.messageName = mName;
		this.selectorForm = sForm;
	}
	
	/** Check that the message exists */
	protected Boolean doInBackground() throws Exception {
		// lookup the message, set the selectedMessage in the form (null or
		// not), and return whether or not the message exists 
		QuickMessage qm = QuickMessageHelper.lookup(messageName);
		selectorForm.setSelectedMessage(qm);
		return qm != null;
	}
	
	protected void done() {
		try {
			Boolean qmExists = get();
			String selectMsg = I18N.get("wysiwyg.selector.select_sign");
			String errorMsg = I18N.get("wysiwyg.selector.error");
			if (!qmExists && !messageName.equals(selectMsg)
					&& !messageName.equals(errorMsg)) {
				// if the message doesn't exist, show a warning
				session.getDesktop().show(new WMsgWarningForm(
						session, selectorForm, "QuickMessage"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}	
}
