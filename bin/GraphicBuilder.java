/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  Minnesota Department of Transportation
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
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * A tool to build IRIS sign graphics.
 *
 * @author Douglas Lau
 */
public class GraphicBuilder {
	static private final Color BG = new Color(0, 40, 0);
	static private final Color FG = Color.WHITE;
	static private final int FONT_HEIGHT = 12;
	static private final int width = 100;
	static private final int height = FONT_HEIGHT * 2 + 4;

	static private Font createFont(int size) {
		return new Font("Overpass", Font.PLAIN, size);
	}

	private final BufferedImage buffer = new BufferedImage(width, height,
		BufferedImage.TYPE_INT_RGB);
	private final Graphics2D g = buffer.createGraphics();
	private final Font font = createFont(FONT_HEIGHT);

	private void render() {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setColor(BG);
		renderPanel(0, 0, width, height);
		System.out.println("Font: " + font.getFontName());
		g.setFont(font);
		g.setColor(FG);
		renderString("Lindau Ln OR", 0);
		renderString("Killebrew Dr", height / 2);
	}

	private void renderString(String s, float y) {
		GlyphVector gv = font.createGlyphVector(
			g.getFontRenderContext(), s);
		Rectangle2D r = gv.getVisualBounds();
		System.out.println("Text: " + s);
		System.out.println("Width: " + r.getWidth());
		System.out.println("Height: " + r.getHeight());
		float x = (width - (float) r.getWidth()) / 2;
		g.drawString(s, x, y + (float) r.getHeight() + 2);
	}

	private void renderPanel(int x, int y, int w, int h) {
		g.fillRect(x, y, w, h);
		g.setColor(FG);
	}

	private void write(String fn) throws IOException {
		ImageIO.write(buffer, "png", new File(fn));
	}

	static public void main(String[] args) {
		try {
			GraphicBuilder gb = new GraphicBuilder();
			gb.render();
			gb.write("file.png");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
