/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.widget.IButton;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Gui for composing messages for DMS.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class SignMessageComposer extends JPanel {

	/** Prototype sign text */
	static protected final SignText PROTOTYPE_SIGN_TEXT =
		new ClientSignText("12345678901234567890");

	/** Cell renderer for sign text in combo boxes */
	protected final SignTextCellRenderer renderer =
		new SignTextCellRenderer();

	/** DMS dispatcher */
	protected final DMSDispatcher dispatcher;

	/** Soanr state */
	protected final SonarState state;

	/** DMS sign group type cache */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache */
	protected final TypeCache<SignText> sign_text;

	/** Cache of font proxy objects */
	protected final TypeCache<Font> fonts;

	/** SONAR user */
	protected final User user;

	/** Tab pane to hold pages */
	protected final JTabbedPane tab = new JTabbedPane(JTabbedPane.RIGHT);

	/** Currently selected DMS */
	protected DMS dms;

	/** Sign text model */
	protected SignTextModel st_model;

	/** Number of pages on the currently selected sign */
	protected int n_pages;

	/** Line combo box widgets */
	protected JComboBox[] cmbLine = new JComboBox[0];

	/** Number of lines on the currently selected sign */
	protected int n_lines;

	/** Font combo box widgets */
	protected FontComboBox[] fontCmb = new FontComboBox[0];

	/** page on-time spinner */
	protected PgTimeSpinner timeSpin = new PgTimeSpinner(null);

	/** blank button */
	protected IButton blankBtn = new IButton("dms.blank");

	/** Preview mode */
	protected boolean preview = false;

	/** Counter to indicate we're adjusting combo boxes */
	protected int adjusting = 0;

	/** Listener for combo box events */
	protected final ActionListener comboListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			handleActionPerformed(e);
		}
	};

	/** handle action event */
	protected void handleActionPerformed(ActionEvent e) {
		if(adjusting != 0)
			return;
		selectPreview(true);
		// the user might have changed cbox contents, so
		// reevaluate the currently selected quick message.
		if(!dispatcher.m_updating_widgets)
			dispatcher.updateTextQLibCBox(false);
	}

	/** Create a new sign message composer */
	public SignMessageComposer(DMSDispatcher ds, SonarState st, User u) {
		dispatcher = ds;
		state = st;
		dms_sign_groups = st.getDmsCache().getDmsSignGroups();
		sign_text = st.getDmsCache().getSignText();
		fonts = st.getDmsCache().getFonts();
		user = u;
		add(createAllWidgets());
		initializeEtcWidgets(1, null);
		initializeWidgets(SystemAttrEnum.DMS_MAX_LINES.getInt(), 1);
	}

	/** Create all widgets */
	protected JPanel createAllWidgets() {
		JPanel panel = new JPanel(new BorderLayout());
		if(PgTimeSpinner.getIEnabled())
			panel.add(createOnTimeBox(), BorderLayout.PAGE_START);
		panel.add(addBlankBtn(), BorderLayout.LINE_START);
		panel.add(tab, BorderLayout.CENTER);
		return panel;
	}

	/** Add blank button */
	protected JPanel addBlankBtn() {
		JPanel panel = new JPanel();
		new ActionJob(blankBtn) {
			public void perform() {
				clearSelections();
				dispatcher.qlibCmb.setSelectedIndex(-1);
			}
		};
		blankBtn.setMaximumSize(blankBtn.getMinimumSize());
		panel.add(blankBtn);
		return panel;
	}

	/** Dispose of the message selector */
	public void dispose() {
		removeAll();
		disposeLines();
		disposeEtcWidgets();
		SignTextModel stm = st_model;
		if(stm != null) {
			stm.dispose();
			st_model = null;
		}
	}

	/** Dispose of the existing line widgets */
	protected void disposeLines() {
		for(int i = 0; i < cmbLine.length; i++) {
			cmbLine[i].removeActionListener(comboListener);
			// dispose of focus listeners
			Component c = cmbLine[i].getEditor().
				getEditorComponent();
			FocusListener[] listeners = cmbLine[i].getEditor().
				getEditorComponent().getFocusListeners();
			for(int j = 0; j < listeners.length; ++j)
				c.removeFocusListener(listeners[j]);
		}
	}

	/** Dispose of the existing combobox widgets */
	protected void disposeEtcWidgets() {
		timeSpin.dispose();
		for(int i = 0; i < fontCmb.length; i++)
			if(fontCmb[i] != null)
				fontCmb[i].dispose();
	}

	/** Set the preview mode.
	 *  @param p True to select preview else false. */
	public void selectPreview(boolean p) {
		preview = p;
		if(adjusting == 0) {
			adjusting++;
			dispatcher.selectPreview(p);
			setMessage();
			adjusting--;
		}
	}

	/** Update the message combo box models */
	public void setSign(DMS proxy, int nl, PixelMapBuilder builder) {
		dms = proxy;
		SignTextModel stm = createSignTextModel(proxy);
		int ml = stm.getMaxLine();
		int np = Math.max(calculateSignPages(ml, nl),
			SystemAttrEnum.DMS_MESSAGE_MIN_PAGES.getInt());
		initializeEtcWidgets(np, builder);
		initializeWidgets(nl, np);
		for(short i = 0; i < cmbLine.length; i++) {
			if(cmbLine[i] != null && stm != null)
				cmbLine[i].setModel(stm.getLineModel(
					(short)(i + 1)));
		}
	}

	/** Create a new sign text model */
	protected SignTextModel createSignTextModel(DMS proxy) {
		SignTextModel stm = new SignTextModel(proxy, state, user);
		stm.initialize();
		SignTextModel om = st_model;
		st_model = stm;
		if(om != null)
			om.dispose();
		return stm;
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

	/** Initialize the other widgets for all pages */
	protected void initializeEtcWidgets(int np, PixelMapBuilder builder) {
		disposeEtcWidgets();
		fontCmb = new FontComboBox[np];
		for(int i = 0; i < np; i++)
			fontCmb[i] = new FontComboBox(fonts, builder, this);
	}

	/** Initialize the page tabs and message combo boxes */
	protected void initializeWidgets(int nl, int np) {
		//if(nl == n_lines && np == n_pages)
		//	return;
		disposeLines();
		n_lines = nl;
		n_pages = np;
		boolean can_add = areEditable();
		cmbLine = new JComboBox[n_lines * n_pages];
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i] = createLineCombo(can_add);
		for(int i = 0; i < n_pages; i++)
			setTab(i, "p." + (i + 1), createPage(i));
		while(n_pages < tab.getTabCount())
			tab.removeTabAt(n_pages);
	}

	/** Determine if the message comboboxes are editable. */
	protected boolean areEditable() {
		// if attribute is true, then always editable
		if(SystemAttrEnum.DMS_CBOXES_EDITABLE.getBoolean())
			return true;
		// otherwise, use rules
		return st_model != null && st_model.canAddLocalSignText();
	}

	/** Create a line combo box */
	protected JComboBox createLineCombo(boolean can_add) {
		JComboBox cmb = new JComboBox();
		if(can_add)
			createEditor(cmb);
		cmb.setMaximumRowCount(21);
		// NOTE: We use a prototype display value so that combo boxes
		//       are always the same size.  This prevents all the
		//       widgets from being rearranged whenever a new sign is
		//       selected.
		cmb.setPrototypeDisplayValue(PROTOTYPE_SIGN_TEXT);
		cmb.setRenderer(renderer);
		cmb.addActionListener(comboListener);
		return cmb;
	}

	/** Create a new page panel */
	protected JPanel createPage(int p) {
		JPanel page = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(n_lines, 1, 6, 6));
		panel.setBackground(Color.BLACK);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		for(int i = 0; i < n_lines; i++)
			panel.add(cmbLine[i + p * n_lines]);
		page.add(panel, BorderLayout.CENTER);
		if(FontComboBox.getIEnabled())
			page.add(createFontBox(p), BorderLayout.PAGE_END);
		return page;
	}

	/** Create a font box */
	protected Box createFontBox(int p) {
		if(fontCmb == null || fontCmb[p] == null)
			return Box.createHorizontalBox();
		Box box = Box.createHorizontalBox();
		JLabel label = new JLabel();
		label.setLabelFor(fontCmb[p]);
		//label.setDisplayedMnemonic('F');
		//box.add(new JLabel("<html><u>F</u>ont</html>"));
		box.add(new JLabel("Font"));
		box.add(Box.createHorizontalStrut(4));
		box.add(fontCmb[p]);
		return box;
	}

	/** Create page on-time box */
	protected Box createOnTimeBox() {
		timeSpin = new PgTimeSpinner(this);
		Box box = Box.createHorizontalBox();
		JLabel label = new JLabel();
		label.setLabelFor(timeSpin);
		label.setDisplayedMnemonic('P');
		label.setText(I18N.get("PgOnTimeSpinner")); //FIXME: move to a new ISpinner class.
		box.add(label);
		box.add(timeSpin);
		box.add(Box.createHorizontalStrut(4));
		return box;
	}

	/** Set a page on one tab */
	protected void setTab(int n, String title, JPanel page) {
		if(n < tab.getTabCount()) {
			tab.setComponentAt(n, page);
			tab.setTitleAt(n, title);
		} else
			tab.addTab(title, page);
	}

	/** Key event saved when making combobox editable */
	protected KeyEvent first_key_event;

	/** Create an editor for a combo box */
	protected void createEditor(final JComboBox cbox) {
		final MsgComboBoxEditor cbe = new MsgComboBoxEditor();
		final java.awt.Component editor = cbe.getEditorComponent();
		cbox.setEditor(cbe);
		cbox.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if(!cbox.isEditable()) {
					cbox.setEditable(true);
					first_key_event = e;
				}
			}
		});
		cbe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cbox.setEditable(false);
			}
		});
		editor.addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				if(first_key_event != null) {
					editor.dispatchEvent(first_key_event);
					first_key_event = null;
				}
			}
			// the cbox editor lost focus, which only happens
			// if the cbox is editable.
			public void focusLost(FocusEvent e) {
				cbox.setEditable(false);
				// the user might have changed cbox contents, 
				// so reevaluate the currently selected quick 
				// message.
				dispatcher.updateTextQLibCBox(false);
			}
		});
	}

	/** Enable or Disable the message selector */
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		blankBtn.setEnabled(b);
		timeSpin.setEnabled(b);
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i].setEnabled(b);
		for(int i = 0; i < fontCmb.length; i++)
			if(fontCmb[i] != null)
				fontCmb[i].setEnabled(b);
	}

	/** Return a MULTI string using the contents of the widgets. */
	public String getMessage() {
		String[] mess = new String[cmbLine.length];
		int m = 0;
		for(int i = 0; i < cmbLine.length; i++) {
			mess[i] = getMessageFromCB(i);
			if(mess[i].length() > 0)
				m = i + 1;
		}
		if(m > 0)
			return buildMulti(mess, m).toString();
		else
			return null;
	}

	/** Get text from combobox line */
	protected String getMessageFromCB(int line) {
		assert line >= 0 && line < cmbLine.length;
		Object o = cmbLine[line].getSelectedItem();
		if(o == null)
			return "";
		if(o instanceof SignText)
			return ((SignText)o).getMessage();
		assert false: "unknown type in getMessageFromCB()";
		return "";
	}

	/** Build a MULTI string from an array of line strings */
	protected MultiString buildMulti(String[] mess, int m) {
		MultiString multi = new MultiString();
		int p = 0;
		Integer f = FontHelper.getDefault();
		if(FontComboBox.getIEnabled())
			f = fontCmb[0].getFontNumber();
		multi.setFont(f);
		if(PgTimeSpinner.getIEnabled())
			multi.setPageOnTime(timeSpin.
				getValuePgTime().toTenths());
		for(int i = 0; i < m; i++) {
			if(i > 0) {
				if(i % n_lines == 0) {
					multi.addPage();
					p++;
					f = fontCmb[p].getFontNumber();
					if(f != null)
						multi.setFont(f);
				} else
					multi.addLine();
			}
			multi.addText(mess[i]);
		}
		return multi;
	}

	/** Set the currently selected message */
	public void setMessage() {
		DMS proxy = dms;	// Avoid races
		if(proxy == null || preview)
			return;
		int n_lines = dispatcher.getLineCount(proxy);
		adjusting++;
		setMessage(proxy.getMessageCurrent(), n_lines);
		adjusting--;
	}

	/** Set the currently selected message */
	protected void setMessage(SignMessage m, int n_lines) {
		String[] lines = SignMessageHelper.createLines(m, n_lines);
		for(int i = 0; i < cmbLine.length; i++) {
			if(i < lines.length)
				setLineSelection(i, lines[i]);
			else if(cmbLine[i].getItemCount() > 0)
				cmbLine[i].setSelectedIndex(0);
		}
		if(m != null) {
			timeSpin.setValueNoAction(m.getMulti());
			setFontComboBoxes(new MultiString(m.getMulti()));
		}
	}

	/** set all font comboboxes using the specified MultiString */
	protected void setFontComboBoxes(MultiString ms) {
		// get the font numbers for each page in the multi
		int[] fnum = ms.getFonts(1);
		// set font for each page tab
		for(int i = 0; i < fontCmb.length; ++i) {	
			int newfn = 1; // assumed default font number
			if(fnum != null && i < fnum.length)
				newfn = fnum[i];
			fontCmb[i].setSelectedItemNoAction(newfn);
		}
	}

	/** Set the selected message for a message line combo box */
	protected void setLineSelection(int i, String m) {
		assert st_model != null;
		SignTextComboBoxModel model = st_model.getLineModel(
			(short)(i + 1));
		model.setSelectedItem(m);
	}

	/** Clear the combobox selections */
	public void clearSelections() {
		adjusting++;
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i].setSelectedIndex(-1);
		adjusting--;
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		if(st_model != null)
			st_model.updateMessageLibrary();
	}

	/** If the page on-time spinner is IRIS enabled, return the current 
	 *  value else return the system default page on-time. */
	public DmsPgTime getCurrentPgOnTime() {
		return timeSpin.getValuePgTime();
	}
}
