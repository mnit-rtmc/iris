/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import java.awt.Color;
import javax.swing.JLabel;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Modem;
import us.mn.state.dot.tms.ModemState;
import us.mn.state.dot.tms.client.Session;

/**
 * A tool panel that displays modem status.
 *
 * @author Douglas Lau
 */
public class ModemPanel extends ToolPanel {

	/** Is this panel IRIS enabled? */
	static public boolean getIEnabled() {
		return true;
	}

	/** Modem cache */
	private final TypeCache<Modem> modems;

	/** Listener for modem changes */
	private final ProxyListener<Modem> listener =
		new ProxyListener<Modem>()
	{
		public void proxyAdded(Modem m) {
			updateWidget(m);
		}
		public void enumerationComplete() { }
		public void proxyRemoved(Modem m) { }
		public void proxyChanged(Modem m, String a) {
			updateWidget(m);
		}
	};

	/** Label displayed on tool panel */
	protected final JLabel modem_lbl = new JLabel();

	/** Create a new modem panel */
	public ModemPanel(Session s) {
		modems = s.getSonarState().getConCache().getModems();
		add(modem_lbl);
		modems.addProxyListener(listener);
	}

	/** Dispose of the modem panel */
	public void dispose() {
		modems.removeProxyListener(listener);
	}

	/** Update the tool panel widget */
	private void updateWidget(Modem m) {
		ModemState ms = ModemState.fromOrdinal(m.getState());
		modem_lbl.setText(m.getName() + ": " + ms);
		setBackground(backgroundColor(ms));
		modem_lbl.setForeground(foregroundColor(ms));
	}

	/** Get background color for a modem state */
	static private Color backgroundColor(ModemState ms) {
		switch (ms) {
		case connecting:
		case online:
			return Color.YELLOW;
		case open_error:
		case connect_error:
			return Color.GRAY;
		default:
			return Color.BLUE;
		}
	}

	/** Get foreground color for a modem state */
	static private Color foregroundColor(ModemState ms) {
		switch (ms) {
		case connecting:
		case online:
			return Color.BLACK;
		default:
			return Color.WHITE;
		}
	}
}
