/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
 * Copyright (C) 2020       SRF Consulting Group
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
package us.mn.state.dot.tms.client.dms;

import java.util.HashMap;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgPriority;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.client.Session;

/**
 * This is a utility class to create sign messages.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class SignMessageCreator {

	/** Message source bits for incident messages */
	static private final int INCIDENT_SRC =
		SignMsgSource.operator.bit() | SignMsgSource.incident.bit();

	/** The maximum number of new sign message names which can be created
	 * before the TypeCache must be updated.  Creating new objects is
	 * done asynchronously. */
	static private final int MAX_IN_PROCESS_NAMES = 8;

	/** Sign message type cache */
	private final TypeCache<SignMessage> sign_messages;

	/** User session */
	private final Session session;

	/** User name */
	private final String user;

	/** Unique ID for sign message naming */
	private int uid = 0;

	/** Create a new sign message creator */
	public SignMessageCreator(Session s) {
		session = s;
		sign_messages =
			s.getSonarState().getDmsCache().getSignMessages();
		user = s.getUser().getName();
	}

	/** Create an operator sign message.
	 *
	 * @param sc Sign configuration.
	 * @param ms MULTI text.
	 * @param fb Flash beacon.
	 * @param dur Message duration; null for indefinite.
	 * @return New sign message, or null on error.
	 */
	public SignMessage createMsg(SignConfig sc, String ms, boolean fb,
		 Integer dur)
	{
		return createMsg(sc, null, ms, fb, SignMsgPriority.high_1,
			SignMsgSource.operator.bit(), dur);
	}

	/** Create a new blank message.
	 *
	 * @return Blank sign message, or null on error.
	 */
	public SignMessage createMsgBlank(SignConfig sc) {
		return createMsg(sc, null, "", false, SignMsgPriority.low_1,
			SignMsgSource.blank.bit(), null);
	}

	/** Create an incident sign message.
	 *
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param ms MULTI text.
	 * @param mp Message priority.
	 * @param dur Message duration; null for indefinite.
	 * @return New sign message, or null on error.
	 */
	public SignMessage createMsg(SignConfig sc, String inc, String ms,
		SignMsgPriority mp, Integer dur)
	{
		if (ms.length() > 0) {
			return createMsg(sc, inc, ms, false, mp,
				INCIDENT_SRC, dur);
		} else
			return createMsgBlank(sc);
	}

	/** Create a new sign message.
	 *
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param ms MULTI text.
	 * @param fb Flash beacon.
	 * @param mp Message priority.
	 * @param src Sign message source bits.
	 * @param dur Message duration; null for indefinite.
	 * @return Proxy of new sign message, or null on error.
	 */
	private SignMessage createMsg(SignConfig sc, String inc, String ms,
		boolean fb, SignMsgPriority mp, int src, Integer dur)
	{
		String owner = SignMessageHelper.makeMsgOwner(src, user);
		SignMessage sm = SignMessageHelper.find(sc, inc, ms, owner,
			fb, mp, dur);
		if (sm != null)
			return sm;
		String name = createName();
		return (name != null)
		      ? createMsg(name, sc, inc, ms, owner, fb, mp, dur)
		      : null;
	}

	/** Create a new sign message.
	 *
	 * @param name Sign message name.
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param ms MULTI text.
	 * @param msg_owner Message owner.
	 * @param fb Flash beacon.
	 * @param mp Message priority.
	 * @param dur Message duration; null for indefinite.
	 * @return Proxy of new sign message, or null on error.
	 */
	private SignMessage createMsg(String name, SignConfig sc, String inc,
		String ms, String msg_owner, boolean fb, SignMsgPriority mp,
		Integer dur)
	{
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("sign_config", sc);
		if (inc != null)
			attrs.put("incident", inc);
		attrs.put("multi", ms);
		attrs.put("msg_owner", msg_owner);
		attrs.put("flash_beacon", fb);
		attrs.put("msg_priority", Integer.valueOf(mp.ordinal()));
		if (dur != null)
			attrs.put("duration", dur);
		sign_messages.createObject(name, attrs);
		SignMessage sm = sign_messages.lookupObjectWait(name);
		// Make sure this is the sign message we just created
		if (sm != null && ms.equals(sm.getMulti()))
			return sm;
		else
			return null;
	}

	/** Create a sign message name */
	private String createName() {
		String name = createUniqueSignMessageName();
		return canAddSignMessage(name) ? name : null;
	}

	/** Create a SignMessage name.  The form of the name is prefix + hash
	 * of user name with uid appended. */
	private String createUniqueSignMessageName() {
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = sign_messages.size() + MAX_IN_PROCESS_NAMES;
		for (int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = createName(_uid);
			if (sign_messages.lookupObject(n) == null) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}

	/** Create a SignMessage name.
	 * @param i ID of name.
	 * @return Name of SignMessage. */
	private String createName(int i) {
		return "user_" + Integer.toHexString((user + i).hashCode());
	}

	/** Check if the user can add the named sign message */
	private boolean canAddSignMessage(String name) {
		return session.isWritePermitted(SignMessage.SONAR_TYPE, name);
	}

	/** Check if the user can create a sign message */
	public boolean canCreate() {
		return canAddSignMessage(createName(0));
	}
}
