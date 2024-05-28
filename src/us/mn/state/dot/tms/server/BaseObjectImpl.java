/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2024  Minnesota Department of Transportation
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

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.server.Server;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.BaseHelper;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.MainServer.FLUSH;
import us.mn.state.dot.tms.server.event.BaseEvent;

/**
 * Base object class for storable SONAR objects.
 *
 * @author Douglas Lau
 */
abstract public class BaseObjectImpl implements Storable, SonarObject {

	/** SONAR namespace */
	static public ServerNamespace namespace;

	/** SQL connection to database */
	static SQLConnection store;

	/** Corridor manager */
	static public final CorridorManager corridors = new CorridorManager();

	/** Load all objects from the database into the SONAR Namespace */
	static void loadAll(SQLConnection s, ServerNamespace ns)
		throws TMSException
	{
		store = s;
		namespace = ns;
		SystemAttributeImpl.loadAll();
		GraphicImpl.loadAll();
		FontImpl.loadAll();
		GlyphImpl.loadAll();
		RoadImpl.loadAll();
		RoadAffixImpl.loadAll();
		GeoLocImpl.loadAll();
		MapExtentImpl.loadAll();
		IncidentDetailImpl.loadAll();
		CommConfigImpl.loadAll();
		CommLinkImpl.loadAll();
		ModemImpl.loadAll();
		CabinetStyleImpl.loadAll();
		ControllerImpl.loadAll();
		SignConfigImpl.loadAll();
		SignDetailImpl.loadAll();
		DayMatcherImpl.loadAll();
		DayPlanImpl.loadAll();
		PlanPhaseImpl.loadAll();
		ActionPlanImpl.loadAll();
		R_NodeImpl.loadAll();
		/* NOTE: must happen after r_nodes are loaded */
		corridors.createCorridors();
		AlarmImpl.loadAll();
		GpsImpl.loadAll();
		CameraTemplateImpl.loadAll();
		VidSourceTemplateImpl.loadAll();
		CameraVidSourceOrderImpl.loadAll();
		DetectorImpl.loadAll();
		TollZoneImpl.loadAll();
		EncoderTypeImpl.loadAll();
		EncoderStreamImpl.loadAll();
		CameraImpl.loadAll();
		CameraPresetImpl.loadAll();
		PlayListImpl.loadAll();
		CatalogImpl.loadAll();
		MonitorStyleImpl.loadAll();
		VideoMonitorImpl.loadAll();
		FlowStreamImpl.loadAll();
		BeaconImpl.loadAll();
		WeatherSensorImpl.loadAll();
		RampMeterImpl.loadAll();
		SignMessageImpl.loadAll();
		DMSImpl.loadAll();
		MsgPatternImpl.loadAll();
		MsgLineImpl.loadAll();
		GateArmArrayImpl.loadAll();
		GateArmImpl.loadAll();
		TagReaderImpl.loadAll();
		LaneMarkingImpl.loadAll();
		LCSArrayImpl.loadAll();
		LCSImpl.loadAll();
		LCSIndicationImpl.loadAll();
		LaneUseMultiImpl.loadAll();
		ParkingAreaImpl.loadAll();
		IncidentImpl.loadAll();
		IncDescriptorImpl.loadAll();
		IncLocatorImpl.loadAll();
		IncAdviceImpl.loadAll();
		TimeActionImpl.loadAll();
		DmsActionImpl.loadAll();
		BeaconActionImpl.loadAll();
		CameraActionImpl.loadAll();
		LaneActionImpl.loadAll();
		MeterActionImpl.loadAll();
		WordImpl.loadAll();
		DMSImpl.updateAllStyles();
		RptConduitImpl.loadAll();
		AlertConfigImpl.loadAll();
		AlertMessageImpl.loadAll();
		AlertInfoImpl.loadAll();
		RwisConditionImpl.loadAll();
		RwisSignImpl.loadAll();
	}

	/** Get the time as a time stamp */
	static protected Date asTimestamp(Long ts) {
		return (ts != null) ? new Date(ts) : null;
	}

	/** Get time as milliseconds since epoch */
	static protected Long stampMillis(Date ts) {
		if (ts != null)
			return ts.getTime();
		else
			return null;
	}

	/** Compare two (possibly-null) objects for equality */
	static protected boolean objectEquals(Object o0, Object o1) {
		return (o0 != null) ? o0.equals(o1) : o1 == null;
	}

	/** Lookup a sonar object */
	static private SonarObject lookupObject(String st, String name) {
		if (namespace != null)
			return namespace.lookupObject(st, name);
		else
			return null;
	}

