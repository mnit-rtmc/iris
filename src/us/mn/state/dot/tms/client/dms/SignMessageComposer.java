/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
import java.awt.GridLayout;
import javax.swing.Box;
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

/**
 * A sign message composer is GUI for composing DMS messages.  It uses a number
 * of optional controls which appear or do not appear on screen as a function
 * of system attributes.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageComposer extends JPanel {

	/** User session */
	protected final Session session;

	/** DMS dispatcher */
	protected final DMSDispatcher dispatcher;

	/** DMS sign group type cache */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache */
	protected final TypeCache<SignText> sign_text;

	/** Cache of font proxy objects */
	protected final TypeCache<Font> fonts;

	/** Maximum number of lines on a sign */
	private final int max_lines;

	/** Minimum number of pages for a sign message */
	private final int min_pages;

	/** Tab pane for message pages */
	private final JTabbedPane page_tab = new JTabbedPane(JTabbedPane.RIGHT);

	/** Default font number */
	private int default_font = FontHelper.DEFAULT_FONT_NUM;

	/** Raster builder for the selected sign */
	private RasterBuilder builder;

	/** Sign text model for the selected sign */
	private SignTextModel st_model;

	/** Message combo box widgets */
	private MsgComboBox[] cmbLine = new MsgComboBox[0];

	/** Font combo box widgets */
	private FontComboBox[] fontCmb = new FontComboBox[0];

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
		protected void do_perform() {
			clearWidgets();
		}
	};

	/** Button to clear the selected message */
	private final JButton clear_btn = new JButton(clear);

	/** Action used to send a message to the DMS */
	private final IAction send_msg = new IAction("dms.send") {
		protected void do_perform() {
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
		protected void do_perform() {
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
		min_pages = SystemAttrEnum.DMS_MESSAGE_MIN_PAGES.getInt();
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
		clearFonts();
		for(MsgComboBox cbox: cmbLine)
			cbox.setSelectedIndex(-1);
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
		disposeLines();
		quick_cbx.dispose();
		pg_on_spn.removeChangeListener(spin_listener);
		disposeEtcWidgets();
		setSignTextModel(null);
	}

	/** Dispose of the existing line widgets */
	protected void disposeLines() {
		for(MsgComboBox cbox: cmbLine)
			cbox.dispose();
		cmbLine = new MsgComboBox[0];
	}

	/** Dispose of the existing combobox widgets */
	protected void disposeEtcWidgets() {
		for(FontComboBox f: fontCmb)
			f.dispose();
		fontCmb = new FontComboBox[0];
	}

	/** Update the message combo box models */
	public void setSign(DMS proxy, RasterBuilder b) {
		builder = b;
		default_font = DMSHelper.getDefaultFontNumber(proxy);
		SignTextModel stm = createSignTextModel(proxy);
		setSignTextModel(stm);
		initializeWidgets();
		if(stm != null) {
			final MsgComboBox[] cl = cmbLine;	// Avoid races
			for(short i = 1; i <= cl.length; i++)
				cl[i - 1].setModel(stm.getLineModel(i));
		}
		quick_cbx.populateModel(proxy);
	}

	/** Create a new sign text model */
	protected SignTextModel createSignTextModel(DMS proxy) {
		if(proxy != null)
			return new SignTextModel(session, proxy);
		else
			return null;
	}

	/** Set a new sign text model */
	protected void setSignTextModel(SignTextModel stm) {
		SignTextModel om = st_model;
		if(stm != null)
			stm.initialize();
		st_model = stm;
		if(om != null)
			om.dispose();
	}

	/** Get the number of lines on the selected sign(s) */
	protected int getLineCount() {
		RasterBuilder b = builder;
		if(b != null)
			return b.getLineCount();
		else
			return max_lines;
	}

	/** Initialize the widgets */
	protected void initializeWidgets() {
		int np = calculateSignPages();
		initializeEtcWidgets(np);
		initializeWidgets(getLineCount(), np);
		dur_cbx.setSelectedIndex(0);
		if(!SystemAttrEnum.DMS_DURATION_ENABLE.getBoolean()) {
			dur_lbl.setVisible(false);
			dur_cbx.setVisible(false);
		}
		if(!PgTimeSpinner.getIEnabled()) {
			pg_on_lbl.setVisible(false);
			pg_on_spn.setVisible(false);
		}
		if(!query_msg.getIEnabled())
			query_btn.setVisible(false);
		clear_btn.setMargin(UI.buttonInsets());
		blank_btn.setMargin(UI.buttonInsets());
		query_btn.setMargin(UI.buttonInsets());
	}

	/** Calculate the number of pages for the selected sign */
	protected int calculateSignPages() {
		int ml = getLibraryLines();
		int nl = getLineCount();
		int np = calculateSignPages(ml, nl);
		return Math.min(DMS_MESSAGE_MAX_PAGES, Math.max(np, min_pages));
	}

	/** Get the number of lines in the message library */
	protected int getLibraryLines() {
		SignTextModel stm = st_model;
		if(stm != null)
			return stm.getMaxLine();
		else
			return 0;
	}

	/** Calculate the number of pages for the sign.
	 * @param ml Number of lines in message library.
	 * @param nl Number of lines on sign face. */
	protected int calculateSignPages(int ml, int nl) {
		if(nl > 0)
			return 1 + Math.max(0, (ml - 1) / nl);
		else
			return 1;
	}

	/** Initialize the other widgets for all pages.
	 * @param np Number of pages. */
	protected void initializeEtcWidgets(int np) {
		disposeEtcWidgets();
		FontComboBox[] fc = new FontComboBox[np];
		for(int i = 0; i < np; i++)
			fc[i] = new FontComboBox(fonts, builder, this);
		fontCmb = fc;
	}

	/** Initialize the page tabs and message combo boxes */
	protected void initializeWidgets(int nl, int np) {
		disposeLines();
		boolean cam = canAddMessages();
		MsgComboBox[] cl = new MsgComboBox[nl * np];
		for(int i = 0; i < cl.length; i++) {
			cl[i] = new MsgComboBox(this, cam);
			cl[i].initialize();
		}
		cmbLine = cl;
		for(int i = 0; i < np; i++)
			setPage(i, createPage(i, nl));
		while(np < page_tab.getTabCount())
			page_tab.removeTabAt(np);
	}

	/** Check if the user can add messages */
	protected boolean canAddMessages() {
		SignTextModel stm = st_model;
		if(stm != null)
			return stm.canAddLocalSignText();
		else
			return false;
	}

	/** Create a new page panel */
	protected JPanel createPage(int p, int nl) {
		JPanel page = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(nl, 1, UI.hgap / 2,
			UI.vgap / 2));
		panel.setBackground(Color.BLACK);
		panel.setBorder(UI.border);
		for(int i = 0; i < nl; i++)
			panel.add(cmbLine[i + p * nl]);
		page.add(panel, BorderLayout.CENTER);
		if(FontComboBox.getIEnabled())
			page.add(createFontBox(p), BorderLayout.PAGE_END);
		return page;
	}

	/** Create a font box */
	protected Box createFontBox(int p) {
		final FontComboBox[] fc = fontCmb;	// Avoid races
		Box box = Box.createHorizontalBox();
		if(p < fc.length) {
			ILabel label = new ILabel("font");
			label.setLabelFor(fc[p]);
			box.add(label);
			box.add(Box.createHorizontalStrut(4));
			box.add(fc[p]);
		}
		return box;
	}

	/** Get the font number for a given page */
	protected int getFontNumber(int p) {
		final FontComboBox[] fc = fontCmb;	// Avoid races
		if(p < fc.length) {
			Integer f = fc[p].getFontNumber();
			if(f != null)
				return f;
		}
		return default_font;
	}

	/** Set a page on one tab */
	protected void setPage(int n, JPanel page) {
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
		if(!b) {
			setSign(null, null);
			setMultiple(false);
		}
		setTabPage(0);
		clear.setEnabled(b);
		adjusting++;
		pg_on_spn.setEnabled(b);
		for(MsgComboBox cbox: cmbLine)
			cbox.setEnabled(b);
		for(FontComboBox f: fontCmb)
			f.setEnabled(b);
		dur_cbx.setEnabled(b);
		dur_cbx.setSelectedItem(0);
		alert_chx.setEnabled(b);
		quick_cbx.setEnabled(b);
		boolean vis = quick_cbx.getItemCount() > 0;
		quick_lbl.setVisible(vis);
		quick_cbx.setVisible(vis);
		send_msg.setEnabled(b);
		blank_msg.setEnabled(b);
		query_msg.setEnabled(b && dispatcher.canRequest());
		adjusting--;
	}

	/** Return a MULTI string using the contents of the widgets */
	public String getMessage() {
		final MsgComboBox[] cl = cmbLine;	// Avoid races
		String[] mess = new String[cl.length];
		int m = 0;
		for(int i = 0; i < cl.length; i++) {
			mess[i] = cl[i].getMessage();
			if(mess[i].length() > 0)
				m = i + 1;
		}
		if(m > 0)
			return buildMulti(mess, m).toString();
		else
			return "";
	}

	/** Build a MULTI string from an array of line strings */
	protected MultiString buildMulti(String[] mess, int m) {
		int nl = getLineCount();
		MultiString multi = new MultiString();
		int p = 0;
		int f = getFontNumber(0);
		if(f != default_font)
			multi.setFont(f, null);
		if(PgTimeSpinner.getIEnabled()) {
			int pt = pg_on_spn.getValuePgTime().toTenths();
			if(pt > 0)
				multi.setPageTimes(pt, null);
		}
		for(int i = 0; i < m; i++) {
			if(i > 0) {
				if(i % nl == 0) {
					multi.addPage();
					p++;
					int fn = getFontNumber(p);
					if(fn != f) {
						multi.setFont(fn, null);
						f = fn;
					}
				} else
					multi.addLine(null);
			}
			multi.addSpan(mess[i]);
		}
		return multi;
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
		setFontComboBoxes(multi);
		String[] lines = multi.getLines();
		final MsgComboBox[] cl = cmbLine;	// Avoid races
		for(int i = 0; i < cl.length; i++) {
			MsgComboBox cbox = cl[i];
			if(i < lines.length)
				setLineSelection(i, lines[i]);
			else if(cbox.getItemCount() > 0)
				cbox.setSelectedIndex(0);
		}
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
	protected void setFontComboBoxes(MultiString ms) {
		final FontComboBox[] fc = fontCmb;	// Avoid races
		int[] fnum = ms.getFonts(default_font);
		for(int i = 0; i < fc.length; i++) {
			if(i < fnum.length)
				fc[i].setSelectedFontNumber(fnum[i]);
			else
				fc[i].setSelectedFontNumber(default_font);
		}
	}

	/** Set the selected message for a message line combo box */
	protected void setLineSelection(int i, String m) {
		SignTextModel stm = st_model;
		if(stm != null) {
			SignTextComboBoxModel model = stm.getLineModel(
				(short)(i + 1));
			model.setSelectedItem(m);
		}
	}

	/** Clear the font comboboxes */
	public void clearFonts() {
		for(FontComboBox f: fontCmb)
			f.setSelectedFontNumber(default_font);
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		SignTextModel stm = st_model;
		if(stm != null)
			stm.updateMessageLibrary();
	}
}
