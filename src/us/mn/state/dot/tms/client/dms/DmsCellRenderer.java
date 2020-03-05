/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2019  Minnesota Department of Transportation
 * Copyright (C) 2009-2010  AHMCT, University of California
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.CellRendererSize;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * This class renders DMSs in a JList within the DMS StyleSummary.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DmsCellRenderer extends JPanel implements ListCellRenderer<DMS> {

	/** DMS cell renderer mode */
	private enum DmsRendererMode {
		SMALL(0, 0, 1, CellRendererSize.SMALL),
		MEDIUM(80, 26, 1, CellRendererSize.MEDIUM),
		LARGE(160, 52, 2, CellRendererSize.LARGE);

		/** Fixed pixel panel size */
		public final Dimension pixel_panel_size;

		/** Associated style summary cell renderer size */
		private final CellRendererSize cell_size;

		/** Number of labels above / below pixel panel */
		private final int n_labels;

		/** Create a new DMS renderer mode */
		private DmsRendererMode(int w, int h, int nl,
			CellRendererSize cs)
		{
			pixel_panel_size = UI.dimension(w, h);
			cell_size = cs;
			n_labels = nl;
		}

		/** Determine the dms renderer mode, which determines the size
		 * and apperance of the renderer.
		 * @param sz StyleSummary renderer size. */
		static private DmsRendererMode determine(CellRendererSize sz) {
			for (DmsRendererMode m : DmsRendererMode.values()) {
				if (m.cell_size == sz)
					return m;
			}
			assert false;
			return LARGE;
		}
	}

	/** Prototype name */
	static private final String PROTOTYPE_NAME = "VM999W99X_9";

	/** User session */
	protected final Session session;

	/** DMS cell renderer mode */
	private final DmsRendererMode mode;

	/** List cell renderer (needed for colors) */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Title panel */
	private final JPanel title_pnl = new JPanel();

	/** Sign ID label */
	private final JLabel name_lbl = new JLabel();

	/** Message owner label */
	private final JLabel owner_lbl = new ILabel("", Font.ITALIC, 0.8f);

	/** Sign pixel panel to display sign message */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(50, 200);

	/** Location panel */
	private final JPanel loc_pnl = new JPanel();

	/** Sign location label */
	private final JLabel loc_lbl = new JLabel();

	/** Create a new DMS cell renderer.
	 * @param sz StyleSummary renderer cell size. */
	public DmsCellRenderer(Session s, CellRendererSize sz) {
		super(new BorderLayout());
		session = s;
		mode = DmsRendererMode.determine(sz);
		initialize();
	}

	/** Initialize the renderer */
	private void initialize() {
		setBorder(UI.cellRendererBorder());
		setPreferredSize();
		switch (mode) {
		case SMALL:
			initSmall();
			break;
		case MEDIUM:
			initMedium();
			break;
		default:
			initLarge();
		}
	}

	/** Set preferred size of renderer */
	private void setPreferredSize() {
		// This is only needed to get preferred height
		name_lbl.setText(PROTOTYPE_NAME);
		Dimension lsz = name_lbl.getPreferredSize();
		Dimension psz = mode.pixel_panel_size;
		setPreferredSize(new Dimension(Math.max(psz.width, lsz.width),
			lsz.height * mode.n_labels + psz.height));
	}

	/** Initialize a small size DMS cell renderer */
	private void initSmall() {
		title_pnl.setLayout(new GridLayout(1, 1));
		title_pnl.add(name_lbl);
		add(title_pnl);
	}

	/** Initialize a medium size DMS cell renderer */
	private void initMedium() {
		title_pnl.setLayout(new GridLayout(1, 1));
		title_pnl.add(name_lbl);
		add(title_pnl, BorderLayout.NORTH);
		add(pixel_pnl, BorderLayout.CENTER);
	}

	/** Initialize a large size DMS cell renderer */
	private void initLarge() {
		title_pnl.setLayout(new BoxLayout(title_pnl, BoxLayout.X_AXIS));
		title_pnl.add(name_lbl);
		title_pnl.add(Box.createGlue());
		title_pnl.add(owner_lbl);
		loc_pnl.setLayout(new BoxLayout(loc_pnl, BoxLayout.X_AXIS));
		loc_pnl.add(loc_lbl);
		loc_pnl.add(Box.createGlue());
		add(title_pnl, BorderLayout.NORTH);
		add(pixel_pnl, BorderLayout.CENTER);
		add(loc_pnl, BorderLayout.SOUTH);
	}

	/** Check if the background is opaque */
	@Override
	public boolean isOpaque() {
		return true;
	}

	/** Get a component configured to render a cell of the list */
	@Override
	public Component getListCellRendererComponent(JList<? extends DMS> list,
		DMS dms, int index, boolean isSelected, boolean cellHasFocus)
	{
		if (isSelected) {
			Component temp = cell.getListCellRendererComponent(list,
				dms, index, isSelected, cellHasFocus);
			title_pnl.setBackground(temp.getBackground());
		} else
			title_pnl.setBackground(name_lbl.getBackground());
		setDMS(dms);
		return this;
	}

	/** Set the DMS to render */
	private void setDMS(DMS dms) {
		String name = dms.getName();
		name_lbl.setText(name);
		owner_lbl.setText(getOwner(dms));
		updatePixelPanel(dms);
		String loc = GeoLocHelper.getLocation(dms.getGeoLoc());
		updateToolTip(dms, name, loc);
		Color bc = getIncBackground(dms);
		pixel_pnl.setBackground(bc);
		loc_lbl.setText(loc);
		loc_pnl.setBackground(bc);
	}

	/** Get background color of associated incident */
	private Color getIncBackground(DMS dms) {
		Incident inc = DMSHelper.lookupIncident(dms);
		return (inc != null)
		      ? session.getIncidentManager().getStyle(inc).fill_color
		      : null;
	}

	/** Get the owner user name (may be overridden) */
	protected String getOwner(DMS dms) {
		String o = DMSHelper.getOwner(dms);
		return (o != null) ? o : "";
	}

	/** Update tooltip */
	private void updateToolTip(DMS dms, String name, String loc) {
		StringBuilder tt = new StringBuilder();
		switch (mode) {
		case SMALL:
			String owner = getOwner(dms);
			tt.append(name);
			if (!owner.isEmpty()) {
				tt.append(": ");
				tt.append(owner);
			}
			tt.append(": ");
			tt.append(loc);
			tt.append(": ");
			tt.append(DMSHelper.buildMsgLine(dms));
			break;
		case MEDIUM:
			tt.append(name);
			tt.append(": ");
			tt.append(loc);
			break;
		}
		setToolTipText(tt.toString());
 	}

	/** Update the pixel panel */
	private void updatePixelPanel(DMS dms) {
		switch (mode) {
		case MEDIUM:
		case LARGE:
			pixel_pnl.setFilterColor(
				SignPixelPanel.filterColor(dms));
			pixel_pnl.setDimensions(dms.getSignConfig());
			pixel_pnl.setGraphic(getPageOne(dms));
			break;
		}
	}

	/** Get the raster graphic for page one (may be overridden) */
	protected RasterGraphic getPageOne(DMS dms) {
		return DMSHelper.getPageOne(dms);
	}
}
