/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for editing DMS message lines.
 *
 * @author Douglas Lau
 */
public class MsgLinePanel extends JPanel {

	/** User session */
	private final Session session;

	/** Hashtag filter label */
	private final ILabel hash_lbl = new ILabel("msg.line.hashtag");

	/** Hashtag filter text field */
	private final JTextField hash_txt = new JTextField(16);

	/** Message line table panel */
	private final ProxyTablePanel<MsgLine> line_pnl;

	/** Message line model */
	private MsgLineModel line_mdl;

	/** Create a new message line panel */
	public MsgLinePanel(Session s) {
		setBorder(UI.border);
		session = s;
		hash_txt.getDocument().addDocumentListener(
			new DocumentListener()
		{
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateHashtag();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateHashtag();
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateHashtag();
			}
		});
		line_mdl = new MsgLineModel(s, null);
		line_pnl = new ProxyTablePanel<MsgLine>(line_mdl) {
			@Override
			protected void selectProxy() {
				super.selectProxy();
				line_mdl.setSelected(getSelectedProxy());
			}
		};
	}

	/** Update the hashtag filter */
	private void updateHashtag() {
		String ht = hash_txt.getText().trim();
		if (ht.isEmpty())
			ht = null;
		line_mdl = new MsgLineModel(session, ht);
		line_pnl.setModel(line_mdl);
	}

	/** Initializze the panel */
	public void initialize() {
		line_pnl.initialize();
		layoutPanel();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		GroupLayout.SequentialGroup g0 = gl.createSequentialGroup();
		g0.addComponent(hash_lbl);
		g0.addGap(UI.hgap);
		g0.addComponent(hash_txt);
		hg.addGroup(g0);
		hg.addComponent(line_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup();
		g0.addComponent(hash_lbl);
		g0.addComponent(hash_txt);
		vg.addGroup(g0);
		vg.addGap(UI.hgap);
		vg.addComponent(line_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		line_pnl.dispose();
		removeAll();
	}
}
