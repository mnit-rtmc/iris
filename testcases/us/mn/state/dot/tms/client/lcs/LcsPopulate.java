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

import us.mn.state.dot.shape.MapObject;
import us.mn.state.dot.shape.shapefile.ShapeLayer;
import us.mn.state.dot.shape.shapefile.ShapeObject;
import us.mn.state.dot.tms.LCSList;
import us.mn.state.dot.tms.LaneControlSignal;
import us.mn.state.dot.tms.client.toast.TMSProxy;

/**
 * This class populates the TMS with LaneControlSignals.
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @version $Revision: 1.7 $ $Date: 2003/05/15 14:39:42 $
 */
public class LcsPopulate {

	/** Creates a new instance of LcsPopulate */
	public LcsPopulate() { }

	public void populate() throws Exception {
		TMSProxy tms = new TMSProxy( "151.111.224.166", "engs1eri" );
		LCSList lcsList = ( LCSList ) tms.getLCSList().getList();
		ShapeLayer layer = new ShapeLayer( "gpoly/lcs", "lcs" );
		MapObject[] objects = layer.getMapObjects();
		for ( int i = 0; i < objects.length; i++ ) {
			ShapeObject object = ( ShapeObject ) objects[ i ];
			String id = object.getValue( "ID" ).toString();
			lcsList.add( id, ( ( Integer ) object.getValue(
				"LANES" ) ).intValue() );
			LaneControlSignal lcs =
				( LaneControlSignal ) lcsList.getElement( id );
			lcs.setX( ( float ) object.getShape().getBounds().getX() );
			lcs.setY( ( float ) object.getShape().getBounds().getY() );
			lcs.setFreeway( object.getValue( "FREEWAY" ).toString() );
			lcs.setCrossStreet( object.getValue( "CROSS" ).toString() );
			lcs.setFreeDir( ( short ) (
				( ( Integer ) object.getValue( "DIR" ) ).intValue() ) );
		}
	}

	/**
	 * @param args  the command line arguments
	 */
	public static void main( String[] args ) {
		LcsPopulate populator = new LcsPopulate();
		try {
			populator.populate();
		} catch ( Exception e ) {
			e.printStackTrace();
		}
	}

}
