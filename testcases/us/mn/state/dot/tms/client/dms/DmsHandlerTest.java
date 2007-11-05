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
package us.mn.state.dot.tms.client.dms;

import javax.swing.ListModel;

import us.mn.state.dot.tms.client.TmsConnection;

/**
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.3 $ $Date: 2003/04/30 15:52:42 $
 */
public class DmsHandlerTest {
	
	/** Creates new DmsHandlerTest */
	public DmsHandlerTest() {
	}
	
	private void printModel( ListModel model ) {
//		System.out.println( "Model: " + model.getName() );
		for ( int i = 0; i < model.getSize(); i++ ) {
			System.out.println( "\t" + model.getElementAt( i ).toString() );
		}
	}
	
	public static void main( String[] args ) {
		TmsConnection connection = new TmsConnection( null, null, null );
		DMSHandler handler = new DMSHandler();
		handler.setConnection( connection );
		connection.open( "tms-iris", "engs1eri" );
		DmsHandlerTest tester = new DmsHandlerTest();
		tester.printModel( handler.getDeployed() );
		tester.printModel( handler.getFailed() );
		tester.printModel( handler.getAvailable() );
	}
	
	
}
