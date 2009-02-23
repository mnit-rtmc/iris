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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Camera;
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

	/** Non-expiring message time */
	static protected final String NO_EXPIRE = "E-XX:XX";

	/** Formatter for displaying the hour and minute */
	static protected final SimpleDateFormat HOUR_MINUTE =
		new SimpleDateFormat("HH:mm");

	/** Number of message lines to render */
	static protected final int MESSAGE_LINES = 3;

	/** Font for rendering */
	static protected final Font FONT = new Font("Dialog", Font.BOLD, 12);

	/** Get the verification camera name */
	static protected String getCameraName(DMS proxy) {
		Camera camera = proxy.getCamera();
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

	/** The label that displays the camera id */
	private final JLabel lblCamera = new JLabel();

	/** The array of labels that display the lines of the sign */
	protected final JLabel[] lblLine = new JLabel[MESSAGE_LINES];

	/** The label that displays the time the sign was deployed */
	private final JLabel lblDeployed = new JLabel();

	/** The label that displays the time the current message will expire */
	private final JLabel lblExpires = new JLabel();

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
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createEmptyBorder(2, 2, 2, 2),
			BorderFactory.createRaisedBevelBorder()));
		JPanel pnlPage = new JPanel(new GridLayout(lblLine.length, 1));
		pnlPage.setBackground(Color.BLACK);
		pnlPage.setBorder(BorderFactory.createLineBorder(
			Color.BLACK, 2));
		for(int i = 0; i < lblLine.length; i++) {
			lblLine[i] = new DmsLineLabel();
			pnlPage.add(lblLine[i]);
		}
		lblID.setFont(FONT);
		lblUser.setFont(FONT);
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(lblID);
		title.add(Box.createGlue());
		title.add(lblUser);
		location.add(lblLocation);
		lblLocation.setFont(FONT);
		location.add(Box.createGlue());

		Box box1 = Box.createHorizontalBox();
		box1.add(lblDeployed);
		lblDeployed.setFont(FONT);
		box1.add(Box.createGlue());
		box1.add(Box.createHorizontalStrut(8));
		box1.add(lblExpires);
		lblExpires.setFont(FONT);
		box1.add(Box.createHorizontalStrut(8));
		box1.add(Box.createGlue());
		box1.add(lblCamera);
		lblCamera.setFont(FONT);

		add(title);
		add(location);
		add(pnlPage);
		add(box1);
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
	protected void setDms(DMS proxy) {
		lblID.setText(proxy.getName());
		lblLocation.setText(GeoLocHelper.getDescription(
			proxy.getGeoLoc()));
		lblCamera.setText(getCameraName(proxy));
		// Note: getMessageCurrent will only return null after the
		//       DMS proxy has been destroyed.
		SignMessage message = proxy.getMessageCurrent();
		if(isDeployed(message)) {
			lblUser.setText(formatOwner(message.getOwner()));
			lblDeployed.setText(formatDeployed(message));
			lblExpires.setText(formatExpiration(message));
		} else {
			lblUser.setText("");
			lblDeployed.setText("");
			lblExpires.setText("");
		}
		String[] lines = SignMessageHelper.createLines(message);
		for(int i = 0; i < lblLine.length; i++) {
			if(i < lines.length)
				lblLine[i].setText(lines[i]);
			else
				lblLine[i].setText(" ");
		}
		// FIXME: set preferred size based on DMS
		setPreferredSize(new Dimension(186, 102));
	}
}
