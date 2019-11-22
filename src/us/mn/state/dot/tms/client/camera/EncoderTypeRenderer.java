/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;

/**
 * Renderer for encoder types.
 *
 * @author Douglas Lau
 */
public class EncoderTypeRenderer extends IListCellRenderer<EncoderType> {

	/** Convert encoder type to a string */
	@Override
	protected String valueToString(EncoderType et) {
		return (et.getMake() + " " +
		        et.getModel() + " " +
		        et.getConfig()).trim();
	}
}
