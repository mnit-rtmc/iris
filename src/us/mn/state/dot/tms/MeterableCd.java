/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2004  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * A meterable CD lane segment
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 */
public class MeterableCd extends MeterableImpl {

	/** ObjectVault table name */
	static public final String tableName = "meterable_cd";

	/** Creates new Meterable Cd */
	public MeterableCd(boolean hovBypass) throws RemoteException {
		super( false, 0, 0, hovBypass );
	}

	/** Create a meterable Cd segment from an ObjectVault field map */
	protected MeterableCd(FieldMap fields) throws RemoteException {
		super(fields);
	}

	/** Validate the segment from the previous segment's values */
	public boolean validate(int lanes, int shift, int cd) {
		return super.validate(lanes, shift, cd) && this.cd > 0;
	}
}
