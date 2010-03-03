/*
 * Copyright (C) 2002-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera.stream;

import java.awt.Dimension;
import java.text.SimpleDateFormat;

/**
 * 
 * @author Timothy Johnson
 *
 */
public class Constants {

	public static final Dimension SIF_QUARTER = new Dimension(176,120);
	public static final Dimension SIF_FULL = new Dimension(352,240);
	public static final Dimension SIF_4X = new Dimension(704,480);
	
	public static final SimpleDateFormat DATE_FORMAT =
		new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");

}
