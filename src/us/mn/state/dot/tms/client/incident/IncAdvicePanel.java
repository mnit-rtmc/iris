/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import javax.swing.GroupLayout;
import javax.swing.JTextField;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Panel for incident advices.
 *
 * @author Douglas Lau
 */
public class IncAdvicePanel extends ProxyTablePanel<IncAdvice> {

	/** Sign group label */
	private final ILabel sign_group_lbl = new ILabel("dms.group");

	/** Sign group field */
	private final JTextField sign_group_txt = new JTextField(16);

	/** Incident advice model */
	private final IncAdviceTableModel model;

	/** Create a new incident advice panel */
	public IncAdvicePanel(IncAdviceTableModel m) {
		super(m);
		model = m;
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		sign_group_lbl.setEnabled(false);
		sign_group_txt.setEnabled(false);
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(sign_group_lbl);
		vg.addComponent(sign_group_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(sign_group_txt);
		vg.addComponent(sign_group_txt);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Update the button panel */
	@Override
	public void updateButtonPanel() {
		sign_group_lbl.setEnabled(model.canAdd());
		sign_group_txt.setEnabled(model.canAdd());
		super.updateButtonPanel();
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		String sgt = sign_group_txt.getText().trim();
		SignGroup sg = SignGroupHelper.lookup(sgt);
		if (sg != null)
			model.create(sg);
		sign_group_txt.setText("");
	}
}
