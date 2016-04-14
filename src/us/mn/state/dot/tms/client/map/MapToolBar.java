/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JToolBar;

/**
 * Toolbar for legend, themes and zoom buttons for MapBean.
 *
 * @author Douglas Lau
 */
public class MapToolBar extends JToolBar {

	/** Map associated with the tool bar */
	private final MapBean map;

	/** Menu bar */
	private final JMenuBar menu = new JMenuBar();

	/** Layer menu */
	private final LayerMenu layers = new LayerMenu();

	/** Legend menu */
	private final JMenu legend = new JMenu("Legend");

	/** Theme selection combo box */
	private final JComboBox<Theme> themes = new JComboBox<Theme>();

	/** Action for zoom in button */
	private final Action zoom_in;

	/** Action for zoom out button */
	private final Action zoom_out;

	/** Create a new map tool bar */
	public MapToolBar(MapBean m) {
		map = m;
		zoom_in = createZoomInAction();
		zoom_out = createZoomOutAction();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		menu.setAlignmentY(.5f);
		menu.add(layers);
		menu.add(legend);
		menu.add(themes);
		map.addLayerChangeListener(new LayerChangeListener() {
			public void layerChanged(LayerChangeEvent ev) {
				if(ev.getReason() == LayerChange.model)
					updateLayerMenu();
			}
		});
	}

	/** Create the zoom-in action */
	protected Action createZoomInAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				map.zoom(true);
			}
		};
		a.putValue(Action.NAME, " + ");
		a.putValue(Action.SHORT_DESCRIPTION, "Zoom map view in");
		return a;
	}

	/** Create the zoom-out action */
	protected Action createZoomOutAction() {
		Action a = new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				map.zoom(false);
			}
		};
		a.putValue(Action.NAME, " - ");
		a.putValue(Action.SHORT_DESCRIPTION, "Zoom map view out");
		return a;
	}

	/** Update the layer menu */
	private void updateLayerMenu() {
		layers.removeAll();
		legend.removeAll();
		for(LayerState ls: map.getLayers()) {
			layers.addLayer(ls);
			addThemeLegend(ls);
		}
	}

	/** Add a theme legend to the tool bar */
	private void addThemeLegend(LayerState ls) {
		String name = ls.getLayer().getName();
		LegendMenu lm = new LegendMenu(name, ls.getTheme());
		if(lm.getItemCount() > 1) {
			legend.add(lm);
			createThemeModel(ls, lm);
		}
	}

	/** Create the theme combo box model */
	private void createThemeModel(LayerState ls, LegendMenu lm) {
		DefaultComboBoxModel<Theme> mdl =
			new DefaultComboBoxModel<Theme>();
		for (Theme t: ls.getThemes())
			mdl.addElement(t);
		// FIXME: this is a fragile way to check for SegmentLayerState
		if (mdl.getSize() > 1) {
			addThemeListener(ls, lm);
			themes.setModel(mdl);
		}
	}

	/** Add a theme listener for one layer state */
	private void addThemeListener(final LayerState ls, final LegendMenu lm){
		themes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object obj = themes.getSelectedItem();
				if(obj instanceof Theme) {
					Theme t = (Theme)obj;
					ls.setTheme(t);
					lm.setTheme(t);
				}
			}
		});
	}

	/** Add the menu, theme and zoom buttons */
	public void addMenu() {
		add(menu);
		add(Box.createGlue());
		addButton(new JButton(zoom_in));
		addButton(new JButton(zoom_out));
	}

	/** Add a button to the toolbar */
	public void addButton(AbstractButton b) {
		b.setMargin(new Insets(2, 2, 2, 2));
		add(b);
		add(Box.createHorizontalStrut(4));
	}

	/** Clear all widgets from tool bar */
	public void clear() {
		for(ActionListener al: themes.getActionListeners())
			themes.removeActionListener(al);
		removeAll();
	}
}
