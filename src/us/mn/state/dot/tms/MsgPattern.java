/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.SonarObject;

/**
 * A message pattern is a partially or fully composed message for a DMS.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface MsgPattern extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "msg_pattern";

	/** Get the SONAR type name */
	@Override
	default String getTypeName() {
		return SONAR_TYPE;
	}

	/** SONAR base type name */
	String SONAR_BASE = DMS.SONAR_TYPE;

	/** Get the message MULTI string.
	 * @return Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	String getMulti();

	/** Set the message MULTI string.
	 * @param multi Message text in MULTI markup.
	 * @see us.mn.state.dot.tms.utils.MultiString */
	void setMulti(String multi);

	/** Get flash beacon flag */
	boolean getFlashBeacon();

	/** Set flash beacon flag */
	void setFlashBeacon(boolean fb);

	/** Get pixel service flag */
	boolean getPixelService();

	/** Set pixel service flag */
	void setPixelService(boolean ps);

	/** Get the hashtag for composing with the pattern.
	 * @return hashtag; null for no composing. */
	String getComposeHashtag();

	/** Set the hashtag for composing with the pattern.
	 * @param cht hashtag; null for no composing. */
	void setComposeHashtag(String cht);
}
