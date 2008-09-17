/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import us.mn.state.dot.tms.utils.SString;

/**
 * A traffic device attribute is a name mapped to a string value. Device 
 * attributes are associated with a single traffic device, identified by id.
 * The attribute value is stored as a String. Setter and getter methods for
 * booleans and ints are provided.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class TrafficDeviceAttributeImpl extends BaseObjectImpl 
	implements TrafficDeviceAttribute 
{
	/** Load all the device attributes */
	static protected void loadAll() throws TMSException {
		System.err.println("Loading traffic device attributes...");
		namespace.registerType(SONAR_TYPE, 
			TrafficDeviceAttributeImpl.class);
		store.query("SELECT name, id, aname, avalue "+
			" FROM traffic_device_attribute;",new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.add(new TrafficDeviceAttributeImpl(
					row.getString(1), // name: e.g. V1_CAWS
					row.getString(2), // id: e.g. V1
					row.getString(3), // aname: e.g. CAWS
					row.getString(4)  // avalue: e.g. True 
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("id", id);
		map.put("aname", aname);
		map.put("avalue", avalue);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new device attribute */
	public TrafficDeviceAttributeImpl(String n) {
		super(n);
	}

	/** Create a new device attribute */
	protected TrafficDeviceAttributeImpl(String arg_name, String arg_id, 
		String arg_aname, String arg_avalue) 
	{
		super(arg_name);	// key
		id = arg_id;
		aname = arg_aname;
		avalue = arg_avalue;
	}

	/** id, e.g. "V1" */
	protected String id;

	/** Set the traffic device id */
	public void setId(String arg_id) {
		id = arg_id;
	}

	/** Set the traffic device id, doSet is required for 
	 *  database backed sonar objects */
	public void doSetId(String arg_id) throws TMSException {
		if( arg_id==null )
			return;
		if(id.equals(arg_id))
			return;
		store.update(this, "id", arg_id);
		setId(arg_id);
	}

	/** Get the traffic device id, e.g. "V1" */
	public String getId() {
		return id;
	}

	/** attribute name */
	protected String aname;

	/** Set the attribute name */
	public void setAttributeName(String arg_aname) {
		aname = arg_aname;
	}

	/** Set the traffic device attribute name, doSet is required for 
	 *  database backed sonar objects */
	public void doSetAttributeName(String arg_aname) throws TMSException {
		if(arg_aname==null)
			return;
		if(id.equals(arg_aname))
			return;
		store.update(this, "aname", arg_aname);
		setAttributeName(arg_aname);
	}

	/** Get the attribute name */
	public String getAttributeName() {
		return aname;
	}

	/** attribute value */
	protected String avalue;

	/** Set the attribute value */
	public void setAttributeValue(String arg_avalue) {
		avalue = arg_avalue;
	}

	/** Set the traffic device attribute value, doSet is required for 
	 *  database backed sonar objects */
	public void doSetAttributeValue(String arg_avalue) throws TMSException {
		if(arg_avalue==null)
			return;
		if(id.equals(arg_avalue))
			return;
		store.update(this, "avalue", arg_avalue);
		setAttributeValue(arg_avalue);
	}

	/** Get the attribute value */
	public String getAttributeValue() {
		return avalue;
	}

	/** Set the attribute value as a boolean. */
	public void setAttributeValueBoolean(boolean arg_avalue) {
		avalue = String.valueOf(arg_avalue);
	}

	/** 
	 *  Get the attribute value as a boolean. 
	 *  @return True if the string attribute value is equal, ignoring 
	 *          case, to "true", else false. If the attribute doesn't 
	 * 	    exist false is returned.
	 */
	public boolean getAttributeValueBoolean() {
		avalue = (avalue == null ? "" : avalue);
		return Boolean.parseBoolean(avalue);
	}

	/** Set the attribute value as an int. */
	public void setAttributeValueInt(int arg_avalue) {
		avalue = String.valueOf(arg_avalue);
	}

	/** 
	 *  Get the attribute value as an int. 
	 *  @return The string attribute converted to an int.
	 */
	public int getAttributeValueInt() {
		avalue = (avalue == null ? "0" : avalue);
		return SString.stringToInt(avalue);
	}

	/** toString */
	public String toString() {
		return "TrafficDeviceAttribute: Id="+getId()+", aname="
			+getAttributeName()+", avalue="+getAttributeValue()
			+".";
	}
}
