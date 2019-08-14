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
package us.mn.state.dot.sonar.client;

import java.util.Iterator;
import java.util.HashMap;
import us.mn.state.dot.sonar.EmptyIterator;
import us.mn.state.dot.sonar.GroupChecker;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.ProtocolError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.User;

/**
 * The client namespace is a cache which contains SonarObject proxies.
 *
 * @author Douglas Lau
 */
public class ClientNamespace extends Namespace {

	/** Default group checker for types */
	static private final GroupChecker NO_GROUP = new GroupChecker() {
		public boolean checkGroup(Name name, User u, String g) {
			return false;
		}
	};

	/** Map of all types in the cache */
	private final HashMap<String, TypeCache> types =
		new HashMap<String, TypeCache>();

	/** Add a new SonarObject type */
	public void addType(TypeCache tc) {
		types.put(tc.tname, tc);
	}

	/** Current type */
	private TypeCache cur_type = null;

	/** Current object */
	protected SonarObject cur_obj = null;

	/** Get the TypeCache for the current type */
	private TypeCache getTypeCache() throws NamespaceError {
		if (cur_type != null)
			return cur_type;
		else
			throw NamespaceError.nameInvalid("No cur_type");
	}

	/** Get the TypeCache for the specified name */
	private TypeCache getTypeCache(Name name) throws NamespaceError {
		String tname = name.getTypePart();
		if (types.containsKey(tname)) {
			cur_type = types.get(tname);
			return cur_type;
		} else
			throw NamespaceError.nameInvalid(name);
	}

	/** Put a new object in the cache */
	void putObject(String n) throws NamespaceError {
		if (Name.isAbsolute(n)) {
			Name name = new Name(n);
			if (!name.isObject())
				throw NamespaceError.nameInvalid(name);
			cur_obj = getTypeCache(name).add(name.getObjectPart());
		} else
			cur_obj = getTypeCache().add(n);
	}

	/** Remove an object from the cache */
	void removeObject(String n) throws NamespaceError {
		if (Name.isAbsolute(n)) {
			Name name = new Name(n);
			if (!name.isObject())
				throw NamespaceError.nameInvalid(name);
			getTypeCache(name).remove(name.getObjectPart());
		} else
			getTypeCache().remove(n);
	}

	/** Update an object attribute */
	void updateAttribute(String n, String[] v) throws SonarException {
		if (Name.isAbsolute(n)) {
			Name name = new Name(n);
			if (!name.isAttribute())
				throw ProtocolError.wrongParameterCount();
			TypeCache t = getTypeCache(name);
			cur_obj = t.getProxy(name.getObjectPart());
			String a = name.getAttributePart();
			updateAttribute(t, cur_obj, a, v);
		} else
			updateAttribute(getTypeCache(), cur_obj, n, v);
	}

	/** Update an object attribute */
	@SuppressWarnings("unchecked")
	private void updateAttribute(TypeCache t, SonarObject o, String a,
		String[] v) throws SonarException
	{
		if (o == null)
			throw NamespaceError.nameInvalid("No object");
		t.updateAttribute(o, a, v);
	}

	/** Process a TYPE message from the server */
	void setCurrentType(String t) throws NamespaceError {
		if (t.equals("") || types.containsKey(t)) {
			if (t.equals("") && cur_type != null)
				cur_type.enumerationComplete();
			TypeCache tc = types.get(t);
			cur_type = tc;
			cur_obj = null;
		} else
			throw NamespaceError.nameInvalid(t);
	}

	/** Lookup an object in the SONAR namespace.
	 * @param tname Sonar type name
	 * @param oname Sonar object name
	 * @return Object from namespace or null if name does not exist */
	@Override
	public SonarObject lookupObject(String tname, String oname) {
		if (oname != null) {
			TypeCache t = types.get(tname);
			if (t != null)
				return t.lookupObject(oname);
		}
		return null;
	}

	/** Get an iterator for all objects of a type.
	 * @param tname Sonar type name.
	 * @return Iterator of all objects of the type. */
	@Override
	@SuppressWarnings("unchecked")
	public Iterator<SonarObject> iterator(String tname) {
		TypeCache t = types.get(tname);
		if (t != null)
			return t.iterator();
		else
			return new EmptyIterator();
	}

	/** Get a count of the number of objects of the specified type.
	 * @param tname Sonar type name
	 * @return Total number of objects of the specified type */
	@Override
	public int getCount(String tname) {
		TypeCache t = types.get(tname);
		if (t != null)
			return t.size();
		else
			return 0;
	}

	/** Get the group checker for a name type */
	@Override
	protected GroupChecker getGroupChecker(Name name) {
		TypeCache t = getTypeCacheOrNull(name);
		return (t != null) ? t.group_chk : NO_GROUP;
	}

	/** Get the TypeCache for the specified name (or null) */
	private TypeCache getTypeCacheOrNull(Name name) {
		return types.get(name.getTypePart());
	}
}
