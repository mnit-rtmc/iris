/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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

import java.util.Iterator;

import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Helper class for IPAWS Alert Deployers. Used on the client and server.
 *
 * @author Gordon Parikh
 */
public class IpawsAlertConfigHelper extends BaseHelper {
	
	/** Don't instantiate */
	private IpawsAlertConfigHelper() {
		assert false;
	}
	
	/** Lookup the alert config with the specified name */
	static public IpawsAlertConfig lookup(String name) {
		return (IpawsAlertConfig) namespace.lookupObject(
				IpawsAlertConfig.SONAR_TYPE, name);
	}
	
	/** Get an IpawsAlertConfig object iterator */
	static public Iterator<IpawsAlertConfig> iterator() {
		return new IteratorWrapper<IpawsAlertConfig>(namespace.iterator(
				IpawsAlertConfig.SONAR_TYPE));
	}
	
	/** Name creator */
	static UniqueNameCreator UNC;
	static {
		UNC = new UniqueNameCreator("ipaws_cfg_%d", (n)->lookup(n));
		UNC.setMaxLength(24);
	}

	/** Create a unique IpawsAlertConfig record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}
}
