/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2015  Minnesota Department of Transportation
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedList;
import us.mn.state.dot.sched.DebugLog;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Simple Network Management Protocol (SNMP)
 *
 * @author Douglas Lau
 */
public class SNMP extends BER {

	/** SNMP debug log */
	static private final DebugLog SNMP_LOG = new DebugLog("snmp");

 	/** SNMP error status codes */
	static protected final int NO_ERROR = 0;
	static protected final int TOO_BIG = 1;
	static protected final int NO_SUCH_NAME = 2;
	static protected final int BAD_VALUE = 3;
	static protected final int READ_ONLY = 4;
	static protected final int GEN_ERROR = 5;

	/** Get a tag that matches */
	@Override
	protected Tag getTag(byte clazz, boolean constructed, int number) {
		Tag tag = super.getTag(clazz, constructed, number);
		if (tag != null)
			return tag;
		else
			return SNMPTag.fromValues(clazz, constructed, number);
	}

	/** SNMP version number */
	static public final int SNMP_VERSION = 0;

	/** Public community name */
	static public final String PUBLIC = "Public";

	/** Ledstar firmware bug workaround. Instead of 128,129,130,..., it
	 * returns -128,-127,-126,... */
	static protected final int REQUEST_ID_MAX_LEDSTAR_BUG = 127;

	/** Last SNMP request-id */
	protected int last_request = 0;

	/** Encode an SNMP message */
	protected void encodeSNMPMessage(String community) throws IOException {
		byte[] pdu = getEncodedData();
		encodeInteger(SNMP_VERSION);
		encodeOctetString(community.getBytes());
		encoder.write(pdu);
		encodeSequence(getEncodedData());
	}

	/** Decode an SNMP message */
	protected void decodeSNMPMessage(InputStream is, String community)
		throws IOException
	{
		if (decodeSequence(is) > is.available())
			throw new ParsingException("INVALID SNMP LENGTH");
		if (decodeInteger(is) != 0)
			throw new ParsingException("SNMP VERSION MISMATCH");
		String c = new String(decodeOctetString(is));
		if (!c.equals(community))
			throw new ParsingException("SNMP COMMUNITY MISMATCH");
	}

	/** SNMP message class */
	public class Message implements CommMessage {

		/** Output stream for this message */
		protected final OutputStream os;

		/** Input stream for this message */
		protected final InputStream is;

		/** Community name */
		protected final String community;

		/** SNMP request-id */
		public final int request_id;

		/** List of objects set or get with this message */
		protected final LinkedList<ASN1Object> mos =
			new LinkedList<ASN1Object>();

		/** Create a new SNMP message */
		public Message(OutputStream o, InputStream i, String c) {
			os = o;
			is = i;
			community = (c != null) ? c : PUBLIC;
			request_id = last_request++;
			if (last_request > REQUEST_ID_MAX_LEDSTAR_BUG)
				last_request = 0;
		}

		/** Add a controller property */
		public void add(ControllerProperty cp) {
			if (cp instanceof ASN1Object)
				mos.add((ASN1Object)cp);
		}

		/** Query the controller properties.  This is accomplished with
		 * an SNMP get-request.
		 * @throws IOException On any errors sending a request or
		 *         receiving response */
		public void queryProps() throws IOException {
			if (mos.isEmpty())
				return;
			is.skip(is.available());
			encodeVarBindList(false);
			encodeRequestPDU(SNMPTag.GET_REQUEST);
			encodeSNMPMessage(community);
			encoder.writeTo(os);
			encoder.reset();
			os.flush();
			decodeResponse();
		}

		/** Store the controller properties.  This is accomplished with
		 * an SNMP set-request.
		 * @throws IOException On any errors sending a request or
		 *         receiving response */
		public void storeProps() throws IOException {
			if (mos.isEmpty())
				return;
			is.skip(is.available());
			encodeVarBindList(true);
			encodeRequestPDU(SNMPTag.SET_REQUEST);
			encodeSNMPMessage(community);
			encoder.writeTo(os);
			encoder.reset();
			os.flush();
			decodeResponse();
		}

		/** Decode a response to a SET or GET request */
		protected void decodeResponse() throws IOException {
			for (int i = 0;; i++) {
				try {
					decodeSNMPMessage(is, community);
					decodeResponsePDU(is);
					decodeVarBindList(is);
					return;
				}
				catch (RequestIDException e) {
					SNMP_LOG.log(e.getMessage());
					is.skip(is.available());
					if (i >= 5)
						throw e;
				}
			}
		}

