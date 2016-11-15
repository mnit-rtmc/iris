/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
 * Copyright (C) 2009-2014  AHMCT, University of California
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
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.SignMessageHelper.DMS_MESSAGE_MAX_PAGES;
import us.mn.state.dot.tms.client.Session;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A sign message composer is GUI for composing DMS messages.  It uses a number
 * of optional controls which appear or do not appear on screen as a function
 * of system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
public class SignMessageComposer extends JPanel {

	/** User session */
	private final Session session;

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

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
	private final ComposerPagePanel[] pages;

	/** Composer miscellaneous panel */
	private final ComposerMiscPanel misc_pnl;

	/** Composer button panel */
	private final ComposerButtonPanel button_pnl;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	protected int adjusting = 0;

	/** Update the DMS dispatcher message */
	public void updateMessage() {
		if (adjusting == 0) {
			adjusting++;
			dispatcher.setMessage(composeMessage());
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
		fonts = s.getSonarState().getDmsCache().getFonts();
		max_lines = SystemAttrEnum.DMS_MAX_LINES.getInt();
		n_lines = max_lines;
		min_pages = SystemAttrEnum.DMS_MESSAGE_MIN_PAGES.getInt();
		n_pages = min_pages;
		pages = new ComposerPagePanel[DMS_MESSAGE_MAX_PAGES];
		for (int i = 0; i < pages.length; i++)
			pages[i] = new ComposerPagePanel(this, max_lines, i);
		misc_pnl = new ComposerMiscPanel(ds, this);
		button_pnl = new ComposerButtonPanel(manager, ds, this);
		layoutPanel();
		initializeWidgets();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		setLayout(gl);
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup vg = gl.createParallelGroup();
		hg.addComponent(page_tab);
		GroupLayout.ParallelGroup hg1 = gl.createParallelGroup();
		hg1.addComponent(misc_pnl);
		hg1.addComponent(button_pnl);
		hg.addGroup(hg1);
		GroupLayout.SequentialGroup vg0 = gl.createSequentialGroup();
		vg0.addComponent(page_tab);
		vg0.addGap(UI.vgap);
		vg.addGroup(vg0);
		GroupLayout.SequentialGroup vg1 = gl.createSequentialGroup();
		vg1.addComponent(misc_pnl);
		vg1.addComponent(button_pnl);
		vg.addGroup(vg1);
		gl.setHorizontalGroup(hg);
		gl.setVerticalGroup(vg);
		misc_pnl.setBorder(UI.panelBorder());
		button_pnl.setBorder(UI.panelBorder());
	}

	/** Set multiple sign selection mode */
	public void setMultiple(boolean m) {
		misc_pnl.setMultiple(m);
	}

	/** Clear the widgets */
	public void clearWidgets() {
		adjusting++;
		setTabPage(0);
		for (ComposerPagePanel pg: pages)
			pg.clearWidgets();
		dispatcher.setMessage("");
		misc_pnl.setMessage("");
		adjusting--;
	}

	/** Set tab to page specified */
	private void setTabPage(int p) {
		if (page_tab.getTabCount() > 0)
			page_tab.setSelectedIndex(p);
	}

	/** Dispose of the message selector */
	public void dispose() {
		removeAll();
		for (ComposerPagePanel pg: pages)
			pg.dispose();
		misc_pnl.dispose();
		button_pnl.dispose();
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
		for (ComposerPagePanel pg: pages) {
			pg.setModels(stm);
			pg.setBuilder(b);
		}
		misc_pnl.setSign(proxy);
	}

	/** Calculate the number of pages for the selected sign */
	private int calculateSignPages(SignTextModel stm) {
		int ml = stm != null ? stm.getLastLine() : max_lines;
		int np = calculateSignPages(ml, n_lines);
		return Math.min(DMS_MESSAGE_MAX_PAGES, Math.max(np, min_pages));
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
		if (proxy != null)
			return new SignTextModel(session, proxy);
		else
			return null;
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
		boolean cam = canAddMessages();
		for (int i = 0; i < n_pages; i++) {
			ComposerPagePanel pg = pages[i];
			pg.setEditMode(cam);
			pg.setLines(n_lines);
			setPage(i, pg);
		}
		while (n_pages < page_tab.getTabCount())
			page_tab.removeTabAt(n_pages);
	}

	/** Check if the user can add messages */
	private boolean canAddMessages() {
		SignTextModel stm = st_model;
		if (stm != null)
			return stm.isLocalSignTextAddPermitted();
		else
			return false;
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

	/** Enable or Disable the message selector */
	@Override
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		setTabPage(0);
		adjusting++;
		for (ComposerPagePanel pnl: pages)
			pnl.setEnabled(b);
		misc_pnl.setEnabled(b);
		button_pnl.setEnabled(b);
		adjusting--;
	}

	/** Compose a MULTI string using the contents of the widgets */
	private String composeMessage() {
		String prefix = getPagePrefix();
		MultiString[] mess = new MultiString[n_pages];
		int fn = default_font;
		int p = 0;
		for (int i = 0; i < n_pages; i++) {
			mess[i] = pages[i].getMulti(fn, prefix);
			if (!mess[i].isBlank())
				p = i + 1;
			fn = pages[i].getFontNumber();
		}
		return combinePages(mess, p);
	}

	/** Get page prefix MULTI string from scheduled message (if any) */
	private String getPagePrefix() {
		DMS dms = dispatcher.getSingleSelection();
		if (dms != null) {
			SignMessage sm = dms.getMsgSched();
			if (sm != null && sm.getActivationPriority() ==
			    DMSMessagePriority.PREFIX_PAGE.ordinal())
				return sm.getMulti();
		}
		return "";
	}

	/** Build a MULTI string from an array of page strings.
	 * @param mess Array of page MULTI strings.
	 * @param p Number of non-blank pages.
	 * @return Combined MULTI string for all pages. */
	private String combinePages(MultiString[] mess, int p) {
		MultiBuilder mb = new MultiBuilder();
		for (int i = 0; i < p; i++) {
			if (i == 0) {
				if (p > 1) {
					Integer pt = misc_pnl.getPageOnTime();
					if (pt != null)
						mb.setPageTimes(pt, null);
				}
			} else
				mb.addPage();
			mb.append(mess[i]);
		}
		return mb.toString();
	}

	/** Set the currently selected message */
	public void setMessage(String ms) {
		adjusting++;
		misc_pnl.setMessage(ms);
		// Note: order here is crucial. The font cbox must be updated
		// first because the line combobox updates (each) result in 
		// intermediate preview updates which read the (incorrect) 
		// font from the font combobox.
		String prefix = getPagePrefix();
		MultiString multi = new MultiString(ms);
		setSelectedFonts(multi);
		String[] lines = multi.getLines(n_lines, prefix);
		for (int i = 0; i < pages.length; i++)
			pages[i].setSelected(lines);
		adjusting--;
	}

	/** Check if beacon is enabled */
	public boolean isBeaconEnabled() {
		// FIXME: add component for this
		return false;
	}

	/** Get the selected duration */
	public Integer getDuration() {
		return misc_pnl.getDuration();
	}

	/** Get the selected priority */
	public DMSMessagePriority getPriority() {
		return misc_pnl.getPriority();
	}

	/** Get the font cache */
	public TypeCache<Font> getFonts() {
		return fonts;
	}

	/** Get the default font number */
	public int getDefaultFont() {
		return default_font;
	}

	/** Set all font comboboxes using the specified MultiString */
	private void setSelectedFonts(MultiString ms) {
		int[] fnum = ms.getFonts(default_font);
		for (int i = 0; i < pages.length; i++) {
			ComposerPagePanel pnl = pages[i];
			int fn = (i < fnum.length) ? fnum[i] : default_font;
			pnl.setFontNumber(fn);
		}
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		SignTextModel stm = st_model;
		if (stm != null)
			stm.updateMessageLibrary();
	}

	/** Store the composed message as a quick-message */
	public void storeAsQuickMessage() {
		String multi = composeMessage();
		session.getDesktop().show(new StoreQuickMessageForm(session,
			multi));
	}
}
