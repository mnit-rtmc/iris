/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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

import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Widgets;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * The ComposerButtonPanel is a GUI panel for buttons related to the sign
 * composer panel.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class ComposerButtonPanel extends JPanel {

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** Message composer */
	private final SignMessageComposer composer;

	/** Clear action */
	private final IAction clear = new IAction("dms.clear") {
		protected void doActionPerformed(ActionEvent e) {
			composer.clearWidgets();
		}
	};

	/** Button to clear the selected message */
	private final JButton clear_btn = new JButton(clear);

	/** Action to store as quick-message */
	private final IAction store = new IAction("dms.quick.message.store", "",
		SystemAttrEnum.DMS_QUICKMSG_STORE_ENABLE)
	{
		protected void doActionPerformed(ActionEvent e) {
			composer.storeAsQuickMessage();
		}
	};

	/** Button to store the message in the composer as a quick-message */
	private final JButton store_btn = new JButton(store);

	/** Action used to send a message to the DMS */
	private final IAction send_msg = new IAction("dms.send") {
		protected void doActionPerformed(ActionEvent e) {
			dispatcher.sendSelectedMessage();
		}
	};

	/** Button to send the selected message */
	private final JButton send_btn = new JButton(send_msg);

	/** Action to blank selected DMS */
	private final BlankDmsAction blank_msg;

	/** Button to blank the selected message */
	private final JButton blank_btn;

	/** Action to query the DMS message (optional) */
	private final IAction query_msg = new IAction("dms.query.msg", "",
		SystemAttrEnum.DMS_QUERYMSG_ENABLE)
	{
		protected void doActionPerformed(ActionEvent e) {
			dispatcher.queryMessage();
		}
	};

	/** Button to query the DMS message */
	private final JButton query_btn = new JButton(query_msg);

	/** Create a new composer button panel */
	public ComposerButtonPanel(DMSManager manager, DMSDispatcher ds,
		SignMessageComposer smc)
	{
		dispatcher = ds;
		composer = smc;
		blank_msg = new BlankDmsAction(dispatcher);
		manager.setBlankAction(blank_msg);
		blank_btn = new JButton(blank_msg);
		layoutPanel();
		initializeWidgets();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		setLayout(gl);
		GroupLayout.ParallelGroup bg = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		bg.addComponent(clear_btn);
		bg.addComponent(store_btn);
		bg.addComponent(send_btn);
		bg.addComponent(blank_btn);
		bg.addComponent(query_btn);
		GroupLayout.SequentialGroup vert_g = gl.createSequentialGroup();
		vert_g.addGroup(bg);
		gl.setVerticalGroup(vert_g);
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addGroup(gl.createParallelGroup().addComponent(clear_btn));
		hg.addGap(UI.hgap);
		hg.addGroup(gl.createParallelGroup().addComponent(store_btn));
		hg.addGap(UI.hgap);
		hg.addGroup(gl.createParallelGroup().addComponent(send_btn));
		hg.addGap(UI.hgap);
		hg.addGroup(gl.createParallelGroup().addComponent(blank_btn));
		hg.addGap(UI.hgap);
		hg.addGroup(gl.createParallelGroup().addComponent(query_btn));
		gl.setHorizontalGroup(hg);
	}

	/** Initialize the widgets */
	private void initializeWidgets() {
		// set visibility for optional buttons
		store_btn.setVisible(store.getIEnabled());
		query_btn.setVisible(query_msg.getIEnabled());

		clear_btn.setMargin(UI.buttonInsets());
		store_btn.setMargin(UI.buttonInsets());
		query_btn.setMargin(UI.buttonInsets());
		// more prominent margins for send and blank
		send_btn.setMargin(new Insets(UI.vgap, UI.hgap, UI.vgap,
			UI.hgap));
		blank_btn.setMargin(new Insets(UI.vgap, UI.hgap, UI.vgap,
			UI.hgap));

		// less prominent fonts for store, clear, and query
		Font f = Widgets.deriveFont("Button.font", Font.PLAIN, 0.80);
		if (f != null) {
			clear_btn.setFont(f);
			store_btn.setFont(f);
			query_btn.setFont(f);
		}
	}

	/** Dispose of the button panel */
	public void dispose() {
		removeAll();
	}

	/** Enable or disable the button panel */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		clear.setEnabled(b);
		store.setEnabled(b);
		send_msg.setEnabled(b && dispatcher.canSend());
		blank_msg.setEnabled(b && dispatcher.canSend());
		query_msg.setEnabled(b && dispatcher.canRequest());
	}
}
