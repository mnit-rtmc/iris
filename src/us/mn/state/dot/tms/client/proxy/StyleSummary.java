/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.utils.I18N;

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
	static private final int STYLE_COLS = 3;

	/** Number of grid columns for each style column */
	static private final int GRID_COLS = 4;

	/** Proxy manager */
	private final ProxyManager<T> manager;

	/** Radio button group */
	private final ButtonGroup r_buttons = new ButtonGroup();

	/** Mapping of style names to widgets */
	private final HashMap<String, StyleWidgets> widgets =
		new HashMap<String, StyleWidgets>();

	/** Titled border */
	private final TitledBorder border;

	/** Dummy list selection model */
	private final DefaultListSelectionModel dummy_model =
		new DefaultListSelectionModel();

	/** Selected style list model */
	private StyleListModel<T> model;

	/** Proxy list */
	private final ProxyJList<T> p_list;

	/** Style status counter */
	private final ProxyListener<T> counter = new ProxyListener<T>() {
		private boolean complete = false;
		@Override
		public void proxyAdded(T proxy) {
			if(complete)
				updateCounts();
		}
		@Override
		public void enumerationComplete() {
			complete = true;
			updateCounts();
		}
		@Override
		public void proxyRemoved(T proxy) {
			updateCounts();
		}
		@Override
		public void proxyChanged(T proxy, String attrib) {
			if(manager.isStyleAttrib(attrib))
				updateCounts();
		}
	};

	/** Widgets for one style */
	private class StyleWidgets {
		private final ItemStyle i_style;
		private final JRadioButton btn;
		private final JLabel legend_lbl;
		private final JLabel count_lbl;
		private int n_count;
		private StyleWidgets(Style sty, Icon legend) {
			i_style = ItemStyle.lookupStyle(sty.toString());
			btn = createRadioButton(i_style);
			legend_lbl = new JLabel(legend);
			count_lbl = new JLabel();
			n_count = 0;
		}
		private void countProxy(T proxy) {
			if (manager.checkStyle(i_style, proxy))
				n_count++;
		}
		private void updateCountLabel() {
			count_lbl.setText(Integer.toString(n_count));
		}
	}

	/** Create a new style summary panel, with optional cell size buttons.
	 * @param man ProxyManager */
	public StyleSummary(final ProxyManager<T> man, ItemStyle def_style,
		boolean enableCellSizeBtns)
	{
		super(new GridBagLayout());
		manager = man;
		border = BorderFactory.createTitledBorder("");
		setBorder(border);
		GridBagConstraints bag = new GridBagConstraints();
		ProxyTheme<T> theme = manager.getTheme();
		List<Style> styles = theme.getStyles();
		p_list = manager.createList();
		p_list.setCellRenderer(manager.createCellRenderer());
		JScrollPane sp = new JScrollPane(p_list);
		final int n_rows = (styles.size() - 1) / STYLE_COLS + 1;
		// grid is filled top to bottom, left to right
		for (int i = 0; i < styles.size() ; i++) {
			int col = i / n_rows;
			int row = i % n_rows;
			Style sty = styles.get(i);
			StyleWidgets sw = new StyleWidgets(sty,
				theme.getLegend(sty));
			widgets.put(sty.toString(), sw);
			bag.gridx = col * GRID_COLS;
			bag.gridy = row;
			bag.insets = new Insets(0, 0, 0, 2);
			bag.anchor = GridBagConstraints.EAST;
			add(sw.legend_lbl, bag);
			bag.gridx = GridBagConstraints.RELATIVE;
			add(sw.count_lbl, bag);
			bag.anchor = GridBagConstraints.WEST;
			add(sw.btn, bag);
		}

		// add space right of each column (except last)
		for(int c = 1; c < STYLE_COLS; c++) {
			bag.gridx = c * GRID_COLS - 1;
			bag.gridy = 0;
			bag.fill = GridBagConstraints.HORIZONTAL;
			bag.weightx = 0.1f;
			bag.insets = new Insets(2, 2, 2, 2);
			add(new JLabel(), bag);
		}

		// add optional panel with cell size selection buttons
		if (enableCellSizeBtns) {
			bag.gridx = 0;
			bag.gridwidth = 1;
			bag.gridheight = 1;
			bag.insets = new Insets(8, 2, 8, 2);
			bag.gridy = n_rows + 1;
			add(createCellSizePanel(), bag);
		}

		// add listbox
		bag.gridx = (enableCellSizeBtns ? 1 : 0);
		bag.gridwidth = GridBagConstraints.REMAINDER;
		bag.insets = new Insets(8, 0, 0, 0);
		bag.gridy = n_rows + 1;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.BOTH;
		add(sp, bag);

		setStyle(def_style);
	}

	/** Initialize the style summary */
	public void initialize() {
		manager.getCache().addProxyListener(counter);
	}

	/** Create a radio button for the given style list model */
	private JRadioButton createRadioButton(final ItemStyle i_style) {
		final JRadioButton btn = new JRadioButton(i_style.toString());
		r_buttons.add(btn);
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setStyleAction(i_style);
			}
		});
		return btn;
	}

	/** Update the count labels for each style status */
	private void updateCounts() {
		IWorker<Void> worker = new IWorker<Void>() {
			@Override
			public Void doInBackground() {
				doUpdateCounts();
				return null;
			}
			@Override
			public void done() {
				updateCountLabels();
			}
		};
		worker.execute();
	}

	/** Update the counts for each style status.  Must be synchronized
	 * in case multiple IWorkers are created. */
	private synchronized void doUpdateCounts() {
		for (StyleWidgets sw: widgets.values())
			sw.n_count = 0;
		for (T proxy: manager.getCache()) {
			for (StyleWidgets sw: widgets.values())
				sw.countProxy(proxy);
		}
	}

	/** Update the count labels.  Must be synchronized in case multiple
	 * IWorkers are created. */
	private synchronized void updateCountLabels() {
		for (StyleWidgets sw: widgets.values())
			sw.updateCountLabel();
	}

	/** Create the optional panel that contains cell size buttons. */
	private JPanel createCellSizePanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(3, 1, 0, 4));
		AbstractButton bs = createSizeButton(CellRendererSize.SMALL);
		AbstractButton bm = createSizeButton(CellRendererSize.MEDIUM);
		AbstractButton bl = createSizeButton(CellRendererSize.LARGE);
		panel.add(bs);
		panel.add(bm);
		panel.add(bl);
		ButtonGroup bgroup = new ButtonGroup();
		bgroup.add(bs);
		bgroup.add(bm);
		bgroup.add(bl);
		bl.setSelected(true);
		return panel;
	}

	/** Create a toggle button for changing cell renderer size */
	private AbstractButton createSizeButton(final CellRendererSize size) {
		String label = I18N.get(size.text_id);
		JToggleButton b = new JToggleButton(label);
		b.setMargin(new Insets(1, 1, 1, 1));
		Font f = b.getFont();
		b.setFont(f.deriveFont(0.8f * f.getSize2D()));
		b.setToolTipText(I18N.get(size.text_id + ".tooltip"));
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateRenderer(size);
			}
		});
		return b;
	}

	/** The cell renderer has changed sizes, update cell renderers. */
	private void updateRenderer(CellRendererSize size) {
		manager.setCellSize(size);
		p_list.setCellRenderer(manager.createCellRenderer());
	}

	/** Set the selected style, results in action + button selection
	 * changes. */
	private void setStyle(ItemStyle i_style) {
		StyleWidgets sw = widgets.get(i_style.toString());
		if (sw != null) {
			sw.btn.setSelected(true);
			setStyleAction(i_style);
		}
	}

	/** Button click action */
	private void setStyleAction(ItemStyle i_style) {
		String t = I18N.get(manager.getSonarType()) + " " +
			I18N.get("device.status") + ": " + i_style;
		border.setTitle(t);
		// Force the border title to be repainted
		repaint();
		StyleListModel<T> mdl = model;
		model = manager.getStyleModel(i_style.toString());
		// JList.setModel clears the selection, so let's use
		// a dummy selection model temporarily
		p_list.setSelectionModel(dummy_model);
		p_list.setModel(model);
		p_list.setSelectionModel(model.getSelectionModel());
		if (mdl != null)
			mdl.dispose();
	}

	/** Dispose of the widget */
	public void dispose() {
		manager.getCache().removeProxyListener(counter);
		removeAll();
	}
}
