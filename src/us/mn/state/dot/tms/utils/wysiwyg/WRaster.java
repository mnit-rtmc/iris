/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.utils.wysiwyg;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DMSType;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.utils.Base64;
import us.mn.state.dot.tms.utils.MultiConfig;

/**
 * WRaster - Base Graphic class used for
 * imaging WYSIWYG-rendered MULTI strings.
 *
 * (Parts of this class are structured similarly to the
 *  method-outline used for the earlier RasterGraphic,
 *  BitmapGraphic, and PixmapGraphic classes.)
 *
 * One of the 4 WRaster subclasses is obtained by
 * calling one of the WRaster.create methods.  Do
 * not create an instance of a WRaster subclass
 * directly.
 *
 * WRaster saves the pixels in a form (based on MULTI
 * tag values) that can be easily converted to a
 * basic multi-page BufferedImage, a WYSIWYG edit-
 * image, or an NTCIP ColorScheme-specific byte-array.
 *
 * For purposes of this class and derived sub-classes,
 * a color can be represented in several different ways:
 * 	tagval   = int-array containing 1 -OR- 3 values.
 *  pixel    = a single-int representation of a tagval
 *  DmsColor = frequently used Iris color class
 *
 * @author John L Stanley - SRF Consulting Group
 */
abstract public class WRaster {

	protected ColorScheme colorscheme;

	/** Width of raster graphic */
	protected final int width;

	/** Height of raster graphic */
	protected final int height;

	/** colorIndex array */
	protected int[] pixels;

	/** Default background pixel int */
	protected int defaultBgPixel;

	/** Default foreground pixel int */
	protected int defaultFgPixel;

	/** Default background color */
	protected DmsColor defaultBG;

	/** Default foreground color */
	protected DmsColor defaultFG;

	/** Maximum legal pixel value */
	protected int max_pixel;

	/** maximum integers in a tagval */
	protected int max_taglen;

	/** maximum for each int in an tagval */
	protected int max_tagitem;

	/** LED module pixel width.
	 *  If row or full matrix, this equals width. */
	protected int moduleW;

	/** LED module pixel height.
	 *  If full matrix, this equals height. */
	protected int moduleH;

	/** List of tokens included in this raster */
	protected List<WToken> tokens = new LinkedList<WToken>();

	// MultiConfig for creating
	private MultiConfig mcfg;

	//===========================================
	// constants

	/** Generic black colorIndex */
	public static final int BLACK = 0;

	/** Generic default background colorIndex */
	public static final int DEFAULT_BG = -1;

	/** Generic default foreground colorIndex */
	public static final int DEFAULT_FG = -2;

	/** Generic default render error color */
	public static final int ERROR_PIXEL = -3;

	/** Color used for rendering a ERROR_PIXEL */
	private static final DmsColor ERROR_COLOR =
			new DmsColor(255,0,255);  // = Fuschia

	//===========================================

	/** Create a blank WRaster from a MultiConfig */
	@SuppressWarnings("incomplete-switch")
	public static WRaster create(MultiConfig mcfg) {
		if (mcfg == null)
			throw new IndexOutOfBoundsException("Null MulticConfig");
		if (!mcfg.isUseable())
			throw new IndexOutOfBoundsException("Unuseable MultiConfig");
		switch (mcfg.colorScheme) {
			case MONOCHROME_1_BIT:
				return new WRasterMono1(mcfg);
			case MONOCHROME_8_BIT:
				return new WRasterMono8(mcfg);
			case COLOR_CLASSIC:
				return new WRasterClassic(mcfg);
			case COLOR_24_BIT:
				return new WRasterColor24(mcfg);
		}
		throw new IndexOutOfBoundsException("Unknown color scheme: " + mcfg.colorScheme.toString());
	}

	/** Create an empty WRaster clone */
	@SuppressWarnings("incomplete-switch")
	public static WRaster create(ColorScheme cs, int width, int height) {
		if (cs == null)
			throw new IndexOutOfBoundsException("Null ColorScheme");
		switch (cs) {
			case MONOCHROME_1_BIT:
				return new WRasterMono1(width, height);
			case MONOCHROME_8_BIT:
				return new WRasterMono8(width, height);
			case COLOR_CLASSIC:
				return new WRasterClassic(width, height);
			case COLOR_24_BIT:
				return new WRasterColor24(width, height);
		}
		throw new IndexOutOfBoundsException("Unknown color scheme: " + cs.toString());
	}

