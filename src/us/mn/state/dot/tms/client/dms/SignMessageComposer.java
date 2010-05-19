/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.widget.IButton;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
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

	/** Tab pane to hold pages */
	protected final JTabbedPane pages = new JTabbedPane(JTabbedPane.RIGHT);

	/** Pixel map builder for the selected sign */
	protected PixelMapBuilder builder;

	/** Sign text model for the selected sign */
	protected SignTextModel st_model;

	/** Line combo box widgets */
	protected JComboBox[] cmbLine = new JComboBox[0];

	/** Font combo box widgets */
	protected FontComboBox[] fontCmb = new FontComboBox[0];

	/** Page on-time spinner */
	protected final PgTimeSpinner timeSpin;

	/** Clear button */
	protected IButton clearBtn = new IButton("dms.clear");

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	protected int adjusting = 0;

	/** Listener for combo box events */
	protected final ActionListener comboListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			updateMessage();
		}
	};

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
	public SignMessageComposer(Session s, DMSDispatcher ds) {
		session = s;
		dispatcher = ds;
		DmsCache dc = s.getSonarState().getDmsCache();
		dms_sign_groups = dc.getDmsSignGroups();
		sign_text = dc.getSignText();
		fonts = dc.getFonts();
		timeSpin = new PgTimeSpinner();
		add(createAllWidgets());
		initializeWidgets();
		timeSpin.addChangeListener(spin_listener);
	}

	/** Create all widgets */
	protected Box createAllWidgets() {
		Box box = Box.createVerticalBox();
		box.add(pages);
		box.add(createLowerBox());
		return box;
	}

	/** Create lower box */
	protected Box createLowerBox() {
		Box box = Box.createHorizontalBox();
		box.add(createClearBtn());
		if(PgTimeSpinner.getIEnabled()) {
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
		}
		return box;
	}

	/** Create the clear button */
	protected JPanel createClearBtn() {
		JPanel panel = new JPanel();
		new ActionJob(clearBtn) {
			public void perform() {
				clearWidgets();
			}
		};
		clearBtn.setMargin(new Insets(0, 6, 0, 6));
		clearBtn.setFont(new java.awt.Font(("SansSerif"), 
			java.awt.Font.PLAIN, 12));
		clearBtn.setMaximumSize(clearBtn.getMinimumSize());
		panel.add(clearBtn);
		return panel;
	}

	/** Clear the widgets */
	protected void clearWidgets() {
		clearSelections();
		clearFonts();
		adjusting++;
		dispatcher.setMessage("");
		timeSpin.setValue("");
		adjusting--;
	}

	/** Dispose of the message selector */
	public void dispose() {
		removeAll();
		disposeLines();
		timeSpin.removeChangeListener(spin_listener);
		disposeEtcWidgets();
		setSignTextModel(null);
	}

	/** Dispose of the existing line widgets */
	protected void disposeLines() {
		for(JComboBox cbox: cmbLine) {
			cbox.removeActionListener(comboListener);
			Component c = cbox.getEditor().getEditorComponent();
			for(FocusListener fl: c.getFocusListeners())
				c.removeFocusListener(fl);
		}
		cmbLine = new JComboBox[0];
	}

	/** Dispose of the existing combobox widgets */
	protected void disposeEtcWidgets() {
		for(FontComboBox f: fontCmb)
			f.dispose();
		fontCmb = new FontComboBox[0];
	}

	/** Update the message combo box models */
	public void setSign(DMS proxy, PixelMapBuilder b) {
		builder = b;
		SignTextModel stm = createSignTextModel(proxy);
		initializeWidgets();
		if(stm != null) {
			final JComboBox[] cl = cmbLine;		// Avoid races
			for(short i = 1; i <= cl.length; i++)
				cl[i - 1].setModel(stm.getLineModel(i));
		}
	}

	/** Create a new sign text model */
	protected SignTextModel createSignTextModel(DMS proxy) {
		SignTextModel stm = new SignTextModel(session, proxy);
		stm.initialize();
		setSignTextModel(stm);
		return stm;
	}

	/** Set a new sign text model */
	protected void setSignTextModel(SignTextModel stm) {
		SignTextModel om = st_model;
		st_model = stm;
		if(om != null)
			om.dispose();
	}

	/** Get the number of lines on the selected sign(s) */
	protected int getLineCount() {
		return Math.min(getLineCountSign(),
			SystemAttrEnum.DMS_MAX_LINES.getInt());
	}

	/** Get the number of lines on the selected sign(s) */
	protected int getLineCountSign() {
		int h = getHeightPixels();
		int lh = getLineHeightPixels();
		if(lh > 0 && h >= lh)
			return h / lh;
		else
			return 3;
	}

	/** Get the line height */
	protected int getLineHeightPixels() {
		PixelMapBuilder b = builder;
		if(b != null)
			return b.getLineHeightPixels();
		else
			return 0;
	}

	/** Get the pixel height */
	protected int getHeightPixels() {
		PixelMapBuilder b = builder;
		if(b != null)
			return b.height;
		else
			return 0;
	}

	/** Initialize the widgets */
	protected void initializeWidgets() {
		int np = calculateSignPages();
		initializeEtcWidgets(np);
		initializeWidgets(getLineCount(), np);
	}

	/** Calculate the number of pages for the selected sign */
	protected int calculateSignPages() {
		int ml = getLibraryLines();
		int nl = getLineCount();
		return Math.max(calculateSignPages(ml, nl),
			SystemAttrEnum.DMS_MESSAGE_MIN_PAGES.getInt());
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
		JComboBox[] cl = new JComboBox[nl * np];
		for(int i = 0; i < cl.length; i++)
			cl[i] = createLineCombo();
		cmbLine = cl;
		for(int i = 0; i < np; i++)
			setPage(i, createPage(i, nl));
		while(np < pages.getTabCount())
			pages.removeTabAt(np);
	}

	/** Create a line combo box */
	protected JComboBox createLineCombo() {
		JComboBox cbox = new JComboBox();
		createEditor(cbox);
		cbox.setMaximumRowCount(21);
		// NOTE: We use a prototype display value so that combo boxes
		//       are always the same size.  This prevents all the
		//       widgets from being rearranged whenever a new sign is
		//       selected.
		cbox.setPrototypeDisplayValue(PROTOTYPE_SIGN_TEXT);
		cbox.setRenderer(renderer);
		cbox.addActionListener(comboListener);
		return cbox;
	}

	/** Create a new page panel */
	protected JPanel createPage(int p, int nl) {
		JPanel page = new JPanel(new BorderLayout());
		JPanel panel = new JPanel(new GridLayout(nl, 1, 6, 6));
		panel.setBackground(Color.BLACK);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
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
		final FontComboBox[] fc = fontCmb;	// Avoid races
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
		if(editmode == EditMode.ALWAYS) {
			if(!cbox.isEditable())
				cbox.setEditable(true);
		}
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
			public void focusLost(FocusEvent e) {
				if(editmode == EditMode.AFTERKEY)
					cbox.setEditable(false);
				updateMessage();
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
		final JComboBox[] cl = cmbLine;		// Avoid races
		String[] mess = new String[cl.length];
		int m = 0;
		for(int i = 0; i < cl.length; i++) {
			mess[i] = getMessageFromCB(cl[i]);
			if(mess[i].length() > 0)
				m = i + 1;
		}
		if(m > 0)
			return buildMulti(mess, m).toString();
		else
			return "";
	}

	/** Get text from combobox line */
	protected String getMessageFromCB(JComboBox cb) {
		Object o = cb.getSelectedItem();
		if(o == null)
			return "";
		if(o instanceof SignText)
			return ((SignText)o).getMessage();
		assert false: "unknown type in getMessageFromCB()";
		return "";
	}

	/** Build a MULTI string from an array of line strings */
	protected MultiString buildMulti(String[] mess, int m) {
		int nl = getLineCount();
		MultiString multi = new MultiString();
		int p = 0;
		Integer f = getFontNumber(0);
		if(f != null)
			multi.setFont(f, null);
		if(PgTimeSpinner.getIEnabled()) {
			int pt = timeSpin.getValuePgTime().toTenths();
			if(pt > 0)
				multi.setPageTimes(pt, null);
		}
		for(int i = 0; i < m; i++) {
			if(i > 0) {
				if(i % nl == 0) {
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
	public void setMessage(String ms) {
		adjusting++;
		timeSpin.setValue(ms);
		// Note: order here is crucial. The font cbox must be updated
		// first because the line combobox updates (each) result in 
		// intermediate preview updates which read the (incorrect) 
		// font from the font combobox.
		MultiString multi = new MultiString(ms);
		setFontComboBoxes(multi);
		int nl = getLineCount();
		String[] lines = multi.getText(nl);
		final JComboBox[] cl = cmbLine;		// Avoid races
		for(int i = 0; i < cl.length; i++) {
			JComboBox cbox = cl[i];
			if(i < lines.length)
				setLineSelection(i, lines[i]);
			else if(cbox.getItemCount() > 0)
				cbox.setSelectedIndex(0);
		}
		adjusting--;
	}

	/** Set all font comboboxes using the specified MultiString */
	protected void setFontComboBoxes(MultiString ms) {
		final FontComboBox[] fc = fontCmb;	// Avoid races
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
		PixelMapBuilder b = builder;	// Avoid race
		if(b != null)
			return b.getDefaultFontNumber();
		else
			return FontHelper.DEFAULT_FONT_NUM;
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

	/** Clear the combobox selections */
	public void clearSelections() {
		adjusting++;
		for(JComboBox cbox: cmbLine)
			cbox.setSelectedIndex(-1);
		adjusting--;
	}

	/** Clear the font comboboxes */
	public void clearFonts() {
		final int dfnum = getDefaultFontNumber();
		for(FontComboBox f: fontCmb)
			f.setSelectedFontNumber(dfnum);
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		SignTextModel stm = st_model;
		if(stm != null)
			stm.updateMessageLibrary();
	}
}
