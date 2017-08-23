/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;

/**
 * Renderer for monitor numbers.
 *
 * @author Douglas Lau
 */
public class MonComboRenderer extends IListCellRenderer<VideoMonitor> {

	/** Convert monitor number to a string */
	@Override
	protected String valueToString(VideoMonitor vm) {
		return Integer.toString(vm.getMonNum());
	}
}
