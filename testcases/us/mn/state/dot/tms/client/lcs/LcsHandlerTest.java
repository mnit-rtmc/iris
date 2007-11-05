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

import us.mn.state.dot.tms.Roadway;
import us.mn.state.dot.tms.client.NamedListModel;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.2 $ $Date: 2002/10/24 19:06:30 $
 */
public class LcsHandlerTest {
	
	/** Creates new DmsHandlerTest */
	public LcsHandlerTest() {
	}
	
	private void printModel( NamedListModel model ) {
		System.out.println( "Model: " + model.getName() );
		for ( int i = 0; i < model.getSize(); i++ ) {
			System.out.println( "\t" + model.getElementAt( i ).toString() );
		}
	}
	
	public static void main( String[] args ) {
		TmsConnection connection = new TmsConnection( null, null, null );
		LcsHandler handler = new LcsHandler();
		handler.setConnection( connection );
		connection.open( "151.111.224.166", "engs1eri" );
		LcsHandlerTest tester = new LcsHandlerTest();
		tester.printModel( handler.getDirectionModel( Roadway.EAST ) );
		tester.printModel( handler.getDirectionModel( Roadway.WEST ) );
	}
	
	
}
