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

	/** DMS sign group type cache */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache */
	protected final TypeCache<SignText> sign_text;

	/** Tab pane to hold pages */
	protected final JTabbedPane tab = new JTabbedPane();

	/** Sign text model */
	protected SignTextModel st_model;

	/** Number of pages on the currently selected sign */
	protected int n_pages;

	/** Line combo box widgets */
	protected JComboBox cmbLine[];

	/** Number of lines on the currently selected sign */
	protected int n_lines;

	/** SONAR user */
	protected final User user;

	/** Create a new sign message composer */
	public SignMessageComposer(TypeCache<DmsSignGroup> d, 
		TypeCache<SignText> t, User u)
	{
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

	/** Set a page on one tab */
	protected void setTab(int n, String title, JPanel page) {
		if(n < tab.getTabCount()) {
			tab.setComponentAt(n, page);
			tab.setTitleAt(n, title);
		} else
			tab.addTab(title, page);
	}

	/** Initialize the page tabs and message combo boxes */
	protected void initializeWidgets(int n, int p) {
		if(n == n_lines && p == n_pages)
			return;
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
		}
		for(p = 0; p < n_pages; p++) {
			JPanel page = createPage(p);
			if(n_pages > 1)
				setTab(p, "Page " + (p + 1), page);
			else
				setTab(p, "Compose Message", page);
		}
		for(p = n_pages; p < tab.getTabCount(); p++)
			tab.removeTabAt(p);
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

	/** Create a new page panel */
	protected JPanel createPage(int p) {
		JPanel panel = new JPanel(new GridLayout(n_lines, 1, 6, 6));
		panel.setBackground(Color.BLACK);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		for(int i = 0; i < n_lines; i++)
			panel.add(cmbLine[i + p * n_lines]);
		return panel;
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
	public void setMessage(DMS proxy) {
		SignMessage m = proxy.getMessageCurrent();
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
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i].setSelectedIndex(-1);
	}

	/** Enable or Disable the message selector */
	public void setEnabled(boolean b) {
		super.setEnabled(b);
		for(int i = 0; i < cmbLine.length; i++)
			cmbLine[i].setEnabled(b);
	}

	/** Update the message combo box models */
	public void setSign(DMS proxy, int lineHeight) {
		createMessageModel(proxy);
		int ml = st_model.getMaxLine();
		int nl = getLineCount(proxy, lineHeight);
		int np = Math.max(calculateSignPages(ml, nl),
			SystemAttributeHelper.getDmsMessageMinPages());
		initializeWidgets(nl, np);
		for(short i = 0; i < cmbLine.length; i++) {
			cmbLine[i].setModel(st_model.getLineModel(
				(short)(i + 1)));
			cmbLine[i].setEnabled(true);
		}
	}

	/** Get the number of lines on the sign */
	protected int getLineCount(DMS proxy, int lineHeight) {
		int ml = SystemAttributeHelper.getDmsMaxLines();
		Integer h = proxy.getHeightPixels();
		if(h != null && && h > 0 && lineHeight >= h) {
			int nl = h / lineHeight;
			return Math.min(nl, ml);
		} else
			return ml;
	}

	/** Create a new message model */
	protected void createMessageModel(DMS proxy) {
		SignTextModel stm = new SignTextModel(proxy, dms_sign_groups,
			sign_text, user);
		stm.initialize();
		SignTextModel om = st_model;
		st_model = stm;
		if(om != null)
			om.dispose();
	}

	/** Calculate the number of pages for the sign */
	protected int calculateSignPages(int ml, int nl) {
		if(nl > 0)
			return 1 + Math.max(0, (ml - 1) / nl);
		else
			return 1;
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		if(st_model != null)
			st_model.updateMessageLibrary();
	}
}
