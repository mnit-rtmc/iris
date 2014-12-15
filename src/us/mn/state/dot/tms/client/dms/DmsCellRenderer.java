/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2014  Minnesota Department of Transportation
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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.IrisUserHelper;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.proxy.CellRendererSize;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * This class renders DMSs in a JList within the DMS StyleSummary.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DmsCellRenderer extends JPanel implements ListCellRenderer {

	/** Prototype name */
	static private final String PROTOTYPE_NAME = "V999W99X";

	/** Sign pixel panel to display sign message */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(50, 200,
		false);

	/** List cell renderer (needed for colors) */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Title bar */
	private final JPanel title = new JPanel();

	/** The label that displays the sign ID */
	private final JLabel name_lbl = new JLabel();

	/** The label for the user */
	private final JLabel user_lbl = new JLabel();

	/** The label that displays the sign location */
	private final JLabel loc_lbl = new JLabel();

	/** DMS cell renderer mode */
	private enum DmsRendererMode {
		LARGE(160, 48, CellRendererSize.LARGE),
		MEDIUM(86, 24, CellRendererSize.MEDIUM),
		SMALL(58, 16, CellRendererSize.SMALL);

		/** Fixed pixel panel size */
		private final Dimension pixel_panel_size;

		/** Associated style summary cell renderer size */
		private final CellRendererSize cell_size;

		/** Create a new DMS renderer mode */
		private DmsRendererMode(int w, int h, CellRendererSize cs) {
			pixel_panel_size = UI.dimension(w, h);
			cell_size = cs;
		}

		/** Get preferred size of the pixel panel */
		public Dimension pixelPanelSize() {
			return pixel_panel_size;
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

	/** DMS to render */
	public final DMS dms;

	/** DMS cell renderer mode */
	private final DmsRendererMode mode;

	/** Create a new DMS cell renderer.
	 * @param d DMS to render.
	 * @param sz StyleSummary renderer cell size. */
	public DmsCellRenderer(DMS d, CellRendererSize sz) {
		super(new BorderLayout());
		dms = d;
		mode = DmsRendererMode.determine(sz);
	}

	/** Initialize a small size DMS cell renderer */
	private void initSmall() {
		setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.RAISED));
		title.setLayout(new GridLayout(1, 1));
		title.add(name_lbl);
		add(title);
		name_lbl.setText(PROTOTYPE_NAME);
		setPreferredSize(name_lbl.getPreferredSize());
	}

	/** Initialize a medium size DMS cell renderer */
	private void initMedium() {
		setBorder(BorderFactory.createCompoundBorder(
			  BorderFactory.createEmptyBorder(1, 1, 1, 1),
			  BorderFactory.createRaisedBevelBorder()));
		title.setLayout(new GridLayout(1, 1));
		title.add(name_lbl);
		add(title, BorderLayout.NORTH);
		add(pixel_pnl, BorderLayout.CENTER);
		// This is only needed to get preferred height
		name_lbl.setText(PROTOTYPE_NAME);
		Dimension lsz = name_lbl.getPreferredSize();
		Dimension psz = mode.pixelPanelSize();
		setPreferredSize(new Dimension(psz.width,
			lsz.height + psz.height));
	}

	/** Initialize a large size DMS cell renderer */
	private void initLarge() {
		setBorder(BorderFactory.createCompoundBorder(
			  BorderFactory.createEmptyBorder(1, 1, 1, 1),
			  BorderFactory.createRaisedBevelBorder()));
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(name_lbl);
		title.add(Box.createGlue());
		title.add(user_lbl);
		Box box = Box.createHorizontalBox();
		box.add(loc_lbl);
		box.add(Box.createGlue());
		add(title, BorderLayout.NORTH);
		add(pixel_pnl, BorderLayout.CENTER);
		add(box, BorderLayout.SOUTH);
		// This is only needed to get preferred height
		name_lbl.setText(PROTOTYPE_NAME);
		Dimension lsz = name_lbl.getPreferredSize();
		Dimension psz = mode.pixelPanelSize();
		setPreferredSize(new Dimension(psz.width,
			lsz.height * 2 + psz.height));
	}

	/** Check if the background is opaque */
	@Override
	public boolean isOpaque() {
		return true;
	}

	/** Get a component configured to render a cell of the list */
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		if (isSelected) {
			Component temp = cell.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
			title.setBackground(temp.getBackground());
		} else
			title.setBackground(name_lbl.getBackground());
		return this;
	}

	/** Update DMS attributes */
	public void initialize() {
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
		updateAttr("messageCurrent");
		updateAttr("ownerCurrent");
	}

	/** Update a specified attribute on the DMS.
	 * @param a Attribute to update. */
	public void updateAttr(String a) {
		if (a.equals("messageCurrent")) {
			String name = dms.getName();
			name_lbl.setText(name);
			String loc = GeoLocHelper.getDescription(
				dms.getGeoLoc());
			loc_lbl.setText(loc);
			updatePixelPanel();
			updateToolTip(name, loc);
		} else if (a.equals("ownerCurrent"))
			user_lbl.setText(formatOwner());
	}

	/** Format the owner name */
	private String formatOwner() {
		return IrisUserHelper.getNamePruned(dms.getOwnerCurrent());
	}

	/** Update the pixel panel */
	private void updatePixelPanel() {
		switch (mode) {
		case MEDIUM:
		case LARGE:
			pixel_pnl.setFilterColor(
				SignPixelPanel.filterColor(dms));
			pixel_pnl.setDimensions(dms);
			pixel_pnl.setGraphic(getPageOne());
			break;
		}
	}

	/** Update tooltip */
	private void updateToolTip(String name, String loc) {
		StringBuilder tt = new StringBuilder();
		switch (mode) {
		case SMALL:
			String owner = formatOwner();
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

	/** Get the raster graphic for page one */
	private RasterGraphic getPageOne() {
		RasterGraphic[] rasters = DMSHelper.getRasters(dms);
		if (rasters != null && rasters.length > 0)
			return rasters[0];
		else
			return null;
	}
}
