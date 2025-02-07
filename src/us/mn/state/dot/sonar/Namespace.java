/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.postgis.MultiPolygon;
import us.mn.state.dot.tms.Hashtags;
import us.mn.state.dot.tms.Permission;
import us.mn.state.dot.tms.Role;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.User;

/**
 * A namespace is a mapping of names to objects.
 *
 * @author Douglas Lau
 */
abstract public class Namespace {

	/** NULL REF string */
	static private String NULL_STR = String.valueOf(Message.NULL_REF.code);

	/** Date formatter for formatting/parsing dates in ISO 8601 format */
	static private final SimpleDateFormat iso8601 =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

	/** Get the name of a SONAR type */
	static public String typeName(Class t)
		throws NoSuchFieldException, IllegalAccessException
	{
		assert SonarObject.class.isAssignableFrom(t);
		Field f = t.getField("SONAR_TYPE");
		return (String) f.get(t);
	}

	/** Get names of possible SONAR types */
	static public String[] typeNames(Class t)
		throws NoSuchFieldException, IllegalAccessException
	{
		assert SonarObject.class.isAssignableFrom(t);
		Field f = t.getField("SONAR_TYPES");
		return (String []) f.get(t);
	}

	/** Get the base name of a SONAR type */
	static public String baseName(Class t)
		throws NoSuchFieldException, IllegalAccessException
	{
		try {
			assert SonarObject.class.isAssignableFrom(t);
			Field f = t.getField("SONAR_BASE");
			return (String) f.get(t);
		}
		catch (NoSuchFieldException | IllegalAccessException e) {
			return typeName(t);
		}
	}

	/** Make an array of the given class and size */
	static private Object[] makeArray(Class t, int size) {
		return (Object []) Array.newInstance(t, size);
	}

	/** Marshall a java object into a parameter value string */
	public String marshall(Object v) {
		if (v instanceof SonarObject) {
			SonarObject o = (SonarObject) v;
			return o.getName();
		} else if (v instanceof Date) {
			String dt = iso8601.format(v);
			return dt;
		}
		else if (v != null)
			return v.toString();
		else
			return NULL_STR;
	}

	/** Marshall java parameters into a parameter value string */
	public String[] marshall(Class t, Object[] v) {
		if (t.isArray())
			v = (Object [])v[0];
		String[] values = new String[v.length];
		for (int i = 0; i < v.length; i++)
			values[i] = marshall(v[i]);
		return values;
	}

	/** Unmarshall a parameter value string into a java object   */
	public Object unmarshall(Class t, String p) throws ProtocolError {
		if (NULL_STR.equals(p))
			return null;
		if (t == String.class)
			return p;
		try {
			if (t == Integer.TYPE || t == Integer.class)
				return Integer.valueOf(p);
			else if (t == Short.TYPE || t == Short.class)
				return Short.valueOf(p);
			else if (t == Boolean.TYPE || t == Boolean.class)
				return Boolean.valueOf(p);
			else if (t == Float.TYPE || t == Float.class)
				return Float.valueOf(p);
			else if (t == Long.TYPE || t == Long.class)
				return Long.valueOf(p);
			else if (t == Double.TYPE || t == Double.class)
				return Double.valueOf(p);
			else if (t == List.class || t == ArrayList.class)
				return Arrays.asList(p);
			else if (t == Date.class)
				return iso8601.parse(p);
			else if (t == MultiPolygon.class)
				return new MultiPolygon(p);
		}
		catch (NumberFormatException|SQLException|ParseException e) {
			throw ProtocolError.invalidParameter(p);
		}
		if (SonarObject.class.isAssignableFrom(t))
			return unmarshallObject(t, p);
		else {
			throw ProtocolError.invalidParameter(p);
		}
	}

	/** Unmarshall a SONAR object reference */
	private Object unmarshallObject(Class t, String p)
		throws ProtocolError
	{
		try {
			return unmarshallObjectB(t, p);
		}
		catch (NoSuchFieldException e) {
			System.err.println("SONAR: SONAR_TYPE and " +
				"SONAR_TYPES not defined for " + t);
			throw ProtocolError.invalidParameter(p);
		}
		catch (Exception e) {
			System.err.println("SONAR: unmarshall \"" + p +
				"\": " + e.getMessage());
			throw ProtocolError.invalidParameter(p);
		}
	}

