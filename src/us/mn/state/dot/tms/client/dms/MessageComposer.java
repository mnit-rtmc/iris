/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2023  Minnesota Department of Transportation
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import static us.mn.state.dot.tms.SignMessage.MAX_PAGES;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Widgets;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

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
			if (adjusting == 0) {
				adjusting++;
				updatePattern();
				adjusting--;
				updateMessage(true);
			}
		}
	};

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

	/** Sign text finder for selected sign */
	private SignTextFinder finder;

	/** Number of text rectangles in selected pattern */
	private int n_rects;

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
		rects = new TextRectComposer[MAX_PAGES];
		for (int i = 0; i < rects.length; i++)
			rects[i] = new TextRectComposer(this);
		n_rects  = 0;
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

	/** Initialize the widgets */
	private void initializeWidgets() {
		// less prominent font for clear button
		Font f = Widgets.deriveFont("Button.font", Font.PLAIN, 0.80);
		if (f != null)
			clear_btn.setFont(f);
		clear_btn.setMargin(UI.buttonInsets());
		// more prominent margins for send and blank buttons
		send_btn.setMargin(UI.insets());
		blank_btn.setMargin(UI.insets());
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
		finder = new SignTextFinder(proxy);
		pattern_cbx.populateModel(proxy);
		dur_cbx.setSelectedIndex(0);
		adjusting--;
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

	/** Update the selected pattern */
	private void updatePattern() {
		List<TextRect> trs = getPatternTextRects();
		n_rects = Math.min(trs.size(), rects.length);
		while (n_rects < rect_tab.getTabCount())
			rect_tab.removeTabAt(n_rects);
		int first = 1;
		int page_number = 0;
		char page_letter = 'a' - 1;
		for (int i = 0; i < n_rects; i++) {
			TextRect tr = trs.get(i);
			if (tr.page_number == page_number)
				page_letter++;
			else
				page_letter = 'a' - 1;
			page_number = tr.page_number;
			String title = (page_letter >= 'a')
				? "" + page_number + page_letter
				: "" + page_number;
			TextRectComposer rc = rects[i];
			int n_lines = tr.getLineCount();
			rc.setModels(finder, first, n_lines);
			rc.setEditMode();
			if (i < rect_tab.getTabCount()) {
				rect_tab.setComponentAt(i, rc);
				rect_tab.setTitleAt(i, title);
			} else
				rect_tab.addTab(title, rc);
			first += n_lines;
		}
	}

	/** Get the text rectangles for the selected pattern */
	private List<TextRect> getPatternTextRects() {
		MsgPattern pat = getMsgPattern();
		if (pat != null)
			return MsgPatternHelper.findTextRectangles(pat);
		else
			return new ArrayList<TextRect>();
	}

	/** Get the selected message pattern */
	public MsgPattern getMsgPattern() {
		MsgPattern pat = pattern_cbx.getSelectedPattern();
		if (pat != null)
			return pat;
		// make a "client" pattern just for composing
		DMS dms = dispatcher.getSingleSelection();
		if (dms != null) {
			SignConfig sc = dms.getSignConfig();
			if (sc != null)
				return new ClientMsgPattern(sc, "");
		}
		return null;
	}

	/** Compose a MULTI string using the contents of the widgets */
	public String getComposedMulti() {
		ArrayList<String> lines = new ArrayList<String>();
		for (int i = 0; i < n_rects; i++)
			rects[i].getSelectedLines(lines);
		MsgPattern pat = getMsgPattern();
		return MsgPatternHelper.fillTextRectangles(pat, lines);
	}

	/** Set the composed MULTI string */
	public void setComposedMulti(String ms) {
		MsgPattern pat = pattern_cbx.findBestPattern(ms);
		pattern_cbx.setSelectedItem(pat);
		// this makes a ClientMsgPattern if none selected
		pat = getMsgPattern();
		if (pat != null) {
			List<String> lines = MsgPatternHelper
				.splitLines(pat, ms);
			Iterator<String> lns = lines.iterator();
			for (int i = 0; i < n_rects; i++)
				rects[i].setSelectedLines(lns);
		}
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
