/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.utils.I18NMessages;
import us.mn.state.dot.tms.utils.SString;

/**
 * Helper class for DMS. Used on the client and server.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSHelper extends BaseHelper {

	/** don't instantiate */
	private DMSHelper() {
		assert false;
	}

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of deployed style */
	static public final String STYLE_DEPLOYED = "User Deployed";

	/** Name of travel time style */
	static public final String STYLE_TRAVEL_TIME = "Travel Time";

	/** Name of automated warning system deployed style */
	static public final String STYLE_AWS_DEPLOYED =
		I18NMessages.get("dms.aws.deployed");

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of automated warning system controlled style */
	static public final String STYLE_AWS_CONTROLLED =
		I18NMessages.get("dms.aws.controlled");

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** all styles */
	static public final String[] STYLES_ALL = {STYLE_AVAILABLE, 
		STYLE_DEPLOYED, STYLE_TRAVEL_TIME, STYLE_MAINTENANCE, 
		STYLE_INACTIVE, STYLE_FAILED, STYLE_AWS_CONTROLLED, 
		STYLE_NO_CONTROLLER};

	/** Test if a DMS is available */
	static public boolean isAvailable(DMS proxy) {
		return isActive(proxy) &&
		       !isFailed(proxy) &&
		       !isDeployed(proxy) &&
		       !needsMaintenance(proxy);
	}

	/** Test if a DMS is active */
	static public boolean isActive(DMS proxy) {
		Controller ctr = proxy.getController();
		return ctr != null && ctr.getActive();
	}

	/** Test if a DMS has a travel time message deployed */
	static public boolean isTravelTime(DMS proxy) {
		SignMessage m = proxy.getMessageCurrent();
		if(m != null) {
			return m.getPriority() ==
			       DMSMessagePriority.TRAVEL_TIME.ordinal();
		} else {
			// messageCurrent should never be null, so this means
			// the proxy has just been removed
			return false;
		}
	}

	/** Test if a DMS is deployed */
	static public boolean isDeployed(DMS proxy) {
		SignMessage m = proxy.getMessageCurrent();
		if(m != null)
			return !SignMessageHelper.isBlank(m);
		else {
			// messageCurrent should never be null, so this means
			// the proxy has just been removed
			return false;
		}
	}

	/** Test if a DMS is active, not failed and deployed */
	static public boolean isMessageDeployed(DMS proxy) {
		return isActive(proxy) &&
		       !isFailed(proxy) &&
		       isDeployed(proxy);
	}

	/** Test if a DMS can be controlled by AWS */
	static public boolean isAwsControlled(DMS proxy) {
		return proxy.getAwsAllowed() && proxy.getAwsControlled();
	}

	/** Test if a DMS has an AWS message deployed */
	static public boolean isAwsDeployed(DMS proxy) {
		SignMessage m = proxy.getMessageCurrent();
		return m.getPriority() == DMSMessagePriority.AWS.ordinal();
	}

	/** Test if a DMS is active, not failed and deployed by AWS */
	static public boolean isAwsMessageDeployed(DMS proxy) {
		return isActive(proxy) &&
		       !isFailed(proxy) &&
		       isAwsDeployed(proxy);
	}

	/** Test if a DMS has been deployed by a user */
	static public boolean isUserDeployed(DMS proxy) {
		return isMessageDeployed(proxy) &&
		       !isTravelTime(proxy) &&
		       !isAwsDeployed(proxy);
	}

	/** Test if a DMS has been deployed for travel time */
	static public boolean isTravelTimeDeployed(DMS proxy) {
		return isMessageDeployed(proxy) &&
		       isTravelTime(proxy);
	}

	/** Test if a DMS needs maintenance */
	static public boolean needsMaintenance(DMS proxy) {
		if(isFailed(proxy) || !isActive(proxy))
			return false;
		Integer h = proxy.getFaceHeight();
		Integer w = proxy.getFaceWidth();
		if(h == null || w == null || h <= 0 || w <= 0)
			return true;
		Controller ctr = proxy.getController();
		if(ctr != null && ctr.getStatus().equals("")) {
			String e = ctr.getError();
			return !e.equals("");
		} else
			return false;
	}

	/** Test if a DMS if failed */
	static public boolean isFailed(DMS proxy) {
		Controller ctr = proxy.getController();
		return ctr != null && (!"".equals(ctr.getStatus()));
	}

	/** Check the style of the specified proxy */
	static public boolean checkStyle(String s, DMS proxy) {
		if(STYLE_AVAILABLE.equals(s))
			return isAvailable(proxy);
		else if(STYLE_DEPLOYED.equals(s))
			return isUserDeployed(proxy);
		else if(STYLE_TRAVEL_TIME.equals(s))
			return isTravelTimeDeployed(proxy);
		else if(STYLE_AWS_DEPLOYED.equals(s))
			return isAwsMessageDeployed(proxy);
		else if(STYLE_MAINTENANCE.equals(s))
			return needsMaintenance(proxy);
		else if(STYLE_INACTIVE.equals(s))
			return !isActive(proxy);
		else if(STYLE_FAILED.equals(s))
			return isFailed(proxy);
		else if(STYLE_AWS_CONTROLLED.equals(s))
			return isAwsControlled(proxy);
		else if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else
			return STYLE_ALL.equals(s);
	}

	/** return a string that contains all active DMS styles,
	 *  separated by commas. */
	static public String getAllStyles(DMS proxy) {
		StringBuilder s = new StringBuilder("");
		for(String style: STYLES_ALL)
			if(checkStyle(style, proxy))
				s.append(style).append(", ");
		return SString.removeTail(s.toString(), ", ");
	}

	/** Lookup the DMS with the specified name */
	static public DMS lookup(String name) {
		return (DMS)namespace.lookupObject(DMS.SONAR_TYPE, name);
	}

	/** Empty text field */
	static protected final String EMPTY_TXT = "    ";

	/** Get the verification camera name */
	static public String getCameraName(DMS proxy) {
		Camera camera = proxy.getCamera();
		if(camera == null)
			return EMPTY_TXT;
		else
			return camera.getName();
	}
}
