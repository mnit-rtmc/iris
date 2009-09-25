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

import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.utils.I18N;
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

	/** Name of scheduled style */
	static public final String STYLE_SCHEDULED = "Scheduled";

	/** Name of automated warning system deployed style */
	static public final String STYLE_AWS_DEPLOYED =
		I18N.get("dms.aws.deployed");

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of automated warning system controlled style */
	static public final String STYLE_AWS_CONTROLLED =
		I18N.get("dms.aws.controlled");

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** all styles */
	static public final String[] STYLES_ALL = {STYLE_AVAILABLE, 
		STYLE_DEPLOYED, STYLE_SCHEDULED, STYLE_TRAVEL_TIME,
		STYLE_MAINTENANCE, STYLE_INACTIVE, STYLE_FAILED,
		STYLE_AWS_CONTROLLED, STYLE_NO_CONTROLLER};

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
		SignMessage sm = proxy.getMessageCurrent();
		if(sm != null) {
			return sm.getRunTimePriority() ==
			       DMSMessagePriority.TRAVEL_TIME.ordinal();
		} else {
			// messageCurrent should never be null, so this means
			// the proxy has just been removed
			return false;
		}
	}

	/** Test if a DMS has a scheduled message deployed */
	static public boolean isScheduled(DMS proxy) {
		SignMessage sm = proxy.getMessageCurrent();
		if(sm != null) {
			DMSMessagePriority rp = DMSMessagePriority.fromOrdinal(
				sm.getRunTimePriority());
			switch(rp) {
			case PSA:
			case TRAVEL_TIME:
			case SCHEDULED:
			case INCIDENT_LOW:
			case INCIDENT_MED:
			case INCIDENT_HIGH:
				return true;
			default:
				return false;
			}
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
		if(proxy == null)
			return false;
		return proxy.getAwsAllowed() && proxy.getAwsControlled();
	}

	/** Test if a DMS has an AWS message deployed */
	static public boolean isAwsDeployed(DMS proxy) {
		if(proxy == null)
			return false;
		SignMessage m = proxy.getMessageCurrent();
		if(m != null) {
			return m.getRunTimePriority() == DMSMessagePriority.AWS.
				ordinal() && !SignMessageHelper.isBlank(m);
		} else {
			// messageCurrent should never be null, so this means
			// the proxy has just been removed
			return false;
		}
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
		       !isScheduled(proxy) &&
		       !isAwsDeployed(proxy);
	}

	/** Test if a DMS has been deployed by travel time */
	static public boolean isTravelTimeDeployed(DMS proxy) {
		return isMessageDeployed(proxy) &&
		       isTravelTime(proxy);
	}

	/** Test if a DMS has been deployed by schedule */
	static public boolean isScheduleDeployed(DMS proxy) {
		return isMessageDeployed(proxy) &&
		       isScheduled(proxy);
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
		if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		// FIXME: this grabs to LCS type lock, and we probably
		//        already have the DMS type lock.  Plus, this doesn't
		//        work until the LCS objects have been enumerated.
		//        There's got to be a better way...
		if(LCSHelper.lookup(proxy.getName()) != null)
			return false;
		if(STYLE_AVAILABLE.equals(s))
			return isAvailable(proxy);
		else if(STYLE_DEPLOYED.equals(s))
			return isUserDeployed(proxy);
		else if(STYLE_TRAVEL_TIME.equals(s))
			return isTravelTimeDeployed(proxy);
		else if(STYLE_SCHEDULED.equals(s))
			return isScheduleDeployed(proxy);
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

	/** Find DMS using a Checker */
	static public DMS find(final Checker<DMS> checker) {
		return (DMS)namespace.findObject(DMS.SONAR_TYPE, checker);
	}

	/** Find DMS using a Checker */
	static public DMS find(final Checker<DMS> checker, final SignGroup sg) {
		DmsSignGroup dsg = (DmsSignGroup)namespace.findObject(
			DmsSignGroup.SONAR_TYPE, new Checker<DmsSignGroup>()
		{
			public boolean check(DmsSignGroup dsg) {
				if(sg == dsg.getSignGroup())
					return checker.check(dsg.getDms());
				else
					return false;
			}
		});
		if(dsg != null)
			return dsg.getDms();
		else
			return null;
	}

	/** Empty text field */
	static protected final String EMPTY_TXT = "    ";

	/** Get the verification camera name */
	static public String getCameraName(DMS proxy) {
		if(proxy != null) {
			Camera camera = proxy.getCamera();
			if(camera != null)
				return camera.getName();
		}
		return EMPTY_TXT;
	}

	/** Get the DMS freeway direction from the geo location as a String */
	static public String getFreeDir(DMS proxy) {
		if(proxy == null)
			return "";
		GeoLoc g = proxy.getGeoLoc();
		if(g == null)
			return "";
		short fd = g.getFreeDir();
		String free = Direction.DIRECTION[fd];
		return free;
	}

	/** Get the default font number for a DMS */
	static public int getDefaultFontNumber(DMS dms) {
		PixelMapBuilder b = createPixelMapBuilder(dms);
		if(b != null)
			return b.getDefaultFontNumber();
		else
			return FontHelper.DEFAULT_FONT_NUM;
	}

	/** Create a pixel map builder for a DMS.
	 * @param dms DMS with proper dimensions for the builder.
	 * @return A pixel map builder, or null is dimensions are invalid. */
	static public PixelMapBuilder createPixelMapBuilder(DMS dms) {
		Integer w = dms.getWidthPixels();
		Integer h = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(w != null && h != null && cw != null && ch != null)
			return new PixelMapBuilder(w, h, cw, ch);
		else
			return null;
	}

	/** Determine if the DMS is periodically queriable. */
	public static boolean isPeriodicallyQueriable(DMS d) {
		// FIXME: signAccess is supposed to indicate the *physical*
		//        access of the DMS.  It was never intended to be used
		//        in this manner.  We should really lookup the comm
		//        link and figure it out from there. This is presently
		//	  agency specific code (Caltrans).
		return !SString.containsIgnoreCase(d.getSignAccess(), "modem");
	}

	/** Get current sign message text as an array of strings. */
	public static String[] getText(DMS proxy) {
		SignMessage sm = proxy.getMessageCurrent();
		if(sm != null) {
			String multi = sm.getMulti();
			if(multi != null)
				return new MultiString(multi).getText();
		}
		return new String[0];
	}

	/** Return a single string which is formated to be readable 
	 *  by the user and contains all sign message lines on the 
	 *  specified DMS. */
	public static String buildMsgLine(DMS proxy) {
		String[] lines = getText(proxy);
		StringBuilder ret = new StringBuilder();
		for(int i = 0; i < lines.length; ++i) {
			if(lines[i] != null)
				ret.append(lines[i]);
			if(i + 1 < lines.length)
				ret.append(" / ");
		}
		return ret.toString();
	}
}
