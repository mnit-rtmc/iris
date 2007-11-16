/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import us.mn.state.dot.vault.FieldMap;

/**
 * A meterable CD lane segment
 *
 * @author Erik Engstrom
 */
public class MeterableCd extends MeterableImpl {

	/** ObjectVault table name */
	static public final String tableName = "meterable_cd";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

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
