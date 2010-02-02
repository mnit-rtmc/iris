/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California, Davis
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import us.mn.state.dot.sonar.SonarObject;

/**
 * Panel to display a summary of styled objects, which contains radio
 * buttons which selected the current view, and a listbox below to show
 * the objects associated with the selected style.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class StyleSummary<T extends SonarObject> extends JPanel {

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Radio button group */
	protected final ButtonGroup r_buttons = new ButtonGroup();

	/** Card layout for style lists */
	protected final CardLayout cards = new CardLayout();

	/** List panel for card layout */
	protected final JPanel list_panel;

	/** Titled border */
	TitledBorder m_border;

	/** Component listener, may be null */
	ComponentListener m_clistener;

	/** Listboxes for each style */
	protected ProxyJList<?>[] m_list;

	/** scroll pane */
	private JScrollPane m_scroll;

	/** Create a new style summary panel.  
	 * @param man ProxyManager.
	 * @param clistener A ComponentListener or null to ignore. */
	public StyleSummary(final ProxyManager<T> man, 
		ComponentListener clistener)
	{
		super(new GridBagLayout());
		m_clistener = clistener;
		if(clistener != null)
			addComponentListener(clistener);
		manager = man;
		ListCellRenderer renderer = manager.createCellRenderer();
		m_border = BorderFactory.createTitledBorder(
			manager.getProxyType() + " Summary");
		setBorder(m_border);
		list_panel = new JPanel(cards);
		GridBagConstraints bag = new GridBagConstraints();
		String[] styles = manager.getStyles();
		m_list = new ProxyJList<?>[styles.length];
		String default_rbutton = "";
		final int colsper = 4; // blank, button, #, icon
		final int numcols = 3;
		final int numrows = (styles.length - 1) / numcols + 1;
		// grid is filled top to bottom, left to right
		for(int i = 0; i < styles.length; i++) {
			int col = i / numrows;
			int row = i % numrows;
			final StyleListModel<T> m =
				manager.getStyleModel(styles[i]);
			m_list[i] = manager.createList(styles[i]); 
			m_list[i].setCellRenderer(renderer);
			m_scroll = new JScrollPane(m_list[i]);
			list_panel.add(m_scroll, m.getName());
			final JRadioButton b = new JRadioButton(m.getName());
			r_buttons.add(b);
			// by default, the 1st button is selected
			if(i == 0)
				default_rbutton = m.getName();
			bag.gridx = col * colsper + 1;
			bag.gridy = row;
			bag.insets.right = 2;
			bag.anchor = GridBagConstraints.WEST;
			add(b, bag);
			bag.gridx = GridBagConstraints.RELATIVE;
			bag.anchor = GridBagConstraints.EAST;
			final JLabel c = new JLabel(Integer.toString(
				m.getSize()));
			add(c, bag);
			add(new JLabel(m.getLegend()), bag);
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setStyleAction(m.getName());
				}
			});
			m.addListDataListener(new ListDataListener() {
				public void contentsChanged(ListDataEvent e) { }
				public void intervalAdded(ListDataEvent e) {
					c.setText(Integer.toString(
						m.getSize()));
				}
				public void intervalRemoved(ListDataEvent e) {
					c.setText(Integer.toString(
						m.getSize()));
				}
			});
		}

		// add vertical space left of each column and right of last
		for(int c = 0; c < numcols + 1; ++c) {
			bag.gridx = c * colsper;
			bag.gridy = 0;
			bag.weightx = 1;
			bag.fill = GridBagConstraints.HORIZONTAL;
			add(new JPanel(), bag);
		}

		// add listbox
		bag.gridx = 0;
		bag.gridwidth = colsper * numcols;
		bag.insets.top = 8;
		bag.gridy = numrows + 1;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.BOTH;
		add(list_panel, bag);

		// These sizes force the SignPixelPanel in the 
		// dispatcher to switch sizes.
		setMinimumSize(new Dimension(500, 200));
		setPreferredSize(new Dimension(500, 275));

		// select default button
		setStyle(default_rbutton);
	}

	/** Set the selected style, results in action + button selection
	 * changes. This method is called by external classes, so the 
	 * specified button is selected, all other deselected. */
	public void setStyle(String style) {
		for(Enumeration e = r_buttons.getElements(); 
			e.hasMoreElements(); ) 
		{
			JRadioButton b = (JRadioButton)e.nextElement();
			b.setSelected(b.getText().equals(style));
		}
		setStyleAction(style);
	}

	/** Button click action. */
	private void setStyleAction(String style) {
		String t = manager.getProxyType() + " status: " + style;
		m_border = BorderFactory.createTitledBorder(t);
		m_border.setTitle(t);
		setBorder(m_border);
		cards.show(list_panel, style);
	}

	/** Get the listbox viewport size */
	public Dimension getViewportExtentSize() {
		return m_scroll.getViewport().getExtentSize();
	}

	/** The JPanel containing the listboxes has been resized. Update
	 *  all cell renderers. */
	public void updateRenderer() {
		ListCellRenderer r = manager.createCellRenderer();
		String[] styles = manager.getStyles();
		for(int i = 0; i < styles.length; i++)
			m_list[i].setCellRenderer(r);
	}

	/** Dispose of the widget */
	public void dispose() {
		if(m_clistener != null)
			removeComponentListener(m_clistener);
		removeAll();
	}
}
