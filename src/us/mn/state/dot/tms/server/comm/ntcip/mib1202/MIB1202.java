/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1202;

import us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.MIBNode;

/**
 * MIB nodes for NTCIP 1202
 *
 * @author Douglas Lau
 */
public enum MIB1202 {
	asc				(MIB1201.devices, 1),
	phase				(asc, 1),
	  maxPhases			(phase, 1),
	  phaseTable			(phase, 2),
	  phaseEntry			(phaseTable, 1),
	    phaseNumber			(phaseEntry, 1),
	    phaseWalk			(phaseEntry, 2),
	    phasePedestrianClear	(phaseEntry, 3),
	    phaseMinimumGreen		(phaseEntry, 4),
	    phasePassage		(phaseEntry, 5),
	    phaseMaximum1		(phaseEntry, 6),
	    phaseMaximum2		(phaseEntry, 7),
	    phaseYellowChange		(phaseEntry, 8),
	    phaseRedClear		(phaseEntry, 9),
	    phaseRedRevert		(phaseEntry, 10),
	    phaseAddedInitial		(phaseEntry, 11),
	    phaseMaximumInitial		(phaseEntry, 12),
	    phaseTimeBeforeReduction	(phaseEntry, 13),
	    phaseCarsBeforeReduction	(phaseEntry, 14),
	    phaseTimeToReduce		(phaseEntry, 15),
	    phaseReduceBy		(phaseEntry, 16),
	    phaseMinimumGap		(phaseEntry, 17),
	    phaseDynamicMaxLimit	(phaseEntry, 18),
	    phaseDynamicMaxStep		(phaseEntry, 19),
	    phaseStartup		(phaseEntry, 20),
	    phaseOptions		(phaseEntry, 21),
	    phaseRing			(phaseEntry, 22),
	    phaseConcurrency		(phaseEntry, 23),
	  maxPhaseGroups		(phase, 3),
	  phaseStatusGroupTable		(phase, 4),
	  phaseStatusGroupEntry		(phaseStatusGroupTable, 1),
	    phaseStatusGroupNumber	(phaseStatusGroupEntry, 1),
	    phaseStatusGroupReds	(phaseStatusGroupEntry, 2),
	    phaseStatusGroupYellows	(phaseStatusGroupEntry, 3),
	    phaseStatusGroupGreens	(phaseStatusGroupEntry, 4),
	    phaseStatusGroupDontWalks	(phaseStatusGroupEntry, 5),
	    phaseStatusGroupPedClears	(phaseStatusGroupEntry, 6),
	    phaseStatusGroupWalks	(phaseStatusGroupEntry, 7),
	    phaseStatusGroupVehCalls	(phaseStatusGroupEntry, 8),
	    phaseStatusGroupPedCalls	(phaseStatusGroupEntry, 9),
	    phaseStatusGroupPhaseOns	(phaseStatusGroupEntry, 10),
	    phaseStatusGroupPhaseNexts	(phaseStatusGroupEntry, 11),
	  phaseControlGroupTable	(phase, 5),
	  phaseControlGroupEntry	(phaseControlGroupTable, 1),
	    phaseControlGroupNumber	(phaseControlGroupEntry, 1),
	    phaseControlGroupPhaseOmit	(phaseControlGroupEntry, 2),
	    phaseControlGroupPedOmit	(phaseControlGroupEntry, 3),
	    phaseControlGroupHold	(phaseControlGroupEntry, 4),
	    phaseControlGroupForceOff	(phaseControlGroupEntry, 5),
	    phaseControlGroupVehCall	(phaseControlGroupEntry, 6),
	    phaseControlGroupPedCall	(phaseControlGroupEntry, 7),
	detector				(asc, 2),
	  maxVehicleDetectors			(detector, 1),
	  vehicleDetectorTable			(detector, 2),
	  vehicleDetectorEntry			(vehicleDetectorTable, 1),
	    vehicleDetectorNumber		(vehicleDetectorEntry, 1),
	    vehicleDetectorOptions		(vehicleDetectorEntry, 2),
	    vehicleDetectorCallPhase		(vehicleDetectorEntry, 4),
	    vehicleDetectorSwitchPhase		(vehicleDetectorEntry, 5),
	    vehicleDetectorDelay		(vehicleDetectorEntry, 6),
	    vehicleDetectorExtend		(vehicleDetectorEntry, 7),
	    vehicleDetectorQueueLimit		(vehicleDetectorEntry, 8),
	    vehicleDetectorNoActivity		(vehicleDetectorEntry, 9),
	    vehicleDetectorMaxPresence		(vehicleDetectorEntry, 10),
	    vehicleDetectorErraticCounts	(vehicleDetectorEntry, 11),
	    vehicleDetectorFailTime		(vehicleDetectorEntry, 12),
	    vehicleDetectorAlarms		(vehicleDetectorEntry, 13),
	    vehicleDetectorReportedAlarms	(vehicleDetectorEntry, 14),
	    vehicleDetectorReset		(vehicleDetectorEntry, 15),
	  maxVehicleDetectorStatusGroups	(detector, 3),
	  vehicleDetectorStatusGroupTable	(detector, 4),
	  vehicleDetectorStatusGroupEntry	(vehicleDetectorStatusGroupTable, 1),
	    vehicleDetectorStatusGroupNumber	(vehicleDetectorStatusGroupEntry, 1),
	    vehicleDetectorStatusGroupActive	(vehicleDetectorStatusGroupEntry, 2),
	    vehicleDetectorStatusGroupAlarms	(vehicleDetectorStatusGroupEntry, 3),
	  volumeOccupancyReport			(detector, 5),
	    volumeOccupancySequence		(volumeOccupancyReport, 1),
	    volumeOccupancyPeriod		(volumeOccupancyReport, 2),
	    activeVolumeOccupancyDetectors	(volumeOccupancyReport, 3),
	    volumeOccupancyTable		(volumeOccupancyReport, 4),
	    volumeOccupancyEntry		(volumeOccupancyTable, 1),
	      detectorVolume			(volumeOccupancyEntry, 1),
	      detectorOccupancy			(volumeOccupancyEntry, 2),
	  maxPedestrianDetectors		(detector, 6),
	  pedestrianDetectorTable		(detector, 7),
	  pedestrianDetectorEntry		(pedestrianDetectorTable, 1),
	    pedestrianDetectorNumber		(pedestrianDetectorEntry, 1),
	    pedestrianDetectorCallPhase		(pedestrianDetectorEntry, 2),
	    pedestrianDetectorNoActivity	(pedestrianDetectorEntry, 3),
	    pedestrianDetectorMaxPresence	(pedestrianDetectorEntry, 4),
	    pedestrianDetectorErraticCounts	(pedestrianDetectorEntry, 5),
	    pedestrianDetectorAlarms		(pedestrianDetectorEntry, 6);
	// FIXME: add unit OIDs
	// FIXME: add coord OIDs
	// FIXME: add timebase OIDs
	// FIXME: add preempt OIDs
	// FIXME: add ring OIDs
	// FIXME: add channel OIDs
	// FIXME: add overlap OIDs
	// FIXME: add ts2port1 OIDs

	/** MIB node */
	public final MIBNode node;

	/** Create a node with MIB1201 parent */
	private MIB1202(MIB1201 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Create a new MIB1202 node */
	private MIB1202(MIB1202 p, int n) {
		node = p.node.child(n, toString());
	}

	/** Make an integer */
	public ASN1Integer makeInt() {
		return new ASN1Integer(node);
	}

	/** Make an integer */
	public ASN1Integer makeInt(int r) {
		return new ASN1Integer(node, r);
	}

	/** Make an integer */
	public ASN1Integer makeInt(int r, int s) {
		return new ASN1Integer(node, r, s);
	}
}
