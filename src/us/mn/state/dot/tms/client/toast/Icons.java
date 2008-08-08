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
package us.mn.state.dot.tms.client.toast;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.FilteredImageSource;
import java.awt.image.RGBImageFilter;
import java.net.URL;
import java.util.WeakHashMap;
import javax.swing.ImageIcon;

/**
 * Icons
 *
 * @author Douglas Lau
 */
public class Icons {

	/** Red/green/blue color channel filter */
	abstract static public class ChannelFilter extends RGBImageFilter {
		public ChannelFilter() {
			canFilterIndexColorModel = true;
		}

		public int filterRGB(int x, int y, int rgb) {
			int r = (rgb & 0xff0000) >> 16;
			int g = (rgb & 0xff00) >> 8;
			int b = rgb & 0xff;
			return (rgb & 0xff000000) | filterChannels(r, g, b);
		}

		abstract protected int filterChannels(int r, int g, int b);
	}

	/** Red channel filter */
	static public class RedFilter extends ChannelFilter {
		protected int filterChannels(int r, int g, int b) {
			r <<= 2;
			if(r > 255)
				r = 255;
			g = 4 * g / 5;
			b = 4 * b / 5;
			return r << 16 | g << 8 | b;
		}
	}

	/** Yellow channel filter */
	static public class YellowFilter extends ChannelFilter {
		protected int filterChannels(int r, int g, int b) {
			r <<= 1;
			if(r > 255)
				r = 255;
			g <<= 1;
			if(g > 255)
				g = 255;
			b = 4 * b / 5;
			return r << 16 | g << 8 | b;
		}
	}

	/** Desaturating channel filter */
	static public class DesaturateFilter extends ChannelFilter {
		protected int filterChannels(int r, int g, int b) {
			return 0x808080 | (r >> 1) << 16 | (g >> 1) << 8 |
				b >> 1;
		}
	}

	/** Cache of ImageIcons */
	static protected final WeakHashMap<String, ImageIcon> map =
		new WeakHashMap<String, ImageIcon>();

	/** Lookup a resource */
	static protected URL lookupResource(String resource) {
		return Icons.class.getResource(resource);
	}

	/** Lookup an image resource */
	static protected URL lookupImageResource(String key) {
		URL url = lookupResource("/images/" + key + ".gif");
		if(url != null)
			return url;
		else
			return lookupResource("/images/" + key + ".png");
	}

	/** Get a requested ImageIcon resource */
	static protected ImageIcon getImageIcon(String key) {
		ImageIcon icon = map.get(key);
		if(icon != null)
			return icon;
		URL url = lookupImageResource(key);
		if(url != null) {
			icon = new ImageIcon(url);
			map.put(key, icon);
		}
		return icon;
	}

	/** Get an icon from a string name */
	static public ImageIcon getIcon(String key) {
		return getImageIcon( key );
	}

	/** Get an image from a string name */
	static public Image getImage(String key) {
		ImageIcon icon = getImageIcon( key );
		if(icon == null)
			return null;
		return icon.getImage();
	}

	/** Filter an image icon */
	static public ImageIcon filter(ImageIcon i, ChannelFilter f) {
		Toolkit t = Toolkit.getDefaultToolkit();
		return new ImageIcon(t.createImage(new FilteredImageSource(
			i.getImage().getSource(), f)));
	}
}
