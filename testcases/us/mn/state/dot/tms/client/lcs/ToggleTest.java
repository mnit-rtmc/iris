/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.lcs;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JToggleButton;

import us.mn.state.dot.tms.LCSModule;

/**
 * Test class for the LcsToggle.
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.4 $ $Date: 2003/05/15 14:39:42 $
 */
public class ToggleTest extends JFrame {
	
	/** Creates new ToggleTest */
	public ToggleTest() {
		JToggleButton button = new JToggleButton();
		button.setIcon( new LcsModule( LCSModule.GREEN ) );
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( button, BorderLayout.CENTER );

	}
	
	public void testToggle() {
		setVisible( true );
	}
	
	public static void main( String[] args ) {
		ToggleTest test = new ToggleTest();
		test.testToggle();
	}
		
}
