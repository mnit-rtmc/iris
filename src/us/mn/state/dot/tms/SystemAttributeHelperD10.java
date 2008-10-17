/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.utils.IrisInfo;
import us.mn.state.dot.tms.utils.SString;

/**
 * Static System Attribute convenience methods accessible from
 * the client and server, and are specific to Caltrans D10.
 * @author Michael Darter
 */
public class SystemAttributeHelperD10 extends SystemAttributeHelper {

	/** d10 specific attribute names */
	private final static String CAWS_ACTIVE = 
		"caltrans_d10_caws_active";
	private final static String DMSLITE_OP_TIMEOUT_SECS = 
		"caltrans_d10_op_timeout_secs";
	private final static String DMSLITE_MODEM_OP_TIMEOUT_SECS = 
		"caltrans_d10_modem_op_timeout_secs";

	/** disallow instantiation */
	protected SystemAttributeHelperD10() {
		assert false;
	}

	/** Return dmslite operation timeout in seconds */
	public static int dmsliteOpTimeoutSecs() {
		return SystemAttributeHelper.getValueIntDef(
			DMSLITE_OP_TIMEOUT_SECS, 60+5);
	}

	/** Return dmslite modem operation timeout in seconds */
	public static int dmsliteModemOpTimeoutSecs() {
		return SystemAttributeHelper.getValueIntDef(
			DMSLITE_MODEM_OP_TIMEOUT_SECS, 5*60+5);
	}

	/** return true if CAWS poller should handle caws messages */
	public static boolean isCAWSActive() {
		return SystemAttributeHelper.getValueBooleanDef(
			CAWS_ACTIVE, false);
	}
}

