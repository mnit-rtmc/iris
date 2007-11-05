/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.rmi.RemoteException;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.utils.ExceptionDialog;

/**
 * A swing widget for displaying and editing VMS brightness tables
 *
 * @author Douglas Lau
 */
public class BrightnessTable extends JPanel {

	/** Red component of LED color */
	static protected final float LED_RED = 1.0f;

	/** Green component of LED color */
	static protected final float LED_GREEN = 0.95f;

	/** Blue component of LED color */
	static protected final float LED_BLUE = 0.2f;

	/** LED color at full brightness */
	static protected final Color BRIGHT_LED =
		new Color(LED_RED, LED_GREEN, LED_BLUE);

	/** Minimum number of pixels considered "near" */
	static protected final int NEAR = 6;

	/** Size of arrow heads for current photocell/light output */
	static protected final int A_SIZE = 6;

	/** Brightness levels which make up the table */
	protected Level[] levels;

	/** Sign which this table is associated with */
	protected final DMS dms;

	/** Administrator flag */
	protected final boolean admin;

	/** Total number of brightness levels in the table */
	protected int brightnessLevels;

	/** Current brightness level */
	protected int brightnessLevel;

	/** Maximum photocell level */
	protected int maxPhotocellLevel;

	/** Current photocell level */
	protected int photocellLevel;

	/** Current light output (0-65535) */
	protected int lightOutput;

	/** Manual brightness control */
	protected boolean manualBrightness;

	/** Current drag listener (if any) */
	protected MouseMotionAdapter dragging;

	/** Left side of component */
	protected int _left;

	/** Right side of component */
	protected int _right;

	/** Top side of component */
	protected int _top;

	/** Botom side of component */
	protected int _bottom;

	/** Width of component */
	protected int _width;

	/** Height of component */
	protected int _height;

	/** Light output gradient */
	protected GradientPaint _grad_output;

	/** Photocell gradient */
	protected GradientPaint _grad_photocell;

	/** Modified flag */
	protected boolean modified;