	/** Lookup a road */
	static protected RoadImpl lookupRoad(String name) {
		SonarObject so = lookupObject(RoadImpl.SONAR_TYPE, name);
		return (so instanceof RoadImpl) ? (RoadImpl) so : null;
	}

	/** Lookup a geo location */
	static protected GeoLocImpl lookupGeoLoc(String name) {
		SonarObject so = lookupObject(GeoLocImpl.SONAR_TYPE, name);
		return (so instanceof GeoLocImpl) ? (GeoLocImpl) so : null;
	}

	/** Lookup an r_node */
	static protected R_NodeImpl lookupR_Node(String name) {
		SonarObject so = lookupObject(R_NodeImpl.SONAR_TYPE, name);
		return (so instanceof R_NodeImpl) ? (R_NodeImpl) so : null;
	}

	/** Lookup a cabinet style */
	static protected CabinetStyleImpl lookupCabinetStyle(String name) {
		SonarObject so = lookupObject(CabinetStyleImpl.SONAR_TYPE,name);
		return (so instanceof CabinetStyleImpl)
		      ? (CabinetStyleImpl) so
		      : null;
	}

	/** Lookup a comm config */
	static protected CommConfigImpl lookupCommConfig(String name) {
		SonarObject so = lookupObject(CommConfigImpl.SONAR_TYPE, name);
		return (so instanceof CommConfigImpl)
		      ? (CommConfigImpl) so
		      : null;
	}

	/** Lookup a comm link */
	static protected CommLinkImpl lookupCommLink(String name) {
		SonarObject so = lookupObject(CommLinkImpl.SONAR_TYPE, name);
		return (so instanceof CommLinkImpl)
		      ?	(CommLinkImpl) so
		      : null;
	}

	/** Lookup a controller */
	static protected ControllerImpl lookupController(String name) {
		SonarObject so = lookupObject(ControllerImpl.SONAR_TYPE, name);
		return (so instanceof ControllerImpl)
		      ? (ControllerImpl) so
		      : null;
	}

	/** Lookup a toll zone */
	static protected TollZoneImpl lookupTollZone(String name) {
		SonarObject so = lookupObject(TollZoneImpl.SONAR_TYPE, name);
		if (so instanceof TollZoneImpl)
			return (TollZoneImpl)so;
		else
			return null;
	}

	/** Lookup a GPS */
	static protected GpsImpl lookupGps(String name) {
		SonarObject so = lookupObject(GpsImpl.SONAR_TYPE, name);
		return (so instanceof GpsImpl) ? (GpsImpl) so : null;
	}

	/** Lookup a graphic */
	static protected GraphicImpl lookupGraphic(String name) {
		SonarObject so = lookupObject(GraphicImpl.SONAR_TYPE, name);
		return (so instanceof GraphicImpl) ? (GraphicImpl) so : null;
	}

	/** Lookup a beacon */
	static protected BeaconImpl lookupBeacon(String name) {
		SonarObject so = lookupObject(BeaconImpl.SONAR_TYPE, name);
		if (so instanceof BeaconImpl)
			return (BeaconImpl)so;
		else
			return null;
	}

	/** Lookup an encoder type */
	static protected EncoderTypeImpl lookupEncoderType(String name) {
		SonarObject so = lookupObject(EncoderTypeImpl.SONAR_TYPE, name);
		return (so instanceof EncoderTypeImpl)
		      ? (EncoderTypeImpl) so
		      :	null;
	}

	/** Lookup a camera template */
	static protected CameraTemplateImpl lookupCameraTemplate(String name) {
		SonarObject so = lookupObject(CameraTemplateImpl.SONAR_TYPE, name);
		return (so instanceof CameraTemplateImpl)
				? (CameraTemplateImpl) so
				: null;
	}

	/** Lookup a monitor style */
	static protected MonitorStyleImpl lookupMonitorStyle(String name) {
		SonarObject so = lookupObject(MonitorStyleImpl.SONAR_TYPE,name);
		return (so instanceof MonitorStyleImpl)
		      ? (MonitorStyleImpl) so
		      :	null;
	}

	/** Lookup a camera */
	static protected CameraImpl lookupCamera(String name) {
		SonarObject so = lookupObject(CameraImpl.SONAR_TYPE, name);
		return (so instanceof CameraImpl) ? (CameraImpl) so : null;
	}

	/** Lookup a play list */
	static protected PlayListImpl lookupPlayList(String name) {
		SonarObject so = lookupObject(PlayListImpl.SONAR_TYPE, name);
		return (so instanceof PlayListImpl) ? (PlayListImpl) so : null;
	}

