/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

import us.mn.state.dot.tms.DmsMessage;
import us.mn.state.dot.tms.SignMessage;

/**
 * Gui for selecting messages for DMS.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MessageSelector extends JPanel {

	static protected final Color HIGHLIGHT = new Color(204, 204, 255);

	static protected DmsMessage makeMessage(String m) {
		return new DmsMessage(0, null, (short)0, m, "", (short)50);
	}

	static protected final DmsMessage PROTOTYPE =
		makeMessage("MESSAGES AS WIDE AS THIS");

	static protected final DmsMessage BLANK = makeMessage("");

	protected SignMessage lastMessage = null;

	protected final DmsMessageCellRenderer renderer;

	/** Tab pane to hold pages */
	protected final JTabbedPane tab = new JTabbedPane();

	/** Number of pages on the currently selected sign */
	protected int n_pages;

	/** Line combo box widgets */
	protected JComboBox cmbLine[];

	/** Number of lines on the currently selected sign */
	protected int n_lines;

	/** Pixel width of the currently selected sign */
	protected int width;

	/** Create a new message selector */
	public MessageSelector() {
		renderer = new DmsMessageCellRenderer();
		add(tab);
		initializeWidgets(0, 1);
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
		cmbLine = new JComboBox[n_lines * n_pages];
		for(int i = 0; i < cmbLine.length; i++) {
			cmbLine[i] = new JComboBox();
			cmbLine[i].setPrototypeDisplayValue(PROTOTYPE);
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

	protected JPanel createPage(int p) {
		JPanel panel = new JPanel(new GridLayout(n_lines, 1, 6, 6));
		panel.setBackground(Color.BLACK);
		panel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		for(int i = 0; i < n_lines; i++)
			panel.add(cmbLine[i + p * n_lines]);
		return panel;
	}

	/** Get the text of the message to send to the sign */
	public String getMessage() {
		String[] mess = new String[cmbLine.length];
		int m = 0;
		for(int i = 0; i < cmbLine.length; i++) {
			DmsMessage dm =
				(DmsMessage)cmbLine[i].getSelectedItem();
			if(dm != null && dm != BLANK) {
				if(dm.m_width <= width)
					mess[i] = dm.message;
				else
					mess[i] = dm.abbrev;
				m = i + 1;
			} else
				mess[i] = "";
		}
		if(m == 0)
			return null;
		StringBuffer b = new StringBuffer();
		for(int i = 0; i < m; i++) {
			if(i > 0) {
				if(i % n_lines == 0)
					b.append("[np]");
				else
					b.append("[nl]");
			}
			b.append(mess[i]);
		}
		return b.toString();
	}

	/** Set the currently selected message */
	public void setMessage(DMSProxy proxy) {
		SignMessage message = proxy.getMessage();
		if(message == null) {
			if(lastMessage != null) {
				clearSelections();
				lastMessage = null;
			}
			return;
		} else if(message.equals(lastMessage))
			return;
		lastMessage = message;
		String[] lines = proxy.getLines();
		for(int i = 0; i < cmbLine.length; i++) {
			if(i < lines.length)
				setLineSelection(i, lines[i]);
			else
				cmbLine[i].setSelectedIndex(0);
		}
	}

	/** Set the selected message for a message line combo box */
	protected void setLineSelection(int i, String m) {
		ComboBoxModel model = cmbLine[i].getModel();
		for(int j = 0; j < model.getSize(); j++) {
			DmsMessage dm = (DmsMessage)model.getElementAt(j);
			if(m.equals(dm.message) || m.equals(dm.abbrev)) {
				cmbLine[i].setSelectedItem(dm);
				break;
			}
		}
	}

	/** Clear the combobox selections */
	public void clearSelections() {
		lastMessage = null;
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
	public void updateModel(DMSProxy proxy) {
		lastMessage = null;
		width = proxy.getSignWidthPixels();
		DmsMessage[] mess = proxy.getMessages();
		int nl = proxy.getTextLines();
		int np = calculateSignPages(mess, nl);
		initializeWidgets(nl, np);
		for(int l = 0; l < cmbLine.length; l++) {
			cmbLine[l].setModel(createMessageModel(mess, l + 1));
			cmbLine[l].setEnabled(true);
		}
	}

	/** Calculate the number of pages for the sign */
	protected int calculateSignPages(DmsMessage[] mess, int nl) {
		int np = 1;
		for(int i = 0; i < mess.length; i++) {
			DmsMessage dm = mess[i];
			if(nl > 0 && dm.dms != null)
				np = Math.max(np, (dm.line - 1) / nl + 1);
		}
		return np;
	}

	/** Create the message model for one sign line */
	protected ComboBoxModel createMessageModel(DmsMessage[] mess, int l) {
		int w = width;
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(BLANK);
		for(int i = 0; i < mess.length; i++) {
			DmsMessage dm = mess[i];
			if(dm.line == l) {
				if(dm.m_width < 0 || dm.a_width < 0)
					continue;
				if(dm.m_width == 0 || dm.m_width > w) {
					if(dm.a_width == 0 || dm.a_width > w)
						continue;
				}
				model.addElement(dm);
			}
		}
		return model;
	}

	/** Cell renderer used for DMS messages */
	class DmsMessageCellRenderer extends BasicComboBoxRenderer {

		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected,
			boolean cellHasFocus)
		{
			DmsMessage dm = (DmsMessage)value;
			String v = "";
			short p = 50;
			if(dm != null) {
				if(dm.m_width <= width)
					v = dm.message;
				else
					v = dm.abbrev;
				p = dm.priority;
			}
			JLabel r = (JLabel)super.getListCellRendererComponent(
				list, v, index, isSelected, cellHasFocus);
			r.setHorizontalAlignment(SwingConstants.CENTER);
			if(p != 50 && !isSelected)
				r.setBackground(HIGHLIGHT);
			return r;
		}
	}
}