	/** Unmarshall a SONAR object reference */
	private Object unmarshallObjectB(Class t, String p)
		throws NoSuchFieldException, IllegalAccessException
	{
		try {
			return lookupObject(typeName(t), p);
		}
		catch (NoSuchFieldException e) {
			for (String typ: typeNames(t)) {
				Object o = lookupObject(typ, p);
				if (o != null)
					return o;
			}
			return null;
		}
	}

	/** Unmarshall parameter strings into one java parameter */
	public Object unmarshall(Class t, String[] v) throws ProtocolError {
		if (t.isArray())
			return unmarshallArray(t.getComponentType(), v);
		else {
			if (v.length != 1)
				throw ProtocolError.wrongParameterCount();
			return unmarshall(t, v[0]);
		}
	}

	/** Unmarshall parameter strings into a java array parameter */
	private Object[] unmarshallArray(Class t, String[] v)
		throws ProtocolError
	{
		Object[] values = makeArray(t, v.length);
		for (int i = 0; i < v.length; i++)
			values[i] = unmarshall(t, v[i]);
		return values;
	}

	/** Unmarshall multiple parameters */
	public Object[] unmarshall(Class[] pt, String[] v) throws ProtocolError
	{
		if (pt.length == 1 && pt[0].isArray()) {
			return new Object[] {
				unmarshall(pt[0], v)
			};
		}
		if (pt.length != v.length)
			throw ProtocolError.wrongParameterCount();
		Object[] params = new Object[pt.length];
		for (int i = 0; i < params.length; i++)
			params[i] = unmarshall(pt[i], v[i]);
		return params;
	}

	/** Get a user's access level for a name.
	 * @param name Name to check.
	 * @param u User to check.
	 * @return Access level (0-4). */
	public int accessLevel(Name name, User u) {
		if (u != null && u.getEnabled()) {
			Role r = u.getRole();
			if (r != null && r.getEnabled())
				return accessLevel(name, r);
		}
		return 0;
	}

	/** Get a user's access level for a name.
	 * @param name Name to check.
	 * @param role Role to check.
	 * @return Access level (0-4). */
	private int accessLevel(Name name, Role role) {
		int level = 0;
		Iterator<SonarObject> it = iterator(Permission.SONAR_TYPE);
		while (it.hasNext()) {
			SonarObject so = it.next();
			if (so instanceof Permission) {
				Permission p = (Permission) so;
				if (p.getRole() == role) {
					int lvl = accessLevel(name, p);
					level = Math.max(level, lvl);
				}
			}
		}
		return level;
	}

	/** Get permission access level for a name.
	 * @param name Name to check.
	 * @param p Permission to check.
	 * @return Access level (0-4). */
	private int accessLevel(Name name, Permission p) {
		String base = getTypeBase(name);
		if (p.getBaseResource().equals(base)) {
			String h = p.getHashtag();
			if (h != null) {
				SonarObject o = lookupObject(name);
				String n = (o != null) ? o.getNotes() : null;
				if (n != null && new Hashtags(n).contains(h))
					return p.getAccessLevel();
				// FIXME: this is hacky -- find a better way!
				String tp = name.getTypePart();
				if (SignMessage.SONAR_TYPE.equals(tp))
					return p.getAccessLevel();
			} else
				return p.getAccessLevel();
		}
		return 0;
	}

	/** Lookup an object in the SONAR namespace.
	 * @param name Sonar name
	 * @return Object from namespace or null if name does not exist */
	private SonarObject lookupObject(Name name) {
		return lookupObject(name.getTypePart(), name.getObjectPart());
	}

	/** Get base resource type name */
	abstract protected String getTypeBase(Name name);

	/** Lookup an object in the SONAR namespace.
	 * @param tname Sonar type name
	 * @param oname Sonar object name
	 * @return Object from namespace or null if name does not exist */
	abstract public SonarObject lookupObject(String tname, String oname);

	/** Get an iterator for all objects of a type.
	 * @param tname Sonar type name.
	 * @return Iterator of all objects of the type. */
	abstract public Iterator<SonarObject> iterator(String tname);

	/** Get a count of the number of objects of the specified type.
	 * @param tname Sonar type name
	 * @return Total number of objects of the specified type */
	abstract public int getCount(String tname);
}