	/** Create an empty WRaster clone */
	@SuppressWarnings("incomplete-switch")
	public static WRaster create(WRaster old) {
		if (old == null)
			throw new IndexOutOfBoundsException("Null WRaster");
		MultiConfig mcfg = old.mcfg;
		if ((mcfg != null) && mcfg.isUseable())
			return WRaster.create(mcfg);
		switch (old.colorscheme) {
			case MONOCHROME_1_BIT:
				return new WRasterMono1(old.width, old.height);
			case MONOCHROME_8_BIT:
				return new WRasterMono8(old.width, old.height);
			case COLOR_CLASSIC:
				return new WRasterClassic(old.width, old.height);
			case COLOR_24_BIT:
				return new WRasterColor24(old.width, old.height);
		}
		throw new IndexOutOfBoundsException("Unknown color scheme: " + old.colorscheme.toString());
	}

	/** Get the raster graphic width */
	public int getWidth() {
		return width;
	}

	/** Get the raster graphic height */
	public int getHeight() {
		return height;
	}

	/** Get color scheme */
	public ColorScheme getColorScheme() {
		return colorscheme;
	}

	/** Protected WRaster constructor */
	protected WRaster(MultiConfig mcfg) {
		setDefaults();
		width          = mcfg.getPixelWidth();
		height         = mcfg.getPixelHeight();
		pixels         = new int[width*height];
		defaultBG      = mcfg.getDefaultBG();
		defaultFG      = mcfg.getDefaultFG();
		// This sets default for tagvalToBgPixel(...)
		defaultBgPixel = tagvalToPixel(mcfg.getDefaultBGTagVal(), 0);
		// This sets default for tagvalToFgPixel(...)
		defaultFgPixel = tagvalToPixel(mcfg.getDefaultFGTagVal(), 1);
		moduleW        = mcfg.getCharWidth();
		moduleH        = mcfg.getCharHeight();
		this.mcfg      = mcfg;
	}

	protected void fixModuleSize() {
		if (moduleW < 2)
			moduleW = width;
		if (moduleH < 2)
			moduleH = height;
	}

	/** Monochrome raster defaults.
	 * @param w width
	 * @param h height
	 */
	public WRaster(int w, int h) {
		setDefaults();
		width          = w;
		height         = h;
		pixels         = new int[width*height];
		defaultBG      = DmsColor.BLACK;
		defaultFG      = DmsColor.AMBER;
		defaultBgPixel = 0;
		defaultFgPixel = 1;
		moduleW        = w;
		moduleH        = h;
		this.mcfg      = null;
	}

	/** Test a pixel to see if it's valid.
	 * Returns true if valid, false if not.
	 **/
	public boolean isValidPixel(int pixel) {
		return ((pixel >= 0)
		     && (pixel <= max_pixel));
	}

	/** Assert that a pixel is valid.
	 * Throws IndexOutOfBoundsException if pixel
	 * is an invalid color */
	public void assertValidPixel(int pixel)
			throws IndexOutOfBoundsException {
		if (!isValidPixel(pixel))
			throw new IndexOutOfBoundsException("Bad colorIndex: "+pixel);
	}

	/** Get the graphic as a Base64 string */
	public String getEncodedPixels() {
		return Base64.encode(getPixelData());
	}

	/** Set the graphic from a Base64 string
	 * @throws IOException */
	public void setEncodedPixels(String str) throws IOException {
		setPixelData(Base64.decode(str));
	}

	/** Get the pixel at the specified location */
	protected int getPixel(int x, int y) {
		return pixels[pixelIndex(x, y)];
	}

	/** Set the pixel at the specified location */
	public void setPixel(int x, int y, int pixel) {
		switch (pixel) {
		case DEFAULT_BG:
			pixel = defaultBgPixel;
			break;
		case DEFAULT_FG:
			pixel = defaultFgPixel;
			break;
		case ERROR_PIXEL:
			// skip assertValidPixel check for an error pixel
			try {
				pixels[pixelIndex(x, y)] = pixel;
			}
			catch (IndexOutOfBoundsException ex) {
				; // ignore
			}
			return;
		}
		assertValidPixel(pixel);
		pixels[pixelIndex(x, y)] = pixel;
	}

	/** Get the DmsColor at the specified location */
	public DmsColor getColor(int x, int y) {
		int pixel = getPixel(x, y);
		if (pixel == ERROR_PIXEL)
			return ERROR_COLOR;
		return pixelToColor(pixel);
	}

	public boolean isLit(int pixel) {
		return pixel != BLACK;
	}