	/** Create a new brightness table */
	public BrightnessTable(DMS d, boolean a) {
		super();
		dms = d;
		admin = a;
		levels = new Level[0];
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				componentResize();
			}
		});
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if(manualBrightness || !admin)
					return;
				for(int i = 0; i < levels.length; i++) {
					if(levels[i].hit(e.getPoint()))
						break;
				}
			}
			public void mouseReleased(MouseEvent e) {
				removeMouseMotionListener(dragging);
				dragging = null;
			}
			public void mouseClicked(MouseEvent e) {
				if(!manualBrightness)
					return;
				for(int i = 0; i < levels.length; i++) {
					levels[i].click(e.getPoint());
				}
			}
		});
	}

	/** Set the manual brightness level */
	protected void setManualLevel(int level) {
		if(level == brightnessLevel)
			return;
		brightnessLevel = level;
		try { dms.setManualBrightness(level); }
		catch(RemoteException e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}

	/** Update the brightness table with new values */
	public void doUpdate() throws RemoteException {
		maxPhotocellLevel = dms.getMaxPhotocellLevel();
		brightnessLevels = dms.getBrightnessLevels();
		int[] b_table = dms.getBrightnessTable();
		Level[] lv = new Level[b_table.length / 3];
		float pl = maxPhotocellLevel;
		if(pl == 0) pl = 1;
		for(int i = 0; i < lv.length; i++) {
			lv[i] = new Level(i + 1);
			lv[i].output = b_table[i * 3] / 65535.0f;
			lv[i].level_down = b_table[i * 3 + 1] / pl;
			lv[i].level_up = b_table[i * 3 + 2] / pl;
		}
		levels = lv;
		calculateLevels();
		repaint();
	}

	/** Called whenever the brightness/level status changes */
	public void doStatus() throws RemoteException {
		brightnessLevel = dms.getBrightnessLevel();
		photocellLevel = dms.getPhotocellLevel();
		lightOutput = dms.getLightOutput();
		manualBrightness = dms.isManualBrightness();
		repaint();
	}

	/** Adjust the layout when the component is resized */
	protected void componentResize() {
		Insets insets = getInsets();
		int w = getWidth();
		int h = getHeight();
		_width = w - insets.left - insets.right;
		_height = h - insets.top - insets.bottom;
		_left = insets.left;
		_top = insets.top;
		_right = w - insets.bottom;
		_bottom = h - insets.bottom;
		_grad_output = new GradientPaint(_left, 0, Color.black,
			_right, 0, BRIGHT_LED);
		_grad_photocell = new GradientPaint(0, _top, Color.white,
			0, _bottom, Color.black);
		calculateLevels();
	}

	/** Calculate all the level bounding rectangles */
	protected void calculateLevels() {
		for(int i = 0; i < levels.length; i++) {
			float o1 = 0;
			float u1 = 0;
			float d1 = 0;
			if(i > 0) {
				o1 = levels[i - 1].output;
				u1 = levels[i - 1].level_up;
				d1 = levels[i - 1].level_down;
			}
			float o2 = 1;
			float u2 = 1;
			float d2 = 1;
			if(i < levels.length - 1) {
				o2 = levels[i + 1].output;
				u2 = levels[i + 1].level_up;
				d2 = levels[i + 1].level_down;
			}
			levels[i].calculate(o1, u1, d1, o2, u2, d2);
		}
	}

	/** Get the brightness table raw data */
	public int[] getTableData() {
		int[] table = new int[levels.length * 3];
		for(int i = 0; i < levels.length; i++) {
			int j = i * 3;
			table[j] = Math.round(levels[i].output * 65535);
			table[j + 1] = Math.round(levels[i].level_down *
				maxPhotocellLevel);
			table[j + 2] = Math.round(levels[i].level_up *
				maxPhotocellLevel);
		}
		return table;
	}

	/** Test if the brightness table is modified */
	public boolean isModified() {
		boolean m = modified;
		modified = false;
		return m;
	}

	/** Paint the brightness table component */
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(_width < 1) componentResize();
		if(manualBrightness) paintLevelDividers(g);
		paintLevels(g);
		if(maxPhotocellLevel > 0) paintPhotocellLevel(g);
		if(lightOutput > 0) paintLightOutput(g);
		if(admin && !manualBrightness) paintLevelKnobs(g);
	}

	/** Paint the dividers between the brightness levels */
	protected void paintLevelDividers(Graphics g) {
		g.setColor(Color.gray);
		for(int i = 1; i < levels.length; i++) {
			g.drawLine(levels[i]._x, _top, levels[i]._x, _bottom);
		}
	}

	/** Paint the brightness levels */
	protected void paintLevels(Graphics g) {
		for(int i = 0; i < levels.length; i++) {
			levels[i].paint(g);
		}
	}

	/** Paint the current photocell level */
	protected void paintPhotocellLevel(Graphics g) {
		int p = _bottom - Math.round(_height *
			photocellLevel / maxPhotocellLevel);
		g.setColor(Color.white);
		g.drawLine(_left, p, _right, p);
		int[] xl = { _left, _left + A_SIZE, _left + A_SIZE };
		int[] yp = { p, p - A_SIZE, p + A_SIZE };
		g.fillPolygon(xl, yp, 3);
		int[] xr = { _right, _right - A_SIZE, _right - A_SIZE };
		g.fillPolygon(xr, yp, 3);
	}

	/** Paint the current light output */
	protected void paintLightOutput(Graphics g) {
		int o = _left + Math.round((float)_width * lightOutput / 65535);
		g.setColor(BRIGHT_LED);
		g.drawLine(o, _top, o, _bottom);
		int[] xp = { o, o - A_SIZE, o + A_SIZE };
		int[] yt = { _top, _top + A_SIZE, _top + A_SIZE };
		g.fillPolygon(xp, yt, 3);
		int[] yb = { _bottom, _bottom - A_SIZE, _bottom - A_SIZE };
		g.fillPolygon(xp, yb, 3);
	}

	/** Paint the level adjustment knobs */
	protected void paintLevelKnobs(Graphics g) {
		for(int i = 0; i < levels.length; i++) {
			levels[i].paintKnobs(g);
		}
	}

	/** Get the minimum size of the brightness table component */
	public Dimension getMinimumSize() {
		return new Dimension(640, 240);
	}

	/** Get the preferred size of the brightness table component */
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}

	/** Individual brightness level */
	public class Level {

		/** Level number */
		protected final int level;

		/** Light output (range 0-1) */
		protected float output;

		/** Level-up threshold (range 0-1) */
		protected float level_up;

		/** Level-down threshold (range 0-1) */
		protected float level_down;

		/** X-coordinate of brightness output */
		protected int _o;

		/** X-coordinate of left side of level */
		protected int _x;

		/** Y-coordinate of top side of level */
		protected int _y;

		/** Width of level */
		protected int _w;

		/** Height of level */
		protected int _h;

		/** Color of level */
		protected Color _paint;

		/** Minimum output limit */
		protected int o_min;

		/** Maximum output limit */
		protected int o_max;

		/** Minimum level-up limit */
		protected int u_min;

		/** Maximum level-up limit */
		protected int u_max;

		/** Minimum level-down limit */
		protected int d_min;

		/** Maximum level-down limit */
		protected int d_max;

		/** Create a new brightness level */
		public Level(int l) {
			level = l;
		}

		/** Calculate the layout and limits for this level */
		protected void calculate(float o1, float u1, float d1,
			float o2, float u2, float d2)
		{
			_o = _left + Math.round(_width * output);
			int x1 = 0;
			if(o1 > 0) x1 = Math.round(_width *
				(o1 + output) / 2);
			int x2 = _width;
			if(o2 < 1) x2 = Math.round(_width *
				(output + o2) / 2);
			int y1 = Math.round(_height * level_up);
			int y2 = Math.round(_height * level_down);
			_x = _left + x1;
			_y = _bottom - y1;
			_w = x2 - x1;
			_h = y1 - y2;
			_paint = new Color(LED_RED * output,
				LED_GREEN * output, LED_BLUE * output);
			o_min = Math.round(_width * o1) + NEAR;
			o_max = Math.round(_width * o2) - NEAR;
			u_min = _bottom - Math.round(_height * u2);
			u_max = Math.round(_height * Math.max(u1, d2));
			u_max = _bottom - Math.max(u_max, y2 + NEAR);
			d_min = Math.round(_height * Math.min(u1, d2));
			d_min = _bottom - Math.min(d_min, y1 - NEAR);
			d_max = _bottom - Math.round(_height * d1);
		}

		/** Test if a mouse press hit one of the knobs */
		protected boolean hit(Point p) {
			if(p.x > _o - NEAR && p.x < _o + NEAR &&
				p.y > _y - NEAR && p.y < _y + NEAR)
			{
				dragging = new MouseMotionAdapter() {
					public void mouseDragged(MouseEvent e) {
						int x = e.getX();
						int y = e.getY();
						y = Math.min(y, u_max);
						y = Math.max(y, u_min);
						set_level(x, y, _y + _h - y);
					}
				};
				addMouseMotionListener(dragging);
				return true;
			}
			if(p.x > _x && p.x < _x + _w &&
				p.y > _y + _h - NEAR && p.y < _y + _h + NEAR)
			{
				dragging = new MouseMotionAdapter() {
					public void mouseDragged(MouseEvent e) {
						int x = e.getX();
						int y = e.getY();
						y = Math.min(y, d_max);
						y = Math.max(y, d_min);
						set_level(x, _y, y - _y);
					}
				};
				addMouseMotionListener(dragging);
				return true;
			}
			return false;
		}

		/** Set the boundaries of this level */
		protected void set_level(int x, int y, int h) {
			if(_width > 0) {
				x -= _left;
				x = Math.max(x, o_min);
				x = Math.min(x, o_max);
				output = (float)x / _width;
			}
			if(_height > 0) {
				float y1 = _bottom - y;
				level_up = y1 / _height;
				level_down = (y1 - h) / _height;
			}
			modified = true;
			calculateLevels();
			repaint();
		}

		/** Test if a mouse click was within this level */
		protected void click(Point p) {
			if(p.x > _x && p.x < _x + _w)
				setManualLevel(level);
		}

		/** Paint the level rectangle */
		protected void paint(Graphics g) {
			if(level == brightnessLevel) {
				g.setColor(Color.gray);
				g.fillRect(_x, _top, _w, _height + 1);
			}
			g.setColor(_paint);
			g.fillRect(_x, _y, _w, _h);
			if(level == brightnessLevel)
				g.setColor(Color.white);
			else g.setColor(Color.gray);
			g.drawRect(_x, _y, _w, _h);
			g.fillRect(_o - 1, _y, 3, _h);
		}

		/** Paint the level adjustment knobs */
		protected void paintKnobs(Graphics g) {
			g.setColor(Color.lightGray);
			g.fillOval(_o - 2, _y - 2, 6, 6);
			g.fillOval(_o - 2, _y + _h - 2, 6, 6);
			g.setColor(Color.white);
			g.drawOval(_o - 2, _y - 2, 6, 6);
			g.drawOval(_o - 2, _y + _h - 2, 6, 6);
			g.setColor(Color.gray);
			g.drawOval(_o - 3, _y - 3, 6, 6);
			g.drawOval(_o - 3, _y + _h - 3, 6, 6);
		}
	}

	/** Brightness table photocell/light output scale border */
	public class ScaleBorder extends EmptyBorder {

		/** Create a new brightness table scale border */
		public ScaleBorder(int s) {
			super(s, s, s, s);
		}

		/** Paint the border around the brightness table */
		public void paintBorder(Component c, Graphics g, int x, int y,
			int w, int h)
		{
			Graphics2D g2 = (Graphics2D)g;
			Insets insets = getBorderInsets();
			g2.setPaint(_grad_output);
			g2.fillRect(_left, y, _width, insets.top - 4);
			g2.fillRect(_left, _bottom + 4,
				_width, insets.bottom - 4);
			g2.setPaint(_grad_photocell);
			g2.fillRect(x, _top, insets.left - 4, _height + 1);
			g2.fillRect(_right + 4, _top,
				insets.right - 4, _height + 1);
		}
	}
}
