/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.TexturePaint;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.tms.IncidentImpact;
import us.mn.state.dot.tms.LaneConfiguration;
import static us.mn.state.dot.tms.R_Node.MAX_SHIFT;

/**
 * Panel for incident impact.
 *
 * @author Douglas Lau
 */
public class ImpactPanel extends JPanel {

	/** Impact stroke line */
	static private final BasicStroke LINE_IMPACT = new BasicStroke(1,
		BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	/** Color for no impact */
	static private final Color COLOR_OPEN = new Color(64, 128, 64, 32);

	/** Color for blocked impact */
	static private final Color COLOR_BLOCKED = new Color(208, 64, 64);

	/** Image for partially-blocked impact */
	static private final BufferedImage IMAGE_CAUTION =
		new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
	static {
		IMAGE_CAUTION.setRGB(0, 0, 0xffffff22);
		IMAGE_CAUTION.setRGB(1, 1, 0xffffff22);
		IMAGE_CAUTION.setRGB(2, 2, 0xffffff22);
		IMAGE_CAUTION.setRGB(3, 3, 0xffffff22);
	}

	/** Paint for caution impact */
	static private final TexturePaint PAINT_CAUTION = new TexturePaint(
		IMAGE_CAUTION, new Rectangle2D.Float(0, 0, 4, 4));

	/** Get next incident impact */
	static private IncidentImpact nextImpact(IncidentImpact v) {
		switch(v) {
		case FREE_FLOWING:
			return IncidentImpact.BLOCKED;
		case BLOCKED:
			return IncidentImpact.PARTIALLY_BLOCKED;
		default:
			return IncidentImpact.FREE_FLOWING;
		}
	}

	/** Panel to display one lane impact */
	private class LaneImpactPanel extends JPanel {
		private final int lane;
		private LaneImpactPanel(int ln) {
			lane = ln;
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					IncidentImpact im = impact[lane];
					if(im != null)
						incrementImpact(im);
				}
			});
		}
		private void incrementImpact(IncidentImpact im) {
			impact[lane] = nextImpact(im);
			fireStateChanged();
		}
		public void paintComponent(Graphics g) {
			IncidentImpact im = impact[lane];
			if(im != null) {
				Graphics2D g2 = (Graphics2D)g.create(0, 0,
					pixels, pixels);
				drawImpact(g2, im);
			}
		}
		private void drawImpact(Graphics2D g, IncidentImpact im) {
			int p = pixels / 2;
			int o = p / 2;
			g.setStroke(LINE_IMPACT);
			switch(im) {
			case FREE_FLOWING:
				g.setColor(COLOR_OPEN);
				g.fillRect(o, o, p, p);
				break;
			case BLOCKED:
				g.setColor(COLOR_BLOCKED);
				g.fillRect(o, o, p, p);
				break;
			case PARTIALLY_BLOCKED:
				g.setPaint(PAINT_CAUTION);
				g.fillRect(o, o, p, p);
				g.setPaint(null);
				break;
			}
			g.setColor(Color.BLACK);
			g.drawRect(o - 1, o - 1, p, p);
			g.setColor(Color.WHITE);
			g.drawRect(o, o, p, p);
		}
	}

	/** The listeners of this model */
	private final LinkedList<ChangeListener> listeners =
		new LinkedList<ChangeListener>();

	/** Pixel size (height and width) of each lane */
	private final int pixels;

	/** Array of lane impact panels from left to right */
	private final LaneImpactPanel[] imp_pnl =
		new LaneImpactPanel[MAX_SHIFT + 1];

	/** Incident impact array */
	private final IncidentImpact[] impact =
		new IncidentImpact[MAX_SHIFT + 1];

	/** Lane configuration at incident */
	private LaneConfiguration config = new LaneConfiguration(0, 0);

	/** Set the lane configuration */
	public void setConfiguration(LaneConfiguration lc) {
		config = lc;
		for(int i = 0; i < impact.length; i++)
			impact[i] = null;
		repaint();
	}

	/** Set the impact */
	public void setImpact(String im) {
		IncidentImpact[] imp = IncidentImpact.fromString(im);
		for(int i = 0; i < impact.length; i++)
			impact[i] = impactShift(imp, i);
		repaint();
	}

	/** Get a shifted incident impact.
	 * @param imp Array of incident impact values.
	 * @param shift Lane shift.
	 * @return Incident impact at specified lane. */
	private IncidentImpact impactShift(IncidentImpact[] imp, int shift) {
		int ln = shift + 1 - config.leftShift;
		if(ln >= 0 && ln < imp.length)
			return imp[ln];
		else
			return null;
	}

	/** Get the impact */
	public String getImpact() {
		int lanes = config.getLanes() + 2;
		IncidentImpact[] imp = new IncidentImpact[lanes];
		for(int i = 0; i < imp.length; i++) {
			int s = config.leftShift + i - 1;
			if(s >= 0 && s < impact.length)
				imp[i] = impact[s];
		}
		return IncidentImpact.fromArray(imp);
	}

	/** Create a new impact panel.
	 * @param p Pixel size for each lane. */
	public ImpactPanel(int p) {
		setLayout(null);
		pixels = p;
		int w = getX(MAX_SHIFT + 1);
		setMinimumSize(new Dimension(w, pixels));
		setPreferredSize(new Dimension(w, pixels));
		for(int i = 0; i < imp_pnl.length; i++) {
			imp_pnl[i] = new LaneImpactPanel(i);
			add(imp_pnl[i]);
			imp_pnl[i].setBounds(getX(i), 3, pixels, pixels);
		}
		setOpaque(false);
	}

	/** Get the X pixel value of a shift.
	 * @param shift Lane shift.
	 * @return X pixel value at lane shift. */
	private int getX(int shift) {
		return 6 + shift * (pixels + 6);
	}

	/** Add a change listener to the model */
	public void addChangeListener(ChangeListener l) {
		listeners.add(l);
	}

	/** Remove a change listener from the model */
	public void removeChangeListener(ChangeListener l) {
		listeners.remove(l);
	}

	/** Fire a change event to all listeners */
	private void fireStateChanged() {
		repaint();
		ChangeEvent ce = new ChangeEvent(this);
		for(ChangeListener l: listeners)
			l.stateChanged(ce);
	}
}
