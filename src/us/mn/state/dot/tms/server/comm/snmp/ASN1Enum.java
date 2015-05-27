/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.snmp;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * ASN1 Enum.
 *
 * @author Douglas Lau
 */
public class ASN1Enum<T extends Enum> extends ASN1Integer {

	/** Create a new ASN1 enum.
	 * @param n MIB node.
	 * @param idx Node index.
	 * @param j Table index. */
	public ASN1Enum(MIBNode n, int idx, int j) {
		super(n, idx, j);
	}

	/** Create a new ASN1 enum.
	 * @param n MIB node.
	 * @param idx Node index. */
	public ASN1Enum(MIBNode n, int idx) {
		super(n, idx);
	}

	/** Create a new ASN1 enum.
	 * @param n MIB node. */
	public ASN1Enum(MIBNode n) {
		super(n);
	}

	/** Set the enum value */
	public void setEnum(T v) {
		setInteger(v.ordinal());
	}

	/** Get the enum value */
	public T getEnum() {
		T[] values = genericType().getEnumConstants();
		if (values != null) {
			int v = getInteger();
			if (v >= 0 && v < values.length)
				return values[v];
		}
		return null;
	}

	/** Lookup the type of T */
	@SuppressWarnings("unchecked")
	private Class<T> genericType() {
		Type sc = getClass().getGenericSuperclass();
		if (sc instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType)sc;
			Type[] types = pt.getActualTypeArguments();
			if (types != null && types.length > 0)
				return (Class<T>) types[0];
		}
		return null;
	}

	/** Get the object value */
	@Override
	public String getValue() {
		T e = getEnum();
		return (e != null) ? e.toString() : "null";
	}
}
