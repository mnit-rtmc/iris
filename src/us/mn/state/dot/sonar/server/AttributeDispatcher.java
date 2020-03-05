/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2019  Minnesota Department of Transportation
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;

/**
 * An attribute dispatcher is an adapter for SonarObjects. It provides
 * a pair of simple methods to set and get attributes of those objects.
 *
 * @author Douglas Lau
 */
public class AttributeDispatcher {

	/** Method name to store an object */
	static private final String DO_STORE_METHOD = "doStore";

	/** Method name to destroy an object */
	static private final String DESTROY_METHOD = "destroy";

	/** Alternate method name to destroy an object */
	static private final String DO_DESTROY_METHOD = "doDestroy";

	/** Empty array of parameters */
	static private final Object[] NO_PARAMS = new Object[0];

	/** Empty array of strings */
	static private final String[] EMPTY_STRING = new String[0];

	/** Test if a class is an interface extending SonarObject */
	static private boolean is_sonar_iface(Class iface) {
		return iface.isInterface() &&
		       SonarObject.class.isAssignableFrom(iface) &&
		       (iface != SonarObject.class);
	}

	/** Check for a valid constructor */
	static private boolean is_valid_constructor(Constructor c) {
		Class[] paramTypes = c.getParameterTypes();
		return (paramTypes.length == 1) &&
			(paramTypes[0] == String.class);
	}

	/** Get an attribute name from a setter/getter */
	static private String attribute_name(String n) {
		return n.substring(3, 4).toLowerCase() + n.substring(4);
	}

	/** Prepend "do" to a method name */
	static private String prepend_do(String n) {
		return "do" + n.substring(0, 1).toUpperCase() + n.substring(1);
	}

	/** Lookup the constructor */
	static private Constructor lookup_constructor(Class c) {
		for (Constructor con: c.getConstructors()) {
			if (is_valid_constructor(con))
				return con;
		}
		return null;
	}

	/** Lookup a method on the specified class.
	 * @param c Class to inspect.
	 * @param method Name of method to lookup.
	 * @return Matching method, or null if not found. */
	static private Method lookup_method(Class c, String method) {
		for (Method m: c.getMethods()) {
			if (method.equalsIgnoreCase(m.getName())) {
				if (!Modifier.isStatic(m.getModifiers()))
					return m;
			}
		}
		return null;
	}

	/** Lookup a method to store new objects */
	static private Method lookup_storer(Class c) {
		return lookup_method(c, DO_STORE_METHOD);
	}

	/** Lookup a method to destroy objects */
	static private Method lookup_destroyer(Class c) {
		Method m = lookup_method(c, DO_DESTROY_METHOD);
		if (m != null)
			return m;
		else
			return lookup_method(c, DESTROY_METHOD);
	}

	/** The implementation class */
	private final Class the_class;

	/** SONAR namespace */
	private final Namespace namespace;

	/** Constructor to create a new object */
	private final Constructor constructor;

	/** Method to store an object */
	private final Method storer;

	/** Method to destroy an object */
	private final Method destroyer;

	/** Mapping of attribute names to setter methods */
	private final HashMap<String, Method> setters =
		new HashMap<String, Method>();

	/** Mapping of attribute names to getter methods */
	private final HashMap<String, Method> getters =
		new HashMap<String, Method>();

	/** Get an array of gettable attributes */
	public String[] getGettableAttributes() {
		return getters.keySet().toArray(EMPTY_STRING);
	}

	/** Test if an attribute is gettable */
	public boolean isGettable(String a) {
		return getters.containsKey(a);
	}

	/** Create a new attribute dispatcher for the given object's type.
	 * @param c The implementation class.
	 * @param ns SONAR namespace. */
	public AttributeDispatcher(Class c, Namespace ns) {
		the_class = c;
		namespace = ns;
		lookup_attributes(c);
		constructor = lookup_constructor(c);
		storer = lookup_storer(c);
		destroyer = lookup_destroyer(c);
	}

	/** Lookup all the attributes of the specified class */
	private void lookup_attributes(Class c) {
		while (c != null) {
			for (Class iface: c.getInterfaces()) {
				if (is_sonar_iface(iface)) {
					lookup_iface_attributes(iface);
					lookup_attributes(iface);
				}
			}
			c = c.getSuperclass();
		}
	}

	/** Lookup all the attributes of the specified interface */
	private void lookup_iface_attributes(Class iface) {
		for (Method m: iface.getDeclaredMethods()) {
			String n = m.getName();
			if (n.startsWith("set"))
				lookup_setter(m);
			if (n.startsWith("get"))
				lookup_getter(m);
		}
	}

