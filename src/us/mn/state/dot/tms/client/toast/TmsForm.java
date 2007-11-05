/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.awt.Color;
import javax.swing.border.EmptyBorder;

/**
 * Constants defined for TMS forms
 *
 * @author Douglas Lau
 */
public interface TmsForm {

	/** Standard horizontal gap between components */
	int HGAP = 4;

	/** Standard vertical gap between components */
	int VGAP = 6;

	/** Empty border to put around panels */
	EmptyBorder BORDER = new EmptyBorder(VGAP, HGAP, VGAP, HGAP);

	/** Ok status label color */
	Color OK = new Color(0f, 0.5f, 0f);

	/** "ERROR" color */
	Color ERROR = new Color(0.5f, 0f, 0f);

	/** Unknown value string */
	String UNKNOWN = "???";
}
