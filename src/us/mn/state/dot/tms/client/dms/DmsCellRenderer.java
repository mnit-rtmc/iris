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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignMessage;

/**
 * This class renders DMSs in a JList within the DMS StyleSummary.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class DmsCellRenderer extends JPanel implements ListCellRenderer {

	/** Non-expiring message time */
	static protected final String NO_EXPIRE = "E-XX:XX";

	/** Formatter for displaying the hour and minute */
	static protected final SimpleDateFormat HOUR_MINUTE =
		new SimpleDateFormat("HH:mm");

	/** Get the verification camera name */
	static protected String getCameraName(DMS dms) {
		Camera camera = dms.getCamera();
		if(camera == null)
			return " ";
		else
			return camera.getName();
	}

	/** Test if a message is deployed */
	static public boolean isDeployed(SignMessage m) {
		if(m != null) {
			MultiString ms = new MultiString(m.getMulti());
			return !ms.isBlank();
		} else
			return false;
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** The label that displays the camera id */
	protected final JLabel lblCamera = new JLabel();

	/** Sign pixel panel to display sign message */
	protected final SignPixelPanel pixelPnl = new SignPixelPanel();

	/** The label that displays the time the sign was deployed */
	protected final JLabel lblDeployed = new JLabel();

	/** The label that displays the time the current message will expire */
	protected final JLabel lblExpires = new JLabel();

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
	public DmsCellRenderer(Namespace ns) {
		super(new BorderLayout());
		namespace = ns;
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(2, 2, 2, 2),
			BorderFactory.createRaisedBevelBorder()));

		Box topBox = Box.createVerticalBox();
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(lblID);
		title.add(Box.createGlue());
		title.add(lblUser);
		topBox.add(title);
		location.add(lblLocation);
		location.add(Box.createGlue());
		topBox.add(location);

		Box bottomBox = Box.createHorizontalBox();
		bottomBox.add(lblDeployed);
		bottomBox.add(Box.createGlue());
		bottomBox.add(Box.createHorizontalStrut(8));
		bottomBox.add(lblExpires);
		bottomBox.add(Box.createHorizontalStrut(8));
		bottomBox.add(Box.createGlue());
		bottomBox.add(lblCamera);

		add(topBox, BorderLayout.NORTH);
		add(pixelPnl, BorderLayout.CENTER);
		add(bottomBox, BorderLayout.SOUTH);

		setPreferredSize(new Dimension(190, 102));
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

	/** Format the message deployed time */
	static protected String formatDeployed(SignMessage m) {
		return "D-" + HOUR_MINUTE.format(m.getDeployTime());
	}

	/** Format the message expriation */
	static protected String formatExpiration(SignMessage m) {
		Integer duration = m.getDuration();
		if(duration == null)
			return NO_EXPIRE;
		if(duration <= 0 || duration >= 65535)
			return NO_EXPIRE;
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(m.getDeployTime());
		c.add(Calendar.MINUTE, duration);
		return "E-" + HOUR_MINUTE.format(c.getTime());
	}

	/** Set the DMS to be displayed */
	protected void setDms(DMS dms) {
		lblID.setText(dms.getName());
		lblLocation.setText(GeoLocHelper.getDescription(
			dms.getGeoLoc()));
		lblCamera.setText(getCameraName(dms));
		setDimensions(dms);
		pixelPnl.setGraphic(null);
		// Note: getMessageCurrent will only return null after the
		//       DMS proxy has been destroyed.
		SignMessage message = dms.getMessageCurrent();
		if(isDeployed(message)) {
			lblUser.setText(formatOwner(message.getOwner()));
			lblDeployed.setText(formatDeployed(message));
			lblExpires.setText(formatExpiration(message));
			BitmapGraphic b = getPageOne(dms);
			if(b != null)
				pixelPnl.setGraphic(b);
		} else {
			lblUser.setText("");
			lblDeployed.setText("");
			lblExpires.setText("");
		}
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
		BitmapGraphic[] bitmaps = getBitmaps(dms);
		if(bitmaps != null && bitmaps.length > 0)
			return bitmaps[0];
		else
			return null;
	}

	/** Get the bitmap graphic for all pages */
	protected BitmapGraphic[] getBitmaps(DMS dms) {
		PixelMapBuilder b = createPixelMapBuilder(dms);
		if(b != null && dms != null) {
			SignMessage m = dms.getMessageCurrent();
			if(m != null) {
				b.clear();
				MultiString multi=new MultiString(m.getMulti());
				multi.parse(b, b.getDefaultFontNumber());
				return b.getPixmaps();
			}
		}
		return null;
	}

	/** Create the pixel map builder */
	protected PixelMapBuilder createPixelMapBuilder(DMS dms) {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(wp != null && hp != null && cw != null && ch != null)
			return new PixelMapBuilder(namespace, wp, hp, cw, ch);
		else
			return null;
	}
}
