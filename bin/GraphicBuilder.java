/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
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
	static private final String HOME_DIR = System.getProperty("user.home");
	static private final File FONT_PATH = new File(HOME_DIR,
		"highway_gothic");

	private final int width = 100;
	private final int height = 28;
	private final Font hwy_a;
	private final Font hwy_b;
	private final Font hwy_c;
	private final Font hwy_d;
	private final Font hwy_e;
	private final BufferedImage buffer = new BufferedImage(width, height,
		BufferedImage.TYPE_INT_RGB);
	private final Graphics2D g = buffer.createGraphics();

	private Font createFont(String fn) throws FontFormatException,
		IOException
	{
		return Font.createFont(Font.TRUETYPE_FONT,
			new File(FONT_PATH, fn));
	}

	private GraphicBuilder() throws FontFormatException, IOException {
		hwy_a = createFont("HWYGCOND.TTF");
		hwy_b = createFont("HWYGNRRW.TTF");
		hwy_c = createFont("HWYGOTH.TTF");
		hwy_d = createFont("HWYGWDE.TTF");
		hwy_e = createFont("HWYGEXPD.TTF");
	}

	private void render() {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, width, height);
		g.setColor(BG);
		renderPanel(0, 0, 100, 28);
		g.setColor(FG);
		g.setFont(hwy_c.deriveFont(14f));
		g.drawString("Lindau Ln OR", 12, 12);
		g.setFont(hwy_d.deriveFont(14f));
		g.drawString("Killebrew Dr", 14, 25);
	}

	private void renderPanel(int x, int y, int w, int h) {
		g.fillRect(x, y, w, h);
		g.setColor(FG);
//		g.draw(new RoundRectangle2D.Float(x, y, w - 1, h - 1, 4, 4));
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
