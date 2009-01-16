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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.sonar.Namespace;

/**
 * Scale GUI representation of a DMS with pixel resolution.
 * Multipage messages are displayed sequentially using the
 * system attributes for DMS message on-time and off-time
 * (blank time). On-time and off-time values are read from
 * the system attributes.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSPanel extends JPanel {

	/** Default face width (mm) */
	static protected final int FACE_WIDTH = 9347;

	/** Default face height (mm) */
	static protected final int FACE_HEIGHT = 2591;

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

	/** Currently displayed sign */
	protected DMS proxy;

	/** Face width (mm) */
	protected int faceWidth = FACE_WIDTH;

	/** Face height (mm) */
	protected int faceHeight = FACE_HEIGHT;

	/** Sign pixel width */
	protected int widthPixels = LINE_WIDTH;

	/** Sign pixel height */
	protected int heightPixels = CHAR_HEIGHT * LINE_COUNT;

	/** Width of horizontal border (mm) */
	protected int horizontalBorder;

	/** Height of vertical border (mm) */
	protected int verticalBorder;

	/** Pixel height of character */
	protected int charHeight = CHAR_HEIGHT;

	/** Pixel width of character */
	protected int charWidth = CHAR_WIDTH;

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

	/** Spacing between characters (mm) */
	protected float charGap;

	/** Current message displayed */
	protected SignMessage message = null;

	/** Bitmaps for each page */
	protected BitmapGraphic[] bitmaps = new BitmapGraphic[0];

	/** Transform from user (mm) to screen coordinates */
	protected AffineTransform transform = new AffineTransform();

	/** Buffer for screen display */
	protected BufferedImage buffer;

	/** Flag that determines if buffer needs repainting */
	protected boolean bufferDirty = false;

	/** Blanking state -- true during blank time between pages */
	protected boolean isBlanking = false;

	/** Current page number to render */
	protected int page = 0;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** Create a new DMS panel */
	public DMSPanel(Namespace ns) {
		super(true);
		namespace = ns;
		_setSign(null);
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent e) {
				rescale();
				repaint();
			}
		});
	}

	/** Calculate the height of the gap between lines (mm) */
	protected float calculateLineGap() {
		float excess = faceHeight - 2 * verticalBorder -
			heightPixels * verticalPitch;
		int gaps = lineCount - 1;
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Calculate the width of the gap between characters (mm) */
	protected float calculateCharGap() {
		float excess = faceWidth - 2 * horizontalBorder -
			widthPixels * verticalPitch;
		int gaps = 0;
		if(charWidth > 0)
			gaps = widthPixels / charWidth - 1;
		if(excess > 0 && gaps > 0)
			return excess / gaps;
		else
			return 0;
	}

	/** Validate the dimensions of the sign */
	protected void validateSignDimensions() {
		if(faceWidth <= 0)
			faceWidth = FACE_WIDTH;
		if(faceHeight <= 0)
			faceHeight = FACE_HEIGHT;
		if(horizontalBorder <= 0)
			horizontalBorder = SIGN_MARGIN;
		if(verticalBorder <= 0)
			verticalBorder = LINE_SPACE;
		if(charHeight < 0)
			charHeight = CHAR_HEIGHT;
		if(charWidth < 0)
			charWidth = CHAR_WIDTH;
		if(horizontalPitch <= 0)
			horizontalPitch = PIXEL_DIAMETER;
		if(verticalPitch <= 0)
			verticalPitch = PIXEL_DIAMETER;
		if(lineHeight <= 0)
			lineHeight = CHAR_HEIGHT;
		charGap = calculateCharGap();
		lineGap = calculateLineGap();
	}

	/** Set the DMS panel to the default size */
	protected void setToDefaultSize() {
		faceWidth = FACE_WIDTH;
		faceHeight = FACE_HEIGHT;
		widthPixels = LINE_WIDTH;
		heightPixels = CHAR_HEIGHT * LINE_COUNT;
		horizontalBorder = SIGN_MARGIN;
		verticalBorder = LINE_SPACE;
		charHeight = CHAR_HEIGHT;
		charWidth = CHAR_WIDTH;
		horizontalPitch = PIXEL_DIAMETER;
		verticalPitch = PIXEL_DIAMETER;
		lineCount = LINE_COUNT;
		lineHeight = CHAR_HEIGHT;
		validateSignDimensions();
		_setMessage(null);
	}

	/** Set the size of the DMS */
	protected void updateSignSize(DMS p) {
		faceWidth = p.getFaceWidth();
		faceHeight = p.getFaceHeight();
		widthPixels = p.getWidthPixels();
		heightPixels = p.getHeightPixels();
		horizontalBorder = p.getHorizontalBorder();
		verticalBorder = p.getVerticalBorder();
		charHeight = p.getCharHeightPixels();
		charWidth = p.getCharWidthPixels();
		horizontalPitch = p.getHorizontalPitch();
		verticalPitch = p.getVerticalPitch();
		PixelMapBuilder builder = new PixelMapBuilder(namespace,
			widthPixels, heightPixels, charWidth, charHeight);
		lineHeight = builder.getLineHeightPixels();
		lineCount = heightPixels / lineHeight;
		validateSignDimensions();
		_setMessage(p.getMessageCurrent());
	}

	/** Set the sign to render */
	protected void _setSign(DMS p) {
		proxy = p;
		if(p == null)
			setToDefaultSize();
		else
			updateSignSize(p);
	}

	/** Set the sign to render */
	public void setSign(DMS p) {
		if(p != proxy) {
			_setSign(p);
			rescale();
			repaint();
		}
	}

	/** Set the message displayed on the DMS */
	protected void _setMessage(SignMessage m) {
		message = m;
		PixelMapBuilder builder = new PixelMapBuilder(namespace,
			widthPixels, heightPixels, charWidth, charHeight);
		if(m != null) {
			MultiString multi = new MultiString(m.getMulti());
			multi.parse(builder);
			bitmaps = builder.getPixmaps();
		} else {
			bitmaps = new BitmapGraphic[] {
				createBlankPage()
			};
		}
		bufferDirty = true;
	}

	/** Create a blank bitmap graphic */
	protected BitmapGraphic createBlankPage() {
		return new BitmapGraphic(widthPixels, heightPixels);
	}

	/** Set the message displayed on the DMS */
	public void setMessage(SignMessage m) {
		_setMessage(m);
		repaint();
	}

	/** Get the character offset (for character-matrix signs only) */
	protected float getCharacterOffset(int x) {
		if(charWidth > 0)
			return charGap * (x / charWidth);
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

	/** Make the display blank (without advanding the page number) */
	public void makeBlank() {
		if(isMultipage()) {
			isBlanking = true;
			bufferDirty = true;
			repaint();
		}
	}

	/** Display the next page of the message */
	public void nextPage() {
		if(isMultipage()) {
			isBlanking = false;
			page++;
			if(page >= bitmaps.length)
				page = 0;
			bufferDirty = true;
			repaint();
		}
	}

	/** Check if the current message has multiple pages */
	protected boolean isMultipage() {
		return bitmaps.length > 1;
	}

	/** Paint the DMS panel onto a graphics context */
	protected void doPaint(Graphics2D g) {
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.transform(transform);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, faceWidth, faceHeight);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		BitmapGraphic b = getCurrentPage();
		paintPixels(g, b);
	}

	/** Get the current page */
	protected BitmapGraphic getCurrentPage() {
		int p = page;
		BitmapGraphic[] bmaps = bitmaps;
		if(isBlanking || p >= bmaps.length)
			return createBlankPage();
		else
			return bmaps[p];
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
			double sx = w / FACE_WIDTH;
			double sy = h / FACE_HEIGHT;
			t.scale(sx, sy);
			double tx = (FACE_WIDTH - faceWidth) / 2;
			double ty = (FACE_HEIGHT - faceHeight) / 2;
			t.translate(tx, ty);
			transform = t;
		}
	}

	/** Get the preferred size of the sign panel */
	public Dimension getPreferredSize() {
		return new Dimension(FACE_WIDTH / 24, FACE_HEIGHT / 24);
	}

	/** Get the minimum size of the sign panel */
	public Dimension getMinimumSize() {
		return new Dimension(FACE_WIDTH / 36, FACE_HEIGHT / 36);
	}
}
