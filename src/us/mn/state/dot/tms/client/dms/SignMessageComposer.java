/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
 * Copyright (C) 2009-2010  AHMCT, University of California
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.SignMessageHelper.DMS_MESSAGE_MAX_PAGES;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.DECISECONDS;

/**
 * A sign message composer is GUI for composing DMS messages.  It uses a number
 * of optional controls which appear or do not appear on screen as a function
 * of system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageComposer extends JPanel {

	/** User session */
	private final Session session;

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** DMS sign group type cache */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache */
	private final TypeCache<SignText> sign_text;

	/** Cache of font proxy objects */
	private final TypeCache<Font> fonts;

	/** Maximum number of lines on a sign */
	private final int max_lines;

	/** Minimum number of pages for a sign message */
	private final int min_pages;

	/** Number of pages on selected sign */
	private int n_pages;

	/** Number of lines on selected sign */
	private int n_lines;

	/** Default font number */
	private int default_font = FontHelper.DEFAULT_FONT_NUM;

	/** Sign text model for the selected sign */
	private SignTextModel st_model;

	/** Tab pane for message pages */
	private final JTabbedPane page_tab = new JTabbedPane(JTabbedPane.RIGHT);

	/** Panels for all pages of message */
	private final PagePanel[] pages;

	/** Quick message label */
	private final ILabel quick_lbl = new ILabel("dms.quick.message");

	/** Combobox used to select a quick message */
	private final QuickMessageCBox quick_cbx;

	/** Duration label */
	private final ILabel dur_lbl = new ILabel("dms.duration");

	/** Used to select the expires time for a message (optional) */
	private final JComboBox dur_cbx = new JComboBox(Expiration.values());

	/** Page on time label */
	private final ILabel pg_on_lbl = new ILabel("dms.page.on.time");

	/** Page on time spinner */
	private final PgTimeSpinner pg_on_spn;

	/** AMBER alert label */
	private final ILabel alert_lbl = new ILabel("dms.alert");

	/** AMBER Alert checkbox */
	private final JCheckBox alert_chx = new JCheckBox();

	/** Clear action */
	private final IAction clear = new IAction("dms.clear") {
		protected void doActionPerformed(ActionEvent e) {
			clearWidgets();
		}
	};

	/** Button to clear the selected message */
	private final JButton clear_btn = new JButton(clear);

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
	private final IAction query_msg = new IAction("dms.query.msg",
		SystemAttrEnum.DMS_QUERYMSG_ENABLE)
	{
		protected void doActionPerformed(ActionEvent e) {
			dispatcher.queryMessage();
		}
	};

	/** Button to query the DMS message */
	private final JButton query_btn = new JButton(query_msg);

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	protected int adjusting = 0;

	/** Listener for spinner change events */
	protected final ChangeListener spin_listener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			updateMessage();
		}
	};

	/** Update the DMS dispatcher message */
	public void updateMessage() {
		if(adjusting == 0) {
			adjusting++;
			dispatcher.setMessage(getMessage());
			dispatcher.selectPreview(true);
			adjusting--;
		}
	}

	/** Create a new sign message composer */
	public SignMessageComposer(Session s, DMSDispatcher ds,
		DMSManager manager)
	{
		session = s;
		dispatcher = ds;
		DmsCache dc = s.getSonarState().getDmsCache();
		dms_sign_groups = dc.getDmsSignGroups();
		sign_text = dc.getSignText();
		fonts = dc.getFonts();
		max_lines = SystemAttrEnum.DMS_MAX_LINES.getInt();
		n_lines = max_lines;
		min_pages = SystemAttrEnum.DMS_MESSAGE_MIN_PAGES.getInt();
		n_pages = min_pages;
		pages = new PagePanel[DMS_MESSAGE_MAX_PAGES];
		for(int i = 0; i < pages.length; i++)
			pages[i] = new PagePanel(this, i);
		quick_cbx = new QuickMessageCBox(dispatcher);
		pg_on_spn = new PgTimeSpinner();
		pg_on_spn.addChangeListener(spin_listener);
		blank_msg = new BlankDmsAction(dispatcher);
		manager.setBlankAction(blank_msg);
		blank_btn = new JButton(blank_msg);
		add(createAllWidgets());
		initializeWidgets();
	}

	/** Create all widgets */
	private JPanel createAllWidgets() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(page_tab, BorderLayout.CENTER);
		Box vbox = Box.createVerticalBox();
		vbox.add(createDeployPanel());
		vbox.add(createButtonPanel());
		panel.add(vbox, BorderLayout.EAST);
		return panel;
	}

	/** Create the deploy panel */
	private JPanel createDeployPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		panel.setLayout(gl);
		GroupLayout.ParallelGroup lg = gl.createParallelGroup(
			GroupLayout.Alignment.TRAILING);
		GroupLayout.ParallelGroup vg = gl.createParallelGroup(
			GroupLayout.Alignment.LEADING);
		// Quick message widgets
		quick_lbl.setLabelFor(quick_cbx);
		lg.addComponent(quick_lbl);
		vg.addComponent(quick_cbx);
		GroupLayout.ParallelGroup g1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g1.addComponent(quick_lbl).addComponent(quick_cbx);
		// Duraton widgets
		dur_lbl.setLabelFor(dur_cbx);
		lg.addComponent(dur_lbl);
		vg.addComponent(dur_cbx);
		GroupLayout.ParallelGroup g2 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g2.addComponent(dur_lbl).addComponent(dur_cbx);
		// Page on time widgets
		pg_on_lbl.setLabelFor(pg_on_spn);
		lg.addComponent(pg_on_lbl);
		vg.addComponent(pg_on_spn);
		GroupLayout.ParallelGroup g3 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g3.addComponent(pg_on_lbl).addComponent(pg_on_spn);
		// AMBER alert widgets
		alert_lbl.setLabelFor(alert_chx);
		lg.addComponent(alert_lbl);
		vg.addComponent(alert_chx);
		GroupLayout.ParallelGroup g4 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		g4.addComponent(alert_lbl).addComponent(alert_chx);
		// Finish group layout
		GroupLayout.SequentialGroup horz_g = gl.createSequentialGroup();
		horz_g.addGap(UI.hgap).addGroup(lg);
		horz_g.addGap(UI.hgap).addGroup(vg);
		gl.setHorizontalGroup(horz_g);
		GroupLayout.SequentialGroup vert_g = gl.createSequentialGroup();
		vert_g.addGap(UI.vgap).addGroup(g1).addGap(UI.vgap);
		vert_g.addGroup(g2).addGap(UI.vgap).addGroup(g3);
		vert_g.addGap(UI.vgap).addGroup(g4).addGap(UI.vgap);
		gl.setVerticalGroup(vert_g);
		return panel;
	}

	/** Create the button panel */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel();
		GroupLayout gl = new GroupLayout(panel);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		panel.setLayout(gl);
		GroupLayout.ParallelGroup bg = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		bg.addComponent(clear_btn);
		bg.addComponent(send_btn);
		bg.addComponent(blank_btn);
		bg.addComponent(query_btn);
		GroupLayout.SequentialGroup vert_g = gl.createSequentialGroup();
		vert_g.addGroup(bg);
		gl.setVerticalGroup(vert_g);
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		hg.addGroup(gl.createParallelGroup().addComponent(clear_btn));
		hg.addGap(UI.hgap * 2, UI.hgap * 4, UI.hgap * 64);
		hg.addGroup(gl.createParallelGroup().addComponent(send_btn));
		hg.addGap(UI.hgap);
		hg.addGroup(gl.createParallelGroup().addComponent(blank_btn));
		hg.addGap(UI.hgap);
		hg.addGroup(gl.createParallelGroup().addComponent(query_btn));
		gl.setHorizontalGroup(hg);
		return panel;
	}

	/** Set multiple sign selection mode */
	public void setMultiple(boolean m) {
		alert_lbl.setVisible(m);
		alert_chx.setEnabled(m);
		alert_chx.setVisible(m);
		alert_chx.setSelected(false);
		if(m)
			quick_cbx.setSelectedItem(null);
	}

	/** Clear the widgets */
	private void clearWidgets() {
		adjusting++;
		setTabPage(0);
		for(PagePanel pg: pages)
			pg.clearWidgets();
		dispatcher.setMessage("");
		pg_on_spn.setValue("");
		adjusting--;
	}

	/** Set tab to page specified */
	private void setTabPage(int p) {
		if(page_tab.getTabCount() > 0)
			page_tab.setSelectedIndex(p);
	}

	/** Dispose of the message selector */
	public void dispose() {
		removeAll();
		for(PagePanel pg: pages)
			pg.dispose();
		quick_cbx.dispose();
		pg_on_spn.removeChangeListener(spin_listener);
		setSignTextModel(null);
	}

	/** Update the message combo box models */
	public void setSign(DMS proxy, RasterBuilder b) {
		SignTextModel stm = createSignTextModel(proxy);
		setSignTextModel(stm);
		n_lines = DMSHelper.getLineCount(proxy);
		n_pages = calculateSignPages(stm);
		default_font = DMSHelper.getDefaultFontNumber(proxy);
		initializeWidgets();
		for(PagePanel pg: pages) {
			if(stm != null)
				pg.setModels(stm);
			pg.setBuilder(b);
		}
		quick_cbx.populateModel(proxy);
	}

	/** Calculate the number of pages for the selected sign */
	private int calculateSignPages(SignTextModel stm) {
		int ml = stm != null ? stm.getMaxLine() : max_lines;
		int np = calculateSignPages(ml, n_lines);
		return Math.min(DMS_MESSAGE_MAX_PAGES, Math.max(np, min_pages));
	}

	/** Calculate the number of pages for the sign.
	 * @param ml Number of lines in message library.
	 * @param nl Number of lines on sign face. */
	static private int calculateSignPages(int ml, int nl) {
		if(nl > 0)
			return 1 + Math.max(0, (ml - 1) / nl);
		else
			return 1;
	}

	/** Create a new sign text model */
	private SignTextModel createSignTextModel(DMS proxy) {
		if(proxy != null)
			return new SignTextModel(session, proxy);
		else
			return null;
	}

	/** Set a new sign text model */
	private void setSignTextModel(SignTextModel stm) {
		SignTextModel om = st_model;
		if(stm != null)
			stm.initialize();
		st_model = stm;
		if(om != null)
			om.dispose();
	}

	/** Initialize the widgets */
	private void initializeWidgets() {
		boolean cam = canAddMessages();
		for(int i = 0; i < n_pages; i++) {
			PagePanel pg = pages[i];
			pg.setEditMode(cam);
			pg.setLines(n_lines);
			setPage(i, pg);
		}
		while(n_pages < page_tab.getTabCount())
			page_tab.removeTabAt(n_pages);
		dur_cbx.setSelectedIndex(0);
		boolean dur = SystemAttrEnum.DMS_DURATION_ENABLE.getBoolean();
		dur_lbl.setVisible(dur);
		dur_cbx.setVisible(dur);
		boolean pg = PgTimeSpinner.getIEnabled();
		pg_on_lbl.setVisible(pg);
		pg_on_spn.setVisible(pg);
		query_btn.setVisible(query_msg.getIEnabled());
		clear_btn.setMargin(UI.buttonInsets());
		blank_btn.setMargin(UI.buttonInsets());
		query_btn.setMargin(UI.buttonInsets());
	}

	/** Check if the user can add messages */
	private boolean canAddMessages() {
		SignTextModel stm = st_model;
		if(stm != null)
			return stm.isLocalSignTextAddPermitted();
		else
			return false;
	}

	/** Set a page on one tab */
	private void setPage(int n, PagePanel page) {
		String title = "p." + (n + 1);
		if(n < page_tab.getTabCount()) {
			page_tab.setComponentAt(n, page);
			page_tab.setTitleAt(n, title);
		} else
			page_tab.addTab(title, page);
	}

	/** Enable or Disable the message selector */
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		if(!b)
			setMultiple(false);
		setTabPage(0);
		clear.setEnabled(b);
		adjusting++;
		for(PagePanel pnl: pages)
			pnl.setEnabled(b);
		boolean vis = quick_cbx.getItemCount() > 0;
		quick_lbl.setVisible(vis);
		quick_cbx.setVisible(vis);
		quick_cbx.setEnabled(b);
		dur_cbx.setEnabled(b);
		dur_cbx.setSelectedItem(0);
		pg_on_spn.setEnabled(b);
		alert_chx.setEnabled(b);
		send_msg.setEnabled(b && dispatcher.canSend());
		blank_msg.setEnabled(b && dispatcher.canSend());
		query_msg.setEnabled(b && dispatcher.canRequest());
		adjusting--;
	}

	/** Return a MULTI string using the contents of the widgets */
	public String getMessage() {
		String prefix = dispatcher.getPagePrefix();
		MultiString[] mess = new MultiString[n_pages];
		int fn = default_font;
		int p = 0;
		for(int i = 0; i < n_pages; i++) {
			mess[i] = pages[i].getMulti(fn, prefix);
			if(!mess[i].isBlank())
				p = i + 1;
			fn = pages[i].getFontNumber();
		}
		return combinePages(mess, p);
	}

	/** Build a MULTI string from an array of page strings */
	private String combinePages(MultiString[] mess, int p) {
		MultiString multi = new MultiString();
		for(int i = 0; i < p; i++) {
			if(i == 0) {
				if(PgTimeSpinner.getIEnabled()) {
					Interval poi =
						pg_on_spn.getValueInterval();
					int pt = poi.round(DECISECONDS);
					if(pt > 0)
						multi.setPageTimes(pt, null);
				}
			} else
				multi.addPage();
			multi.append(mess[i]);
		}
		return multi.toString();
	}

	/** Set the currently selected message */
	public void setMessage(String ms) {
		adjusting++;
		quick_cbx.setMessage(ms);
		pg_on_spn.setValue(ms);
		// Note: order here is crucial. The font cbox must be updated
		// first because the line combobox updates (each) result in 
		// intermediate preview updates which read the (incorrect) 
		// font from the font combobox.
		MultiString multi = new MultiString(ms);
		setSelectedFonts(multi);
		String[] lines = multi.getLines(n_lines);
		for(int i = 0; i < pages.length; i++)
			pages[i].setSelected(lines);
		adjusting--;
	}

	/** Get the selected duration */
	public Integer getDuration() {
		Expiration e = (Expiration)dur_cbx.getSelectedItem();
		if(e != null)
			return e.duration;
		else
			return null;
	}

	/** Get the selected priority */
	public DMSMessagePriority getPriority() {
		if(alert_chx.isSelected())
		       return DMSMessagePriority.ALERT;
		else
		       return DMSMessagePriority.OPERATOR;
	}

	/** Set all font comboboxes using the specified MultiString */
	private void setSelectedFonts(MultiString ms) {
		int[] fnum = ms.getFonts(default_font);
		for(int i = 0; i < pages.length; i++) {
			FontComboBox cbx = pages[i].font_cbx;
			if(i < fnum.length)
				cbx.setSelectedFontNumber(fnum[i]);
			else
				cbx.setSelectedFontNumber(default_font);
		}
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		SignTextModel stm = st_model;
		if(stm != null)
			stm.updateMessageLibrary();
	}

	/** Inner class for one page of a message */
	private class PagePanel extends JPanel {

		/** Page number (0-relative) */
		private final int n_page;

		/** Panels to wrap message line combo boxes */
		private final JPanel[] line_pnl;

		/** Message combo box widgets */
		private final MsgComboBox[] line_cbx;

		/** Font combo box widget */
		private final FontComboBox font_cbx;

		/** Create a new page panel */
		private PagePanel(SignMessageComposer composer, int p) {
			n_page = p;
			line_cbx = new MsgComboBox[max_lines];
			line_pnl = new JPanel[max_lines];
			font_cbx = new FontComboBox(fonts, composer);
			for(int i = 0; i < max_lines; i++) {
				line_cbx[i] = new MsgComboBox(composer);
				line_cbx[i].initialize();
			}
			initWidgets();
		}

		/** Initialize the page panel */
		private void initWidgets() {
			for(int i = 0; i < max_lines; i++)
				initLine(i);
			GroupLayout gl = new GroupLayout(this);
			gl.setAutoCreateGaps(false);
			gl.setAutoCreateContainerGaps(false);
			gl.setHonorsVisibility(false);
			setLayout(gl);
			GroupLayout.SequentialGroup hg =
				gl.createSequentialGroup();
			GroupLayout.ParallelGroup phg =gl.createParallelGroup();
			GroupLayout.SequentialGroup vg =
				gl.createSequentialGroup();
			for(int i = 0; i < max_lines; i++) {
				phg.addComponent(line_pnl[i]);
				GroupLayout.ParallelGroup pg =
					gl.createParallelGroup();
				vg.addGroup(pg.addComponent(line_pnl[i]));
			}
			if(FontComboBox.getIEnabled()) {
				Box box = createFontBox();
				phg.addComponent(box);
				GroupLayout.ParallelGroup pg =
					gl.createParallelGroup();
				vg.addGroup(pg.addComponent(box));
			}
			hg.addGroup(phg);
			gl.setHorizontalGroup(hg);
			gl.setVerticalGroup(vg);
		}

		/** Initialize one line panel */
		private void initLine(int i) {
			JPanel pnl = new JPanel(new BorderLayout());
			pnl.setBackground(Color.BLACK);
			if(i == 0)
				pnl.setBorder(UI.panelBorder());
			else
				pnl.setBorder(UI.noTopBorder());
			pnl.add(line_cbx[i]);
			line_pnl[i] = pnl;
		}

		/** Create a font box */
		private Box createFontBox() {
			ILabel label = new ILabel("font");
			label.setLabelFor(font_cbx);
			Box box = Box.createHorizontalBox();
			box.add(label);
			box.add(Box.createHorizontalStrut(UI.hgap));
			box.add(font_cbx);
			return box;
		}

		/** Clear the widgets */
		private void clearWidgets() {
			for(MsgComboBox cbox: line_cbx)
				cbox.setSelectedIndex(-1);
			font_cbx.setSelectedFontNumber(default_font);
		}

		/** Dispose of page panel */
		private void dispose() {
			removeAll();
			for(MsgComboBox cbox: line_cbx)
				cbox.dispose();
			font_cbx.dispose();
		}

		/** Enable or Disable the page panel */
		public void setEnabled(boolean b) {
			for(MsgComboBox cbx: line_cbx)
				cbx.setEnabled(b);
			font_cbx.setEnabled(b);
			super.setEnabled(b);
		}

		/** Set the edit mode */
		private void setEditMode(boolean cam) {
			for(MsgComboBox cbox: line_cbx)
				cbox.setEditMode(cam);
		}

		/** Set the number of lines on the page */
		private void setLines(int nl) {
			for(int i = 0; i < max_lines; i++)
				line_pnl[i].setVisible(i < nl);
		}

		/** Set the message combo box models */
		private void setModels(SignTextModel stm) {
			for(int n = 0; n < max_lines; n++) {
				MsgComboBox cl = line_cbx[n];
				short i = (short)(n_page * n_lines + n + 1);
				if(n < n_lines)
					cl.setModel(stm.getLineModel(i));
				else
					cl.setModel(new DefaultComboBoxModel());
			}
		}

		/** Set the raster builder */
		private void setBuilder(RasterBuilder rb) {
			font_cbx.setBuilder(rb);
		}

		/** Set the selected lines */
		private void setSelected(String[] lines) {
			for(int n = 0; n < max_lines; n++) {
				MsgComboBox cl = line_cbx[n];
				int i = n_page * n_lines + n;
				if(i < lines.length)
					cl.getModel().setSelectedItem(lines[i]);
				else if(cl.getItemCount() > 0)
					cl.setSelectedIndex(0);
			}
		}

		/** Get a MULTI string for the page.
		 * @param n_font Current font number.
		 * @param prefix MULTI prefix for each page.
		 * @return MULTI string for the page. */
		private MultiString getMulti(int n_font, String prefix) {
			MultiString multi = new MultiString(prefix);
			String[] mess = new String[n_lines];
			int m = 0;
			for(int i = 0; i < mess.length; i++) {
				mess[i] = line_cbx[i].getMessage();
				if(mess[i].length() > 0)
					m = i + 1;
			}
			for(int i = 0; i < m; i++) {
				if(i == 0) {
					int fn = getFontNumber();
					if(fn != n_font)
						multi.setFont(fn, null);
				} else
					multi.addLine(null);
				multi.addSpan(mess[i]);
			}
			return multi;
		}

		/** Get the font number for the page */
		private int getFontNumber() {
			Integer f = font_cbx.getFontNumber();
			return f != null ? f : default_font;
		}
	}
}
