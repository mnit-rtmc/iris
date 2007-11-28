/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Iterator;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import us.mn.state.dot.tms.log.LogImpl;
import us.mn.state.dot.vault.ObjectVault;
import us.mn.state.dot.vault.ObjectVaultException;

/**
 * The TMSImpl class is an RMI object which contains all the global traffic
 * management system object lists
 *
 * @author Douglas Lau
 */
final class TMSImpl extends TMSObjectImpl implements TMS {

	/** Worker thread */
	static protected final Scheduler TIMER =
		new Scheduler("Scheduler: TIMER");

	/** Detector data flush thread */
	static public final Scheduler FLUSH =
		new Scheduler("Scheduler: FLUSH");

	/** Open the ObjectVault */
	static protected void openVault(Properties props) throws IOException,
		TMSException, ObjectVaultException
	{
		vault = new ObjectVault(
			props.getProperty( "HostName" ),
			props.getProperty( "Port" ),
			props.getProperty( "Database" ),
			props.getProperty( "UserName" ),
			props.getProperty( "Password" )
		);
		vault.addRootTable( UnicastRemoteObject.class );
		vault.enableLogging( false );
		store = new SQLConnection(
			props.getProperty("HostName"),
			props.getProperty("Port"),
			props.getProperty("Database"),
			props.getProperty("UserName"),
			props.getProperty("Password")
		);
		SegmentImpl.mapping = new TableMapping(store, "segment",
			"detector");
		R_NodeImpl.mapping = new TableMapping(store, "r_node",
			"detector");
		TrafficDeviceImpl.plan_mapping = new TableMapping(store,
			"traffic_device", "timing_plan");
	}

	/** Open the EventVault */
	static protected void openEventVault(Properties props)
		throws TMSException
	{
		eventLog = new LogImpl(
			props.getProperty("HostName"),
			props.getProperty("Port"),
			"log", // database name
			props.getProperty("UserName"),
			props.getProperty("Password")
		);
	}

	/** Load the TMS root object from the ObjectVault */
	void loadFromVault(Properties props) throws IOException,
		ObjectVaultException, TMSException
	{
		openVault(props);
		openEventVault(props);
		System.err.println("Loading system-wide policy...");
		policy = new SystemPolicyImpl(store);
		System.err.println( "Loading comm lines..." );
		lines.load( CommunicationLineImpl.class, "index" );
		System.err.println( "Loading node groups..." );
		groups.load( NodeGroupImpl.class, "index" );
		System.err.println("Loading circuits...");
		loadCircuits();
		System.err.println("Loading controllers...");
		loadControllers();
		System.err.println("Loading alarms...");
		loadAlarms();
		System.err.println("Loading detectors...");
		detectors.load(DetectorImpl.class, "index");
		// Roadways must be after detectors until segment lists go away
		System.err.println("Loading roadways...");
		roadways.load(RoadwayImpl.class, "name");
		System.err.println("Loading r_nodes...");
		// NOTE: must be called after detList is populated
		r_nodes.load(R_NodeImpl.class, "vault_oid");
		System.err.println( "Loading meters..." );
		meters.load( RampMeterImpl.class, "id" );
		System.err.println("Loading holidays...");
		holidays.load(MeteringHolidayImpl.class, "name");
		System.err.println( "Loading dms list..." );
		dmss.load( DMSImpl.class, "id" );
		System.err.println("Loading warning signs...");
		warn_signs.load(WarningSignImpl.class, "id");
		System.err.println( "Loading fonts..." );
		fonts.load( PixFontImpl.class, "index" );
		System.err.println( "Loading cameras..." );
		cameras.load( CameraImpl.class, "id" );
		System.err.println( "Loading monitors..." );
		monitors.load( MonitorImpl.class, "name" );
		System.err.println( "Loading tours..." );
		tours.load( TourImpl.class, "name" );
		System.err.println( "Loading lane control signals..." );
		lcss.load( LaneControlSignalImpl.class, "id" );
		devices.addFiltered(cameras);
		devices.addFiltered(lcss);
		devices.addFiltered(meters);
		devices.addFiltered(dmss);
	}

	/** Load all the circuits from the database */
	protected void loadCircuits() throws ObjectVaultException {
		Iterator it = vault.lookup(CircuitImpl.class, "id");
		while(it.hasNext()) {
			CircuitImpl c = (CircuitImpl)vault.load(it.next());
			c.initTransients();
		}
	}

	/** Load all the controllers from the database */
	protected void loadControllers() throws ObjectVaultException {
		Iterator it = vault.lookup(ControllerImpl.class, "circuit");
		while(it.hasNext()) {
			ControllerImpl c = (ControllerImpl)vault.load(
				it.next());
			c.initTransients();
		}
	}

	/** Load all the controller alarms */
	protected void loadAlarms() throws TMSException, ObjectVaultException,
		RemoteException
	{
		Iterator it = vault.lookup(AlarmImpl.class, "vault_oid");
		while(it.hasNext()) {
			AlarmImpl a = (AlarmImpl)vault.load(it.next());
			a.initTransients();
		}
	}

