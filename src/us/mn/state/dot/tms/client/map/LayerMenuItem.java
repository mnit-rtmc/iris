/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;

/**
 * Menu item for controlling layer visibility.
 *
 * @author Douglas Lau
 */
public class LayerMenuItem extends JMenuItem {

	/** Visibility state enum */
	static public enum State {
		automatic(null), visible(true), invisible(false);
		private State(Boolean v) {
			visibility = v;
		}
		protected final Boolean visibility;
		static State fromBoolean(Boolean v) {
			for(State s: values()) {
				if(v == s.visibility)
					return s;
			}
			return automatic;
		}
	}

	/** Visibility state */
	protected State state = State.automatic;

	/** Create a new layer menu item */
	public LayerMenuItem(final LayerState ls) {
		super(ls.getLayer().getName(), new AutoIcon());
		setState(State.fromBoolean(ls.getVisible()));
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				setState(nextState());
				ls.setVisible(state.visibility);
			}
		});
	}

	/** Set the visibility state */
	public void setState(State st) {
		state = st;
		switch(st) {
		case visible:
			setIcon(new VisibleIcon());
			break;
		case invisible:
			setIcon(new InvisibleIcon());
			break;
		default:
			setIcon(new AutoIcon());
			break;
		}
	}

	/** Get the visibility state */
	public State getState() {
		return state;
	}

	/** Get the next visibility state */
	protected State nextState() {
		switch(getState()) {
		case visible:
			return State.invisible;
		case invisible:
			return State.automatic;
		default:
			return State.visible;
		}
	}

	/** Icon to show on menu item */
	static abstract class TristateIcon implements Icon {
		public int getIconHeight() {
			return 18;
		}
		public int getIconWidth() {
			return 18;
		}
	}

	/** Icon for automatic state */
	static class AutoIcon extends TristateIcon {
		protected final EtchedBorder border = new EtchedBorder();
		public void paintIcon(Component c, Graphics g, int x, int y) {
			border.paintBorder(c, g, x, y, 16, 16);
		}
	}

	/** Icon for invisible state */
	static class InvisibleIcon extends TristateIcon {
		protected final Color color = new Color(0.7f, 0, 0);
		protected final SoftBevelBorder border = new SoftBevelBorder(
			SoftBevelBorder.RAISED);
		public void paintIcon(Component c, Graphics g, int x, int y) {
			border.paintBorder(c, g, x, y, 16, 16);
			g.setColor(color);
			Rectangle r = border.getInteriorRectangle(c, x, y,
				16, 16);
			g.drawOval(r.x, r.y, r.width, r.height);
			g.drawLine(r.x + r.width - 2, r.y + 2, r.x + 2,
				r.y + r.height - 2);
		}
	}

	/** Icon for visible state */
	static class VisibleIcon extends TristateIcon {
		protected final Color color = new Color(0, 0.6f, 0);
		protected final SoftBevelBorder border = new SoftBevelBorder(
			SoftBevelBorder.LOWERED);
		public void paintIcon(Component c, Graphics g, int x, int y) {
			border.paintBorder(c, g, x, y, 16, 16);
			g.setColor(color);
			Rectangle r = border.getInteriorRectangle(c, x, y,
				16, 16);
			g.drawLine(r.x, r.y, r.x + r.width, r.y + r.height);
			g.drawLine(r.x + r.width, r.y, r.x, r.y + r.height);
		}
	}
}
