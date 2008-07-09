/*
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * This class defines a pan-tilt-zoom button.
 *
 * @author Stephen Donecker
 * @company University of California, Davis
 * @created June 26, 2008
 */
public class PTZButton extends JButton {

	/** The preferred size of the button */
	static protected final Dimension SIZE = new Dimension(30, 30);

	/** The preferred insets for the button */
	static protected final Insets INSETS = new Insets(0, 0, 0, 0);

	/** Font to use for the button */
	static protected final Font FONT = new Font(null, Font.PLAIN, 20);

	/** The pan-tilt-zoom unit vector  */
	private final int[] m_unitVector;

	/** The name of the button */
	private final String m_name;

	/** The tool tip description of the button */
	private final String m_description;

	/** Create a new PTZ button */
	protected PTZButton(String name, String description, int[] unitVector) {
		
		super(name);
		
		// check preconditions
		assert name != null : "PTZButton.PTZButton: The name argument is null";
		assert description != null : "PTZButton.PTZButton: The description argument is null";
		assert unitVector.length == 3 : "PTZButton.PTZButton: The unitVector is the wrong length";
		assert (unitVector[0] >= -1 && unitVector[0] <= 1) : "PTZButton.PTZButton: The unitVector[0] is out of bounds";
		assert (unitVector[1] >= -1 && unitVector[1] <= 1) : "PTZButton.PTZButton: The unitVector[0] is out of bounds";
		assert (unitVector[2] >= -1 && unitVector[2] <= 1) : "PTZButton.PTZButton: The unitVector[0] is out of bounds";

		m_name = name;
		m_description = description;
		m_unitVector = unitVector;
		setFont(FONT);
		setPreferredSize(SIZE);
		setMinimumSize(SIZE);
		setMargin(INSETS);
		setToolTipText(description);
	}

	/** Get the unit vector */
	public int[] getUnitVector() {
		return m_unitVector;
	}
	
	/** Get the button name */
	public String getName() {
		return m_name;
	}

	/** Get the button tool tip description */
	public String getDescription() {
		return m_description;
	}
}
