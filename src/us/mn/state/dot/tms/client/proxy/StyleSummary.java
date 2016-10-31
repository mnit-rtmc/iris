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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import static javax.swing.LayoutStyle.ComponentPlacement.RELATED;
import javax.swing.SwingConstants;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.widget.IWorker;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Panel to display a summary of styled objects, which contains toggle
 * buttons which selected the current view, and a listbox below to show
 * the objects associated with the selected style.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class StyleSummary<T extends SonarObject> extends JPanel {

	/** Style button rows */
	static private final int STYLE_ROWS = 2;

	/** Get the count of style rows */
	static private int rowCount(int n_buttons) {
		return Math.min(n_buttons, STYLE_ROWS);
	}

	/** Get the count of style columns */
	static private int colCount(int n_buttons) {
		return Math.min(n_buttons, (n_buttons - 1) / STYLE_ROWS + 1);
	}

	/** Style button */
	static private class StyleButton extends JToggleButton {
		private final ItemStyle i_style;
		private int n_count;
		private StyleButton(ItemStyle is, Icon legend) {
			super(is.toString(), legend);
			setBorder(UI.buttonBorder());
			setMargin(UI.buttonInsets());
			setHorizontalAlignment(SwingConstants.LEADING);
			i_style = is;
			n_count = 0;
		}
	}

	/** Proxy manager */
	private final ProxyManager<T> manager;

	/** Default style */
	private final ItemStyle def_style;

	/** Style buttons */
	private final StyleButton[] buttons;

	/** Dummy list selection model */
	private final DefaultListSelectionModel dummy_model =
		new DefaultListSelectionModel();

	/** Selected style list model */
	private StyleListModel<T> model;

	/** Cell size buttons */
	private final JToggleButton[] sz_btns;

	/** Proxy list */
	private final ProxyJList<T> p_list;

	/** List scrollpane */
	private final JScrollPane s_pane;

	/** Style status counter */
	private final ProxyListener<T> counter = new ProxyListener<T>() {
		private boolean complete = false;
		@Override public void proxyAdded(T proxy) {
			if (complete)
				updateCounts();
		}
		@Override public void enumerationComplete() {
			complete = true;
			updateCounts();
		}
		@Override public void proxyRemoved(T proxy) {
			updateCounts();
		}
		@Override public void proxyChanged(T proxy, String attrib) {
			if (manager.isStyleAttrib(attrib))
				updateCounts();
		}
	};

	/** Create a new style summary panel.
	 * @param m ProxyManager.
	 * @param ds Default style.
	 * @param enableCellSizeBtns Flag to enable cell size buttons. */
	public StyleSummary(ProxyManager<T> m, ItemStyle ds,
		boolean enableCellSizeBtns)
	{
		manager = m;
		def_style = ds;
		sz_btns = enableCellSizeBtns ? createSizeButtons() : null;
		buttons = createStyleButtons();
		p_list = manager.createList();
 		s_pane = new JScrollPane(p_list);
	}

	/** Create cell size buttons */
	private JToggleButton[] createSizeButtons() {
		JToggleButton[] btns = new JToggleButton[3];
		btns[0] = createSizeButton(CellRendererSize.SMALL);
		btns[1] = createSizeButton(CellRendererSize.MEDIUM);
		btns[2] = createSizeButton(CellRendererSize.LARGE);
		ButtonGroup bg = new ButtonGroup();
		for (JToggleButton b : btns)
			bg.add(b);
		btns[2].setSelected(true);
		return btns;
	}

	/** Create a toggle button for changing cell renderer size */
	private JToggleButton createSizeButton(final CellRendererSize size) {
		JToggleButton b = new JToggleButton(I18N.get(size.text_id));
		b.setBorder(UI.buttonBorder());
		b.setMargin(UI.buttonInsets());
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

	/** Update cell renderer size */
	private void updateRenderer(CellRendererSize size) {
		manager.setCellSize(size);
		p_list.setCellRenderer(manager.createCellRenderer());
	}

	/** Create style buttons */
	private StyleButton[] createStyleButtons() {
		ProxyTheme<T> theme = manager.getTheme();
		List<Style> styles = theme.getStyles();
		ButtonGroup bg = new ButtonGroup();
		int n_styles = styles.size();
		StyleButton[] btns = new StyleButton[n_styles];
		for (int i = 0; i < btns.length; i++) {
			Style sty = styles.get(i);
			final ItemStyle i_style = ItemStyle.lookupStyle(
				sty.toString());
			StyleButton btn = new StyleButton(i_style,
				theme.getLegend(sty));
			btn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent a) {
					setStyleAction(i_style);
				}
			});
			btns[i] = btn;
			bg.add(btn);
		}
		return btns;
	}

	/** Initialize the style summary */
	public void initialize() {
		manager.getCache().addProxyListener(counter);
		p_list.setCellRenderer(manager.createCellRenderer());
		layoutPanel();
		setStyle(def_style);
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		GroupLayout.SequentialGroup sg = gl.createSequentialGroup();
		if (sz_btns != null) {
			GroupLayout.ParallelGroup sz = gl.createParallelGroup();
			for (JToggleButton b : sz_btns)
				sz.addComponent(b);
			sg.addGroup(sz);
			gl.linkSize(SwingConstants.HORIZONTAL, sz_btns);
		}
		sg.addPreferredGap(RELATED, UI.hgap, Short.MAX_VALUE);
		int n_rows = rowCount(buttons.length);
		for (int c = 0; c < colCount(buttons.length); c++) {
			StyleButton[] col = createColumn(n_rows, c);
			if (col.length > 0) {
				if (c > 0)
					sg.addGap(UI.hgap);
				GroupLayout.ParallelGroup cg =
					gl.createParallelGroup();
				for (StyleButton b : col)
					cg.addComponent(b);
				sg.addGroup(cg);
				gl.linkSize(SwingConstants.HORIZONTAL, col);
			}
		}
		sg.addPreferredGap(RELATED, UI.hgap, Short.MAX_VALUE);
		hg.addGroup(sg);
		hg.addComponent(s_pane);
		return hg;
	}

	/** Create a column of style buttons */
	private StyleButton[] createColumn(int n_rows, int c) {
		ArrayList<StyleButton> col = new ArrayList<StyleButton>();
		for (int r = 0; r < n_rows; r++) {
			int i = c * n_rows + r;
			if (i < buttons.length)
				col.add(buttons[i]);
		}
		return col.toArray(new StyleButton[0]);
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup pg = gl.createParallelGroup();
		if (sz_btns != null) {
			GroupLayout.SequentialGroup sz =
				gl.createSequentialGroup();
			sz.addComponent(sz_btns[0]);
			sz.addGap(UI.vgap);
			sz.addComponent(sz_btns[1]);
			sz.addGap(UI.vgap);
			sz.addComponent(sz_btns[2]);
			pg.addGroup(sz);
		}
		GroupLayout.SequentialGroup bg = gl.createSequentialGroup();
		int n_rows = rowCount(buttons.length);
		for (int r = 0; r < n_rows; r++) {
			if (r > 0)
				bg.addGap(UI.vgap);
			GroupLayout.ParallelGroup rg = gl.createParallelGroup();
			for (int c = 0; c < colCount(buttons.length); c++) {
				int i = c * n_rows + r;
				if (i < buttons.length)
					rg.addComponent(buttons[i]);
			}
			bg.addGroup(rg);
		}
		pg.addGroup(bg);
		vg.addGap(UI.vgap);
		vg.addGroup(pg);
		vg.addGap(UI.vgap);
		vg.addComponent(s_pane);
		return vg;
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
		for (StyleButton btn : buttons)
			btn.n_count = 0;
		for (T proxy : manager.getCache()) {
			for (StyleButton btn : buttons) {
				if (manager.checkStyle(btn.i_style, proxy))
					btn.n_count++;
			}
		}
	}

	/** Update the count labels.  Must be synchronized in case multiple
	 * IWorkers are created. */
	private synchronized void updateCountLabels() {
		for (StyleButton btn : buttons) {
			btn.setText(Integer.toString(btn.n_count) + ' ' +
				btn.i_style);
		}
	}

	/** Set the selected style */
	private void setStyle(ItemStyle i_style) {
		for (StyleButton btn : buttons) {
			if (i_style == btn.i_style) {
				btn.setSelected(true);
				setStyleAction(i_style);
			}
		}
	}

	/** Button click action */
	private void setStyleAction(ItemStyle i_style) {
		String t = I18N.get(manager.getSonarType()) + " " +
			I18N.get("device.status") + ": " + i_style;
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
