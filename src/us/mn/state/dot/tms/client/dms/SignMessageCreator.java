/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import java.util.HashSet;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DmsMsgPriority;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SignMessageHelper;
import static us.mn.state.dot.tms.SignMsgSource.operator;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;

/**
 * This is a utility class to create sign messages.
 *
 * @author Douglas Lau
 */
public class SignMessageCreator {

	/** Extra message ID numbers for new sign messages */
	static protected final int EXTRA_MSG_IDS = 5;

	/** Sign message type cache */
	private final TypeCache<SignMessage> sign_messages;

	/** User session */
	private final Session session;

	/** SONAR User for permission checks */
	protected final User user;

	/** Unique ID for sign message naming */
	protected int uid = 0;

	/** Create a new sign message creator */
	public SignMessageCreator(Session s, User u) {
		session = s;
		sign_messages =
			s.getSonarState().getDmsCache().getSignMessages();
		user = u;
	}

	/** 
	 * Create a new sign message.
	 * @param multi MULTI text.
	 * @param be Beacon enabled.
	 * @param bitmaps Base64-encoded bitmaps.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param owner User name.
	 * @param duration Message duration; null for indefinite.
	 * @return Proxy of new sign message, or null on error.
	 */
	public SignMessage create(String multi, boolean be, String bitmaps,
		DmsMsgPriority ap, DmsMsgPriority rp, String owner,
		Integer duration)
	{
		SignMessage sm = SignMessageHelper.find(multi, bitmaps, ap, rp,
			operator.ordinal(), owner, duration);
		if (sm != null)
			return sm;
		String name = createName();
		if (name != null) {
			return create(name, multi, be, bitmaps, ap, rp, owner,
				duration);
		} else
			return null;
	}

	/** 
	 * Create a new sign message.
	 * @param name Sign message name.
	 * @param multi MULTI text.
	 * @param be Beacon enabled.
	 * @param bitmaps Base64-encoded bitmaps.
	 * @param ap Activation priority.
	 * @param rp Run-time priority.
	 * @param owner User name.
	 * @param duration Message duration; null for indefinite.
	 * @return Proxy of new sign message, or null on error.
	 */
	private SignMessage create(String name, String multi, boolean be,
		String bitmaps, DmsMsgPriority ap, DmsMsgPriority rp,
		String owner, Integer duration)
	{
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("multi", multi);
		attrs.put("beacon_enabled", be);
		attrs.put("bitmaps", bitmaps);
		attrs.put("activationPriority", new Integer(ap.ordinal()));
		attrs.put("runTimePriority", new Integer(rp.ordinal()));
		attrs.put("source", new Integer(operator.ordinal()));
		if (owner != null)
			attrs.put("owner", owner);
		if (duration != null)
			attrs.put("duration", duration);
		sign_messages.createObject(name, attrs);
		SignMessage sm = getProxy(name);
		// Make sure this is the sign message we just created
		if (sm != null && multi.equals(sm.getMulti()))
			return sm;
		else
			return null;
	}

	/** Get the sign message proxy object */
	protected SignMessage getProxy(String name) {
		// wait for up to 20 seconds for proxy to be created
		for (int i = 0; i < 200; i++) {
			SignMessage m = sign_messages.lookupObject(name);
			if (m != null)
				return m;
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				// Ignore
			}
		}
		return null;
	}

	/** Create a sign message name */
	protected String createName() {
		String name = createUniqueSignMessageName();
		if (canAddSignMessage(name))
			return name;
		else
			return null;
	}

	/** 
	 * Create a SignMessage name, which is in this form: 
	 *    user.name + "_" + uniqueid
	 *    where uniqueid is a sequential integer.
	 */
	protected String createUniqueSignMessageName() {
		HashSet<String> names = createSignMessageNameSet();
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = names.size() + EXTRA_MSG_IDS;
		for(int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = createName(_uid);
			if(!names.contains(n)) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}

	/** Create a SignMessage name.
	 * @param uid ID of name.
	 * @return Name of SignMessage. */
	private String createName(int uid) {
		return user.getName() + '_' + uid;
	}

	/** 
	 * Create a HashSet containing all SignMessage names for the user.
	 * @return A HashSet with entries as SignMessage names.
	 */
	private HashSet<String> createSignMessageNameSet() {
		String name = user.getName();
		HashSet<String> names = new HashSet<String>();
		for (SignMessage sm: sign_messages) {
			if (sm.getName().startsWith(name))
				names.add(sm.getName());
		}
		return names;
	}

	/** Check if the user can add the named sign message */
	public boolean canAddSignMessage(String name) {
		return session.isAddPermitted(SignMessage.SONAR_TYPE, name);
	}

	/** Check if the user can create a sign message */
	public boolean canCreate() {
		return canAddSignMessage(createName(0));
	}
}
