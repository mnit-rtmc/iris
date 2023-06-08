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
import javax.swing.JInternalFrame;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.wysiwyg.editor.WMsgEditorForm;

/**
 * Message pattern table panel
 *
 * @author Doug Lau
 */
public class MsgPatternTablePanel extends ProxyTablePanel<MsgPattern> {

	/** User session */
	private final Session session;

	final MsgPatternPanel pat_pnl;

	/** Create a WYSIWYG edit button action */
	private final IAction wysiwyg_act =
		new IAction("wysiwyg.selector.edit")
	{
		protected void doActionPerformed(ActionEvent e) {
			MsgPattern pat = getSelectedProxy();
			SignConfig sc = pat_pnl.getSelectedSignConfig();
			if (pat == null || sc == null)
				return;
			// Find a sign using the selected configuration
			DMS sign = null;
			for (DMS d : SignConfigHelper.getAllSigns(sc)) {
				sign = d;
				break;
			}
			if (sign == null)
				return;
			WMsgEditorForm editor = new WMsgEditorForm(session,
				pat, sign);
			JInternalFrame frame = session.getDesktop()
				.show(editor);
			editor.setFrame(frame);
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
		boolean canEdit = pat != null;
		boolean canAdd = model.canAdd();
		wysiwyg_act.setEnabled(canEdit && canAdd);
	}
}
