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

	/** Sign text model for the selected sign */
	private SignTextModel st_model;

	/** Tab pane for message pages */
	private final JTabbedPane page_tab = new JTabbedPane(JTabbedPane.RIGHT);

	/** Panels for all pages of message */
	private final ComposerPagePanel[] pages;

	/** Duration label */
	private final ILabel dur_lbl = new ILabel("dms.duration");

	/** Used to select the expires time for a message (optional) */
	private final JComboBox<Expiration> dur_cbx =
		new JComboBox<Expiration>(Expiration.values());

	/** Button to send composed message */
	private final JButton send_btn;

	/** Button to blank selected signs */
	private final JButton blank_btn;

	/** Number of pages on selected sign */
	private int n_pages;

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
			dispatcher.setComposedMulti(getComposedMulti());
			if (unlink_incident)
				dispatcher.unlinkIncident();
			dispatcher.selectPreview(true);
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
		n_pages = 1;
		n_lines = MAX_LINES;
		pages = new ComposerPagePanel[MAX_PAGES];
		for (int i = 0; i < pages.length; i++)
			pages[i] = new ComposerPagePanel(this, n_lines, i);
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
		  .addComponent(page_tab)
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
		vg.addComponent(page_tab);
		vg.addGroup(vg1);
		gl.setVerticalGroup(vg);
		setLayout(gl);
	}

	/** Clear the widgets */
	private void clearWidgets() {
		adjusting++;
		pattern_cbx.setSelectedItem(null);
		setTabPage(0);
		for (ComposerPagePanel pg: pages)
			pg.clearWidgets();
		dispatcher.setComposedMulti("");
		dispatcher.unlinkIncident();
		adjusting--;
	}

	/** Set tab to page specified */
	private void setTabPage(int p) {
		if (page_tab.getTabCount() > 0)
			page_tab.setSelectedIndex(p);
	}

	/** Dispose of the message selector */
	public void dispose() {
		pattern_cbx.removeActionListener(pattern_listener);
		removeAll();
		for (ComposerPagePanel pg: pages)
			pg.dispose();
		setSignTextModel(null);
	}

	/** Set the selected sign */
	public void setSelectedSign(DMS proxy) {
		adjusting++;
		pattern_cbx.populateModel(proxy);
		SignTextModel stm = createSignTextModel(proxy);
		setSignTextModel(stm);
		n_lines = DMSHelper.getLineCount(proxy);
		n_pages = calculateSignPages(stm);
		initializeWidgets();
		for (ComposerPagePanel pg: pages) {
			pg.setModels(stm);
		}
		adjusting--;
	}

	/** Calculate the number of pages for the selected sign */
	private int calculateSignPages(SignTextModel stm) {
		int ml = (stm != null) ? stm.getLastLine() : MAX_LINES;
		int np = calculateSignPages(ml, n_lines);
		return Math.min(MAX_PAGES, np);
	}

	/** Calculate the number of pages for the sign.
	 * @param ml Number of lines in message library.
	 * @param nl Number of lines on sign face. */
	static private int calculateSignPages(int ml, int nl) {
		if (nl > 0)
			return 1 + Math.max(0, (ml - 1) / nl);
		else
			return 1;
	}

	/** Create a new sign text model */
	private SignTextModel createSignTextModel(DMS proxy) {
		return (proxy != null)
		      ? new SignTextModel(session, proxy)
		      : null;
	}

	/** Set a new sign text model */
	private void setSignTextModel(SignTextModel stm) {
		SignTextModel om = st_model;
		if (stm != null)
			stm.initialize();
		st_model = stm;
		if (om != null)
			om.dispose();
	}

	/** Initialize the widgets */
	private void initializeWidgets() {
		clear_btn.setMargin(UI.buttonInsets());
		send_btn.setMargin(UI.buttonInsets());
		blank_btn.setMargin(UI.buttonInsets());
		boolean cam = canAddMessages();
		for (int i = 0; i < n_pages; i++) {
			ComposerPagePanel pg = pages[i];
			pg.setEditMode(cam);
			pg.setLines(n_lines);
			setPage(i, pg);
		}
		while (n_pages < page_tab.getTabCount())
			page_tab.removeTabAt(n_pages);
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

	/** Check if the user can add messages */
	private boolean canAddMessages() {
		SignTextModel stm = st_model;
		return (stm != null)
		      ? stm.isLocalSignTextAddPermitted()
		      : false;
	}

	/** Set a page on one tab */
	private void setPage(int n, ComposerPagePanel page) {
		String title = Integer.toString(n + 1);
		if (n < page_tab.getTabCount()) {
			page_tab.setComponentAt(n, page);
			page_tab.setTitleAt(n, title);
		} else
			page_tab.addTab(title, page);
	}

	/** Enable or Disable the message composer */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		adjusting++;
		setTabPage(0);
		pattern_cbx.setEnabled(b);
		for (ComposerPagePanel pnl: pages)
			pnl.setEnabled(b);
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
		MultiString[] mess = new MultiString[n_pages];
		int p = 0;
		for (int i = 0; i < n_pages; i++) {
			mess[i] = pages[i].getMulti();
			if (!mess[i].isBlank())
				p = i + 1;
		}
		return concatenatePages(mess, p);
	}

	/** Concatenate an array of MULTI pages together.
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
		for (int i = 0; i < pages.length; i++)
			pages[i].setSelectedLines(lines);
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

	/** Update the message library with the currently selected messages */
	public void updateLibrary() {
		SignTextModel stm = st_model;
		if (stm != null)
			stm.updateLibrary();
	}
}