	/** Lookup a camera preset */
	static protected CameraPresetImpl lookupPreset(String name) {
		SonarObject so = lookupObject(CameraPresetImpl.SONAR_TYPE,name);
		if (so instanceof CameraPresetImpl)
			return (CameraPresetImpl)so;
		else
			return null;
	}

	/** Assign a camera preset */
	static protected void assignPreset(CameraPresetImpl op,
		CameraPresetImpl np)
	{
		if (op != null)
			op.setAssignedNotify(false);
		if (np != null)
			np.setAssignedNotify(true);
	}

	/** Lookup a DMS */
	static protected DMSImpl lookupDMS(String name) {
		SonarObject so = lookupObject(DMSImpl.SONAR_TYPE, name);
		return (so instanceof DMSImpl) ? (DMSImpl) so : null;
	}

	/** Lookup a sign config */
	static protected SignConfigImpl lookupSignConfig(String name) {
		SonarObject so = lookupObject(SignConfigImpl.SONAR_TYPE, name);
		return (so instanceof SignConfigImpl)
		      ? (SignConfigImpl) so
		      : null;
	}

	/** Lookup a message pattern */
	static protected MsgPatternImpl lookupMsgPattern(String name) {
		SonarObject so = lookupObject(MsgPatternImpl.SONAR_TYPE, name);
		return (so instanceof MsgPatternImpl)
		      ? (MsgPatternImpl) so
		      : null;
	}

	/** Lookup an LCS */
	static protected LCSImpl lookupLCS(String name) {
		SonarObject so = lookupObject(LCSImpl.SONAR_TYPE, name);
		return (so instanceof LCSImpl) ? (LCSImpl) so : null;
	}

	/** Lookup an incident detail */
	static protected IncidentDetailImpl lookupIncDetail(String name) {
		SonarObject so = lookupObject(IncidentDetailImpl.SONAR_TYPE,
			name);
		return (so instanceof IncidentDetailImpl)
		      ? (IncidentDetailImpl) so
		      : null;
	}

	/** Lookup an incident */
	static protected IncidentImpl lookupIncident(String name) {
		SonarObject so = lookupObject(IncidentImpl.SONAR_TYPE, name);
		return (so instanceof IncidentImpl) ? (IncidentImpl) so : null;
	}

	/** Lookup an action plan */
	static protected ActionPlanImpl lookupActionPlan(String name) {
		SonarObject so = lookupObject(ActionPlanImpl.SONAR_TYPE, name);
		return (so instanceof ActionPlanImpl)
		      ? (ActionPlanImpl) so
		      : null;
	}

	/** Lookup a plan phase */
	static protected PlanPhaseImpl lookupPlanPhase(String name) {
		SonarObject so = lookupObject(PlanPhaseImpl.SONAR_TYPE, name);
		return (so instanceof PlanPhaseImpl) ? (PlanPhaseImpl)so : null;
	}

	/** Lookup a time action */
	static protected TimeActionImpl lookupTimeAction(String name) {
		SonarObject so = lookupObject(TimeActionImpl.SONAR_TYPE, name);
		return (so instanceof TimeActionImpl)
		      ? (TimeActionImpl) so
		      : null;
	}

	/** Lookup a DMS action */
	static protected DmsActionImpl lookupDmsAction(String name) {
		SonarObject so = lookupObject(DmsActionImpl.SONAR_TYPE, name);
		return (so instanceof DmsActionImpl)
		      ? (DmsActionImpl) so
		      : null;
	}

	/** Lookup an alert config */
	static protected AlertConfigImpl lookupAlertConfig(String name) {
		SonarObject so = lookupObject(AlertConfigImpl.SONAR_TYPE, name);
		return (so instanceof AlertConfigImpl)
		      ? (AlertConfigImpl) so
		      : null;
	}

	/** Lookup an alert info */
	static protected AlertInfoImpl lookupAlertInfo(String name) {
		SonarObject so = lookupObject(AlertInfoImpl.SONAR_TYPE, name);
		return (so instanceof AlertInfoImpl)
		      ? (AlertInfoImpl) so
		      : null;
	}

	/** Lookup an alert message */
	static protected AlertMessageImpl lookupAlertMessage(String name) {
		SonarObject so = lookupObject(AlertMessageImpl.SONAR_TYPE,name);
		return (so instanceof AlertMessageImpl)
		      ? (AlertMessageImpl) so
		      : null;
	}

	/** Get the primary key name */
	@Override
	public String getPKeyName() {
		return "name";
	}

	/** Get the primary key */
	@Override
	public String getPKey() {
		return name;
	}

	/** Base object name */
	protected final String name;

