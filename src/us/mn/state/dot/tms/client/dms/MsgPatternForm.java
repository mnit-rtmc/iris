/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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
import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.wysiwyg.selector.WMsgSelectorForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing message patterns.
 *
 * @see us.mn.state.dot.tms.MsgPattern
 *
 * @author Doug Lau
 * @author Michael Darter
 */
public class MsgPatternForm extends ProxyTableForm<MsgPattern> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(MsgPattern.SONAR_TYPE);
	}

	/** Message pattern table panel */
	static private class MsgPatternTablePanel
		extends ProxyTablePanel<MsgPattern>
	{
		/** User session */
		private final Session session;

		private final MsgPatternPanel pat_pnl;

		private MsgPatternTablePanel(Session s) {
			super(new MsgPatternTableModel(s));
			pat_pnl = new MsgPatternPanel(s, true);
			session = s;
		}

		@Override
		public void initialize() {
			super.initialize();
			pat_pnl.initialize();
		}

		@Override
		public void dispose() {
			pat_pnl.dispose();
			super.dispose();
		}

		@Override
		protected void selectProxy() {
			super.selectProxy();
			pat_pnl.setMsgPattern(getSelectedProxy());
		}

		@Override
		protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
				GroupLayout.ParallelGroup vg)
		{
			super.addCreateDeleteWidgets(hg, vg);
			hg.addGap(2 * UI.hgap);
			JButton edit_btn = new JButton(wysiwyg_selector);
			hg.addComponent(edit_btn);
			vg.addComponent(edit_btn);
			wysiwyg_selector.setEnabled(false);
		}

		/** Update the button panel */
		public void updateButtonPanel() {
			super.updateButtonPanel();
			MsgPattern pat = getSelectedProxy();
			boolean permitted = WMsgSelectorForm.isPermitted(session);
			boolean canEdit = pat != null && pat.getSignGroup() != null;
			boolean canAdd = model.canAdd();
			wysiwyg_selector.setEnabled(permitted && canEdit && canAdd);
		}

		/** Create a WYSIWYG Selector button action */
		@SuppressWarnings("serial")
		private final IAction wysiwyg_selector =
			new IAction("wysiwyg.selector.edit")
		{
			protected void doActionPerformed(ActionEvent e) {
				MsgPattern pat = getSelectedProxy();
				SignGroup sg = pat.getSignGroup();
				session.getDesktop().show(
					new WMsgSelectorForm(session, pat, sg));
			}
		};
	}

	/** Message pattern panel */
	private final MsgPatternPanel pat_pnl;

	/** Create a new message pattern form.
	 * @param s Session. */
	public MsgPatternForm(Session s) {
		super(I18N.get("msg.patterns"), new MsgPatternTablePanel(s));
		pat_pnl = ((MsgPatternTablePanel) panel).pat_pnl;
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		setLayout(new GridLayout(1, 2));
		add(pat_pnl);
	}
}