	/** Schedule all repeating jobs */
	public void scheduleJobs() {
		TIMER.addJob(new TimerJobSigns());
		TIMER.addJob(new TimerJob30Sec());
		TIMER.addJob(new TimerJob5Min());
		TIMER.addJob(new Scheduler.Job(Calendar.HOUR, 1) {
			public void perform() {
				lines.poll1Hour();
				System.out.println(new Date());
				Profile.printMemory(System.out);
				Profile.printThreads(System.out);
			}
		} );
		TIMER.addJob(new Scheduler.Job(Calendar.DATE, 1) {
			public void perform() {
				lines.poll1Day();
			}
		} );
		TIMER.addJob(new Scheduler.Job(Calendar.DATE, 1,
			Calendar.HOUR, 20)
		{
			public void perform() throws IOException {
				writeXmlConfiguration();
			}
		});
		TIMER.addJob(new Scheduler.Job(Calendar.DATE, 1,
			Calendar.HOUR, 4)
		{
			public void perform() {
				System.err.println( "Performing download to"
					+ " all controllers @ " + new Date() );
				lines.download();
			}
		} );
		TIMER.addJob(new Scheduler.Job(500) {
			public void perform() {
				System.err.println( "Performing download to"
					+ " all controllers @ " + new Date() );
				lines.download();
			}
		} );
		TIMER.addJob(new Scheduler.Job(1000) {
			public void perform() throws IOException {
				writeXmlConfiguration();
			}
		});
	}

	/** Write the TMS xml configuration files */
	protected void writeXmlConfiguration() throws IOException {
		System.err.println("Writing TMS XML files @ " + new Date());
		detectors.writeXml();
		meters.writeXml();
		r_nodes.writeXml();
		System.err.println("Completed TMS XML dump @ " + new Date());
	}

	/** Calculate the 30-second interval for the given time stamp */
	static protected int calculateInterval(Calendar stamp) {
		return stamp.get(Calendar.HOUR_OF_DAY) * 120 +
			stamp.get(Calendar.MINUTE) * 2 +
			stamp.get(Calendar.SECOND) / 30;
	}

	/** Sign polling timer job */
	protected class TimerJobSigns extends Scheduler.Job {

		/** Job completer */
		protected final Completer comp;

		/** Job to be performed on each completion */
		protected final Scheduler.Job job = new Scheduler.Job(0) {
			public void perform() {
				dmss.notifyStatus();
				warn_signs.notifyStatus();
				lcss.notifyStatus();
			}
		};

		/** Create a new sign polling timer job */
		protected TimerJobSigns() {
			super(Calendar.SECOND, 30, Calendar.SECOND, 4);
			comp = new Completer("Sign Poll", TIMER, job);
		}

		/** Perform the sign poll job */
		public void perform() {
			if(!comp.checkComplete())
				return;
			comp.reset(Calendar.getInstance());
			lines.pollSigns(comp);
			comp.makeReady();
		}
	}

	/** 30-second timer job */
	protected class TimerJob30Sec extends Scheduler.Job {

		/** Job completer */
		protected final Completer comp;

		/** Current time stamp */
		protected Calendar stamp;

		/** Job to be performed on completion */
		protected final Scheduler.Job job = new Scheduler.Job(0) {
			public void perform() {
				detectors.calculateFakeFlows();
				detectors.writeSampleXml();
				stations.calculateData(stamp);
				station_map.calculateData(stamp);
				station_map.writeSampleXml();
				station_map.writeStationXml();
				int interval = calculateInterval(stamp);
				meters.computeDemand(interval);
				if(holidays.allowMetering(stamp)) {
					meters.validateTimingPlans(interval);
					dmss.updateTravelTimes(interval);
				}
				meters.notifyStatus();
				groups.notifyStatus();
				lines.notifyStatus();
			}
		};

		/** Create a new 30-second timer job */
		protected TimerJob30Sec() {
			super(Calendar.SECOND, 30, Calendar.SECOND, 8);
			comp = new Completer("30-Second", TIMER, job);
		}

		/** Perform the 30-second timer job */
		public void perform() {
			if(!comp.checkComplete())
				return;
			Calendar s = Calendar.getInstance();
			s.add(Calendar.SECOND, -30);
			stamp = s;
			comp.reset(stamp);
			lines.poll30Second(comp);
			comp.makeReady();
		}
	}

	/** 5-minute timer job */
	protected class TimerJob5Min extends Scheduler.Job {

		/** Job completer */
		protected final Completer comp;

		/** Current time stamp */
		protected Calendar stamp;

		/** Job to be performed on completion */
		protected final Scheduler.Job job = new Scheduler.Job(500) {
			public void perform() {
				detectors.flush(stamp);
			}
		};

		/** Create a new 5-minute timer job */
		protected TimerJob5Min() {
			super(Calendar.MINUTE, 5, Calendar.SECOND, 12);
			comp = new Completer("5-Minute", FLUSH, job);
		}

