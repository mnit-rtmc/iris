/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.sonar.client.ProxyListener;

/**
 * Scale GUI representation of a DMS with pixel resolution.
 * Multipage messages are displayed sequentially using the
 * system properties for DMS message on-time and off-time
 * (blank time). On-time and off-time values are read from
 * the system policy.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSPanel extends JPanel {

	/** Default sign width (mm) */
	static protected final int SIGN_WIDTH = 9347;

	/** Default sign height (mm) */
	static protected final int SIGN_HEIGHT = 2591;

	/** Default pixel diameter (mm) */
	static protected final int PIXEL_DIAMETER = 70;

	/** Default height of line space (mm) */
	static protected final int LINE_SPACE = 305;

	/** Default width of the sign margin (mm) */
	static protected final int SIGN_MARGIN = 305;

	/** Default character height (pixels) */
	static protected final int CHAR_HEIGHT = 7;

	/** Default character width (pixels) */
	static protected final int CHAR_WIDTH = 0;

	/** Default line width (pixels) */
	static protected final int LINE_WIDTH = 125;

	/** Default number of lines */
	static protected final int LINE_COUNT = 3;

	/** Time counter for amount of time message has been displayed */
	static protected final int timerTickLengthMS = 100;

	/** Currently displayed sign */
	protected DMSProxy proxy;

	/** Sign width (mm) */
	protected int signWidth = SIGN_WIDTH;

	/** Sign height (mm) */
	protected int signHeight = SIGN_HEIGHT;

	/** Sign pixel width */
	protected int widthPixels = LINE_WIDTH;

	/** Sign pixel height */
	protected int heightPixels = CHAR_HEIGHT * LINE_COUNT;

	/** Width of horizontal border (mm) */
	protected int horizontalBorder;

	/** Height of vertical border (mm) */
	protected int verticalBorder;

	/** Pixel height of character */
	protected int characterHeight = CHAR_HEIGHT;

	/** Pixel width of character */
	protected int characterWidth = CHAR_WIDTH;

	/** Width of individual pixels (mm) */
	protected int horizontalPitch;

	/** Height of individual pixels (mm) */
	protected int verticalPitch;

	/** Number of lines on the sign */
	protected int lineCount;

	/** Spacing between lines (mm) */
	protected float lineGap;

	/** Optimal line height (pixels) */
	protected int lineHeight;

	/** Character height (mm) */
	protected int charHeight;

	/** Spacing between characters (mm) */
	protected float charGap;

	/** Current message displayed */
	protected SignMessage message = null;

	/** Transform from user (mm) to screen coordinates */
	protected AffineTransform transform = new AffineTransform();

	/** Buffer for screen display */
	protected BufferedImage buffer;

	/** Flag that determines if buffer needs repainting */
	protected boolean bufferDirty = false;

	/** 
	 * Display state. True to display actual page, false to display a blank
	 * page. For single page messages it is always false.
	 */
	protected boolean displayBlankPage = false;

	/** current page number to render */
	protected int pagenumber = 0;

	/** time counter for amount of time message has been displayed */
	protected int pageTimeCounterMS = 0;

	/** Multipage message on time, read from system attributes */
	protected int onTimeMS = 2000;

	/** Multipage message off time, read from system attributes */
	protected int offTimeMS = 0;

	/** System attribute type cache */
	protected final TypeCache<SystemAttribute> cache;

	/** Create a new DMS panel */
	public DMSPanel(TypeCache<SystemAttribute> c) {
		super(true);
		_setSign(null);
		cache = c;
		cache.addProxyListener(sa_listener);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				rescale();
				repaint();
			}
		});
		readSystemDMSPageTimes();
		Timer pt = new Timer(timerTickLengthMS, new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				pageTimerTick();
			} 
		});
		pt.start();
	}

	/** Calculate the height of the gap between lines (mm) */
	protected float calculateLineGap() {
		float excess = signHeight - 2 * verticalBorder -
			heightPixels * verticalPitch;
		int gaps = lineCount - 1;
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Calculate the width of the gap between characters (mm) */
	protected float calculateCharGap() {
		float excess = signWidth - 2 * horizontalBorder -
			widthPixels * verticalPitch;
		int gaps = 0;
		if(characterWidth > 0)
			gaps = widthPixels / characterWidth - 1;
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Validate the dimensions of the sign */
	protected void validateSignDimensions() {
		if(signWidth <= 0)
			signWidth = SIGN_WIDTH;
		if(signHeight <= 0)
			signHeight = SIGN_HEIGHT;
		if(horizontalBorder <= 0)
			horizontalBorder = SIGN_MARGIN;
		if(verticalBorder <= 0)
			verticalBorder = LINE_SPACE;
		if(characterHeight < 0)
			characterHeight = CHAR_HEIGHT;
		if(characterWidth < 0)
			characterWidth = CHAR_WIDTH;
		if(horizontalPitch <= 0)
			horizontalPitch = PIXEL_DIAMETER;
		if(verticalPitch <= 0)
			verticalPitch = PIXEL_DIAMETER;
		if(lineHeight <= 0)
			lineHeight = CHAR_HEIGHT;
		charGap = calculateCharGap();
		lineGap = calculateLineGap();
		charHeight = lineHeight * verticalPitch;
	}

	/** Set the DMS panel to the default size */
	protected void setToDefaultSize() {
		signWidth = SIGN_WIDTH;
		signHeight = SIGN_HEIGHT;
		widthPixels = LINE_WIDTH;
		heightPixels = CHAR_HEIGHT * LINE_COUNT;
		horizontalBorder = SIGN_MARGIN;
		verticalBorder = LINE_SPACE;
		characterHeight = CHAR_HEIGHT;
		characterWidth = CHAR_WIDTH;
		horizontalPitch = PIXEL_DIAMETER;
		verticalPitch = PIXEL_DIAMETER;
		lineCount = LINE_COUNT;
		lineHeight = CHAR_HEIGHT;
		validateSignDimensions();
		_setMessage(null);
	}

	/** Set the size of the DMS */
	protected void updateSignSize(DMSProxy p) {
		signWidth = p.getSignWidth();
		signHeight = p.getSignHeight();
		widthPixels = p.getSignWidthPixels();
		heightPixels = p.getSignHeightPixels();
		horizontalBorder = p.getHorizontalBorder();
		verticalBorder = p.getVerticalBorder();
		characterHeight = p.getCharacterHeightPixels();
		characterWidth = p.getCharacterWidthPixels();
		horizontalPitch = p.getHorizontalPitch();
		verticalPitch = p.getVerticalPitch();
		lineCount = p.getTextLines();
		lineHeight = p.getLineHeightPixels();
		validateSignDimensions();
		_setMessage(p.getMessage());
	}

	/** Set the sign to render */
	protected void _setSign(DMSProxy p) {
		proxy = p;
		if(p == null)
			setToDefaultSize();
		else
			updateSignSize(p);
	}

	/** Set the sign to render */
	public void setSign(DMSProxy p) {
		if(p != proxy) {
			_setSign(p);
			rescale();
			repaint();
		}
	}

	/** Set the message displayed on the DMS */
	protected void _setMessage(SignMessage m) {
		message = m;
		bufferDirty = true;
	}

	/** Set the message displayed on the DMS */
	public void setMessage(SignMessage m) {
		_setMessage(m);
		repaint();
	}

	/** Get the character offset (for character-matrix signs only) */
	protected float getCharacterOffset(int x) {
		if(characterWidth > 0)
			return charGap * (x / characterWidth);
		else
			return 0;
	}

	/** Get the x-distance to the given pixel */
	protected float getPixelX(int x) {
		return horizontalBorder + getCharacterOffset(x) +
			horizontalPitch * x;
	}

	/** Get the line offset (for line- or character-matrix signs) */
	protected float getLineOffset(int y) {
		if(lineHeight > 0)
			return lineGap * (y / lineHeight);
		else
			return 0;
	}

	/** Get the y-distance to the given pixel */
	protected float getPixelY(int y) {
		return verticalBorder + getLineOffset(y) + verticalPitch * y;
	}

	/** Paint the pixels of the sign */
	protected void paintPixels(Graphics2D g, final BitmapGraphic b) {
		Ellipse2D pixel = new Ellipse2D.Float();
		for(int y = 0; y < b.height; y++) {
			float yy = getPixelY(y);
			for(int x = 0; x < b.width; x++) {
				float xx = getPixelX(x);
				if(b.getPixel(x, y) > 0)
					g.setColor(Color.YELLOW);
				else
					g.setColor(Color.GRAY);
				pixel.setFrame(xx, yy, horizontalPitch,
					verticalPitch);
				g.fill(pixel);
			}
		}
	}

	/** 
	 * Page timer tick. Called periodically to change the sign contents
	 * for multipage signs.
	 */
	protected void pageTimerTick() {
		SignMessage m = message;	// Avoid NPE race
		if(m == null)
			return;
		int np = m.getNumPages();
		if(np <= 1) {
			displayBlankPage = false;
			pagenumber = 0;
		} else if(doTick(np)) {
			bufferDirty = true;
			repaint();
		}
	}

	/** Update the timer for one tick.
	 * @return True if panel needs repainting */
	protected boolean doTick(int num_pages) {
		pageTimeCounterMS += timerTickLengthMS;
		if(displayBlankPage) {
			if(pageTimeCounterMS >= offTimeMS) {
				displayBlankPage = false;
				pageTimeCounterMS = 0;
				return true;
			}
		} else {
			if(pageTimeCounterMS >= onTimeMS) {
				if(offTimeMS > 0)
					displayBlankPage = true;
				pageTimeCounterMS = 0;
				pagenumber++;
				if(pagenumber >= num_pages)
					pagenumber = 0;
				return true;
			}
		}
		return false;
	}

	/** Paint the DMS panel onto a graphics context */
	protected void doPaint(Graphics2D g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.transform(transform);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, signWidth, signHeight);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		SignMessage m = message;	// Avoid NPE race
		if(m != null) {
			BitmapGraphic b = displayBlankPage ?
				m.getBlankBitmap() : m.getBitmap(pagenumber);
			if(b != null)
				paintPixels(g, b);
		}
	}

	/** Update the screen buffer to reflect current sign state */
	protected void updateBuffer() {
		bufferDirty = false;
		if(buffer == null) {
			buffer = new BufferedImage(getWidth(), getHeight(),
				BufferedImage.TYPE_INT_RGB);
		}
		doPaint(buffer.createGraphics());
	}

	/** Paint this on the screen */
	public void paintComponent(Graphics g) {
		while(bufferDirty)
			updateBuffer();
		BufferedImage b = buffer;
		if(b != null)
			g.drawImage(b, 0, 0, this);
	}

	/** Rescale when the component is resized or the sign changes */
	protected void rescale() {
		buffer = null;
		bufferDirty = true;
		double h = getHeight();
		double w = getWidth();
		if(w > 0 && h > 0) {
			AffineTransform t = new AffineTransform();
			double sx = w / SIGN_WIDTH;
			double sy = h / SIGN_HEIGHT;
			t.scale(sx, sy);
			double tx = (SIGN_WIDTH - signWidth) / 2;
			double ty = (SIGN_HEIGHT - signHeight) / 2;
			t.translate(tx, ty);
			transform = t;
		}
	}

	/** Get the preferred size of the sign panel */
	public Dimension getPreferredSize() {
		return new Dimension(SIGN_WIDTH / 24, SIGN_HEIGHT / 24);
	}

	/** Get the minimum size of the sign panel */
	public Dimension getMinimumSize() {
		return new Dimension(SIGN_WIDTH / 36, SIGN_HEIGHT / 36);
	}

	/** 
	 * Read the system DMS page on and off times. These are the amount of
	 * time each page is displayed and blanked for multipage messages.
	 */
	protected void readSystemDMSPageTimes() {
		// note: the retrieve values are in seconds and converted to MS
		onTimeMS = Math.round(1000 *
			SystemAttributeHelper.getDmsPageOnSecs());
		offTimeMS = Math.round(1000 *
			SystemAttributeHelper.getDmsPageOffSecs());
	}

	/** Proxy listener for System Attribute proxies */
	protected final ProxyListener<SystemAttribute> sa_listener =
		new ProxyListener<SystemAttribute>()
	{
		public void proxyAdded(SystemAttribute p) { }
		public void enumerationComplete() { }
		public void proxyRemoved(SystemAttribute p) { }
		public void proxyChanged(SystemAttribute p, String a) {
			// FIXME: probably should only call on DMS_PAGE_ attrs
			readSystemDMSPageTimes();
		}
	};

	/** Dispose of the form */
	protected void dispose() {
		cache.removeProxyListener(sa_listener);
	}
}
