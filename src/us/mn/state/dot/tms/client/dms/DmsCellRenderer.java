/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
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
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.EtchedBorder;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.client.proxy.CellRendererSize;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.IrisUserHelper;
import us.mn.state.dot.tms.utils.SString;

/**
 * This class renders DMSs in a JList within the DMS StyleSummary.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DmsCellRenderer extends JPanel implements ListCellRenderer {

	/** Sign pixel panel to display sign message */
	protected final SignPixelPanel pixelPnl = new SignPixelPanel(false);

	/** List cell renderer (needed for colors) */
	protected final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Title bar */
	protected final JPanel title = new JPanel();

	/** The label that displays the sign ID */
	protected final JLabel lblID = new JLabel();

	/** The label for the user */
	protected final JLabel lblUser = new JLabel();

	/** Location bar */
	protected final Box location = Box.createHorizontalBox();

	/** The label that displays the sign location */
	protected final JLabel lblLocation = new JLabel();

	/** DMS cell renderer mode */
	private enum DmsRendererMode {
		LARGE(190, 92, CellRendererSize.LARGE),
		MEDIUM(46 * 2, 46, CellRendererSize.MEDIUM),
		SMALL(64, 20, CellRendererSize.SMALL);

		/** Fixed cell size */
		protected final Dimension size;

		/** Associated style summary cell renderer size */
		protected final CellRendererSize cell_size;

		/** constructor */
		private DmsRendererMode(int w, int h, CellRendererSize cs) {
			size = new Dimension(w, h);
			cell_size = cs;
		}

		/** Get renderer size */
		public Dimension getSize() {
			return size;
		}

		/** Determine the dms renderer mode, which determines the size
		 * and apperance of the renderer.
		 * @param sz StyleSummary renderer size. */
		static private DmsRendererMode determine(CellRendererSize sz) {
			for(DmsRendererMode m : DmsRendererMode.values()) 
				if(m.cell_size == sz)
					return m;
			assert false;
			return LARGE;
		}
	}

	/** DMS cell renderer mode */
	private final DmsRendererMode mode;

	/** Create a new DMS cell renderer.
	 * @param sz StyleSummary renderer cell size. */
	public DmsCellRenderer(CellRendererSize sz) {
		super(new BorderLayout());
		mode = DmsRendererMode.determine(sz);
		switch(mode) {
		case LARGE:
			initLarge();
			break;
		case MEDIUM:
			initMedium();
			break;
		case SMALL:
			initSmall();
			break;
		default:
			assert false;
			initLarge();
		}
		setPreferredSize(mode.getSize());
	}

	/** Initialize a small size DMS cell renderer */
	private void initSmall() {
		setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.RAISED));
		title.setLayout(new GridLayout(1, 1));
		title.add(lblID);
		add(title);
	}

	/** Initialize a medium size DMS cell renderer */
	private void initMedium() {
		setBorder(BorderFactory.createCompoundBorder(
			  BorderFactory.createEmptyBorder(1, 1, 1, 1),
			  BorderFactory.createRaisedBevelBorder()));
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(lblID);
		title.add(Box.createGlue());
		title.add(lblUser);
		add(title, BorderLayout.NORTH);
		add(pixelPnl, BorderLayout.CENTER);
	}

	/** Initialize a large size DMS cell renderer */
	private void initLarge() {
		setBorder(BorderFactory.createCompoundBorder(
			  BorderFactory.createEmptyBorder(1, 1, 1, 1),
			  BorderFactory.createRaisedBevelBorder()));
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(lblID);
		title.add(Box.createGlue());
		title.add(lblUser);
		location.add(lblLocation);
		location.add(Box.createGlue());
		add(title, BorderLayout.NORTH);
		add(pixelPnl, BorderLayout.CENTER);
		add(location, BorderLayout.SOUTH);
	}

	/** Check if the background is opaque */
	public boolean isOpaque() {
		return true;
	}

	/** Get a component configured to render a cell of the list */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		if(isSelected) {
			Component temp = cell.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
			title.setBackground(temp.getBackground());
		} else
			title.setBackground(lblID.getBackground());
		return this;
	}

	/** Return the owner name as a function of the display mode */
	protected String formatOwner(DMS dms) {
		String s = IrisUserHelper.getNamePruned(dms.getOwnerCurrent());
		if(mode == DmsRendererMode.MEDIUM)
			return SString.truncate(s, 8);
		else
			return s;
	}

	/** Set the DMS to be displayed. All attributes are updated. */
	public void setDms(DMS dms) {
		updateDms(dms, "messageCurrent");
		updateDms(dms, "ownerCurrent");
	}

	/** Update a specified attribute on the DMS.
	 *  @param dms DMS to update.
	 *  @param a Attribute to update. */
	public void updateDms(DMS dms, String a) {
		if(a.equals("messageCurrent")) {
			String dmsname = dms.getName();
			lblID.setText(dmsname);
			String loca = GeoLocHelper.
				getDescription(dms.getGeoLoc());
			lblLocation.setText(loca);
			setDimensions(dms);
			pixelPnl.setGraphic(getPageOne(dms));
			updateToolTip(dms, dmsname, loca, formatOwner(dms));
		} else if(a.equals("ownerCurrent"))
			lblUser.setText(formatOwner(dms));
	}

	/** Update tooltip */
	private void updateToolTip(DMS dms, String dmsname, String loca, 
		String author) 
	{
		StringBuilder tt = new StringBuilder("");
		if(mode == DmsRendererMode.SMALL) {
			tt.append(dmsname);
			if(!author.isEmpty())
				tt.append(": ").append(author);
			tt.append(": ").append(loca);
			tt.append(": ").append(DMSHelper.buildMsgLine(dms));
		} else if(mode == DmsRendererMode.MEDIUM)
			tt.append(dmsname).append(": ").append(loca);
		setToolTipText(tt.toString());
 	}

	/** Set the dimensions of the pixel panel */
	protected void setDimensions(DMS dms) {
		setPhysicalDimensions(dms);
		setLogicalDimensions(dms);
	}

	/** Set the physical dimensions of the pixel panel */
	protected void setPhysicalDimensions(DMS dms) {
		Integer w = dms.getFaceWidth();
		Integer h = dms.getFaceHeight();
		Integer hp = dms.getHorizontalPitch();
		Integer vp = dms.getVerticalPitch();
		Integer hb = dms.getHorizontalBorder();
		Integer vb = dms.getVerticalBorder();
		if(w != null && h != null && hp != null && vp != null &&
		   hb != null && vb != null)
		{
			pixelPnl.setPhysicalDimensions(w, h, hb, vb, hp, vp);
		} else
			pixelPnl.setPhysicalDimensions(0, 0, 0, 0, 0, 0);
	}

	/** Set the logical dimensions of the pixel panel */
	protected void setLogicalDimensions(DMS dms) {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(wp != null && hp != null && cw != null && ch != null)
			pixelPnl.setLogicalDimensions(wp, hp, cw, ch);
		else
			pixelPnl.setLogicalDimensions(0, 0, 0, 0);
	}

	/** Get the bitmap graphic for page one */
	protected BitmapGraphic getPageOne(DMS dms) {
		BitmapGraphic[] bitmaps = DMSHelper.getBitmaps(dms);
		if(bitmaps != null && bitmaps.length > 0)
			return bitmaps[0];
		else
			return null;
	}
}
