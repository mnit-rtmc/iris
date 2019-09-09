/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.server;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import us.mn.state.dot.sonar.GroupChecker;
import us.mn.state.dot.sonar.Message;
import us.mn.state.dot.sonar.MessageEncoder;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A type node represents the first-level nodes in the SONAR namespace. It
 * contains all information about a SONAR type.
 *
 * @author Douglas Lau
 */
public class TypeNode {

	/** Initial capacity of type hash */
	static private final int INITIAL_CAPACITY = 256;

	/** Type name */
	public final String name;

	/** Group privilege checker */
	public final GroupChecker group_chk;

	/** All child objects of this type are put here.  Note: we still
	 * synchronize on updates to this hash map to prevent inconsistency.
	 * Synchronization is not needed to read or iterate over the map. */
	private final ConcurrentHashMap<String, SonarObject> children =
		new ConcurrentHashMap<String, SonarObject>(INITIAL_CAPACITY,
		0.75f, 1);

	/** An attribute dispatcher can set and get attributes on objects */
	private final AttributeDispatcher dispatcher;

	/** Create a namespace type node */
	public TypeNode(Namespace ns, String n, Class c, GroupChecker gc) {
		name = n;
		group_chk = gc;
		dispatcher = new AttributeDispatcher(c, ns);
	}

	/** Create a new object in the type node */
	public SonarObject createObject(String name) throws SonarException {
		if (children.containsKey(name))
			throw NamespaceError.nameExists(name);
		return dispatcher.createObject(name);
	}

	/** Store an object in the type node */
	public void storeObject(SonarObject o) throws SonarException {
		String name = o.getName();
		synchronized (children) {
			if (children.containsKey(name))
				throw NamespaceError.nameExists(name);
			dispatcher.storeObject(o);
			children.put(name, o);
		}
	}

	/** Add an object to the type node without storing */
	public void addObject(SonarObject o) throws NamespaceError {
		String name = o.getName();
		synchronized (children) {
			if (children.containsKey(name))
				throw NamespaceError.nameExists(name);
			else
				children.put(name, o);
		}
	}

	/** Remove an object from the type node */
	public void removeObject(SonarObject o) throws SonarException {
		String n = o.getName();
		synchronized (children) {
			SonarObject obj = children.remove(n);
			if (obj == null)
				throw NamespaceError.nameUnknown(n);
			if (obj != o)
				throw NamespaceError.nameExists(n);
			try {
				dispatcher.destroyObject(o);
			}
			catch (SonarException e) {
				children.put(n, o);
				throw e;
			}
		}
	}

	/** Lookup an object from the given name */
	public SonarObject lookupObject(String n) {
		return children.get(n);
	}

	/** Test if an attribute is gettable */
	public boolean isGettable(String a) {
		return dispatcher.isGettable(a);
	}

	/** Get the value of an attribute */
	public String[] getValue(SonarObject o, String a)
		throws SonarException
	{
		return dispatcher.getValue(o, a);
	}

	/** Enumerate all attributes of the named object */
	public void enumerateObject(MessageEncoder enc, SonarObject o)
		throws SonarException, IOException
	{
		assert(o.getTypeName() == name);
		boolean first = true;
		for (String a: dispatcher.getGettableAttributes()) {
			String[] v = getValue(o, a);
			if (first) {
				a = new Name(o, a).toString();
				first = false;
			}
			enc.encode(Message.ATTRIBUTE, a, v);
		}
		if (first)
			enc.encode(Message.TYPE, name);
		enc.encode(Message.OBJECT, o.getName());
	}

	/** Enumerate all the objects of the type node */
	public void enumerateObjects(MessageEncoder enc) throws SonarException,
		IOException
	{
		// We must synchronize here to ensure that no objects are
		// added or removed while enumerating
		synchronized (children) {
			for (SonarObject o: children.values())
				enumerateObject(enc, o);
		}
	}

	/** Set the value of an attribute.
	 * @param name Attribute name in SONAR namespace.
	 * @param v New attribute value.
	 * @return phantom object if one was created; null otherwise */
	public SonarObject setValue(Name name, String[] v)
		throws SonarException
	{
		String oname = name.getObjectPart();
		String aname = name.getAttributePart();
		SonarObject o = children.get(oname);
		if (o != null) {
			dispatcher.setValue(o, aname, v);
			return null;
		} else {
			o = dispatcher.createObject(oname);
			setField(o, aname, v);
			return o;
		}
	}

	/** Set the field attribute value */
	public void setField(SonarObject o, String a, String[] v)
		throws SonarException
	{
		dispatcher.setField(o, a, v);
	}

	/** Get an iterator of all objects of the type */
	public Iterator<SonarObject> iterator() {
		return Collections.unmodifiableCollection(
			children.values()).iterator();
	}

	/** Get the number of objects of this type */
	public int size() {
		return children.size();
	}
}
