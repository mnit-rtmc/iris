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

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttributeHelper;

/**
 * Gui for composing messages for DMS.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class SignMessageComposer extends JPanel {

	/** Cell renderer for sign text in combo boxes */
	protected final SignTextCellRenderer renderer =
		new SignTextCellRenderer();

	/** DMS dispatcher */
	protected final DMSDispatcher dispatcher;

	/** DMS sign group type cache */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache */
	protected final TypeCache<SignText> sign_text;

	/** Tab pane to hold pages */
	protected final JTabbedPane tab = new JTabbedPane(JTabbedPane.RIGHT);

	/** SONAR user */
	protected final User user;

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

	/** Preview mode */
	protected boolean preview = false;

	/** Counter to indicate we're adjusting combo boxes */
	protected int adjusting = 0;

	/** Listener for combo box events */
	protected final ActionListener comboListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			if(adjusting == 0)
				selectPreview(true);
		}
	};

	/** Create a new sign message composer */
	public SignMessageComposer(DMSDispatcher ds, TypeCache<DmsSignGroup> d, 
		TypeCache<SignText> t, User u)
	{
		dispatcher = ds;
		dms_sign_groups = d;
		sign_text = t;
		user = u;
		add(tab);
		initializeWidgets(SystemAttributeHelper.getDmsMaxLines(), 1);
	}

	/** Dispose of the message selector */
	public void dispose() {
		removeAll();
		SignTextModel stm = st_model;
		if(stm != null) {
			stm.dispose();
			st_model = null;
		}
	}

	/** Set the preview mode */
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
	public void setSign(DMS proxy, int nl) {
		dms = proxy;
		SignTextModel stm = createSignTextModel(proxy);
		int ml = stm.getMaxLine();
		int np = Math.max(calculateSignPages(ml, nl),
			SystemAttributeHelper.getDmsMessageMinPages());
		initializeWidgets(nl, np);
		for(short i = 0; i < cmbLine.length; i++) {
			cmbLine[i].setModel(stm.getLineModel(
				(short)(i + 1)));
			cmbLine[i].setEnabled(true);
		}
	}

	/** Create a new sign text model */
	protected SignTextModel createSignTextModel(DMS proxy) {
		SignTextModel stm = new SignTextModel(proxy, dms_sign_groups,
			sign_text, user);
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

	/** Initialize the page tabs and message combo boxes */
	protected void initializeWidgets(int n, int p) {
		if(n == n_lines && p == n_pages)
			return;
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i].removeActionListener(comboListener);
		n_lines = n;
		n_pages = p;
		boolean can_add = st_model != null &&
			st_model.canAddSignText("arbitrary_name");
		cmbLine = new JComboBox[n_lines * n_pages];
		for(int i = 0; i < cmbLine.length; i++) {
			cmbLine[i] = new JComboBox();
			if(can_add)
				createEditor(cmbLine[i]);
			cmbLine[i].setMaximumRowCount(21);
			cmbLine[i].setRenderer(renderer);
			cmbLine[i].addActionListener(comboListener);
		}
		for(p = 0; p < n_pages; p++) {
			JPanel page = createPage(p);
			setTab(p, "p." + (p + 1), page);
		}
		while(n_pages < tab.getTabCount())
			tab.removeTabAt(n_pages);
	}

	/** Create a new page panel */
	protected JPanel createPage(int p) {
		JPanel panel = new JPanel(new GridLayout(n_lines, 1, 6, 6));
		panel.setBackground(Color.BLACK);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		for(int i = 0; i < n_lines; i++)
			panel.add(cmbLine[i + p * n_lines]);
		return panel;
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
			public void focusLost(FocusEvent e) {
				cbox.setEditable(false);
			}
		});
	}

	/** Enable or Disable the message selector */
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i].setEnabled(b);
	}

	/** Get the text of the message to send to the sign */
	public String getMessage(Integer font) {
		String[] mess = new String[cmbLine.length];
		int m = 0;
		for(int i = 0; i < cmbLine.length; i++) {
			mess[i] = getMessageFromCB(i);
			if(mess[i].length() > 0)
				m = i + 1;
		}
		if(m > 0)
			return buildMulti(font, mess, m).toString();
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
	protected MultiString buildMulti(Integer font, String[] mess, int m) {
		MultiString multi = new MultiString();
		if(font != null)
			multi.setFont(font);
		for(int i = 0; i < m; i++) {
			if(i > 0) {
				if(i % n_lines == 0)
					multi.addPage();
				else
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
		adjusting++;
		setMessage(proxy.getMessageCurrent());
		adjusting--;
	}

	/** Set the currently selected message */
	protected void setMessage(SignMessage m) {
		String[] lines = SignMessageHelper.createLines(m);
		for(int i = 0; i < cmbLine.length; i++) {
			if(i < lines.length)
				setLineSelection(i, lines[i]);
			else
				cmbLine[i].setSelectedIndex(0);
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
}
