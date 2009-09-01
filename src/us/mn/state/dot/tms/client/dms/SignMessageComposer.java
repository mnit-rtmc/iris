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
import java.awt.Insets;
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
import javax.swing.SwingConstants;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.widget.IButton;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
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
 * GUI for composing DMS messages.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignMessageComposer extends JPanel {

	/** Combobox edit mode */
	public enum EditMode {
		NOT(0), ALWAYS(1), AFTERKEY(2);

		/** The id must correspond with the system attribute value */
		private final int m_id;

		/** constructor */
	 	EditMode(int id) {
			m_id = id;
		}

		/** Convert an int to enum */
		public static EditMode fromId(final int id) {
			for(EditMode e : EditMode.values())
				if (e.m_id == id)
					return e;
			return EditMode.NOT;
		}

		/** Get the edit mode */
		public static EditMode getEditMode(SignTextModel stm) {
			EditMode m = EditMode.fromId(SystemAttrEnum.
				DMS_COMPOSER_EDIT_MODE.getInt());
			return rules(m, stm);
		}

		/** Apply rules */
		private static EditMode rules(EditMode m, SignTextModel stm) {
			// AFTERKEY is only active if a sign group with the
			// same name as the DMS (e.g. 'V2') exists.
			if(m == EditMode.AFTERKEY)
				if(stm == null || !stm.canAddLocalSignText())
					m = EditMode.NOT;
			return m;
		}
	}

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
	protected final JTabbedPane pages = new JTabbedPane(JTabbedPane.RIGHT);

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

	/** Clear button */
	protected IButton clearBtn = new IButton("dms.clear");

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
	protected Box createAllWidgets() {
		Box box = Box.createVerticalBox();
		box.add(pages);
		if(PgTimeSpinner.getIEnabled())
			box.add(createOnTimeBox());
		box.add(createClearBtn());
		return box;
	}

	/** Create page on-time box */
	protected Box createOnTimeBox() {
		timeSpin = new PgTimeSpinner(this);
		Box box = Box.createHorizontalBox();
		JLabel label = new JLabel();
		label.setLabelFor(timeSpin);
		label.setHorizontalAlignment(SwingConstants.RIGHT);
		label.setDisplayedMnemonic('P');
		// FIXME: move to a new ISpinner class.
		label.setText(I18N.get("PgOnTimeSpinner"));
		label.setMaximumSize(label.getMinimumSize());
		box.add(Box.createHorizontalGlue());
		box.add(label);
		box.add(Box.createHorizontalStrut(4));
		box.add(timeSpin);
		box.add(Box.createHorizontalGlue());
		return box;
	}

	/** Create the clear button */
	protected JPanel createClearBtn() {
		JPanel panel = new JPanel();
		new ActionJob(clearBtn) {
			public void perform() {
				clearSelections();
				dispatcher.qlibCmb.setSelectedIndex(-1);
				selectPreview(true);
			}
		};
		clearBtn.setMargin(new Insets(0, 6, 0, 6));
		clearBtn.setMaximumSize(clearBtn.getMinimumSize());
		panel.add(clearBtn);
		return panel;
	}

	/** Dispose of the message selector */
	public void dispose() {
		removeAll();
		disposeLines();
		timeSpin.dispose();
		disposeEtcWidgets();
		SignTextModel stm = st_model;
		if(stm != null) {
			stm.dispose();
			st_model = null;
		}
	}

	/** Dispose of the existing line widgets */
	protected void disposeLines() {
		for(JComboBox cbox: cmbLine) {
			cbox.removeActionListener(comboListener);
			Component c = cbox.getEditor().getEditorComponent();
			for(FocusListener fl: c.getFocusListeners())
				c.removeFocusListener(fl);
		}
	}

	/** Dispose of the existing combobox widgets */
	protected void disposeEtcWidgets() {
		for(FontComboBox f: fontCmb)
			f.dispose();
		fontCmb = new FontComboBox[0];
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
		FontComboBox[] fc = new FontComboBox[np];
		for(int i = 0; i < np; i++)
			fc[i] = new FontComboBox(fonts, builder, this);
		fontCmb = fc;
	}

	/** Initialize the page tabs and message combo boxes */
	protected void initializeWidgets(int nl, int np) {
		disposeLines();
		n_lines = nl;
		n_pages = np;
		cmbLine = new JComboBox[n_lines * n_pages];
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i] = createLineCombo();
		for(int i = 0; i < n_pages; i++)
			setPage(i, createPage(i));
		while(n_pages < pages.getTabCount())
			pages.removeTabAt(n_pages);
	}

	/** Create a line combo box */
	protected JComboBox createLineCombo() {
		JComboBox cmb = new JComboBox();
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
		final FontComboBox[] fc = fontCmb;
		Box box = Box.createHorizontalBox();
		if(p < fc.length) {
			JLabel label = new JLabel();
			label.setLabelFor(fc[p]);
			box.add(new JLabel("Font"));
			box.add(Box.createHorizontalStrut(4));
			box.add(fc[p]);
		}
		return box;
	}

	/** Get the font number for a given page */
	protected Integer getFontNumber(int p) {
		final FontComboBox[] fc = fontCmb;
		if(p < fc.length)
			return fc[p].getFontNumber();
		else
			return null;
	}

	/** Set a page on one tab */
	protected void setPage(int n, JPanel page) {
		String title = "p." + (n + 1);
		if(n < pages.getTabCount()) {
			pages.setComponentAt(n, page);
			pages.setTitleAt(n, title);
		} else
			pages.addTab(title, page);
	}

	/** Key event saved when making combobox editable */
	protected KeyEvent first_key_event;

	/** Create an editor for a combo box */
	protected void createEditor(final JComboBox cbox) {
		final EditMode editmode = EditMode.getEditMode(st_model);
		if(editmode == EditMode.NOT)
			return;
		final MsgComboBoxEditor cbe = new MsgComboBoxEditor();
		final java.awt.Component editor = cbe.getEditorComponent();
		cbox.setEditor(cbe);
		if(editmode == EditMode.ALWAYS)
			if(!cbox.isEditable())
				cbox.setEditable(true);
		cbox.addKeyListener(new KeyAdapter() {
			public void keyTyped(KeyEvent e) {
				if(!cbox.isEditable()) {
					if(editmode == EditMode.AFTERKEY)
						cbox.setEditable(true);
					first_key_event = e;
				}
			}
		});
		cbe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(editmode == EditMode.AFTERKEY)
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
				if(editmode == EditMode.AFTERKEY)
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
		if(pages.getTabCount() > 0)
			pages.setSelectedIndex(0);
		clearBtn.setEnabled(b);
		timeSpin.setEnabled(b);
		for(JComboBox cbox: cmbLine)
			cbox.setEnabled(b);
		for(FontComboBox f: fontCmb)
			f.setEnabled(b);
	}

	/** Return a MULTI string using the contents of the widgets */
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
		assert cmbLine[line] != null;
		if(line < 0 || line >= cmbLine.length)
			return "";
		if(cmbLine[line] == null)
			return "";
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
		Integer f = getFontNumber(0);
		if(f != null)
			multi.setFont(f, null);
		if(PgTimeSpinner.getIEnabled()) {
			multi.setPageTimes(timeSpin.
				getValuePgTime().toTenths(), null);
		}
		for(int i = 0; i < m; i++) {
			if(i > 0) {
				if(i % n_lines == 0) {
					multi.addPage();
					p++;
					f = getFontNumber(p);
					if(f != null)
						multi.setFont(f, null);
				} else
					multi.addLine();
			}
			multi.addSpan(mess[i]);
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
		// Note: order here is crucial. The font cbox must be updated
		// first because the line combobox updates (each) result in 
		// intermediate preview updates which read the (incorrect) 
		// font from the font combobox.
		if(m != null) {
			timeSpin.setValueNoAction(m.getMulti());
			setFontComboBoxes(new MultiString(m.getMulti()));
		}
		String[] lines = SignMessageHelper.createLines(m, n_lines);
		for(int i = 0; i < cmbLine.length; i++) {
			if(i < lines.length)
				setLineSelection(i, lines[i]);
			else if(cmbLine[i].getItemCount() > 0)
				cmbLine[i].setSelectedIndex(0);
		}
	}

	/** set all font comboboxes using the specified MultiString */
	protected void setFontComboBoxes(MultiString ms) {
		final FontComboBox[] fc = fontCmb;
		final int dfnum = getDefaultFontNumber();
		int[] fnum = ms.getFonts(dfnum);
		for(int i = 0; i < fc.length; i++) {
			if(i < fnum.length)
				fc[i].setSelectedFontNumber(fnum[i]);
			else
				fc[i].setSelectedFontNumber(dfnum);
		}
	}

	/** Get default font number for the selected DMS */
	protected int getDefaultFontNumber() {
		DMS proxy = dms;	// Avoid races
		if(proxy != null)
			return DMSHelper.getDefaultFontNumber(proxy);
		else
			return 1;
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
		for(JComboBox cbox: cmbLine)
			cbox.setSelectedIndex(-1);
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
