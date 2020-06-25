/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2020  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;

/**
 * Cell renderer used for cameras.
 *
 * @author Douglas Lau
 */
public class CameraCellRenderer extends IListCellRenderer<Camera> {

	/** Convert value to a string */
	@Override
	protected String valueToString(Camera value) {
		return CameraHelper.getCameraNum(value);
	}
}
