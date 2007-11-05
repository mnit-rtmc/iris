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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;

import us.mn.state.dot.tms.client.IrisTab;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.layers.RoadLayer;
import us.mn.state.dot.tms.client.layers.WaterLayer;
import us.mn.state.dot.tms.client.security.IrisUser;
import us.mn.state.dot.tms.client.toast.ExceptionDialog;
import us.mn.state.dot.tms.client.toast.Icons;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * Test Class for LcsTab.
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version   $Revision: 1.8 $ $Date: 2003/05/15 14:39:42 $
 */
public class LcsTabTest extends JFrame {

	private final SmartDesktop desktop = new SmartDesktop( true );

	private JTabbedPane tabs = new JTabbedPane( SwingConstants.BOTTOM );

	private final JPanel panel = new JPanel( new BorderLayout() );

	private final TmsConnection tmsConnection =
			new TmsConnection( desktop, null, null );


	/**
	 * Creates a new instance of LcsTabTest
	 */
	public LcsTabTest() {
		super( "LCS tab test" );
		addWindowListener( new WindowAdapter() {
			public void windowClosing( WindowEvent evt ) {
				System.exit( 0 );
			}
		});
		this.setSize( 1000, 1000 );
		JPanel contentPane = new JPanel( new BorderLayout() );
		setContentPane( contentPane );
		contentPane.add( desktop, BorderLayout.CENTER );
		panel.setSize( 1000, 1000 );
		desktop.add( panel, JLayeredPane.FRAME_CONTENT_LAYER );
		desktop.addComponentListener( new ComponentAdapter() {
			public void componentResized( ComponentEvent evt ) {
				panel.setSize( desktop.getWidth(), desktop.getHeight() );
				panel.validate();
			}
		});
		createBaseLayers();
		IrisTab lcsTab = new us.mn.state.dot.tms.client.lcs.LcsTab( 
			tmsConnection );
		tabs.addTab( "LCS", Icons.getIcon( "cls" ), lcsTab,
				"Operate Lane Control Signals" );
		panel.add( tabs, BorderLayout.CENTER );
		tmsConnection.open( "151.111.224.166", "engs1eri" );
	}


	/**
	 * Executes the test.
	 *
	 * @param args  the command line arguments
	 */
	public static void main( String[] args ) {
		LcsTabTest test = new LcsTabTest();
		test.setVisible( true );
	}


	/**
	 * For looking up IrisTab objects.
	 *
	 * @param tabClass  The class of the requested tab.
	 * @return          The requested IrisTab.
	 */
	public IrisTab getTab( Class tabClass ) {
		return null;
	}


	/**
	 * Called when a successful authentication has happened.
	 *
	 * @param user	The user that logged in.
	 */
	public void login( IrisUser user ) {
	}


	/**
	 * Called when a user has logged off.
	 */
	public void logout() {
	}


	/**
	 * Called when user has quit.
	 */
	public void quit() {
	}


	/**
	 * Create the base layers shared by all maps.
	 *
	 * @return   List of base layers.
	 */
	private java.util.List createBaseLayers() {
		ArrayList layers = new ArrayList();
		javax.swing.ProgressMonitor monitor = new ProgressMonitor(
				desktop, "Loading base layers.", "Loading water layer", 0, 3 );
		try {
			layers.add( new WaterLayer() );
			monitor.setNote( "Loading met-cr" );
			monitor.setProgress( 1 );
			layers.add( new RoadLayer( "met-cr" ) );
			monitor.setNote( "Loading met-csah" );
			monitor.setProgress( 2 );
			layers.add( new RoadLayer( "met-csah" ) );
			monitor.setNote( "Loading stateart" );
			monitor.setProgress( 3 );
			layers.add( new RoadLayer( "stateart" ) );
		} catch ( java.io.IOException ioe ) {
			new ExceptionDialog( ioe ).setVisible( true );
		}
		return layers;
	}

}
