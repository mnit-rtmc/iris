/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010 AHMCT, University of California
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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import javax.swing.JButton;

/**
 * This class defines a pan-tilt-zoom preset button.
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 * @created June 26, 2008
 */
public class PresetButton extends JButton {

	/** The preferred size of the button */
	static protected final Dimension SIZE = new Dimension(30, 30);

	/** The preferred insets for the button */
	static protected final Insets INSETS = new Insets(0, 0, 0, 0);

	/** Font to use for the button */
	static protected final Font FONT = new Font(null, Font.PLAIN, 20);

	/** The preset of the button */
	private final int m_preset;

	/** Create a new PTZ button */
	protected PresetButton(int preset, String description) {
		
		super(new Integer(preset).toString());

		// check preconditions
		assert preset > 0 : "PresetButton.PresetButton: Invalid preset";

		m_preset = preset;
		setFont(FONT);
		setPreferredSize(SIZE);
		setMinimumSize(SIZE);
		setMargin(INSETS);
		setToolTipText(description);
	}

	/** Get the button preset */
	public int getPreset() {
		return m_preset;
	}
}
