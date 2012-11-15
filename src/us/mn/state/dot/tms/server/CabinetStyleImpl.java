/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.tms.CabinetStyle;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.TMSException;

/**
 * A cabinet style has attributes related to controller cabinets.
 *
 * @author Douglas Lau
 */
public class CabinetStyleImpl extends BaseObjectImpl implements CabinetStyle {

	/** Load all the cabinet styles */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, CabinetStyleImpl.class);
		store.query("SELECT name, dip FROM iris." + SONAR_TYPE + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CabinetStyleImpl(
					row.getString(1),	// name
					(Integer)row.getObject(2) // dip
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("dip", dip);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new cabinet style */
	public CabinetStyleImpl(String n) {
		super(n);
	}

	/** Create a new cabinet style */
	protected CabinetStyleImpl(String n, Integer d) {
		this(n);
		dip = d;
	}

	/** Check for special null dip setting */
	protected Integer checkDip(Integer d) {
		if(d != null && d == 0 && !name.equals("336"))
			return null;
		else
			return d;
	}

	/** DIP switch setting */
	protected Integer dip;

	/** Set the DIP switch setting */
	public void setDip(Integer d) {
		dip = d;
	}

	/** Set the DIP switch setting */
	public void doSetDip(Integer d) throws TMSException {
		d = checkDip(d);
		if(d == dip)
			return;
		if(d < 0)
			throw new ChangeVetoException("Invalid DIP");
		store.update(this, "dip", d);
		setDip(d);
	}

	/** Get the DIP switch setting */
	public Integer getDip() {
		return dip;
	}
}
