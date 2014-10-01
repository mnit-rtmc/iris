/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import us.mn.state.dot.tms.CameraPreset;

/**
 * Renderer for camera preset combo boxes.
 *
 * @author Douglas Lau
 */
public class PresetComboRenderer extends DefaultListCellRenderer {

	/** Get component to render list cell */
	@Override
	public Component getListCellRendererComponent(JList list,
		Object value, int index, boolean isSelected,
		boolean cellHasFocus)
	{
		return super.getListCellRendererComponent(list,
			presetLabel(value), index, isSelected, cellHasFocus);
	}

	/** Get a label for a camera preset */
	private Object presetLabel(Object value) {
		if (value instanceof CameraPreset) {
			CameraPreset p = (CameraPreset)value;
			return p.getCamera().getName() + ":" + p.getPresetNum();
		} else
			return value;
	}
}
