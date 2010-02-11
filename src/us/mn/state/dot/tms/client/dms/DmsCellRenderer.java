/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
 * Copyright (C) 2009-2010 AHMCT, University of California
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
import us.mn.state.dot.tms.Base64;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.client.proxy.CellRendererSize;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;
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
		SMALL(36, 24, CellRendererSize.SMALL);

		/** Fixed cell size */
		Dimension m_size = new Dimension(1, 1);

		/** Associated style summary renderer size */
		CellRendererSize m_rs;

		/** constructor */
		private DmsRendererMode(int w, int h, CellRendererSize rs) {
			m_size.width = w;
			m_size.height = h;
			m_rs = rs;
		}

		/** Get size */
		public Dimension getSize() {
			return m_size;
		}

		/** Calculate number of vertical large renderers */
		public int numVert(int vertpix) {
			return vertpix / (int)getSize().getHeight();
		}

		/** Determine the dms renderer mode, which determines the size
		 * and apperance of the renderer.
		 * @param sz StyleSummary renderer size. */
		private static DmsRendererMode determine(CellRendererSize sz) {
			for(DmsRendererMode e : DmsRendererMode.values()) 
				if(e.m_rs == sz)
					return e;
			assert false;
			return MEDIUM;
		}
	}

	/** DMS cell renderer mode */
	private DmsRendererMode m_mode = DmsRendererMode.LARGE;

	/** Last render mode, which is used to determine if a resize
	 *  will result in a cell size change. */
	private static DmsRendererMode m_lastmode = DmsRendererMode.LARGE;

	/** Set cell render mode. */
	private void setMode(DmsRendererMode newmode) {
		m_mode = newmode;
		m_lastmode = newmode;
	}

	/** Create a new DMS cell renderer as a function of the style
	 * summary viewport size. 
	 * @param sz StyleSummary renderer cell size. */
	public DmsCellRenderer(CellRendererSize sz) {
		super(new BorderLayout());
		create(DmsRendererMode.determine(sz));
	}

	/** Create a new DMS cell renderer of the specified size. */
	private void create(DmsRendererMode m) {
		setMode(m);
		if(m == DmsRendererMode.LARGE)
			createLarge();
		else if(m == DmsRendererMode.MEDIUM)
			createMedium();
		else if(m == DmsRendererMode.SMALL)
			createSmall();
		else
			createLarge();
	}

	/** Create a new DMS cell renderer with small cells */
	private void createSmall() {
		setBorder(BorderFactory.createEtchedBorder(
			EtchedBorder.RAISED));
		title.setLayout(new GridLayout(1, 1));
		title.add(lblID);
		add(title);
		setPreferredSize(m_mode.getSize());
		//setPreferredSize(new Dimension(36, 24));
	}

	/** Create a new DMS cell renderer with medium cells */
	private void createMedium() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createCompoundBorder(
			  BorderFactory.createEmptyBorder(1, 1, 1, 1),
			  BorderFactory.createRaisedBevelBorder()));
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(lblID);
		title.add(Box.createGlue());
		title.add(lblUser);
		add(title, BorderLayout.NORTH);
		add(pixelPnl, BorderLayout.CENTER);
		setPreferredSize(new Dimension(46 * 2, 46));
	}

	/** Create a new DMS cell renderer with large cells */
	private void createLarge() {
		setLayout(new BorderLayout());
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
		setPreferredSize(new Dimension(190, 92)); // aspect 2.065
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
		User u = dms.getOwnerCurrent();
		String s = pruneOwner(u);
		if(m_mode == DmsRendererMode.MEDIUM)
			s = SString.truncate(s, 8);
		return s;
	}

	/** Prune the owner string to the first dot. FIXME:move to UserHelper*/
	static protected String pruneOwner(User owner) {
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
		if(m_mode == DmsRendererMode.SMALL) {
			tt.append(dmsname);
			if(!author.isEmpty())
			tt.append(": ").append(author);
			tt.append(": ").append(loca);
			tt.append(": ").append(DMSHelper.buildMsgLine(dms));
		} else if(m_mode == DmsRendererMode.MEDIUM)
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
		int blen = bg.length();
		if(blen == 0 || bmaps.length % blen != 0)
			return null;
		byte[] b = new byte[blen];
		System.arraycopy(bmaps, 0, b, 0, blen);
		bg.setPixels(b);
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
