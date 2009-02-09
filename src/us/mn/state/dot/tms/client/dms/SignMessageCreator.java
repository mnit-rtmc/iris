/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMSMessagePriority;
import us.mn.state.dot.tms.SignMessage;

/**
 * This is a utility class to create sign messages.
 *
 * @author Douglas Lau
 */
public class SignMessageCreator {

	/** Create a SONAR name to check for allowed updates */
	static protected String createNamespaceString(String name) {
		return SignMessage.SONAR_TYPE + "/" + name;
	}

	/** Sign message type cache */
	protected final TypeCache<SignMessage> sign_messages;

	/** SONAR User for permission checks */
	protected final User user;

	/** Unique ID for sign message naming */
	protected int uid = 0;

	/** Create a new sign message creator */
	public SignMessageCreator(TypeCache<SignMessage> sm, User u) {
		sign_messages = sm;
		user = u;
	}

	/** 
	 * Create a new sign message.
	 * @param multi MULTI text.
	 * @param bitmaps Base64-encoded array of bitmaps.
	 * @param duration Message duration; null for indefinite.
	 * @return Proxy of new sign message.
	 */
	public SignMessage create(String multi, String[] bitmaps,
		Integer duration)
	{
		String name = createName();
		if(name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("multi", multi);
			attrs.put("bitmaps", bitmaps);
			attrs.put("duration", duration);
			attrs.put("activationPriority", new Integer(
				DMSMessagePriority.OPERATOR.ordinal()));
			attrs.put("runTimePriority", new Integer(
				DMSMessagePriority.OPERATOR.ordinal()));
			attrs.put("owner", user);
			sign_messages.createObject(name, attrs);
			return getProxy(name);
		} else
			return null;
	}

	/** Get the sign message proxy object */
	protected SignMessage getProxy(String name) {
		// wait for up to 20 seconds for proxy to be created
		for(int i = 0; i < 200; i++) {
			SignMessage m = sign_messages.lookupObject(name);
			if(m != null)
				return m;
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {
				// Ignore
			}
		}
		return null;
	}

	/** Create a sign message name */
	protected String createName() {
		String name = createUniqueSignMessageName();
		if(canAddSignMessage(name))
			return name;
		else
			return null;
	}

	/** Check if the user can add the named sign message */
	public boolean canAddSignMessage(String name) {
		return name != null && user.canAdd(createNamespaceString(name));
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
		final int uid_max = names.size() + 1;
		for(int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = user.getName() + "_" + _uid;
			if(!names.contains(n)) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}

	/** 
	 * Create a HashSet containing all SignMessage names for the user.
	 * @return A HashSet with entries as SignMessage names.
	 */
	protected HashSet<String> createSignMessageNameSet() {
		final HashSet<String> names = new HashSet<String>();
		sign_messages.findObject(new Checker<SignMessage>() {
			public boolean check(SignMessage sm) {
				if(sm.getOwner() == user)
					names.add(sm.getName());
				return false;
			}
		});
		return names;
	}
}