	/** Lookup a setter method.
	 * @param im Setter method from interface. */
	private void lookup_setter(Method im) {
		Method m = lookup__etter(im);
		if (m != null)
			setters.put(attribute_name(im.getName()), m);
	}

	/** Lookup a getter method.
	 * @param im Getter method from interface. */
	private void lookup_getter(Method im) {
		Method m = lookup__etter(im);
		if (m != null)
			getters.put(attribute_name(im.getName()), m);
	}

	/** Lookup a setter or getter method.
	 * @param im Setter or getter method from interface.
	 * @return Matching method, or null if not found. */
	private Method lookup__etter(Method im) {
		// A "do" prefix is required for methods which can throw
		// exceptions not declared in the interface specification.
		// First, check for "do..." methods
		String do_name = prepend_do(im.getName());
		for (Method m: the_class.getMethods()) {
			String n = m.getName();
			if (n.equals(do_name) && compare_methods(im, m))
				return m;
		}
		// "do..." method not found
		for (Method m: the_class.getMethods()) {
			String n = m.getName();
			if (n.equals(im.getName()) && compare_methods(im, m))
				return m;
		}
		return null;
	}

	/** Compare two methods for a signature match. */
	private boolean compare_methods(Method m0, Method m1) {
		if (Modifier.isStatic(m0.getModifiers()) !=
		    Modifier.isStatic(m1.getModifiers()))
			return false;
		if (m0.getReturnType() != m1.getReturnType())
			return false;
		return Arrays.equals(m0.getParameterTypes(),
		                     m1.getParameterTypes());
	}

	/** Create a new object with the given name */
	public SonarObject createObject(String name) throws SonarException {
		if (constructor == null)
			throw PermissionDenied.cannotAdd();
		Object[] params = { name };
		try {
			return (SonarObject)constructor.newInstance(params);
		}
		catch (Exception e) {
			throw new SonarException(e);
		}
	}

	/** Invoke a method on the given SONAR object */
	private Object _invoke(SonarObject o, Method method, Object[] params)
		throws SonarException
	{
		try {
			return method.invoke(o, params);
		}
		catch (Exception e) {
			throw new SonarException(e);
		}
	}

	/** Invoke a method on the given SONAR object */
	private Object invoke(SonarObject o, Method method, String[] v)
		throws SonarException
	{
		Class[] p_types = method.getParameterTypes();
		Object[] params = namespace.unmarshall(p_types, v);
		return _invoke(o, method, params);
	}

	/** Store the given object */
	public void storeObject(SonarObject o) throws SonarException {
		if (storer == null)
			throw PermissionDenied.cannotAdd();
		invoke(o, storer, EMPTY_STRING);
	}

	/** Destroy the given object */
	public void destroyObject(SonarObject o) throws SonarException {
		if (destroyer == null)
			throw PermissionDenied.cannotRemove();
		invoke(o, destroyer, EMPTY_STRING);
	}

	/** Set the value of the named attribute */
	public void setValue(SonarObject o, String a, String[] v)
		throws SonarException
	{
		Method m = setters.get(a);
		if (m == null)
			throw PermissionDenied.cannotWrite(a);
		invoke(o, m, v);
	}

	/** Lookup the named field from the given class */
	static private Field lookupField(Class c, String a)
		throws SonarException
	{
		try {
			Field f = c.getDeclaredField(a);
			f.setAccessible(true);
			return f;
		}
		catch (NoSuchFieldException e) {
			c = c.getSuperclass();
			if (c != null)
				return lookupField(c, a);
			else
				throw new SonarException("No such field: " + a);
		}
		catch (Exception e) {
			throw new SonarException(e);
		}
	}

	/** Set a field directly (through reflection) */
	public void setField(SonarObject o, String a, String[] v)
		throws SonarException
	{
		Field f = lookupField(o.getClass(), a);
		Object param = namespace.unmarshall(f.getType(), v);
		try {
			f.set(o, param);
		}
		catch (Exception e) {
			throw new SonarException(e);
		}
	}

	/** Get the value of the named attribute */
	public String[] getValue(SonarObject o, String a)
		throws SonarException
	{
		Method m = getters.get(a);
		if (m == null)
			throw PermissionDenied.cannotRead(a);
		Object result = _invoke(o, m, NO_PARAMS);
		if (result instanceof Object[]) {
			Object[] r = (Object [])result;
			String[] res = new String[r.length];
			for (int i = 0; i < r.length; i++)
				res[i] = namespace.marshall(r[i]);
			return res;
		} else
			return new String[] { namespace.marshall(result) };
	}
}