	/** Are all pixels in the raster the same color?
	 *  Also returns true if the raster is zero-sized. */
	public boolean isBlank() {
		if ((pixels == null) || (pixels.length == 0))
			return true;
		int p1 = pixels[0];
		for (int p2 : pixels)
			if (p1 != p2)
				return false;
		return true;
	}

	/** Get the count of lit pixels */
	public int getLitCount() {
		int lit = 0;
		for (int pixel : pixels) {
			if (isLit(pixel))
				lit++;
		}
		return lit;
	}

	/** Copy the common region of the specified raster */
	public void copy(WRaster b) {
		int x0 = Math.max(width - b.width, 0) / 2;
		int x1 = Math.max(b.width - width, 0) / 2;
		int y0 = Math.max(height - b.height, 0) / 2;
		int y1 = Math.max(b.height - height, 0) / 2;
		int w = Math.min(width, b.width);
		int h = Math.min(height, b.height);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int v = b.getPixel(x1 + x, y1 + y);
				setPixel(x0 + x, y0 + y, v);
			}
		}
	}

	/** Copy a WRaster into this WRaster.
	 * Black pixels in the graphic being copied are
	 * considered transparent.
	 * @param wg WRaster to copy.
	 * @param x0 X-position on raster (0-based).
	 * @param y0 Y-position on raster (0-based). */
	public void copy(WRaster wg, int x0, int y0) {
		int w = wg.getWidth();
		int h = wg.getHeight();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int c = wg.getPixel(x, y);
				if (isLit(c))
					setPixel(x0 + x, y0 + y, c);
			}
		}
	}

	/** Copy a WRaster into this WRaster.
	 * Black pixels in the raster being copied are
	 * considered transparent.
	 * Non-black pixels are placed in the target
	 * as the foreground pixel value fg.
	 * @param wg WRaster to copy.
	 * @param x0 X-position on raster (0-based).
	 * @param y0 Y-position on raster (0-based).
	 * @param fg Foreground pixel color. */
	public void copy(WRaster wg, int x0, int y0, int fg) {
		int w = wg.getWidth();
		int h = wg.getHeight();
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (isLit(wg.getPixel(x, y)))
					setPixel(x0 + x, y0 + y, fg);
			}
		}
	}

	/** Draw a solid (filled-in) box onto the WRaster.
	 * @param pix pixel color of the box.
	 * @param x0 X-position on raster (0-based).
	 * @param y0 Y-position on raster (0-based).
	 * @param w Width of box
	 * @param h Height of box */
	public void drawSolidBox(int pix, int x0, int y0, int w, int h) {
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
					setPixel(x0 + x, y0 + y, pix);
			}
		}
	}

	/** Update the raster by clearing pixels not set in another raster */
	public void union(WRaster rg) {
		if (width != rg.width)
			throw new IndexOutOfBoundsException("width mismatch");
		if (height != rg.height)
			throw new IndexOutOfBoundsException("height mismatch");
		int cnt = width * height;
		for (int i = 0; i < cnt; i++) {
			if (!isLit(rg.pixels[i]))
				pixels[i] = BLACK;
		}
	}

	/** Update the raster by clearing pixels set in another raster */
	public void difference(WRaster rg) {
		if (width != rg.width)
			throw new IndexOutOfBoundsException("width mismatch");
		if (height != rg.height)
			throw new IndexOutOfBoundsException("height mismatch");
		int cnt = width * height;
		for (int i = 0; i < cnt; i++) {
			if (isLit(rg.pixels[i]))
				pixels[i] = BLACK;
		}
	}

	//================================

	/** Get the pixel index for the specified location */
	protected int pixelIndex(int x, int y) {
		if (x < 0 || x >= width) {
			throw new IndexOutOfBoundsException("x=" + x +
				", width=" + width);
		}
		if (y < 0 || y >= height) {
			throw new IndexOutOfBoundsException("y=" + y +
				", height=" + height);
		}
		return (y * width) + x;
	}

	//===========================================
	// tagval helper methods

	/** Convert a tagval to a background pixel value
	 * If tagval is null, returns the default background color.
	 * If tagval is invalid, returns null. */
	public Integer tagvalToBgPixel(int[] tagval) {
		return tagvalToPixel(tagval, defaultBgPixel);
	}

	/** Convert a tagval to a foreground pixel value
	 * If tagval is null, returns the default foreground color.
	 * If tagval is invalid, returns null. */
	public Integer tagvalToFgPixel(int[] tagval) {
		return tagvalToPixel(tagval, defaultFgPixel);
	}

	/** Convert a tagval to a pixel value.
	 * If tagval is null, returns the designated default pixel value.
	 * If tagval is invalid, returns null. */
	Integer tagvalToPixel(int[] tagval, Integer defaultPixel) {
		if (tagval == null)
			return defaultPixel;
		int len = tagval.length;
		if (len == 0)
			return defaultPixel;
		if ((len == 2) || (len > max_taglen)) {
			//TODO:  Can this happen?  If so, how to flag the error?
			return defaultPixel;
		}
		return tagvalToPixel2(tagval);
	}

	//===========================================
	// methods to generate various image objects

