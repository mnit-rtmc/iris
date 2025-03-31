/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
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

	/** Maximum allowed number of pages in a message */
	static private final int MAX_PAGES = 6;

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
			updateMessage(true);
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

	/** Selected sign */
	private DMS dms;

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
		setTabRect(0);
		dur_cbx.setSelectedIndex(0);
		setComposedMulti("");
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
		if (!DMSHelper.objectEquals(proxy, dms)) {
			dms = proxy;
			pattern_cbx.populateModel(proxy);
			// if sign is null, pattern_listener doesn't call this
			if (proxy == null)
				pattern_cbx.setSelectedItem(null);
			dur_cbx.setSelectedIndex(0);
		}
	}

	/** Enable or Disable the message composer */
	@Override
	public void setEnabled(boolean b) {
		if (b != isEnabled()) {
			super.setEnabled(b);
			pattern_cbx.setEnabled(b);
			setTabRect(0);
			for (TextRectComposer rc: rects)
				rc.setEnabled(b);
			dur_cbx.setEnabled(b);
			dur_cbx.setSelectedIndex(0);
		}
	}

	/** Update the selected pattern */
	private void updatePattern() {
		MsgPattern pat = getMsgPattern();
		MsgLineFinder finder = MsgLineFinder.create(pat, dms);
		List<TextRect> trs = getPatternTextRects(pat);
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
			adjusting++;
			rc.setModels(finder, first, n_lines);
			adjusting--;
			if (i < rect_tab.getTabCount()) {
				rect_tab.setComponentAt(i, rc);
				rect_tab.setTitleAt(i, title);
			} else
				rect_tab.addTab(title, rc);
			first += n_lines;
		}
		// at least one tab required for proper layout
		if (rect_tab.getTabCount() < 1) {
			adjusting++;
			rects[0].setModels(null, 0, 0);
			adjusting--;
			rect_tab.addTab("", rects[0]);
		}
	}

	/** Get the text rectangles for the given pattern */
	private List<TextRect> getPatternTextRects(MsgPattern pat) {
		TextRect tr = fullTextRect();
		return (tr != null && pat != null)
		      ? tr.find(pat.getMulti())
		      : new ArrayList<TextRect>();
	}

	/** Get the selected message pattern */
	private MsgPattern getMsgPattern() {
		return pattern_cbx.getSelectedPattern();
	}

	/** Get MULTI string of selected message pattern */
	private String getPatternMulti() {
		MsgPattern pat = getMsgPattern();
		return (pat != null) ? pat.getMulti() : "";
	}

	/** Get the full text rectangle of the selected sign */
	private TextRect fullTextRect() {
		SignConfig sc = (dms != null) ? dms.getSignConfig() : null;
		return SignConfigHelper.textRect(sc);
	}

	/** Compose a MULTI string using the contents of the widgets */
	public String getComposedMulti() {
		TextRect tr = fullTextRect();
		if (tr != null) {
			ArrayList<String> lines = new ArrayList<String>();
			for (int i = 0; i < n_rects; i++)
				rects[i].getSelectedLines(lines);
			String ms = tr.fill(getPatternMulti(), lines);
			MultiString multi = new MultiString(ms);
			return multi.stripTrailingWhitespaceTags().toString();
		} else
			return "";
	}

	/** Set the composed MULTI string */
	public void setComposedMulti(String ms) {
		TextRect tr = fullTextRect();
		if (tr != null) {
			MsgPattern pat = pattern_cbx.findBestPattern(ms, tr);
			pattern_cbx.setSelectedItem(pat);
			String pat_ms = getPatternMulti();
			List<String> lines = tr.splitLines(pat_ms, ms);
			Iterator<String> lns = lines.iterator();
			adjusting++;
			for (int i = 0; i < n_rects; i++)
				rects[i].setSelectedLines(lns);
			adjusting--;
		} else {
			pattern_cbx.setSelectedItem(null);
			adjusting++;
			for (TextRectComposer rc: rects)
				rc.clearWidgets();
			adjusting--;
		}
	}

	/** Check if beacon should be flashing */
	public boolean getFlashBeacon() {
		MsgPattern pat = getMsgPattern();
		return (pat != null) && pat.getFlashBeacon();
	}

	/** Check if pixel service is enabled */
	public boolean getPixelService() {
		MsgPattern pat = getMsgPattern();
		return (pat != null) && pat.getPixelService();
	}

	/** Get the selected duration */
	public Integer getDuration() {
		Expiration e = (Expiration) dur_cbx.getSelectedItem();
		return (e != null) ? e.duration : null;
	}
}
