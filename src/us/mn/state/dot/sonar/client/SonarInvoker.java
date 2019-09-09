/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2012  Minnesota Department of Transportation
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;

/**
 * A SonarInvoker handles method invocations on SonarObject proxies.
 *
 * @author Douglas Lau
 */
class SonarInvoker implements InvocationHandler {

	/** Get an attribute name from a method */
	static protected String attribute_name(String prefix, Method m) {
		String n = m.getName();
		if(n.startsWith(prefix)) {
			int p = prefix.length();
			StringBuilder b = new StringBuilder();
			b.append(Character.toLowerCase(n.charAt(p)));
			b.append(n.substring(p + 1));
			return b.toString();
		} else
			return null;
	}

	/** Lookup the accessor methods on an interface with a given prefix */
	static protected HashMap<Method, String> lookup_accessors(Class iface,
		String prefix)
	{
		HashMap<Method, String> methods = new HashMap<Method, String>();
		for(Method m: iface.getMethods()) {
			String a = attribute_name(prefix, m);
			if(a != null)
				methods.put(m, a);
		}
		return methods;
	}

	/** Lookup all the setter methods on the specified interface */
	static protected HashMap<Method, String> lookup_setters(Class iface) {
		return lookup_accessors(iface, "set");
	}

	/** Lookup all the getter methods on the specified interface */
	static protected HashMap<Method, String> lookup_getters(Class iface) {
		return lookup_accessors(iface, "get");
	}

	/** Cache of all proxy objects of the specified type */
	protected final TypeCache cache;

	/** Map of setter methods to attribute names */
	protected final HashMap<Method, String> setters;

	/** Map of getter methods to attribute names */
	protected final HashMap<Method, String> getters;

	/** Type name attribute (shared by all proxies of a type) */
	protected final Attribute typeName;

	/** Create an invoker for the specified interface */
	public SonarInvoker(TypeCache c, Class iface) {
		cache = c;
		setters = lookup_setters(iface);
		getters = lookup_getters(iface);
		typeName = new Attribute(c.tname);
	}

	/** Invoke a method call on a proxy instance */
	@SuppressWarnings("unchecked")
	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
		throws SonarException
	{
		assert proxy instanceof SonarObject;
		SonarObject o = (SonarObject)proxy;
		if(getters.containsKey(method))
			return cache.getAttribute(o, getters.get(method));
		else if(setters.containsKey(method)) {
			String aname = setters.get(method);
			boolean check = getters.containsValue(aname);
			cache.setAttribute(o, aname, args, check);
			return null;
		} else {
			String m = method.getName();
			if(m.equals("hashCode"))
				return System.identityHashCode(proxy);
			if(m.equals("equals"))
				return proxy == args[0];
			if(m.equals("toString"))
				return cache.getAttribute(o, "name");
			if(m.equals("destroy")) {
				cache.removeObject(o);
				return null;
			}
		}
		throw NamespaceError.nameUnknown("*method*");
	}

	/** Create attributes for one proxy instance */
	public Map<String, Attribute> createAttributes(String name) {
		HashMap<String, Attribute> amap =
			new HashMap<String, Attribute>();
		for(Map.Entry<Method, String> e: getters.entrySet()) {
			Method m = e.getKey();
			String n = e.getValue();
			Attribute a = new Attribute(m.getReturnType());
			amap.put(n, a);
		}
		for(Map.Entry<Method, String> e: setters.entrySet()) {
			Method m = e.getKey();
			String n = e.getValue();
			if(!amap.containsKey(n)) {
				Class[] p_types = m.getParameterTypes();
				Attribute a = new Attribute(p_types[0]);
				amap.put(n, a);
			}
		}
		amap.put("typeName", typeName);
		amap.put("name", new Attribute(name));
		return Collections.unmodifiableMap(amap);
	}
}