		/** Encode a null variable binding */
		protected void encodeVarBind(ASN1Object mo, boolean set)
			throws IOException
		{
			encodeObjectIdentifier(mo.getOID());
			if (set)
				mo.encode(SNMP.this);
			else
				encodeNull();
			encodeSequence(getEncodedData());
		}

		/** Encode the variable binding list */
		protected void encodeVarBindList(boolean set)
			throws IOException
		{
			ByteArrayOutputStream vb = new ByteArrayOutputStream();
			for (ASN1Object mo: mos) {
				encodeVarBind(mo, set);
				vb.write(getEncodedData());
			}
			encodeSequence(vb.toByteArray());
		}

		/** Encode an SNMP request PDU
		 * @param tag PDU type identifier */
		protected void encodeRequestPDU(Tag tag) throws IOException {
			byte[] varBindList = getEncodedData();
			encodeInteger(request_id);
			encodeInteger(0);	// error-status
			encodeInteger(0);	// error-index
			encoder.write(varBindList);
			byte[] buffer = getEncodedData();
			encodeIdentifier(tag);
			encodeLength(buffer.length);
			encoder.write(buffer);
		}

		/** Decode a variable binding */
		protected void decodeVarBind(InputStream is, ASN1Object mo)
			throws IOException
		{
			decodeSequence(is);
			// FIXME: compare with OID from mo
			decodeObjectIdentifier(is);
			mo.decode(is, SNMP.this);
		}

		/** Decode the variable binding list */
		protected void decodeVarBindList(InputStream is)
			throws IOException
		{
			decodeSequence(is);
			for (ASN1Object mo: mos)
				decodeVarBind(is, mo);
		}

		/** Decode an SNMP response PDU */
		protected void decodeResponsePDU(InputStream is)
			throws IOException
		{
			if (decodeIdentifier(is) != SNMPTag.GET_RESPONSE)
				throw new ParsingException("!GET_RESPONSE TAG");
			if (decodeLength(is) > is.available())
				throw new ParsingException("INVALID PDU LEN");
			int request = decodeInteger(is);
			if (request != request_id)
				throw new RequestIDException(request);
			int error = decodeInteger(is);
			int index = decodeInteger(is);
			switch (error) {
			case TOO_BIG:
				throw new TooBig();
			case NO_SUCH_NAME:
				throw new NoSuchName(getName(index));
			case BAD_VALUE:
				throw new BadValue(getObject(index));
			case READ_ONLY:
				throw new ReadOnly(getName(index));
			case GEN_ERROR:
				throw new GenError(getObject(index));
			}
		}

		/** Get the object name */
		private ASN1Object getObject(int i) {
			if (i > 0 && i <= mos.size())
				return mos.get(i - 1);
			else
				return null;
		}

		/** Get the object name */
		private String getName(int i) {
			ASN1Object o = getObject(i);
			return (o != null) ? o.getName() : null;
		}

		/** Request ID mismatch exception */
		public class RequestIDException extends ParsingException {

			/** Create a new Request-ID exception */
			protected RequestIDException(int request) {
				super("SNMP REQUEST ID: " + request + " != " +
					request_id);
			}
		}

		/** TooBig exception */
		public class TooBig extends ControllerException {

			/** Create a new TooBig exception */
			protected TooBig() {
				super("SNMP: TOO BIG");
			}
		}

		/** NoSuchName exception */
		public class NoSuchName extends ControllerException {

			/** Create a new NoSuchName exception */
			protected NoSuchName(String n) {
				super("SNMP: NO SUCH NAME: " + n);
			}
		}

		/** BadValue exception */
		public class BadValue extends ControllerException {

			/** Create a new BadValue exception */
			protected BadValue(ASN1Object o) {
				super("SNMP: BAD VALUE: " + o);
			}
		}

		/** ReadOnly exception */
		public class ReadOnly extends ControllerException {

			/** Create a new ReadOnly exception */
			protected ReadOnly(String n) {
				super("SNMP: READ ONLY: " + n);
			}
		}

		/** GenError exception */
		public class GenError extends ControllerException {

			/** Create a new GenError exception */
			protected GenError(ASN1Object o) {
				super("SNMP: GEN ERROR: " + o);
			}
		}
	}
}
