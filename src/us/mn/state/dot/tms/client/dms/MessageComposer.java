/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2022  Minnesota Department of Transportation
 * Copyright (C) 2008-2014  AHMCT, University of California
 * Copyright (C) 2021  Iteris Inc.
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
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPattern;
import static us.mn.state.dot.tms.SignMessage.MAX_LINES;
import static us.mn.state.dot.tms.SignMessage.MAX_PAGES;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Widgets;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Message composer GUI for sign messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
public class MessageComposer extends JPanel {

	/** User session */
	private final Session session;

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** Message pattern label */
	private final ILabel pattern_lbl = new ILabel("msg.pattern");

	/** Message pattern combo box */
	private final MsgPatternCBox pattern_cbx;

	/** Action listener for pattern combo box */
	private final ActionListener pattern_listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			updatePattern();
		}
	};

	/** Update the selected pattern */
	private void updatePattern() {
		if (adjusting == 0) {
			adjusting++;
			// FIXME: clear all page widgets
			adjusting--;
		}
		updateMessage(true);
	}

	/** Clear action */
	private final IAction clear_act = new IAction("dms.clear") {
		protected void doActionPerformed(ActionEvent e) {
			clearWidgets();
		}
	};

	/** Button to clear the selected message */
	private final JButton clear_btn = new JButton(clear_act);

	/** Tab pane for text rects */
	private final JTabbedPane rect_tab = new JTabbedPane(JTabbedPane.RIGHT);

	/** Panels for all text rectangles */
	private final TextRectComposer[] rects;

	/** Duration label */
	private final ILabel dur_lbl = new ILabel("dms.duration");

	/** Used to select the expires time for a message (optional) */
	private final JComboBox<Expiration> dur_cbx =
		new JComboBox<Expiration>(Expiration.values());

	/** Button to send composed message */
	private final JButton send_btn;

	/** Button to blank selected signs */
	private final JButton blank_btn;

	/** Number of text rectangles */
	private int n_rects;

	/** Number of lines on selected sign */
	private int n_lines;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	protected int adjusting = 0;

	/** Update the DMS dispatcher message */
	public void updateMessage(boolean unlink_incident) {
		if (adjusting == 0) {
			adjusting++;
			if (unlink_incident)
				dispatcher.unlinkIncident();
			dispatcher.updateMessage();
			adjusting--;
		}
	}

	/** Create a new message composer */
	public MessageComposer(Session s, DMSDispatcher ds,
		DMSManager manager)
	{
		session = s;
		dispatcher = ds;
		pattern_cbx = new MsgPatternCBox();
		pattern_cbx.addActionListener(pattern_listener);
		pattern_lbl.setLabelFor(pattern_cbx);
		dur_lbl.setLabelFor(dur_cbx);
		n_rects  = 1;
		n_lines = MAX_LINES;
		rects = new TextRectComposer[MAX_PAGES];
		for (int i = 0; i < rects.length; i++)
			rects[i] = new TextRectComposer(this, n_lines, i);
		send_btn = new JButton(dispatcher.getSendMsgAction());
		blank_btn = new JButton(dispatcher.getBlankMsgAction());
		layoutPanel();
		initializeWidgets();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		// horizontal layout
		GroupLayout.ParallelGroup hg0 = gl.createParallelGroup(
			GroupLayout.Alignment.TRAILING);
		hg0.addComponent(pattern_lbl)
		   .addComponent(pattern_cbx)
		   .addComponent(clear_btn);
		GroupLayout.SequentialGroup hgb = gl.createSequentialGroup();
		hgb.addComponent(send_btn)
		   .addGap(UI.hgap)
		   .addComponent(blank_btn);
		GroupLayout.ParallelGroup hg1 = gl.createParallelGroup();
		hg1.addComponent(dur_lbl)
		   .addComponent(dur_cbx)
		   .addGroup(hgb);
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addContainerGap()
		  .addGroup(hg0)
		  .addGap(UI.hgap)
		  .addComponent(rect_tab)
		  .addGap(UI.hgap)
		  .addGroup(hg1)
		  .addContainerGap();
		gl.setHorizontalGroup(hg);
		// vertical layout
		int vgap = 16 * UI.vgap;
		GroupLayout.SequentialGroup vg0 = gl.createSequentialGroup();
		vg0.addComponent(pattern_lbl)
		   .addGap(UI.vgap)
		   .addComponent(pattern_cbx)
		   .addGap(UI.vgap, vgap, vgap)
		   .addComponent(clear_btn);
		GroupLayout.ParallelGroup vgb = gl.createParallelGroup();
		vgb.addComponent(send_btn).addComponent(blank_btn);
		GroupLayout.SequentialGroup vg1 = gl.createSequentialGroup();
		vg1.addComponent(dur_lbl)
		   .addGap(UI.vgap)
		   .addComponent(dur_cbx)
		   .addGap(UI.vgap, vgap, vgap)
		   .addGroup(vgb);
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		vg.addGroup(vg0);
		vg.addComponent(rect_tab);
		vg.addGroup(vg1);
		gl.setVerticalGroup(vg);
		setLayout(gl);
	}

	/** Clear the widgets */
	private void clearWidgets() {
		adjusting++;
		pattern_cbx.setSelectedItem(null);
		setTabRect(0);
		for (TextRectComposer rc: rects)
			rc.clearWidgets();
		adjusting--;
		updateMessage(true);
	}

	/** Set tab to specified text rect */
	private void setTabRect(int r) {
		if (rect_tab.getTabCount() > 0)
			rect_tab.setSelectedIndex(r);
	}

	/** Dispose of the message selector */
	public void dispose() {
		pattern_cbx.removeActionListener(pattern_listener);
		removeAll();
		for (TextRectComposer rc: rects)
			rc.dispose();
	}

	/** Set the selected sign */
	public void setSelectedSign(DMS proxy) {
		adjusting++;
		pattern_cbx.populateModel(proxy);
		SignTextFinder stf = new SignTextFinder(proxy);
		n_lines = DMSHelper.getLineCount(proxy);
		initializeWidgets();
		for (TextRectComposer rc: rects)
			rc.setModels(stf);
		adjusting--;
	}

	/** Initialize the widgets */
	private void initializeWidgets() {
		clear_btn.setMargin(UI.buttonInsets());
		send_btn.setMargin(UI.buttonInsets());
		blank_btn.setMargin(UI.buttonInsets());
		for (int i = 0; i < n_rects; i++) {
			TextRectComposer rc = rects[i];
			rc.setEditMode();
			rc.setLines(n_lines);
			setRect(i, rc);
		}
		while (n_rects < rect_tab.getTabCount())
			rect_tab.removeTabAt(n_rects);
		dur_cbx.setSelectedIndex(0);

		// more prominent margins for send and blank
		send_btn.setMargin(new Insets(UI.vgap, UI.hgap, UI.vgap,
			UI.hgap));
		blank_btn.setMargin(new Insets(UI.vgap, UI.hgap, UI.vgap,
			UI.hgap));

		// less prominent font for clear button
		Font f = Widgets.deriveFont("Button.font", Font.PLAIN, 0.80);
		if (f != null)
			clear_btn.setFont(f);
	}

	/** Set a text rect on one tab */
	private void setRect(int n, TextRectComposer rc) {
		String title = Integer.toString(n + 1);
		if (n < rect_tab.getTabCount()) {
			rect_tab.setComponentAt(n, rc);
			rect_tab.setTitleAt(n, title);
		} else
			rect_tab.addTab(title, rc);
	}

	/** Enable or Disable the message composer */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		adjusting++;
		setTabRect(0);
		pattern_cbx.setEnabled(b);
		for (TextRectComposer rc: rects)
			rc.setEnabled(b);
		dur_cbx.setEnabled(b);
		dur_cbx.setSelectedItem(0);
		adjusting--;
	}

	/** Get the selected message pattern */
	public MsgPattern getMsgPattern() {
		return pattern_cbx.getSelectedPattern();
	}

	/** Compose a MULTI string using the contents of the widgets */
	public String getComposedMulti() {
		// FIXME: use multi based on selected pattern
		MultiString[] mess = new MultiString[n_rects];
		int p = 0;
		for (int i = 0; i < n_rects; i++) {
			mess[i] = rects[i].getMulti();
			if (!mess[i].isBlank())
				p = i + 1;
		}
		return concatenatePages(mess, p);
	}

	/** Concatenate an array of MULTI rects together.
	 * @param mess Array of page MULTI strings.
	 * @param p Number of non-blank pages.
	 * @return Combined MULTI string for all pages. */
	private String concatenatePages(MultiString[] mess, int p) {
		MultiBuilder mb = new MultiBuilder();
		for (int i = 0; i < p; i++) {
			if (i > 0)
				mb.addPage();
			mb.append(mess[i]);
		}
		return mb.toString();
	}

	/** Set the composed MULTI string */
	public void setComposedMulti(String ms) {
		adjusting++;
		MultiString multi = new MultiString(ms);
		String[] lines = multi.getLines(n_lines);
		for (int i = 0; i < rects.length; i++)
			rects[i].setSelectedLines(lines);
		adjusting--;
	}

	/** Check if beacon is enabled */
	public boolean isBeaconEnabled() {
		// FIXME: add component for this
		return false;
	}

	/** Get the selected duration */
	public Integer getDuration() {
		Expiration e = (Expiration) dur_cbx.getSelectedItem();
		return (e != null) ? e.duration : null;
	}
}
