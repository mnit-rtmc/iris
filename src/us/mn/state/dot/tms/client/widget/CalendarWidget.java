/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JPanel;

/**
 * ZTable is a simple JTable extension which adds a setVisibleRowCount method
 *
 * @author Douglas Lau
 */
public class CalendarWidget extends JPanel {

	/** Calendar highlighter */
	static public interface Highlighter {
		boolean isHighlighted(Calendar cal);
	}

	/** Color to draw outlines of date boxes */
	static protected final Color OUTLINE = new Color(0, 0, 0, 32);

	/** Color to fill non-holiday boxes */
	static protected final Color COL_DAY = new Color(192, 224, 128);

	/** Color to fill holiday boxes */
	static protected final Color COL_HOLIDAY = new Color(224, 192, 128);

	/** Formatter for weekday labels */
	static protected final SimpleDateFormat WEEK_DAY =
		new SimpleDateFormat("EEE");

	/** Calendar for selected month */
	protected final Calendar month = Calendar.getInstance();

	/** Calendar highlighter */
	protected Highlighter highlighter = new Highlighter() {
		public boolean isHighlighted(Calendar cal) {
			return false;
		}
	};

	/** Set the calendar highlighter */
	public void setHighlighter(Highlighter h) {
		highlighter = h;
	}

	/** Create a new calendar widget */
	public CalendarWidget() {
		month.set(Calendar.DAY_OF_MONTH, 1);
		month.set(Calendar.HOUR_OF_DAY, 6);
	}

	/** Set the month to display on the calendar widget */
	public void setMonth(Calendar c) {
		month.setTimeInMillis(c.getTimeInMillis());
		month.set(Calendar.DAY_OF_MONTH, 1);
		month.set(Calendar.HOUR_OF_DAY, 6);
		repaint();
	}

	/** Get the preferred size of the calendar widget */
	public Dimension getPreferredSize() {
		Font f = getFont();
		int h = 2 * f.getSize();
		int w = 3 * f.getSize();
		return new Dimension(w * 7, h * 6);
	}

	/** Paint the calendar widget */
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		Dimension size = getSize();
		int hgap = size.width / 7;
		int vgap = size.height / 7;
		drawWeekdayLabels(g2, hgap, vgap);
		drawDayOfMonths(g2, hgap, vgap);
	}

	/** Draw the weekday labels */
	private void drawWeekdayLabels(Graphics2D g2, int hgap, int vgap) {
		Calendar wcal = Calendar.getInstance();
		wcal.set(Calendar.DAY_OF_WEEK, wcal.getFirstDayOfWeek());
		for(int box = 0; box < 7; box++) {
			int v = box % 7;
			int h = box / 7;
			int x = v * hgap;
			int y = h * vgap;
			g2.setColor(Color.GRAY);
			drawText(g2, WEEK_DAY.format(wcal.getTime()),
				x + hgap / 2, y + vgap / 2);
			wcal.add(Calendar.DATE, 1);
		}
	}

	/** Draw the day-of-month boxes and labels */
	private void drawDayOfMonths(Graphics2D g2, int hgap, int vgap) {
		Calendar tcal = Calendar.getInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(month.getTimeInMillis());
		int wday = month.get(Calendar.DAY_OF_WEEK) -
			month.getFirstDayOfWeek();
		while(cal.get(Calendar.MONTH) == month.get(Calendar.MONTH)) {
			int day = cal.get(Calendar.DAY_OF_MONTH);
			int box = day + wday + 6;
			int v = box % 7;
			int h = box / 7;
			int x = v * hgap;
			int y = h * vgap;
			g2.setColor(OUTLINE);
			g2.drawRect(x + 1, y + 1, hgap - 2, vgap - 2);
			tcal.setTimeInMillis(cal.getTimeInMillis());
			int half = hgap / 2;
			if(highlighter.isHighlighted(tcal)) {
				g2.setColor(COL_HOLIDAY);
				g2.fillRect(x + 1, y + 1, hgap - 2, vgap - 2);
				g2.setColor(Color.BLACK);
				g2.drawLine(x + 2, y + 2, x + hgap - 2,
					y + vgap - 2);
				g2.drawLine(x + hgap - 2, y + 2, x + 2,
					y + vgap - 2);
			} else {
				g2.setColor(COL_DAY);
				g2.fillRect(x + 1, y + 1, hgap - 2, vgap - 2);
			}
			g2.setColor(Color.BLACK);
			drawText(g2, String.valueOf(day), x + hgap / 2,
				y + vgap / 2);
			cal.add(Calendar.DATE, 1);
		}
	}

	/** Draw text centered at a given point */
	private void drawText(Graphics2D g2, String text, int x, int y) {
		Font font = g2.getFont();
		GlyphVector gv = font.createGlyphVector(
			g2.getFontRenderContext(), text);
		Rectangle2D rect = gv.getVisualBounds();
		int tx = (int)Math.round(rect.getWidth() / 2.0);
		int ty = (int)Math.round(rect.getHeight() / 2.0);
		g2.drawGlyphVector(gv, x - tx, y + ty);
	}
}
