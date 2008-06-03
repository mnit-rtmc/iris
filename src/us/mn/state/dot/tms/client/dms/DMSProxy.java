/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
import java.awt.GridLayout;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import us.mn.state.dot.tms.DmsMessage;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.LogDeviceAction;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;

import us.mn.state.dot.tms.utils.I18NMessages;


/**
 * The DMSProxy class provides a proxy representation of a DMS object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class DMSProxy extends TrafficDeviceProxy {

	/** Proxy type name */
	static public final String PROXY_TYPE = "DMS";

	/** Get the proxy type name */
	public String getProxyType() {
		return (I18NMessages.get("MesgSignLabel"));
	}

	/** The DMS that this proxy represents */
	public final DMS dms;

	/** ID of the validation camera */
	protected String camera_id = " ";

	/** Get the ID of the validation camera */
	public String getCameraId() {
		return camera_id;
	}

	/** Number of text lines */
	protected int text_lines = 3;

	/** Get the number of text lines */
	public int getTextLines() {
		return text_lines;
	}

	/** Optimal line height */
	protected int line_height = 7;

	/** Get the optimal line height (pixels) */
	public int getLineHeightPixels() {
		return line_height;
	}

	/** Current message */
	protected SignMessage message = null;

	/** Get the message currently displayed on the sign */
	public SignMessage getMessage() {
		return message;
	}

	/** Text lines of current message */
	protected String[] lines = new String[0];

	/** Get the text lines of the current message */
	public String[] getLines() {
		return lines;
	}

	/** Join two strings with a space and then trim */
	static protected String trimJoin(String a, String b) {
		String j = a + " " + b;
		return j.trim();
	}

	/** Create an array of lines from the given message */
	protected String[] createLines(SignMessage m) {
		if(m == null)
			return new String[text_lines];
		final ArrayList<String> al = new ArrayList<String>(text_lines);
		MultiString multi = m.getMulti();
		multi.parse(new MultiString.Callback() {
			public void addText(int p, int l,
				MultiString.JustificationLine j, String t)
			{
				int m_lines = Math.max(text_lines, l + 1);
				while(al.size() < (p + 1) * m_lines)
					al.add("");
				int i = p * m_lines + l;
				String v = al.get(i);
				al.set(i, trimJoin(v, t));
			}
		});
		return al.toArray(new String[0]);
	}

	/**
	 * Create a new DMSProxy representing the passed DMS object.
	 * @param sign, the DMS that will be proxied.
	 */
	public DMSProxy(DMS sign) throws RemoteException {
		super(sign);
		dms = sign;
		updateUpdateInfo();
		updateStatusInfo();
	}

	/** Does the sign have a message on it? */
	protected boolean isDeployed() {
		return message != null && !message.isBlank();
	}

	/** Update the DMS update information */
	public void updateUpdateInfo() throws RemoteException {
		super.updateUpdateInfo();
		TrafficDevice camera = dms.getCamera();
		if(camera == null)
			camera_id = " ";
		else
			camera_id = camera.getId();
		text_lines = dms.getTextLines();
		line_height = dms.getLineHeightPixels();
		messages = dms.getMessages();

		signWidth = dms.getSignWidth();
		signHeight = dms.getSignHeight();
		widthPixels = dms.getSignWidthPixels();
		heightPixels = dms.getSignHeightPixels();
		horizontalBorder = dms.getHorizontalBorder();
		verticalBorder = dms.getVerticalBorder();
		characterHeight = dms.getCharacterHeightPixels();
		characterWidth = dms.getCharacterWidthPixels();
		horizontalPitch = dms.getHorizontalPitch();
		verticalPitch = dms.getVerticalPitch();
	}

	protected DmsMessage[] messages;

	public DmsMessage[] getMessages() {
		return messages;
	}

	protected int signWidth;

	public int getSignWidth() {
		return signWidth;
	}

	protected int signHeight;

	public int getSignHeight() {
		return signHeight;
	}

	protected int widthPixels;

	public int getSignWidthPixels() {
		return widthPixels;
	}

	protected int heightPixels;

	public int getSignHeightPixels() {
		return heightPixels;
	}

	protected int horizontalBorder;

	public int getHorizontalBorder() {
		return horizontalBorder;
	}

	protected int verticalBorder;

	public int getVerticalBorder() {
		return verticalBorder;
	}

	protected int characterHeight;

	public int getCharacterHeightPixels() {
		return characterHeight;
	}

	protected int characterWidth;

	public int getCharacterWidthPixels() {
		return characterWidth;
	}

	protected int horizontalPitch;

	public int getHorizontalPitch() {
		return horizontalPitch;
	}

	protected int verticalPitch;

	public int getVerticalPitch() {
		return verticalPitch;
	}

	protected int light_output;

	public int getLightOutput() {
		return light_output;
	}

	/** Update the sign status information */
	public void updateStatusInfo() throws RemoteException {
		super.updateStatusInfo();
		message = dms.getMessage();
		lines = createLines(message);
		light_output = dms.getLightOutput();
	}

	/** Show the properties form for the DMS */
	public void showPropertiesForm(TmsConnection tc) throws RemoteException
	{
		tc.getDesktop().show(new DMSProperties(tc, id));
	}

	/** Make a message label for the popup menu */
	static protected JLabel makeMessageLabel(String t) {
		JLabel l = new JLabel(t);
		l.setHorizontalAlignment(JLabel.CENTER);
		l.setForeground(Color.YELLOW);
		return l;
	}

	/** Get a popup for this DMS */
	public JPopupMenu getPopup(TmsConnection c) {
		JPopupMenu popup = makePopup(toString());
		String[] lines = getLines();
		JPanel msg = new JPanel(new GridLayout(lines.length, 1));
		msg.setBackground(Color.BLACK);
		for(int i = 0; i < lines.length; i++) {
			JLabel line = makeMessageLabel(lines[i]);
			msg.add(line);
		}
		popup.add(msg);
		popup.addSeparator();
		popup.add(new JMenuItem(new ClearDmsAction(this, c)));
		popup.add(new JMenuItem(new LogDeviceAction(this, c)));
		popup.addSeparator();
		popup.add(new JMenuItem(new PropertiesAction(this, c)));
		return popup;
	}
}
