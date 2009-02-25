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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;

/**
 * This class renders DMSs in a JList within the DMS StyleSummary.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class DmsCellRenderer extends JPanel implements ListCellRenderer {

	/** Test if a message is deployed */
	static public boolean isDeployed(SignMessage m) {
		if(m != null) {
			MultiString ms = new MultiString(m.getMulti());
			return !ms.isBlank();
		} else
			return false;
	}

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

	/** Create a new DMS cell renderer */
	public DmsCellRenderer() {
		super(new BorderLayout());
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
		setPreferredSize(new Dimension(190, 92));
	}

	/** Check if the background is opaque */
	public boolean isOpaque() {
		return true;
	}

	/** Get a component configured to render a cell of the list */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		if(value instanceof DMS)
			setDms((DMS)value);
		if(isSelected) {
			Component temp = cell.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
			title.setBackground(temp.getBackground());
		} else
			title.setBackground(lblID.getBackground());
		return this;
	}

	/** Format the owner of the given sign message */
	static protected String formatOwner(SignMessage m) {
		if(m != null)
			return formatOwner(m.getOwner());
		else
			return "";
	}

	/** Prune the owner string to the first dot */
	static protected String formatOwner(User owner) {
		if(owner != null) {
			String o = owner.getName();
			int i = o.indexOf('.');
			if(i >= 0)
				return o.substring(0, i);
			else
				return o;
		} else
			return "";
	}

	/** Set the DMS to be displayed */
	protected void setDms(DMS dms) {
		lblID.setText(dms.getName());
		lblLocation.setText(GeoLocHelper.getDescription(
			dms.getGeoLoc()));
		setDimensions(dms);
		pixelPnl.setGraphic(getPageOne(dms));
		SignMessage message = dms.getMessageCurrent();
		lblUser.setText(formatOwner(message));
	}

	/** Set the dimensions of the pixel panel */
	protected void setDimensions(DMS dms) {
		setPhysicalDimensions(dms);
		setLogicalDimensions(dms);
		pixelPnl.verifyDimensions();
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
		if(dms == null)
			return null;
		SignMessage m = dms.getMessageCurrent();
		if(m == null)
			return null;
		byte[] bmaps = decodeBitmaps(m.getBitmaps());
		if(bmaps == null || bmaps.length == 0)
			return null;
		BitmapGraphic bg = createBitmapGraphic(dms);
		if(bg == null)
			return null;
		int blen = bg.getBitmap().length;
		if(blen == 0 || bmaps.length % blen != 0)
			return null;
		byte[] b = new byte[blen];
		System.arraycopy(bmaps, 0, b, 0, blen);
		bg.setBitmap(b);
		return bg;
	}

	/** Decode the bitmaps */
	protected byte[] decodeBitmaps(String bitmaps) {
		try {
			return Base64.decode(bitmaps);
		}
		catch(IOException e) {
			return null;
		}
	}

	/** Create a bitmap graphic */
	protected BitmapGraphic createBitmapGraphic(DMS dms) {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		if(wp != null && hp != null)
			return new BitmapGraphic(wp, hp);
		else
			return null;
	}
}