//	/** Get raw rendered image.
//	 *
//	 * This should never be needed.  Use
//	 * getPreviewImage, getPreviewImageIcon,
//	 * or getWysiwygImage() instead.
//	 */
//	public BufferedImage getImage() {
//		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//		DmsColor dc;
//		int rgb;
//		for (int y = 0; (y < height); ++y) {
//			for (int x = 0; (x < width); ++x) {
//				dc = getColor(x, y);
//				rgb = dc.rgb() & 0x0ffffff;
//				bi.setRGB(x, y, rgb);
//			}
//		}
//		return bi;
//	}

	/** Generate preview-image with black border and row/column dividers. */
	public BufferedImage getPreviewImage() {
		int dividersX = 2;
		int dividersY = 3;
		int borders = 5;
		int width2 = width + (borders * 2);
		int height2 = height + (borders * 2);
		fixModuleSize();
		if (moduleW > 1)
			width2 += ((width / moduleW) - 1) * dividersX;
		if (moduleH > 1)
			height2 += ((height / moduleH) - 1) * dividersY;
		BufferedImage bi = new BufferedImage(width2, height2, BufferedImage.TYPE_INT_RGB);
		DmsColor dc;
		int rgb;

		// Fill entire buffer with dark gray
		//TODO: Rewrite to do this more efficiently
		dc = new DmsColor(50, 50, 50);
		int darkGray = dc.rgb();
		for (int y = 0; (y < height2); ++y) {
			for (int x = 0; (x < width2); ++x) {
				bi.setRGB(x, y, darkGray);
			}
		}

		// copy main area(s)
		int x, x2, y, y2;
		y2 = borders;
		for (y = 0; (y < height); ++y) {
			x2 = borders;
			for (x = 0; (x < width); ++x) {
				dc = getColor(x, y);
				rgb = dc.rgb();
				bi.setRGB(x2, y2, rgb);
				x2 += 1;
				if (((x + 1) % moduleW) == 0)
					x2 += dividersX;
			}
			y2 += 1;
			if (((y + 1) % moduleH) == 0)
				y2 += dividersY;
		}
		return bi;
	}

	/** Generate a preview ImageIcon for the GUI */
	public ImageIcon getPreviewImageIcon() {
		BufferedImage bi = getPreviewImage();
		return new ImageIcon(bi);
	}

	//===========================================
	// Used for unit-testing and error messages

	/** Convert a tagval (int array) to a string.
	 *  (Used for generating error messages.) */
	static public String toStr(int[] tagval) {
		if ((tagval == null) || (tagval.length == 0))
			return "";
		StringBuilder sb = new StringBuilder();
		int len = tagval.length;
		for (int i = 0; (i < len); ++i) {
			if (sb.length() > 0)
				sb.append(",");
			sb.append(tagval[i]);
		}
		String str = sb.toString();
		if ((len != 1) && (len != 3))
			throw new IndexOutOfBoundsException(
					"Bad tagval length: ["+str+"]");
		return sb.toString();
	}

	/** Convert a string to a tagval (int array).
	 *  If the string is a null or an empty
	 *  string, returns null.
	 *  (Used for unit-testing.) */
	public int[] toTag(String str) {
		if ((str == null) || str.isEmpty())
			return null;
		String[] parts = str.split(",");
		int x;
		int len = parts.length;
		if ((len != 1) && (len != 3))
			throw new IndexOutOfBoundsException(
					"Bad tagval length: \""+str+"\"");
		int[] tagval = new int[len];
		for (int i = 0; (i < len); ++i) {
			x = Integer.parseInt(parts[i].trim());
			if (x < 0 || (x > max_tagitem))
				throw new IndexOutOfBoundsException(
						"Bad tagval value: \""+str+"\"");
			tagval[i] = x;
		}
		return tagval;
	}

	/** Dump a BW text representation to a buffered writer */
	public void dumpBW(PrintWriter out) {
		int h = getHeight();
		int w = getWidth();
		int pixel;
		for (int y = 0; (y < h); ++y) {
			out.print(String.format("row%02d=",y));
			for (int x = 0; (x < w); ++x) {
				pixel = getPixel(x, y);
				if (isLit(pixel))
					out.print(" X");
				else
					out.print(" .");
			}
			out.println();
		}
	}

	static public int dmsToGray(DmsColor dc, int maxGray) {
//		double gray = (0.2989 * dc.red) + (0.5870 * dc.green) + (0.1140 * dc.blue);
		double gray = (0.21 * dc.red) + (0.72 * dc.green) + (0.07 * dc.blue);
		return (int) Math.round(gray * maxGray / 256);
	}

	/** Dump a grayscale text representation
	 *  of raster image to a buffered writer. */
	public void dumpGray(PrintWriter out) {
//		String grayStr = "$@B%8&WM#*oahkbdpqwmZO0QLCJUYXzcvunxrjft/\\|()1{}[]?-_+~<>i!lI;:,\"^`'. ";
		String grayStr = "@%#x+=:-. ";
		int h = getHeight();
		int w = getWidth();
		DmsColor dc;
		int grayMax = grayStr.length()-1;
		int gray;
		char ch;
		try {
			for (int y = 0; (y < h); ++y) {
				out.print(String.format("row%02d=",y));
				for (int x = 0; (x < w); ++x) {
					dc = getColor(x, y);
					gray = dmsToGray(dc, grayMax);
					if ((gray < 0) || (gray > grayStr.length()))
						out.print(String.format("(%d)", gray));
					else {
						ch = grayStr.charAt(gray);
//						out.print(" "+ch);
						out.print(""+ch+ch);
					}
				}
				out.println();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace(out);
		}
	}

	/** Save a BufferedImage to a .png file. */
	static public void dumpPng(BufferedImage bi, String filename) {
		File out = new File(filename);
		try {
			ImageIO.write(bi, "png", out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

//	//-------------------------------------------
//	// Token coordinate management
//
//	/** Add token to raster. (Coordinates of
//	 *  token are updated as part of raster
//	 *  copy operations.) */
//	public void bindToken(WToken tok) {
////		tok.clearTokCoordinates();
//		tokens.add(tok);
//	}

	//===========================================
	// Subclass (Color-scheme specific) methods

	/** Initialize all raster variables that have the
	 * same value for all constructors in that subclass. */
	abstract protected void setDefaults();

	/** Get the byte-array length of an equivalent
	 *  NTCIP 1203 DMS-graphic byte-array */
	abstract public int length();

	/** Load graphic from an NTCIP 1203
	 *  DMS-graphic byte-array */
	abstract public void setPixelData(byte[] p);

	/** Convert the graphic to an NTCIP 1203
	 *  DMS-graphic byte-array */
	abstract public byte[] getPixelData();

	/** Convert a pixel to a DmsColor */
	abstract protected DmsColor pixelToColor(int pixel);

	/** Convert a tagval to a pixel value.
	 * Returns null if tagval can't be converted for this raster. */
	abstract protected Integer tagvalToPixel2(int[] tagval);

	/** Convert a classic-color int to a pixel int.
	 * Returns null if specific classic-color value
	 * can't be converted for this raster. */
	abstract public Integer classicColorToPixel(int cco);

	//===========================================
	// Methods to provide WISIWIG-editor sign-images.

	/** Horizontal coordinate mapping array */
	private int horizCoords[];

	/** Vertical coordinate mapping array */
	private int vertCoords[];

	/** width of sign-image */
	private int wysiwygImgWidth;

	/** height of sign-image */
	private int wysiwygImgHeight;

	/** Special mapping-array values */
	static final int PIXEL_SEPARATOR  = -1; // gap between pixels
	static final int MODULE_SEPARATOR = -2; // gap between modules
	static final int SIGN_BORDER      = -3; // sign border
	static final int OFF_SIGN         = -4; // off edge of sign

	/** class used to generate coordinate arrays */
	class IntArrayGen {
		private int[] ia;
		private int maxLen;
		public int offset = 0;
		private boolean bError = false;
		private float excess = 0;

		IntArrayGen(int maxLen) {
			ia = new int[maxLen];
			this.maxLen = maxLen;
		}
		void add(int val) {
			if (offset < maxLen)
				ia[offset++] = val;
			else
				bError = true;
		}
		void add(int val, int len) {
			for (int i = 0; (i < len); ++i)
				add(val);
		}
		void add(int val, float flen) {
			flen += excess;
			int len = (int)flen;
			excess = flen - len;
			add(val, len);
		}
		boolean error() {
			return bError;
		}
		int[] getArray() {
			while (offset < maxLen)
				add(OFF_SIGN);
			return ia;
		}
	}

	/** Generate horizontal or vertical coordinate array.
	 * @param arrayLen        Total length of array
	 * @param pixelsPerBorder Length of border
	 * @param modulesPerFace  Number of modules
	 * @param pixPerModuleSepX Length of module separator
	 * @param ledsPerModule   Number of LEDs per module
	 * @param pixelsPerLed    Length of single LED
	 * @return The coordinate array.
	 */
	private int[] genCoordArray(
			int arrayLen,
			int pixelsPerBorder,
			int modulesPerFace,
			float pixPerModuleSepX,
			int ledsPerModule,
			float pixelsPerLed) {
		IntArrayGen gen = new IntArrayGen(arrayLen);
		gen.add(SIGN_BORDER, pixelsPerBorder);
		int pixNum = 0;
		for (int mod = 0; (mod < modulesPerFace); ++mod) {
			if (mod > 0)
				gen.add(MODULE_SEPARATOR, pixPerModuleSepX);
			for (int modPix = 0; (modPix < ledsPerModule); ++modPix) {
				if (pixelsPerLed < 1.9) {
					if (modPix == 0)
						gen.add(SIGN_BORDER);
					gen.add(pixNum++, pixelsPerLed+1);
				}
				else {
					if (modPix == 0)
						gen.add(PIXEL_SEPARATOR);
					gen.add(pixNum++, pixelsPerLed);
					gen.add(PIXEL_SEPARATOR);
				}
			}
		}
		gen.add(SIGN_BORDER, pixelsPerBorder);
		return gen.getArray();
	}

	/** Set width/height for WISIWIG sign-image.
	 *  (Generates horizontal and vertical
	 *   coordinate-mapping arrays.)
	 *
	 *  In the following, "pix" variables contain the
	 *  number of bitmap pixels used to draw something
	 *  (width or height) in the WYSIWIG sign-image.
	 *
	 * @param pixWidth  desired sign-image width (in pixels)
	 * @param pixHeight desired sign-image height (in pixels)
	 * @throws InvalidMsgException
	 */
	public void setWysiwygImageSize(int pixWidth, int pixHeight)
			throws InvalidMsgException {
		if (mcfg == null)
			throw new InvalidMsgException("Missing MultiConfig");
		if ((width <= 0) || (height <= 0))
			throw new InvalidMsgException("Invalid MULTI-image size");
		if ((pixWidth <= 0) || (pixHeight <= 0))
			throw new InvalidMsgException("Invalid WYSIWYG-image size");
		wysiwygImgWidth  = pixWidth;
		wysiwygImgHeight = pixHeight;
		DMSType dmsType = mcfg.getDmsType();
		int ledsPerFaceX = width;
		int ledsPerFaceY = height;
		int ledsPerModuleX;
		int ledsPerModuleY;
		int modulesPerFaceX;
		int modulesPerFaceY;
		int sepCountX; // total pix reserved for separator lines
		int sepCountY; // total pix reserved for separator lines
		switch (dmsType) {
			case VMS_CHAR:
				ledsPerModuleX = mcfg.getCharWidth();
				if (ledsPerModuleX == 0)
					ledsPerModuleX = ledsPerFaceX;
				ledsPerModuleY = mcfg.getCharHeight();
				if (ledsPerModuleY == 0)
					ledsPerModuleY = ledsPerFaceY;
				modulesPerFaceX = ledsPerFaceX / ledsPerModuleX;
				modulesPerFaceY = ledsPerFaceY / ledsPerModuleY;
				sepCountX = ledsPerFaceX + modulesPerFaceX;
				sepCountY = ledsPerFaceY + modulesPerFaceY;
				break;
			case VMS_LINE:
				ledsPerModuleX = ledsPerFaceX;
				ledsPerModuleY = mcfg.getCharHeight();
				if (ledsPerModuleY == 0)
					ledsPerModuleY = ledsPerFaceY;
				modulesPerFaceX = 1;
				modulesPerFaceY = ledsPerFaceY / ledsPerModuleY;
				sepCountX = ledsPerFaceX + 1;
				sepCountY = ledsPerFaceY + modulesPerFaceY;
				break;
			case VMS_FULL:
				ledsPerModuleX = ledsPerFaceX;
				ledsPerModuleY = ledsPerFaceY;
				modulesPerFaceX = 1;
				modulesPerFaceY = 1;
				sepCountX = ledsPerFaceX + 1;
				sepCountY = ledsPerFaceY + 1;
				break;
			default:
				throw new InvalidMsgException(
					"Can't use WYSIWYG editor on "
					+ mcfg.getDmsType()	+ " signs");
		}
		// a "space" is one led-width or led-height
		float spacesPerFaceX = (float) (width  + ((modulesPerFaceX - 1) * 1.5));
		float spacesPerFaceY = (float) (height + ((modulesPerFaceY - 1) * 4.0));

		int pixBorderHoriz = 20;
		int pixBorderVert  = 20;
		// calc number of pix not used by border and led-separators
		int pixPerFaceX = pixWidth  - (sepCountX + (pixBorderHoriz*2));
		int pixPerFaceY = pixHeight - (sepCountY + (pixBorderVert*2));
		// calc pix size of each LED (not counting separators)
		float pixPerLedX = (float)pixPerFaceX / spacesPerFaceX;
		float pixPerLedY = (float)pixPerFaceY / spacesPerFaceY;
		float pixPerLed = Math.min(pixPerLedX, pixPerLedY);
		float pixPerModuleSepX = (float) (pixPerLed * 1.5);
		float pixPerModuleSepY = (float) (pixPerLed * 4.0);

		horizCoords = genCoordArray(
				pixWidth,
				pixBorderHoriz, //4, //int borderLen,
				modulesPerFaceX,
				pixPerModuleSepX,
				ledsPerModuleX,
				pixPerLed
				);
		vertCoords = genCoordArray(
				pixHeight,
				pixBorderVert, //4, //int borderLen,
				modulesPerFaceY,
				pixPerModuleSepY,
				ledsPerModuleY,
				pixPerLed
				);
	}

	/** Return whether or not the WYSIWYG image size has been set. */
	public boolean isWysiwygInitialized() {
		return horizCoords != null && vertCoords != null;
	}

	static final int RGB_BLACK     = Color.BLACK.getRGB();
	static final int RGB_DARKGRAY  = Color.DARK_GRAY.getRGB();
	static final int RGB_GRAY      = Color.GRAY.getRGB();
	static final int RGB_LIGHTGRAY = Color.LIGHT_GRAY.getRGB();

	/** Generate WYSIWYG-editor image.
	 * (Uses size set by setWysiwygImageSize(...)) */
	public BufferedImage getWysiwygImage() {
		BufferedImage img = new BufferedImage(
				wysiwygImgWidth,
				wysiwygImgHeight,
				BufferedImage.TYPE_INT_RGB);
		int rgb;
		int x2, y2, min2;
		for (int x = 0; (x < wysiwygImgWidth); ++x) {
			x2 = horizCoords[x];
			for (int y = 0; (y < wysiwygImgHeight); ++y) {
				y2 = vertCoords[y];
				min2 = Math.min(x2, y2);
				switch (min2) {
					case PIXEL_SEPARATOR: // gap between pixels
						rgb = RGB_DARKGRAY;
						break;
					case MODULE_SEPARATOR: // gap between modules
						rgb = RGB_BLACK;
						break;
					case SIGN_BORDER: // sign border
						rgb = RGB_BLACK;
						break;
					case OFF_SIGN: // off edge of sign
						rgb = RGB_LIGHTGRAY;
						break;
					default:
						rgb = getColor(x2, y2).rgb() & 0x0ffffff;
				}
				img.setRGB(x, y, rgb);
			}
		}
		return img;
	}

	//===========================================
	// Convert WYSIWYG image coordinates
	// to sign-tag coordinates.  (Used
	// for WYSIWYG mouse operations.)

	private int findClosestSignCoord(int[] coords, int maxCoord, int wc)
			throws IndexOutOfBoundsException {
		if (coords == null)
			throw new IndexOutOfBoundsException("Uninitialized WYSIWIG coordinates");
		if (wc < 0)
			return 1;
		int len = coords.length;
		if (wc >= len)
			return maxCoord;
		int sc = coords[wc];
		if (sc >= 0)
			return sc + 1;
		int d1 = wc - 1;
		int sc1 = -1;
		int d2 = wc + 1;
		int sc2 = -1;
		// search down
		while (d1 >= 0) {
			sc1 = coords[d1];
			if (sc1 >= 0)
				break;
			--d1;
		}
		// search up
		while (d2 < len) {
			sc2 = coords[d2];
			if (sc2 >= 0)
				break;
			++d2;
		}
		// find closest LED coordinate
		if (sc1 < 0) {
			if (sc2 < 0)
				throw new IndexOutOfBoundsException("Bad sign-coordinate array");
			return sc2 + 1;
		}
		if (sc2 < 0)
			return sc1 + 1;
		return (((wc - d1) <= (d2 - wc)) ? sc1 : sc2) + 1;
	}

	/** Convert sign-tag coordinate to WYSIWYG coordinates.
	 *  Returns the first or last WYSIWYG coordinates assigned to the given
	 *  sign-tag coordinate (returns the first coordinate if first is true,
	 *  and the last coordinate otherwise).
	 *  If sep is true, the coordinates of the LED separators around the sign
	 *  coordinate (LED) are returned instead of the LEDs themselves.
	 *  If the coordinate is invalid, -1 is returned.
	 */
	private int findWysiwygCoords(int[] coords, int sc, boolean first, boolean sep)
			throws IndexOutOfBoundsException {
		if (coords == null)
			throw new IndexOutOfBoundsException("Uninitialized WYSIWIG coordinates");
		boolean found = false;
		--sc;
		for (int i = 0; i < coords.length; ++i) {
			if (!found) {
				if (coords[i] == sc) {
					if (first) {
						if (sep)
							return i-1;
						else
							return i;
					}
					found = true;
				}
			} else {
				if (coords[i] != sc) {
					if (sep)
						return i;
					else
						return i-1;
				}
			}
		}
		return -1;
	}

	/** Convert horizontal WYSIWIG coordinate
	 *  to a sign-tag coordinate.
	 *  If the x coordinate points to a non-LED
	 *  part of the image, this returns the
	 *  closest LED coordinate.
	 * @param x  0-based horizontal image coordinate
	 * @return   1-based horizontal sign-tag coordinate
	 * @throws IndexOutOfBoundsException if the setWysiwygImageSize method has not been called
	 */
	public int cvtWysiwygToSignX(int x)
			throws IndexOutOfBoundsException {
		return findClosestSignCoord(horizCoords, width, x);
	}

	/** Convert horizontal sign-tag coordinate
	 *  to WYSIWYG coordinates.
	 *  Returns either the first or last WYSIWYG X coordinate
	 *  assigned to the given sign-tag coordinate. If the
	 *  coordinate is invalid, -1 is returned.
	 * @param x  1-based horizontal sign-tag coordinate
	 * @param first if true, return first matching coordinate, otherwise last
	 * @param sep return coordinate of LED separators around sign instead
	 *        of LEDs themselves
	 * @return   The WYSIWYG coordinate.
	 * @throws IndexOutOfBoundsException if the setWysiwygImageSize method has not been called
	 */
	public int cvtSignToWysiwygX(int x, boolean first, boolean sep)
			throws IndexOutOfBoundsException {
		return findWysiwygCoords(horizCoords, x, first, sep);
	}

	/** Convert vertical WYSIWIG coordinate
	 *  to a sign-tag coordinate.
	 *  If the y coordinate points to a non-LED
	 *  part of the image, this returns the
	 *  closest LED coordinate.
	 * @param y  0-based vertical image coordinate
	 * @return   1-based vertical sign-tag coordinate
	 * @throws IndexOutOfBoundsException if the setWysiwygImageSize method has not been called
	 */
	public int cvtWysiwygToSignY(int y)
			throws IndexOutOfBoundsException {
		return findClosestSignCoord(vertCoords, height, y);
	}

	/** Convert vertical sign-tag coordinate
	 *  to WYSIWYG coordinates - convenience method.
	 *  Returns either the first or last WYSIWYG Y coordinate
	 *  assigned to the given sign-tag coordinate. If the
	 *  coordinate is invalid, -1 is returned.
	 * @param y  1-based horizontal sign-tag coordinate
	 * @param first if true, return first matching coordinate, otherwise last
	 * @param sep return coordinate of LED separators around sign instead
	 *        of LEDs themselves
	 * @return   The WYSIWYG coordinate.
	 * @throws IndexOutOfBoundsException if the setWysiwygImageSize method has not been called
	 */
	public int cvtSignToWysiwygY(int y, boolean first, boolean sep)
			throws IndexOutOfBoundsException {
		return findWysiwygCoords(vertCoords, y, first, sep);
	}
}
