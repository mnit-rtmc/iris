/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2021  Minnesota Department of Transportation
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
import java.sql.SQLException;
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
		store.query("SELECT name, police_panel_pin_1, " +
			"police_panel_pin_2, watchdog_reset_pin_1, " +
			"watchdog_reset_pin_2, dip FROM iris." + SONAR_TYPE +
			";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new CabinetStyleImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("dip", dip);
		return map;
	}

	/** Get the database table name */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new cabinet style */
	public CabinetStyleImpl(String n) {
		super(n);
	}

	/** Create a new cabinet style */
	private CabinetStyleImpl(ResultSet row) throws SQLException {
		super(row.getString(1)); // name
		police_panel_pin_1 = (Integer) row.getObject(2);
		police_panel_pin_2 = (Integer) row.getObject(3);
		watchdog_reset_pin_1 = (Integer) row.getObject(4);
		watchdog_reset_pin_2 = (Integer) row.getObject(5);
		dip = (Integer) row.getObject(6);
	}

	/** Police panel input pin 1 */
	private Integer police_panel_pin_1;

	/** Set the police panel input pin for meter 1 */
	@Override
	public void setPolicePanelPin1(Integer p) {
		police_panel_pin_1 = p;
	}

	/** Set the police panel input pin for meter 1 */
	public void doSetPolicePanelPin1(Integer p) throws TMSException {
		if (p < 1 || p > 104)
			throw new ChangeVetoException("Invalid pin");
		if (p != police_panel_pin_1) {
			store.update(this, "police_panel_pin_1", p);
			setPolicePanelPin1(p);
		}
	}

	/** Get the police panel input pin for meter 1 */
	@Override
	public Integer getPolicePanelPin1() {
		return police_panel_pin_1;
	}

	/** Police panel input pin 2 */
	private Integer police_panel_pin_2;

	/** Set the police panel input pin for meter 2 */
	@Override
	public void setPolicePanelPin2(Integer p) {
		police_panel_pin_2 = p;
	}

	/** Set the police panel input pin for meter 2 */
	public void doSetPolicePanelPin2(Integer p) throws TMSException {
		if (p < 1 || p > 104)
			throw new ChangeVetoException("Invalid pin");
		if (p != police_panel_pin_2) {
			store.update(this, "police_panel_pin_2", p);
			setPolicePanelPin2(p);
		}
	}

	/** Get the police panel input pin for meter 2 */
	@Override
	public Integer getPolicePanelPin2() {
		return police_panel_pin_2;
	}

	/** Watchdog reset pin for meter 1 */
	private Integer watchdog_reset_pin_1;

	/** Set the watchdog reset pin for meter 1 */
	@Override
	public void setWatchdogResetPin1(Integer p) {
		watchdog_reset_pin_1 = p;
	}

	/** Set the watchdog reset pin for meter 1 */
	public void doSetWatchdogResetPin1(Integer p) throws TMSException {
		if (p < 1 || p > 104)
			throw new ChangeVetoException("Invalid pin");
		if (p != watchdog_reset_pin_1) {
			store.update(this, "watchdog_reset_pin_1", p);
			setWatchdogResetPin1(p);
		}
	}

	/** Get the watchdog reset pin for meter 1 */
	@Override
	public Integer getWatchdogResetPin1() {
		return watchdog_reset_pin_1;
	}

	/** Watchdog reset pin for meter 2 */
	private Integer watchdog_reset_pin_2;

	/** Set the watchdog reset pin for meter 2 */
	@Override
	public void setWatchdogResetPin2(Integer p) {
		watchdog_reset_pin_2 = p;
	}

	/** Set the watchdog reset pin for meter 2 */
	public void doSetWatchdogResetPin2(Integer p) throws TMSException {
		if (p < 1 || p > 104)
			throw new ChangeVetoException("Invalid pin");
		if (p != watchdog_reset_pin_2) {
			store.update(this, "watchdog_reset_pin_2", p);
			setWatchdogResetPin2(p);
		}
	}

	/** Get the watchdog reset pin for meter 2 */
	@Override
	public Integer getWatchdogResetPin2() {
		return watchdog_reset_pin_2;
	}

	/** DIP switch setting */
	private Integer dip;

	/** Set the DIP switch setting */
	@Override
	public void setDip(Integer d) {
		dip = d;
	}

	/** Set the DIP switch setting */
	public void doSetDip(Integer d) throws TMSException {
		d = checkDip(d);
		if (d < 0)
			throw new ChangeVetoException("Invalid DIP");
		if (d != dip) {
			store.update(this, "dip", d);
			setDip(d);
		}
	}

	/** Check for special null dip setting */
	private Integer checkDip(Integer d) {
		if (d != null && d == 0 && !name.equals("336"))
			return null;
		else
			return d;
	}

	/** Get the DIP switch setting */
	@Override
	public Integer getDip() {
		return dip;
	}
}