		/** Perform the 5-minute timer job */
		public void perform() {
			if(!comp.checkComplete()) {
				lines.print(System.err);
				return;
			}
			stamp = Calendar.getInstance();
			Calendar s = (Calendar)stamp.clone();
			s.add(Calendar.MINUTE, -5);
			comp.reset(s);
			lines.poll5Minute(comp);
			comp.makeReady();
		}
	}

	/** Master communication line list */
	protected final CommunicationLineList lines;

	/** Master node group list */
	protected final NodeGroupList groups;

	/** Master roadway list */
	protected final RoadwayListImpl roadways;

	/** Master detector list */
	protected final DetectorListImpl detectors;

	/** Master station list */
	protected final StationListImpl stations;

	/** Master station list */
	protected final StationMapImpl station_map;

	/** Master segment map */
	protected final SegmentMapImpl segments;

	/** Master r_node map */
	protected final R_NodeMapImpl r_nodes;

	/** Master timing plan list */
	protected final TimingPlanListImpl plans;

	/** Master ramp meter list */
	protected final RampMeterListImpl meters;

	/** Master metering holiday list */
	protected final MeteringHolidayListImpl holidays;

	/** Dynamic message sign list */
	protected final DMSListImpl dmss;

	/** Warning sign list */
	protected final WarningSignList warn_signs;

	/** Pixel font list */
	protected final PixFontList fonts;

	/** Camera list */
	protected final CameraList cameras;

	/** Monitor list */
	protected final MonitorListImpl monitors;

	/** Tour list */
	protected final TourListImpl tours;

	/** Lane Control Signals list */
	protected final LCSListImpl lcss;

	/** Available device list */
	protected transient SubsetList devices;

	/** Initialize the subset list */
	protected void initialize() throws RemoteException {
		devices = new SubsetList( new DeviceFilter() );

		// This is an ugly hack, but it works
		lineList = lines;
		groupList = groups;
		roadList = roadways;
		detList = detectors;
		statList = stations;
		statMap = station_map;
		segMap = segments;
		nodeMap = r_nodes;
		planList = plans;
		meterList = meters;
		dmsList = dmss;
		warnList = warn_signs;
		deviceList = devices;
		availableMeters = (SubsetList)meters.getAvailableList();
		cameraList = cameras;
		lcsList = lcss;
	}

	/** Create a new TMS object */
	TMSImpl() throws TMSException, RemoteException {
		super();
		lines = new CommunicationLineList();
		groups = new NodeGroupList();
		roadways = new RoadwayListImpl();
		detectors = new DetectorListImpl();
		stations = new StationListImpl();
		station_map = new StationMapImpl();
		segments = new SegmentMapImpl();
		r_nodes = new R_NodeMapImpl();
		plans = new TimingPlanListImpl();
		meters = new RampMeterListImpl();
		holidays = new MeteringHolidayListImpl();
		dmss = new DMSListImpl();
		warn_signs = new WarningSignList();
		fonts = new PixFontList();
		cameras = new CameraList();
		monitors = new MonitorListImpl();
		tours = new TourListImpl();
		lcss = new LCSListImpl();
		initialize();
	}

	/** Get the system-wide policy */
	public SystemPolicy getPolicy() {
		return policy;
	}

	/** Get the communication line list */
	public IndexedList getLineList() { return lines; }

	/** Get the node group list */
	public IndexedList getNodeGroupList() { return groups; }

	/** Get the roadway list */
	public RoadwayList getRoadwayList() { return roadways; }

	/** Get the detector list */
	public IndexedList getDetectorList() { return detectors; }

	/** Get the station list */
	public StationList getStationList() { return stations; }

	/** Get the station map */
	public StationMap getStationMap() {
		return station_map;
	}

	/** Get the segment map */
	public SegmentMap getSegmentMap() {
		return segments;
	}

	/** Get the r_node map */
	public R_NodeMap getR_NodeMap() {
		return r_nodes;
	}

	/** Get the timing plan list */
	public TimingPlanList getTimingPlanList() { return plans; }

	/** Get the ramp meter list */
	public RampMeterList getRampMeterList() { return meters; }

	/** Get the metering holiday list */
	public MeteringHolidayList getMeteringHolidayList() { return holidays; }

	/** Get the dynamic message sign list */
	public DMSList getDMSList() { return dmss; }

	/** Get the warning sign list */
	public SortedList getWarningSignList() { return warn_signs; }

	/** Get the pixel font list */
	public IndexedList getFontList() { return fonts; }

	/** Get the camera list */
	public SortedList getCameraList() { return cameras; }

	/** Get the monitor list */
	public SortedList getMonitorList() { return monitors; }

	/** Get the tour list */
	public SortedList getTourList() { return tours; }
	
	/** Get the lane control signal list */
	public SortedList getLCSList() { return lcss; }

	/** Get the available device list */
	public SortedList getDeviceList() { return devices; }

	/** Get a TMS object by its object ID */
	public TMSObject getObject(int oid) {
		Object obj = vault.getObject(oid);
		if(obj instanceof TMSObject)
			return (TMSObject)obj;
		else
			return null;
	}
}
