/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2010  Minnesota Department of Transportation
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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;
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

	/** Number of style columns in summary */
	static protected final int STYLE_COLS = 3;

	/** Number of grid columns for each style column */
	static protected final int GRID_COLS = 4;

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Radio button group */
	protected final ButtonGroup r_buttons = new ButtonGroup();

	/** Card layout for style lists */
	protected final CardLayout cards = new CardLayout();

	/** List panel for card layout */
	protected final JPanel list_panel;

	/** Titled border */
	protected final TitledBorder border;

	/** Listboxes for each style */
	protected final ProxyJList<?>[] s_list;

	/** scroll pane.  FIXME: this is only the last scroll pane created. */
	private JScrollPane m_scroll;

	/** Indicates if optional cell size buttons are enabled. */
	private boolean m_enableCellSizeBtns;

	/** Callback for cell resize event handling, may be null */
	private ProxyManager.ResizeEventCallback m_resizeEvent;

	/** Create a new style summary panel, with optional cell size buttons.
	 * @param man ProxyManager */
	public StyleSummary(final ProxyManager<T> man, 
		boolean enableCellSizeBtns, 
		ProxyManager.ResizeEventCallback resizeEvent)
	{
		super(new GridBagLayout());
		manager = man;
		m_enableCellSizeBtns = enableCellSizeBtns;
		m_resizeEvent = resizeEvent;
		ListCellRenderer renderer = manager.createCellRenderer();
		border = BorderFactory.createTitledBorder("");
		setBorder(border);
		list_panel = new JPanel(cards);
		GridBagConstraints bag = new GridBagConstraints();
		String[] styles = manager.getStyles();
		s_list = new ProxyJList<?>[styles.length];
		String default_rbutton = "";
		final int n_rows = (styles.length - 1) / STYLE_COLS + 1;
		// grid is filled top to bottom, left to right
		for(int i = 0; i < styles.length; i++) {
			int col = i / n_rows;
			int row = i % n_rows;
			final StyleListModel<T> m =
				manager.getStyleModel(styles[i]);
			s_list[i] = manager.createList(styles[i]); 
			s_list[i].setCellRenderer(renderer);
			m_scroll = new JScrollPane(s_list[i]);
			list_panel.add(m_scroll, m.getName());
			// by default, the 1st button is selected
			if(i == 0)
				default_rbutton = m.getName();
			bag.gridx = col * GRID_COLS;
			bag.gridy = row;
			bag.insets = new Insets(0, 0, 0, 2);
			bag.anchor = GridBagConstraints.EAST;
			add(new JLabel(m.getLegend()), bag);
			bag.gridx = GridBagConstraints.RELATIVE;
			add(createCount(m), bag);
			bag.anchor = GridBagConstraints.WEST;
			add(createRadioButton(m), bag);
		}

		// add vertical space right of each column (except last)
		for(int c = 1; c < STYLE_COLS; c++) {
			bag.gridx = c * GRID_COLS - 1;
			bag.gridy = 0;
			bag.weightx = 1;
			bag.fill = GridBagConstraints.HORIZONTAL;
			bag.insets = new Insets(0, 0, 0, 0);
			add(new JLabel(), bag);
		}

		// add optional panel with cell size selection buttons
		if(m_enableCellSizeBtns) {
			bag.gridx = 0;
			bag.gridwidth = 1;
			bag.gridheight = 1;
			bag.insets = new Insets(8, 2, 0, 0);
			bag.gridy = n_rows + 1;
			bag.weightx = 1;
			bag.weighty = 1;
			bag.fill = GridBagConstraints.BOTH;
			add(createCellSizePanel(), bag);
		}

		// add listbox
		bag.gridx = (m_enableCellSizeBtns ? 1 : 0);
		bag.gridwidth = GridBagConstraints.REMAINDER;
		bag.insets = new Insets(8, 0, 0, 0);
		bag.gridy = n_rows + 1;
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

	/** Create the optional panel that contains cell size buttons. */
	private JPanel createCellSizePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		AbstractButton bs = createButton(CellRendererSize.SMALL);
		AbstractButton bm = createButton(CellRendererSize.MEDIUM);
		AbstractButton bl = createButton(CellRendererSize.LARGE);
		panel.add(bs);
		Dimension vspace = new Dimension(0,4);
		panel.add(Box.createRigidArea(vspace));
		panel.add(bm);
		panel.add(Box.createRigidArea(vspace));
		panel.add(bl);
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(bs);
		bgroup.add(bm);
		bgroup.add(bl);
		selectCellBtn(bgroup, bm);
		return panel;
	}

	/** Create a toggle button for a cell renderer size. */
	private AbstractButton createButton(CellRendererSize size) {
		String label = size.m_sname;
		JToggleButton b = new JToggleButton(label);
		b.setMargin(new Insets(0, 0, 0, 0));
		b.setFont(new java.awt.Font(("SansSerif"), 
			java.awt.Font.PLAIN, 10));
		b.setToolTipText("Switch to " + size.m_name + " " + 
			manager.getProxyType() + " icons.");
		Dimension bsize = new Dimension(18, 24);
		b.setPreferredSize(bsize);
		b.setMaximumSize(bsize);
		b.setMinimumSize(bsize);
		b.addActionListener(new ActionListener() {
			// cell resize button pressed
			public void actionPerformed(ActionEvent e) {
				if(m_resizeEvent == null)
					return;
				m_resizeEvent.resized(CellRendererSize.get(
					e.getActionCommand()));
			}
		});
		return b;
	}

	/** Select toggle button */
	private static void selectCellBtn(ButtonGroup g, AbstractButton d) {
		for(Enumeration e = g.getElements(); e.hasMoreElements();) {
			AbstractButton b = (AbstractButton)e.nextElement();
			b.setSelected(b == d);
		}
	}

	/** Create a radio button for the given style list model */
	protected JRadioButton createRadioButton(final StyleListModel<T> mdl) {
		final JRadioButton btn = new JRadioButton(mdl.getName());
		r_buttons.add(btn);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setStyleAction(mdl.getName());
			}
		});
		return btn;
	}

	/** Create a count label for the given style list model */
	protected JLabel createCount(final StyleListModel<T> mdl) {
		final JLabel lbl = new JLabel(Integer.toString(mdl.getSize()));
		mdl.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent e) { }
			public void intervalAdded(ListDataEvent e) {
				lbl.setText(Integer.toString(mdl.getSize()));
			}
			public void intervalRemoved(ListDataEvent e) {
				lbl.setText(Integer.toString(mdl.getSize()));
			}
		});
		return lbl;
	}

	/** Set the selected style, results in action + button selection
	 * changes. This method is called by external classes, so the 
	 * specified button is selected, all other deselected. */
	public void setStyle(String style) {
		for(Enumeration e = r_buttons.getElements(); 
		    e.hasMoreElements(); ) 
		{
			JRadioButton btn = (JRadioButton)e.nextElement();
			btn.setSelected(btn.getText().equals(style));
		}
		setStyleAction(style);
	}

	/** Button click action. */
	private void setStyleAction(String style) {
		String t = manager.getProxyType() + " status: " + style;
		border.setTitle(t);
		// Force the border title to be repainted
		repaint();
		cards.show(list_panel, style);
	}

	/** The cell renderer has changed sizes, update cell renderers. */
	public void updateRenderer(CellRendererSize cellSize) {
		manager.setCellSize(cellSize);
		ListCellRenderer r = manager.createCellRenderer();
		String[] styles = manager.getStyles();
		for(int i = 0; i < styles.length; i++)
			s_list[i].setCellRenderer(r);
	}

	/** Dispose of the widget */
	public void dispose() {
		removeAll();
	}
}
