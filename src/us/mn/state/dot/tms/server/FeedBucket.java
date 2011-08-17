/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Feed bucket for storing feed messages.
 *
 * @author Douglas Lau
 */
public class FeedBucket {

	/** Bucket hash map */
	static private final HashMap<String, HashMap<String, FeedMsg>> bucket =
		new HashMap<String, HashMap<String, FeedMsg>>();

	/** Add a feed message to the bucket */
	static public synchronized void add(FeedMsg msg) {
		HashMap<String, FeedMsg> feed = getFeed(msg.getFeed());
		feed.put(msg.getDms(), msg);
	}

	/** Get a feed message from the bucket */
	static public synchronized FeedMsg getMessage(String fid, String dms) {
		HashMap<String, FeedMsg> feed = getFeed(fid);
		return feed.get(dms);
	}

	/** Get the specified feed */
	static private HashMap<String, FeedMsg> getFeed(String fid) {
		if(bucket.containsKey(fid))
			return bucket.get(fid);
		else {
			HashMap<String, FeedMsg> feed =
				new HashMap<String, FeedMsg>();
			bucket.put(fid, feed);
			return feed;
		}
	}

	/** Purge all expired feed messages */
	static public synchronized void purgeExpired() {
		for(HashMap<String, FeedMsg> feed: bucket.values())
			purgeExpired(feed);
	}

	/** Purge expired messages in the given feed */
	static private void purgeExpired(HashMap<String, FeedMsg> feed) {
		Iterator<String> it = feed.keySet().iterator();
		while(it.hasNext()) {
			FeedMsg msg = feed.get(it.next());
			if(msg.hasExpired())
				it.remove();
		}
	}
}
