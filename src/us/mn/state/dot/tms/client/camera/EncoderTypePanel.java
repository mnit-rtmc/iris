/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import javax.swing.GroupLayout;
import javax.swing.JPanel;
import us.mn.state.dot.tms.EncoderStream;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for editing encoder types and streams.
 *
 * @author Douglas Lau
 */
public class EncoderTypePanel extends JPanel {

	/** User session */
	private final Session session;

	/** Encoder type table panel */
	private final ProxyTablePanel<EncoderType> et_pnl;

	/** Encoder stream table panel */
	private final ProxyTablePanel<EncoderStream> stream_pnl;

	/** Create a new encoder type panel */
	public EncoderTypePanel(Session s) {
		setBorder(UI.border);
		session = s;
		et_pnl = new ProxyTablePanel<EncoderType>(
			new EncoderTypeModel(s))
		{
			protected void selectProxy() {
				selectEncoderType();
				super.selectProxy();
			}
		};
		stream_pnl = new ProxyTablePanel<EncoderStream>(
			new EncoderStreamModel(s, null));
	}

	/** Initializze the panel */
	public void initialize() {
		et_pnl.initialize();
		stream_pnl.initialize();
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
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addComponent(et_pnl);
		hg.addGap(UI.hgap);
		hg.addComponent(stream_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		vg.addComponent(et_pnl);
		vg.addComponent(stream_pnl);
		return vg;
	}

	/** Dispose of the panel */
	public void dispose() {
		et_pnl.dispose();
		stream_pnl.dispose();
		removeAll();
	}

	/** Change the selected encoder type */
	private void selectEncoderType() {
		EncoderType et = et_pnl.getSelectedProxy();
		stream_pnl.setModel(new EncoderStreamModel(session, et));
	}
}