	/** Get the object name */
	@Override
	public String getName() {
		return name;
	}

	/** Create a new base object */
	protected BaseObjectImpl(String n) {
		name = n;
	}

	/** Get a string representation of the object */
	@Override
	public final String toString() {
		return name;
	}

	/** Calculate a hash code */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	/** Test if the object equals another */
	@Override
	public boolean equals(Object o) {
		if (o instanceof BaseObjectImpl)
			return name.equals(((BaseObjectImpl) o).name);
		else
			return false;
	}

	/** Store an object */
	public void doStore() throws TMSException {
		store.create(this);
		initTransients();
	}

	/** Destroy an object */
	@Override
	public void destroy() {
		// Handled by doDestroy() method
	}

	/** Destroy an object */
	public void doDestroy() throws TMSException {
		store.destroy(this);
	}

	/** Initialize the transient fields */
	protected void initTransients() throws TMSException {
		// Override this to initialize new objects
	}

	/** Get user for current SONAR message processing */
	public String getProcUser() {
		Server s = MainServer.server;
		if (s != null) {
			String u = s.getProcUser();
			if (u != null)
				return u;
		}
		return BaseHelper.USER_AUTO;
	}

	/** Notify SONAR clients of an object created */
	public void notifyCreate() throws SonarException {
		Server s = MainServer.server;
		if (s != null)
			s.createObject(this);
		else
			namespace.storeObject(this);
	}

	/** Notify SONAR clients of an object removed */
	public void notifyRemove() {
		Server s = MainServer.server;
		if (s != null)
			s.removeObject(this);
	}

	/** Notify SONAR clients of a change to an attribute. Attribute names
	 *  should use lower camel case instead of underscores (e.g.
	 *  "someAttribute" instead of "some_attribute").
	 */
	protected void notifyAttribute(String aname) {
		Server s = MainServer.server;
		if (s != null)
			s.setAttribute(this, aname);
	}

	/** Format a float value */
	static String formatFloat(float value, int digits) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(digits);
		return nf.format(value);
	}

	/** Format a double value */
	static String formatDouble(double value) {
		NumberFormat nf = NumberFormat.getNumberInstance();
		nf.setGroupingUsed(false);
		nf.setMaximumFractionDigits(5);
		return nf.format(value);
	}

	/** Get a String array from a particular column of a ResultSet.
	 * Guards against null pointer exceptions. */
	static protected String[] getStringArray(ResultSet row, int columnIndex)
		throws SQLException
	{
		Array arr = row.getArray(columnIndex);
		return row.wasNull() ? null : (String []) arr.getArray();
	}

	/** Get an array representation of a string given a string array.  If
	 *  the array provided is null, null is returned (instead of the string
	 *  "null" returned by Arrays.toString(). */
	static protected String arrayToString(String[] a) {
		return (a != null)
		      ? Arrays.toString(a).replace("[", "{").replace("]", "}")
		      : null;
	}

	/** Trim and truncate a string to a given maximum length.
	 * @param value String to be truncated (may be null).
	 * @param maxlen Maximum length of string (characters).
	 * @return Trimmed, truncated string, or null. */
	static protected String trimTruncate(String value, int maxlen) {
		if (value != null) {
			String v = value.trim();
			if (v.length() > 0) {
				return (v.length() <= maxlen)
				      ? v
				      : v.substring(0, maxlen);
			}
		}
		return null;
	}

	/** Log an event */
	static public void logEvent(final BaseEvent ev) {
		FLUSH.addJob(new Job() {
			public void perform() throws TMSException {
				ev.doStore();
			}
		});
	}

	/** Query user's permission access level for this object.
	 *
	 * @return Access level (0-4)
	 *         0 none, 1 view, 2 operate, 3 manage, 4 configure */
	public int queryPermAccess() throws TMSException {
		String res = getTypeName();
		String user = getProcUser();
		final int[] access = { 0 };
		store.query("SELECT max(access_n) " +
			"FROM iris.permission p " +
			"JOIN iris.user_id u ON u.role = p.role " +
			"WHERE p.resource_n = '" + res + "' " +
			"AND u.name = '" + user + "' " +
			"AND (" +
				"p.hashtag IS NULL OR " +
				"p.hashtag IN (" +
					"SELECT hashtag " +
					"FROM iris.hashtag " +
					"WHERE resource_n = '" + res + "' " +
					"AND name = '" + getName() + "'" +
				")" +
			");",
			new ResultFactory()
		{
			@Override public void create(ResultSet row)
				throws SQLException
			{
				access[0] = row.getInt(1);
			}
		});
		return access[0];
	}
}
