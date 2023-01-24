/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.wysiwyg.selector.WMsgSelectorForm;

/**
 * Message pattern table panel
 *
 * @author Doug Lau
 */
public class MsgPatternTablePanel extends ProxyTablePanel<MsgPattern> {

	/** User session */
	private final Session session;

	final MsgPatternPanel pat_pnl;

	/** Create a WYSIWYG Selector button action */
	private final IAction wysiwyg_act =
		new IAction("wysiwyg.selector.edit")
	{
		protected void doActionPerformed(ActionEvent e) {
			MsgPattern pat = getSelectedProxy();
			session.getDesktop().show(
				new WMsgSelectorForm(session, pat));
		}
	};

	/** Create a new pattern table panel */
	public MsgPatternTablePanel(Session s) {
		super(new MsgPatternTableModel(s));
		session = s;
		pat_pnl = new MsgPatternPanel(s);
	}

	@Override public void initialize() {
		super.initialize();
		pat_pnl.initialize();
	}

	@Override public void dispose() {
		pat_pnl.dispose();
		super.dispose();
	}

	@Override protected void selectProxy() {
		super.selectProxy();
		pat_pnl.setMsgPattern(getSelectedProxy());
	}

	@Override protected void addCreateDeleteWidgets(
		GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		super.addCreateDeleteWidgets(hg, vg);
		hg.addGap(2 * UI.hgap);
		JButton edit_btn = new JButton(wysiwyg_act);
		hg.addComponent(edit_btn);
		vg.addComponent(edit_btn);
		wysiwyg_act.setEnabled(false);
	}

	@Override public void updateButtonPanel() {
		super.updateButtonPanel();
		MsgPattern pat = getSelectedProxy();
		boolean permitted = WMsgSelectorForm.isPermitted(session);
		boolean canEdit = pat != null;
		boolean canAdd = model.canAdd();
		wysiwyg_act.setEnabled(permitted && canEdit && canAdd);
	}
}
