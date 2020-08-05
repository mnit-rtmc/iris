/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Renderer for camera preset combo boxes.
 *
 * @author Michael Janson
 * @author Gordon Parikh
 */
public class PresetComboRendererLong extends IListCellRenderer<CameraPreset> {

	/** Get a CameraPreset as a string. If null (used for the first entry as a
	 *  "title"), an I18N message is returned.
	 */
	@Override
	protected String asText(CameraPreset cp) {
		if (cp == null)
			return I18N.get("camera.camera_preset");
		return super.asText(cp);
	}
	
	/** Convert camera preset to a string */
	@Override
	protected String valueToString(CameraPreset cp) {
		Direction dir = Direction.fromOrdinal(cp.getDirection());
		if (dir != Direction.UNKNOWN)
			return dir.description;
		else
			return "Preset #" + cp.getPresetNum();
	}
}

