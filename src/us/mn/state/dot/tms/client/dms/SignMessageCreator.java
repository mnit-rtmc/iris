/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import us.mn.state.dot.tms.SignMsgSource;
import us.mn.state.dot.tms.client.Session;

/**
 * This is a utility class to create sign messages.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class SignMessageCreator {

	/** Create sign message name prefix.
	 * @param src Sign message source bits. */
	static private String createPrefix(int src) {
		return (SignMsgSource.blank.bit() == src) ? "blank_" : "user_";
	}

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

	/** Create a new sign message creator */
	public SignMessageCreator() {
		this(Session.getCurrent());
	}

	/** Create a new sign message.
	 *
	 * @param sc Sign configuration.
	 * @param multi MULTI text.
	 * @param be Beacon enabled.
	 * @param duration Message duration; null for indefinite.
	 * @return New sign message, or null on error.
	 */
	public SignMessage create(SignConfig sc, String multi, boolean be,
		Integer duration)
	{
		return create(sc, null, multi, be, DmsMsgPriority.OPERATOR,
			SignMsgSource.operator.bit(), user, duration);
	}

	/** Create a new blank message.
	 *
	 * @return Blank sign message, or null on error.
	 */
	public SignMessage createBlankMessage(SignConfig sc) {
		return create(sc, null, "", false, DmsMsgPriority.BLANK,
			SignMsgSource.blank.bit(), null, null);
	}

	/** Create an incident sign message.
	 *
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param multi MULTI text.
	 * @param mp Message priority.
	 * @param duration Message duration; null for indefinite.
	 * @return New sign message, or null on error.
	 */
	public SignMessage create(SignConfig sc, String inc, String multi,
		DmsMsgPriority mp, Integer duration)
	{
		if (multi.length() > 0) {
			return create(sc, inc, multi, false, mp, INCIDENT_SRC,
				user, duration);
		} else
			return createBlankMessage(sc);
	}

	/** Create a new sign message.
	 *
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param multi MULTI text.
	 * @param be Beacon enabled.
	 * @param mp Message priority.
	 * @param src Sign message source bits.
	 * @param owner User name.
	 * @param duration Message duration; null for indefinite.
	 * @return Proxy of new sign message, or null on error.
	 */
	private SignMessage create(SignConfig sc, String inc, String multi,
		boolean be, DmsMsgPriority mp, int src, String owner,
		Integer duration)
	{
		boolean pp = false; // Operators cannot enable prefix page
		SignMessage sm = SignMessageHelper.find(sc, inc, multi, pp, mp,
			src, owner, duration);
		String prefix = createPrefix(src);
		if (sm != null && sm.getName().startsWith(prefix))
			return sm;
		String name = createName(prefix);
		if (name != null) {
			return create(name, sc, inc, multi, be, pp, mp, src,
			              owner, duration);
		} else
			return null;
	}

	/** Create a new (IPAWS) sign message.
	 *
	 * @param name Sign message name.
	 * @param sc Sign configuration.
	 * @param multi MULTI text.
	 * @param priority Message priority.
	 * @param src Sign message source bits.
	 * @param owner User name.
	 * @param duration Message duration; null for indefinite.
	 * @return Proxy of new sign message, or null on error.
	 */
	public SignMessage create(String name, SignConfig sc,
			String multi, int priority, int src,
			String owner, Integer duration) {
		DmsMsgPriority mp = DmsMsgPriority.fromOrdinal(priority);
		return create(name, sc, null,
			multi, false, false, mp,
			src, owner, duration);
	}

	/** Create a new sign message.
	 *
	 * @param name Sign message name.
	 * @param sc Sign configuration.
	 * @param inc Associated incident (original name).
	 * @param multi MULTI text.
	 * @param be Beacon enabled.
	 * @param pp Prefix page.
	 * @param mp Message priority.
	 * @param src Sign message source bits.
	 * @param owner User name.
	 * @param duration Message duration; null for indefinite.
	 * @return Proxy of new sign message, or null on error.
	 */
	private SignMessage create(String name, SignConfig sc, String inc,
		String multi, boolean be, boolean pp, DmsMsgPriority mp,
		int src, String owner, Integer duration)
	{
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("sign_config", sc);
		if (inc != null)
			attrs.put("incident", inc);
		attrs.put("multi", multi);
		attrs.put("beacon_enabled", be);
		attrs.put("prefix_page", pp);
		attrs.put("msg_priority", new Integer(mp.ordinal()));
		attrs.put("source", new Integer(src));
		if (owner != null)
			attrs.put("owner", owner);
		if (duration != null)
			attrs.put("duration", duration);
		sign_messages.createObject(name, attrs);
		SignMessage sm = sign_messages.lookupObjectWait(name);
		// Make sure this is the sign message we just created
		if (sm != null && multi.equals(sm.getMulti()))
			return sm;
		else
			return null;
	}

	/** Create a sign message name.
	 * @param prefix Prefix to use for name. */
	private String createName(String prefix) {
		String name = createUniqueSignMessageName(prefix);
		return canAddSignMessage(name) ? name : null;
	}

	/** Create a SignMessage name.  The form of the name is prefix + hash
	 * of user name with uid appended.
	 * @param prefix Prefix to use for name. */
	private String createUniqueSignMessageName(String prefix) {
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = sign_messages.size() + MAX_IN_PROCESS_NAMES;
		for (int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = createName(prefix, _uid);
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
	private String createName(String prefix, int i) {
		return prefix + Integer.toHexString((user + i).hashCode());
	}

	/** Check if the user can add the named sign message */
	private boolean canAddSignMessage(String name) {
		return session.isWritePermitted(SignMessage.SONAR_TYPE, name);
	}

	/** Check if the user can create a sign message */
	public boolean canCreate() {
		return canAddSignMessage(createName(createPrefix(0), 0));
	}
}
