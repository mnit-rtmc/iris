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

/**
 * ASN1 object factory.
 *
 * @author Douglas Lau
 */
abstract public class ObjFactory<T extends ASN1Object> {

	/** Make an object */
	abstract public T make(MIBNode n, int[] idx);

	/** Make an object */
	abstract public T make(MIBNode n);

	/** Integer factory */
	static public ObjFactory<ASN1Integer> INTEGER =
		new ObjFactory<ASN1Integer>()
	{
		public ASN1Integer make(MIBNode n, int[] idx) {
			return new ASN1Integer(n, idx);
		}
		public ASN1Integer make(MIBNode n) {
			return new ASN1Integer(n);
		}
	};
}
