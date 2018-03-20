/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2018  Minnesota Department of Transportation
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

import java.awt.GridLayout;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing quick messages.
 * @see QuickMessage, QuickMessageImpl
 *
 * @author Doug Lau
 * @author Michael Darter
 */
public class QuickMessageForm extends ProxyTableForm<QuickMessage> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(QuickMessage.SONAR_TYPE);
	}

	/** Quick message table panel */
	static private class QuickMsgTablePanel
		extends ProxyTablePanel<QuickMessage>
	{
		private final QuickMessagePanel qm_pnl;
		private QuickMsgTablePanel(Session s) {
			super(new QuickMessageTableModel(s));
			qm_pnl = new QuickMessagePanel(s, true);
		}
		@Override
		public void initialize() {
			super.initialize();
			qm_pnl.initialize();
		}
		@Override
		public void dispose() {
			qm_pnl.dispose();
			super.dispose();
		}
		@Override
		protected void selectProxy() {
			super.selectProxy();
			qm_pnl.setQuickMsg(getSelectedProxy());
		}
	}

	/** Quick message panel */
	private final QuickMessagePanel qm_pnl;

	/** Create a new quick message form.
	 * @param s Session. */
	public QuickMessageForm(Session s) {
		super(I18N.get("quick.messages"), new QuickMsgTablePanel(s));
		qm_pnl = ((QuickMsgTablePanel) panel).qm_pnl;
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		setLayout(new GridLayout(1, 2));
		add(qm_pnl);
	}
}
